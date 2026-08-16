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
	private static final String TOURIST_QUEST_SLOT = "bilet_turystyczny";

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
		final boolean result = super.useTeleportScroll(player);
		if (result && player.hasQuest(TOURIST_QUEST_SLOT)) {
			final String[] tokens = player.getQuest(TOURIST_QUEST_SLOT).split(";");
			if (tokens.length == 4) {
				player.setQuest(TOURIST_QUEST_SLOT, "bought;" + tokens[1]
						+ ";taken;" + System.currentTimeMillis());
			}
		}
		return result;
	}
	
	@Override
	protected String getBeforeReturnMessage() {
		return "Zaczynasz odczuwać pragnienie...";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "Znak biletu ściąga cię z powrotem z pustyni. Jesteś wyczerpany i spragniony, ale przejście zadziałało dokładnie tak, jak obiecał Juhas.";
	}
}
