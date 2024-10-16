/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common;

/**
 * General constants.
 */
public final class Constants {
	public static final String[] EQUIPMENT_SLOTS = {
			"head", "rhand", "lhand", "armor", "finger", "cloak", "legs", "feet",
			"neck", "glove", "fingerb", "pas",
			"offensive_rune", "defensive_rune", "resistance_rune", "utility_rune", "healing_rune",
			"control_rune", "special_rune"
	};

	public static final String[] RUNE_SLOTS = {
			"offensive_rune", "defensive_rune", "resistance_rune", "utility_rune", "healing_rune",
			"control_rune", "special_rune"
	};

	/**
	 * All the slots considered to be "with" the entity. Listed in priority
	 * order (i.e. bag first).
	 */
	// TODO: let the slots decide that themselves
	public static final String[] CARRYING_SLOTS = {
			"pouch", "bag", "magicbag", "portfolio", "keyring", "back", "belt", "neck",
			"head", "cloak", "lhand", "armor", "rhand", "finger", "fingerb",
			"glove", "pas", "legs", "feet",
			"offensive_rune", "defensive_rune", "resistance_rune", "utility_rune", "healing_rune",
			"control_rune", "special_rune"
	};

	/** Distance at which entity sounds can be heard. */
	public static final int DEFAULT_SOUND_RADIUS = 23;
}
