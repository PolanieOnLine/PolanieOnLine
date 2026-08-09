/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Central precision policy for materialized item rolls and modifiers. */
public final class ItemRollPrecision {
	public static final int SCALE = 2;
	private static final double MIN_POSITIVE = 0.01;

	private ItemRollPrecision() {
		// utility class
	}

	/**
	 * Rounds a finite materialized roll to two decimal places using HALF_UP.
	 * Non-finite values are returned unchanged so existing saturation/validation
	 * code can handle them at its own boundary.
	 */
	public static double round(final double value) {
		if (Double.isNaN(value) || Double.isInfinite(value)) {
			return value;
		}
		final double rounded = BigDecimal.valueOf(value)
				.setScale(SCALE, RoundingMode.HALF_UP).doubleValue();
		return rounded == 0.0 ? 0.0 : rounded;
	}

	/**
	 * Rounds a positive multiplier while keeping the smallest representable
	 * positive value at the configured precision.
	 */
	public static double roundPositive(final double value) {
		if (Double.isNaN(value) || Double.isInfinite(value) || value <= 0.0) {
			return value;
		}
		return Math.max(MIN_POSITIVE, round(value));
	}
}
