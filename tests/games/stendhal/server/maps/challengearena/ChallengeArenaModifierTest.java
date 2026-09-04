package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class ChallengeArenaModifierTest {
	@Test
	public void zeroModifiersProducesEmptyList() {
		assertTrue(ChallengeArenaModifier.randomModifiers(0).isEmpty());
	}

	@Test
	public void requestedModifiersAreUnique() {
		final List<ChallengeArenaModifier> modifiers =
				ChallengeArenaModifier.randomModifiers(2);

		assertEquals(2, modifiers.size());
		assertEquals(2, new HashSet<ChallengeArenaModifier>(modifiers).size());
	}

	@Test
	public void requestIsClampedToAvailableModifiers() {
		final List<ChallengeArenaModifier> modifiers =
				ChallengeArenaModifier.randomModifiers(100);

		assertEquals(ChallengeArenaModifier.values().length, modifiers.size());
	}
}
