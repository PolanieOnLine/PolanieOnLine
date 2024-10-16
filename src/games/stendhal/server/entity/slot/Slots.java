/***************************************************************************
 *                (C) Copyright 2014-2024 - Faiumoni e. V.                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.slot;

import com.google.common.collect.ImmutableList;

import games.stendhal.common.Constants;

/**
 * slot types
 *
 * @author hendrik
 */
public enum Slots {
	/** Slots where equipment can be carried for use (weapons, armor, and rings). */
	EQUIPMENT(ImmutableList.copyOf(Constants.EQUIPMENT_SLOTS)),

	/** Slots designated for carrying magical runes or glyphs. */
	GLYPHS(ImmutableList.copyOf(Constants.RUNE_SLOTS)),

	/**
	 * slots which may be carried by an entity (e. g. a bag)
	 */
	CARRYING(ImmutableList.copyOf(Constants.CARRYING_SLOTS));

	private ImmutableList<String> names;

	/**
	 * constructor
	 *
	 * @param names list of slot names
	 */
	Slots(ImmutableList<String> names) {
		this.names = names;
	}

	/**
	 * gets the list of slot names
	 *
	 * @return slot names
	 */
	public ImmutableList<String> getNames() {
		return names;
	}
}
