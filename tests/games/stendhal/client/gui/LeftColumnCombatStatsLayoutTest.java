package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import javax.swing.*;
import org.junit.Test;
import games.stendhal.client.MockStendhalClient;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.map.MapPanelController;
import games.stendhal.client.gui.stats.StatsPanelController;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.CombatStat;

public class LeftColumnCombatStatsLayoutTest {
	private JScrollPane dockUnderTest;
	private JTextArea rowsUnderTest;
	private JTabbedPane tabsUnderTest;
	private JToggleButton toggleUnderTest;

	@Test public void naturalWindowValidationRecalculatesWrappedHeightAfterNarrowing() throws Exception {
		org.junit.Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		checkDockAndKeepFixture();
		JFrame[] window = new JFrame[1];
		try {
			SwingUtilities.invokeAndWait(() -> {
				toggleUnderTest.doClick();
				window[0] = new JFrame(); window[0].setContentPane(dockUnderTest); window[0].pack();
			});
			for (int width : new int[] {180, 600, 180}) {
				SwingUtilities.invokeAndWait(() -> window[0].setSize(width, 360));
				// Let resize notifications and normal deferred revalidation complete.
				for (int i = 0; i < 6; i++) SwingUtilities.invokeAndWait(() -> window[0].validate());
				SwingUtilities.invokeAndWait(() -> {
					assertTrue("Natural validation must not clip wrapped statistics",
							rowsUnderTest.getHeight() >= rowsUnderTest.getPreferredSize().height);
					dockUnderTest.getVerticalScrollBar().setValue(dockUnderTest.getVerticalScrollBar().getMaximum());
					assertTrue(tabsUnderTest.getVisibleRect().height > 0);
				});
			}
		} finally {
			SwingUtilities.invokeAndWait(() -> {
				if (toggleUnderTest.isSelected()) toggleUnderTest.doClick();
				if (window[0] != null) window[0].dispose();
			});
		}
	}

	@Test public void shortDockKeepsAllStatisticsAndTabsReachableWhenResized() throws Exception {
		checkDockAndKeepFixture();
	}

	private void checkDockAndKeepFixture() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			LookAndFeel originalStyle = UIManager.getLookAndFeel();
			Object originalTheme = UIManager.get("StendhalStyle");
			try {
				Field preferences = WtWindowManager.class.getDeclaredField("properties");
				preferences.setAccessible(true);
				Object originalPreferences = preferences.get(WtWindowManager.getInstance());
				try {
					preferences.set(WtWindowManager.getInstance(), new Properties());
					UIManager.setLookAndFeel(new StyledLookAndFeel(StyleFactory.createStyle("Drewno (default)")));
					JComponent column = SwingClientGUI.createLeftColumn();
					column.add(new MapPanelController(new MockStendhalClient() {}).getComponent(), SLayout.EXPAND_X);
					JComponent stats = (JComponent) StatsPanelController.get().getComponent();
					Method money = stats.getClass().getDeclaredMethod("setMoney", int.class, int.class, int.class, int.class);
					money.setAccessible(true); money.invoke(stats, 0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
					Field combatField = stats.getClass().getDeclaredField("combatStatsPanel");
					combatField.setAccessible(true);
					JComponent combat = (JComponent) combatField.get(stats);
					Method update = combat.getClass().getDeclaredMethod("setSnapshot", Map.class);
					update.setAccessible(true);
					Map<String, String> snapshot = new LinkedHashMap<>();
					for (CombatStat stat : CombatStat.values()) {
						snapshot.put(stat.getKey(), "35"); snapshot.put(stat.getLimitKey(), "60");
					}
					update.invoke(combat, snapshot);
					JToggleButton toggle = find(combat, JToggleButton.class);
					toggle.doClick();
					column.add(stats, SLayout.EXPAND_X);
					JTabbedPane tabs = new JTabbedPane(SwingConstants.BOTTOM);
					tabs.add("Przyjaciele", new JLabel("Przyjaciel")); tabs.add("Grupa", new JLabel("Gracz"));
					column.add(tabs, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
					JScrollPane scroll = SwingClientGUI.createLeftColumnScrollPane(column);
					JTextArea rows = find(combat, JTextArea.class);
					dockUnderTest = scroll; rowsUnderTest = rows; tabsUnderTest = tabs; toggleUnderTest = toggle;
					int minimumWidth = column.getMinimumSize().width;
					for (boolean limits : new boolean[] {false, true}) {
						WtWindowManager.getInstance().setProperty("ui.show_stat_limits", Boolean.toString(limits));
						for (int width : new int[] {180, 600, 180}) {
							scroll.setSize(width, 320); settle(scroll);
							assertTrue("Statistics must not be clipped", rows.getHeight() >= rows.getPreferredSize().height);
							assertTrue("Tabs must keep their usable height", tabs.getHeight() >= tabs.getPreferredSize().height);
							assertEquals(minimumWidth, column.getMinimumSize().width);
							assertEquals(scroll.getViewport().getWidth(), column.getWidth());
							assertTrue("Test must include the maximum money amount", assertMoneyFits(stats));
							if (width == 180) {
								assertTrue("Overflow must be scrollable", scroll.getVerticalScrollBar().isVisible());
								scroll.getVerticalScrollBar().setValue(scroll.getVerticalScrollBar().getMaximum());
								assertTrue("Bottom tabs must be reachable", tabs.getVisibleRect().height > 0);
							}
						}
					}
					scroll.setSize(180, 900); settle(scroll);
					assertFalse("Tall dock should not need scrolling", scroll.getVerticalScrollBar().isVisible());
					toggle.doClick(); scroll.setSize(180, 400); settle(scroll);
					assertFalse("Collapsed statistics should release their space", scroll.getVerticalScrollBar().isVisible());
					assertTrue(tabs.getVisibleRect().height > 0);
				} finally { preferences.set(WtWindowManager.getInstance(), originalPreferences); }
			} catch (Exception e) { throw new AssertionError(e); }
			finally {
				try { UIManager.setLookAndFeel(originalStyle); UIManager.put("StendhalStyle", originalTheme); }
				catch (Exception e) { throw new AssertionError(e); }
			}
		});
	}

	private static void settle(Container root) {
		for (int i = 0; i < 6; i++) layout(root);
	}
	private static boolean assertMoneyFits(Container root) {
		boolean found = false;
		for (Component child : root.getComponents()) {
			if (child instanceof JLabel && "2147483647".equals(((JLabel) child).getText())) {
				found = true;
				assertTrue("Full money amount must fit", child.getWidth() >= child.getPreferredSize().width);
				for (Component sibling : child.getParent().getComponents()) {
					assertTrue("Money must not extend outside the dock", sibling.getX() + sibling.getWidth() <= child.getParent().getWidth());
				}
			}
			if (child instanceof Container) found |= assertMoneyFits((Container) child);
		}
		return found;
	}
	private static void layout(Container root) {
		root.invalidate(); root.doLayout();
		for (Component child : root.getComponents()) if (child instanceof Container) layout((Container) child);
		root.invalidate();
	}
	private static <T extends Component> T find(Container root, Class<T> type) {
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) return type.cast(child);
			if (child instanceof Container) {
				T found = find((Container) child, type); if (found != null) return found;
			}
		}
		return null;
	}
}
