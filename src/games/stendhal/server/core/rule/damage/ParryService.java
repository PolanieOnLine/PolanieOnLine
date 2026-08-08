/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import games.stendhal.common.Rand;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/**
 * Resolves the chance that a player parries an incoming melee attack with the
 * weapons currently held in their hands.
 */
public final class ParryService {
	public static final String PARRY_CHANCE_ATTRIBUTE = "parry_chance";
	/** Hard cap for the player's final parry chance. */
	public static final double MAX_PARRY_CHANCE = 0.15;

	private ParryService() {
		// utility class
	}

	/**
	 * Returns the combined parry chance of currently held melee weapons.
	 * Individual weapon chances are independent, but the final result is capped
	 * at 15%. Values outside the supported range are clamped defensively.
	 *
	 * @param player defending player
	 * @return combined chance in the range [0, 0.15]
	 */
	public static double getParryChance(final Player player) {
		if (player == null) {
			return 0.0;
		}

		double failureChance = 1.0;
		boolean hasParrySource = false;
		for (final Item weapon : player.getWeapons()) {
			if (weapon == null || weapon.isNonMeleeWeapon()
					|| !weapon.has(PARRY_CHANCE_ATTRIBUTE)) {
				continue;
			}

			final double chance = clampChance(
					weapon.getDouble(PARRY_CHANCE_ATTRIBUTE));
			if (chance <= 0.0) {
				continue;
			}
			hasParrySource = true;
			failureChance *= 1.0 - chance;
		}

		return hasParrySource
				? clampChance(1.0 - failureChance)
				: 0.0;
	}

	/**
	 * Rolls one parry attempt for an incoming melee attack.
	 *
	 * @param player defending player
	 * @return {@code true} when the incoming attack is completely parried
	 */
	public static boolean rollParry(final Player player) {
		final double chance = getParryChance(player);
		if (chance <= 0.0) {
			return false;
		}
		return isParrySuccessful(chance, Rand.rand());
	}

	/** Package-visible deterministic seam for unit tests. */
	static boolean isParrySuccessful(final double chance, final double roll) {
		if (Double.isNaN(roll) || roll < 0.0 || roll >= 1.0) {
			throw new IllegalArgumentException("Parry roll must be in [0, 1)");
		}
		return roll < clampChance(chance);
	}

	private static double clampChance(final double chance) {
		if (Double.isNaN(chance) || chance <= 0.0) {
			return 0.0;
		}
		return Math.min(MAX_PARRY_CHANCE, chance);
	}
}
