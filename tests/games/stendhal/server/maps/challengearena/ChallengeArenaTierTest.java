package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ChallengeArenaTierTest {
	@Test
	public void supportedStakesMapToFixedTiers() {
		assertEquals(ChallengeArenaTier.TRIAL,
				ChallengeArenaTier.forStake(100000));
		assertEquals(ChallengeArenaTier.LEGEND,
				ChallengeArenaTier.forStake(5000000));
		assertNull(ChallengeArenaTier.forStake(750000));
	}

	@Test
	public void higherStakeMeansMoreCreaturesAndLargerWaves() {
		assertTrue(ChallengeArenaTier.LEGEND.getCreatureCount()
				> ChallengeArenaTier.TRIAL.getCreatureCount());
		assertTrue(ChallengeArenaTier.LEGEND.getWaveSize()
				> ChallengeArenaTier.TRIAL.getWaveSize());
	}

	@Test
	public void creatureLevelRisesAcrossOneRun() {
		final ChallengeArenaTier tier = ChallengeArenaTier.LEGEND;
		final int first = tier.getTargetCreatureLevel(300, 1);
		final int last = tier.getTargetCreatureLevel(300,
				tier.getCreatureCount());

		assertEquals(308, first);
		assertEquals(316, last);
		assertTrue(last > first);
	}

	@Test
	public void lowTierHasNoForcedEliteButHighTierDoes() {
		for (int i = 1; i <= ChallengeArenaTier.TRIAL.getCreatureCount(); i++) {
			assertFalse(ChallengeArenaTier.TRIAL.shouldForceElite(i));
		}

		int elites = 0;
		for (int i = 1; i <= ChallengeArenaTier.LEGEND.getCreatureCount(); i++) {
			if (ChallengeArenaTier.LEGEND.shouldForceElite(i)) {
				elites++;
			}
		}
		assertEquals(ChallengeArenaTier.LEGEND.getForcedEliteCount(), elites);
	}

	@Test
	public void onlyHigherTiersAwardEquipmentChest() {
		assertFalse(ChallengeArenaTier.TRIAL.awardsEquipmentChest());
		assertFalse(ChallengeArenaTier.SKIRMISH.awardsEquipmentChest());
		assertTrue(ChallengeArenaTier.HUNTER.awardsEquipmentChest());
		assertTrue(ChallengeArenaTier.VETERAN.awardsEquipmentChest());
		assertTrue(ChallengeArenaTier.CHAMPION.awardsEquipmentChest());
		assertTrue(ChallengeArenaTier.LEGEND.awardsEquipmentChest());

		assertEquals(0, ChallengeArenaTier.TRIAL.getRewardRarityRolls());
		assertEquals(1, ChallengeArenaTier.HUNTER.getRewardRarityRolls());
		assertEquals(4, ChallengeArenaTier.LEGEND.getRewardRarityRolls());
	}

	@Test
	public void onlyTopTiersEndWithArenaChampion() {
		assertFalse(ChallengeArenaTier.VETERAN.isFinalChampion(
				ChallengeArenaTier.VETERAN.getCreatureCount()));
		assertTrue(ChallengeArenaTier.CHAMPION.isFinalChampion(
				ChallengeArenaTier.CHAMPION.getCreatureCount()));
		assertTrue(ChallengeArenaTier.LEGEND.isFinalChampion(
				ChallengeArenaTier.LEGEND.getCreatureCount()));
		assertFalse(ChallengeArenaTier.LEGEND.isFinalChampion(
				ChallengeArenaTier.LEGEND.getCreatureCount() - 1));
	}

	@Test
	public void legendChampionIsStrongerThanChampionTierFinale() {
		assertTrue(ChallengeArenaTier.LEGEND.getChampionHpMultiplier()
				> ChallengeArenaTier.CHAMPION.getChampionHpMultiplier());
		assertTrue(ChallengeArenaTier.LEGEND.getChampionAttackMultiplier()
				> ChallengeArenaTier.CHAMPION.getChampionAttackMultiplier());
		assertTrue(ChallengeArenaTier.LEGEND.getChampionDefenseMultiplier()
				> ChallengeArenaTier.CHAMPION.getChampionDefenseMultiplier());
	}
}
