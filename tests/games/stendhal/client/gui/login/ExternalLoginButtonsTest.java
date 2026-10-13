package games.stendhal.client.gui.login;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;

import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JButton;
import javax.swing.SwingUtilities;

import org.junit.Test;

public class ExternalLoginButtonsTest {
	@Test
	public void providersHaveIconsEqualWidthsAndIndependentActions() throws Exception {
		SwingUtilities.invokeAndWait(() -> {
			final AtomicInteger google = new AtomicInteger();
			final AtomicInteger steam = new AtomicInteger();
			final ExternalLoginButtons panel = new ExternalLoginButtons(event -> google.incrementAndGet(), event -> steam.incrementAndGet());
			panel.setSize(214, 26);
			panel.doLayout();
			assertEquals("Google", panel.google.getText());
			assertEquals("Steam", panel.steam.getText());
			assertNotNull(panel.google.getIcon());
			assertNotNull(panel.steam.getIcon());
			assertEquals(14, panel.google.getIcon().getIconWidth());
			assertEquals(14, panel.steam.getIcon().getIconHeight());
			assertNull(panel.getBorder());
			assertEquals(panel.google.getWidth(), panel.steam.getWidth());
			assertTrue(panel.google.getX() + panel.google.getWidth() < panel.steam.getX());
			panel.google.doClick();
			assertEquals(1, google.get()); assertEquals(0, steam.get());
			panel.steam.doClick();
			assertEquals(1, steam.get());
		});
	}

	@Test
	public void loginDialogPlacesProvidersAbovePasswordLoginWithoutOverlaps() throws Exception {
		assumeFalse(GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			final LoginDialog dialog = new LoginDialog(null, null, false);
			try {
				final Container pane = dialog.getContentPane();
				final JButton google = findButton(pane, "Google");
				final JButton steam = findButton(pane, "Steam");
				final JButton login = findButton(pane, "Zaloguj do serwera");
				final Rectangle g = SwingUtilities.convertRectangle(google.getParent(), google.getBounds(), pane);
				final Rectangle s = SwingUtilities.convertRectangle(steam.getParent(), steam.getBounds(), pane);
				final Rectangle l = SwingUtilities.convertRectangle(login.getParent(), login.getBounds(), pane);
				assertEquals(g.y, s.y);
				assertEquals(g.width, s.width);
				assertFalse(g.intersects(s));
				assertTrue(g.y + g.height <= l.y);
				assertTrue(g.x > pane.getWidth() / 3);
				assertTrue(google.isEnabled());
				assertTrue(steam.isEnabled());
				dialog.setEnabled(false);
				assertFalse(google.isEnabled()); assertFalse(steam.isEnabled());
				dialog.setEnabled(true);
				assertTrue(google.isEnabled()); assertTrue(steam.isEnabled());
			} finally { dialog.dispose(); }
		});
	}

	private JButton findButton(final Container parent, final String text) {
		for (final Component component : parent.getComponents()) {
			if (component instanceof JButton && text.equals(((JButton) component).getText())) return (JButton) component;
			if (component instanceof Container) {
				final JButton found = findButton((Container) component, text);
				if (found != null) return found;
			}
		}
		return null;
	}
}
