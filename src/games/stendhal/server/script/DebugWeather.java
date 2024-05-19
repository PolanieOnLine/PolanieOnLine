/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import games.stendhal.server.constants.StandardMessages;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.WeatherUpdater;
import games.stendhal.server.core.scripting.AbstractAdminScript;
import marauroa.common.Pair;

/**
 * For debugging weather server-side. Sets weather in zone where admin is located.
 */
public class DebugWeather extends AbstractAdminScript {
	@Override
	protected void run(final List<String> args) {
		checkNotNull(admin);
		final int argc = args.size();
		final Set<String> registered = Collections.unmodifiableSet(getTypes());
		if (registered.size() == 0) {
			sendError("Brak zarejestrowanych typów pogody.");
			return;
		}

		String requested = args.get(0).toLowerCase(Locale.ENGLISH);
		String disableCommand = "none";
		if (Arrays.asList(disableCommand, "stop", "disable").contains(requested)) {
			// weather should be deactivated for zone
			disableCommand = requested;
			requested = null;
		} else if (!registered.contains(requested)) {
			sendError("Nieznany typ pogody \"" + requested + "\". Dostępne: "
					+ String.join(", ", registered));
			return;
		}
		if (requested == null && argc > 1) {
			sendError("\"" + disableCommand + "\" nie może występować z innymi parametrami.");
			showUsage();
			return;
		}
		boolean thunder = false;
		if (argc > 1) {
			final String p2 = args.get(1);
			if (!"thunder".equals(p2.toLowerCase(Locale.ENGLISH))) {
				StandardMessages.unknownParameter(admin, p2);
				showUsage();
				return;
			}
			thunder = true;
		}

		final Pair<String, Boolean> description = new Pair<>(requested, thunder);

		// get zone & check weather entity where admin is located
		final StendhalRPZone zone = admin.getZone();
		if (zone == null) {
			sendError("Nie można określić strefy administratora " + admin.getName() + ".");
			return;
		}
		if (!checkWeatherEntity(zone)) {
			return;
		}

		final WeatherUpdater manager = WeatherUpdater.get();
		manager.updateAndNotify(zone, description);

		sendInfo((requested != null ? "Włączono " + requested : "Wyłączono pogodę") + " w strefie "
				+ zone.getName() + (thunder ? " wraz z burzą" : "") + ".");
	}

	/**
	 * Gets available weather parameter options.
	 *
	 * @return
	 *   Weather types.
	 */
	private Set<String> getTypes() {
		final Set<String> registered = WeatherUpdater.get().getTypes();
		// remove thunder as it is an optional additional parameter
		registered.remove("thunder");
		return registered;
	}

	/**
	 * Checks if zone can handle weather.
	 *
	 * @param zone
	 *   Zone to be checked.
	 * @return
	 *   {@code true} if zone has a weather managing entity.
	 */
	private boolean checkWeatherEntity(final StendhalRPZone zone) {
		return zone.getWeatherEntity() != null;
	}

	@Override
	public int getMinParams() {
		return 1;
	}

	@Override
	public int getMaxParams() {
		return 2;
	}

	@Override
	protected List<String> getParamStrings() {
		return Arrays.asList(
			"none",
			"<weather> [thunder]"
		);
	}

	@Override
	protected List<String> getParamDetails() {
		return Arrays.asList(
			"none: Wyłącza aktualną pogodę.",
			"weather: Typ pogody (t.j. \"rain\", \"snow\", \"snow_heavy\").",
			"thunder: Włącza efekt dźwiękowy burzy."
		);
	}

	@Override
	public String getUsage() {
		String usage = super.getUsage();
		usage += "\nDostępne typy pogody:\n&nbsp;&nbsp;";
		final Set<String> registered = getTypes();
		if (registered.size() > 0) {
			usage += String.join(", ", registered);
		} else {
			usage += "żadne dostępne";
		}
		return usage;
	}
}