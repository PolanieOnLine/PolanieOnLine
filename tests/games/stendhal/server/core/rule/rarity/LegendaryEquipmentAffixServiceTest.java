/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class LegendaryEquipmentAffixServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void bastionStoresExactBonusAndMaterializesDefense() {
		final Item armor = item("armor", "def", 100);

		assertTrue(LegendaryEquipmentAffixService.applyBastion(
				armor, new Random(7L)));

		final int bonus = armor.getInt(
				LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE);
		assertTrue(bonus >= 20 && bonus <= 30);
		assertEquals(100 + bonus, armor.getInt("def"));
	}

	@Test
	public void bastionCannotBeAppliedTwice() {
		final Item armor = item("armor", "def", 100);
		assertTrue(LegendaryEquipmentAffixService.applyBastion(
				armor, new Random(1L)));
		assertFalse(LegendaryEquipmentAffixService.applyBastion(
				armor, new Random(2L)));
	}

	@Test
	public void relicPowerStoresExactBonusAndMaterializesAttack() {
		final Item ring = item("ring", "atk", 3);

		assertTrue(LegendaryEquipmentAffixService.applyRelicPower(
				ring, new Random(11L)));

		final int bonus = ring.getInt(
				LegendaryEquipmentAffixService.RELIC_POWER_ATTRIBUTE);
		assertTrue(bonus >= 4 && bonus <= 7);
		assertEquals(3 + bonus, ring.getInt("atk"));
	}

	@Test
	public void legendaryEquipmentSignaturesRespectItemFamilies() {
		assertFalse(LegendaryEquipmentAffixService.isBastionEligible(
				item("ring", "def", 5)));
		assertFalse(LegendaryEquipmentAffixService.isRelicPowerEligible(
				item("armor", "atk", 5)));
	}

	private Item item(final String itemClass, final String attribute,
			final int value) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(attribute, Integer.toString(value));
		return new Item("legendary equipment test", itemClass, "test", attributes);
	}
}
