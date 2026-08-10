/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Random;

import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.entity.item.Item;

/** Materializes legendary-only weapon bonuses which alter persistent stats. */
public final class LegendaryWeaponAffixService {
	public static final int MERCILESS_REACH_RANGE_BONUS = 1;

	private LegendaryWeaponAffixService() {
		// utility class
	}

	public static boolean isMercilessReachEligible(final Item item) {
		return item != null && "whip".equals(item.getItemClass())
				&& !item.has(WeaponAffixCombatService.LEGENDARY_MERCILESS_REACH_ATTRIBUTE);
	}

	/** Adds exactly one tile to the materialized attack range of a whip. */
	public static boolean applyMercilessReach(final Item item, final Random random) {
		if (!isMercilessReachEligible(item)) {
			return false;
		}
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		final int currentRange = item.has("range")
				? Math.max(0, item.getInt("range")) : 0;
		item.put(WeaponAffixCombatService.LEGENDARY_MERCILESS_REACH_ATTRIBUTE, 1.0);
		item.put("range", Math.min(Short.MAX_VALUE,
				currentRange + MERCILESS_REACH_RANGE_BONUS));
		return true;
	}
}
