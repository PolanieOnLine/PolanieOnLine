package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Arrays;
import java.awt.Component;
import java.awt.Container;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import org.junit.Assume;
import org.junit.Test;

import games.stendhal.client.gui.styled.StyledLookAndFeel;
import games.stendhal.client.gui.styled.styles.StyleFactory;
import games.stendhal.common.constants.Actions;
import games.stendhal.server.events.ChatOptionsEvent;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

public class ArenaObservationWindowTest {
	@Test public void controlsNeverSendPublicChat() {
		for (final String operation : new String[] {"list", "return", "close"}) {
			final RPAction action = ArenaObservationWindow.createAction(operation, null);
			assertEquals(Actions.ARENA_OBSERVATION, action.get(Actions.TYPE));
			assertEquals(operation, action.get("operation"));
			assertFalse(action.has(Actions.TEXT));
			assertFalse(action.has("target"));
		}
		final RPAction action = ArenaObservationWindow.createAction("watch", "lista");
		assertEquals("watch", action.get("operation"));
		assertEquals("lista", action.get("target"));
		assertFalse(action.has(Actions.TEXT));
	}

	@Test public void emptyChooserClosesButAnActiveViewCanStillOfferReturn() {
		assertTrue(ArenaObservationWindow.shouldClose(
				ChatOptionsEvent.arenaObservation("open", "", Collections.emptyList())));
		assertTrue(ArenaObservationWindow.shouldClose(
				ChatOptionsEvent.arenaObservation("close", "", Collections.emptyList())));
		assertFalse(ArenaObservationWindow.shouldClose(
				ChatOptionsEvent.arenaObservation("watch", "", Collections.emptyList())));
		assertFalse(ArenaObservationWindow.shouldClose(
				ChatOptionsEvent.arenaObservation("open", "", Arrays.asList("Alice", "Bob"))));
		assertEquals(Arrays.asList("Alice", "Bob"), ArenaObservationWindow.choices(
				ChatOptionsEvent.arenaObservation("open", "", Arrays.asList("Alice", "Bob"))));
	}

	@Test public void chooserAndActiveViewFitWithoutAnOversizedPlayerList() throws Exception {
		withWindow(window -> {
			apply(window, "watch", "Obserwujesz: karajuss", "karajuss");
			assertTrue(window.getHeight() < 120);
			assertTrue(window.getWidth() < 360);
			assertEquals(window.getPreferredSize(), window.getSize());
			layout(window);
			assertTrue(((JComboBox<?>) field(window, "players")).getHeight() <= 30);
			assertTrue(((JButton) field(window, "leave")).isVisible());
			assertFalse(((JButton) field(window, "observe")).isFocusable());
			for (final String button : new String[] {"observe", "leave"}) {
				final JButton control = (JButton) field(window, button);
				final java.awt.Point point = SwingUtilities.convertPoint(
						control, control.getWidth(), control.getHeight(), window);
				assertTrue(point.x <= window.getWidth());
				assertTrue(point.y <= window.getHeight());
			}
			apply(window, "open", "Wybierz gracza do obserwacji", "karajuss");
			assertFalse(((JButton) field(window, "leave")).isVisible());
			assertTrue(window.getHeight() < 120);
		});
	}

	@Test public void resizingPreservesAvailableSelectionAndDisplaysPlainText() throws Exception {
		withWindow(window -> {
			apply(window, "watch", "Obserwujesz: Bob", "Alice", "Bob");
			final JComboBox<?> players = (JComboBox<?>) field(window, "players");
			players.setSelectedItem("Bob");
			apply(window, "watch", "<html>Bob", "Alice", "Bob");
			assertEquals("Bob", players.getSelectedItem());
			final JLabel status = (JLabel) field(window, "status");
			assertEquals(Boolean.TRUE, status.getClientProperty("html.disable"));
			assertEquals("<html>Bob", status.getText());
			apply(window, "watch", "Obserwujesz: Alice", "Alice");
			assertEquals("Alice", players.getSelectedItem());
		});
	}

	@Test public void activeViewWithoutFightersStillAllowsReturningToLobby() throws Exception {
		withWindow(window -> {
			apply(window, "watch", "", new String[0]);
			assertFalse(((JButton) field(window, "observe")).isEnabled());
			assertTrue(((JButton) field(window, "leave")).isVisible());
			assertTrue(window.getHeight() < 120);
		});
	}

	private static void apply(final ArenaObservationWindow window,
			final String phase, final String title, final String... players) throws Exception {
		final Method apply = ArenaObservationWindow.class.getDeclaredMethod("apply", RPEvent.class);
		apply.setAccessible(true);
		apply.invoke(window, ChatOptionsEvent.arenaObservation(phase, title, Arrays.asList(players)));
	}

	private static Object field(final ArenaObservationWindow window, final String name) throws Exception {
		final Field field = ArenaObservationWindow.class.getDeclaredField(name);
		field.setAccessible(true);
		return field.get(window);
	}

	private static void layout(final Container container) {
		container.doLayout();
		for (final Component child : container.getComponents()) {
			if (child instanceof Container) layout((Container) child);
		}
	}

	private interface WindowCheck {
		void run(ArenaObservationWindow window) throws Exception;
	}

	private static void withWindow(final WindowCheck check) throws Exception {
		Assume.assumeFalse(GraphicsEnvironment.isHeadless());
		SwingUtilities.invokeAndWait(() -> {
			final LookAndFeel oldLookAndFeel = UIManager.getLookAndFeel();
			final Object oldStyle = UIManager.get("StendhalStyle");
			try {
				UIManager.setLookAndFeel(new StyledLookAndFeel(StyleFactory.createStyle("Drewno (default)")));
				final Constructor<ArenaObservationWindow> constructor =
						ArenaObservationWindow.class.getDeclaredConstructor();
				constructor.setAccessible(true);
				check.run(constructor.newInstance());
			} catch (final Exception exception) {
				throw new RuntimeException(exception);
			} finally {
				try {
					UIManager.setLookAndFeel(oldLookAndFeel);
					UIManager.put("StendhalStyle", oldStyle);
				} catch (final Exception exception) {
					throw new RuntimeException(exception);
				}
			}
		});
	}
}
