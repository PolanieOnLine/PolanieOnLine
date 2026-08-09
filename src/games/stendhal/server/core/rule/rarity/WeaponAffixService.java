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
	public static final int MIN_ARMOR_PENETRATION_PERCENT = 10;
	public static final int MAX_ARMOR_PENETRATION_PERCENT = 25;

	private static final Set<String> WEAPON_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("club", "sword", "dagger",
					"axe", "ranged", "missile", "wand", "whip")));
	private static final Set<String> ARMOR_MATCHUP_WEAPON_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"club", "sword", "dagger", "axe")));

	private WeaponAffixService() {
		// utility class
	}

	/** Returns whether this item may receive the requested random weapon affix. */
	public static boolean isEligible(final Item item, final String attribute) {
		return item != null && attribute != null
				&& WEAPON_CLASSES.contains(item.getItemClass())
				&& !item.has(attribute);
	}

	/** Returns whether armor penetration can affect this weapon's matchup. */
	public static boolean isArmorPenetrationEligible(final Item item) {
		return item != null
				&& ARMOR_MATCHUP_WEAPON_CLASSES.contains(item.getItemClass())
				&& !item.has(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE);
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
		item.put(LIFESTEAL_ATTRIBUTE, percent / 100.0);
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

	/**
	 * Rolls +3-10 percentage points of critical-hit chance. The combat service
	 * adds this value to the player's base critical chance.
	 */
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

	/**
	 * Rolls 10-25% semantic armor penetration for weapon classes which have an
	 * armor matchup. It is stored as a fraction, e.g. 20% becomes 0.20.
	 */
	public static boolean applyArmorPenetration(final Item item,
			final Random random) {
		if (!isArmorPenetrationEligible(item)) {
			return false;
		}
		final int percent = rollInclusive(random,
				MIN_ARMOR_PENETRATION_PERCENT,
				MAX_ARMOR_PENETRATION_PERCENT);
		item.put(WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE,
				percent / 100.0);
		return true;
	}

	private static int rollInclusive(final Random random, final int minimum,
			final int maximum) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		return minimum + random.nextInt(maximum - minimum + 1);
	}
}
