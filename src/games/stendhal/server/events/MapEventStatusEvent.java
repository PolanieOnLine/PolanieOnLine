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
package games.stendhal.server.events;

import java.util.List;

import games.stendhal.common.constants.Events;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;

/**
 * Snapshot of map event status for client-side UI.
 */
public class MapEventStatusEvent extends RPEvent {

	public static void generateRPClass() {
		final RPClass rpclass = new RPClass(Events.MAP_EVENT_STATUS);
		rpclass.add(DefinitionClass.ATTRIBUTE, "eventId", Type.STRING);
		rpclass.add(DefinitionClass.ATTRIBUTE, "eventName", Type.STRING);
		rpclass.add(DefinitionClass.ATTRIBUTE, "isActive", Type.STRING);
		rpclass.add(DefinitionClass.ATTRIBUTE, "remainingSeconds", Type.INT);
		rpclass.add(DefinitionClass.ATTRIBUTE, "totalSeconds", Type.INT);
		rpclass.add(DefinitionClass.ATTRIBUTE, "zones", Type.VERY_LONG_STRING);
	}

	public MapEventStatusEvent(final String eventId, final String eventName, final boolean isActive,
			final Integer remainingSeconds, final Integer totalSeconds,
			final List<String> zones) {
		super(Events.MAP_EVENT_STATUS);
		put("eventId", eventId);
		put("eventName", eventName);
		put("isActive", Boolean.toString(isActive));
		if (remainingSeconds != null) {
			put("remainingSeconds", remainingSeconds.intValue());
		}
		if (totalSeconds != null) {
			put("totalSeconds", totalSeconds.intValue());
		}
		if (zones != null) {
			put("zones", zones);
		}
	}
}
