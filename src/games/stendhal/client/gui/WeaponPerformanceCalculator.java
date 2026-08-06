/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;

/** Calculates neutral weapon performance for desktop presentation. */
final class WeaponPerformanceCalculator {
	private static final double SECONDS_PER_TURN = 0.3;
	private static final int DEFAULT_ATTACK_RATE = 5;

	private WeaponPerformanceCalculator() {
		// utility class
	}

	static WeaponPerformance calculate(final RPObject object) {
		if (object == null) {
			return null;
		}

		final int meleeAttack = getPositiveInt(object, ItemTooltip.ATTACK);
		final int rangedAttack = getPositiveInt(object, ItemTooltip.RANGED_ATTACK);
		final boolean ranged = meleeAttack <= 0 && rangedAttack > 0;
		final int attackPoints = ranged ? rangedAttack : meleeAttack;
		if (attackPoints <= 0) {
			return null;
		}

		int attackRate = getPositiveInt(object, ItemTooltip.ATTACK_RATE);
		if (attackRate <= 0) {
			attackRate = DEFAULT_ATTACK_RATE;
		}

		final double attackIntervalSeconds = attackRate * SECONDS_PER_TURN;
		final double attacksPerSecond = 1.0 / attackIntervalSeconds;
		final double baseDps = attackPoints * attacksPerSecond;

		return new WeaponPerformance(attackPoints, attackRate,
				attackIntervalSeconds, attacksPerSecond, baseDps, ranged);
	}

	static String getTooltipValue(final RPObject object, final String key) {
		if (object.hasMap(ItemTooltip.ATTRIBUTE)
				&& object.getMap(ItemTooltip.ATTRIBUTE).containsKey(key)) {
			return object.getMap(ItemTooltip.ATTRIBUTE).get(key);
		}
		return object.has(key) ? object.get(key) : null;
	}

	static int getInt(final RPObject object, final String key) {
		final String value = getTooltipValue(object, key);
		if (value == null) {
			return 0;
		}
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	static double getDouble(final RPObject object, final String key) {
		final String value = getTooltipValue(object, key);
		if (value == null) {
			return 0.0;
		}
		try {
			return Double.parseDouble(value);
		} catch (final NumberFormatException e) {
			return 0.0;
		}
	}

	private static int getPositiveInt(final RPObject object, final String key) {
		return Math.max(0, getInt(object, key));
	}

	static final class WeaponPerformance {
		private final int attackPoints;
		private final int attackRate;
		private final double attackIntervalSeconds;
		private final double attacksPerSecond;
		private final double baseDps;
		private final boolean ranged;

		private WeaponPerformance(final int attackPoints, final int attackRate,
				final double attackIntervalSeconds,
				final double attacksPerSecond, final double baseDps,
				final boolean ranged) {
			this.attackPoints = attackPoints;
			this.attackRate = attackRate;
			this.attackIntervalSeconds = attackIntervalSeconds;
			this.attacksPerSecond = attacksPerSecond;
			this.baseDps = baseDps;
			this.ranged = ranged;
		}

		int getAttackPoints() { return attackPoints; }
		int getAttackRate() { return attackRate; }
		double getAttackIntervalSeconds() { return attackIntervalSeconds; }
		double getAttacksPerSecond() { return attacksPerSecond; }
		double getBaseDps() { return baseDps; }
		boolean isRanged() { return ranged; }
	}
}
