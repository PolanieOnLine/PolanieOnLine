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
package games.stendhal.common.constants;

/** Shared wire identifiers used by structured item tooltips. */
public final class ItemTooltip {
	/** Volatile map sent to clients with presentation-safe item statistics. */
	public static final String ATTRIBUTE = "tooltip_stats";
	/** Hidden definition override populated from the item XML. */
	public static final String CATEGORY_OVERRIDE = "tooltip_category_override";

	public static final String CATEGORY = "category";
	public static final String CATEGORY_WEAPON = "weapon";
	public static final String CATEGORY_ARMOUR = "armour";
	public static final String CATEGORY_ACCESSORY = "accessory";
	public static final String CATEGORY_OTHER = "other";

	public static final String ATTACK = "atk";
	public static final String RANGED_ATTACK = "ratk";
	public static final String ATTACK_RATE = "rate";
	public static final String ATTACK_INTERVAL_SECONDS = "attack_interval_seconds";
	public static final String ATTACKS_PER_SECOND = "attacks_per_second";
	public static final String DAMAGE_MIN = "damage_min";
	public static final String DAMAGE_MAX = "damage_max";
	public static final String DEFENSE = "def";
	/** Prefix for final resistance percentages, e.g. resistance_light. */
	public static final String RESISTANCE_PREFIX = "resistance_";
	public static final String RANGE = "range";
	public static final String LIFESTEAL = "lifesteal";
	public static final String PARRY_CHANCE = "parry_chance";
	public static final String ARMOR_PENETRATION = "armor_penetration";
	public static final String CRITICAL_DAMAGE_BONUS = "critical_damage_bonus";
	public static final String BLEED_ON_HIT = "bleed_on_hit";
	public static final String EXECUTE_DAMAGE = "execute_damage";
	public static final String POISON_ON_HIT = "poison_on_hit";
	public static final String DISTANCE_DAMAGE = "distance_damage";
	public static final String DAMAGE_TYPE = "damage_type";
	public static final String STATUS_ATTACK = "statusattack";
	public static final String ACCURACY_BONUS = "accuracy_bonus";
	public static final String SKILL_ATTACK = "skill_atk";
	public static final String ATTACK_BONUS = "atk_additional_bonus";
	public static final String DEFENSE_BONUS = "def_additional_bonus";
	public static final String RATE_INCREASE = "rate_increase";
	public static final String CRITICAL_CHANCE = "critical_chance";
	public static final String CRITICAL_BONUS = "critical_additional_bonus";
	public static final String LIFESTEAL_INCREASE = "lifesteal_increase";
	public static final String HEALTH = "health";
	public static final String MIN_LEVEL = "min_level";
	public static final String MIN_USE = "min_use";
	public static final String IMPROVE = "improve";
	public static final String MAX_IMPROVES = "max_improves";
	public static final String DURABILITY = "durability";
	public static final String USES = "uses";
	public static final String VALUE = "value";

	private ItemTooltip() {
		// constants class
	}

	/**
	 * Checks whether a value can be used as a structured tooltip category.
	 *
	 * @param category category identifier
	 * @return {@code true} for one of the supported stable identifiers
	 */
	public static boolean isValidCategory(final String category) {
		return CATEGORY_WEAPON.equals(category)
				|| CATEGORY_ARMOUR.equals(category)
				|| CATEGORY_ACCESSORY.equals(category)
				|| CATEGORY_OTHER.equals(category);
	}
}
