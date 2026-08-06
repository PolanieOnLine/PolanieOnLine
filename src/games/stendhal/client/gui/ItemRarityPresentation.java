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
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;

/** Builds structured desktop item tooltips without graphics dependencies. */
final class ItemRarityPresentation {
	private static final String DEFAULT_TITLE_COLOR = "#ffffff";
	private static final String PRIMARY_VALUE_COLOR = "#ffffff";
	private static final String MUTED_COLOR = "#d8d8d8";
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
		final boolean hasStructuredStats = object.hasMap(ItemTooltip.ATTRIBUTE);

		if (rarity == null && performance == null && !hasStructuredStats) {
			return scrollDestination;
		}

		final StringBuilder tooltip = new StringBuilder("<html>");
		tooltip.append("<div style='width:230px;padding:3px'>");
		appendHeader(tooltip, entity, rarity);
		if (performance != null) {
			appendWeaponPerformance(tooltip, object, performance);
		}
		appendCoreStats(tooltip, object);
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
		tooltip.append("<div style='text-align:center'><font size='+1' color='");
		tooltip.append(PRIMARY_VALUE_COLOR);
		tooltip.append("'><b>");
		tooltip.append(formatOneDecimal(performance.getBaseDps()));
		tooltip.append(" DPS</b></font><br><font color='");
		tooltip.append(MUTED_COLOR);
		tooltip.append("'><b>");
		tooltip.append(performance.getDamageMin()).append("–")
				.append(performance.getDamageMax());
		tooltip.append(" obrażeń</b> &nbsp; ");
		tooltip.append(formatTwoDecimals(performance.getAttacksPerSecond()));
		tooltip.append(" ataku/s</font></div>");

		final int range = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.RANGE);
		if (range > 0) {
			tooltip.append("<br>Zasięg: ").append(range);
		}
		final String damageType = WeaponPerformanceCalculator.getTooltipValue(
				object, ItemTooltip.DAMAGE_TYPE);
		if (damageType != null) {
			tooltip.append("<br>Typ obrażeń: ");
			tooltip.append(escapeHtml(localizeDamageType(damageType)));
		}
		final String statuses = WeaponPerformanceCalculator.getTooltipValue(
				object, ItemTooltip.STATUS_ATTACK);
		if (statuses != null && !statuses.isEmpty()) {
			tooltip.append("<br>Efekty trafienia: ");
			tooltip.append(escapeHtml(statuses.replace(';', ',')));
		}
	}

	private static void appendCoreStats(final StringBuilder tooltip,
			final RPObject object) {
		final StringBuilder stats = new StringBuilder();
		appendPlainStat(stats, "Obrona", WeaponPerformanceCalculator.getInt(
				object, ItemTooltip.DEFENSE));
		appendPlainStat(stats, "Siła ataku", WeaponPerformanceCalculator.getInt(
				object, ItemTooltip.SKILL_ATTACK));
		final int improve = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.IMPROVE);
		final int maxImproves = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.MAX_IMPROVES);
		if (maxImproves > 0 || improve > 0) {
			appendLine(stats, "Ulepszenie: +" + improve
					+ (maxImproves > 0 ? "/" + maxImproves : ""));
		}
		if (stats.length() > 0) {
			tooltip.append("<hr>").append(stats);
		}
	}

	private static void appendBonuses(final StringBuilder tooltip,
			final RPObject object) {
		final StringBuilder bonuses = new StringBuilder();
		appendPercentageBonus(bonuses, object, ItemTooltip.ATTACK_BONUS,
				"bonusu ataku", false);
		appendPercentageBonus(bonuses, object, ItemTooltip.ACCURACY_BONUS,
				"bonusu precyzji", false);
		appendPercentageBonus(bonuses, object, ItemTooltip.CRITICAL_CHANCE,
				"szansy na trafienie krytyczne", false);
		appendPercentageBonus(bonuses, object, ItemTooltip.CRITICAL_BONUS,
				"obrażeń krytycznych", false);
		appendPercentageBonus(bonuses, object, ItemTooltip.LIFESTEAL,
				"kradzieży życia", true);
		appendPercentageBonus(bonuses, object, ItemTooltip.LIFESTEAL_INCREASE,
				"zwiększonej kradzieży życia", false);
		appendIntegerBonus(bonuses, object, ItemTooltip.HEALTH, "zdrowia");
		appendPercentageBonus(bonuses, object, ItemTooltip.DEFENSE_BONUS,
				"bonusu obrony", false);

		if (bonuses.length() > 0) {
			tooltip.append("<hr><font color='").append(BONUS_COLOR)
					.append("'>").append(bonuses).append("</font>");
		}
	}

	private static void appendPercentageBonus(final StringBuilder bonuses,
			final RPObject object, final String attribute, final String label,
			final boolean fraction) {
		double value = WeaponPerformanceCalculator.getDouble(object, attribute);
		if (value == 0.0) {
			return;
		}
		if (fraction && Math.abs(value) <= 1.0) {
			value *= 100.0;
		}
		appendBonusLine(bonuses, signed(formatCompact(value)) + "% " + label);
	}

	private static void appendIntegerBonus(final StringBuilder bonuses,
			final RPObject object, final String attribute, final String label) {
		final int value = WeaponPerformanceCalculator.getInt(object, attribute);
		if (value != 0) {
			appendBonusLine(bonuses, signed(Integer.toString(value)) + " " + label);
		}
	}

	private static void appendPlainStat(final StringBuilder stats,
			final String label, final int value) {
		if (value != 0) {
			appendLine(stats, label + ": " + value);
		}
	}

	private static void appendLine(final StringBuilder target, final String line) {
		if (target.length() > 0) {
			target.append("<br>");
		}
		target.append(escapeHtml(line));
	}

	private static void appendBonusLine(final StringBuilder bonuses,
			final String line) {
		if (bonuses.length() > 0) {
			bonuses.append("<br>");
		}
		bonuses.append("&#9670; ").append(escapeHtml(line));
	}

	private static void appendFooter(final StringBuilder tooltip,
			final RPObject object, final String scrollDestination) {
		final StringBuilder footer = new StringBuilder();
		final int minLevel = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.MIN_LEVEL);
		if (minLevel > 0) {
			footer.append("Wymagany poziom: ").append(minLevel);
		}
		final int value = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.VALUE);
		if (value > 0) {
			appendFooterSeparator(footer);
			footer.append("Wartość: ").append(value);
		}
		final int durability = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.DURABILITY);
		if (durability > 0) {
			appendFooterSeparator(footer);
			final int uses = WeaponPerformanceCalculator.getInt(object,
					ItemTooltip.USES);
			footer.append("Wytrzymałość: ")
					.append(Math.max(0, durability - uses)).append("/")
					.append(durability);
		}
		if (scrollDestination != null) {
			appendFooterSeparator(footer);
			footer.append(escapeHtml(scrollDestination));
		}
		if (footer.length() > 0) {
			tooltip.append("<hr><font color='").append(MUTED_COLOR)
					.append("'>").append(footer).append("</font>");
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

	private static String localizeDamageType(final String value) {
		final String type = value.toLowerCase(Locale.ROOT);
		if ("light".equals(type)) return "Światło";
		if ("dark".equals(type)) return "Mrok";
		if ("fire".equals(type)) return "Ogień";
		if ("ice".equals(type)) return "Lód";
		if ("water".equals(type)) return "Woda";
		if ("earth".equals(type)) return "Natura";
		if ("cut".equals(type)) return "Fizyczne";
		return value;
	}

	private static String signed(final String value) {
		return value.startsWith("-") ? value : "+" + value;
	}

	private static String formatCompact(final double value) {
		return Math.rint(value) == value
				? Integer.toString((int) value) : formatOneDecimal(value);
	}

	private static synchronized String formatOneDecimal(final double value) {
		return ONE_DECIMAL.format(value);
	}

	private static synchronized String formatTwoDecimals(final double value) {
		return TWO_DECIMALS.format(value);
	}

	private static DecimalFormat createDecimalFormat(final String pattern) {
		return new DecimalFormat(pattern,
				new DecimalFormatSymbols(new Locale("pl", "PL")));
	}

	private static String escapeHtml(final String text) {
		if (text == null) return "";
		return text.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
