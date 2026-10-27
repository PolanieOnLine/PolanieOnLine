package games.stendhal.server.maps.quests.hunting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class HuntingTargetSelectorTest {
	@Test
	public void separateWiderBandsAtOrdinaryLevels() {
		List<Integer> levels = Arrays.asList(12, 18, 20, 25, 26, 29, 30, 31, 32, 35, 37, 45);
		assertEquals(Arrays.asList(18, 20, 25), select(30, HuntingDifficulty.EASY, levels));
		assertEquals(Arrays.asList(26, 29, 30, 31), select(30, HuntingDifficulty.MEDIUM, levels));
		assertEquals(Arrays.asList(32, 35, 37), select(30, HuntingDifficulty.HARD, levels));
	}

	@Test
	public void easyHasManyPossibleTargetsAtHigherLevelsWithoutOverlappingMedium() {
		List<Integer> levels = Arrays.asList(45, 60, 65, 70, 75, 80, 85, 86, 95, 100, 105, 106, 115, 125);
		assertEquals(Arrays.asList(60, 65, 70, 75, 80, 85),
				select(100, HuntingDifficulty.EASY, levels));
		assertEquals(Arrays.asList(86, 95, 100, 105),
				select(100, HuntingDifficulty.MEDIUM, levels));
		assertEquals(Arrays.asList(106, 115, 125),
				select(100, HuntingDifficulty.HARD, levels));
	}

	@Test
	public void veteranCanChooseDifferentPoolsWhenOutlevellingPublicMonsters() {
		List<Integer> levels = Arrays.asList(1, 100, 380, 420, 500, 530, 542, 552, 562, 570, 578);
		assertEquals(Arrays.asList(380, 420, 500),
				select(600, HuntingDifficulty.EASY, levels));
		assertEquals(Arrays.asList(530, 542, 552, 562, 570, 578),
				select(600, HuntingDifficulty.MEDIUM, levels));
		assertEquals(Arrays.asList(552, 562, 570, 578),
				select(600, HuntingDifficulty.HARD, levels));
	}

	@Test
	public void highLevelFallbackHasSeveralTargetsWhenExactBandIsEmpty() {
		List<Integer> levels = Arrays.asList(1, 40, 100, 542, 552, 562, 570, 578);
		assertEquals(Arrays.asList(542, 552, 562, 570),
				select(600, HuntingDifficulty.EASY, levels));
		assertEquals(Arrays.asList(542, 552, 562, 570, 578),
				select(600, HuntingDifficulty.MEDIUM, levels));
		assertEquals(Arrays.asList(552, 562, 570, 578),
				select(600, HuntingDifficulty.HARD, levels));
	}

	@Test
	public void lowLevelHuntsKeepTheirOriginalRanges() {
		assertTrue(HuntingDifficulty.MEDIUM.accepts(1, 1));
		assertTrue(HuntingDifficulty.EASY.accepts(0, 1));
		assertTrue(HuntingDifficulty.HARD.accepts(4, 7));
		assertFalse(HuntingDifficulty.HARD.accepts(4, 1));
	}

	@Test
	public void doesNotOfferTrivialOrUnreachableOpponents() {
		assertTrue(select(600, HuntingDifficulty.HARD, Arrays.asList(1, 30, 100)).isEmpty());
		assertTrue(select(600, HuntingDifficulty.HARD, Arrays.asList(610, 800)).isEmpty());
		assertTrue(select(2, HuntingDifficulty.HARD, Arrays.asList(1)).isEmpty());
		assertFalse(select(600, HuntingDifficulty.HARD, Arrays.asList(580)).isEmpty());
	}

	@Test
	public void hardHuntsRequireMoreKillsWithoutChangingAcceptedMediumHunts() {
		assertEquals(20, HuntingDifficulty.MEDIUM.getMinimumKillCount());
		assertEquals(30, HuntingDifficulty.MEDIUM.getMaximumKillCount());
		assertEquals(40, HuntingDifficulty.HARD.getMinimumKillCount());
		assertEquals(50, HuntingDifficulty.HARD.getMaximumKillCount());
	}

	private List<Integer> select(int level, HuntingDifficulty difficulty, List<Integer> available) {
		return HuntingTargetSelector.select(level, difficulty, available, Integer::intValue);
	}
}
