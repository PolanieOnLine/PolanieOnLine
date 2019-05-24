/*
 * $Id$
 */
/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item.scroll;

import java.util.Map;

import games.stendhal.server.entity.player.Player;

/**
 * Represents the last minute that takes the player to the desert world zone,
 * after which it will teleport player to a random location in 0_zakopane_c.
 */
public class LastMinuteScroll extends TimedTeleportScroll {

	/**
	 * Creates a new timed marked LastMinuteScroll scroll.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public LastMinuteScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public LastMinuteScroll(final LastMinuteScroll item) {
		super(item);
	}
	
	@Override
	protected boolean useTeleportScroll(final Player player) {
		return super.useTeleportScroll(player);
	}
	
	@Override
	protected String getBeforeReturnMessage() {
		return "Zaczynasz odczuwać pragnienie...";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "Znalazłeś się w lesie wyczerpany i odwodniony."
				+ " Nigdy nie czułeś, aż tak wielkiego pragnienia napicia się choć kropli wody.";
	}
}
