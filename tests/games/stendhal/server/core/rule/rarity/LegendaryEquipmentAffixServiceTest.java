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
	public void armourLegendaryMarkersMaterializeWithoutChangingBaseStats() {
		final Item ironWill = item("armor", "def", 50);
		final Item unyielding = item("helmet", "def", 20);

		assertTrue(LegendaryEquipmentAffixService.applyIronWill(
				ironWill, new Random(1L)));
		assertTrue(ironWill.has(LegendaryEquipmentAffixService.IRON_WILL_ATTRIBUTE));
		assertEquals(50, ironWill.getInt("def"));

		assertTrue(LegendaryEquipmentAffixService.applyUnyieldingProtection(
				unyielding, new Random(2L)));
		assertTrue(unyielding.has(
				LegendaryEquipmentAffixService.UNYIELDING_PROTECTION_ATTRIBUTE));
		assertEquals(20, unyielding.getInt("def"));
	}

	@Test
	public void accessoryLegendaryMarkersMaterializeWithoutChangingBaseStats() {
		final Item heroEye = item("ring", "atk", 3);
		final Item guardianSeal = item("necklace", "def", 2);

		assertTrue(LegendaryEquipmentAffixService.applyHeroEye(
				heroEye, new Random(3L)));
		assertTrue(heroEye.has(LegendaryEquipmentAffixService.HERO_EYE_ATTRIBUTE));
		assertEquals(3, heroEye.getInt("atk"));

		assertTrue(LegendaryEquipmentAffixService.applyGuardianSeal(
				guardianSeal, new Random(4L)));
		assertTrue(guardianSeal.has(
				LegendaryEquipmentAffixService.GUARDIAN_SEAL_ATTRIBUTE));
		assertEquals(2, guardianSeal.getInt("def"));
	}

	@Test
	public void legendaryEquipmentSignaturesRespectItemFamilies() {
		final Item ring = item("ring", "def", 5);
		final Item armor = item("armor", "atk", 5);
		assertFalse(LegendaryEquipmentAffixService.isBastionEligible(ring));
		assertFalse(LegendaryEquipmentAffixService.isIronWillEligible(ring));
		assertFalse(LegendaryEquipmentAffixService.isUnyieldingProtectionEligible(ring));
		assertFalse(LegendaryEquipmentAffixService.isRelicPowerEligible(armor));
		assertFalse(LegendaryEquipmentAffixService.isHeroEyeEligible(armor));
		assertFalse(LegendaryEquipmentAffixService.isGuardianSealEligible(armor));
	}

	private Item item(final String itemClass, final String attribute,
			final int value) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put(attribute, Integer.toString(value));
		return new Item("legendary equipment test", itemClass, "test", attributes);
	}
}
