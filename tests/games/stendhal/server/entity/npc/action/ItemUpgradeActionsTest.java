package games.stendhal.server.entity.npc.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;

public class ItemUpgradeActionsTest {
	private static final String[] CHAIN = {"ciupaga", "złota ciupaga", "złota ciupaga z wąsem",
			"złota ciupaga z dwoma wąsami", "złota ciupaga z trzema wąsami"};

	private Item source(final Player player, final ItemRarity rarity) {
		final Item item = SingletonRepository.getEntityManager().getItem("ciupaga", ItemCreationContext
				.builder(ItemCreationContext.Source.QUEST).withRarity(rarity)
				.randomizeModifiers(false).withAffixSeed(81L).build());
		item.setBoundTo(player.getName());
		player.getSlot("bag").add(item);
		return item;
	}

	@Test
	public void allFourUpgradesKeepLegendaryRollsAcrossCharacterReloads() {
		Player player = PlayerTestHelper.createPlayer("legendary_crafter");
		final Item original = source(player, ItemRarity.LEGENDARY);
		final Map<String, String> affixes = ItemAffixState.getValues(original);
		for (int stage = 1; stage < CHAIN.length; stage++) {
			final String quest = "upgrade_stage_" + stage;
			player.setQuest(quest, "start;0;7");
			new CaptureItemUpgradeAction(quest, CHAIN[stage - 1]).fire(player, null, null);
			assertTrue(player.dropSubmittable(CHAIN[stage - 1], 1));
			new SetQuestAction(quest, 0, "forging").fire(player, null, null);
			new SetQuestToTimeStampAction(quest, 1).fire(player, null, null);
			player = new Player((RPObject) player.clone());
			new EquipUpgradedItemAction(quest, CHAIN[stage], ItemRarity.EPIC).fire(player, null, null);
			final Item received = player.getFirstEquipped(CHAIN[stage]);
			assertNotNull(received);
			assertEquals(ItemRarity.LEGENDARY, received.getRarity());
			assertEquals(affixes, ItemAffixState.getValues(received));
			assertEquals(player.getName(), received.getBoundTo());
			assertEquals("7", player.getQuest(quest, 2));
			assertEquals("", player.getQuest(quest, CaptureItemUpgradeAction.SNAPSHOT_INDEX));
		}
	}

	@Test
	public void rareGainsOneAffixAndFollowingStagesKeepBoth() {
		final Player player = PlayerTestHelper.createPlayer("rare_crafter");
		Map<String, String> affixes = ItemAffixState.getValues(source(player, ItemRarity.RARE));
		for (int stage = 1; stage < CHAIN.length; stage++) {
			final String quest = "rare_stage_" + stage;
			new CaptureItemUpgradeAction(quest, CHAIN[stage - 1]).fire(player, null, null);
			assertTrue(player.dropSubmittable(CHAIN[stage - 1], 1));
			new EquipUpgradedItemAction(quest, CHAIN[stage], ItemRarity.EPIC).fire(player, null, null);
			final Item received = player.getFirstEquipped(CHAIN[stage]);
			assertNotNull(received);
			assertEquals(ItemRarity.EPIC, received.getRarity());
			assertEquals(2, ItemAffixState.getValues(received).size());
			assertTrue(ItemAffixState.getValues(received).entrySet().containsAll(affixes.entrySet()));
			affixes = ItemAffixState.getValues(received);
		}
	}

	@Test
	public void legacyOrderWithoutSourceSnapshotStillReturnsEpicReward() {
		final Player player = PlayerTestHelper.createPlayer("old_upgrade");
		player.setQuest("old_order", "forging;1");
		new EquipUpgradedItemAction("old_order", "złota ciupaga", ItemRarity.EPIC).fire(player, null, null);
		final Item received = player.getFirstEquipped("złota ciupaga");
		assertNotNull(received);
		assertEquals(ItemRarity.EPIC, received.getRarity());
		assertEquals(2, ItemAffixState.getValues(received).size());
	}

	@Test
	public void damagedSnapshotDoesNotIssueARerolledReplacement() {
		final Player player = PlayerTestHelper.createPlayer("bad_upgrade");
		player.setQuest("bad_order", "forging;1;0;broken");
		try {
			new EquipUpgradedItemAction("bad_order", "złota ciupaga", ItemRarity.EPIC).fire(player, null, null);
			fail("Corrupt inheritance must not silently replace a saved roll");
		} catch (final IllegalArgumentException expected) {
			assertEquals(0, player.getNumberOfEquipped("złota ciupaga"));
			assertEquals("broken", player.getQuest("bad_order", CaptureItemUpgradeAction.SNAPSHOT_INDEX));
		}
	}

	@Test
	public void capturesTheSameFirstMatchingItemThatTheBlacksmithConsumes() {
		final Player player = PlayerTestHelper.createPlayer("multiple_ciupagas");
		final Item first = source(player, ItemRarity.RARE);
		final Item second = source(player, ItemRarity.LEGENDARY);
		new CaptureItemUpgradeAction("chosen_order", "ciupaga").fire(player, null, null);
		assertTrue(player.dropSubmittable("ciupaga", 1));
		new EquipUpgradedItemAction("chosen_order", "złota ciupaga", ItemRarity.EPIC).fire(player, null, null);
		assertEquals(second, player.getFirstEquipped("ciupaga"));
		final Item result = player.getFirstEquipped("złota ciupaga");
		assertEquals(ItemRarity.EPIC, result.getRarity());
		assertTrue(ItemAffixState.getValues(result).entrySet().containsAll(ItemAffixState.getValues(first).entrySet()));
	}
}
