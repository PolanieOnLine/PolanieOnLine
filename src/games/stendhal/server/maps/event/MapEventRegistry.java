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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.maps.dragon.DragonLandEvent;

public final class MapEventRegistry {
	private static final Logger LOGGER = Logger.getLogger(MapEventRegistry.class);
	private static final Map<String, ConfiguredMapEvent> EVENT_INSTANCES = createRegistry();

	private MapEventRegistry() {
		// utility class
	}

	public static ConfiguredMapEvent getEvent(final String eventId) {
		if (eventId == null) {
			LOGGER.warn("Map event lookup failed: eventId is null.");
			return null;
		}

		final String normalizedEventId = eventId.trim().toLowerCase(Locale.ROOT);
		if (normalizedEventId.isEmpty()) {
			LOGGER.warn("Map event lookup failed: eventId is blank.");
			return null;
		}

		final ConfiguredMapEvent event = EVENT_INSTANCES.get(normalizedEventId);
		if (event == null) {
			final String lookupScope = MapEventConfigLoader.hasConfigId(normalizedEventId)
					? "configured but not registered"
					: "unknown";
			LOGGER.warn("Map event lookup failed for eventId='" + eventId + "' (normalized='"
					+ normalizedEventId + "', scope=" + lookupScope + "). Available eventIds: "
					+ knownEventIds() + ".");
		}
		return event;
	}

	public static Set<String> knownEventIds() {
		return Collections.unmodifiableSet(EVENT_INSTANCES.keySet());
	}

	private static Map<String, ConfiguredMapEvent> createRegistry() {
		final Map<String, ConfiguredMapEvent> events = new LinkedHashMap<>();
		for (String configId : MapEventConfigLoader.availableConfigIds()) {
			try {
				events.put(configId, new ConfiguredMapEvent(LOGGER, MapEventConfigLoader.load(configId)));
			} catch (IllegalArgumentException e) {
				LOGGER.error("Failed to build default configured event for configId='" + configId + "'.", e);
			}
		}

		events.putAll(createSpecializedEvents());

		if (events.isEmpty()) {
			LOGGER.warn("Map event registry initialized without any events.");
		}

		return Collections.unmodifiableMap(events);
	}

	private static Map<String, ConfiguredMapEvent> createSpecializedEvents() {
		final Map<String, ConfiguredMapEvent> specializedEvents = new LinkedHashMap<>();

		final DragonLandEvent dragonEvent = DragonLandEvent.getInstance();
		specializedEvents.put("dragon_land", dragonEvent);
		specializedEvents.put(MapEventConfigLoader.DRAGON_LAND_DEFAULT, dragonEvent);

		final ConfiguredMapEvent kikareukinEvent = new KikareukinAngelEvent();
		specializedEvents.put("kikareukin", kikareukinEvent);
		specializedEvents.put(MapEventConfigLoader.KIKAREUKIN_ANGEL_PREVIEW, kikareukinEvent);

		return specializedEvents;
	}
}
