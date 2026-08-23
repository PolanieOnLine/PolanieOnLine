/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.transformer.ItemTransformer;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
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
		assertEquals(2, ItemAffixState.getValues(reward).size());
		assertTrue(ItemAffixState.hasSeed(reward));
		assertEquals(1, player.getNumberOfEquipped("złota ciupaga"));
		final Map<String, String> affixes = new LinkedHashMap<String, String>(
				ItemAffixState.getValues(reward));
		final Long seed = ItemAffixState.getSeed(reward);
		assertEquals(0, QuestRewardRarityMigrationService.migrate(player));
		assertEquals(affixes, ItemAffixState.getValues(reward));
		assertEquals(seed, ItemAffixState.getSeed(reward));
	}

	@Test
	public void everyRegisteredLegacyRewardSupportsFullEpicRarity() {
		final List<String> rewardNames =
				QuestRewardRarityMigrationService.registeredItemNames();
		assertFalse(rewardNames.isEmpty());
		for (final String itemName : rewardNames) {
			final Item reward = SingletonRepository.getEntityManager().getItem(
					itemName, ItemCreationContext.questReward());
			assertTrue(itemName, ItemRarityService.getInstance().isEligible(reward));
			assertSame(itemName, ItemRarity.EPIC, reward.getRarity());
			assertFalse(itemName, reward.getRarityModifiers().isEmpty());
			assertEquals(itemName, 2, ItemAffixState.getValues(reward).size());
			assertTrue(itemName, ItemAffixState.hasSeed(reward));
		}
	}

	@Test
	public void migratedStateAndUpgradeSurviveRepeatedRelog() throws IOException {
		final Player player = PlayerTestHelper.createPlayer("upgraded_reward");
		player.setQuest("zlota_ciupaga", "done");
		final Item reward = SingletonRepository.getEntityManager().getItem(
				"złota ciupaga", ItemCreationContext.quest());
		reward.setBoundTo(player.getName());
		reward.setUpgradeLevel(2);
		reward.setID(new ID(201, "quest_reward_migration"));
		assertTrue(player.equipToInventoryOnly(reward));

		assertEquals(1, QuestRewardRarityMigrationService.migrate(player));

		final Map<String, String> affixes = new LinkedHashMap<String, String>(
				ItemAffixState.getValues(reward));
		final Long seed = ItemAffixState.getSeed(reward);
		final Item firstLoad = new ItemTransformer().transform(serializedCopy(reward));
		final Item secondLoad = new ItemTransformer().transform(
				serializedCopy(firstLoad));

		assertEquals(2, secondLoad.getUpgradeLevel());
		assertSame(ItemRarity.EPIC, secondLoad.getRarity());
		assertEquals(affixes, ItemAffixState.getValues(secondLoad));
		assertEquals(seed, ItemAffixState.getSeed(secondLoad));
		assertEquals(player.getName(), secondLoad.getBoundTo());
	}

	@Test
	public void registryExcludesNonRarityAndInheritedRarityRewards() {
		final List<String> names =
				QuestRewardRarityMigrationService.registeredItemNames();
		assertFalse(names.contains("bielikrasa"));
		assertFalse(names.contains("złoty amulet"));
		assertFalse(names.contains("tarcza ciemnomithrilowa"));
		assertFalse(names.contains("złoty róg"));
		assertFalse(names.contains("zdobyczny hełm"));
	}

	@Test
	public void completedQuestWithoutItemDoesNotRecreateReward() {
		final Player player = PlayerTestHelper.createPlayer("lost_quest_reward");
		player.setQuest("zlota_ciupaga", "done");

		assertEquals(0, QuestRewardRarityMigrationService.migrate(player));
		assertEquals(0, player.getNumberOfEquipped("złota ciupaga"));
	}

	@Test
	public void completedQuestRingRemainsCommon() {
		final Player player = PlayerTestHelper.createPlayer("old_quest_ring");
		player.setQuest("pierscien_mieszczanina", "done");
		final Item ring = SingletonRepository.getEntityManager().getItem(
				"pierścień mieszczanina", ItemCreationContext.quest());
		ring.setBoundTo(player.getName());
		assertTrue(player.equipToInventoryOnly(ring));

		assertEquals(0, QuestRewardRarityMigrationService.migrate(player));

		assertSame(ItemRarity.COMMON, ring.getRarity());
		assertEquals(1, player.getNumberOfEquipped("pierścień mieszczanina"));
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

	private RPObject serializedCopy(final RPObject source) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(
				new ByteArrayInputStream(bytes.toByteArray())));
		return copy;
	}
}
