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

	public static final String[] RESERVE_EQUIPMENT_SLOTS = {
			"neck_set", "rhand_set", "finger_set", "fingerb_set",
			"head_set", "armor_set", "pas_set", "legs_set", "feet_set",
			"cloak_set", "lhand_set", "glove_set", "pouch_set"
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
			"pouch", "pouch_set", "bag", "magicbag", "portfolio", "keyring", "back", "belt", "neck", "neck_set",
			"head", "head_set", "cloak", "cloak_set", "lhand", "lhand_set", "armor", "armor_set", "rhand", "rhand_set", "finger", "finger_set", "fingerb", "fingerb_set",
			"glove", "glove_set", "pas", "pas_set", "legs", "legs_set", "feet", "feet_set",
			"offensive_rune", "defensive_rune", "resistance_rune", "utility_rune", "healing_rune",
			"control_rune", "special_rune"
	};

	/** Distance at which entity sounds can be heard. */
	public static final int DEFAULT_SOUND_RADIUS = 23;
}
