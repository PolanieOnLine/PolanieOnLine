/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.util;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ScreenAnnouncementEvent;

/**
 * Helper for broadcasting game-screen announcement banners.
 */
public final class ScreenAnnouncementBroadcaster {
	public static final String CATEGORY_ADMIN = "admin";
	public static final String CATEGORY_EVENT = "event";
	public static final String CATEGORY_DEFAULT = "default";

	private ScreenAnnouncementBroadcaster() {
		// utility class
	}

	public static void broadcastToAllPlayers(final String title, final String text, final String category) {
		final String trimmedText = text == null ? "" : text.trim();
		if (trimmedText.isEmpty()) {
			return;
		}

		for (Player player : SingletonRepository.getRuleProcessor().getOnlinePlayers().getAllPlayers()) {
			player.addEvent(new ScreenAnnouncementEvent(title, trimmedText, category));
			player.notifyWorldAboutChanges();
		}
	}
}
