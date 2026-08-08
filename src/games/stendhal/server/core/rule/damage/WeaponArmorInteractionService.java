/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;

/**
 * Resolves weapon-class advantages against semantic creature armor.
 * Creature DEF remains the normal defense stat and is intentionally not used
 * to infer armor: high DEF can represent agility, evasion or other defenses.
 */
public final class WeaponArmorInteractionService {
	private WeaponArmorInteractionService() {
		// utility class
	}

	public enum ArmorTier {
		NONE,
		LIGHT,
		MEDIUM,
		HEAVY
	}

	/**
	 * Returns the matchup multiplier for one held weapon and target. Keeping
	 * this decision per weapon allows mixed dual-wield sets to resolve each
	 * damage roll independently.
	 *
	 * @param weapon held weapon whose roll is being resolved
	 * @param defender attack target
	 * @return damage multiplier for this weapon roll
	 */
	public static double getDamageMultiplier(final Item weapon,
			final RPEntity defender) {
		if (weapon == null || !(defender instanceof Creature)) {
			return 1.0;
		}
		return getDamageMultiplier(weapon.getWeaponType(),
				((Creature) defender).getArmorType());
	}

	public static ArmorTier classify(final String armorType) {
		if ("light".equals(armorType)) {
			return ArmorTier.LIGHT;
		}
		if ("medium".equals(armorType)) {
			return ArmorTier.MEDIUM;
		}
		if ("heavy".equals(armorType)) {
			return ArmorTier.HEAVY;
		}
		return ArmorTier.NONE;
	}

	public static double getDamageMultiplier(final String weaponClass,
			final String armorType) {
		if (weaponClass == null) {
			return 1.0;
		}

		final ArmorTier tier = classify(armorType);
		if ("dagger".equals(weaponClass)) {
			return daggerMultiplier(tier);
		}
		if ("sword".equals(weaponClass)) {
			return swordMultiplier(tier);
		}
		if ("axe".equals(weaponClass) || "club".equals(weaponClass)) {
			return armorBreakerMultiplier(tier);
		}
		return 1.0;
	}

	private static double daggerMultiplier(final ArmorTier tier) {
		switch (tier) {
		case LIGHT:
			return 1.10;
		case MEDIUM:
			return 0.85;
		case HEAVY:
			return 0.60;
		case NONE:
		default:
			return 1.0;
		}
	}

	private static double swordMultiplier(final ArmorTier tier) {
		switch (tier) {
		case MEDIUM:
			return 1.10;
		case HEAVY:
			return 0.95;
		default:
			return 1.0;
		}
	}

	private static double armorBreakerMultiplier(final ArmorTier tier) {
		switch (tier) {
		case LIGHT:
			return 0.85;
		case HEAVY:
			return 1.25;
		case NONE:
		case MEDIUM:
		default:
			return 1.0;
		}
	}
}
