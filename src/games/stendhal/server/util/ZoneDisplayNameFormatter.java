/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import games.stendhal.server.core.engine.StendhalRPZone;

/**
 * Creates player-facing names for zones without exposing internal map ids.
 */
public final class ZoneDisplayNameFormatter {
	private static final Pattern ZONE_PREFIX = Pattern.compile("^(int|alt_-?\\d+|-?\\d+)_(.+)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern INTEGER = Pattern.compile("^\\d+$");
	private static final Pattern SECTOR = Pattern.compile("^[nsewc](?:[0-9]*[nsewc])*[0-9]*$", Pattern.CASE_INSENSITIVE);

	private ZoneDisplayNameFormatter() {
		// utility class
	}

	/**
	 * Returns a stable, readable location name for a zone.
	 *
	 * @param zone zone to format
	 * @return player-facing zone name
	 */
	public static String format(final StendhalRPZone zone) {
		if (zone == null) {
			return "Nieznana lokacja";
		}

		if (zone.getName().startsWith("instance_")) {
			if (zone.getAttributes() != null) {
				final String readableName = zone.getAttributes().get("readable_name");
				if (readableName != null && !readableName.trim().isEmpty()
						&& !normalizeName(zone.getName()).equals(normalizeName(readableName))) {
					return readableName.trim();
				}
			}
			return "Prywatna lokacja";
		}

		return formatTechnicalName(zone.getName());
	}

	static String formatTechnicalName(final String zoneName) {
		if (zoneName == null || zoneName.trim().isEmpty()) {
			return "Nieznana lokacja";
		}
		if ("int_tarnow_challenge_arena".equalsIgnoreCase(zoneName.trim())) {
			return "Arena Wyzwań";
		}

		String level = null;
		String remainder = zoneName.trim();
		final Matcher prefixMatcher = ZONE_PREFIX.matcher(remainder);
		if (prefixMatcher.matches()) {
			level = prefixMatcher.group(1).toLowerCase(Locale.ROOT);
			remainder = prefixMatcher.group(2);
		}

		final List<String> tokens = new ArrayList<String>();
		for (final String token : remainder.split("_")) {
			if (!token.isEmpty()) {
				tokens.add(token);
			}
		}

		String interiorFloor = null;
		if ("int".equals(level) && !tokens.isEmpty()) {
			final String last = tokens.get(tokens.size() - 1);
			if (INTEGER.matcher(last).matches()) {
				interiorFloor = last;
				tokens.remove(tokens.size() - 1);
			}
		}

		final List<String> baseTokens = new ArrayList<String>();
		final StringBuilder sector = new StringBuilder();
		for (final String token : tokens) {
			if (isSectorToken(token)) {
				sector.append(token.toUpperCase(Locale.ROOT));
			} else {
				baseTokens.add(token);
			}
		}

		String displayName = formatBaseName(baseTokens);
		if (displayName.isEmpty()) {
			displayName = titleize(remainder);
		}
		if (sector.length() > 0) {
			displayName += " " + sector.toString();
		}

		if (interiorFloor != null && !"0".equals(interiorFloor)) {
			displayName += " Poziom " + interiorFloor;
		} else if (level != null && level.matches("-?\\d+") && !"0".equals(level)) {
			final int numericLevel = Integer.parseInt(level);
			if (numericLevel < 0) {
				displayName += ", poziom " + numericLevel;
			} else {
				displayName += " Poziom " + numericLevel;
			}
		} else if (level != null && level.startsWith("alt_")) {
			displayName += " Świat alternatywny";
		}

		return displayName.trim();
	}

	private static boolean isSectorToken(final String token) {
		if (!SECTOR.matcher(token).matches()) {
			return false;
		}

		boolean containsDigit = false;
		for (int i = 0; i < token.length(); i++) {
			if (Character.isDigit(token.charAt(i))) {
				containsDigit = true;
				break;
			}
		}
		return containsDigit || token.length() <= 3;
	}

	private static String formatBaseName(final List<String> tokens) {
		if (tokens.isEmpty()) {
			return "";
		}

		final String name = String.join("_", tokens).toLowerCase(Locale.ROOT);
		if ("dragon_land".equals(name)) {
			return "Smocza Kraina";
		}
		if (name.startsWith("dragon_knights_")) {
			final String type = translateLocationType(name.substring("dragon_knights_".length()));
			if (type != null) {
				return type + " Smoczych Rycerzy";
			}
		} else if (name.startsWith("dragon_")) {
			final String type = translateLocationType(name.substring("dragon_".length()));
			if (type != null) {
				return type + " Smoczej Krainy";
			}
		}

		if (tokens.size() >= 2) {
			final String type = translateLocationType(String.join("_", tokens.subList(1, tokens.size())));
			if (type != null) {
				return type + " " + translatePlaceName(tokens.get(0));
			}
		}

		final List<String> translated = new ArrayList<String>();
		for (final String token : tokens) {
			final String type = translateLocationType(token);
			translated.add(type != null ? type : translatePlaceName(token));
		}
		return String.join(" ", translated);
	}

	private static String translatePlaceName(final String token) {
		switch (token.toLowerCase(Locale.ROOT)) {
			case "gdansk":
				return "Gdańsk";
			case "krakow":
				return "Kraków";
			case "koscielisko":
				return "Kościelisko";
			default:
				return titleize(token);
		}
	}

	private static String translateLocationType(final String token) {
		final String value = token.toLowerCase(Locale.ROOT);
		switch (value) {
			case "plain":
			case "plains":
				return "Równiny";
			case "mountain":
			case "mountains":
				return "Góry";
			case "forest":
			case "woods":
				return "Las";
			case "swamp":
			case "swamps":
				return "Bagna";
			case "bank":
				return "Bank";
			case "tavern":
				return "Tawerna";
			case "inn":
				return "Karczma";
			case "hospital":
				return "Szpital";
			case "church":
				return "Kościół";
			case "temple":
				return "Świątynia";
			case "mine":
			case "mines":
			case "kopalnia":
				return "Kopalnia";
			case "salt_mines":
				return "Kopalnia soli";
			case "coalmine":
				return "Kopalnia węgla";
			case "dwarf_mine":
				return "Kopalnia krasnoludów";
			case "cave":
				return "Jaskinia";
			case "caves":
				return "Jaskinie";
			case "great_cave":
				return "Wielka jaskinia";
			case "lost_caves":
				return "Zaginione jaskinie";
			case "mountain_cave":
				return "Górska jaskinia";
			case "yeti_cave":
				return "Jaskinia yeti";
			case "dungeon":
				return "Loch";
			case "dungeons":
				return "Lochy";
			case "little_dungeon":
				return "Mały loch";
			case "castle_dungeons":
				return "Lochy zamku";
			case "catacombs":
				return "Katakumby";
			case "underground":
				return "Podziemia";
			case "underground_forest":
				return "Podziemny las";
			case "canals":
			case "sewers":
				return "Kanały";
			case "tunnel":
				return "Tunel";
			case "drows_tunnel":
				return "Tunel drowów";
			case "vault":
				return "Krypta";
			case "vaults":
				return "Krypty";
			case "jail":
			case "prison":
				return "Więzienie";
			case "sea_floor":
				return "Dno morza";
			case "lich_palace":
				return "Pałac licza";
			case "castle":
				return "Zamek";
			case "abandoned_keep":
				return "Opuszczona warownia";
			case "armory":
				return "Zbrojownia";
			case "bar":
				return "Tawerna";
			case "barrack":
				return "Koszary";
			case "chappel":
			case "chapel":
				return "Kaplica";
			case "courtyard":
				return "Dziedziniec";
			case "guard_room":
				return "Wartownia";
			case "main_hall":
				return "Sala główna";
			case "meeting_room":
				return "Sala narad";
			case "room":
				return "Komnata";
			case "castle_room":
				return "Komnata zamku";
			case "springs":
				return "Źródła";
			case "treasury":
				return "Skarbiec";
			case "warehouse":
				return "Magazyn";
			case "workshop":
				return "Warsztat";
			case "wizard_tower":
				return "Wieża czarodzieja";
			case "wizard_tower_room":
				return "Komnata wieży czarodzieja";
			case "house":
				return "Dom";
			case "school":
				return "Szkoła";
			case "library":
				return "Biblioteka";
			case "market":
				return "Targ";
			case "arena":
				return "Arena";
			case "farm":
				return "Gospodarstwo";
			case "city":
				return "Miasto";
			case "village":
				return "Wioska";
			case "canyon":
				return "Kanion";
			case "island":
				return "Wyspa";
			case "river":
				return "Rzeka";
			case "lake":
				return "Jezioro";
			case "road":
				return "Droga";
			case "valley":
				return "Dolina";
			case "desert":
				return "Pustynia";
			case "beach":
				return "Plaża";
			case "harbour":
			case "harbor":
				return "Port";
			default:
				return null;
		}
	}

	private static String normalizeName(final String value) {
		return value.replace('_', ' ').trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
	}

	private static String titleize(final String value) {
		final StringBuilder result = new StringBuilder();
		for (final String word : value.replace('-', ' ').split("[ _]+")) {
			if (word.isEmpty()) {
				continue;
			}
			if (result.length() > 0) {
				result.append(' ');
			}
			result.append(Character.toUpperCase(word.charAt(0)));
			if (word.length() > 1) {
				result.append(word.substring(1));
			}
		}
		return result.toString();
	}
}
