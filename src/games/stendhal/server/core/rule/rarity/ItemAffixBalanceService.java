/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.EquipmentStatusResistanceService;
import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.status.StatusType;

/** Applies current per-item limits without rerolling saved affixes or upgrades. */
public final class ItemAffixBalanceService {
	private ItemAffixBalanceService() { }

	/**
	 * Run after rarity scaling and flat-bonus materialization, before tooltip
	 * generation. Only recorded random affixes are limited; definition-owned
	 * and unclassified legacy bonuses retain their values. Glyphs and legendary
	 * signature effects are separate authored systems. Lifesteal and parry are exempt.
	 */
	public static void enforce(final Item item) {
		if (item == null || item instanceof Glyph) return;
		final boolean weapon = WeaponAffixService.isWeapon(item);
		final boolean accessory = EquipmentAffixService.isAccessory(item);
		if (!weapon && !EquipmentAffixService.isEquipment(item)) return;
		cap(item, CombatAffixDefinition.CRUSHING.getAttribute(), 0.15);
		cap(item, CombatAffixDefinition.HARDENING.getAttribute(), 0.08);

		if (weapon || accessory) {
			cap(item, WeaponAffixService.ACCURACY_ATTRIBUTE, weapon
					? WeaponAffixService.MAX_ACCURACY_PERCENT
					: EquipmentAffixService.MAX_ACCESSORY_ACCURACY_PERCENT);
			cap(item, CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE, weapon
					? WeaponAffixService.MAX_CRITICAL_CHANCE_PERCENT
					: EquipmentAffixService.MAX_ACCESSORY_CRITICAL_CHANCE_PERCENT);
			cap(item, CriticalHitService.CRITICAL_DAMAGE_BONUS_ATTRIBUTE, (weapon
					? WeaponAffixService.MAX_CRITICAL_DAMAGE_PERCENT
					: EquipmentAffixService.MAX_ACCESSORY_CRITICAL_DAMAGE_PERCENT) / 100.0);
		}
		if (weapon) {
			cap(item, WeaponAffixCombatService.BLEED_ON_HIT_ATTRIBUTE,
					WeaponAffixService.MAX_BLEED_ON_HIT_PERCENT / 100.0);
			cap(item, WeaponAffixCombatService.POISON_ON_HIT_ATTRIBUTE,
					WeaponAffixService.MAX_POISON_ON_HIT_PERCENT / 100.0);
			cap(item, WeaponAffixCombatService.EXECUTE_DAMAGE_ATTRIBUTE,
					WeaponAffixService.MAX_EXECUTE_DAMAGE_PERCENT / 100.0);
			cap(item, WeaponAffixCombatService.DISTANCE_DAMAGE_ATTRIBUTE,
					WeaponAffixService.MAX_DISTANCE_DAMAGE_PERCENT / 100.0);
			cap(item, WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE,
					WeaponAffixService.MAX_ARMOR_PENETRATION_PERCENT / 100.0);
		}
		if (EquipmentAffixService.isEquipment(item)) {
			upgradeSpikedPlating(item);
			for (final StatusType status : StatusType.values()) {
				cap(item, EquipmentStatusResistanceService.getResistanceAttribute(status),
						EquipmentAffixService.MAX_STATUS_RESISTANCE_PERCENT / 100.0);
			}
			cap(item, EquipmentAffixService.SPIKED_PLATING_ATTRIBUTE,
					EquipmentAffixService.MAX_SPIKED_PLATING_PERCENT / 100.0);
		}
		if (accessory && item.has(EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE)
				&& ItemAffixState.has(item, EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE)) {
			final String attribute = EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE;
			final int previous = item.getInt(attribute);
			final int maximum = EquipmentAffixService.MAX_ACCESSORY_ATTACK_BONUS;
			if (previous > maximum) {
				// The flat bonus is already included in raw ATK, unlike upgrades.
				if (item.has("atk")) {
					item.put("atk", (int) Math.max(Short.MIN_VALUE,
							(long) item.getInt("atk") - previous + maximum));
				}
				item.put(attribute, maximum);
				updateRecordedValue(item, attribute);
			}
		}
	}

	private static void cap(final Item item, final String attribute, final double maximum) {
		if (!item.has(attribute) || !ItemAffixState.has(item, attribute)) return;
		final double value = item.getDouble(attribute);
		// Preserve lower rolls and deliberate negative drawbacks exactly.
		if (value > maximum) {
			item.put(attribute, maximum);
			updateRecordedValue(item, attribute);
		}
	}

	/** Keeps the relative strength of recorded 2-4% rolls in the new 20-30% range. */
	private static void upgradeSpikedPlating(final Item item) {
		final String attribute = EquipmentAffixService.SPIKED_PLATING_ATTRIBUTE;
		if (!item.has(attribute) || !ItemAffixState.has(item, attribute)
				|| !("armor".equals(item.getItemClass()) || "shield".equals(item.getItemClass()))) {
			return;
		}
		final double value = item.getDouble(attribute);
		if (value >= 0.02 && value <= 0.04) {
			final double rollPosition = (value - 0.02) / 0.02;
			final double percent = EquipmentAffixService.MIN_SPIKED_PLATING_PERCENT
					+ rollPosition * (EquipmentAffixService.MAX_SPIKED_PLATING_PERCENT
							- EquipmentAffixService.MIN_SPIKED_PLATING_PERCENT);
			item.put(attribute, ItemRollPrecision.round(percent / 100.0));
			updateRecordedValue(item, attribute);
		}
	}

	private static void updateRecordedValue(final Item item, final String attribute) {
		if (ItemAffixState.has(item, attribute)) {
			item.put(ItemAffixState.ATTRIBUTE, attribute, item.get(attribute));
		}
	}
}
