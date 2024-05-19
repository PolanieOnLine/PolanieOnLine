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
package games.stendhal.server.entity.item.scroll;

import java.util.Map;

import games.stendhal.server.core.events.DelayedPlayerTextSender;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * Represents the balloon that takes the player to 7 kikareukin clouds,
 * after which it will teleport player to a random location in 6 kikareukin islands.
 */
public class WhiteBalloonScroll extends TimedTeleportScroll {
	private static final long DELAY = 6 * TimeUtil.MILLISECONDS_IN_HOUR;
	private static final int NEWTIME = 540;

	/**
	 * Creates a new timed marked BalloonScroll scroll.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public WhiteBalloonScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public WhiteBalloonScroll(final WhiteBalloonScroll item) {
		super(item);
	}

	@Override
	protected String getBeforeReturnMessage() {
		return "Czujesz jakby chmury już nie mogły wytrzymać pod Twoim ciężarem... ";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "Spadłeś przez dziurę w chmurach na twardą ziemię.";
	}

	// Only let player use balloon from 6 kika clouds
	// Balloons used more frequently than every 6 hours only last 5 minutes
	@Override
	protected boolean useTeleportScroll(final Player player) {
		if (!"6_kikareukin_islands".equals(player.getZone().getName())) {
			if ("6_zakopane_clouds".equals(player.getZone().getName())) {
				player.sendPrivateText("Inny balon nie mógł wynieść cię wyżej.");
			} else {
				player.sendPrivateText("Balon próbował unieść cię wyżej, ale wysokość była zbyt niska, aby podnieść Ciebie. " 
									  + "Spróbuj przejść gdzieś, gdzie jest wyżej.");
			}
			return false;
		}
		long lastuse = -1;
		if (player.hasQuest("balloon_white")) {
			lastuse = Long.parseLong(player.getQuest("balloon_white"));
		}

		player.setQuest("balloon_white", Long.toString(System.currentTimeMillis()));

		final long timeRemaining = (lastuse + DELAY) - System.currentTimeMillis();
		if (timeRemaining > 0) {
			// player used the balloon within the last DELAY hours
			// so this use of balloon is going to be shortened
			// (the clouds can't take so much weight on them)
			// delay message for 1 turn for technical reasons
			new DelayedPlayerTextSender(player, "Chmury osłabły od ostatniego razu i nie utrzymają Ciebie zbyt długo.", 1);

			return super.useTeleportScroll(player, "6_zakopane_clouds", 8, 6, NEWTIME);
		}

		return super.useTeleportScroll(player);
	}
}
