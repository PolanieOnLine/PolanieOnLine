/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
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
	public void daggerHasExtremeLightAdvantageAndArmorPenalties() {
		assertEquals(1.20, multiplier("dagger", "light"), 0.0);
		assertEquals(0.80, multiplier("dagger", "medium"), 0.0);
		assertEquals(0.40, multiplier("dagger", "heavy"), 0.0);
	}

	@Test
	public void daggerIsBestAgainstLightArmor() {
		final double dagger = multiplier("dagger", "light");
		assertTrue(dagger > multiplier("sword", "light"));
		assertTrue(dagger > multiplier("axe", "light"));
	}

	@Test
	public void swordHasUniversalPositiveLightAndMediumProfile() {
		assertEquals(1.10, multiplier("sword", "light"), 0.0);
		assertEquals(1.30, multiplier("sword", "medium"), 0.0);
		assertEquals(0.80, multiplier("sword", "heavy"), 0.0);
	}

	@Test
	public void swordIsBestAgainstMediumArmor() {
		final double sword = multiplier("sword", "medium");
		assertTrue(sword > multiplier("dagger", "medium"));
		assertTrue(sword > multiplier("axe", "medium"));
	}

	@Test
	public void axeAndClubHaveStrongArmorBreakerProfile() {
		assertEquals(0.90, multiplier("axe", "light"), 0.0);
		assertEquals(1.10, multiplier("axe", "medium"), 0.0);
		assertEquals(1.30, multiplier("axe", "heavy"), 0.0);
		assertEquals(0.90, multiplier("club", "light"), 0.0);
		assertEquals(1.10, multiplier("club", "medium"), 0.0);
		assertEquals(1.30, multiplier("club", "heavy"), 0.0);
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
	public void armorPenetrationOnlyMitigatesNegativeMatchups() {
		assertEquals(0.55, WeaponArmorInteractionService.applyArmorPenetration(
				0.40, 0.25), 0.0000001);
		assertEquals(0.85, WeaponArmorInteractionService.applyArmorPenetration(
				0.80, 0.25), 0.0000001);
		assertEquals(0.925, WeaponArmorInteractionService.applyArmorPenetration(
				0.90, 0.25), 0.0000001);
		assertEquals(1.30, WeaponArmorInteractionService.applyArmorPenetration(
				1.30, 0.25), 0.0);
		assertEquals(1.0, WeaponArmorInteractionService.applyArmorPenetration(
				1.0, 0.25), 0.0);
	}

	@Test
	public void normalAndLegendaryPenetrationCombineIndependently() {
		assertEquals(0.55, WeaponArmorInteractionService.combineIndependentFractions(
				0.25, 0.40), 0.0000001);
	}

	@Test
	public void armorPenetrationHasSafeBoundaries() {
		assertEquals(0.40, WeaponArmorInteractionService.applyArmorPenetration(
				0.40, 0.0), 0.0);
		assertEquals(0.40, WeaponArmorInteractionService.applyArmorPenetration(
				0.40, -0.20), 0.0);
		assertEquals(1.0, WeaponArmorInteractionService.applyArmorPenetration(
				0.40, 1.0), 0.0);
		assertEquals(1.0, WeaponArmorInteractionService.applyArmorPenetration(
				0.40, 2.0), 0.0);
	}

	@Test
	public void weaponArmorPenetrationAffectsResolvedMatchup() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");
		final Weapon dagger = weapon("dagger");
		dagger.put(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE, 0.25);

		assertEquals(0.55, WeaponArmorInteractionService.getDamageMultiplier(
				dagger, defender), 0.0000001);
		assertEquals(55, WeaponArmorInteractionService.applyDamageMultiplier(
				100, Arrays.<Item>asList(dagger), defender));
	}

	@Test
	public void legendaryArmorBreakerMitigatesFortyPercentOfPenalty() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");
		final Weapon dagger = weapon("dagger");
		dagger.put(WeaponArmorInteractionService.LEGENDARY_ARMOR_BREAKER_ATTRIBUTE,
				1.0);

		assertEquals(0.64, WeaponArmorInteractionService.getDamageMultiplier(
				dagger, defender), 0.0000001);
	}

	@Test
	public void normalAndLegendaryArmorBreakerStackWithoutTouchingDefense() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");
		defender.setDef(150);
		final Weapon dagger = weapon("dagger");
		dagger.put(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE, 0.25);
		dagger.put(WeaponArmorInteractionService.LEGENDARY_ARMOR_BREAKER_ATTRIBUTE,
				1.0);

		assertEquals(0.73, WeaponArmorInteractionService.getDamageMultiplier(
				dagger, defender), 0.0000001);
		assertEquals(150, defender.getDef());
	}

	@Test
	public void penetrationDoesNotAmplifyAdvantageousWeaponMatchups() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");
		final Weapon axe = weapon("axe");
		axe.put(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE, 0.25);
		axe.put(WeaponArmorInteractionService.LEGENDARY_ARMOR_BREAKER_ATTRIBUTE,
				1.0);

		assertEquals(1.30, WeaponArmorInteractionService.getDamageMultiplier(
				axe, defender), 0.0);
	}

	@Test
	public void unsupportedWeaponClassRemainsNeutral() {
		assertEquals(1.0, multiplier("wand", "heavy"), 0.0);
		assertEquals(1.0, multiplier(null, "heavy"), 0.0);
	}

	@Test
	public void highDefenseWithoutArmorTypeRemainsNeutral() {
		final Creature defender = new Creature();
		defender.setDef(150);

		assertEquals(1.0, WeaponArmorInteractionService.getDamageMultiplier(
				weapon("dagger"), defender), 0.0);
	}

	@Test
	public void explicitArmorTypeControlsMultiplierIndependentlyOfDefense() {
		final Creature defender = new Creature();
		defender.setDef(1);
		defender.setArmorType("heavy");

		assertEquals(1.30, WeaponArmorInteractionService.getDamageMultiplier(
				weapon("axe"), defender), 0.0);
		assertEquals(0.40, WeaponArmorInteractionService.getDamageMultiplier(
				weapon("dagger"), defender), 0.0);
	}

	@Test
	public void differentWeaponsResolveDifferentMultipliersAgainstSameTarget() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		assertEquals(0.40, WeaponArmorInteractionService.getDamageMultiplier(
				weapon("dagger"), defender), 0.0);
		assertEquals(1.30, WeaponArmorInteractionService.getDamageMultiplier(
				weapon("axe"), defender), 0.0);
	}

	@Test
	public void resolvedDamageUsesFinalMatchupPercentage() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		assertEquals(40, WeaponArmorInteractionService.applyDamageMultiplier(
				100, Arrays.<Item>asList(weapon("dagger")), defender));
		assertEquals(130, WeaponArmorInteractionService.applyDamageMultiplier(
				100, Arrays.<Item>asList(weapon("axe")), defender));
	}

	@Test
	public void zeroAndTenPercentScaleTheResolvedHitDirectly() {
		assertEquals(0, WeaponArmorInteractionService.scaleDamage(100, 0.0));
		assertEquals(10, WeaponArmorInteractionService.scaleDamage(100, 0.10));
	}

	@Test
	public void pairedWeaponsUseDamageWeightedMatchup() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");

		// Equal 30-damage weapons: dagger 40%, axe 130% => 85% total.
		assertEquals(85, WeaponArmorInteractionService.applyDamageMultiplier(
				100, Arrays.<Item>asList(weapon("dagger"), weapon("axe")),
				defender));
	}

	@Test
	public void nonCreatureTargetRemainsNeutral() {
		final RPEntity target = new RPEntity() {
			@Override
			protected void dropItemsOn(final Corpse corpse) {
				// no items
			}

			@Override
			public void logic() {
				// no logic
			}
		};

		assertEquals(1.0, WeaponArmorInteractionService.getDamageMultiplier(
				weapon("dagger"), target), 0.0);
		assertEquals(100, WeaponArmorInteractionService.applyDamageMultiplier(
				100, Arrays.<Item>asList(weapon("dagger")), target));
	}

	@Test
	public void missingWeaponRemainsNeutral() {
		final Creature defender = new Creature();
		defender.setArmorType("heavy");
		assertEquals(1.0, WeaponArmorInteractionService.getDamageMultiplier(
				null, defender), 0.0);
	}

	private double multiplier(final String weaponClass,
			final String armorType) {
		return WeaponArmorInteractionService.getDamageMultiplier(
				weaponClass, armorType);
	}

	private Weapon weapon(final String itemClass) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "5");
		return new Weapon("test weapon", itemClass, "test", attributes);
	}
}
