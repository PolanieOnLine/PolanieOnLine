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

import games.stendhal.server.maps.dragon.DragonLandEvent;

public final class MapEventRegistry {
	private static final Map<String, ConfiguredMapEvent> EVENT_INSTANCES = createRegistry();

	private MapEventRegistry() {
		// utility class
	}

	public static ConfiguredMapEvent getEvent(final String eventId) {
		if (eventId == null) {
			return null;
		}
		return EVENT_INSTANCES.get(eventId.toLowerCase(Locale.ROOT));
	}

	public static Set<String> knownEventIds() {
		return Collections.unmodifiableSet(EVENT_INSTANCES.keySet());
	}

	private static Map<String, ConfiguredMapEvent> createRegistry() {
		final Map<String, ConfiguredMapEvent> events = new LinkedHashMap<>();

		final DragonLandEvent dragonEvent = DragonLandEvent.getInstance();
		events.put("dragon_land", dragonEvent);
		events.put(MapEventConfigLoader.DRAGON_LAND_DEFAULT, dragonEvent);

		final ConfiguredMapEvent kikareukinEvent = new KikareukinAngelEvent();
		events.put("kikareukin", kikareukinEvent);
		events.put(MapEventConfigLoader.KIKAREUKIN_ANGEL_PREVIEW, kikareukinEvent);

		return Collections.unmodifiableMap(events);
	}
}
