/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.config;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Adds natural command synonyms to production activities declared in XML.
 *
 * The aliases are tied to the semantic activity already assigned to a
 * producer. This keeps, for example, brewing commands away from smiths and
 * sewing commands away from alchemists.
 */
final class ProductionActivityAliases {
	private ProductionActivityAliases() {
		// utility class
	}

	static List<String> expand(final List<String> configuredActivities) {
		final Set<String> result = new LinkedHashSet<String>();
		for (final String configured : configuredActivities) {
			if (configured == null) {
				continue;
			}
			final String activity = configured.trim();
			if (activity.isEmpty()) {
				continue;
			}
			result.add(activity);
			addAliases(result, activity.toLowerCase(Locale.ROOT));
		}
		return new ArrayList<String>(result);
	}

	private static void addAliases(final Set<String> result, final String activity) {
		switch (activity) {
			case "make":
				add(result, "wykonaj", "przygotuj");
				break;
			case "brew":
				add(result, "warz", "wywarz", "uwarz");
				break;
			case "mix":
				add(result, "mieszaj", "zmieszaj");
				break;
			case "concoct":
				add(result, "przygotuj", "przyrządź", "wymieszaj", "mieszaj");
				break;
			case "cast":
				add(result, "wytop", "przetop");
				break;
			case "mill":
				add(result, "miel");
				break;
			case "bake":
				add(result, "piecz");
				break;
			case "sew":
				add(result, "szyj");
				break;
			case "grind":
				add(result, "szlifuj");
				break;
			case "soak":
				add(result, "nasączaj");
				break;
			case "fill":
				add(result, "napełniaj");
				break;
			case "swap":
				add(result, "wymień");
				break;
			case "braid":
				add(result, "pleć");
				break;
			default:
				break;
		}
	}

	private static void add(final Set<String> result, final String... aliases) {
		for (final String alias : aliases) {
			result.add(alias);
		}
	}
}
