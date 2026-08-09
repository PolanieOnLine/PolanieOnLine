/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;

/** Builds the dedicated Polish presentation of materialized item affixes. */
final class LegendaryAffixPresentation {
	private static final String LEGENDARY_TEXT_COLOR = "#f28c28";
	private static final String LEGENDARY_NUMBER_COLOR = "#f3e2b8";

	private LegendaryAffixPresentation() {
		// utility class
	}

	static String build(final RPObject object) {
		if (object == null || !object.hasMap(ItemTooltip.ATTRIBUTE)) {
			return "";
		}

		final StringBuilder result = new StringBuilder();
		appendRegularRolled(result, object, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS,
				"dodatkowego ataku");
		appendRegularRolled(result, object, ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS,
				"dodatkowego pancerza");

		if (has(object, ItemTooltip.LEGENDARY_DEEP_WOUNDS)) {
			appendFixed(result, "Głębokie Rany", new String[] {
					"", "15%", " szansy na krwawienie; rana zadaje ", "35%",
					" obrażeń trafienia."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_ARMOR_BREAKER)) {
			appendFixed(result, "Łamacz Pancerzy", new String[] {
					"Redukuje ", "40%",
					" niekorzystnej kary wynikającej z pancerza celu."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_LONGSHOT)) {
			appendFixed(result, "Dalekosiężność", new String[] {
					"Ataki wykonane z dystansu zadają ", "+25%", " obrażeń."
			});
		}
		if (has(object, ItemTooltip.LEGENDARY_EXECUTIONER)) {
			appendFixed(result, "Egzekutor", new String[] {
					"Przeciw celom poniżej ", "20%", " PW zadajesz ", "+35%",
					" obrażeń."
			});
		}
		appendRolled(result, object, ItemTooltip.LEGENDARY_BASTION_BONUS,
				"Niezłomny Bastion", "+", " pkt. dodatkowego pancerza.");
		appendRolled(result, object, ItemTooltip.LEGENDARY_RELIC_POWER,
				"Relikt Mocy", "+", " pkt. dodatkowego ataku.");
		return result.toString();
	}

	private static boolean has(final RPObject object, final String key) {
		return WeaponPerformanceCalculator.getTooltipValue(object, key) != null;
	}

	private static void appendRegularRolled(final StringBuilder result,
			final RPObject object, final String key, final String label) {
		final String raw = WeaponPerformanceCalculator.getTooltipValue(object, key);
		if (raw == null) {
			return;
		}
		final Integer value = parseInteger(raw);
		if (value == null || value.intValue() == 0) {
			return;
		}
		result.append("<div style='margin-top:4px'><font size='-1'>&#9670; ")
				.append(value.intValue() > 0 ? "+" : "")
				.append(value.intValue()).append(" ").append(label)
				.append("</font></div>");
	}

	/**
	 * Parts alternate orange text and cream numeric fragments, beginning with
	 * orange text. Empty first text is allowed when the line starts with a number.
	 */
	private static void appendFixed(final StringBuilder result,
			final String title, final String[] parts) {
		beginLine(result, title);
		for (int index = 0; index < parts.length; index++) {
			appendColored(result, parts[index], index % 2 == 0
					? LEGENDARY_TEXT_COLOR : LEGENDARY_NUMBER_COLOR);
		}
		endLine(result);
	}

	private static void appendRolled(final StringBuilder result,
			final RPObject object, final String key, final String title,
			final String prefix, final String suffix) {
		final String raw = WeaponPerformanceCalculator.getTooltipValue(object, key);
		if (raw == null) {
			return;
		}
		final Integer value = parseInteger(raw);
		if (value == null) {
			return;
		}
		beginLine(result, title);
		appendColored(result, prefix + value, LEGENDARY_NUMBER_COLOR);
		appendColored(result, suffix, LEGENDARY_TEXT_COLOR);
		endLine(result);
	}

	private static Integer parseInteger(final String value) {
		try {
			return Integer.valueOf(value);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	private static void beginLine(final StringBuilder result, final String title) {
		result.append("<div style='margin-top:4px'><b><font size='-1' color='")
				.append(LEGENDARY_TEXT_COLOR).append("'>")
				.append(title).append(": </font>");
	}

	private static void appendColored(final StringBuilder result,
			final String text, final String color) {
		if (text == null || text.length() == 0) {
			return;
		}
		result.append("<font size='-1' color='").append(color).append("'>")
				.append(text).append("</font>");
	}

	private static void endLine(final StringBuilder result) {
		result.append("</b></div>");
	}
}
