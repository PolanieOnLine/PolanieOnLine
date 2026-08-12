/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.maps.quests.revivalweeks;

import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.Test;

/**
 * Regression tests for the asynchronous construction NPC lifecycle.
 */
public class BuilderNPCTest {
	@Test
	public void testDelayedCallbackDoesNothingAfterRemoval() throws Exception {
		final BuilderNPC builder = new BuilderNPC();
		final Field active = BuilderNPC.class.getDeclaredField("active");
		active.setAccessible(true);
		active.setBoolean(builder, true);

		assertTrue(builder.removeFromWorld());

		// Simulates a TurnNotifier callback which was already queued while the
		// database request was still in flight. It must not dereference the old
		// command or recreate Klaus after the construction quest was unloaded.
		builder.onTurnReached(0);
	}

	@Test
	public void testRemovalIsIdempotentBeforeNpcExists() {
		final BuilderNPC builder = new BuilderNPC();
		assertTrue(builder.removeFromWorld());
		assertTrue(builder.removeFromWorld());
	}
}
