/***************************************************************************
 *                 (C) Copyright 2019-2026 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.RPEntity;

/**
 * Applies physical bleeding wounds after successful damaging hits.
 */
public class BleedingAttacker extends StatusAttacker {
	public static final double DEFAULT_DAMAGE_FACTOR = 0.20;
	public static final int DEFAULT_TICKS = 4;
	public static final int DEFAULT_TICK_INTERVAL_TURNS = 3;

	private static final int PROC_ROLL_SCALE = 10000;
	private static final String RESISTANCE_ATTRIBUTE = "resist_bleeding";

	private final double damageFactor;

	/**
	 * @param probability chance in percent (0..100)
	 * @param damageFactor total wound damage as a fraction of the actual hit
	 */
	public BleedingAttacker(final double probability, final double damageFactor) {
		super(validateProbability(probability));
		if (Double.isNaN(damageFactor) || damageFactor <= 0.0
				|| damageFactor > 1.0) {
			throw new IllegalArgumentException(
					"Bleeding damage factor must be in (0, 1]");
		}
		this.damageFactor = damageFactor;
	}

	@Override
	public void onHit(final RPEntity target, final RPEntity attacker,
			final int damage) {
		if (target == null || attacker == null || damage <= 0
				|| target.getHP() <= 0) {
			return;
		}

		final double resistance = getBleedingResistance(target);
		final double actualProbability = getProbability() * (1.0 - resistance);
		if (actualProbability <= 0.0
				|| !rollChance(actualProbability,
						Rand.randUniform(1, PROC_ROLL_SCALE))) {
			return;
		}

		final BleedingStatus wound = new BleedingStatus(
				calculateTotalDamage(damage, damageFactor), DEFAULT_TICKS,
				DEFAULT_TICK_INTERVAL_TURNS, attacker);
		target.getStatusList().inflictStatus(wound, attacker);
		if (wound.wasApplied()) {
			new GameEvent(attacker.getName(), "bleeding", target.getName()).raise();
			target.sendPrivateText(target.getGenderVerb("Zostałeś") + " "
					+ target.getGenderVerb("zraniony") + " przez "
					+ attacker.getName() + ".");
		}
	}

	@Override
	public String getStatusName() {
		return "bleeding";
	}

	public double getDamageFactor() {
		return damageFactor;
	}

	static int calculateTotalDamage(final int hitDamage,
			final double damageFactor) {
		if (hitDamage <= 0) {
			return 0;
		}
		return Math.max(1, (int) Math.round(hitDamage * damageFactor));
	}

	static double getBleedingResistance(final RPEntity target) {
		if (target == null || !target.has(RESISTANCE_ATTRIBUTE)) {
			return 0.0;
		}
		return clampFraction(target.getDouble(RESISTANCE_ATTRIBUTE));
	}

	/** Package-visible deterministic seam for probability tests. */
	static boolean rollChance(final double probabilityPercent, final int roll) {
		if (roll < 1 || roll > PROC_ROLL_SCALE) {
			throw new IllegalArgumentException("Bleeding roll must be in [1, 10000]");
		}
		final double chance = Math.max(0.0,
				Math.min(100.0, probabilityPercent));
		return roll <= Math.round(chance * 100.0);
	}

	private static double validateProbability(final double probability) {
		if (Double.isNaN(probability) || probability < 0.0 || probability > 100.0) {
			throw new IllegalArgumentException(
					"Bleeding probability must be in [0, 100]");
		}
		return probability;
	}

	private static double clampFraction(final double value) {
		if (Double.isNaN(value)) {
			return 0.0;
		}
		return Math.max(0.0, Math.min(1.0, value));
	}
}
