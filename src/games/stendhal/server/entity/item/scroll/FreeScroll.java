/*
 * $Id$
 */
/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;

import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * Represents a free teleport scroll.
 */
public class FreeScroll extends TeleportScroll {

	private static final Logger logger = Logger.getLogger(FreeScroll.class);

	/**
	 * Creates a new free teleport scroll.
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public FreeScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 * 
	 * @param item
	 *            item to copy
	 */
	public FreeScroll(final FreeScroll item) {
		super(item);
	}

	/**
	 * Is invoked when a teleporting scroll is used. Tries to put the player on
	 * the scroll's destination, or near it.
	 * 
	 * @param player
	 *            The player who used the scroll and who will be teleported
	 * @return true if teleport was successful
	 */
	@Override
	protected boolean useTeleportScroll(final Player player) {
		// init as home_scroll
		StendhalRPZone zone = SingletonRepository.getRPWorld().getZone("0_zakopane_s");
		int x = 30;
		int y = 30;

		/*
		 * Marked scrolls have a destination which is stored in the infostring,
		 * existing of a zone name and x and y coordinates
		 */
		final String infostring = getInfoString();

		if (infostring != null) {
			final StringTokenizer st = new StringTokenizer(infostring);
			if (st.countTokens() == 3) {
				// check destination
				final String zoneName = st.nextToken();
				final StendhalRPZone temp = SingletonRepository.getRPWorld().getZone(zoneName);
				if (temp == null) {
					// invalid zone (the scroll may have been marked in an
					// old version and the zone was removed)
					player.sendPrivateText("Z dziwnych powodów zwój nie przeniósł mnie tam gdzie chciałem.");
					logger.warn("free scroll to unknown zone " + infostring
							+ " teleported " + player.getName()
							+ " to Zakopane instead");
				} else {
					zone = temp;
					x = Integer.parseInt(st.nextToken());
					y = Integer.parseInt(st.nextToken());
					if (!zone.isTeleportOutAllowed(x, y)) {
						player.sendPrivateText("Silna antymagiczna aura blokuje działanie zwoju!");
						return false;
					}
				}
			}
		}

		// we use the player as teleporter (last parameter) to give feedback
		// if something goes wrong.
		return player.teleport(zone, x, y, null, player);
	}
}
