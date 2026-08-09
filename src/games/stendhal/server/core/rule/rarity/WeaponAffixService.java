/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.entity.item.Item;

/** Rolls shared offensive affixes for weapon instances. */
public final class WeaponAffixService {
	public static final String LIFESTEAL_ATTRIBUTE = "lifesteal";
	public static final String ACCURACY_ATTRIBUTE = "accuracy_bonus";

	public static final int MIN_LIFESTEAL_PERCENT = 3;
	public static final int MAX_LIFESTEAL_PERCENT = 10;
	public static final int MIN_ACCURACY_PERCENT = 5;
	public static final int MAX_ACCURACY_PERCENT = 15;
	public static final int MIN_CRITICAL_CHANCE_PERCENT = 3;
	public static final int MAX_CRITICAL_CHANCE_PERCENT = 10;
	public static final int MIN_CRITICAL_DAMAGE_PERCENT = 10;
	public static final int MAX_CRITICAL_DAMAGE_PERCENT = 30;
	public static final int MIN_BLEED_ON_HIT_PERCENT = 5;
	public static final int MAX_BLEED_ON_HIT_PERCENT = 15;
	public static final int MIN_EXECUTE_DAMAGE_PERCENT = 10;
	public static final int MAX_EXECUTE_DAMAGE_PERCENT = 25;
	public static final int MIN_POISON_ON_HIT_PERCENT = 5;
	public static final int MAX_POISON_ON_HIT_PERCENT = 12;
	public static final int MIN_DISTANCE_DAMAGE_PERCENT = 10;
	public static final int MAX_DISTANCE_DAMAGE_PERCENT = 20;
	public static final int MIN_ARMOR_PENETRATION_PERCENT = 10;
	public static final int MAX_ARMOR_PENETRATION_PERCENT = 25;

	private static final Set<String> WEAPON_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("club", "sword", "dagger",
					"axe", "ranged", "missile", "wand", "whip")));
	private static final Set<String> ARMOR_MATCHUP_WEAPON_CLASSES =
			classes("club", "sword", "dagger", "axe");
	private static final Set<String> CRITICAL_DAMAGE_WEAPON_CLASSES =
			classes("club", "sword", "dagger", "axe", "ranged", "missile",
					"whip");
	private static final Set<String> BLEED_WEAPON_CLASSES =
			classes("dagger", "axe", "whip", "sword");
	private static final Set<String> EXECUTE_WEAPON_CLASSES =
			classes("dagger", "axe");
	private static final Set<String> POISON_WEAPON_CLASSES =
			classes("dagger", "missile", "wand");
	private static final Set<String> DISTANCE_DAMAGE_WEAPON_CLASSES =
			classes("ranged", "missile", "wand");

	private WeaponAffixService() {
		// utility class
	}

	/** Returns whether this item may receive the requested random weapon affix. */
	public static boolean isEligible(final Item item, final String attribute) {
		return isEligibleForClasses(item, attribute, WEAPON_CLASSES);
	}

	public static boolean isCriticalDamageEligible(final Item item) {
		return isEligibleForClasses(item,
				CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE,
				CRITICAL_DAMAGE_WEAPON_CLASSES);
	}

	public static boolean isBleedOnHitEligible(final Item item) {
		return isEligibleForClasses(item,
				WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE,
				BLEED_WEAPON_CLASSES);
	}

	public static boolean isExecuteDamageEligible(final Item item) {
		return isEligibleForClasses(item,
				WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE,
				EXECUTE_WEAPON_CLASSES);
	}

	public static boolean isPoisonOnHitEligible(final Item item) {
		return isEligibleForClasses(item,
				WeaponAffixCombatService.POISON_ON_HIT_ATTRIBUTE,
				POISON_WEAPON_CLASSES);
	}

	public static boolean isDistanceDamageEligible(final Item item) {
		return isEligibleForClasses(item,
				WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE,
				DISTANCE_DAMAGE_WEAPON_CLASSES);
	}

	/** Returns whether armor penetration can affect this weapon's matchup. */
	public static boolean isArmorPenetrationEligible(final Item item) {
		return isEligibleForClasses(item,
				WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE,
				ARMOR_MATCHUP_WEAPON_CLASSES);
	}

	/**
	 * Rolls 3-10% lifesteal and stores it as the combat fraction used by the
	 * existing lifesteal implementation (for example 7% becomes 0.07).
	 */
	public static boolean applyLifesteal(final Item item, final Random random) {
		if (!isEligible(item, LIFESTEAL_ATTRIBUTE)) {
			return false;
		}
		final int percent = rollInclusive(random,
				MIN_LIFESTEAL_PERCENT, MAX_LIFESTEAL_PERCENT);
		item.put(LIFESTEAL_ATTRIBUTE,
				ItemRollPrecision.round(percent / 100.0));
		return true;
	}

	/**
	 * Rolls +5-15% accuracy. Accuracy uses whole percentage points in the
	 * existing hit-chance implementation, so 12% is stored as 12.0.
	 */
	public static boolean applyAccuracy(final Item item, final Random random) {
		if (!isEligible(item, ACCURACY_ATTRIBUTE)) {
			return false;
		}
		final int percent = rollInclusive(random,
				MIN_ACCURACY_PERCENT, MAX_ACCURACY_PERCENT);
		item.put(ACCURACY_ATTRIBUTE, (double) percent);
		return true;
	}

	/** Rolls +3-10 percentage points of critical-hit chance. */
	public static boolean applyCriticalChance(final Item item,
			final Random random) {
		if (!isEligible(item, CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE)) {
			return false;
		}
		final int percent = rollInclusive(random,
				MIN_CRITICAL_CHANCE_PERCENT, MAX_CRITICAL_CHANCE_PERCENT);
		item.put(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE, (double) percent);
		return true;
	}

	/** Rolls +10-30% damage for successful critical hits. */
	public static boolean applyCriticalDamage(final Item item,
			final Random random) {
		if (!isCriticalDamageEligible(item)) {
			return false;
		}
		putPercentFraction(item, CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE,
				rollInclusive(random, MIN_CRITICAL_DAMAGE_PERCENT,
						MAX_CRITICAL_DAMAGE_PERCENT));
		return true;
	}

	/** Rolls 5-15% chance to inflict bleeding on a damaging hit. */
	public static boolean applyBleedOnHit(final Item item, final Random random) {
		if (!isBleedOnHitEligible(item)) {
			return false;
		}
		putPercentFraction(item, WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE,
				rollInclusive(random, MIN_BLEED_ON_HIT_PERCENT,
						MAX_BLEED_ON_HIT_PERCENT));
		return true;
	}

	/** Rolls +10-25% damage against targets below 25% maximum HP. */
	public static boolean applyExecuteDamage(final Item item,
			final Random random) {
		if (!isExecuteDamageEligible(item)) {
			return false;
		}
		putPercentFraction(item, WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE,
				rollInclusive(random, MIN_EXECUTE_DAMAGE_PERCENT,
						MAX_EXECUTE_DAMAGE_PERCENT));
		return true;
	}

	/** Rolls 5-12% chance to poison on a damaging hit. */
	public static boolean applyPoisonOnHit(final Item item, final Random random) {
		if (!isPoisonOnHitEligible(item)) {
			return false;
		}
		putPercentFraction(item, WeaponAffixCombatService.POISON_ON_HIT_ATTRIBUTE,
				rollInclusive(random, MIN_POISON_ON_HIT_PERCENT,
						MAX_POISON_ON_HIT_PERCENT));
		return true;
	}

	/** Rolls +10-20% damage for attacks actually performed from distance. */
	public static boolean applyDistanceDamage(final Item item,
			final Random random) {
		if (!isDistanceDamageEligible(item)) {
			return false;
		}
		putPercentFraction(item, WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE,
				rollInclusive(random, MIN_DISTANCE_DAMAGE_PERCENT,
						MAX_DISTANCE_DAMAGE_PERCENT));
		return true;
	}

	/**
	 * Rolls 10-25% semantic armor penetration for weapon classes which have an
	 * armor matchup. It is stored as a fraction, e.g. 20% becomes 0.20.
	 */
	public static boolean applyArmorPenetration(final Item item,
			final Random random) {
		if (!isArmorPenetrationEligible(item)) {
			return false;
		}
		putPercentFraction(item,
				WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE,
				rollInclusive(random, MIN_ARMOR_PENETRATION_PERCENT,
						MAX_ARMOR_PENETRATION_PERCENT));
		return true;
	}

	private static boolean isEligibleForClasses(final Item item,
			final String attribute, final Set<String> eligibleClasses) {
		return item != null && attribute != null && eligibleClasses != null
				&& eligibleClasses.contains(item.getItemClass())
				&& !item.has(attribute);
	}

	private static void putPercentFraction(final Item item,
			final String attribute, final int percent) {
		item.put(attribute, ItemRollPrecision.round(percent / 100.0));
	}

	private static Set<String> classes(final String... values) {
		return Collections.unmodifiableSet(
				new HashSet<String>(Arrays.asList(values)));
	}

	private static int rollInclusive(final Random random, final int minimum,
			final int maximum) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		return minimum + random.nextInt(maximum - minimum + 1);
	}
}
