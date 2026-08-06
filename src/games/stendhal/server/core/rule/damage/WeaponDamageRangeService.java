/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.WeaponImpl;

/**
 * Creates a stable per-instance weapon damage range from the legacy attack
 * value. Explicit {@code damage_min}/{@code damage_max} attributes remain an
 * opt-in override for exceptional weapons.
 */
public final class WeaponDamageRangeService {
	private static final double DEFAULT_SPREAD = 0.10;
	private static final double DAGGER_SPREAD = 0.05;
	private static final double AXE_SPREAD = 0.15;
	private static final double HEAVY_WEAPON_SPREAD = 0.20;
	private static final int HEAVY_WEAPON_RATE = 9;
	private static final int MINIMUM_ATTACK_FOR_VARIANCE = 4;

	private WeaponDamageRangeService() {
		// utility class
	}

	/**
	 * Initializes a damage range for a newly-created weapon instance. Restore
	 * contexts are handled after the saved instance values have been applied by
	 * {@link #migrateRestored(Item, int, Integer, Integer)}.
	 *
	 * @param item item being created
	 * @param context item creation context
	 */
	public static void initialize(final Item item,
			final ItemCreationContext context) {
		if (!isEligibleWeapon(item) || item.has(Item.RARITY_ID)
				|| (context != null && context.isRestore())) {
			return;
		}
		initializeGeneratedRange(item);
	}

	/**
	 * Adds a range to an existing saved weapon which predates the range system.
	 * The calculation uses the final stored attack value restored from the
	 * database, so old and newly-created copies of the same weapon follow the
	 * same combat rules.
	 *
	 * A valid saved range is authoritative and is never recalculated. When an
	 * exceptional weapon defines a range in XML, its template is scaled from the
	 * definition attack to the restored instance attack.
	 *
	 * @param item restored item after all saved values and converters were applied
	 * @param definitionAttack attack value from the current XML definition
	 * @param definitionMinimum optional XML damage minimum
	 * @param definitionMaximum optional XML damage maximum
	 */
	public static void migrateRestored(final Item item,
			final int definitionAttack, final Integer definitionMinimum,
			final Integer definitionMaximum) {
		if (!isEligibleWeapon(item) || hasValidRange(item)) {
			return;
		}

		removeRange(item);
		final int attack = getStoredAttack(item);
		if (attack <= 0) {
			return;
		}

		final DamageRange range;
		if (hasValidRange(definitionMinimum, definitionMaximum)
				&& definitionAttack > 0) {
			final double scale = attack / (double) definitionAttack;
			final int minimum = scaleEndpoint(
					definitionMinimum.intValue(), scale);
			final int maximum = Math.max(minimum, scaleEndpoint(
					definitionMaximum.intValue(), scale));
			range = new DamageRange(minimum, maximum);
		} else {
			range = calculate(item.getItemClass(), getStoredAttackRate(item),
					attack);
		}

		storeRange(item, range);
		copyAttackRarityModifier(item);
	}

	private static void initializeGeneratedRange(final Item item) {
		final int attack = getStoredAttack(item);
		if (attack <= 0 || hasValidRange(item)) {
			return;
		}

		// A partial or invalid override must not create a malformed range.
		removeRange(item);
		storeRange(item, calculate(item.getItemClass(),
				getStoredAttackRate(item), attack));
	}

	static DamageRange calculate(final String itemClass, final int attackRate,
			final int attack) {
		if (attack < MINIMUM_ATTACK_FOR_VARIANCE) {
			return new DamageRange(Math.max(1, attack), Math.max(1, attack));
		}

		final double spread = resolveSpread(itemClass, attackRate);
		final int difference = Math.max(1,
				(int) Math.round(attack * spread));
		return new DamageRange(Math.max(1, attack - difference),
				Math.min(Short.MAX_VALUE, attack + difference));
	}

	private static boolean isEligibleWeapon(final Item item) {
		return item != null && !(item instanceof StackableItem)
				&& item instanceof WeaponImpl;
	}

	private static int getStoredAttack(final Item item) {
		final int melee = item.has("atk") ? Math.max(0, item.getInt("atk")) : 0;
		final int ranged = item.has("ratk") ? Math.max(0, item.getInt("ratk")) : 0;
		return Math.max(melee, ranged);
	}

	private static int getStoredAttackRate(final Item item) {
		if (item.has("rate") && item.getInt("rate") > 0) {
			return item.getInt("rate");
		}
		return Item.getDefaultAttackRate();
	}

	private static boolean hasValidRange(final Item item) {
		if (!item.has("damage_min") || !item.has("damage_max")) {
			return false;
		}
		return hasValidRange(Integer.valueOf(item.getInt("damage_min")),
				Integer.valueOf(item.getInt("damage_max")));
	}

	private static boolean hasValidRange(final Integer minimum,
			final Integer maximum) {
		return minimum != null && maximum != null
				&& minimum.intValue() > 0
				&& maximum.intValue() >= minimum.intValue();
	}

	private static int scaleEndpoint(final int endpoint, final double scale) {
		return Math.max(1, Math.min(Short.MAX_VALUE,
				(int) Math.round(endpoint * scale)));
	}

	private static void storeRange(final Item item, final DamageRange range) {
		item.put("damage_min", range.getMinimum());
		item.put("damage_max", range.getMaximum());
	}

	private static void removeRange(final Item item) {
		if (item.has("damage_min")) {
			item.remove("damage_min");
		}
		if (item.has("damage_max")) {
			item.remove("damage_max");
		}
	}

	private static void copyAttackRarityModifier(final Item item) {
		Double multiplier = item.getRarityModifier("atk");
		if (multiplier == null) {
			multiplier = item.getRarityModifier("ratk");
		}
		if (multiplier != null) {
			item.setRarityModifier("damage_min", multiplier.doubleValue());
			item.setRarityModifier("damage_max", multiplier.doubleValue());
		}
	}

	private static double resolveSpread(final String itemClass,
			final int attackRate) {
		if (attackRate >= HEAVY_WEAPON_RATE) {
			return HEAVY_WEAPON_SPREAD;
		}
		if ("dagger".equals(itemClass)) {
			return DAGGER_SPREAD;
		}
		if ("axe".equals(itemClass)) {
			return AXE_SPREAD;
		}
		return DEFAULT_SPREAD;
	}

	/** Immutable generated range. */
	static final class DamageRange {
		private final int minimum;
		private final int maximum;

		private DamageRange(final int minimum, final int maximum) {
			this.minimum = minimum;
			this.maximum = maximum;
		}

		int getMinimum() {
			return minimum;
		}

		int getMaximum() {
			return maximum;
		}
	}
}
