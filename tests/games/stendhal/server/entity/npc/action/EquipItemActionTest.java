/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.npc.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Log4J;
import utilities.PlayerTestHelper;

public class EquipItemActionTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		MockStendlRPWorld.get();
	}

	@Test
	public void testLegacyQuestRewardIsDeterministicCommon() {
		final Player player = PlayerTestHelper.createPlayer("player");
		final Item baseItem = SingletonRepository.getEntityManager().getItem(
				"sztylecik", ItemCreationContext.restore());
		new EquipItemAction("sztylecik").fire(player, null, null);

		final Item item = (Item) player.getSlot("bag").getFirst();
		assertEquals(ItemRarity.COMMON, item.getRarity());
		assertEquals(baseItem.getAttack(), item.getAttack());
		assertEquals(Double.valueOf(1.0), item.getRarityModifier("atk"));
		assertEquals(Double.valueOf(1.0), item.getRarityModifier("value"));
	}

	@Test
	public void testFixedQuestRewardIsIdenticalForDifferentPlayers() {
		final ItemRarityModifiers modifiers = ItemRarityModifiers.builder()
				.attackMultiplier(1.30)
				.defenseMultiplier(1.15)
				.valueMultiplier(2.00)
				.build();
		final EquipItemAction action = new EquipItemAction(
				"sztylecik", 1, true, ItemRarity.LEGENDARY, modifiers);
		final Player first = PlayerTestHelper.createPlayer("first");
		final Player second = PlayerTestHelper.createPlayer("second");

		action.fire(first, null, null);
		action.fire(second, null, null);

		final Item firstItem = (Item) first.getSlot("bag").getFirst();
		final Item secondItem = (Item) second.getSlot("bag").getFirst();
		assertEquals(ItemRarity.LEGENDARY, firstItem.getRarity());
		assertEquals(firstItem.getAttack(), secondItem.getAttack());
		assertEquals(firstItem.getDefense(), secondItem.getDefense());
		assertEquals(firstItem.getValue(), secondItem.getValue());
		assertEquals(firstItem.getRarityModifiers(), secondItem.getRarityModifiers());
	}

	@Test
	public void testRarityParticipatesInActionIdentity() {
		final EquipItemAction first = new EquipItemAction(
				"sztylecik", 1, false, ItemRarity.RARE);
		final EquipItemAction same = new EquipItemAction(
				"sztylecik", 1, false, ItemRarity.RARE);
		final EquipItemAction different = new EquipItemAction(
				"sztylecik", 1, false, ItemRarity.EPIC);

		assertEquals(first, same);
		assertEquals(first.hashCode(), same.hashCode());
		assertFalse(first.equals(different));
		assertTrue(different.toString().contains("EPIC"));
	}

	@Test
	public void explicitQuestRewardFactoryUsesEpicContext() {
		final EquipItemAction action = EquipItemAction.boundQuestReward("sztylecik");

		assertTrue(action.toString().contains("questRarity=EPIC"));
		assertTrue(action.toString().contains("randomizeModifiers=false"));
		assertTrue(action.toString().contains("generateAffixes=false"));
	}
}
