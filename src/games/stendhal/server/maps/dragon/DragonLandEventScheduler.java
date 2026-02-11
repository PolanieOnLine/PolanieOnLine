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
package games.stendhal.server.maps.dragon;

import java.util.HashMap;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.maps.event.GenericMapEventScheduler;
import games.stendhal.server.maps.event.MapEventConfigLoader;

public class DragonLandEventScheduler implements ZoneConfigurator {
	private static final String EVENT_START_TIME_TEXT = "20:00";
	private static final String DEFAULT_TRIGGER_TYPE = "BOTH";
	private final GenericMapEventScheduler delegate = new GenericMapEventScheduler();

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		final Map<String, String> mergedAttributes = new HashMap<>(attributes);
		mergedAttributes.putIfAbsent("eventId", MapEventConfigLoader.DRAGON_LAND_DEFAULT);
		mergedAttributes.putIfAbsent("startTime", EVENT_START_TIME_TEXT);
		mergedAttributes.putIfAbsent("intervalDays", String.valueOf(2));
		mergedAttributes.putIfAbsent("triggerType", DEFAULT_TRIGGER_TYPE);
		delegate.configureZone(zone, mergedAttributes);
	}
}
