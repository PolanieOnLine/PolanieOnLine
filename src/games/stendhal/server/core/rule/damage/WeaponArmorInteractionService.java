/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.List;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;

/**
 * Resolves weapon-class advantages against semantic creature armor.
 * Creature DEF remains the normal defense stat and is intentionally not used
 * to infer armor: high DEF can represent agility, evasion or other defenses.
 */
public final class WeaponArmorInteractionService {
	public static final String ARMOR_PENETRATION_ATTRIBUTE = "armor_penetration";

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
	 * Returns the matchup multiplier for one held weapon and target. Armor
	 * penetration only mitigates a negative matchup towards neutral 1.0; it
	 * never increases an already neutral or advantageous matchup.
	 *
	 * @param weapon held weapon being resolved
	 * @param defender attack target
	 * @return damage multiplier for this weapon
	 */
	public static double getDamageMultiplier(final Item weapon,
			final RPEntity defender) {
		if (weapon == null || !(defender instanceof Creature)) {
			return 1.0;
		}
		final double baseMultiplier = getDamageMultiplier(weapon.getWeaponType(),
				((Creature) defender).getArmorType());
		final double penetration = weapon.has(ARMOR_PENETRATION_ATTRIBUTE)
				? weapon.getDouble(ARMOR_PENETRATION_ATTRIBUTE) : 0.0;
		return applyArmorPenetration(baseMultiplier, penetration);
	}

	/**
	 * Moves a disadvantage multiplier towards neutral by the penetrated share.
	 * For example 0.40 with 25% penetration becomes 0.55. Advantageous values
	 * such as 1.30 are intentionally unchanged.
	 */
	public static double applyArmorPenetration(final double baseMultiplier,
			final double penetration) {
		if (baseMultiplier >= 1.0 || penetration <= 0.0
				|| Double.isNaN(penetration)) {
			return baseMultiplier;
		}
		final double clampedPenetration = Math.min(1.0, penetration);
		return baseMultiplier
				+ (1.0 - baseMultiplier) * clampedPenetration;
	}

	/**
	 * Resolves one multiplier for the complete hit. With a single weapon this is
	 * exactly that weapon's matchup. Paired weapons use their average weapon
	 * damage as weights, so a mixed set does not arbitrarily inherit only one
	 * hand's matchup.
	 *
	 * @param weapons held weapons taking part in the attack
	 * @param defender attack target
	 * @return multiplier to apply to the already calculated hit damage
	 */
	public static double getCombinedDamageMultiplier(final List<Item> weapons,
			final RPEntity defender) {
		if (weapons == null || weapons.isEmpty()
				|| !(defender instanceof Creature)) {
			return 1.0;
		}

		double weightedMultiplier = 0.0;
		double totalWeight = 0.0;
		for (final Item weapon : weapons) {
			if (weapon == null) {
				continue;
			}

			// Damage ranges are already rolled by RPEntity for the attack. Use
			// the stable average only to determine the contribution of each hand
			// to the final matchup multiplier; never roll the weapon a second time.
			double weight = Math.max(0.0, weapon.getAverageDamage());
			if (weight == 0.0) {
				weight = 1.0;
			}
			weightedMultiplier += weight * getDamageMultiplier(weapon, defender);
			totalWeight += weight;
		}

		return totalWeight == 0.0 ? 1.0 : weightedMultiplier / totalWeight;
	}

	/**
	 * Applies the weapon/armor matchup to damage after the normal combat formula
	 * has resolved ATK, DEF, level, karma and damage-type susceptibility. This is
	 * deliberately before critical-hit and lifesteal handling in the attack flow.
	 *
	 * @param damage damage produced by the normal combat formula
	 * @param weapons held weapons taking part in the attack
	 * @param defender attack target
	 * @return matchup-adjusted damage
	 */
	public static int applyDamageMultiplier(final int damage,
			final List<Item> weapons, final RPEntity defender) {
		return scaleDamage(damage,
				getCombinedDamageMultiplier(weapons, defender));
	}

	/** Package-visible for precise regression tests of percentage semantics. */
	static int scaleDamage(final int damage, final double multiplier) {
		if (damage <= 0) {
			return damage;
		}
		return Math.max(0,
				(int) Math.round(damage * Math.max(0.0, multiplier)));
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
			return 1.20;
		case MEDIUM:
			return 0.80;
		case HEAVY:
			return 0.40;
		case NONE:
		default:
			return 1.0;
		}
	}

	private static double swordMultiplier(final ArmorTier tier) {
		switch (tier) {
		case LIGHT:
			return 1.10;
		case MEDIUM:
			return 1.30;
		case HEAVY:
			return 0.80;
		case NONE:
		default:
			return 1.0;
		}
	}

	private static double armorBreakerMultiplier(final ArmorTier tier) {
		switch (tier) {
		case LIGHT:
			return 0.90;
		case MEDIUM:
			return 1.10;
		case HEAVY:
			return 1.30;
		case NONE:
		default:
			return 1.0;
		}
	}
}
