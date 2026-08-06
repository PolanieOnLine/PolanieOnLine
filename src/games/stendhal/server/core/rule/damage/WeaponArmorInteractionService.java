/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;

/**
 * Applies small, explicit weapon-class advantages against creature armor.
 * The existing creature DEF is used as the default armor score, so the
 * matchup system covers all current creatures without duplicating defense.
 */
public final class WeaponArmorInteractionService {
	private static final int LIGHT_ARMOR_MAX = 30;
	private static final int MEDIUM_ARMOR_MAX = 80;

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
	 * Adjusts only the primary weapon contribution. Bonuses from rings, glyphs
	 * and other equipment stay neutral and are not incorrectly treated as the
	 * weapon itself.
	 */
	public static double adjustAttack(final double totalItemAttack,
			final Item primaryWeapon, final RPEntity defender) {
		if (primaryWeapon == null || !(defender instanceof Creature)) {
			return totalItemAttack;
		}

		final int armorScore = ((Creature) defender).getArmor();
		final double multiplier = getDamageMultiplier(
				primaryWeapon.getWeaponType(), armorScore);
		return adjustWeaponContribution(totalItemAttack,
				primaryWeapon.getAverageDamage(), multiplier);
	}

	/**
	 * Applies a matchup multiplier only to the weapon's part of the complete
	 * equipment attack. Package visibility keeps the arithmetic directly
	 * testable without constructing the entire combat engine.
	 */
	static double adjustWeaponContribution(final double totalItemAttack,
			final double weaponContribution, final double multiplier) {
		final double safeWeaponContribution = Math.max(0.0,
				weaponContribution);
		return Math.max(0.0, totalItemAttack
				+ safeWeaponContribution * (multiplier - 1.0));
	}

	public static ArmorTier classify(final int armorScore) {
		if (armorScore <= 0) {
			return ArmorTier.NONE;
		}
		if (armorScore <= LIGHT_ARMOR_MAX) {
			return ArmorTier.LIGHT;
		}
		if (armorScore <= MEDIUM_ARMOR_MAX) {
			return ArmorTier.MEDIUM;
		}
		return ArmorTier.HEAVY;
	}

	public static double getDamageMultiplier(final String weaponClass,
			final int armorScore) {
		if (weaponClass == null) {
			return 1.0;
		}

		final ArmorTier tier = classify(armorScore);
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
		case NONE:
		case LIGHT:
			return 1.10;
		case MEDIUM:
			return 0.95;
		case HEAVY:
			return 0.80;
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
		case NONE:
		case LIGHT:
			return 0.95;
		case HEAVY:
			return 1.15;
		default:
			return 1.0;
		}
	}
}
