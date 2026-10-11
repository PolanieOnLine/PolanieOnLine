package games.stendhal.client.gui;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Arrays;
import org.junit.Test;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.events.ChatOptionsEvent;
import marauroa.common.game.RPAction;

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
}
