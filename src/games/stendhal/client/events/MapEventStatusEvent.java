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
package games.stendhal.client.events;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.status.MapEventStatusStore;

/**
 * Parses map event status network events and maps them into UI model store.
 */
class MapEventStatusEvent extends Event<RPEntity> {
	private static final Logger logger = Logger.getLogger(MapEventStatusEvent.class);

	@Override
	public void execute() {
		try {
			final String eventId = event.has("eventId") ? event.get("eventId") : null;
			final String eventName = event.has("eventName") ? event.get("eventName") : "";
			final boolean isActive = parseBoolean(event, "isActive");
			final Integer remainingSeconds = event.has("remainingSeconds") ? Integer.valueOf(event.getInt("remainingSeconds")) : null;
			final Integer totalSeconds = resolveTotalSeconds();
			final Integer eventTotalSpawnedCreatures = event.has("eventTotalSpawnedCreatures")
					? Integer.valueOf(event.getInt("eventTotalSpawnedCreatures")) : Integer.valueOf(0);
			final Integer eventDefeatedCreatures = event.has("eventDefeatedCreatures")
					? Integer.valueOf(event.getInt("eventDefeatedCreatures")) : Integer.valueOf(0);
			final Integer eventDefeatPercent = event.has("eventDefeatPercent")
					? Integer.valueOf(event.getInt("eventDefeatPercent")) : Integer.valueOf(0);
			final Integer currentWave = event.has("currentWave") ? Integer.valueOf(event.getInt("currentWave")) : Integer.valueOf(0);
			final Integer totalWaves = event.has("totalWaves") ? Integer.valueOf(event.getInt("totalWaves")) : Integer.valueOf(0);
			final String defenseStatus = event.has("defenseStatus") ? event.get("defenseStatus") : "";
			final List<String> zones = resolveZones();

			MapEventStatusStore.get().updateStatus(eventId, eventName, isActive, remainingSeconds, totalSeconds,
					eventTotalSpawnedCreatures, eventDefeatedCreatures, eventDefeatPercent,
					currentWave, totalWaves, defenseStatus, zones);
		} catch (RuntimeException e) {
			logger.error("Failed to parse map event status event: " + event, e);
		}
	}

	private Integer resolveTotalSeconds() {
		if (event.has("totalSeconds")) {
			return Integer.valueOf(event.getInt("totalSeconds"));
		}
		if (event.has("totalDurationSeconds")) {
			return Integer.valueOf(event.getInt("totalDurationSeconds"));
		}
		return null;
	}

	private List<String> resolveZones() {
		if (event.has("zones")) {
			return event.getList("zones");
		}
		if (event.has("allowedZones")) {
			return event.getList("allowedZones");
		}
		return Collections.emptyList();
	}

	private boolean parseBoolean(final marauroa.common.game.RPEvent source, final String key) {
		if (!source.has(key)) {
			return false;
		}
		return Boolean.parseBoolean(source.get(key));
	}
}
