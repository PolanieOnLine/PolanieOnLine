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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.maps.dragon.DragonLandEvent;
import games.stendhal.server.maps.kikareukin.KikareukinAngelEvent;
import games.stendhal.server.maps.koscielisko.KoscieliskoGiantEscortEvent;

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
					+ listAvailableEventIds() + ".");
		}
		return event;
	}

	public static Set<String> listAvailableEventIds() {
		return Collections.unmodifiableSet(EVENT_INSTANCES.keySet());
	}

	public static Collection<ConfiguredMapEvent> listEvents() {
		return Collections.unmodifiableSet(new LinkedHashSet<>(EVENT_INSTANCES.values()));
	}

	/**
	 * Backward-compatible alias for callers that still use the old method name.
	 */
	public static Set<String> knownEventIds() {
		return listAvailableEventIds();
	}

	private static Map<String, ConfiguredMapEvent> createRegistry() {
		final Map<String, ConfiguredMapEvent> events = new LinkedHashMap<>();
		for (String configId : MapEventConfigLoader.availableConfigIds()) {
			try {
				registerEvent(events, configId, new ConfiguredMapEvent(LOGGER, MapEventConfigLoader.load(configId)));
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
		registerEvent(specializedEvents, "dragon_land", dragonEvent);
		registerEvent(specializedEvents, MapEventConfigLoader.DRAGON_LAND_DEFAULT, dragonEvent);

		final ConfiguredMapEvent kikareukinEvent = new KikareukinAngelEvent();
		registerEvent(specializedEvents, "kikareukin", kikareukinEvent);
		registerEvent(specializedEvents, MapEventConfigLoader.KIKAREUKIN_ANGEL_PREVIEW, kikareukinEvent);

		final ConfiguredMapEvent koscieliskoEvent = new KoscieliskoGiantEscortEvent();
		registerEvent(specializedEvents, "koscielisko_giant_escort", koscieliskoEvent);
		registerEvent(specializedEvents, MapEventConfigLoader.KOSCIELISKO_GIANT_ESCORT, koscieliskoEvent);

		final ConfiguredMapEvent tatryKuzniceBanditRaidEvent = new ConfiguredMapEvent(LOGGER,
				MapEventConfigLoader.load(MapEventConfigLoader.TATRY_KUZNICE_BANDIT_RAID));
		registerEvent(specializedEvents, "tatry_kuznice", tatryKuzniceBanditRaidEvent);
		registerEvent(specializedEvents, MapEventConfigLoader.TATRY_KUZNICE_BANDIT_RAID, tatryKuzniceBanditRaidEvent);

		return specializedEvents;
	}

	private static void registerEvent(final Map<String, ConfiguredMapEvent> events, final String eventId,
			final ConfiguredMapEvent event) {
		event.setEventIdIfMissing(eventId);
		events.put(eventId, event);
	}
}
