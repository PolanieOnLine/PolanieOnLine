/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common.constants;

import java.awt.Color;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.Rand;

/**
 * Enumeration describing item rarities together with attribute modifiers and
 * colours used by the client.
 */
public enum ItemRarity {

/** Common items are slightly weaker than the XML baseline. */
COMMON("common", "Zwykły", new Color(0x5b, 0x5b, 0x5b), 0.97, 1.03, 0.92, 70, 1.0),

/** Rare items are a touch stronger than the XML baseline while remaining close to it. */
RARE("rare", "Rzadki", new Color(0x1e, 0x90, 0xff), 1.02, 0.99, 1.05, 22, 0.6),

/** Epic items provide a noticeable improvement over the XML baseline. */
EPIC("epic", "Epicki", new Color(0x99, 0x32, 0xcc), 1.07, 0.96, 1.12, 6, 0.35),

/** Legendary items are substantially better than the XML baseline without being overpowering. */
LEGENDARY("legendary", "Legendarny", new Color(0xff, 0xa5, 0x00), 1.12, 0.93, 1.25, 2, 0.2);

	private static final Map<String, ItemRarity> BY_ID = new HashMap<String, ItemRarity>();

	private static final Set<String> POSITIVE_INT_ATTRIBUTES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList(
					"atk",
					"ratk",
					"def",
					"health",
					"range",
					"skill_atk",
					"rate_increase")));

	private static final Set<String> POSITIVE_FLOAT_ATTRIBUTES = Collections.unmodifiableSet(
			new HashSet<String>(Arrays.asList(
					"accuracy_bonus",
					"atk_additional_bonus",
					"critical_additional_bonus",
					"def_additional_bonus",
					"lifesteal",
					"critical_chance",
					"lifesteal_increase")));

	static {
		for (ItemRarity rarity : ItemRarity.values()) {
			BY_ID.put(rarity.id, rarity);
		}
	}

	private final String id;
	private final String displayName;
	private final Color color;
	private final double positiveModifier;
	private final double rateModifier;
	private final double valueModifier;
	private final double weight;

	private final double dropRateModifier;

	ItemRarity(String id, String displayName, Color color, double positiveModifier,
			double rateModifier, double valueModifier, double weight, double dropRateModifier) {
		this.id = id;
		this.displayName = displayName;
		this.color = color;
		this.positiveModifier = positiveModifier;
		this.rateModifier = rateModifier;
		this.valueModifier = valueModifier;
		this.weight = weight;
		this.dropRateModifier = dropRateModifier;
	}

	/**
	 * Identifier stored in the RPObject. Lowercase to simplify network payload.
	 *
	 * @return rarity identifier
	 */
	public String getId() {
		return id;
	}

	/**
	 * Human readable rarity name.
	 *
	 * @return display name
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Border colour used on the client.
	 *
	 * @return colour representing the rarity
	 */
	public Color getColor() {
		return color;
	}

	/**
	 * Multiplier used for attributes where a larger value is beneficial.
	 *
	 * @return multiplier for positive attributes
	 */
	public double getPositiveModifier() {
		return positiveModifier;
	}

	/**
	 * Multiplier applied to the weapon speed attribute. Values above one slow
	 * the weapon down, values below one make it faster.
	 *
	 * @return rate modifier
	 */
	public double getRateModifier() {
		return rateModifier;
	}

	/**
	 * Multiplier applied to the item gold value.
	 *
	 * @return value modifier
	 */
	public double getValueModifier() {
		return valueModifier;
	}

	/**
	 * Weight used when rolling a random rarity.
	 *
	 * @return random weight
	 */
	public double getWeight() {
		return weight;
	}

	/**
	 * Modifier applied to a creature's base drop probability when rolling this rarity.
	 *
	 * @return drop rate modifier for creature loot
	 */
	public double getDropRateModifier() {
		return dropRateModifier;
	}

	/**
	 * Returns rarity by identifier.
	 *
	 * @param id rarity id
	 * @return matching rarity, or {@link #COMMON} if no match was found
	 */
	public static ItemRarity byId(String id) {
		if (id == null) {
			return COMMON;
		}

		ItemRarity rarity = BY_ID.get(id.toLowerCase());
		if (rarity != null) {
			return rarity;
		}

		return COMMON;
	}

	/**
	 * Rolls a random rarity according to the configured weight distribution.
	 *
	 * @return random rarity
	 */
	public static ItemRarity rollRandom() {
		double totalWeight = 0.0;
		for (ItemRarity rarity : ItemRarity.values()) {
			totalWeight += rarity.weight;
		}

		double roll = Rand.rand() * totalWeight;

		for (ItemRarity rarity : ItemRarity.values()) {
			roll -= rarity.weight;
			if (roll <= 0) {
				return rarity;
			}
		}

		return LEGENDARY;
	}

	/**
	 * Applies rarity modifiers to a copy of the provided base attribute map.
	 * Only recognised attributes are adjusted. Unrecognised attributes are left
	 * untouched.
	 *
	 * @param baseAttributes base attribute map from XML
	 * @return map containing the adjusted attributes
	 */
	public Map<String, String> applyToAttributes(Map<String, String> baseAttributes) {
		if (baseAttributes == null) {
			return null;
		}

		Map<String, String> adjusted = new LinkedHashMap<String, String>(baseAttributes.size());

		for (Map.Entry<String, String> entry : baseAttributes.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();

			if (value == null) {
				adjusted.put(key, null);
				continue;
			}

			if (POSITIVE_INT_ATTRIBUTES.contains(key)) {
				adjusted.put(key, String.valueOf(applyPositiveInt(value)));
			} else if (POSITIVE_FLOAT_ATTRIBUTES.contains(key)) {
				adjusted.put(key, String.valueOf(applyPositiveFloat(value)));
			} else if ("rate".equals(key)) {
				adjusted.put(key, String.valueOf(applyRate(value)));
			} else {
				adjusted.put(key, value);
			}
		}

		return adjusted;
	}

	private int applyPositiveInt(String value) {
		try {
			int base = Integer.parseInt(value);
			int modified = (int) Math.round(base * positiveModifier);
			return Math.max(0, modified);
		} catch (NumberFormatException ex) {
			return Integer.parseInt(value);
		}
	}

	private float applyPositiveFloat(String value) {
		try {
			float base = Float.parseFloat(value);
			return (float) (base * positiveModifier);
		} catch (NumberFormatException ex) {
			return Float.parseFloat(value);
		}
	}

	private int applyRate(String value) {
		try {
			int base = Integer.parseInt(value);
			int modified = (int) Math.round(base * rateModifier);
			return Math.max(1, modified);
		} catch (NumberFormatException ex) {
			return Integer.parseInt(value);
		}
	}

	/**
	 * Adjusts base gold value according to rarity.
	 *
	 * @param baseValue base value from XML
	 * @return adjusted value
	 */
	public int applyValueModifier(int baseValue) {
		int modified = (int) Math.round(baseValue * valueModifier);
		return Math.max(0, modified);
	}
}

