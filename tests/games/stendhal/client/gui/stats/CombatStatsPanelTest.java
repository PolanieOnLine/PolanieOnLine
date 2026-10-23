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
import games.stendhal.common.constants.CombatStat;

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
			assertEquals("Statystyki", button.getText());
			assertEquals("Do not stack a panel frame around the button", 0, panel.getInsets().top);
			assertEquals(1, panel.getComponentCount());
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
			assertArrayEquals(new String[] {"Odporność na krwawienie:", "35%"}, cells());
			button.doClick(); assertFalse(window.isVisible()); button.doClick(); assertTrue(window.isVisible());
			WtWindowManager.getInstance().setProperty("ui.show_combat_stats", "false");
			assertFalse(panel.isVisible()); assertFalse(window.isVisible());
		});
	}
	@Test public void limitsRefreshOverlayAndNeverChangeSourceValues() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable(); Map<String,String> source = snapshot("35"); panel.setSnapshot(source);
			WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "true");
			assertArrayEquals(new String[] {"Odporność na krwawienie:", "35%", "/ 60%"}, cells());
			WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "false");
			assertArrayEquals(new String[] {"Odporność na krwawienie:", "35%"}, cells()); assertEquals("35", source.get("resist_bleeding"));
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
				assertEquals(0, cells().length);
			}
		});
	}
	@Test public void languageChangesUpdateDetachedOverlay() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable(); panel.setSnapshot(snapshot("35.5"));
			WtWindowManager.getInstance().setProperty("ui.language", "en"); UiTranslation.apply(panel);
			assertArrayEquals(new String[] {"Bleeding resistance:", "35.5%"}, cells());
			WtWindowManager.getInstance().setProperty("ui.language", "pl"); UiTranslation.apply(panel);
			assertArrayEquals(new String[] {"Odporność na krwawienie:", "35,5%"}, cells());
		});
	}
	private void enable() { WtWindowManager.getInstance().setProperty("ui.show_combat_stats", "true"); }
	private JPanel rows() {
		try { Field field = CombatStatsPanel.class.getDeclaredField("rows"); field.setAccessible(true); return (JPanel)field.get(panel); }
		catch (Exception e) { throw new AssertionError(e); }
	}
	private String[] cells() {
		return Arrays.stream(rows().getComponents()).map(c -> ((JLabel)c).getText()).toArray(String[]::new);
	}
	@Test public void valuesAndLimitsAlignAndWindowFitsContentBeforeFirstLayout() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable(); WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "true");
			Map<String,String> source = snapshot("35");
			source.put("critical_chance", "9.5"); source.put("critical_chance.limit", "50");
			panel.setSnapshot(source);
			InternalManagedWindow overlay = window(); JLayeredPane host = new JLayeredPane(); host.setSize(800, 600);
			host.add(panel); panel.setBounds(0, 20, 180, 30); host.add(overlay, JLayeredPane.PALETTE_LAYER); overlay.setVisible(false);
			find(panel, JButton.class).doClick(); layout(overlay);
			assertTrue("Width should follow the longest row, not a fixed 330px", overlay.getWidth() < 330);
			assertTrue("Two rows must not leave a large empty window", overlay.getHeight() < 100);
			Component[] row = rows().getComponents();
			assertEquals(row[1].getX() + row[1].getWidth(), row[4].getX() + row[4].getWidth());
			assertEquals(row[2].getX() + row[2].getWidth(), row[5].getX() + row[5].getWidth());
			Dimension full = overlay.getSize(); panel.setSnapshot(snapshot("35")); layout(overlay);
			assertTrue("Removing rows should shrink the open window", overlay.getHeight() < full.height);
		});
	}
	private static void layout(Container root) {
		root.doLayout(); for (Component child : root.getComponents()) if (child instanceof Container) layout((Container)child);
	}
	@Test public void fullSnapshotFitsWithoutBlankSpaceAndNarrowHostRemainsScrollable() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			enable(); WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "true");
			Map<String,String> source = new LinkedHashMap<>();
			for (CombatStat stat : CombatStat.values()) { source.put(stat.getKey(), "13"); source.put(stat.getLimitKey(), "60"); }
			panel.setSnapshot(source);
			source.put("critical_chance", "14"); panel.setSnapshot(source);
			InternalManagedWindow overlay = window(); JLayeredPane host = new JLayeredPane(); host.setSize(800, 600);
			host.add(panel); panel.setBounds(0, 20, 180, 30); host.add(overlay, JLayeredPane.PALETTE_LAYER); overlay.setVisible(false);
			find(panel, JButton.class).doClick(); layout(overlay);
			assertTrue("First opening must fit the actual nine-row grid", overlay.getHeight() <= rows().getPreferredSize().height + 30);
			JScrollPane viewport = find(overlay, JScrollPane.class);
			assertFalse(viewport.getVerticalScrollBar().isVisible()); assertFalse(viewport.getHorizontalScrollBar().isVisible());
			int withLimits = overlay.getWidth(); WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "false"); layout(overlay);
			assertTrue("Turning limits off should release their column width", overlay.getWidth() < withLimits);
			host.setSize(180, 100); host.dispatchEvent(new java.awt.event.ComponentEvent(host, java.awt.event.ComponentEvent.COMPONENT_RESIZED));
			layout(overlay);
			assertTrue(overlay.getWidth() <= host.getWidth()); assertTrue(overlay.getHeight() <= host.getHeight());
			assertTrue(viewport.getVerticalScrollBar().isVisible()); assertTrue(viewport.getHorizontalScrollBar().isVisible());
		});
	}
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
