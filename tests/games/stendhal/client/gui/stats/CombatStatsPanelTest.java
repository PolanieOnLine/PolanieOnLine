package games.stendhal.client.gui.stats;

import static org.junit.Assert.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.lang.reflect.Field;
import java.util.*;
import javax.swing.*;
import org.junit.*;
import games.stendhal.client.gui.InternalManagedWindow;
import games.stendhal.client.gui.UiTranslation;
import games.stendhal.client.gui.styled.StyledButtonUI;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import games.stendhal.client.gui.wt.core.WtWindowManager;

public class CombatStatsPanelTest {
	private Field preferences;
	private Object previous;
	private CombatStatsPanel panel;
	private LookAndFeel previousLook;
	private Object previousStyle;
	@Before public void isolatePreferences() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties"); preferences.setAccessible(true);
		previous = preferences.get(WtWindowManager.getInstance());
		preferences.set(WtWindowManager.getInstance(), new Properties());
		previousLook = UIManager.getLookAndFeel(); previousStyle = UIManager.get("StendhalStyle");
		SwingUtilities.invokeAndWait(() -> {
			try { UIManager.setLookAndFeel(new StyledLookAndFeel(StyleFactory.createStyle("Drewno (default)"))); }
			catch (Exception e) { throw new AssertionError(e); }
			panel = new CombatStatsPanel();
		});
	}
	@After public void restore() throws Exception {
		SwingUtilities.invokeAndWait(() -> panel.setSnapshot(Collections.emptyMap()));
		preferences.set(WtWindowManager.getInstance(), previous);
		UIManager.setLookAndFeel(previousLook); UIManager.put("StendhalStyle", previousStyle);
	}
	@Test public void accessIsOffByDefaultAndButtonUsesClientSkin() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			panel.setSnapshot(snapshot("35")); assertFalse("Stats access must be opt-in", panel.isVisible());
			enable(); assertTrue(panel.isVisible());
			JButton button = find(panel, JButton.class); assertNotNull("Use a regular client button", button);
			assertTrue(button.getUI() instanceof StyledButtonUI);
			assertNull("Rows must not be inside the sidebar", find(panel, JTextArea.class));
		});
	}
	@Test public void buttonOpensOverlayWithoutGrowingSidebarAndDisablingClosesIt() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable(); panel.setSnapshot(snapshot("35"));
			InternalManagedWindow window = window();
			JLayeredPane game = new JLayeredPane(); game.setSize(800, 600);
			game.add(panel); panel.setBounds(0, 30, 180, 30);
			game.add(window, JLayeredPane.PALETTE_LAYER); window.setVisible(false);
			Dimension before = panel.getPreferredSize();
			JButton button = find(panel, JButton.class); button.doClick();
			assertTrue(window.isVisible()); assertSame(game, window.getParent());
			assertTrue(window.getX() >= panel.getX() + panel.getWidth());
			assertTrue(window.getX() + window.getWidth() <= game.getWidth());
			assertTrue(window.getY() + window.getHeight() <= game.getHeight());
			assertEquals(before, panel.getPreferredSize());
			assertEquals("Odporność na krwawienie: 35%", find(window, JTextArea.class).getText());
			button.doClick(); assertFalse(window.isVisible()); button.doClick(); assertTrue(window.isVisible());
			WtWindowManager.getInstance().setProperty("ui.show_combat_stats", "false");
			assertFalse(panel.isVisible()); assertFalse(window.isVisible());
		});
	}
	@Test public void limitsRefreshOverlayAndNeverChangeSourceValues() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable(); Map<String,String> source = snapshot("35"); panel.setSnapshot(source);
			JTextArea rows = find(window(), JTextArea.class);
			WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "true");
			assertEquals("Odporność na krwawienie: 35%\u00a0/\u00a060%", rows.getText());
			WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "false");
			assertEquals("Odporność na krwawienie: 35%", rows.getText()); assertEquals("35", source.get("resist_bleeding"));
		});
	}
	@Test public void logoutOrInvalidDataClearsAndClosesOverlay() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable();
			JLayeredPane game = new JLayeredPane(); game.setSize(800, 600);
			game.add(panel); panel.setBounds(0, 30, 180, 30);
			game.add(window(), JLayeredPane.PALETTE_LAYER); window().setVisible(false);
			for (Map<String,String> source : Arrays.asList(Collections.<String,String>emptyMap(), snapshot("NaN"), snapshot("wrong"))) {
				panel.setSnapshot(snapshot("35")); assertTrue(panel.isVisible());
				find(panel, JButton.class).doClick(); assertTrue(window().isVisible()); panel.setSnapshot(source);
				assertFalse(panel.isVisible()); assertFalse(window().isVisible());
				assertEquals("", find(window(), JTextArea.class).getText());
			}
		});
	}
	@Test public void languageChangesUpdateDetachedOverlay() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable(); panel.setSnapshot(snapshot("35.5"));
			WtWindowManager.getInstance().setProperty("ui.language", "en"); UiTranslation.apply(panel);
			assertEquals("Bleeding resistance: 35.5%", find(window(), JTextArea.class).getText());
			WtWindowManager.getInstance().setProperty("ui.language", "pl"); UiTranslation.apply(panel);
			assertEquals("Odporność na krwawienie: 35,5%", find(window(), JTextArea.class).getText());
		});
	}
	private void enable() { WtWindowManager.getInstance().setProperty("ui.show_combat_stats", "true"); }
	private InternalManagedWindow window() {
		try {
			for (Field field : CombatStatsPanel.class.getDeclaredFields()) {
				if (InternalManagedWindow.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true); return (InternalManagedWindow) field.get(panel);
				}
			}
		} catch (Exception e) { throw new AssertionError(e); }
		throw new AssertionError("Stats require a standard internal client window");
	}
	private Map<String,String> snapshot(String value) {
		Map<String,String> result = new LinkedHashMap<>(); result.put("resist_bleeding", value);
		result.put("resist_bleeding.limit", "60"); return result;
	}
	private static <T extends Component> T find(Container root, Class<T> type) {
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) return type.cast(child);
			if (child instanceof Container) { T found = find((Container)child, type); if (found != null) return found; }
		}
		return null;
	}
}
