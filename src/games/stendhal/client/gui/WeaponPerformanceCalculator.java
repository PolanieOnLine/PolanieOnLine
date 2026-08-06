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

import marauroa.common.game.RPObject;

/**
 * Calculates neutral weapon performance for desktop presentation.
 *
 * The result intentionally uses only the attributes of the item instance. It
 * does not include player statistics, critical hits, target defence or status
 * effects, so the displayed value is a base DPS rather than combat DPS.
 */
final class WeaponPerformanceCalculator {
	/** Server turns currently last 300 ms. */
	private static final double SECONDS_PER_TURN = 0.3;
	/** Mirrors the server-side default used when an item has no rate. */
	private static final int DEFAULT_ATTACK_RATE = 5;

	private WeaponPerformanceCalculator() {
		// utility class
	}

	/**
	 * Calculates performance for an item information object.
	 *
	 * @param object item RPObject
	 * @return performance, or {@code null} when the object is not a weapon
	 */
	static WeaponPerformance calculate(final RPObject object) {
		if (object == null) {
			return null;
		}

		final int meleeAttack = getPositiveInt(object, "atk");
		final int rangedAttack = getPositiveInt(object, "ratk");
		final boolean ranged = meleeAttack <= 0 && rangedAttack > 0;
		final int attackPoints = ranged ? rangedAttack : meleeAttack;
		if (attackPoints <= 0) {
			return null;
		}

		int attackRate = getPositiveInt(object, "rate");
		if (attackRate <= 0) {
			attackRate = DEFAULT_ATTACK_RATE;
		}

		final double attackIntervalSeconds = attackRate * SECONDS_PER_TURN;
		final double attacksPerSecond = 1.0 / attackIntervalSeconds;
		final double baseDps = attackPoints * attacksPerSecond;

		return new WeaponPerformance(attackPoints, attackRate,
				attackIntervalSeconds, attacksPerSecond, baseDps, ranged);
	}

	private static int getPositiveInt(final RPObject object,
			final String attribute) {
		if (!object.has(attribute)) {
			return 0;
		}
		return Math.max(0, object.getInt(attribute));
	}

	/** Immutable calculated weapon values used by the tooltip. */
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

		int getAttackPoints() {
			return attackPoints;
		}

		int getAttackRate() {
			return attackRate;
		}

		double getAttackIntervalSeconds() {
			return attackIntervalSeconds;
		}

		double getAttacksPerSecond() {
			return attacksPerSecond;
		}

		double getBaseDps() {
			return baseDps;
		}

		boolean isRanged() {
			return ranged;
		}
	}
}
