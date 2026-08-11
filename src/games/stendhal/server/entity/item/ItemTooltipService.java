/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
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

import games.stendhal.common.Constants;
import games.stendhal.common.constants.GameTiming;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.common.constants.Nature;
import games.stendhal.server.core.rule.rarity.LegendaryWeaponAffixService;
import games.stendhal.server.entity.status.StatusAttacker;

/** Builds a presentation-safe map of final item statistics for clients. */
public final class ItemTooltipService {
	/* Keep item value calculation/persistence available for future economy UI,
	 * but do not expose it in player-facing tooltips for now. */
	private static final boolean PUBLISH_ITEM_VALUE = false;

	private static final Set<String> WEAPON_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("club", "sword", "dagger",
					"axe", "ranged", "wand", "whip")));
	private static final Set<String> ARMOUR_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("armor", "shield", "helmet",
					"cloak", "boots", "glove", "gloves", "legs", "belt", "belts")));
	private static final Set<String> ACCESSORY_CLASSES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList("ring", "necklace")));
	private static final Set<String> EQUIPMENT_SLOTS = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList(Constants.EQUIPMENT_SLOTS)));

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
		publishEquipmentSlots(item);
		putPositiveInt(item, ItemTooltip.ATTACK, displayedAttack(item));
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
		putPositiveInt(item, ItemTooltip.DEFENSE, displayedDefense(item));
		for (final java.util.Map.Entry<Nature, Double> entry
				: item.getSusceptibilities().entrySet()) {
			final int resistance = (int) Math.round(
					200.0 - (100.0 * entry.getValue().doubleValue()));
			put(item, ItemTooltip.RESISTANCE_PREFIX
					+ entry.getKey().name().toLowerCase(Locale.ROOT),
					Integer.toString(resistance));
		}
		putPositiveInt(item, ItemTooltip.RANGE, displayedRange(item));

		copyDouble(item, "lifesteal", ItemTooltip.LIFESTEAL);
		copyDouble(item, ItemTooltip.PARRY_CHANCE, ItemTooltip.PARRY_CHANCE);
		copyDouble(item, ItemTooltip.ARMOR_PENETRATION,
				ItemTooltip.ARMOR_PENETRATION);
		copyDouble(item, ItemTooltip.CRITICAL_DAMAGE_BONUS,
				ItemTooltip.CRITICAL_DAMAGE_BONUS);
		copyDouble(item, ItemTooltip.BLEED_ON_HIT, ItemTooltip.BLEED_ON_HIT);
		copyDouble(item, ItemTooltip.LEGENDARY_DEEP_WOUNDS,
				ItemTooltip.LEGENDARY_DEEP_WOUNDS);
		copyDouble(item, ItemTooltip.LEGENDARY_ARMOR_BREAKER,
				ItemTooltip.LEGENDARY_ARMOR_BREAKER);
		copyDouble(item, ItemTooltip.LEGENDARY_LONGSHOT,
				ItemTooltip.LEGENDARY_LONGSHOT);
		copyDouble(item, ItemTooltip.LEGENDARY_EXECUTIONER,
				ItemTooltip.LEGENDARY_EXECUTIONER);
		copyDouble(item, ItemTooltip.LEGENDARY_DUEL_MASTER,
				ItemTooltip.LEGENDARY_DUEL_MASTER);
		copyDouble(item, ItemTooltip.LEGENDARY_CRUSHING_BLOW,
				ItemTooltip.LEGENDARY_CRUSHING_BLOW);
		copyDouble(item, ItemTooltip.LEGENDARY_STUNNING_FORCE,
				ItemTooltip.LEGENDARY_STUNNING_FORCE);
		copyDouble(item, ItemTooltip.LEGENDARY_BINDING_STRIKE,
				ItemTooltip.LEGENDARY_BINDING_STRIKE);
		copyDouble(item, ItemTooltip.LEGENDARY_MERCILESS_REACH,
				ItemTooltip.LEGENDARY_MERCILESS_REACH);
		copyDouble(item, ItemTooltip.LEGENDARY_FALCON_EYE,
				ItemTooltip.LEGENDARY_FALCON_EYE);
		copyDouble(item, ItemTooltip.LEGENDARY_FIRST_SALVO,
				ItemTooltip.LEGENDARY_FIRST_SALVO);
		copyDouble(item, ItemTooltip.LEGENDARY_POWER_OVERLOAD,
				ItemTooltip.LEGENDARY_POWER_OVERLOAD);
		copyDouble(item, ItemTooltip.LEGENDARY_ARCANE_FOCUS,
				ItemTooltip.LEGENDARY_ARCANE_FOCUS);
		copyInt(item, ItemTooltip.LEGENDARY_BASTION_BONUS,
				ItemTooltip.LEGENDARY_BASTION_BONUS);
		copyDouble(item, ItemTooltip.LEGENDARY_IRON_WILL,
				ItemTooltip.LEGENDARY_IRON_WILL);
		copyDouble(item, ItemTooltip.LEGENDARY_UNYIELDING_PROTECTION,
				ItemTooltip.LEGENDARY_UNYIELDING_PROTECTION);
		copyInt(item, ItemTooltip.LEGENDARY_RELIC_POWER,
				ItemTooltip.LEGENDARY_RELIC_POWER);
		copyDouble(item, ItemTooltip.LEGENDARY_HERO_EYE,
				ItemTooltip.LEGENDARY_HERO_EYE);
		copyDouble(item, ItemTooltip.LEGENDARY_GUARDIAN_SEAL,
				ItemTooltip.LEGENDARY_GUARDIAN_SEAL);
		copyDouble(item, ItemTooltip.EXECUTE_DAMAGE, ItemTooltip.EXECUTE_DAMAGE);
		copyDouble(item, ItemTooltip.POISON_ON_HIT, ItemTooltip.POISON_ON_HIT);
		copyDouble(item, ItemTooltip.DISTANCE_DAMAGE, ItemTooltip.DISTANCE_DAMAGE);
		copyInt(item, ItemTooltip.FLAT_ATTACK_BONUS,
				ItemTooltip.AFFIX_FLAT_ATTACK_BONUS);
		copyInt(item, ItemTooltip.FLAT_DEFENSE_BONUS,
				ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS);
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

		if (PUBLISH_ITEM_VALUE && item.getValue() > 0) {
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
		if (statuses.length() > 0) {
			put(item, ItemTooltip.STATUS_ATTACK, statuses.toString());
		}
	}

	private static void publishEquipmentSlots(final Item item) {
		final StringBuilder slots = new StringBuilder();
		for (final String slot : item.getPossibleSlots()) {
			if (!EQUIPMENT_SLOTS.contains(slot)) {
				continue;
			}
			if (slots.length() > 0) {
				slots.append(';');
			}
			slots.append(slot);
		}
		if (slots.length() > 0) {
			put(item, ItemTooltip.EQUIPMENT_SLOTS, slots.toString());
		}
	}

	private static int displayedAttack(final Item item) {
		int attack = item.getAttributeWithImprovement("atk", 0);
		attack -= intAttribute(item, ItemTooltip.FLAT_ATTACK_BONUS);
		attack -= intAttribute(item, ItemTooltip.LEGENDARY_RELIC_POWER);
		return Math.max(0, attack);
	}

	private static int displayedDefense(final Item item) {
		int defense = item.getAttributeWithImprovement("def", 0);
		defense -= intAttribute(item, ItemTooltip.FLAT_DEFENSE_BONUS);
		defense -= intAttribute(item, ItemTooltip.LEGENDARY_BASTION_BONUS);
		return Math.max(0, defense);
	}

	private static int displayedRange(final Item item) {
		int range = item.getRange();
		if (item.has(ItemTooltip.LEGENDARY_MERCILESS_REACH)) {
			range -= LegendaryWeaponAffixService.MERCILESS_REACH_RANGE_BONUS;
		}
		return Math.max(0, range);
	}

	private static int intAttribute(final Item item, final String attribute) {
		return item.has(attribute) ? item.getInt(attribute) : 0;
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
