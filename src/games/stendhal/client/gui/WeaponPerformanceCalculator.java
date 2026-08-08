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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import games.stendhal.common.constants.GameTiming;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;

/** Calculates neutral weapon performance for desktop presentation. */
final class WeaponPerformanceCalculator {
	private static final int DEFAULT_ATTACK_RATE = 5;
	private static final Set<String> LEGACY_WEAPON_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"club", "sword", "dagger", "axe", "ranged", "missile",
					"wand", "whip")));

	private WeaponPerformanceCalculator() {
		// utility class
	}

	/**
	 * Uses the category published by the server. The class-name fallback keeps
	 * compatibility with old servers and direct test objects.
	 */
	static boolean isWeapon(final RPObject object) {
		if (object == null) {
			return false;
		}
		final String category = getTooltipValue(object, ItemTooltip.CATEGORY);
		if (category != null) {
			return ItemTooltip.CATEGORY_WEAPON.equals(category);
		}
		return object.has("class")
				&& LEGACY_WEAPON_CLASSES.contains(object.get("class"));
	}

	static WeaponPerformance calculate(final RPObject object) {
		if (!isWeapon(object)) {
			return null;
		}

		final int meleeAttack = getPositiveInt(object, ItemTooltip.ATTACK);
		final int rangedAttack = getPositiveInt(object, ItemTooltip.RANGED_ATTACK);
		final boolean ranged = meleeAttack <= 0 && rangedAttack > 0;
		final int legacyAttack = ranged ? rangedAttack : meleeAttack;
		int damageMin = getPositiveInt(object, ItemTooltip.DAMAGE_MIN);
		int damageMax = getPositiveInt(object, ItemTooltip.DAMAGE_MAX);
		if (damageMin <= 0) {
			damageMin = legacyAttack;
		}
		if (damageMax <= 0) {
			damageMax = damageMin;
		}
		damageMax = Math.max(damageMin, damageMax);
		if (damageMin <= 0) {
			return null;
		}

		int attackRate = getPositiveInt(object, ItemTooltip.ATTACK_RATE);
		if (attackRate <= 0) {
			attackRate = DEFAULT_ATTACK_RATE;
		}

		double attackIntervalSeconds = getDouble(object,
				ItemTooltip.ATTACK_INTERVAL_SECONDS);
		double attacksPerSecond = getDouble(object,
				ItemTooltip.ATTACKS_PER_SECOND);
		if (attackIntervalSeconds <= 0.0 && attacksPerSecond > 0.0) {
			attackIntervalSeconds = 1.0 / attacksPerSecond;
		}
		if (attacksPerSecond <= 0.0 && attackIntervalSeconds > 0.0) {
			attacksPerSecond = 1.0 / attackIntervalSeconds;
		}
		if (attackIntervalSeconds <= 0.0 || attacksPerSecond <= 0.0) {
			attackIntervalSeconds = attackRate * GameTiming.SECONDS_PER_TURN;
			attacksPerSecond = 1.0 / attackIntervalSeconds;
		}

		final double averageDamage = (damageMin + damageMax) / 2.0;
		final double baseDps = averageDamage * attacksPerSecond;

		return new WeaponPerformance(damageMin, damageMax, attackRate,
				attackIntervalSeconds, attacksPerSecond, baseDps, ranged);
	}

	static String getTooltipValue(final RPObject object, final String key) {
		if (object == null) {
			return null;
		}
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
		private final int damageMin;
		private final int damageMax;
		private final int attackRate;
		private final double attackIntervalSeconds;
		private final double attacksPerSecond;
		private final double baseDps;
		private final boolean ranged;

		private WeaponPerformance(final int damageMin, final int damageMax,
				final int attackRate,
				final double attackIntervalSeconds,
				final double attacksPerSecond, final double baseDps,
				final boolean ranged) {
			this.damageMin = damageMin;
			this.damageMax = damageMax;
			this.attackRate = attackRate;
			this.attackIntervalSeconds = attackIntervalSeconds;
			this.attacksPerSecond = attacksPerSecond;
			this.baseDps = baseDps;
			this.ranged = ranged;
		}

		int getAttackPoints() {
			return (int) Math.round((damageMin + damageMax) / 2.0);
		}

		int getDamageMin() {
			return damageMin;
		}

		int getDamageMax() {
			return damageMax;
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
