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

/** Creates configured bleeding attackers from creature AI profiles. */
public final class BleedingAttackerFactory {
	private BleedingAttackerFactory() {
		// utility class
	}

	/**
	 * Parses {@code chancePercent[;damageFactor]}.
	 *
	 * <p>For example {@code 15} means a 15% chance with the default creature
	 * wound damage factor (100% of the actual hit), while {@code 20;0.30}
	 * means a 20% chance with a wound initially worth 30% of the actual hit.
	 * Creature wounds are additionally clamped to 2-5% of the target's maximum
	 * HP so rare procs stay noticeable without becoming excessive after very
	 * large hits.</p>
	 */
	public static BleedingAttacker get(final String profile) {
		if (profile == null || profile.trim().isEmpty()) {
			return null;
		}
		final String[] params = profile.split(";");
		if (params.length > 2) {
			throw new IllegalArgumentException(
					"Bleeding profile must be chancePercent[;damageFactor]");
		}
		final double probability = Double.parseDouble(params[0].trim());
		final double damageFactor = params.length == 2
				? Double.parseDouble(params[1].trim())
				: BleedingAttacker.CREATURE_DEFAULT_DAMAGE_FACTOR;
		return new BleedingAttacker(probability, damageFactor,
				BleedingAttacker.CREATURE_MIN_TARGET_HP_FACTOR,
				BleedingAttacker.CREATURE_MAX_TARGET_HP_FACTOR);
	}
}
