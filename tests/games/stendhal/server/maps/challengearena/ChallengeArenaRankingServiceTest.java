package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChallengeArenaRankingServiceTest {
	@Test
	public void higherTierAlwaysOutranksLowerTier() {
		final int slowLegend = ChallengeArenaRankingService.calculateScore(
				ChallengeArenaTier.LEGEND, 60L * 60L * 1000L);
		final int fastChampion = ChallengeArenaRankingService.calculateScore(
				ChallengeArenaTier.CHAMPION, 1000L);

		assertTrue(slowLegend > fastChampion);
	}

	@Test
	public void fasterRunWinsInsideSameTier() {
		final int fast = ChallengeArenaRankingService.calculateScore(
				ChallengeArenaTier.LEGEND, 60_000L);
		final int slow = ChallengeArenaRankingService.calculateScore(
				ChallengeArenaTier.LEGEND, 120_000L);

		assertTrue(fast > slow);
	}
}
