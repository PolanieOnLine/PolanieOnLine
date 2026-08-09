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
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
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
		assertFractionRoll("sword", WeaponAffixService.LIFESTEAL_ATTRIBUTE,
				3, 10, new AffixApplier() {
					@Override public boolean apply(Item item, Random random) {
						return WeaponAffixService.applyLifesteal(item, random);
					}
				});
	}

	@Test
	public void accuracyRollUsesWholePercentagePoints() {
		for (int seed = 0; seed < 50; seed++) {
			final Item item = item("axe");
			assertTrue(WeaponAffixService.applyAccuracy(item, new Random(seed)));
			final double value = item.getDouble(WeaponAffixService.ACCURACY_ATTRIBUTE);
			assertTrue(value >= 5.0);
			assertTrue(value <= 15.0);
			assertEquals(Math.rint(value), value, 0.0);
		}
	}

	@Test
	public void criticalChanceRollUsesWholePercentagePoints() {
		for (int seed = 0; seed < 50; seed++) {
			final Item item = item("sword");
			assertTrue(WeaponAffixService.applyCriticalChance(item, new Random(seed)));
			final double value = item.getDouble(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE);
			assertTrue(value >= 3.0);
			assertTrue(value <= 10.0);
			assertEquals(Math.rint(value), value, 0.0);
		}
	}

	@Test
	public void criticalDamageRollUsesPersistentFraction() {
		assertFractionRoll("sword", CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE,
				10, 30, new AffixApplier() {
					@Override public boolean apply(Item item, Random random) {
						return WeaponAffixService.applyCriticalDamage(item, random);
					}
				});
	}

	@Test
	public void bleedRollUsesPersistentFraction() {
		assertFractionRoll("dagger", WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE,
				5, 15, new AffixApplier() {
					@Override public boolean apply(Item item, Random random) {
						return WeaponAffixService.applyBleedOnHit(item, random);
					}
				});
	}

	@Test
	public void executeRollUsesPersistentFraction() {
		assertFractionRoll("axe", WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE,
				10, 25, new AffixApplier() {
					@Override public boolean apply(Item item, Random random) {
						return WeaponAffixService.applyExecuteDamage(item, random);
					}
				});
	}

	@Test
	public void poisonRollUsesPersistentFraction() {
		assertFractionRoll("wand", WeaponAffixCombatService.POISON_ON_HIT_ATTRIBUTE,
				5, 12, new AffixApplier() {
					@Override public boolean apply(Item item, Random random) {
						return WeaponAffixService.applyPoisonOnHit(item, random);
					}
				});
	}

	@Test
	public void distanceDamageRollUsesPersistentFraction() {
		assertFractionRoll("ranged", WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE,
				10, 20, new AffixApplier() {
					@Override public boolean apply(Item item, Random random) {
						return WeaponAffixService.applyDistanceDamage(item, random);
					}
				});
	}

	@Test
	public void armorPenetrationRollUsesPersistentFraction() {
		assertFractionRoll("dagger",
				WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE,
				10, 25, new AffixApplier() {
					@Override public boolean apply(Item item, Random random) {
						return WeaponAffixService.applyArmorPenetration(item, random);
					}
				});
	}

	@Test
	public void classSpecificEligibilityBuildsDistinctPools() {
		assertTrue(WeaponAffixService.isCriticalDamageEligible(item("sword")));
		assertFalse(WeaponAffixService.isCriticalDamageEligible(item("wand")));

		assertTrue(WeaponAffixService.isBleedOnHitEligible(item("sword")));
		assertTrue(WeaponAffixService.isBleedOnHitEligible(item("dagger")));
		assertTrue(WeaponAffixService.isBleedOnHitEligible(item("axe")));
		assertTrue(WeaponAffixService.isBleedOnHitEligible(item("whip")));
		assertFalse(WeaponAffixService.isBleedOnHitEligible(item("wand")));

		assertTrue(WeaponAffixService.isExecuteDamageEligible(item("dagger")));
		assertTrue(WeaponAffixService.isExecuteDamageEligible(item("axe")));
		assertFalse(WeaponAffixService.isExecuteDamageEligible(item("sword")));

		assertTrue(WeaponAffixService.isPoisonOnHitEligible(item("dagger")));
		assertFalse(WeaponAffixService.isPoisonOnHitEligible(item("missile")));
		assertTrue(WeaponAffixService.isPoisonOnHitEligible(item("wand")));
		assertFalse(WeaponAffixService.isPoisonOnHitEligible(item("sword")));

		assertTrue(WeaponAffixService.isDistanceDamageEligible(item("ranged")));
		assertFalse(WeaponAffixService.isDistanceDamageEligible(item("missile")));
		assertTrue(WeaponAffixService.isDistanceDamageEligible(item("wand")));
		assertFalse(WeaponAffixService.isDistanceDamageEligible(item("sword")));
	}

	@Test
	public void missileCannotReceiveAnyWeaponAffix() {
		final Item missile = item("missile");
		assertFalse(WeaponAffixService.applyLifesteal(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyAccuracy(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalChance(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalDamage(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyBleedOnHit(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyExecuteDamage(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyPoisonOnHit(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyDistanceDamage(missile, new Random(1L)));
		assertFalse(WeaponAffixService.applyArmorPenetration(missile, new Random(1L)));
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
		item.put(CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE, 0.40);
		item.put(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE, 0.30);
		item.put(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE, 0.40);

		assertFalse(WeaponAffixService.applyLifesteal(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyAccuracy(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalChance(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalDamage(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyBleedOnHit(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyArmorPenetration(item, new Random(1L)));
		assertEquals(0.30, item.getDouble(WeaponAffixService.LIFESTEAL_ATTRIBUTE), 0.0);
		assertEquals(25.0, item.getDouble(WeaponAffixService.ACCURACY_ATTRIBUTE), 0.0);
		assertEquals(20.0, item.getDouble(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE), 0.0);
		assertEquals(0.40, item.getDouble(CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE), 0.0);
		assertEquals(0.30, item.getDouble(WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE), 0.0);
		assertEquals(0.40, item.getDouble(
				WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE), 0.0);
	}

	@Test
	public void nonWeaponCannotReceiveWeaponAffixes() {
		final Item item = item("armor");
		assertFalse(WeaponAffixService.applyLifesteal(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyAccuracy(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalChance(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyCriticalDamage(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyBleedOnHit(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyExecuteDamage(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyPoisonOnHit(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyDistanceDamage(item, new Random(1L)));
		assertFalse(WeaponAffixService.applyArmorPenetration(item, new Random(1L)));
	}

	private void assertFractionRoll(final String itemClass, final String attribute,
			final int minimumPercent, final int maximumPercent,
			final AffixApplier applier) {
		for (int seed = 0; seed < 50; seed++) {
			final Item item = item(itemClass);
			assertTrue(applier.apply(item, new Random(seed)));
			final double value = item.getDouble(attribute);
			assertTrue(value >= minimumPercent / 100.0);
			assertTrue(value <= maximumPercent / 100.0);
			assertEquals(Math.rint(value * 100.0), value * 100.0, 0.0000001);
		}
	}

	private Item item(final String itemClass) {
		return new Item("weapon affix test", itemClass, "test", null);
	}

	private interface AffixApplier {
		boolean apply(Item item, Random random);
	}
}
