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
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventRegistry;

/**
 * Starts a configured map event by id.
 */
public class StartMapEvent extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args == null || args.isEmpty() || args.size() > 2) {
			usage(admin);
			return;
		}

		final String eventId = args.get(0);
		final String mode = args.size() == 2 ? args.get(1) : "safe";
		final boolean force;
		if ("force".equalsIgnoreCase(mode)) {
			force = true;
		} else if ("safe".equalsIgnoreCase(mode)) {
			force = false;
		} else {
			sandbox.privateText(admin, "Nieznany tryb startu: '" + mode + "'. Dozwolone: force, safe.");
			usage(admin);
			return;
		}

		final ConfiguredMapEvent event = MapEventRegistry.getEvent(eventId);
		if (event == null) {
			sandbox.privateText(admin,
					"Nieznany eventId: '" + eventId + "'. Dostępne eventy: " + MapEventRegistry.knownEventIds() + ".");
			return;
		}

		if (event.startFromScript(force)) {
			sandbox.privateText(admin, "Start eventu '" + eventId + "' zakończony sukcesem (mode=" + mode.toLowerCase() + ").");
			return;
		}

		sandbox.privateText(admin, "Event '" + eventId + "' jest już aktywny.");
	}

	private void usage(final Player admin) {
		sandbox.privateText(admin, "Użycie: /script StartMapEvent.class <eventId> [force|safe]");
	}
}
