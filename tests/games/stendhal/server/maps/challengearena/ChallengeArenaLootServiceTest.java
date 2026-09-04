/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;

public class ChallengeArenaLootServiceTest {
	private static final ChallengeArenaTier[] REWARD_TIERS = {
		ChallengeArenaTier.HUNTER,
		ChallengeArenaTier.VETERAN,
		ChallengeArenaTier.CHAMPION,
		ChallengeArenaTier.LEGEND
	};

	@BeforeClass
	public static void createRPClasses() {
		new RPClassGenerator().createRPClassesWithoutBaking();
	}

	@Test
	public void configuredPoolsUseFullHundredPercentWeight() {
		for (final ChallengeArenaTier tier : REWARD_TIERS) {
			assertEquals(tier.name(), 100,
					ChallengeArenaLootService.getConfiguredTotalWeight(tier));
			final List<String> names =
					ChallengeArenaLootService.getConfiguredRewardNames(tier);
			final Set<String> unique = new HashSet<String>(names);
			assertEquals(tier.name(), names.size(), unique.size());
		}
		assertTrue(ChallengeArenaLootService.getConfiguredRewardNames(
				ChallengeArenaTier.TRIAL).isEmpty());
		assertTrue(ChallengeArenaLootService.getConfiguredRewardNames(
				ChallengeArenaTier.SKIRMISH).isEmpty());
	}

	@Test
	public void everyConfiguredItemExistsAndIsSafeEquipment() {
		final EntityManager entityManager = SingletonRepository.getEntityManager();
		for (final ChallengeArenaTier tier : REWARD_TIERS) {
			for (final String itemName
					: ChallengeArenaLootService.getConfiguredRewardNames(tier)) {
				assertTrue(itemName, entityManager.isItem(itemName));
				assertTrue(itemName,
						ChallengeArenaLootService.isSafeEquipmentReward(
								entityManager, itemName, 500));
			}
		}
	}

	@Test
	public void weightedSelectionUsesDesignedGroupBoundaries() {
		assertEquals("zbroja chaosu", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.HUNTER, 0.0, 0.0));
		assertEquals("lodowa zbroja", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.HUNTER, 0.45, 0.0));
		assertEquals("zbroja mainiocyjska", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.HUNTER, 0.70, 0.0));
		assertEquals("miecz chaosu", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.HUNTER, 0.90, 0.0));

		assertEquals("czarna zbroja", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.LEGEND, 0.0, 0.0));
		assertEquals("magiczna zbroja płytowa",
				ChallengeArenaLootService.selectRewardName(
						500, ChallengeArenaTier.LEGEND, 0.25, 0.0));
		assertEquals("zbroja wampirza", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.LEGEND, 0.45, 0.0));
		assertEquals("czarny płaszcz smoczy",
				ChallengeArenaLootService.selectRewardName(
						500, ChallengeArenaTier.LEGEND, 0.60, 0.0));
		assertEquals("ognisty miecz demonów",
				ChallengeArenaLootService.selectRewardName(
						500, ChallengeArenaTier.LEGEND, 0.76, 0.0));
		assertEquals("zbroja z mithrilu", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.LEGEND, 0.96, 0.0));
		assertEquals("płaszcz z mithrilu", ChallengeArenaLootService.selectRewardName(
				500, ChallengeArenaTier.LEGEND, 0.999999, 0.999999));
	}

	@Test
	public void itemLevelMayExceedPlayerByAtMostFifty() {
		final EntityManager entityManager = SingletonRepository.getEntityManager();
		assertFalse(ChallengeArenaLootService.isSafeEquipmentReward(
				entityManager, "ognisty miecz demonów", 249));
		assertTrue(ChallengeArenaLootService.isSafeEquipmentReward(
				entityManager, "ognisty miecz demonów", 250));
	}

	@Test
	public void definitionValueDoesNotLimitCuratedRewards() {
		final EntityManager entityManager = SingletonRepository.getEntityManager();
		final Item item = entityManager.getItem("ognisty miecz demonów",
				ItemCreationContext.restore());
		assertNotNull(item);
		assertTrue(item.getDefinitionValue()
				> ChallengeArenaTier.CHAMPION.getStake() / 5);
		assertTrue(ChallengeArenaLootService.isSafeEquipmentReward(
				entityManager, item.getName(), 500));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsOutOfRangeRewardRolls() {
		ChallengeArenaLootService.selectRewardName(500,
				ChallengeArenaTier.LEGEND, 1.0, 0.0);
	}
}
