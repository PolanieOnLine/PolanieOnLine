/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
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
import java.time.format.DateTimeParseException;
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
		final TriggerType triggerType = parseTriggerType(
				attributes.get(TRIGGER_TYPE_PARAMETER),
				centralSchedule == null ? null : centralSchedule.getTriggerType(),
				zone,
				eventId);
		if (triggerType == null) {
			return;
		}

		if (triggerType.includesGuaranteedSchedule()) {
			final LocalTime configStartTime = centralSchedule != null
					? centralSchedule.getStartTime()
					: event.getConfig().getDefaultStartTime();
			final Integer configIntervalDays = Integer.valueOf(centralSchedule != null
					? centralSchedule.getIntervalDays()
					: event.getConfig().getDefaultIntervalDays());

			final LocalTime startTime = parseStartTime(
					attributes.get(START_TIME_PARAMETER),
					configStartTime,
					zone,
					eventId);
			if (startTime == null) {
				return;
			}
			final Integer intervalDays = parseIntervalDays(
					attributes.get(INTERVAL_DAYS_PARAMETER),
					configIntervalDays,
					zone,
					eventId);
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

	private static String getRequiredAttribute(final Map<String, String> attributes, final String key) {
		final String value = attributes.get(key);
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}

	private LocalTime parseStartTime(final String value, final LocalTime configuredDefault, final StendhalRPZone zone,
			final String eventId) {
		if (value == null || value.trim().isEmpty()) {
			if (configuredDefault != null) {
				return configuredDefault;
			}
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': missing parameter '" + START_TIME_PARAMETER
					+ "' and no default found in MapEventConfig.");
			return null;
		}
		try {
			final LocalTime xmlValue = LocalTime.parse(value.trim());
			if (configuredDefault != null && !configuredDefault.equals(xmlValue)) {
				LOGGER.warn("Map event scheduler for zone " + zone.getName() + " and eventId='" + eventId
						+ "' uses XML parameter '" + START_TIME_PARAMETER + "=" + xmlValue
						+ "' instead of MapEventConfig default '" + configuredDefault + "'.");
			}
			return xmlValue;
		} catch (DateTimeParseException e) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': invalid '" + START_TIME_PARAMETER + "' value '"
					+ value + "'. Expected ISO local time (e.g. 20:00).", e);
			return null;
		}
	}

	private Integer parseIntervalDays(final String value, final Integer configuredDefault, final StendhalRPZone zone,
			final String eventId) {
		if (value == null || value.trim().isEmpty()) {
			if (configuredDefault != null) {
				return configuredDefault;
			}
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': missing parameter '" + INTERVAL_DAYS_PARAMETER
					+ "' and no default found in MapEventConfig.");
			return null;
		}
		try {
			final int parsed = Integer.parseInt(value.trim());
			if (parsed <= 0) {
				LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
						+ " and eventId='" + eventId + "': '" + INTERVAL_DAYS_PARAMETER
						+ "' must be greater than zero, got " + parsed + ".");
				return null;
			}
			if (configuredDefault != null && configuredDefault.intValue() != parsed) {
				LOGGER.warn("Map event scheduler for zone " + zone.getName() + " and eventId='" + eventId
						+ "' uses XML parameter '" + INTERVAL_DAYS_PARAMETER + "=" + parsed
						+ "' instead of MapEventConfig default '" + configuredDefault + "'.");
			}
			return Integer.valueOf(parsed);
		} catch (NumberFormatException e) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': invalid '" + INTERVAL_DAYS_PARAMETER + "' value '"
					+ value + "'.", e);
			return null;
		}
	}

	private TriggerType parseTriggerType(final String value, final String configuredDefault, final StendhalRPZone zone,
			final String eventId) {
		final TriggerType parsed = parseTriggerTypeValue(value, configuredDefault);
		if (parsed != null) {
			return parsed;
		}
		if (value != null && !value.trim().isEmpty()) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': invalid '" + TRIGGER_TYPE_PARAMETER + "' value '"
					+ value + "'. Allowed values: guaranteed, observer, both.");
		} else {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': invalid central triggerType='"
					+ configuredDefault + "'. Allowed values: guaranteed, observer, both.");
		}
		return null;
	}

	private TriggerType parseTriggerTypeValue(final String value, final String configuredDefault) {
		final String candidate = (value == null || value.trim().isEmpty()) ? configuredDefault : value;
		if (candidate == null || candidate.trim().isEmpty()) {
			return TriggerType.BOTH;
		}
		try {
			return TriggerType.valueOf(candidate.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private void logStartupGuaranteedScheduleTable() {
		if (!STARTUP_TABLE_LOGGED.compareAndSet(false, true)) {
			return;
		}
		final StringBuilder table = new StringBuilder();
		table.append("Map event guaranteed schedule table:\n");
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
			final TriggerType triggerType = parseTriggerTypeValue(null,
					centralSchedule == null ? null : centralSchedule.getTriggerType());
			table.append(" - ").append(eventId)
					.append(" | triggerType=").append(triggerType == null ? "invalid" : triggerType.name().toLowerCase(Locale.ROOT));
			if (triggerType != null && triggerType.includesGuaranteedSchedule() && startTime != null) {
				final LocalDateTime nextStart = BaseMapEvent.nearestGuaranteedStart(startTime);
				table.append(" | nearestGuaranteedStart=").append(nextStart)
						.append(" | intervalDays=").append(intervalDays);
			} else {
				table.append(" | nearestGuaranteedStart=n/a");
			}
			table.append("\n");
		}
		LOGGER.info(table.toString());
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
