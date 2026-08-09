/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import games.stendhal.server.entity.item.Item;

/** Rolls shared offensive affixes for weapon instances. */
public final class WeaponAffixService {
	public static final String LIFESTEAL_ATTRIBUTE = "lifesteal";
	public static final String ACCURACY_ATTRIBUTE = "accuracy_bonus";

	public static final int MIN_LIFESTEAL_PERCENT = 3;
	public static final int MAX_LIFESTEAL_PERCENT = 10;
	public static final int MIN_ACCURACY_PERCENT = 5;
	public static final int MAX_ACCURACY_PERCENT = 15;

	private static final Set<String> WEAPON_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("club", "sword", "dagger",
					"axe", "ranged", "missile", "wand", "whip")));

	private WeaponAffixService() {
		// utility class
	}

	/** Returns whether this item may receive the requested random weapon affix. */
	public static boolean isEligible(final Item item, final String attribute) {
		return item != null && attribute != null
				&& WEAPON_CLASSES.contains(item.getItemClass())
				&& !item.has(attribute);
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

	private static int rollInclusive(final Random random, final int minimum,
			final int maximum) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		return minimum + random.nextInt(maximum - minimum + 1);
	}
}
