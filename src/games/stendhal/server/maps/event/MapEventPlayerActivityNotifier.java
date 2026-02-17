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
package games.stendhal.server.maps.event;

import java.util.LinkedHashMap;
import java.util.Map;

import games.stendhal.common.NotificationType;
import games.stendhal.server.entity.player.Player;

/**
 * Sends lightweight live activity updates for map-event participants.
 */
public final class MapEventPlayerActivityNotifier {
	private static final int[] THRESHOLDS = new int[] { 15, 30, 60 };

	private final Map<String, Integer> lastNotifiedPointsByPlayer = new LinkedHashMap<String, Integer>();

	public void clear() {
		lastNotifiedPointsByPlayer.clear();
	}

	public void notifyLiveProgress(final String eventName, final Player player,
			final MapEventContributionTracker.ContributionSnapshot snapshot) {
		if (player == null || snapshot == null) {
			return;
		}
		final int points = resolvePoints(snapshot);
		final String playerName = player.getName();
		final Integer previous = lastNotifiedPointsByPlayer.get(playerName);
		if (previous != null && Math.abs(points - previous.intValue()) < 2
				&& resolveNearestThreshold(previous.intValue()) == resolveNearestThreshold(points)) {
			return;
		}
		lastNotifiedPointsByPlayer.put(playerName, Integer.valueOf(points));
		final Integer nextThreshold = resolveNearestThreshold(points);
		final String thresholdText = (nextThreshold == null)
				? "Masz już maksimum progów (60+)."
				: "Najbliższy próg: " + nextThreshold + " (brakuje " + Math.max(0, nextThreshold.intValue() - points) + ")";
		player.sendPrivateText(NotificationType.INFORMATION,
				"[Aktywność eventu] " + eventName + " • punkty: " + points + ". " + thresholdText);
	}

	private static Integer resolveNearestThreshold(final int points) {
		for (int threshold : THRESHOLDS) {
			if (points < threshold) {
				return Integer.valueOf(threshold);
			}
		}
		return null;
	}

	private static int resolvePoints(final MapEventContributionTracker.ContributionSnapshot snapshot) {
		final double points = (snapshot.getDamage() * 0.01d)
				+ (snapshot.getKillAssists() * 2.0d)
				+ (snapshot.getObjectiveActions() * 1.0d)
				+ (snapshot.getTimeInZoneSeconds() * 0.05d);
		return Math.max(0, (int) Math.round(points));
	}
}
