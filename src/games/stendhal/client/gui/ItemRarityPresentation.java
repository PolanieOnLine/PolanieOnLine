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
package games.stendhal.client.gui;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Item;
import games.stendhal.client.gui.WeaponPerformanceCalculator.WeaponPerformance;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPObject;

/** Builds structured desktop item tooltips without graphics dependencies. */
final class ItemRarityPresentation {
	private static final String DEFAULT_TITLE_COLOR = "#ffffff";
	private static final String PRIMARY_VALUE_COLOR = "#f2f2f2";
	private static final String MUTED_COLOR = "#b8b8b8";
	private static final String BONUS_COLOR = "#6f9cff";
	private static final DecimalFormat ONE_DECIMAL = createDecimalFormat("0.0");
	private static final DecimalFormat TWO_DECIMALS = createDecimalFormat("0.00");

	private ItemRarityPresentation() {
		// utility class
	}

	static String buildItemToolTip(final IEntity entity) {
		if (entity == null) {
			return null;
		}

		final String scrollDestination = getScrollDestination(entity);
		if (!(entity instanceof Item)) {
			return scrollDestination;
		}

		final Item item = (Item) entity;
		final RPObject object = item.getRPObject();
		final ItemRarity rarity = item.getRarity();
		final WeaponPerformance performance =
				WeaponPerformanceCalculator.calculate(object);

		if (rarity == null && performance == null) {
			return scrollDestination;
		}

		final StringBuilder tooltip = new StringBuilder("<html>");
		tooltip.append("<div style='width:260px;padding:4px'>");
		appendHeader(tooltip, entity, rarity);

		if (performance != null) {
			appendWeaponPerformance(tooltip, object, performance);
		}
		appendBonuses(tooltip, object);
		appendFooter(tooltip, object, scrollDestination);

		tooltip.append("</div></html>");
		return tooltip.toString();
	}

	private static void appendHeader(final StringBuilder tooltip,
			final IEntity entity, final ItemRarity rarity) {
		final String title = entity.getTitle();
		if (title != null) {
			tooltip.append("<div style='text-align:center'><b><font color='");
			tooltip.append(escapeHtml(rarity == null
					? DEFAULT_TITLE_COLOR : rarity.getColorHex()));
			tooltip.append("'>");
			tooltip.append(escapeHtml(title.toUpperCase(Locale.ROOT)));
			tooltip.append("</font></b></div>");
		}

		if (rarity != null) {
			tooltip.append("<div style='text-align:center'><font color='");
			tooltip.append(escapeHtml(rarity.getColorHex()));
			tooltip.append("'>");
			tooltip.append(escapeHtml(rarity.getPolishDisplayName()));
			tooltip.append("</font></div>");
		}
	}

	private static void appendWeaponPerformance(final StringBuilder tooltip,
			final RPObject object, final WeaponPerformance performance) {
		tooltip.append("<hr>");
		tooltip.append("<div style='text-align:center'><font size='+2' color='");
		tooltip.append(PRIMARY_VALUE_COLOR);
		tooltip.append("'><b>");
		tooltip.append(formatOneDecimal(performance.getBaseDps()));
		tooltip.append("</b></font><br><font color='");
		tooltip.append(MUTED_COLOR);
		tooltip.append("'>Bazowy DPS</font></div><br>");

		tooltip.append("<b>");
		tooltip.append(performance.getAttackPoints());
		tooltip.append(performance.isRanged()
				? " pkt. ataku dystansowego" : " pkt. ataku");
		tooltip.append("</b><br>");
		tooltip.append(formatTwoDecimals(performance.getAttacksPerSecond()));
		tooltip.append(" ataku na sekundę<br>");
		tooltip.append(formatTwoDecimals(performance.getAttackIntervalSeconds()));
		tooltip.append(" s między atakami");

		if (object.has("range") && object.getInt("range") > 0) {
			tooltip.append("<br>Zasięg: ");
			tooltip.append(object.getInt("range"));
		}
	}

	private static void appendBonuses(final StringBuilder tooltip,
			final RPObject object) {
		final StringBuilder bonuses = new StringBuilder();
		appendPercentageBonus(bonuses, object, "atk_additional_bonus",
				"bonus ataku");
		appendPercentageBonus(bonuses, object, "accuracy_bonus",
				"bonus precyzji");
		appendPercentageBonus(bonuses, object, "critical_chance",
				"szansy na trafienie krytyczne");
		appendPercentageBonus(bonuses, object, "critical_additional_bonus",
				"obrażeń krytycznych");
		appendPercentageBonus(bonuses, object, "lifesteal",
				"kradzieży życia");
		appendPercentageBonus(bonuses, object, "lifesteal_increase",
				"zwiększonej kradzieży życia");
		appendIntegerBonus(bonuses, object, "health", "zdrowia");
		appendIntegerBonus(bonuses, object, "def", "obrony");

		if (bonuses.length() > 0) {
			tooltip.append("<hr><font color='");
			tooltip.append(BONUS_COLOR);
			tooltip.append("'>");
			tooltip.append(bonuses);
			tooltip.append("</font>");
		}
	}

	private static void appendPercentageBonus(final StringBuilder bonuses,
			final RPObject object, final String attribute, final String label) {
		if (!object.has(attribute)) {
			return;
		}
		final double value = object.getDouble(attribute);
		if (value == 0.0) {
			return;
		}
		appendBonusLine(bonuses, signed(formatCompact(value)) + "% " + label);
	}

	private static void appendIntegerBonus(final StringBuilder bonuses,
			final RPObject object, final String attribute, final String label) {
		if (!object.has(attribute)) {
			return;
		}
		final int value = object.getInt(attribute);
		if (value == 0) {
			return;
		}
		appendBonusLine(bonuses, signed(Integer.toString(value)) + " " + label);
	}

	private static void appendBonusLine(final StringBuilder bonuses,
			final String line) {
		if (bonuses.length() > 0) {
			bonuses.append("<br>");
		}
		bonuses.append("&#9670; ");
		bonuses.append(escapeHtml(line));
	}

	private static void appendFooter(final StringBuilder tooltip,
			final RPObject object, final String scrollDestination) {
		final StringBuilder footer = new StringBuilder();
		if (object.has("min_level") && object.getInt("min_level") > 0) {
			footer.append("Wymagany poziom: ");
			footer.append(object.getInt("min_level"));
		}
		if (object.has("value") && object.getInt("value") > 0) {
			appendFooterSeparator(footer);
			footer.append("Wartość: ");
			footer.append(object.getInt("value"));
		}
		if (object.has("durability")) {
			appendFooterSeparator(footer);
			footer.append("Wytrzymałość: ");
			if (object.has("uses")) {
				footer.append(Math.max(0,
						object.getInt("durability") - object.getInt("uses")));
				footer.append("/");
			}
			footer.append(object.getInt("durability"));
		}
		if (scrollDestination != null) {
			appendFooterSeparator(footer);
			footer.append(escapeHtml(scrollDestination));
		}

		if (footer.length() > 0) {
			tooltip.append("<hr><font color='");
			tooltip.append(MUTED_COLOR);
			tooltip.append("'>");
			tooltip.append(footer);
			tooltip.append("</font>");
		}
	}

	private static void appendFooterSeparator(final StringBuilder footer) {
		if (footer.length() > 0) {
			footer.append("<br>");
		}
	}

	private static String getScrollDestination(final IEntity entity) {
		final RPObject object = entity.getRPObject();
		if ("scroll".equals(entity.getEntityClass()) && object.has("dest")) {
			return object.get("dest").replaceFirst(",", " ");
		}
		return null;
	}

	private static String signed(final String value) {
		return value.startsWith("-") ? value : "+" + value;
	}

	private static String formatCompact(final double value) {
		if (Math.rint(value) == value) {
			return Integer.toString((int) value);
		}
		return formatOneDecimal(value);
	}

	private static synchronized String formatOneDecimal(final double value) {
		return ONE_DECIMAL.format(value);
	}

	private static synchronized String formatTwoDecimals(final double value) {
		return TWO_DECIMALS.format(value);
	}

	private static DecimalFormat createDecimalFormat(final String pattern) {
		final DecimalFormatSymbols symbols =
				new DecimalFormatSymbols(new Locale("pl", "PL"));
		return new DecimalFormat(pattern, symbols);
	}

	private static String escapeHtml(final String text) {
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;")
				.replace("<", "&lt;")
				.replace(">", "&gt;")
				.replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
