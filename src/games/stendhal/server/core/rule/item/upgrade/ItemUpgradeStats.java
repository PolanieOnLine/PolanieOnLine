/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.item.upgrade;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.WeaponImpl;

/** Final gameplay statistics displayed by the item-upgrade preview. */
public final class ItemUpgradeStats {
	public static final String ATTACK = "atk";
	public static final String RANGED_ATTACK = "ratk";
	public static final String DAMAGE_MIN = "damage_min";
	public static final String DAMAGE_MAX = "damage_max";
	public static final String DEFENSE = "def";
	public static final String RANGE = "range";
	public static final String ATTACK_RATE = "rate";

	private final Map<String, Integer> values;

	private ItemUpgradeStats(final Map<String, Integer> values) {
		this.values = Collections.unmodifiableMap(values);
	}

	/**
	 * Captures the same final values used by combat and tooltips for an explicit
	 * upgrade level. Rarity and affixes have already been materialized on the
	 * item instance and are deliberately not reapplied here.
	 */
	public static ItemUpgradeStats atLevel(final Item item,
			final int upgradeLevel) {
		final Map<String, Integer> stats = new LinkedHashMap<String, Integer>();
		if (item.has(ATTACK)) {
			stats.put(ATTACK, item.getAttackAtUpgradeLevel(upgradeLevel));
		}
		if (item.has(RANGED_ATTACK)) {
			stats.put(RANGED_ATTACK,
					item.getRangedAttackAtUpgradeLevel(upgradeLevel));
		}
		if (item instanceof WeaponImpl) {
			stats.put(DAMAGE_MIN,
					item.getDamageMinAtUpgradeLevel(upgradeLevel));
			stats.put(DAMAGE_MAX,
					item.getDamageMaxAtUpgradeLevel(upgradeLevel));
		}
		if (item.has(DEFENSE)) {
			stats.put(DEFENSE, item.getDefenseAtUpgradeLevel(upgradeLevel));
		}
		return new ItemUpgradeStats(stats);
	}

	/** Add a stat that changes only when the item reaches its maximum level. */
	ItemUpgradeStats with(final String statistic, final int value) {
		final Map<String, Integer> copy =
				new LinkedHashMap<String, Integer>(values);
		copy.put(statistic, value);
		return new ItemUpgradeStats(copy);
	}

	public Map<String, Integer> getValues() {
		return values;
	}
}
