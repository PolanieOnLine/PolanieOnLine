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
package games.stendhal.server.entity.item;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.entity.status.StatusAttacker;

/**
 * Builds a small, presentation-safe map of final item statistics for clients.
 * Internal item attributes remain hidden; only selected values needed by the
 * tooltip are copied to the volatile wire map.
 */
public final class ItemTooltipService {
	private ItemTooltipService() {
		// utility class
	}

	/** Refreshes the client tooltip map from the current final item state. */
	public static void update(final Item item) {
		if (item == null) {
			return;
		}

		if (item.has(ItemTooltip.ATTRIBUTE)) {
			item.remove(ItemTooltip.ATTRIBUTE);
		}

		// Read the stored item values directly. Some subclasses calculate contextual
		// defense from their owner and cannot safely do that while being copied.
		putPositiveInt(item, ItemTooltip.ATTACK,
				item.getAttributeWithImprovement("atk", 0));
		putPositiveInt(item, ItemTooltip.RANGED_ATTACK,
				item.getAttributeWithImprovement("ratk", 0));
		putPositiveInt(item, ItemTooltip.DAMAGE_MIN, item.getDamageMin());
		putPositiveInt(item, ItemTooltip.DAMAGE_MAX, item.getDamageMax());
		if (item.has("atk") || item.has("ratk")) {
			putPositiveInt(item, ItemTooltip.ATTACK_RATE, item.getAttackRate());
		}
		putPositiveInt(item, ItemTooltip.DEFENSE,
				item.getAttributeWithImprovement("def", 0));
		putPositiveInt(item, ItemTooltip.RANGE, item.getRange());

		copyDouble(item, "lifesteal", ItemTooltip.LIFESTEAL);
		copyDouble(item, "accuracy_bonus", ItemTooltip.ACCURACY_BONUS);
		copyInt(item, "skill_atk", ItemTooltip.SKILL_ATTACK);
		copyDouble(item, "atk_additional_bonus", ItemTooltip.ATTACK_BONUS);
		copyDouble(item, "def_additional_bonus", ItemTooltip.DEFENSE_BONUS);
		copyInt(item, "rate_increase", ItemTooltip.RATE_INCREASE);
		copyDouble(item, "critical_chance", ItemTooltip.CRITICAL_CHANCE);
		copyDouble(item, "critical_additional_bonus", ItemTooltip.CRITICAL_BONUS);
		copyDouble(item, "lifesteal_increase", ItemTooltip.LIFESTEAL_INCREASE);
		copyInt(item, "health", ItemTooltip.HEALTH);
		copyInt(item, "min_level", ItemTooltip.MIN_LEVEL);
		copyInt(item, "min_use", ItemTooltip.MIN_USE);
		copyInt(item, "improve", ItemTooltip.IMPROVE);
		copyInt(item, "max_improves", ItemTooltip.MAX_IMPROVES);
		copyInt(item, "durability", ItemTooltip.DURABILITY);
		copyInt(item, "uses", ItemTooltip.USES);

		if (item.getValue() > 0) {
			put(item, ItemTooltip.VALUE, Integer.toString(item.getValue()));
		}

		if (item.getDamageType() != null) {
			put(item, ItemTooltip.DAMAGE_TYPE,
					item.getDamageType().name().toLowerCase());
		}

		final StringBuilder statuses = new StringBuilder();
		for (final StatusAttacker attacker : item.getStatusAttackers()) {
			if (statuses.length() > 0) {
				statuses.append(';');
			}
			statuses.append(attacker.getStatusName());
		}
		if (statuses.length() > 0) {
			put(item, ItemTooltip.STATUS_ATTACK, statuses.toString());
		}
	}

	private static void putPositiveInt(final Item item, final String key,
			final int value) {
		if (value > 0) {
			put(item, key, Integer.toString(value));
		}
	}

	private static void copyInt(final Item item, final String source,
			final String target) {
		if (item.has(source)) {
			put(item, target, Integer.toString(item.getInt(source)));
		}
	}

	private static void copyDouble(final Item item, final String source,
			final String target) {
		if (item.has(source)) {
			put(item, target, Double.toString(item.getDouble(source)));
		}
	}

	private static void put(final Item item, final String key,
			final String value) {
		item.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
