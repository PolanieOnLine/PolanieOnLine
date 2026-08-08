/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Random;

import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.entity.item.Item;

/**
 * Defines the random parry affix for swords.
 *
 * The affix selector is responsible for deciding whether parry was selected.
 * Once selected, this service rolls the concrete value exactly once and stores
 * it directly on the item instance.
 */
public final class ParryAffixService {
	public static final int MIN_PERCENT = 5;
	public static final int MAX_PERCENT = 15;

	private ParryAffixService() {
		// utility class
	}

	/**
	 * Returns whether the random parry affix may be rolled for this item.
	 * Random parry is intentionally a sword identity affix. Other melee weapon
	 * classes may still carry an intrinsic parry_chance when explicitly defined.
	 *
	 * @param item item being considered
	 * @return true only for swords without an existing parry value
	 */
	public static boolean isEligible(final Item item) {
		return item != null
				&& "sword".equals(item.getItemClass())
				&& !item.has(ParryService.PARRY_CHANCE_ATTRIBUTE);
	}

	/**
	 * Applies parry after the affix selector has chosen it for an item.
	 * Existing values are never rerolled.
	 *
	 * @param item selected item
	 * @param random random source
	 * @return rolled integer percentage, or 0 when the item is not eligible
	 */
	public static int applySelectedAffix(final Item item, final Random random) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		if (!isEligible(item)) {
			return 0;
		}

		final int percent = rollPercent(random);
		item.put(ParryService.PARRY_CHANCE_ATTRIBUTE, percent / 100.0);
		return percent;
	}

	/**
	 * Rolls a weighted whole-percent value between 5% and 15%.
	 * Mid-range rolls are most common while 14-15% remain deliberately rare.
	 */
	static int rollPercent(final Random random) {
		final double band = random.nextDouble();
		if (band < 0.25) {
			return randomInclusive(random, 5, 7);
		}
		if (band < 0.65) {
			return randomInclusive(random, 8, 10);
		}
		if (band < 0.90) {
			return randomInclusive(random, 11, 13);
		}
		return randomInclusive(random, 14, 15);
	}

	private static int randomInclusive(final Random random, final int minimum,
			final int maximum) {
		return minimum + random.nextInt(maximum - minimum + 1);
	}
}
