package games.stendhal.client.gui;

import static org.junit.Assert.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import javax.swing.*;
import org.junit.Test;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.gui.layout.SLayout;
import games.stendhal.client.gui.stats.StatsPanelController;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.common.constants.CombatStat;

public class LeftColumnCombatStatsLayoutTest {
	@Test public void statsOpenOverGameWithoutMovingBuddyTabsAndFitAfterResize() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			try {
				Field properties = WtWindowManager.class.getDeclaredField("properties"); properties.setAccessible(true);
				Object oldProperties = properties.get(WtWindowManager.getInstance());
				try {
					Properties preferences = new Properties(); preferences.setProperty("config.ui.show_combat_stats", "true");
					properties.set(WtWindowManager.getInstance(), preferences);
					JComponent stats = StatsPanelController.get().getComponent();
					Field field = stats.getClass().getDeclaredField("combatStatsPanel"); field.setAccessible(true);
					JComponent launcher = (JComponent) field.get(stats);
					Method update = launcher.getClass().getDeclaredMethod("setSnapshot", Map.class); update.setAccessible(true);
					Map<String,String> values = new LinkedHashMap<>();
					for (CombatStat stat : CombatStat.values()) { values.put(stat.getKey(), "35"); values.put(stat.getLimitKey(), "60"); }
					update.invoke(launcher, values);
					Field overlayField = launcher.getClass().getDeclaredField("window"); overlayField.setAccessible(true);
					InternalManagedWindow overlay = (InternalManagedWindow) overlayField.get(launcher);
					JComponent column = SwingClientGUI.createLeftColumn(); column.add(stats, SLayout.EXPAND_X);
					JTabbedPane tabs = new JTabbedPane(); tabs.add("Przyjaciele", new JLabel("Przyjaciel")); tabs.add("Grupa", new JLabel("Gracz"));
					column.add(tabs, SBoxLayout.constraint(SLayout.EXPAND_X, SLayout.EXPAND_Y));
					JScrollPane dock = SwingClientGUI.createLeftColumnScrollPane(column); dock.setPreferredSize(new Dimension(180, 320));
					JLayeredPane game = new JLayeredPane(); game.add(overlay, JLayeredPane.PALETTE_LAYER);
					overlay.setVisible(false);
					JPanel root = new JPanel(new BorderLayout()); root.add(dock, BorderLayout.WEST); root.add(game, BorderLayout.CENTER);
					root.setSize(800, 320); layout(root);
					Rectangle buddyBounds = tabs.getBounds(); Dimension sidebarSize = column.getPreferredSize();
					JButton button = (JButton) launcher.getComponent(0); button.doClick(); layout(root);
					assertTrue(overlay.isVisible()); assertSame(game, overlay.getParent());
					assertEquals(sidebarSize, column.getPreferredSize()); assertEquals(buddyBounds, tabs.getBounds());
					assertTrue(tabs.getVisibleRect().height > 0); assertEquals(0, overlay.getX());
					for (int width : new int[] {200, 600, 200}) {
						game.setSize(width, 180);
						game.dispatchEvent(new ComponentEvent(game, ComponentEvent.COMPONENT_RESIZED)); layout(game);
						assertTrue(overlay.getX() >= 0); assertTrue(overlay.getY() >= 0);
						assertTrue(overlay.getX() + overlay.getWidth() <= game.getWidth());
						assertTrue(overlay.getY() + overlay.getHeight() <= game.getHeight());
					}
					overlay.close(); assertFalse(overlay.isVisible()); button.doClick(); assertTrue(overlay.isVisible());
					update.invoke(launcher, Collections.emptyMap()); assertFalse(overlay.isVisible());
				} finally { properties.set(WtWindowManager.getInstance(), oldProperties); }
			} catch (Exception e) { throw new AssertionError(e); }
		});
	}
	private static void layout(Container root) {
		for (int i=0; i<3; i++) {
			root.invalidate(); root.doLayout();
			for (Component child : root.getComponents()) if (child instanceof Container) layoutOnce((Container)child);
		}
	}
	private static void layoutOnce(Container root) {
		root.invalidate(); root.doLayout();
		for (Component child : root.getComponents()) if (child instanceof Container) layoutOnce((Container)child);
	}
}
