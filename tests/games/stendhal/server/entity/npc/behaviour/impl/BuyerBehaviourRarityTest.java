/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.behaviour.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.engine.RPClassGenerator;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject.ID;
import utilities.PlayerTestHelper;

public class BuyerBehaviourRarityTest {
	@BeforeClass
	public static void createRPClasses() {
		new RPClassGenerator().createRPClassesWithoutBaking();
	}

	@Test
	public void quoteUsesValueMultiplierOfConcreteItemsInDropOrder() {
		final Player player = PlayerTestHelper.createPlayer("seller");
		final Item common = item("test blade", ItemRarity.COMMON, 1.0);
		final Item legendary = item("test blade", ItemRarity.LEGENDARY, 2.0);
		common.setID(new ID(101, "buyer_rarity_test"));
		legendary.setID(new ID(102, "buyer_rarity_test"));
		assertTrue(player.equipToInventoryOnly(common));
		assertTrue(player.equipToInventoryOnly(legendary));
		final BuyerBehaviour buyer = new BuyerBehaviour(
				Collections.singletonMap("test blade", Integer.valueOf(100)));

		assertEquals(100, buyer.getCharge(
				new ItemParserResult(true, "test blade", 1, null), player));
		assertEquals(300, buyer.getCharge(
				new ItemParserResult(true, "test blade", 2, null), player));
	}

	@Test
	public void missingRarityMetadataKeepsBasePrice() {
		final Player player = PlayerTestHelper.createPlayer("legacy seller");
		final Item legacy = new Item("test blade", "sword", "test",
				new HashMap<String, String>());
		legacy.setEquipableSlots(Arrays.asList("rhand", "bag"));
		assertTrue(player.equipToInventoryOnly(legacy));
		final BuyerBehaviour buyer = new BuyerBehaviour(
				Collections.singletonMap("test blade", Integer.valueOf(75)));

		assertEquals(75, buyer.getCharge(
				new ItemParserResult(true, "test blade", 1, null), player));
	}

	@Test
	public void extremeSavedMultiplierSaturatesWithoutOverflow() {
		final Player player = PlayerTestHelper.createPlayer("overflow seller");
		final Item first = item("test blade", ItemRarity.LEGENDARY, 1.0e308);
		final Item second = item("test blade", ItemRarity.LEGENDARY, 1.0e308);
		first.setID(new ID(201, "buyer_rarity_test"));
		second.setID(new ID(202, "buyer_rarity_test"));
		assertTrue(player.equipToInventoryOnly(first));
		assertTrue(player.equipToInventoryOnly(second));
		final BuyerBehaviour buyer = new BuyerBehaviour(
				Collections.singletonMap("test blade", Integer.valueOf(100)));

		assertEquals(Integer.MAX_VALUE, buyer.getCharge(
				new ItemParserResult(true, "test blade", 2, null), player));
	}

	private Item item(final String name, final ItemRarity rarity,
			final double valueMultiplier) {
		final Item item = new Item(name, "sword", "test",
				new HashMap<String, String>());
		item.setEquipableSlots(Arrays.asList("rhand", "bag"));
		item.setRarity(rarity);
		item.setRarityModifier("value", valueMultiplier);
		return item;
	}
}
