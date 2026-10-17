package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.awt.Component;
import java.awt.Container;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.swing.AbstractButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.LookAndFeel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.client.ClientLanguage;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.sound.nosound.NoSoundFacade;
import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import utilities.MemoryClientPersistence;

public class StartupLanguageSelectionTest {
	private Field preferences;
	private Object previousPreferences;
	private StartupSettingsDialog dialog;
	private StendhalFirstScreen screen;
	private javax.swing.JFrame owner;
	private LookAndFeel previousLookAndFeel;
	private Object previousStyle;
	private MemoryClientPersistence persistence;
	private Field pendingLanguage;
	private Field languageSubmitted;
	private Object previousPendingLanguage;
	private Object previouslySubmitted;

	@Before public void isolatePreferences() throws Exception {
		persistence = new MemoryClientPersistence();
		pendingLanguage = ClientLanguage.class.getDeclaredField("pendingStartupLanguage");
		pendingLanguage.setAccessible(true);
		previousPendingLanguage = pendingLanguage.get(null);
		languageSubmitted = ClientLanguage.class.getDeclaredField("startupLanguageSubmittedTo");
		languageSubmitted.setAccessible(true);
		previouslySubmitted = languageSubmitted.get(null);
		pendingLanguage.set(null, null);
		languageSubmitted.set(null, null);
		preferences = WtWindowManager.class.getDeclaredField("properties");
		preferences.setAccessible(true);
		previousPreferences = preferences.get(WtWindowManager.getInstance());
		preferences.set(WtWindowManager.getInstance(), new Properties());
		SwingUtilities.invokeAndWait(() -> {
			previousLookAndFeel = UIManager.getLookAndFeel();
			previousStyle = UIManager.get("StendhalStyle");
			try { UIManager.setLookAndFeel(new StyledLookAndFeel(StyleFactory.createStyle("Drewno (default)"))); }
			catch (Exception error) { throw new AssertionError(error); }
			owner = new javax.swing.JFrame();
			owner.setFont(UIManager.getFont("Label.font"));
		});
	}

	@After public void closeWindowsAndRestorePreferences() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			if (dialog != null) dialog.dispose();
			if (screen != null) { screen.stopStartupMusic(); screen.dispose(); }
			if (owner != null) owner.dispose();
			try { UIManager.setLookAndFeel(previousLookAndFeel); UIManager.put("StendhalStyle", previousStyle); }
			catch (Exception error) { throw new AssertionError(error); }
		});
		SwingUtilities.invokeAndWait(() -> { });
		preferences.set(WtWindowManager.getInstance(), previousPreferences);
		pendingLanguage.set(null, previousPendingLanguage);
		languageSubmitted.set(null, previouslySubmitted);
		persistence.close();
	}

	@Test public void startupSelectorChangesLanguageAndRemembersItWhenReopened() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			dialog = new StartupSettingsDialog(owner, new StartupMusicController(new NoSoundFacade()));
			JComboBox<?> selector = languageSelector(dialog);
			assertNotNull("Startup settings should expose the language selector", selector);
			assertEquals("Polski", selector.getItemAt(0));
			assertEquals("English", selector.getItemAt(1));
			assertEquals(0, selector.getSelectedIndex());
			selector.setSelectedIndex(1);
			assertEquals("en", ClientLanguage.getLanguage());
		});
		SwingUtilities.invokeAndWait(() -> {
			assertEquals("Client settings", dialog.getTitle());
			assertTrue(hasText(dialog, "Game language:"));
			assertTrue(hasText(dialog, "Close"));
			dialog.dispose();
			dialog = new StartupSettingsDialog(owner, new StartupMusicController(new NoSoundFacade()));
			assertEquals(1, languageSelector(dialog).getSelectedIndex());
			languageSelector(dialog).setSelectedIndex(0);
			assertEquals("pl", ClientLanguage.getLanguage());
		});
		SwingUtilities.invokeAndWait(() -> assertEquals("Ustawienia klienta", dialog.getTitle()));
	}

	@Test public void firstScreenUsesSavedLanguageAndUpdatesWhileOpen() throws Exception {
		((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "en");
		StendhalClient client = clientWithoutNetwork();
		SwingUtilities.invokeAndWait(() -> screen = new StendhalFirstScreen(client, new NoSoundFacade()));
		SwingUtilities.invokeAndWait(() -> {
			assertTrue(hasText(screen, "Create account"));
			assertTrue(hasText(screen, "Client settings"));
			assertTrue(screen.getTitle(), screen.getTitle().contains("free MMORPG"));
			assertTrue(hasTooltip(screen, "Mute all sounds"));
			dialog = new StartupSettingsDialog(screen, new StartupMusicController(new NoSoundFacade()));
			languageSelector(dialog).setSelectedIndex(0);
		});
		SwingUtilities.invokeAndWait(() -> {
			assertTrue(hasText(screen, "Utwórz konto"));
			assertTrue(hasText(screen, "Ustawienia klienta"));
			assertTrue(screen.getTitle().contains("darmowa gra MMORPG"));
			assertTrue(hasTooltip(screen, "Wycisz wszystkie dźwięki"));
		});
	}

	@Test @SuppressWarnings({"rawtypes", "unchecked"}) public void windowModeOnlyTranslatesItsRenderedLabel() throws Exception {
		((Properties) preferences.get(WtWindowManager.getInstance())).setProperty("config." + ClientLanguage.PROPERTY, "en");
		SwingUtilities.invokeAndWait(() -> {
			dialog = new StartupSettingsDialog(owner, new StartupMusicController(new NoSoundFacade()));
			JComboBox selector = modeSelector(dialog);
			assertNotNull(selector);
			assertSame(WindowMode.WINDOWED, selector.getSelectedItem());
			JLabel display = (JLabel) selector.getRenderer().getListCellRendererComponent(new JList(), WindowMode.WINDOWED, 0, false, false);
			assertEquals("Window", display.getText());
			assertEquals("Okno", WindowMode.WINDOWED.toString());
			assertEquals("windowed", WindowMode.WINDOWED.getPropertyValue());
		});
	}

	private static JComboBox<?> languageSelector(Container root) {
		for (Component child : root.getComponents()) {
			if (child instanceof JComboBox && "language-selector".equals(child.getName())) return (JComboBox<?>) child;
			if (child instanceof Container) { JComboBox<?> found = languageSelector((Container) child); if (found != null) return found; }
		}
		return null;
	}

	private static JComboBox<?> modeSelector(Container root) {
		for (Component child : root.getComponents()) {
			if (child instanceof JComboBox && ((JComboBox<?>) child).getItemAt(0) instanceof WindowMode) return (JComboBox<?>) child;
			if (child instanceof Container) { JComboBox<?> found = modeSelector((Container) child); if (found != null) return found; }
		}
		return null;
	}

	private static boolean hasText(Container root, String text) {
		for (Component child : root.getComponents()) {
			if (child instanceof AbstractButton && text.equals(((AbstractButton) child).getText())) return true;
			if (child instanceof JLabel && text.equals(((JLabel) child).getText())) return true;
			if (child instanceof Container && hasText((Container) child, text)) return true;
		}
		return false;
	}

	private static boolean hasTooltip(Container root, String text) {
		for (Component child : root.getComponents()) {
			if (child instanceof javax.swing.JComponent && text.equals(((javax.swing.JComponent) child).getToolTipText())) return true;
			if (child instanceof Container && hasTooltip((Container) child, text)) return true;
		}
		return false;
	}

	private static StendhalClient clientWithoutNetwork() throws Exception {
		Class<?> unsafe = Class.forName("sun.misc.Unsafe");
		Field singleton = unsafe.getDeclaredField("theUnsafe");
		singleton.setAccessible(true);
		return (StendhalClient) unsafe.getMethod("allocateInstance", Class.class).invoke(singleton.get(null), StendhalClient.class);
	}
}
