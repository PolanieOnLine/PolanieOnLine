/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import marauroa.common.game.RPAction;

/**
 * Extracts item creation options from administrator command arguments.
 */
final class ItemCreationCommandOptions {
	static final String RARITY = "rarity";
	static final String RANDOMIZE_MODIFIERS = "randomize-modifiers";
	static final String AFFIXES = "affixes";
	static final String SEED = "seed";
	static final String STAT_MULTIPLIER = "stat-multiplier";
	static final String ATTACK_MULTIPLIER = "attack-multiplier";
	static final String DEFENSE_MULTIPLIER = "defense-multiplier";
	static final String SPEED_MULTIPLIER = "speed-multiplier";
	static final String RANGE_MULTIPLIER = "range-multiplier";
	static final String VALUE_MULTIPLIER = "value-multiplier";

	private static final Set<String> SUPPORTED_OPTIONS = new HashSet<String>(Arrays.asList(
			RARITY,
			RANDOMIZE_MODIFIERS,
			AFFIXES,
			SEED,
			STAT_MULTIPLIER,
			ATTACK_MULTIPLIER,
			DEFENSE_MULTIPLIER,
			SPEED_MULTIPLIER,
			RANGE_MULTIPLIER,
			VALUE_MULTIPLIER));

	private ItemCreationCommandOptions() {
		// utility class
	}

	/**
	 * Copies a recognized {@code key=value} option to the outgoing action.
	 * Unknown tokens are deliberately left to the item name parser.
	 *
	 * @param token command token
	 * @param action outgoing action
	 * @return {@code true} if the token was a recognized option
	 */
	static boolean copyToAction(final String token, final RPAction action) {
		if (token == null) {
			return false;
		}

		final int separator = token.indexOf('=');
		if (separator <= 0) {
			return false;
		}

		final String key = token.substring(0, separator).toLowerCase();
		if (!SUPPORTED_OPTIONS.contains(key)) {
			return false;
		}

		action.put(key, token.substring(separator + 1));
		return true;
	}
}
