/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class WeaponAffixServiceTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void lifestealRollUsesPersistentCombatFraction() {
		for (int seed = 0; seed < 50; seed++) {
			final Item item = item("sword");
			assertTrue(WeaponAffixService.applyLifesteal(item,
					new Random(seed)));
			final double value = item.getDouble(
					WeaponAffixService.LIFESTEAL_ATTRIBUTE);
			assertTrue(value >= 0.03);
			assertTrue(value <= 0.10);
			assertEquals(Math.rint(value * 100.0), value * 100.0, 0.0000001);
		}
	}

	@Test
	public void accuracyRollUsesWholePercentagePoints() {
		for (int seed = 0; seed < 50; seed++) {
			final Item item = item("axe");
			assertTrue(WeaponAffixService.applyAccuracy(item,
					new Random(seed)));
			final double value = item.getDouble(
					WeaponAffixService.ACCURACY_ATTRIBUTE);
			assertTrue(value >= 5.0);
			assertTrue(value <= 15.0);
			assertEquals(Math.rint(value), value, 0.0);
		}
	}

	@Test
	public void criticalChanceRollUsesWholePercentagePoints() {
		for (int seed = 0; seed < 50; seed++) {
			final Item item = item("sword");
			assertTrue(WeaponAffixService.applyCriticalChance(item,
					new Random(seed)));
			final double value = item.getDouble(
					CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE);
			assertTrue(value >= 3.0);
			assertTrue(value <= 10.0);
			assertEquals(Math.rint(value), value, 0.0);
		}
	}

	@Test
	public void armorPenetrationRollUsesPersistentFraction() {
		for (int seed = 0; seed < 50; seed++) {
			final Item item = item("dagger");
			assertTrue(WeaponAffixService.applyArmorPenetration(item,
					new Random(seed)));
			final double value = item.getDouble(
					WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE);
			assertTrue(value >= 0.10);
			assertTrue(value <= 0.25);
			assertEquals(Math.rint(value * 100.0), value * 100.0, 0.0000001);
		}
	}

	@Test
	public void armorPenetrationOnlyRollsForArmorMatchupWeapons() {
		assertTrue(WeaponAffixService.isArmorPenetrationEligible(item("dagger")));
		assertTrue(WeaponAffixService.isArmorPenetrationEligible(item("sword")));
		assertTrue(WeaponAffixService.isArmorPenetrationEligible(item("axe")));
		assertTrue(WeaponAffixService.isArmorPenetrationEligible(item("club")));
		assertFalse(WeaponAffixService.isArmorPenetrationEligible(item("ranged")));
		assertFalse(WeaponAffixService.isArmorPenetrationEligible(item("wand")));
		assertFalse(WeaponAffixService.isArmorPenetrationEligible(item("armor")));
	}

	@Test
	public void intrinsicValuesAreNeverOverwritten() {
		final Item item = item("sword");
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, 0.30);
		item.put(WeaponAffixService.ACCURACY_ATTRIBUTE, 25.0);
		item.put(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE, 20.0);
		item.put(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE, 0.40);

		assertFalse(WeaponAffixService.applyLifesteal(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyAccuracy(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalChance(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyArmorPenetration(item, new Random(1L)));
		assertEquals(0.30, item.getDouble(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE), 0.0);
		assertEquals(25.0, item.getDouble(
				WeaponAffixService.ACCURACY_ATTRIBUTE), 0.0);
		assertEquals(20.0, item.getDouble(
				CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE), 0.0);
		assertEquals(0.40, item.getDouble(
				WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE), 0.0);
	}

	@Test
	public void nonWeaponCannotReceiveWeaponAffixes() {
		final Item item = item("armor");
		assertFalse(WeaponAffixService.applyLifesteal(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyAccuracy(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalChance(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyArmorPenetration(item, new Random(1L)));
	}

	private Item item(final String itemClass) {
		return new Item("weapon affix test", itemClass, "test", null);
	}
}
