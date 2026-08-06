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
package games.stendhal.server.actions.admin;

import java.util.Locale;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemCreationContext.Source;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import marauroa.common.game.RPAction;

/**
 * Validates item rarity options sent with administrator summon actions.
 */
final class ItemCreationCommandOptions {
	static final String RARITY = "rarity";
	static final String RANDOMIZE_MODIFIERS = "randomize-modifiers";
	static final String STAT_MULTIPLIER = "stat-multiplier";
	static final String ATTACK_MULTIPLIER = "attack-multiplier";
	static final String DEFENSE_MULTIPLIER = "defense-multiplier";
	static final String SPEED_MULTIPLIER = "speed-multiplier";
	static final String RANGE_MULTIPLIER = "range-multiplier";
	static final String VALUE_MULTIPLIER = "value-multiplier";

	private static final String[] MODIFIER_OPTIONS = {
		STAT_MULTIPLIER,
		ATTACK_MULTIPLIER,
		DEFENSE_MULTIPLIER,
		SPEED_MULTIPLIER,
		RANGE_MULTIPLIER,
		VALUE_MULTIPLIER
	};

	private ItemCreationCommandOptions() {
		// utility class
	}

	static boolean hasOptions(final RPAction action) {
		if (action.has(RARITY) || action.has(RANDOMIZE_MODIFIERS)) {
			return true;
		}
		for (final String option : MODIFIER_OPTIONS) {
			if (action.has(option)) {
				return true;
			}
		}
		return false;
	}

	static ItemCreationContext fromAction(final RPAction action) {
		final ItemCreationContext.Builder context = ItemCreationContext.builder(Source.ADMIN);
		if (action.has(RARITY)) {
			final String rawRarity = action.get(RARITY);
			if (rawRarity == null || rawRarity.trim().isEmpty()) {
				throw new IllegalArgumentException("Rarity nie może być puste.");
			}
			final String rarityId = rawRarity.trim().toLowerCase(Locale.ENGLISH);
			final ItemRarity rarity = ItemRarity.fromId(rarityId);
			if (rarity == null) {
				throw new IllegalArgumentException("Nieznana rzadkość przedmiotu: " + rarityId);
			}
			context.withRarity(rarity);
		}

		Boolean randomizeModifiers = null;
		if (action.has(RANDOMIZE_MODIFIERS)) {
			randomizeModifiers = parseBoolean(action.get(RANDOMIZE_MODIFIERS), RANDOMIZE_MODIFIERS);
		}

		final ItemRarityModifiers.Builder modifiers = ItemRarityModifiers.builder();
		boolean hasModifiers = false;
		for (final String option : MODIFIER_OPTIONS) {
			if (!action.has(option)) {
				continue;
			}
			hasModifiers = true;
			final double value = parseMultiplier(action.get(option), option);
			if (STAT_MULTIPLIER.equals(option)) {
				modifiers.statMultiplier(value);
			} else if (ATTACK_MULTIPLIER.equals(option)) {
				modifiers.attackMultiplier(value);
			} else if (DEFENSE_MULTIPLIER.equals(option)) {
				modifiers.defenseMultiplier(value);
			} else if (SPEED_MULTIPLIER.equals(option)) {
				modifiers.speedMultiplier(value);
			} else if (RANGE_MULTIPLIER.equals(option)) {
				modifiers.rangeMultiplier(value);
			} else if (VALUE_MULTIPLIER.equals(option)) {
				modifiers.valueMultiplier(value);
			}
		}

		if (hasModifiers) {
			if (!action.has(RARITY)) {
				throw new IllegalArgumentException("Stałe modyfikatory wymagają podania rarity.");
			}
			if (Boolean.TRUE.equals(randomizeModifiers)) {
				throw new IllegalArgumentException(
						"Nie można jednocześnie podać stałych modyfikatorów i randomize-modifiers=true.");
			}
			context.withModifiers(modifiers.build()).randomizeModifiers(false);
		} else if (randomizeModifiers != null) {
			context.randomizeModifiers(randomizeModifiers.booleanValue());
		}

		return context.build();
	}

	private static boolean parseBoolean(final String text, final String option) {
		if ("true".equalsIgnoreCase(text)) {
			return true;
		}
		if ("false".equalsIgnoreCase(text)) {
			return false;
		}
		throw new IllegalArgumentException(option + " musi mieć wartość true albo false.");
	}

	private static double parseMultiplier(final String text, final String option) {
		if (text == null || text.trim().isEmpty()) {
			throw new IllegalArgumentException("Brak wartości " + option + ".");
		}
		final double value;
		try {
			value = Double.parseDouble(text);
		} catch (final NumberFormatException e) {
			throw new IllegalArgumentException("Nieprawidłowa wartość " + option + ": " + text, e);
		}
		if (!Double.isFinite(value) || value <= 0.0) {
			throw new IllegalArgumentException(option + " musi być dodatnią, skończoną liczbą.");
		}
		return value;
	}
}
