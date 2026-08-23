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
import games.stendhal.server.core.rule.damage.EquipmentStatusResistanceService;
import games.stendhal.server.entity.RPEntity;

/**
 * Applies physical bleeding wounds after successful damaging hits.
 */
public class BleedingAttacker extends StatusAttacker {
	public static final double DEFAULT_DAMAGE_FACTOR = 0.20;
	public static final double CREATURE_DEFAULT_DAMAGE_FACTOR = 1.00;
	public static final double CREATURE_MIN_TARGET_HP_FACTOR = 0.02;
	public static final double CREATURE_MAX_TARGET_HP_FACTOR = 0.05;
	public static final int DEFAULT_TICKS = 4;
	public static final int DEFAULT_TICK_INTERVAL_TURNS = 3;

	private static final int PROC_ROLL_SCALE = 10000;

	private final double damageFactor;
	private final double minimumTargetHpFactor;
	private final double maximumTargetHpFactor;

	/**
	 * Creates ordinary hit-scaled bleeding without target-HP guard rails.
	 *
	 * @param probability chance in percent (0..100)
	 * @param damageFactor total wound damage as a fraction of the actual hit
	 */
	public BleedingAttacker(final double probability, final double damageFactor) {
		this(probability, damageFactor, 0.0, 0.0);
	}

	/**
	 * Creates hit-scaled bleeding with optional target-HP minimum and cap.
	 *
	 * <p>This variant is used by creature profiles so a rare bleeding proc is
	 * noticeable even after a small hit, while very large monster hits cannot
	 * turn the damage-over-time component into an excessive burst.</p>
	 *
	 * @param probability chance in percent (0..100)
	 * @param damageFactor total wound damage as a fraction of the actual hit
	 * @param minimumTargetHpFactor minimum wound as a fraction of target max HP,
	 * 		or {@code 0} to disable the minimum
	 * @param maximumTargetHpFactor maximum wound as a fraction of target max HP,
	 * 		or {@code 0} to disable the cap
	 */
	public BleedingAttacker(final double probability, final double damageFactor,
			final double minimumTargetHpFactor,
			final double maximumTargetHpFactor) {
		super(validateProbability(probability));
		if (Double.isNaN(damageFactor) || damageFactor <= 0.0
				|| damageFactor > 1.0) {
			throw new IllegalArgumentException(
					"Bleeding damage factor must be in (0, 1]");
		}
		validateTargetHpFactor("minimum", minimumTargetHpFactor);
		validateTargetHpFactor("maximum", maximumTargetHpFactor);
		if (maximumTargetHpFactor > 0.0
				&& minimumTargetHpFactor > maximumTargetHpFactor) {
			throw new IllegalArgumentException(
					"Bleeding target HP minimum must not exceed maximum");
		}
		this.damageFactor = damageFactor;
		this.minimumTargetHpFactor = minimumTargetHpFactor;
		this.maximumTargetHpFactor = maximumTargetHpFactor;
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
				calculateTotalDamage(damage, damageFactor, target.getBaseHP(),
						minimumTargetHpFactor, maximumTargetHpFactor),
				DEFAULT_TICKS, DEFAULT_TICK_INTERVAL_TURNS, attacker);
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

	public double getMinimumTargetHpFactor() {
		return minimumTargetHpFactor;
	}

	public double getMaximumTargetHpFactor() {
		return maximumTargetHpFactor;
	}

	static int calculateTotalDamage(final int hitDamage,
			final double damageFactor) {
		return calculateTotalDamage(hitDamage, damageFactor, 0, 0.0, 0.0);
	}

	static int calculateTotalDamage(final int hitDamage,
			final double damageFactor, final int targetMaxHp,
			final double minimumTargetHpFactor,
			final double maximumTargetHpFactor) {
		if (hitDamage <= 0) {
			return 0;
		}
		int totalDamage = Math.max(1,
				(int) Math.round(hitDamage * damageFactor));
		if (targetMaxHp <= 0) {
			return totalDamage;
		}
		if (minimumTargetHpFactor > 0.0) {
			final int minimumDamage = Math.max(1,
					(int) Math.round(targetMaxHp * minimumTargetHpFactor));
			totalDamage = Math.max(totalDamage, minimumDamage);
		}
		if (maximumTargetHpFactor > 0.0) {
			final int maximumDamage = Math.max(1,
					(int) Math.round(targetMaxHp * maximumTargetHpFactor));
			totalDamage = Math.min(totalDamage, maximumDamage);
		}
		return totalDamage;
	}

	static double getBleedingResistance(final RPEntity target) {
		return EquipmentStatusResistanceService.getResistance(
				target, StatusType.BLEEDING);
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

	private static void validateTargetHpFactor(final String name,
			final double factor) {
		if (Double.isNaN(factor) || factor < 0.0 || factor > 1.0) {
			throw new IllegalArgumentException(
					"Bleeding target HP " + name + " must be in [0, 1]");
		}
	}
}
