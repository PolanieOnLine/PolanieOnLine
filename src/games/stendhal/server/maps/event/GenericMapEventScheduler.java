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

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;

public class GenericMapEventScheduler implements ZoneConfigurator {
	private static final Logger LOGGER = Logger.getLogger(GenericMapEventScheduler.class);

	private static final String EVENT_ID_PARAMETER = "eventId";
	private static final String START_TIME_PARAMETER = "startTime";
	private static final String INTERVAL_DAYS_PARAMETER = "intervalDays";
	private static final String TRIGGER_TYPE_PARAMETER = "triggerType";
	private static final AtomicBoolean STARTUP_TABLE_LOGGED = new AtomicBoolean(false);

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String eventId = getRequiredAttribute(attributes, EVENT_ID_PARAMETER);
		if (eventId == null) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ ": missing required parameter '" + EVENT_ID_PARAMETER + "'.");
			return;
		}

		final ConfiguredMapEvent event = MapEventRegistry.getEvent(eventId);
		if (event == null) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ ": unknown eventId='" + eventId + "'. Available eventIds: "
					+ MapEventRegistry.listAvailableEventIds() + ".");
			return;
		}

		final CentralMapEventSchedule.Entry centralSchedule = CentralMapEventSchedule.get(eventId);
		warnIfLegacyXmlScheduleAttributesPresent(attributes, zone, eventId);

		final TriggerType triggerType = parseTriggerType(
				centralSchedule == null ? null : centralSchedule.getTriggerType(),
				zone,
				eventId);
		if (triggerType == null) {
			return;
		}

		if (triggerType.includesGuaranteedSchedule()) {
			final LocalTime startTime = resolveStartTime(event, centralSchedule, zone, eventId);
			if (startTime == null) {
				return;
			}
			final Integer intervalDays = resolveIntervalDays(event, centralSchedule, zone, eventId);
			if (intervalDays == null) {
				return;
			}
			event.scheduleGuaranteedStart(startTime, intervalDays.intValue());
		}

		if (triggerType.includesObserverTrigger()) {
			event.registerObserverZone(zone);
		}

		logStartupGuaranteedScheduleTable();
	}

	private void warnIfLegacyXmlScheduleAttributesPresent(final Map<String, String> attributes,
			final StendhalRPZone zone, final String eventId) {
		if (!hasValue(attributes.get(TRIGGER_TYPE_PARAMETER))
				&& !hasValue(attributes.get(START_TIME_PARAMETER))
				&& !hasValue(attributes.get(INTERVAL_DAYS_PARAMETER))) {
			return;
		}
		LOGGER.warn("Map event scheduler for zone " + zone.getName() + " and eventId='" + eventId
				+ "' ignores XML schedule parameters ('" + START_TIME_PARAMETER + "', '"
				+ INTERVAL_DAYS_PARAMETER + "', '" + TRIGGER_TYPE_PARAMETER
				+ "'). Values are resolved from central schedule and MapEventConfig defaults.");
	}

	private LocalTime resolveStartTime(final ConfiguredMapEvent event, final CentralMapEventSchedule.Entry centralSchedule,
			final StendhalRPZone zone, final String eventId) {
		final LocalTime resolved = centralSchedule != null
				? centralSchedule.getStartTime()
				: event.getConfig().getDefaultStartTime();
		if (resolved != null) {
			return resolved;
		}
		LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
				+ " and eventId='" + eventId + "': missing central/default '" + START_TIME_PARAMETER
				+ "' value.");
		return null;
	}

	private Integer resolveIntervalDays(final ConfiguredMapEvent event,
			final CentralMapEventSchedule.Entry centralSchedule,
			final StendhalRPZone zone, final String eventId) {
		final int resolved = centralSchedule != null
				? centralSchedule.getIntervalDays()
				: event.getConfig().getDefaultIntervalDays();
		if (resolved > 0) {
			return Integer.valueOf(resolved);
		}
		LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
				+ " and eventId='" + eventId + "': missing/invalid central/default '"
				+ INTERVAL_DAYS_PARAMETER + "' value.");
		return null;
	}

	private static String getRequiredAttribute(final Map<String, String> attributes, final String key) {
		final String value = attributes.get(key);
		if (!hasValue(value)) {
			return null;
		}
		return value.trim();
	}

	private static boolean hasValue(final String value) {
		return value != null && !value.trim().isEmpty();
	}

	private TriggerType parseTriggerType(final String configuredDefault, final StendhalRPZone zone,
			final String eventId) {
		final TriggerType parsed = parseTriggerTypeValue(configuredDefault);
		if (parsed != null) {
			return parsed;
		}
		LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
				+ " and eventId='" + eventId + "': invalid central/default triggerType='"
				+ configuredDefault + "'. Allowed values: guaranteed, observer, both.");
		return null;
	}

	private TriggerType parseTriggerTypeValue(final String configuredDefault) {
		if (configuredDefault == null || configuredDefault.trim().isEmpty()) {
			return TriggerType.BOTH;
		}
		try {
			return TriggerType.valueOf(configuredDefault.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private void logStartupGuaranteedScheduleTable() {
		if (!STARTUP_TABLE_LOGGED.compareAndSet(false, true)) {
			return;
		}

		final List<StartupRow> rows = new ArrayList<>();
		final Map<LocalDateTime, Integer> collisionsByStart = new HashMap<>();

		for (String eventId : MapEventConfigLoader.availableConfigIds()) {
			final ConfiguredMapEvent event = MapEventRegistry.getEvent(eventId);
			if (event == null) {
				continue;
			}

			final CentralMapEventSchedule.Entry centralSchedule = CentralMapEventSchedule.get(eventId);
			final LocalTime startTime = centralSchedule != null
					? centralSchedule.getStartTime()
					: event.getConfig().getDefaultStartTime();
			final int intervalDays = centralSchedule != null
					? centralSchedule.getIntervalDays()
					: event.getConfig().getDefaultIntervalDays();
			final TriggerType triggerType = parseTriggerTypeValue(
					centralSchedule == null ? null : centralSchedule.getTriggerType());

			LocalDateTime nearestStart = null;
			if (triggerType != null && triggerType.includesGuaranteedSchedule() && startTime != null) {
				nearestStart = BaseMapEvent.nearestGuaranteedStart(startTime);
				collisionsByStart.put(nearestStart,
						Integer.valueOf(collisionsByStart.getOrDefault(nearestStart, Integer.valueOf(0)).intValue() + 1));
			}

			rows.add(new StartupRow(eventId,
					triggerType == null ? "invalid" : triggerType.name().toLowerCase(Locale.ROOT),
					centralSchedule != null ? "central" : "fallback",
					nearestStart,
					intervalDays));
		}

		int collisionSlots = 0;
		for (Integer count : collisionsByStart.values()) {
			if (count.intValue() > 1) {
				collisionSlots++;
			}
		}

		final StringBuilder table = new StringBuilder();
		table.append("Map event startup schedule table (nearest starts):\n");
		table.append(String.format(Locale.ROOT, " | %-32s | %-10s | %-8s | %-19s | %-8s | %-9s |%n",
				"eventId", "trigger", "source", "nearestStart", "interval", "collision"));

		for (StartupRow row : rows) {
			final String nearest = row.nearestStart == null ? "n/a" : row.nearestStart.toString();
			final String interval = row.nearestStart == null ? "n/a" : Integer.toString(row.intervalDays);
			final String collision;
			if (row.nearestStart == null) {
				collision = "n/a";
			} else {
				final int count = collisionsByStart.getOrDefault(row.nearestStart, Integer.valueOf(0)).intValue();
				collision = count > 1 ? "x" + count : "ok";
			}
			table.append(String.format(Locale.ROOT, " | %-32s | %-10s | %-8s | %-19s | %-8s | %-9s |%n",
					row.eventId, row.triggerType, row.source, nearest, interval, collision));
		}

		table.append("Startup collision summary: ").append(collisionSlots).append(" slot(s) with collisions.");
		LOGGER.info(table.toString());
	}

	private static final class StartupRow {
		private final String eventId;
		private final String triggerType;
		private final String source;
		private final LocalDateTime nearestStart;
		private final int intervalDays;

		private StartupRow(final String eventId, final String triggerType, final String source,
				final LocalDateTime nearestStart, final int intervalDays) {
			this.eventId = eventId;
			this.triggerType = triggerType;
			this.source = source;
			this.nearestStart = nearestStart;
			this.intervalDays = intervalDays;
		}
	}

	private enum TriggerType {
		GUARANTEED,
		OBSERVER,
		BOTH;

		boolean includesGuaranteedSchedule() {
			return this == GUARANTEED || this == BOTH;
		}

		boolean includesObserverTrigger() {
			return this == OBSERVER || this == BOTH;
		}
	}
}
