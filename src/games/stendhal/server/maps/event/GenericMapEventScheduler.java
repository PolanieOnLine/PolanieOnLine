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

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.maps.dragon.DragonLandEvent;

public class GenericMapEventScheduler implements ZoneConfigurator {
	private static final Logger LOGGER = Logger.getLogger(GenericMapEventScheduler.class);

	private static final String EVENT_ID_PARAMETER = "eventId";
	private static final String START_TIME_PARAMETER = "startTime";
	private static final String INTERVAL_DAYS_PARAMETER = "intervalDays";
	private static final String TRIGGER_TYPE_PARAMETER = "triggerType";

	private static final Map<String, MapEventBinding> EVENT_BINDINGS = createEventBindings();

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		final String eventId = getRequiredAttribute(attributes, EVENT_ID_PARAMETER);
		if (eventId == null) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ ": missing required parameter '" + EVENT_ID_PARAMETER + "'.");
			return;
		}

		final MapEventBinding binding = EVENT_BINDINGS.get(eventId.toLowerCase(Locale.ROOT));
		if (binding == null) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ ": unknown eventId='" + eventId + "'. Available eventIds: "
					+ EVENT_BINDINGS.keySet() + ".");
			return;
		}

		final TriggerType triggerType = parseTriggerType(attributes.get(TRIGGER_TYPE_PARAMETER), zone, eventId);
		if (triggerType == null) {
			return;
		}

		if (triggerType.includesGuaranteedSchedule()) {
			final LocalTime startTime = parseStartTime(attributes.get(START_TIME_PARAMETER), zone, eventId);
			if (startTime == null) {
				return;
			}
			final Integer intervalDays = parseIntervalDays(attributes.get(INTERVAL_DAYS_PARAMETER), zone, eventId);
			if (intervalDays == null) {
				return;
			}
			binding.scheduleEveryDaysAt(startTime, intervalDays.intValue());
		}

		if (triggerType.includesObserverTrigger()) {
			binding.registerZoneObserver(zone);
		}
	}

	private static Map<String, MapEventBinding> createEventBindings() {
		final Map<String, MapEventBinding> bindings = new HashMap<>();
		final MapEventBinding dragonBinding = new MapEventBinding() {
			@Override
			public void scheduleEveryDaysAt(final LocalTime time, final int intervalDays) {
				DragonLandEvent.scheduleGuaranteedEveryDaysAt(time, intervalDays);
			}

			@Override
			public void registerZoneObserver(final StendhalRPZone zone) {
				DragonLandEvent.registerZoneObserver(zone);
			}
		};

		bindings.put("dragon_land", dragonBinding);
		bindings.put(MapEventConfigLoader.DRAGON_LAND_DEFAULT, dragonBinding);
		return Collections.unmodifiableMap(bindings);
	}

	private static String getRequiredAttribute(final Map<String, String> attributes, final String key) {
		final String value = attributes.get(key);
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		return value.trim();
	}

	private LocalTime parseStartTime(final String value, final StendhalRPZone zone, final String eventId) {
		if (value == null || value.trim().isEmpty()) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': missing required parameter '" + START_TIME_PARAMETER + "'.");
			return null;
		}
		try {
			return LocalTime.parse(value.trim());
		} catch (DateTimeParseException e) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': invalid '" + START_TIME_PARAMETER + "' value '"
					+ value + "'. Expected ISO local time (e.g. 20:00).", e);
			return null;
		}
	}

	private Integer parseIntervalDays(final String value, final StendhalRPZone zone, final String eventId) {
		if (value == null || value.trim().isEmpty()) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': missing required parameter '" + INTERVAL_DAYS_PARAMETER
					+ "'.");
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
			return Integer.valueOf(parsed);
		} catch (NumberFormatException e) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': invalid '" + INTERVAL_DAYS_PARAMETER + "' value '"
					+ value + "'.", e);
			return null;
		}
	}

	private TriggerType parseTriggerType(final String value, final StendhalRPZone zone, final String eventId) {
		if (value == null || value.trim().isEmpty()) {
			return TriggerType.BOTH;
		}
		try {
			return TriggerType.valueOf(value.trim().toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			LOGGER.error("Cannot configure map event scheduler for zone " + zone.getName()
					+ " and eventId='" + eventId + "': invalid '" + TRIGGER_TYPE_PARAMETER + "' value '"
					+ value + "'. Allowed values: guaranteed, observer, both.");
			return null;
		}
	}

	private interface MapEventBinding {
		void scheduleEveryDaysAt(LocalTime time, int intervalDays);

		void registerZoneObserver(StendhalRPZone zone);
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
