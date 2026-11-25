package games.stendhal.common;

import marauroa.common.game.RPObject;

/** Availability is published by the server, independently of the client's clock. */
public final class AdventCalendarAccess {
	public static final String ATTRIBUTE = "advent_calendar_available";

	private AdventCalendarAccess() {
	}

	public static boolean isAvailable(final RPObject player) {
		return player != null && player.has(ATTRIBUTE) && player.getInt(ATTRIBUTE) == 1;
	}
}
