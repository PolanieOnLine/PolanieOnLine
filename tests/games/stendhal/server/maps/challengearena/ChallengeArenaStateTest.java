package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChallengeArenaStateTest {
	@Test
	public void stateRoundTripsThroughQuestString() {
		final ChallengeArenaState started = ChallengeArenaState
				.start(ChallengeArenaTier.VETERAN)
				.withSpawnedCreatures(7);

		final ChallengeArenaState parsed = ChallengeArenaState.parse(
				started.serialize());

		assertNotNull(parsed);
		assertEquals(ChallengeArenaState.Lifecycle.ACTIVE,
				parsed.getLifecycle());
		assertEquals(ChallengeArenaTier.VETERAN, parsed.getTier());
		assertEquals(7, parsed.getSpawnedCreatures());
		assertTrue(parsed.getStartedAt() > 0L);
	}

	@Test
	public void malformedLegacyValueDoesNotCrashArena() {
		assertNull(ChallengeArenaState.parse("garbage"));
		assertNull(ChallengeArenaState.parse("active;unknown;5;123"));
		assertNull(ChallengeArenaState.parse("active;TRIAL;bad;123"));
		assertNull(ChallengeArenaState.parse("active;TRIAL;5;bad"));
	}

	@Test
	public void lifecycleCanChangeWithoutLosingRunData() {
		final ChallengeArenaState started = ChallengeArenaState
				.start(ChallengeArenaTier.CHAMPION)
				.withSpawnedCreatures(22);
		final ChallengeArenaState victory = started.withLifecycle(
				ChallengeArenaState.Lifecycle.VICTORY);

		assertEquals(ChallengeArenaTier.CHAMPION, victory.getTier());
		assertEquals(22, victory.getSpawnedCreatures());
		assertEquals(started.getStartedAt(), victory.getStartedAt());
	}
}
