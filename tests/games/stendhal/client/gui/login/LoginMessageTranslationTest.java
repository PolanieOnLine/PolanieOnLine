package games.stendhal.client.gui.login;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import utilities.MemoryClientPersistence;

public class LoginMessageTranslationTest {
	private Field preferences;
	private Object previousPreferences;
	private MemoryClientPersistence persistence;
	private LookAndFeel previousLook;
	private JDialog source;

	@Before public void isolatePreferencesAndUseSystemStyle() throws Exception {
		persistence = new MemoryClientPersistence();
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		Properties properties = new Properties();
		properties.setProperty("config." + ClientLanguage.PROPERTY, "en");
		preferences.set(WtWindowManager.getInstance(), properties);
		previousLook = UIManager.getLookAndFeel();
		UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
	}

	@After public void cleanup() throws Exception {
		SwingUtilities.invokeAndWait(() -> { if (source != null) source.dispose(); });
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
		persistence.close();
		UIManager.setLookAndFeel(previousLook);
	}

	@Test public void loginPortErrorTranslatesItsStandaloneTitleAndBody() throws Exception {
		AtomicReference<String[]> captured = new AtomicReference<String[]>();
		SwingUtilities.invokeAndWait(() -> {
			source = new LoginDialog(null, null, false);
			field(source, "usernameField", JTextField.class).setText("Zamknij");
			field(source, "passwordField", JPasswordField.class).setText("Hasło123!");
			JTextField port = field(source, "serverPortField", JTextField.class);
			try { port.getDocument().remove(0, port.getDocument().getLength()); }
			catch (Exception error) { throw new AssertionError(error); }
			captureNextOption(captured);
			invoke(source, "loginButtonActionPerformed");
			assertEquals("Zamknij", field(source, "usernameField", JTextField.class).getText());
			assertArrayEquals("Hasło123!".toCharArray(), field(source, "passwordField", JPasswordField.class).getPassword());
		});
		assertNotNull(captured.get());
		assertEquals("Invalid port", captured.get()[0]);
		assertEquals("Invalid port number. Please try again.", captured.get()[1]);
	}
	@Test public void externalLoginStatusTranslatesEachUpdateAndPreservesTheVerificationCode() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			source = new LoginDialog(null, null, false);
			JLabel status = field(source,"externalStatusLabel",JLabel.class);
			String[][] messages = {{"Łączę się ze stroną...","Connecting to the website..."},
				{"Kod Google do wpisania na stronie: ABC0123456","Enter this Google code on the website: ABC0123456"},
				{"Logowanie do gry...","Logging in to the game..."},
				{"Nie udało się zalogować przez Steam.","Could not log in with Steam."}};
			for(String[] message:messages) {status.setText(message[0]);assertEquals(message[1],status.getText());}
		});
	}

	@Test public void createAccountPasswordMismatchTranslatesWithoutChangingInputs() throws Exception {
		AtomicReference<String[]> captured = new AtomicReference<String[]>();
		SwingUtilities.invokeAndWait(() -> {
			source = new CreateAccountDialog();
			field(source, "passwordField", JPasswordField.class).setText("Zamknij123!");
			field(source, "passwordretypeField", JPasswordField.class).setText("Different123!");
			captureNextOption(captured);
			invoke(source, "checkFields");
			assertArrayEquals("Zamknij123!".toCharArray(), field(source, "passwordField", JPasswordField.class).getPassword());
		});
		assertNotNull(captured.get());
		assertEquals("Passwords do not match", captured.get()[0]);
		assertEquals("The passwords do not match. Enter both passwords again.", captured.get()[1]);
	}

	@Test public void passwordWarningTranslatesItsChoiceButtonsUnderSystemStyle() throws Exception {
		AtomicReference<String[]> captured = new AtomicReference<String[]>();
		SwingUtilities.invokeAndWait(() -> {
			source = new CreateAccountDialog();
			field(source, "usernameField", JTextField.class).setText("Zamknij");
			field(source, "passwordField", JPasswordField.class).setText("Zamknij123!");
			field(source, "passwordretypeField", JPasswordField.class).setText("Zamknij123!");
			captureNextOption(captured);
			invoke(source, "checkFields");
		});
		assertNotNull(captured.get());
		assertEquals("Incorrect password", captured.get()[0]);
		assertEquals("Yes", captured.get()[2]);
		assertEquals("No", captured.get()[3]);
	}

	private void captureNextOption(AtomicReference<String[]> captured) {
		Timer timer = new Timer(20, null);
		timer.addActionListener(event -> {
			for (Window window : Window.getWindows()) if (window instanceof JDialog && window != source && window.isShowing()) {
				JOptionPane pane = optionPane((Container) window);
				if (pane != null) {
					Object[] options = pane.getOptions();
					captured.set(new String[] { ((JDialog) window).getTitle(), String.valueOf(pane.getMessage()),
							options == null ? "" : String.valueOf(options[0]), options == null ? "" : String.valueOf(options[1]) });
					timer.stop();
					if (options != null) pane.setValue(options[1]);
					window.dispose();
					return;
				}
			}
		});
		timer.start();
	}

	private JOptionPane optionPane(Container root) {
		for (Component child : root.getComponents()) {
			if (child instanceof JOptionPane) return (JOptionPane) child;
			if (child instanceof Container) { JOptionPane pane = optionPane((Container) child); if (pane != null) return pane; }
		}
		return null;
	}

	private static <T> T field(Object object, String name, Class<T> type) {
		try { Field field = object.getClass().getDeclaredField(name); field.setAccessible(true); return type.cast(field.get(object)); }
		catch (Exception error) { throw new AssertionError(error); }
	}

	private static void invoke(Object object, String method) {
		try { Method call = object.getClass().getDeclaredMethod(method); call.setAccessible(true); call.invoke(object); }
		catch (Exception error) { throw new AssertionError(error); }
	}
}
