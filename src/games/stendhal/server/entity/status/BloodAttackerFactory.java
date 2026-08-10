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

/**
 * Compatibility bridge for the legacy creature profile wiring.
 *
 * <p>Creature data still reaches this factory through the historical
 * {@code perilous} profile. The actual effect is always created by the
 * Bleeding 2.0 factory, so proc chance and wound damage no longer depend on
 * the creature's base attack value.</p>
 *
 * @deprecated migrate the remaining creature profile wiring to
 *     {@link BleedingAttackerFactory} and {@code bleeding_attack}
 */
@Deprecated
public final class BloodAttackerFactory {
	private BloodAttackerFactory() {
		// compatibility utility
	}

	/**
	 * Creates the Bleeding 2.0 attacker used by legacy creature profiles.
	 *
	 * @param profile bleeding chance and optional damage factor
	 * @param ignoredAttack retained only for source compatibility with the old
	 *     factory signature
	 * @return configured Bleeding 2.0 attacker, or {@code null}
	 */
	public static BleedingAttacker get(final String profile,
			@SuppressWarnings("unused") final int ignoredAttack) {
		return BleedingAttackerFactory.get(profile);
	}
}
