/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;

public class QuestRewardRarityMigrationServiceTest {
	@BeforeClass
	public static void setUpWorld() {
		MockStendlRPWorld.get();
	}

	@Test
	public void promotesOwnedBoundCompletedRewardWithoutCreatingDuplicate() {
		final Player player = PlayerTestHelper.createPlayer("old_quest_reward");
		player.setQuest("zlota_ciupaga", "done");
		final Item reward = SingletonRepository.getEntityManager().getItem(
				"złota ciupaga", ItemCreationContext.quest());
		reward.setBoundTo(player.getName());
		assertTrue(player.equipToInventoryOnly(reward));

		assertEquals(1, QuestRewardRarityMigrationService.migrate(player));

		assertSame(ItemRarity.EPIC, reward.getRarity());
		assertEquals(1, player.getNumberOfEquipped("złota ciupaga"));
		assertEquals(0, QuestRewardRarityMigrationService.migrate(player));
	}

	@Test
	public void completedQuestWithoutItemDoesNotRecreateReward() {
		final Player player = PlayerTestHelper.createPlayer("lost_quest_reward");
		player.setQuest("zlota_ciupaga", "done");

		assertEquals(0, QuestRewardRarityMigrationService.migrate(player));
		assertEquals(0, player.getNumberOfEquipped("złota ciupaga"));
	}

	@Test
	public void doesNotPromoteUnboundOrUnprovenItem() {
		final Player player = PlayerTestHelper.createPlayer("unproven_reward");
		final Item reward = SingletonRepository.getEntityManager().getItem(
				"złota ciupaga", ItemCreationContext.quest());
		assertTrue(player.equipToInventoryOnly(reward));

		assertEquals(0, QuestRewardRarityMigrationService.migrate(player));
		assertSame(ItemRarity.COMMON, reward.getRarity());
	}

	@Test
	public void restoresStatsSuppressedOnTheLegacyCommonReward() {
		final Player player = PlayerTestHelper.createPlayer("legacy_vampire");
		player.setQuest("vs_quest", "done");
		final Item sword = SingletonRepository.getEntityManager().getItem(
				"krwiopijca", ItemCreationContext.quest());
		final Item expected = SingletonRepository.getEntityManager().getItem(
				"krwiopijca", ItemCreationContext.questReward());
		assertFalse(sword.has("lifesteal"));
		sword.setBoundTo(player.getName());
		assertTrue(player.equipToInventoryOnly(sword));

		assertEquals(1, QuestRewardRarityMigrationService.migrate(player));

		assertSame(ItemRarity.EPIC, sword.getRarity());
		assertEquals(expected.getDouble("lifesteal"),
				sword.getDouble("lifesteal"), 0.000001);
		assertEquals(expected.getRarityModifier("lifesteal"),
				sword.getRarityModifier("lifesteal"));
	}
}
