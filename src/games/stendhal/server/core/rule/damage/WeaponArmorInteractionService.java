/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;

/**
 * Applies small, explicit weapon-class advantages against creature armor.
 * Existing DEF remains the general defensive statistic; armor selects the
 * matchup profile and therefore does not double the creature's mitigation.
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

		final int armor = ((Creature) defender).getArmor();
		final double multiplier = getDamageMultiplier(
				primaryWeapon.getWeaponType(), armor);
		final double weaponContribution = Math.max(0.0,
				primaryWeapon.getAverageDamage());
		return Math.max(0.0, totalItemAttack
				+ weaponContribution * (multiplier - 1.0));
	}

	public static ArmorTier classify(final int armor) {
		if (armor <= 0) {
			return ArmorTier.NONE;
		}
		if (armor <= LIGHT_ARMOR_MAX) {
			return ArmorTier.LIGHT;
		}
		if (armor <= MEDIUM_ARMOR_MAX) {
			return ArmorTier.MEDIUM;
		}
		return ArmorTier.HEAVY;
	}

	public static double getDamageMultiplier(final String weaponClass,
			final int armor) {
		if (weaponClass == null) {
			return 1.0;
		}

		final ArmorTier tier = classify(armor);
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
