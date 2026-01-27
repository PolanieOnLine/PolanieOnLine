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

import java.time.LocalTime;
import java.util.Map;

import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;

public class DragonLandEventScheduler implements ZoneConfigurator {
	private static final LocalTime EVENT_START_TIME = LocalTime.of(20, 0);

	@Override
	public void configureZone(StendhalRPZone zone, Map<String, String> attributes) {
		DragonLandEvent.scheduleEveryTwoDaysAt(EVENT_START_TIME);
		DragonLandEvent.registerZoneObserver(zone);
	}
}
