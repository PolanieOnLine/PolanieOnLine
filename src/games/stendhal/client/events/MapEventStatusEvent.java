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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import games.stendhal.client.entity.RPEntity;
import games.stendhal.client.gui.status.ActiveMapEventStatus;
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
			final List<String> activityTop = resolveActivityTop();
			final List<String> zones = resolveZones();
			final List<ActiveMapEventStatus.CapturePointStatus> capturePoints = resolveCapturePoints();

			MapEventStatusStore.get().updateStatus(eventId, eventName, isActive, remainingSeconds, totalSeconds,
					eventTotalSpawnedCreatures, eventDefeatedCreatures, eventDefeatPercent,
					currentWave, totalWaves, defenseStatus, activityTop, zones, capturePoints);
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

	private List<String> resolveActivityTop() {
		if (event.has("activityTop")) {
			return event.getList("activityTop");
		}
		return Collections.emptyList();
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

	private List<ActiveMapEventStatus.CapturePointStatus> resolveCapturePoints() {
		if (!event.has("capturePoints")) {
			return Collections.emptyList();
		}
		final Object parsed = JSONValue.parse(event.get("capturePoints"));
		if (!(parsed instanceof JSONArray)) {
			return Collections.emptyList();
		}
		final JSONArray pointsArray = (JSONArray) parsed;
		if (pointsArray.isEmpty()) {
			return Collections.emptyList();
		}
		final List<ActiveMapEventStatus.CapturePointStatus> points =
				new ArrayList<ActiveMapEventStatus.CapturePointStatus>();
		for (Object rawPoint : pointsArray) {
			if (!(rawPoint instanceof JSONObject)) {
				continue;
			}
			final ActiveMapEventStatus.CapturePointStatus mapped = mapCapturePoint((JSONObject) rawPoint);
			if (mapped != null) {
				points.add(mapped);
			}
		}
		if (points.isEmpty()) {
			return Collections.emptyList();
		}
		return points;
	}

	private ActiveMapEventStatus.CapturePointStatus mapCapturePoint(final JSONObject rawPoint) {
		final String pointId = readString(rawPoint, "pointId");
		final String zone = readString(rawPoint, "zone");
		final int x = readInt(rawPoint, "x", 0);
		final int y = readInt(rawPoint, "y", 0);
		final int radiusTiles = readInt(rawPoint, "radiusTiles", 0);
		final int progressPercent = readInt(rawPoint, "progressPercent", 0);
		final String ownerFaction = firstNonBlank(readString(rawPoint, "owner"), readString(rawPoint, "faction"));
		final boolean contested = readBoolean(rawPoint, "contested");
		final int remainingBossWaves = readInt(rawPoint, "remainingBossWaves", 0);
		if ((pointId == null) || (zone == null)) {
			return null;
		}
		return new ActiveMapEventStatus.CapturePointStatus(pointId, zone, x, y, radiusTiles,
				progressPercent, ownerFaction, contested, remainingBossWaves);
	}

	private String firstNonBlank(final String first, final String second) {
		if (first != null && !first.trim().isEmpty()) {
			return first;
		}
		return second;
	}

	private String readString(final Map<?, ?> source, final String key) {
		final Object value = source.get(key);
		if (!(value instanceof String)) {
			return null;
		}
		return (String) value;
	}

	private int readInt(final Map<?, ?> source, final String key, final int fallbackValue) {
		final Object value = source.get(key);
		if (value instanceof Number) {
			return ((Number) value).intValue();
		}
		if (value instanceof String) {
			try {
				return Integer.parseInt((String) value);
			} catch (NumberFormatException e) {
				return fallbackValue;
			}
		}
		return fallbackValue;
	}

	private boolean readBoolean(final Map<?, ?> source, final String key) {
		final Object value = source.get(key);
		if (value instanceof Boolean) {
			return ((Boolean) value).booleanValue();
		}
		if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		}
		return false;
	}

	private boolean parseBoolean(final marauroa.common.game.RPEvent source, final String key) {
		if (!source.has(key)) {
			return false;
		}
		return Boolean.parseBoolean(source.get(key));
	}
}
