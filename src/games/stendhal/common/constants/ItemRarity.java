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
package games.stendhal.common.constants;

import java.util.Locale;

/**
 * Stable rarity identifiers shared by the server and the clients.
 */
public enum ItemRarity {
	COMMON("common", "#9e9e9e", "Zwykły", "Common"),
	RARE("rare", "#4a90e2", "Rzadki", "Rare"),
	EPIC("epic", "#9b59b6", "Epicki", "Epic"),
	LEGENDARY("legendary", "#ff8c00", "Legendarny", "Legendary");

	private final String id;
	private final String colorHex;
	private final String polishDisplayName;
	private final String englishDisplayName;

	ItemRarity(final String id, final String colorHex,
			final String polishDisplayName, final String englishDisplayName) {
		this.id = id;
		this.colorHex = colorHex;
		this.polishDisplayName = polishDisplayName;
		this.englishDisplayName = englishDisplayName;
	}

	/**
	 * Parses the persistent wire identifier.
	 *
	 * @param id identifier to parse
	 * @return parsed rarity, or {@code null} for null/unknown values
	 */
	public static ItemRarity fromId(final String id) {
		if (id != null) {
			final String normalized = id.trim().toLowerCase(Locale.ENGLISH);
			for (final ItemRarity rarity : values()) {
				if (rarity.id.equals(normalized)) {
					return rarity;
				}
			}
		}
		return null;
	}

	/**
	 * Parses a rarity, treating missing legacy data as common.
	 *
	 * @param id identifier to parse
	 * @return parsed rarity, or {@code COMMON} for null/unknown values
	 */
	public static ItemRarity fromIdOrCommon(final String id) {
		final ItemRarity rarity = fromId(id);
		return rarity == null ? COMMON : rarity;
	}

	public String getId() {
		return id;
	}

	public String getColorHex() {
		return colorHex;
	}

	public String getPolishDisplayName() {
		return polishDisplayName;
	}

	public String getEnglishDisplayName() {
		return englishDisplayName;
	}
}
