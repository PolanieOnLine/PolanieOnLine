/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService.ArmorTier;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Corpse;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import utilities.RPClass.CreatureTestHelper;
import utilities.RPClass.ItemTestHelper;

public class WeaponArmorInteractionServiceTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
		CreatureTestHelper.generateRPClasses();
	}

	@Test
	public void classifiesSemanticArmorTiers() {
		assertEquals(ArmorTier.NONE,
				WeaponArmorInteractionService.classify(null));
		assertEquals(ArmorTier.NONE,
				WeaponArmorInteractionService.classify("none"));
		assertEquals(ArmorTier.LIGHT,
				WeaponArmorInteractionService.classify("light"));
		assertEquals(ArmorTier.MEDIUM,
				WeaponArmorInteractionService.classify("medium"));
		assertEquals(ArmorTier.HEAVY,
				WeaponArmorInteractionService.classify("heavy"));
		assertEquals(ArmorTier.NONE,
				WeaponArmorInteractionService.classify("unknown"));
	}

	@Test
	public void unarmoredTargetsAreNeutralForSupportedWeaponClasses() {
		assertEquals(1.0, multiplier("dagger", "none"), 0.0);
		assertEquals(1.0, multiplier("sword", "none"), 0.0);
		assertEquals(1.0, multiplier("axe", "none"), 0.0);
		assertEquals(1.0, multiplier("club", "none"), 0.0);
	}

	@Test
	public void daggerIsBestAgainstLightArmor() {
		final double dagger = multiplier("dagger", "light");
		assertTrue(dagger > multiplier("sword", "light"));
		assertTrue(dagger > multiplier("axe", "light"));
	}

	@Test
	public void swordIsBestAgainstMediumArmor() {
		final double sword = multiplier("sword", "medium");
		assertTrue(sword > multiplier("dagger", "medium"));
		assertTrue(sword > multiplier("axe", "medium"));
	}

	@Test
	public void axeAndClubAreBestAgainstHeavyArmor() {
		final double axe = multiplier("axe", "heavy");
		final double club = multiplier("club", "heavy");
		assertEquals(axe, club, 0.0);
		assertTrue(axe > multiplier("sword", "heavy"));
		assertTrue(axe > multiplier("dagger", "heavy"));
	}

	@Test
	public void unsupportedWeaponClassRemainsNeutral() {
		assertEquals(1.0, multiplier("wand", "heavy"), 0.0);
		assertEquals(1.0, multiplier(null, "heavy"), 0.0);
	}

	@Test
	public void highDefenseWithoutArmorTypeRemainsUnarmored() {
		final Creature defender = new Creature();
		defender.setDef(150);
		final Weapon weapon = weapon("dagger", 30);

		assertEquals(50.0, WeaponArmorInteractionService.adjustAttack(
				50.0, 50.0, Arrays.asList(weapon), weapon, defender),
				0.000001);
	}

	@Test
	public void explicitArmorTypeControlsMatchupIndependentlyOfDefense() {
		final Creature defender = new Creature();
		defender.setDef(1);
		defender.setArmorType("heavy");
		final Weapon weapon = weapon("axe", 30);

		assertEquals(expectedAdjusted(50.0, 50.0, weapon, 1.15),
				WeaponArmorInteractionService.adjustAttack(50.0, 50.0,
						Arrays.asList(weapon), weapon, defender), 0.000001);
	}

	@Test
	public void matchupUsesActualRolledWeaponContribution() {
		final Creature defender = new Creature();
		defender.setArmorType("light");
		final Weapon weapon = weapon("dagger", 30);
		final double stableAttack = 50.0;
		final double rolledAttack = 44.0;

		final double stableWeapon = weapon.getAverageDamage();
		final double rolledWeapon = rolledAttack
				- (stableAttack - stableWeapon);
		final double expected = rolledAttack + rolledWeapon * 0.10;

		assertEquals(expected, WeaponArmorInteractionService.adjustAttack(
				rolledAttack, stableAttack, Arrays.asList(weapon), weapon,
				defender), 0.000001);
		assertTrue(Math.abs(expected - (rolledAttack + stableWeapon * 0.10))
				> 0.000001);
	}

	@Test
	public void pairedWeaponsAreAdjustedWithoutMultiplyingFlatEquipmentAttack() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");
		final Weapon first = weapon("axe", 20);
		final Weapon second = weapon("axe", 10);
		final double stableAttack = 80.0;
		final double rolledAttack = 75.0;
		final double stableWeapons = first.getAverageDamage()
				+ second.getAverageDamage();
		final double stableNonWeaponAttack = stableAttack - stableWeapons;
		final double rolledWeapons = rolledAttack - stableNonWeaponAttack;
		final double expected = rolledAttack + rolledWeapons * 0.15;

		assertEquals(expected, WeaponArmorInteractionService.adjustAttack(
				rolledAttack, stableAttack, Arrays.asList(first, second), first,
				defender), 0.000001);
		assertTrue(Math.abs(expected - rolledAttack * 1.15) > 0.000001);
	}

	@Test
	public void nonCreatureTargetRemainsNeutral() {
		final Weapon weapon = weapon("dagger", 30);
		final RPEntity playerTarget = new RPEntity() {
			@Override
			protected void dropItemsOn(final Corpse corpse) {
				// no items
			}

			@Override
			public void logic() {
				// no logic
			}
		};

		assertEquals(50.0, WeaponArmorInteractionService.adjustAttack(
				50.0, 50.0, Arrays.asList(weapon), weapon, playerTarget),
				0.000001);
	}

	@Test
	public void missingWeaponRemainsNeutral() {
		assertEquals(25.0, WeaponArmorInteractionService.adjustAttack(
				25.0, 25.0, Collections.<Item>emptyList(), null, null),
				0.000001);
	}

	@Test
	public void negativeWeaponContributionCannotCorruptAttack() {
		assertEquals(25.0,
				WeaponArmorInteractionService.adjustWeaponContribution(
						25.0, -10.0, 1.15), 0.000001);
	}

	private double expectedAdjusted(final double rolledAttack,
			final double stableAttack, final Weapon weapon,
			final double multiplier) {
		final double rolledWeapon = rolledAttack
				- (stableAttack - weapon.getAverageDamage());
		return rolledAttack + rolledWeapon * (multiplier - 1.0);
	}

	private double multiplier(final String weaponClass,
			final String armorType) {
		return WeaponArmorInteractionService.getDamageMultiplier(
				weaponClass, armorType);
	}

	private Weapon weapon(final String itemClass, final int attack) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", Integer.toString(attack));
		attributes.put("rate", "5");
		return new Weapon("test weapon", itemClass, "test", attributes);
	}
}
