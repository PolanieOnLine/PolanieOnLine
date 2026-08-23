/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.DelayedPlayerTextSender;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * Represents the balloon that takes the player to Zakopane clouds, after
 * which it will teleport the player back to the Tatra foothills.
 */
public class WhiteBalloonScroll extends TimedTeleportScroll {
	private static final long DELAY = 6 * TimeUtil.MILLISECONDS_IN_HOUR;
	private static final int NEWTIME = 540;
	private static final String CLOUDS = "6_zakopane_clouds";
	private static final String ALTERNATIVE_CLOUDS = "alt_6_zakopane_clouds";
	private static final String RETURN_ZONE = "0_zakopane_ne";
	private static final int RETURN_X = 70;
	private static final int RETURN_Y = 5;

	public WhiteBalloonScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

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

	/**
	 * The ordinary timed-scroll code compares the current zone with the target
	 * name literally. Alternative Zakopane clouds are the same timed area from
	 * the gameplay point of view, so a player logging in there must be returned
	 * just like a player logging in to the normal clouds.
	 */
	@Override
	public boolean teleportBack(final Player player) {
		if (player != null && player.getZone() != null && ALTERNATIVE_CLOUDS.equals(player.getZone().getName())) {
			final StendhalRPZone returnZone = SingletonRepository.getRPWorld().getZone(RETURN_ZONE);
			if (returnZone == null) {
				return false;
			}
			final boolean result = player.teleport(returnZone, RETURN_X, RETURN_Y, null, player);
			if (result) {
				player.sendPrivateText(getAfterReturnMessage());
			}
			return result;
		}
		return super.teleportBack(player);
	}

	static boolean isZakopaneCloudZone(final String zoneName) {
		return CLOUDS.equals(zoneName) || ALTERNATIVE_CLOUDS.equals(zoneName);
	}

	// Only let player use balloon from 6 kika clouds.
	// Balloons used more frequently than every 6 hours only last 5 minutes.
	@Override
	protected boolean useTeleportScroll(final Player player) {
		if (!"6_kikareukin_islands".equals(player.getZone().getName())) {
			if (CLOUDS.equals(player.getZone().getName())) {
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
			new DelayedPlayerTextSender(player, "Chmury osłabły od ostatniego razu i nie utrzymają Ciebie zbyt długo.", 1);
			return super.useTeleportScroll(player, CLOUDS, 8, 6, NEWTIME);
		}

		return super.useTeleportScroll(player);
	}
}
