package games.stendhal.client.gui.stats;

import static org.junit.Assert.assertTrue;

import java.beans.PropertyChangeSupport;
import java.lang.reflect.Field;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.junit.Test;

public class StatsPanelHealthTest {
	@Test
	public void temporaryHpTakesPrecedenceAndDoesNotLeakIntoTheNextCharacter() throws Exception {
		final StatsPanelController controller = StatsPanelController.get();
		final PropertyChangeSupport changes = new PropertyChangeSupport(this);
		controller.registerListeners(changes);
		changes.firePropertyChange("base_hp", null, "32767");
		changes.firePropertyChange("hp", null, "32767");
		changes.firePropertyChange("modified_hp", null, "39320");
		assertHealth(controller, "39320/32767");
		changes.firePropertyChange("hp", "32767", "32700");
		assertHealth(controller, "39320/32767");
		changes.firePropertyChange("modified_hp", "39320", "32700");
		assertHealth(controller, "32700/32767");
		changes.firePropertyChange("modified_hp", "32700", null);
		assertHealth(controller, "32700/32767");
		controller.registerListeners(new PropertyChangeSupport(new Object()));
		changes.firePropertyChange("base_hp", "32767", "100");
		changes.firePropertyChange("hp", "32700", "100");
		assertHealth(controller, "100/100");
	}

	private void assertHealth(final StatsPanelController controller, final String expected) throws Exception {
		SwingUtilities.invokeAndWait(() -> { });
		final Field labelField = StatsPanel.class.getDeclaredField("hpLabel");
		labelField.setAccessible(true);
		final JTextArea label = (JTextArea) labelField.get(controller.getComponent());
		assertTrue(label.getText(), label.getText().endsWith(expected));
	}
}
