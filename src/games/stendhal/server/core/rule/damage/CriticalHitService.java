/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.common.Rand;
import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Resolves player critical-hit chance and critical damage bonuses. */
public final class CriticalHitService {
	public static final String CRITICAL_CHANCE_ATTRIBUTE = "critical_chance";
	public static final String CRITICAL_DAMAGE_BONUS_ATTRIBUTE =
			"critical_damage_bonus";
	public static final double BASE_CRITICAL_CHANCE = 10.0;
	public static final double MAX_CRITICAL_CHANCE = 50.0;
	public static final double BASE_CRITICAL_DAMAGE_MULTIPLIER = 2.0;
	public static final double MAX_CRITICAL_DAMAGE_BONUS = 0.50;
	public static final double FALCON_EYE_MIN_DISTANCE_SQUARED = 16.0;

	private CriticalHitService() {
		// utility class
	}

	/**
	 * Returns final critical chance in percentage points. Equipment bonuses are
	 * additive: a value of 7.0 means +7 percentage points to the 10% base chance.
	 * Held weapons, jewellery and equipped glyphs contribute. Legendary Falcon
	 * Eye adds 10 points only when the current attack target is at least four
	 * tiles away and the attacking weapon is ranged. Hero Eye jewellery always
	 * adds 8 points. All sources obey the shared final 50% cap.
	 */
	public static double getCriticalChance(final Player player) {
		return getCriticalChance(player, isFalconEyeDistanceActive(player));
	}

	/** Package-visible deterministic seam for distance-gated legendary tests. */
	static double getCriticalChance(final Player player,
			final boolean falconEyeDistanceActive) {
		if (player == null) {
			return BASE_CRITICAL_CHANCE;
		}

		double chance = BASE_CRITICAL_CHANCE;
		for (final Item weapon : player.getWeapons()) {
			chance += getBonus(weapon, falconEyeDistanceActive);
		}
		chance += getBonus(player.getRing(), false);
		chance += getBonus(player.getRingB(), false);
		chance += getBonus(player.getNecklace(), false);
		for (final Item glyph : player.getAllEquippedGlyphs()) {
			chance += getBonus(glyph, false);
		}
		return Math.min(MAX_CRITICAL_CHANCE, Math.max(0.0, chance));
	}

	/**
	 * Returns the final critical damage multiplier. A weapon affix value of 0.20
	 * raises a normal 2.00x critical to 2.20x. Dual-wield weapon bonuses are
	 * weighted by average damage; jewellery contributes its own flat fractions.
	 */
	public static double getCriticalDamageMultiplier(final Player player) {
		if (player == null) {
			return BASE_CRITICAL_DAMAGE_MULTIPLIER;
		}
		double bonus = WeaponAffixCombatService.getWeightedFraction(
				player.getWeapons(), CRITICAL_DAMAGE_BONUS_ATTRIBUTE);
		bonus += getFraction(player.getRing(), CRITICAL_DAMAGE_BONUS_ATTRIBUTE);
		bonus += getFraction(player.getRingB(), CRITICAL_DAMAGE_BONUS_ATTRIBUTE);
		bonus += getFraction(player.getNecklace(), CRITICAL_DAMAGE_BONUS_ATTRIBUTE);
		bonus = Math.min(MAX_CRITICAL_DAMAGE_BONUS, Math.max(0.0, bonus));
		return BASE_CRITICAL_DAMAGE_MULTIPLIER + bonus;
	}

	/**
	 * Applies critical damage while preserving the existing flat
	 * critical_additional_bonus supplied by equipped glyphs.
	 */
	public static int applyCriticalDamage(final Player player, final int damage) {
		if (damage <= 0) {
			return damage;
		}
		int result = (int) Math.round(damage * getCriticalDamageMultiplier(player));
		if (player != null) {
			double flatBonus = 0.0;
			for (final Item glyph : player.getAllEquippedGlyphs()) {
				if (glyph.has("critical_additional_bonus")) {
					flatBonus += glyph.getDouble("critical_additional_bonus");
				}
			}
			result += (int) flatBonus;
		}
		return Math.max(0, result);
	}

	/** Rolls one critical-hit attempt using the final percentage chance. */
	public static boolean rollCritical(final Player player) {
		return isCriticalSuccessful(getCriticalChance(player), Rand.roll1D100());
	}

	/** Package-visible deterministic seam for unit tests. */
	static boolean isCriticalSuccessful(final double chance, final int roll) {
		if (roll < 1 || roll > 100) {
			throw new IllegalArgumentException("Critical roll must be in [1, 100]");
		}
		final double clamped = Math.min(MAX_CRITICAL_CHANCE,
				Math.max(0.0, chance));
		return roll <= clamped;
	}

	static boolean isFalconEyeDistanceActive(final Player player) {
		if (player == null) {
			return false;
		}
		final Item attackWeapon = player.getWeapon();
		final RPEntity target = player.getAttackTarget();
		return attackWeapon != null && attackWeapon.isNonMeleeWeapon()
				&& target != null
				&& player.squaredDistance(target) >= FALCON_EYE_MIN_DISTANCE_SQUARED;
	}

	private static double getBonus(final Item item,
			final boolean falconEyeDistanceActive) {
		if (item == null) {
			return 0.0;
		}
		double bonus = 0.0;
		if (item.has(CRITICAL_CHANCE_ATTRIBUTE)) {
			final double value = item.getDouble(CRITICAL_CHANCE_ATTRIBUTE);
			if (!Double.isNaN(value)) {
				bonus += Math.max(0.0, value);
			}
		}
		if (falconEyeDistanceActive
				&& item.has(WeaponAffixCombatService.LEGENDARY_FALCON_EYE_ATTRIBUTE)) {
			bonus += WeaponAffixCombatService.LEGENDARY_FALCON_EYE_CRITICAL_CHANCE_BONUS;
		}
		if (item.has(LegendaryEquipmentAffixService.HERO_EYE_ATTRIBUTE)) {
			bonus += LegendaryEquipmentAffixService.HERO_EYE_CRITICAL_CHANCE_BONUS;
		}
		return bonus;
	}

	private static double getFraction(final Item item, final String attribute) {
		if (item == null || attribute == null || !item.has(attribute)) {
			return 0.0;
		}
		final double value = item.getDouble(attribute);
		if (Double.isNaN(value)) {
			return 0.0;
		}
		return Math.min(1.0, Math.max(0.0, value));
	}
}
