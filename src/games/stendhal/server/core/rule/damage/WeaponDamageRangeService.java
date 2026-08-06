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
	 * Initializes a damage range for a newly-created weapon instance.
	 * Restoration deliberately skips generation so old saved items without a
	 * range keep their historical fixed {@code atk-atk} behavior.
	 *
	 * @param item item being created
	 * @param context item creation context
	 */
	public static void initialize(final Item item,
			final ItemCreationContext context) {
		if (item == null || item instanceof StackableItem
				|| !(item instanceof WeaponImpl)
				|| item.has(Item.RARITY_ID)
				|| (context != null && context.isRestore())) {
			return;
		}

		final int attack = Math.max(item.getAttack(), item.getRangedAttack());
		if (attack <= 0) {
			return;
		}

		if (hasValidExplicitRange(item)) {
			return;
		}

		// A partial or invalid override must not create a malformed range.
		if (item.has("damage_min")) {
			item.remove("damage_min");
		}
		if (item.has("damage_max")) {
			item.remove("damage_max");
		}

		final DamageRange range = calculate(item.getItemClass(),
				item.getAttackRate(), attack);
		item.put("damage_min", range.getMinimum());
		item.put("damage_max", range.getMaximum());
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

	private static boolean hasValidExplicitRange(final Item item) {
		if (!item.has("damage_min") || !item.has("damage_max")) {
			return false;
		}
		final int minimum = item.getInt("damage_min");
		final int maximum = item.getInt("damage_max");
		return minimum > 0 && maximum >= minimum;
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
