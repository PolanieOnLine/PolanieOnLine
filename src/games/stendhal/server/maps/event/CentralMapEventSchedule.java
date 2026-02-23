/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public final class CentralMapEventSchedule {
	private static final Logger LOGGER = Logger.getLogger(CentralMapEventSchedule.class);
	private static final String SCHEDULE_RESOURCE = "data/conf/map_event_schedule.json";
	private static final Map<String, Entry> ENTRIES_BY_EVENT_ID = loadEntries();

	private CentralMapEventSchedule() {
		// utility class
	}

	public static Entry get(final String eventId) {
		if (eventId == null) {
			return null;
		}
		return ENTRIES_BY_EVENT_ID.get(eventId.trim().toLowerCase(Locale.ROOT));
	}

	private static Map<String, Entry> loadEntries() {
		final InputStream stream = CentralMapEventSchedule.class.getClassLoader().getResourceAsStream(SCHEDULE_RESOURCE);
		if (stream == null) {
			LOGGER.warn("Central map event schedule resource not found: " + SCHEDULE_RESOURCE + ".");
			return Collections.emptyMap();
		}

		try (InputStream in = stream; InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
			final Object parsed = JSONValue.parse(reader);
			if (!(parsed instanceof JSONObject)) {
				LOGGER.error("Central map event schedule has invalid root. Expected JSON object in "
						+ SCHEDULE_RESOURCE + ".");
				return Collections.emptyMap();
			}

			final JSONObject root = (JSONObject) parsed;
			final Map<String, Entry> entries = new LinkedHashMap<>();
			for (Object keyObject : root.keySet()) {
				if (!(keyObject instanceof String)) {
					continue;
				}
				final String eventId = ((String) keyObject).trim().toLowerCase(Locale.ROOT);
				final Object rawValue = root.get(keyObject);
				if (!(rawValue instanceof JSONObject)) {
					LOGGER.error("Central schedule entry for eventId='" + eventId + "' must be an object.");
					continue;
				}
				final Entry entry = parseEntry(eventId, (JSONObject) rawValue);
				if (entry != null) {
					entries.put(eventId, entry);
				}
			}
			LOGGER.info("Loaded central map event schedule entries: " + entries.keySet() + ".");
			return Collections.unmodifiableMap(entries);
		} catch (Exception e) {
			LOGGER.error("Cannot load central map event schedule from " + SCHEDULE_RESOURCE + ".", e);
			return Collections.emptyMap();
		}
	}

	private static Entry parseEntry(final String eventId, final JSONObject source) {
		final LocalTime startTime = parseStartTime(eventId, source.get("startTime"));
		final Integer intervalDays = parseIntervalDays(eventId, source.get("intervalDays"));
		final String triggerType = parseTriggerType(eventId, source.get("triggerType"));
		if (startTime == null || intervalDays == null || triggerType == null) {
			return null;
		}
		return new Entry(startTime, intervalDays.intValue(), triggerType);
	}

	private static LocalTime parseStartTime(final String eventId, final Object value) {
		if (!(value instanceof String)) {
			LOGGER.error("Central schedule eventId='" + eventId + "' has missing/invalid startTime.");
			return null;
		}
		try {
			return LocalTime.parse(((String) value).trim());
		} catch (DateTimeParseException e) {
			LOGGER.error("Central schedule eventId='" + eventId + "' has invalid startTime='" + value + "'.", e);
			return null;
		}
	}

	private static Integer parseIntervalDays(final String eventId, final Object value) {
		final int parsed;
		if (value instanceof Number) {
			parsed = ((Number) value).intValue();
		} else if (value instanceof String) {
			try {
				parsed = Integer.parseInt(((String) value).trim());
			} catch (NumberFormatException e) {
				LOGGER.error("Central schedule eventId='" + eventId + "' has invalid intervalDays='" + value + "'.", e);
				return null;
			}
		} else {
			LOGGER.error("Central schedule eventId='" + eventId + "' has missing/invalid intervalDays.");
			return null;
		}
		if (parsed <= 0) {
			LOGGER.error("Central schedule eventId='" + eventId + "' has non-positive intervalDays=" + parsed + ".");
			return null;
		}
		return Integer.valueOf(parsed);
	}

	private static String parseTriggerType(final String eventId, final Object value) {
		if (!(value instanceof String)) {
			LOGGER.error("Central schedule eventId='" + eventId + "' has missing/invalid triggerType.");
			return null;
		}
		final String normalized = ((String) value).trim().toLowerCase(Locale.ROOT);
		if (normalized.isEmpty()) {
			LOGGER.error("Central schedule eventId='" + eventId + "' has blank triggerType.");
			return null;
		}
		return normalized;
	}

	public static final class Entry {
		private final LocalTime startTime;
		private final int intervalDays;
		private final String triggerType;

		private Entry(final LocalTime startTime, final int intervalDays, final String triggerType) {
			this.startTime = startTime;
			this.intervalDays = intervalDays;
			this.triggerType = triggerType;
		}

		public LocalTime getStartTime() {
			return startTime;
		}

		public int getIntervalDays() {
			return intervalDays;
		}

		public String getTriggerType() {
			return triggerType;
		}
	}
}
