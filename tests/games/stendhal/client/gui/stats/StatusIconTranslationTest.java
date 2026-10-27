/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui.stats;
import static org.junit.Assert.*;
import java.lang.reflect.Field;
import java.util.Properties;
import javax.swing.SwingUtilities;
import org.junit.Test;
import games.stendhal.client.gui.UiTranslation;
import games.stendhal.client.gui.wt.core.WtWindowManager;

public class StatusIconTranslationTest {
	@Test public void moodMessagesAreNeverTranslatedEvenWhenTheyMatchInterfaceText() throws Exception {
		WtWindowManager manager = WtWindowManager.getInstance();
		Field field = WtWindowManager.class.getDeclaredField("properties"); field.setAccessible(true);
		Object previous = field.get(manager); field.set(manager, new Properties());
		StatusIconPanel panel = new StatusIconPanel();
		try {
			panel.setAway("Zamknij"); panel.setGrumpy("Plecak"); UiTranslation.register(panel);
			SwingUtilities.invokeAndWait(() -> manager.setProperty("ui.language", "en"));
			SwingUtilities.invokeAndWait(() -> { });
			assertEquals("<html>You are away with the message:<br><b>Zamknij</b></html>", panel.away.getToolTipText());
			assertEquals("<html>You are unavailable with the message:<br><b>Plecak</b></html>", panel.grumpy.getToolTipText());
			assertTrue(panel.getToolTipText().contains("Active statuses:"));
			assertTrue(panel.getToolTipText().contains("Zamknij"));
			assertTrue(panel.getToolTipText().contains("Plecak"));
			assertFalse(panel.getToolTipText().contains("Close"));
			panel.setAway("<b>Plecak</b>");
			assertTrue(panel.away.getToolTipText().contains("&lt;b&gt;Plecak&lt;/b&gt;"));
		} finally {
			panel.setAway(null); panel.setGrumpy(null); field.set(manager, previous);
		}
	}
}
