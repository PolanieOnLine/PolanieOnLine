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
	public void higherStakeMeansMoreCreaturesAndMoreCombatWaves() {
		assertTrue(ChallengeArenaTier.LEGEND.getCreatureCount()
				> ChallengeArenaTier.TRIAL.getCreatureCount());
		assertTrue(ChallengeArenaTier.LEGEND.getWaveCount()
				> ChallengeArenaTier.TRIAL.getWaveCount());
	}

	@Test
	public void trialUsesTenCreaturesInThreeDenseWaves() {
		final ChallengeArenaTier tier = ChallengeArenaTier.TRIAL;
		assertEquals(10, tier.getCreatureCount());
		assertEquals(3, tier.getWaveCount());
		assertEquals(4, tier.getWaveSizeForWave(1));
		assertEquals(3, tier.getWaveSizeForWave(2));
		assertEquals(3, tier.getWaveSizeForWave(3));
		assertEquals(1, tier.getNextWaveNumber(0));
		assertEquals(2, tier.getNextWaveNumber(4));
		assertEquals(3, tier.getNextWaveNumber(7));
		assertEquals(4, tier.getNextWaveNumber(10));
	}

	@Test
	public void hunterUsesTwentyCreaturesInSixWaves() {
		final ChallengeArenaTier tier = ChallengeArenaTier.HUNTER;
		assertEquals(20, tier.getCreatureCount());
		assertEquals(6, tier.getWaveCount());
		assertEquals(4, tier.getWaveSizeForWave(1));
		assertEquals(4, tier.getWaveSizeForWave(2));
		assertEquals(3, tier.getWaveSizeForWave(3));
		assertEquals(3, tier.getWaveSizeForWave(6));
	}

	@Test
	public void creatureLevelRisesWellAbovePlayerAcrossOneRun() {
		final ChallengeArenaTier tier = ChallengeArenaTier.LEGEND;
		final int first = tier.getTargetCreatureLevel(300, 1);
		final int last = tier.getTargetCreatureLevel(300,
				tier.getCreatureCount());

		assertEquals(316, first);
		assertEquals(336, last);
		assertTrue(last > first);
	}

	@Test
	public void trialAlsoStartsAbovePlayerLevel() {
		assertEquals(202,
				ChallengeArenaTier.TRIAL.getTargetCreatureLevel(200, 1));
		assertEquals(210,
				ChallengeArenaTier.TRIAL.getTargetCreatureLevel(200, 10));
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
