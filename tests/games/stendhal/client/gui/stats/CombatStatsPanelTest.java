package games.stendhal.client.gui.stats;

import static org.junit.Assert.*;
import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.Properties;
import javax.swing.JComponent;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import games.stendhal.client.gui.UiTranslation;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPClass;
import marauroa.common.game.Definition.Type;

public class CombatStatsPanelTest {
	private static final RPClass WIRE_CLASS = new RPClass("combat_stats_client_test");
	static { WIRE_CLASS.addAttribute("combat_stats", Type.MAP); }
	private Field preferences;
	private Object previous;
	private JComponent panel;
	@Before public void isolatePreferencesAndFindView() throws Exception {
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previous = preferences.get(WtWindowManager.getInstance());
		preferences.set(WtWindowManager.getInstance(), new Properties());
		Field view = null;
		for (Field field : StatsPanel.class.getDeclaredFields()) {
			if (field.getName().equals("combatStatsPanel")) view = field;
		}
		assertNotNull("Character panel needs a combat percentage view", view);
		view.setAccessible(true);
		panel = (JComponent) view.get(StatsPanelController.get().getComponent());
	}
	@After public void restorePreferences() throws Exception {
		preferences.set(WtWindowManager.getInstance(), previous);
	}
	@Test public void currentValuesAreAvailableWithLimitsHiddenAndSectionCollapsed() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			update(snapshot("35"));
			JToggleButton toggle = find(panel, JToggleButton.class);
			assertFalse(toggle.isSelected());
			toggle.doClick();
			assertTrue(toggle.isSelected());
			assertEquals("Odporność na krwawienie: 35%", find(panel, JTextArea.class).getText());
			toggle.doClick();
		});
	}
	@Test public void limitsToggleUpdatesExistingValuesWithoutChangingTheSnapshot() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			RPObject source = snapshot("35");
			update(source);
			WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "true");
			assertEquals("Odporność na krwawienie: 35%\u00a0/\u00a060%", find(panel, JTextArea.class).getText());
			WtWindowManager.getInstance().setProperty("ui.show_stat_limits", "false");
			assertEquals("Odporność na krwawienie: 35%", find(panel, JTextArea.class).getText());
			assertEquals("35", source.get("combat_stats", "resist_bleeding"));
		});
	}
	@Test public void missingInvalidAndLoggedOutSnapshotsNeverLeaveStaleValues() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			update(snapshot("35"));
			assertTrue(panel.isVisible());
			for (RPObject source : new RPObject[] {null, new RPObject(), snapshot("NaN"), snapshot("wrong")}) {
				update(source);
				assertFalse(panel.isVisible());
				assertEquals("", find(panel, JTextArea.class).getText());
			}
		});
	}
	@Test public void languageChangesKeepValuesAndDoNotGetStuckInEnglish() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			update(snapshot("35.5"));
			WtWindowManager.getInstance().setProperty("ui.language", "en");
			UiTranslation.apply(panel);
			assertEquals("Bleeding resistance: 35.5%", find(panel, JTextArea.class).getText());
			WtWindowManager.getInstance().setProperty("ui.language", "pl");
			UiTranslation.apply(panel);
			assertEquals("Odporność na krwawienie: 35,5%", find(panel, JTextArea.class).getText());
		});
	}
	private RPObject snapshot(String value) {
		RPObject object = new RPObject();
		object.setRPClass(WIRE_CLASS);
		object.put("combat_stats", "resist_bleeding", value);
		object.put("combat_stats", "resist_bleeding.limit", "60");
		return object;
	}
	private void update(RPObject source) {
		StatsPanelController.get().updateCombatStats(source);
	}
	private <T extends Component> T find(Container root, Class<T> type) {
		for (Component child : root.getComponents()) {
			if (type.isInstance(child)) return type.cast(child);
			if (child instanceof Container) {
				T found = find((Container) child, type);
				if (found != null) return found;
			}
		}
		return null;
	}
}
