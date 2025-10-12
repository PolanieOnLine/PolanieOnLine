/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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

import games.stendhal.server.entity.Entity;

/**
 * Slots of players which contain items.
 *
 * @author hendrik
 */
public class PlayerSlot extends EntitySlot {

	/**
	 * Creates a new PlayerSlot.
	 *
	 * @param name
	 *            name of slot
	 */
	public PlayerSlot(final String name) {
		super(name, resolveContentSlotName(name));
	}

	private static String resolveContentSlotName(String name) {
		if ((name != null) && name.endsWith("_set")) {
			return name.substring(0, name.length() - 4);
		}
		return name;
	}

	@Override
	public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
		setErrorMessage("Hej, żadnych kradzieży!");
		return super.hasAsAncestor(entity);
	}
}
