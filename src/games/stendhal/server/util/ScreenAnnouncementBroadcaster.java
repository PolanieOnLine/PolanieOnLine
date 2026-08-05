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

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

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
		broadcast(title, text, category, null);
	}

	public static void broadcastToPlayersInZones(final String title, final String text, final String category,
			final Collection<String> zoneNames) {
		if (zoneNames == null || zoneNames.isEmpty()) {
			return;
		}
		broadcast(title, text, category, new LinkedHashSet<String>(zoneNames));
	}

	private static void broadcast(final String title, final String text, final String category,
			final Set<String> allowedZones) {
		final String trimmedText = text == null ? "" : text.trim();
		if (trimmedText.isEmpty()) {
			return;
		}

		for (Player player : SingletonRepository.getRuleProcessor().getOnlinePlayers().getAllPlayers()) {
			if (!isAllowed(player, allowedZones)) {
				continue;
			}
			player.addEvent(new ScreenAnnouncementEvent(title, trimmedText, category));
			player.notifyWorldAboutChanges();
		}
	}

	private static boolean isAllowed(final Player player, final Set<String> allowedZones) {
		if (player == null || player.getZone() == null) {
			return false;
		}
		if (allowedZones == null || allowedZones.isEmpty()) {
			return true;
		}
		return allowedZones.contains(player.getZone().getName());
	}
}
