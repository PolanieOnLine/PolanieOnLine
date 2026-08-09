/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.common.Rand;
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

	private CriticalHitService() {
		// utility class
	}

	/**
	 * Returns final critical chance in percentage points. Equipment bonuses are
	 * additive: a value of 7.0 means +7 percentage points to the 10% base chance.
	 * Held weapons, jewellery and equipped glyphs contribute.
	 */
	public static double getCriticalChance(final Player player) {
		if (player == null) {
			return BASE_CRITICAL_CHANCE;
		}

		double chance = BASE_CRITICAL_CHANCE;
		for (final Item weapon : player.getWeapons()) {
			chance += getBonus(weapon);
		}
		chance += getBonus(player.getRing());
		chance += getBonus(player.getRingB());
		chance += getBonus(player.getNecklace());
		for (final Item glyph : player.getAllEquippedGlyphs()) {
			chance += getBonus(glyph);
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

	private static double getBonus(final Item item) {
		if (item == null || !item.has(CRITICAL_CHANCE_ATTRIBUTE)) {
			return 0.0;
		}
		final double value = item.getDouble(CRITICAL_CHANCE_ATTRIBUTE);
		return Double.isNaN(value) ? 0.0 : Math.max(0.0, value);
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
