/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.common.Rand;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Resolves player critical-hit chance from equipped weapons and glyphs. */
public final class CriticalHitService {
	public static final String CRITICAL_CHANCE_ATTRIBUTE = "critical_chance";
	public static final double BASE_CRITICAL_CHANCE = 10.0;
	public static final double MAX_CRITICAL_CHANCE = 50.0;

	private CriticalHitService() {
		// utility class
	}

	/**
	 * Returns final critical chance in percentage points. Equipment bonuses are
	 * additive: a value of 7.0 means +7 percentage points to the 10% base chance.
	 * Only held weapons and equipped glyphs currently contribute.
	 */
	public static double getCriticalChance(final Player player) {
		if (player == null) {
			return BASE_CRITICAL_CHANCE;
		}

		double chance = BASE_CRITICAL_CHANCE;
		for (final Item weapon : player.getWeapons()) {
			chance += getBonus(weapon);
		}
		for (final Item glyph : player.getAllEquippedGlyphs()) {
			chance += getBonus(glyph);
		}
		return Math.min(MAX_CRITICAL_CHANCE, Math.max(0.0, chance));
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
}
