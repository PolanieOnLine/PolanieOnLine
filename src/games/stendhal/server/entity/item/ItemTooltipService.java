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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import games.stendhal.common.constants.GameTiming;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.entity.status.StatusAttacker;

/** Builds a presentation-safe map of final item statistics for clients. */
public final class ItemTooltipService {
	private static final Set<String> WEAPON_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("club", "sword", "dagger",
					"axe", "ranged", "missile", "wand", "whip")));
	private static final Set<String> ARMOUR_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("armor", "shield", "helmet",
					"cloak", "boots", "glove", "gloves", "legs", "belt", "belts")));
	private static final Set<String> ACCESSORY_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("ring", "necklace")));

	private ItemTooltipService() {
		// utility class
	}

	public static void update(final Item item) {
		if (item == null) {
			return;
		}

		if (item.has(ItemTooltip.ATTRIBUTE)) {
			item.remove(ItemTooltip.ATTRIBUTE);
		}

		put(item, ItemTooltip.CATEGORY, resolveCategory(item));
		putPositiveInt(item, ItemTooltip.ATTACK,
				item.getAttributeWithImprovement("atk", 0));
		putPositiveInt(item, ItemTooltip.RANGED_ATTACK,
				item.getAttributeWithImprovement("ratk", 0));
		putPositiveInt(item, ItemTooltip.DAMAGE_MIN, item.getDamageMin());
		putPositiveInt(item, ItemTooltip.DAMAGE_MAX, item.getDamageMax());
		if (item.has("atk") || item.has("ratk")) {
			final int attackRate = item.getAttackRate();
			putPositiveInt(item, ItemTooltip.ATTACK_RATE, attackRate);
			if (attackRate > 0) {
				final double interval = attackRate * GameTiming.SECONDS_PER_TURN;
				put(item, ItemTooltip.ATTACK_INTERVAL_SECONDS,
						Double.toString(interval));
				put(item, ItemTooltip.ATTACKS_PER_SECOND,
						Double.toString(1.0 / interval));
			}
		}
		putPositiveInt(item, ItemTooltip.DEFENSE,
				item.getAttributeWithImprovement("def", 0));
		for (final java.util.Map.Entry<Nature, Double> entry
				: item.getSusceptibilities().entrySet()) {
			final int resistance = (int) Math.round(
					200.0 - (100.0 * entry.getValue().doubleValue()));
			put(item, ItemTooltip.RESISTANCE_PREFIX
					+ entry.getKey().name().toLowerCase(Locale.ROOT),
					Integer.toString(resistance));
		}
		putPositiveInt(item, ItemTooltip.RANGE, item.getRange());

		copyDouble(item, "lifesteal", ItemTooltip.LIFESTEAL);
		copyDouble(item, ItemTooltip.PARRY_CHANCE, ItemTooltip.PARRY_CHANCE);
		copyDouble(item, ItemTooltip.ARMOR_PENETRATION,
				ItemTooltip.ARMOR_PENETRATION);
		copyDouble(item, ItemTooltip.CRITICAL_DAMAGE_BONUS,
				ItemTooltip.CRITICAL_DAMAGE_BONUS);
		copyDouble(item, ItemTooltip.BLEED_ON_HIT, ItemTooltip.BLEED_ON_HIT);
		copyDouble(item, ItemTooltip.LEGENDARY_DEEP_WOUNDS,
				ItemTooltip.LEGENDARY_DEEP_WOUNDS);
		copyDouble(item, ItemTooltip.EXECUTE_DAMAGE, ItemTooltip.EXECUTE_DAMAGE);
		copyDouble(item, ItemTooltip.POISON_ON_HIT, ItemTooltip.POISON_ON_HIT);
		copyDouble(item, ItemTooltip.DISTANCE_DAMAGE, ItemTooltip.DISTANCE_DAMAGE);
		copyInt(item, ItemTooltip.FLAT_ATTACK_BONUS, ItemTooltip.FLAT_ATTACK_BONUS);
		copyInt(item, ItemTooltip.FLAT_DEFENSE_BONUS, ItemTooltip.FLAT_DEFENSE_BONUS);
		copyDouble(item, ItemTooltip.RESIST_POISONED, ItemTooltip.RESIST_POISONED);
		copyDouble(item, ItemTooltip.RESIST_BLEEDING, ItemTooltip.RESIST_BLEEDING);
		copyDouble(item, ItemTooltip.RESIST_SHOCKED, ItemTooltip.RESIST_SHOCKED);
		copyDouble(item, ItemTooltip.RESIST_CONFUSED, ItemTooltip.RESIST_CONFUSED);
		copyDouble(item, ItemTooltip.RESIST_HEAVY, ItemTooltip.RESIST_HEAVY);
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
			appendStatus(statuses, attacker.getStatusName());
		}
		if (item.has(ItemTooltip.LEGENDARY_DEEP_WOUNDS)) {
			appendStatus(statuses,
					"Głębokie Rany (15% szansy, 35% obrażeń trafienia)");
		}
		if (statuses.length() > 0) {
			put(item, ItemTooltip.STATUS_ATTACK, statuses.toString());
		}
	}

	private static void appendStatus(final StringBuilder statuses,
			final String status) {
		if (status == null || status.length() == 0) {
			return;
		}
		if (statuses.length() > 0) {
			statuses.append(';');
		}
		statuses.append(status);
	}

	private static String resolveCategory(final Item item) {
		if (item.has(ItemTooltip.CATEGORY_OVERRIDE)) {
			final String override = item.get(ItemTooltip.CATEGORY_OVERRIDE);
			if (ItemTooltip.isValidCategory(override)) {
				return override;
			}
		}
		final String itemClass = item.getItemClass();
		if (WEAPON_CLASSES.contains(itemClass)) {
			return ItemTooltip.CATEGORY_WEAPON;
		}
		if (ARMOUR_CLASSES.contains(itemClass)) {
			return ItemTooltip.CATEGORY_ARMOUR;
		}
		if (ACCESSORY_CLASSES.contains(itemClass)) {
			return ItemTooltip.CATEGORY_ACCESSORY;
		}
		return ItemTooltip.CATEGORY_OTHER;
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
