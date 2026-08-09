/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.EquipmentStatusResistanceService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StatusResistantItem;
import games.stendhal.server.entity.status.StatusType;

/** Rolls random affixes for armour and accessory instances. */
public final class EquipmentAffixService {
	public static final String FLAT_ATTACK_BONUS_ATTRIBUTE = "flat_attack_bonus";
	public static final String FLAT_DEFENSE_BONUS_ATTRIBUTE = "flat_defense_bonus";

	public static final int MIN_ACCESSORY_ATTACK_BONUS = 1;
	public static final int MAX_ACCESSORY_ATTACK_BONUS = 3;
	public static final int MIN_ACCESSORY_DEFENSE_BONUS = 1;
	public static final int MAX_ACCESSORY_DEFENSE_BONUS = 3;
	public static final int MIN_STATUS_RESISTANCE_PERCENT = 10;
	public static final int MAX_STATUS_RESISTANCE_PERCENT = 25;
	public static final int MIN_ACCESSORY_ACCURACY_PERCENT = 3;
	public static final int MAX_ACCESSORY_ACCURACY_PERCENT = 10;
	public static final int MIN_ACCESSORY_CRITICAL_CHANCE_PERCENT = 2;
	public static final int MAX_ACCESSORY_CRITICAL_CHANCE_PERCENT = 7;
	public static final int MIN_ACCESSORY_CRITICAL_DAMAGE_PERCENT = 5;
	public static final int MAX_ACCESSORY_CRITICAL_DAMAGE_PERCENT = 15;

	private static final Set<String> ARMOUR_CLASSES = classes("armor", "shield",
			"helmet", "cloak", "boots", "glove", "gloves", "legs", "belt", "belts");
	private static final Set<String> ACCESSORY_CLASSES = classes("ring", "necklace");

	private EquipmentAffixService() {
		// utility class
	}

	public static boolean isArmour(final Item item) {
		return item != null && ARMOUR_CLASSES.contains(item.getItemClass());
	}

	public static boolean isAccessory(final Item item) {
		return item != null && ACCESSORY_CLASSES.contains(item.getItemClass());
	}

	public static boolean isEquipment(final Item item) {
		return isArmour(item) || isAccessory(item);
	}

	public static boolean isFlatAttackEligible(final Item item) {
		return isAccessory(item) && !item.has(FLAT_ATTACK_BONUS_ATTRIBUTE);
	}

	public static boolean isFlatDefenseEligible(final Item item) {
		return isEquipment(item) && !item.has(FLAT_DEFENSE_BONUS_ATTRIBUTE);
	}

	public static boolean isStatusResistanceEligible(final Item item,
			final StatusType statusType) {
		final String attribute = EquipmentStatusResistanceService.getResistanceAttribute(statusType);
		if (!isEquipment(item) || attribute == null || item.has(attribute)) {
			return false;
		}
		if (item instanceof StatusResistantItem) {
			final StatusResistantItem resistantItem = (StatusResistantItem) item;
			if (resistantItem.getStatusResistancesList().getMap().containsKey(statusType)) {
				return false;
			}
		}
		return true;
	}

	public static boolean isAccessoryAccuracyEligible(final Item item) {
		return isAccessory(item) && !item.has(WeaponAffixService.ACCURACY_ATTRIBUTE);
	}

	public static boolean isAccessoryCriticalChanceEligible(final Item item) {
		return isAccessory(item)
				&& !item.has(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE);
	}

	public static boolean isAccessoryCriticalDamageEligible(final Item item) {
		return isAccessory(item)
				&& !item.has(CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE);
	}

	/** Adds an exact persistent flat ATK bonus to a jewellery instance. */
	public static boolean applyFlatAttack(final Item item, final Random random) {
		if (!isFlatAttackEligible(item)) {
			return false;
		}
		final int bonus = rollInclusive(random, MIN_ACCESSORY_ATTACK_BONUS,
				MAX_ACCESSORY_ATTACK_BONUS);
		item.put(FLAT_ATTACK_BONUS_ATTRIBUTE, bonus);
		item.put("atk", clampShort((item.has("atk") ? item.getInt("atk") : 0) + bonus));
		return true;
	}

	/**
	 * Adds an exact persistent flat DEF bonus. Jewellery rolls 1-3 points. Armour
	 * rolls roughly 5-15% of its current rarity-scaled DEF, with at least 1 point.
	 */
	public static boolean applyFlatDefense(final Item item, final Random random) {
		if (!isFlatDefenseEligible(item)) {
			return false;
		}
		final int currentDefense = item.has("def") ? Math.max(0, item.getInt("def")) : 0;
		final int minimum;
		final int maximum;
		if (isAccessory(item)) {
			minimum = MIN_ACCESSORY_DEFENSE_BONUS;
			maximum = MAX_ACCESSORY_DEFENSE_BONUS;
		} else {
			minimum = Math.max(1, (int) Math.ceil(currentDefense * 0.05));
			maximum = Math.max(minimum, (int) Math.ceil(currentDefense * 0.15));
		}
		final int bonus = rollInclusive(random, minimum, maximum);
		item.put(FLAT_DEFENSE_BONUS_ATTRIBUTE, bonus);
		item.put("def", clampShort(currentDefense + bonus));
		return true;
	}

	/** Rolls 10-25% resistance to one combat status and stores it as a fraction. */
	public static boolean applyStatusResistance(final Item item,
			final StatusType statusType, final Random random) {
		if (!isStatusResistanceEligible(item, statusType)) {
			return false;
		}
		final String attribute = EquipmentStatusResistanceService.getResistanceAttribute(statusType);
		final int percent = rollInclusive(random, MIN_STATUS_RESISTANCE_PERCENT,
				MAX_STATUS_RESISTANCE_PERCENT);
		item.put(attribute, ItemRollPrecision.round(percent / 100.0));
		return true;
	}

	/** Rolls +3-10 percentage points of accessory accuracy. */
	public static boolean applyAccessoryAccuracy(final Item item,
			final Random random) {
		if (!isAccessoryAccuracyEligible(item)) {
			return false;
		}
		item.put(WeaponAffixService.ACCURACY_ATTRIBUTE,
				(double) rollInclusive(random, MIN_ACCESSORY_ACCURACY_PERCENT,
						MAX_ACCESSORY_ACCURACY_PERCENT));
		return true;
	}

	/** Rolls +2-7 percentage points of accessory critical-hit chance. */
	public static boolean applyAccessoryCriticalChance(final Item item,
			final Random random) {
		if (!isAccessoryCriticalChanceEligible(item)) {
			return false;
		}
		item.put(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE,
				(double) rollInclusive(random, MIN_ACCESSORY_CRITICAL_CHANCE_PERCENT,
						MAX_ACCESSORY_CRITICAL_CHANCE_PERCENT));
		return true;
	}

	/** Rolls +5-15% critical-hit damage for jewellery. */
	public static boolean applyAccessoryCriticalDamage(final Item item,
			final Random random) {
		if (!isAccessoryCriticalDamageEligible(item)) {
			return false;
		}
		item.put(CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE,
				ItemRollPrecision.round(rollInclusive(random,
						MIN_ACCESSORY_CRITICAL_DAMAGE_PERCENT,
						MAX_ACCESSORY_CRITICAL_DAMAGE_PERCENT) / 100.0));
		return true;
	}

	private static int clampShort(final int value) {
		return Math.min(Short.MAX_VALUE, Math.max(Short.MIN_VALUE, value));
	}

	private static Set<String> classes(final String... values) {
		return Collections.unmodifiableSet(
				new HashSet<String>(Arrays.asList(values)));
	}

	private static int rollInclusive(final Random random, final int minimum,
			final int maximum) {
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		return minimum + random.nextInt(maximum - minimum + 1);
	}
}
