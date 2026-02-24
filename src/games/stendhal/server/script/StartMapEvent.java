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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventRegistry;

/**
 * Starts a configured map event by id.
 */
public class StartMapEvent extends ScriptImpl {
	private static final Logger LOGGER = Logger.getLogger(StartMapEvent.class);
	private static final int MAX_PRIVATE_TEXT_LENGTH = 1500;

	@Override
	public void execute(final Player admin, final List<String> args) {
		if (admin.getAdminLevel() <= 0) {
			admin.sendPrivateText(NotificationType.ERROR, "Brak uprawnień. Skrypt mogą uruchamiać tylko GM/administratorzy.");
			return;
		}
		if (args == null || args.isEmpty() || args.size() > 2) {
			usage(admin);
			return;
		}

		final String eventId = args.get(0);
		if (isListRequest(eventId)) {
			if (args.size() > 1) {
				usage(admin);
				return;
			}
			listEvents(admin);
			return;
		}

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
					"Nieznany eventId: '" + eventId + "'. Dostępne eventy: " + MapEventRegistry.listAvailableEventIds() + ".");
			return;
		}

		if (event.startFromScript(force)) {
			LOGGER.info("Map event start requested by '" + admin.getName() + "': eventId='" + eventId
					+ "', mode='" + mode.toLowerCase() + "'.");
			sandbox.privateText(admin, "Start eventu '" + eventId + "' zakończony sukcesem (mode=" + mode.toLowerCase() + ").");
			return;
		}

		sandbox.privateText(admin, "Event '" + eventId + "' jest już aktywny.");
	}

	private boolean isListRequest(final String arg) {
		if (arg == null) {
			return false;
		}
		final String normalized = arg.trim().toLowerCase(Locale.ROOT);
		return "-list".equals(normalized) || "list".equals(normalized);
	}

	private void listEvents(final Player admin) {
		final Set<String> knownEventIds = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
		knownEventIds.addAll(MapEventRegistry.listAvailableEventIds());

		if (knownEventIds.isEmpty()) {
			sandbox.privateText(admin, "Brak zarejestrowanych eventów mapowych.");
			return;
		}

		final Set<ConfiguredMapEvent> listedEvents = Collections.newSetFromMap(new IdentityHashMap<>());
		final StringBuilder builder = new StringBuilder("Dostępne eventy mapowe:");
		for (String eventId : knownEventIds) {
			final ConfiguredMapEvent event = MapEventRegistry.getEvent(eventId);
			if (event == null || listedEvents.contains(event)) {
				continue;
			}
			listedEvents.add(event);

			final String eventName = event.getEventDisplayName();
			final String eventType = event.getClass().getSimpleName();
			builder.append('\n')
				.append("- ")
				.append(eventId)
				.append(" | name=\"")
				.append(eventName)
				.append("\" | type=")
				.append(eventType);
		}

		if (listedEvents.isEmpty()) {
			sandbox.privateText(admin, "Nie udało się załadować eventów z rejestru.");
			return;
		}

		sendChunkedPrivateText(admin, builder.toString());
	}

	private void sendChunkedPrivateText(final Player admin, final String message) {
		if (message.length() <= MAX_PRIVATE_TEXT_LENGTH) {
			sandbox.privateText(admin, message);
			return;
		}

		final String[] lines = message.split("\\n");
		final StringBuilder chunk = new StringBuilder();
		for (String line : lines) {
			final int lineLength = line.length();
			if (lineLength > MAX_PRIVATE_TEXT_LENGTH) {
				if (chunk.length() > 0) {
					sandbox.privateText(admin, chunk.toString());
					chunk.setLength(0);
				}
				sendLongLineInChunks(admin, line);
				continue;
			}

			final int additionalLength = chunk.length() == 0 ? lineLength : lineLength + 1;
			if (chunk.length() + additionalLength > MAX_PRIVATE_TEXT_LENGTH) {
				sandbox.privateText(admin, chunk.toString());
				chunk.setLength(0);
			}

			if (chunk.length() > 0) {
				chunk.append('\n');
			}
			chunk.append(line);
		}

		if (chunk.length() > 0) {
			sandbox.privateText(admin, chunk.toString());
		}
	}

	private void sendLongLineInChunks(final Player admin, final String line) {
		for (int from = 0; from < line.length(); from += MAX_PRIVATE_TEXT_LENGTH) {
			final int to = Math.min(from + MAX_PRIVATE_TEXT_LENGTH, line.length());
			sandbox.privateText(admin, line.substring(from, to));
		}
	}

	private void usage(final Player admin) {
		sandbox.privateText(admin, "Użycie: /script StartMapEvent.class -list");
		sandbox.privateText(admin, "Użycie: /script StartMapEvent.class <eventId> [force|safe]");
	}
}
