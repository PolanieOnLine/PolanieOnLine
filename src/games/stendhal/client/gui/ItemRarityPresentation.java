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

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.swing.ToolTipManager;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Item;
import games.stendhal.client.gui.WeaponPerformanceCalculator.WeaponPerformance;
import games.stendhal.client.gui.styled.Style;
import games.stendhal.client.gui.styled.StyleUtil;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;

/** Builds compact structured desktop item tooltips. */
final class ItemRarityPresentation {
	private static final String FALLBACK_TEXT_COLOR = "#f3efe7";
	private static final String FALLBACK_ACCENT_COLOR = "#a37861";
	private static final String RARITY_GLOW_MARKER = "item-rarity-glow:";
	private static final DecimalFormat ONE_DECIMAL = createDecimalFormat("0.0");
	private static final DecimalFormat TWO_DECIMALS = createDecimalFormat("0.00");

	/* Compatibility fallback for old servers that do not publish a tooltip
	 * category. New clients should prefer ItemTooltip.CATEGORY. */
	private static final Set<String> LEGACY_ARMOUR_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"armor", "shield", "helmet", "cloak", "boots", "gloves",
					"legs", "belt", "belts")));
	private static final Set<String> LEGACY_ACCESSORY_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"ring", "necklace")));

	static {
		/* Swing hides standard tooltips after four seconds by default. Item
		 * descriptions stay visible while the cursor remains over the slot. */
		final ToolTipManager manager = ToolTipManager.sharedInstance();
		manager.setDismissDelay(60000);
		manager.setInitialDelay(350);
		manager.setReshowDelay(100);
	}

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
		final String category = resolveCategory(object);
		final boolean weapon = ItemTooltip.CATEGORY_WEAPON.equals(category);
		final boolean armour = ItemTooltip.CATEGORY_ARMOUR.equals(category)
				&& WeaponPerformanceCalculator.getInt(object,
						ItemTooltip.DEFENSE) > 0;
		final WeaponPerformance performance = weapon
				? WeaponPerformanceCalculator.calculate(object) : null;
		final boolean hasStructuredStats = object.hasMap(ItemTooltip.ATTRIBUTE);

		if (rarity == null && performance == null && !hasStructuredStats) {
			return scrollDestination;
		}

		final StringBuilder tooltip = new StringBuilder("<html>");
		appendRarityGlowMarker(tooltip, rarity);
		/* Do not paint a private card background here. StyledToolTipUI tiles the
		 * currently selected client skin behind this transparent HTML content. */
		tooltip.append("<table width='190' cellpadding='4' cellspacing='0'><tr><td>")
				.append("<font color='").append(getTextColor()).append("'>");
		appendHeader(tooltip, entity, object, rarity);
		if (performance != null) {
			appendWeaponPerformance(tooltip, object, performance);
		} else if (armour) {
			appendArmourPerformance(tooltip, object);
		}
		appendCoreStats(tooltip, object, weapon);
		appendBonuses(tooltip, object, weapon, armour);
		appendFooter(tooltip, object, scrollDestination);
		tooltip.append("</font></td></tr></table></html>");
		return tooltip.toString();
	}

	private static String resolveCategory(final RPObject object) {
		final String published = WeaponPerformanceCalculator.getTooltipValue(
				object, ItemTooltip.CATEGORY);
		if (ItemTooltip.CATEGORY_WEAPON.equals(published)
				|| ItemTooltip.CATEGORY_ARMOUR.equals(published)
				|| ItemTooltip.CATEGORY_ACCESSORY.equals(published)
				|| ItemTooltip.CATEGORY_OTHER.equals(published)) {
			return published;
		}

		if (WeaponPerformanceCalculator.isWeapon(object)) {
			return ItemTooltip.CATEGORY_WEAPON;
		}
		if (object != null && object.has("class")) {
			final String itemClass = object.get("class");
			if (LEGACY_ARMOUR_CLASSES.contains(itemClass)) {
				return ItemTooltip.CATEGORY_ARMOUR;
			}
			if (LEGACY_ACCESSORY_CLASSES.contains(itemClass)) {
				return ItemTooltip.CATEGORY_ACCESSORY;
			}
		}
		return ItemTooltip.CATEGORY_OTHER;
	}

	private static void appendRarityGlowMarker(final StringBuilder tooltip,
			final ItemRarity rarity) {
		if (rarity == null) {
			return;
		}
		tooltip.append("<!--").append(RARITY_GLOW_MARKER)
				.append(escapeHtml(rarity.getColorHex())).append(":")
				.append(getGlowOpacity(rarity)).append("-->");
	}

	private static String getGlowOpacity(final ItemRarity rarity) {
		switch (rarity) {
		case LEGENDARY:
			return "0.14";
		case EPIC:
			return "0.12";
		case RARE:
			return "0.09";
		case COMMON:
		default:
			return "0.05";
		}
	}

	private static void appendHeader(final StringBuilder tooltip,
			final IEntity entity, final RPObject object, final ItemRarity rarity) {
		final String title = entity.getTitle();
		if (title != null) {
			final String titleColor = rarity == null ? getTextColor()
					: rarity.getColorHex();
			tooltip.append("<font size='-1' color='")
					.append(escapeHtml(titleColor)).append("'><b>")
					.append(escapeHtml(title.toUpperCase(Locale.ROOT)))
					.append("</b></font>");
		}
		if (rarity != null) {
			tooltip.append("<br><font size='-1' color='")
					.append(getTextColor()).append("'>")
					.append(escapeHtml(rarity.getPolishDisplayName()))
					.append("</font>");
		}
		appendImprovement(tooltip, object, rarity);
	}

	private static void appendImprovement(final StringBuilder tooltip,
			final RPObject object, final ItemRarity rarity) {
		final int improve = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.IMPROVE);
		final int maxImproves = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.MAX_IMPROVES);
		if (maxImproves <= 0 && improve <= 0) {
			return;
		}

		final String color = rarity == null ? getAccentColor()
				: rarity.getColorHex();
		tooltip.append("<br><font size='-1' color='")
				.append(escapeHtml(color)).append("'>&#9670;&nbsp; ")
				.append("Ulepszenie: +").append(improve);
		if (maxImproves > 0) {
			tooltip.append("/").append(maxImproves);
		}
		tooltip.append("</font>");
	}

	private static void appendWeaponPerformance(final StringBuilder tooltip,
			final RPObject object, final WeaponPerformance performance) {
		appendDivider(tooltip);
		appendPrimaryValue(tooltip,
				formatOneDecimal(performance.getBaseDps())
						+ " pkt. obrażeń na sekundę", null);

		tooltip.append("<table cellpadding='0' cellspacing='0'>");
		appendTreeDetail(tooltip, true,
				"[" + performance.getDamageMin() + "–"
						+ performance.getDamageMax()
						+ "] pkt. obrażeń za trafienie");
		appendTreeDetail(tooltip, false,
				formatTwoDecimals(performance.getAttacksPerSecond())
						+ " ataku na sekundę ("
						+ getWeaponSpeedLabel(performance.getAttacksPerSecond()) + ")");
		tooltip.append("</table>");

		final int range = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.RANGE);
		final String damageType = WeaponPerformanceCalculator.getTooltipValue(
				object, ItemTooltip.DAMAGE_TYPE);
		final String statuses = WeaponPerformanceCalculator.getTooltipValue(
				object, ItemTooltip.STATUS_ATTACK);
		if (range > 0 || damageType != null
				|| (statuses != null && !statuses.isEmpty())) {
			tooltip.append("<div style='margin-top:3px'><font size='-1' color='")
					.append(getTextColor()).append("'>");
			if (range > 0) {
				tooltip.append("Zasięg: ").append(range);
			}
			if (damageType != null) {
				if (range > 0) {
					tooltip.append("<br>");
				}
				tooltip.append("Typ obrażeń: ")
						.append(escapeHtml(localizeDamageType(damageType)));
			}
			if (statuses != null && !statuses.isEmpty()) {
				if (range > 0 || damageType != null) {
					tooltip.append("<br>");
				}
				tooltip.append("Efekty trafienia: ")
						.append(escapeHtml(statuses.replace(';', ',')));
			}
			tooltip.append("</font></div>");
		}
	}

	private static void appendArmourPerformance(final StringBuilder tooltip,
			final RPObject object) {
		final int armour = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.DEFENSE);
		if (armour <= 0) {
			return;
		}
		appendDivider(tooltip);
		appendPrimaryValue(tooltip, armour + " pkt. pancerza", null);
		tooltip.append("<table cellpadding='0' cellspacing='0'>");
		appendTreeDetail(tooltip, false, "Ochrona podstawowa");
		tooltip.append("</table>");
	}

	private static void appendPrimaryValue(final StringBuilder tooltip,
			final String value, final String details) {
		/* Keep the primary value at the client's configured base font size. The
		 * bold weight establishes hierarchy without oversized Swing HTML text. */
		tooltip.append("<div><b>").append(value).append("</b>");
		if (details != null && !details.isEmpty()) {
			tooltip.append("<br><font size='-1'>").append(details)
					.append("</font>");
		}
		tooltip.append("</div>");
	}

	private static void appendTreeDetail(final StringBuilder tooltip,
			final boolean branchContinues, final String details) {
		tooltip.append("<tr><td valign='top'><font size='-1' color='")
				.append(getAccentColor()).append("'>")
				.append(branchContinues
						? "&#9500;&#9472;&#9670;&nbsp;" : "&#9492;&#9472;&#9670;&nbsp;")
				.append("</font></td><td><font size='-1' color='")
				.append(getTextColor()).append("'>")
				.append(escapeHtml(details))
				.append("</font></td></tr>");
	}

	private static void appendDivider(final StringBuilder tooltip) {
		tooltip.append("<div style='text-align:center'><font size='-1' color='")
				.append(getAccentColor())
				.append("'>&#9472;&#9472;&#9472;&#9472;&#9671;&#9671;")
				.append("&#9472;&#9472;&#9472;&#9472;</font></div>");
	}

	private static String getWeaponSpeedLabel(final double attacksPerSecond) {
		if (attacksPerSecond >= 2.0) {
			return "Bardzo szybka broń";
		}
		if (attacksPerSecond >= 1.25) {
			return "Szybka broń";
		}
		if (attacksPerSecond >= 1.0) {
			return "Umiarkowana broń";
		}
		if (attacksPerSecond >= 0.6) {
			return "Powolna broń";
		}
		return "Bardzo powolna broń";
	}

	private static void appendCoreStats(final StringBuilder tooltip,
			final RPObject object, final boolean weapon) {
		final StringBuilder stats = new StringBuilder();
		if (weapon) {
			appendPlainStat(stats, "Pancerz", WeaponPerformanceCalculator.getInt(
					object, ItemTooltip.DEFENSE));
		}

		appendPlainStat(stats, "Siła ataku", WeaponPerformanceCalculator.getInt(
				object, ItemTooltip.SKILL_ATTACK));
		if (stats.length() > 0) {
			appendDivider(tooltip);
			tooltip.append("<font size='-1'>").append(stats).append("</font>");
		}
	}

	private static void appendBonuses(final StringBuilder tooltip,
			final RPObject object, final boolean weapon, final boolean armour) {
		final StringBuilder bonuses = new StringBuilder();

		appendResistance(bonuses, object, "light", "ŚWIATŁO");
		appendResistance(bonuses, object, "dark", "MROK");
		appendResistance(bonuses, object, "fire", "OGIEŃ");
		appendResistance(bonuses, object, "ice", "LÓD");
		appendResistance(bonuses, object, "earth", "NATURA");
		appendResistance(bonuses, object, "water", "WODA");
		appendResistance(bonuses, object, "cut", "FIZYCZNE");

		/* ATK on armour and accessories is an equipment bonus, not weapon DPS. */
		if (!weapon) {
			final int attack = Math.max(
					WeaponPerformanceCalculator.getInt(object, ItemTooltip.ATTACK),
					WeaponPerformanceCalculator.getInt(object,
							ItemTooltip.RANGED_ATTACK));
			if (attack != 0) {
				appendBonusLine(bonuses,
						signed(Integer.toString(attack)) + " ataku");
			}
		}

		/* Accessories and miscellaneous equipment keep DEF beside other bonuses.
		 * Only real armour receives the large primary armour block. */
		if (!weapon && !armour) {
			final int defense = WeaponPerformanceCalculator.getInt(object,
					ItemTooltip.DEFENSE);
			if (defense != 0) {
				appendBonusLine(bonuses,
						signed(Integer.toString(defense)) + " pancerza");
			}
		}

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
				"bonusu pancerza", false);

		if (bonuses.length() > 0) {
			appendDivider(tooltip);
			tooltip.append("<font size='-1'>").append(bonuses)
					.append("</font>");
		}
	}

	private static void appendResistance(final StringBuilder bonuses,
			final RPObject object, final String nature, final String label) {
		final String value = WeaponPerformanceCalculator.getTooltipValue(object,
				ItemTooltip.RESISTANCE_PREFIX + nature);
		if (value != null) {
			appendBonusLine(bonuses, label + ": " + value + "%");
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
		appendBonusLine(bonuses,
				signed(formatCompact(value)) + "% " + label);
	}

	private static void appendIntegerBonus(final StringBuilder bonuses,
			final RPObject object, final String attribute, final String label) {
		final int value = WeaponPerformanceCalculator.getInt(object, attribute);
		if (value != 0) {
			appendBonusLine(bonuses,
					signed(Integer.toString(value)) + " " + label);
		}
	}

	private static void appendPlainStat(final StringBuilder stats,
			final String label, final int value) {
		if (value != 0) {
			appendLine(stats, label + ": " + value);
		}
	}

	private static void appendLine(final StringBuilder target,
			final String line) {
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
			appendDivider(tooltip);
			tooltip.append("<div style='text-align:right'><font size='-1' color='")
					.append(getFooterColor()).append("'>")
					.append(footer).append("</font></div>");
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

	private static String getTextColor() {
		final Style style = StyleUtil.getStyle();
		return style == null ? FALLBACK_TEXT_COLOR
				: colorToHex(style.getForeground());
	}

	private static String getAccentColor() {
		final Style style = StyleUtil.getStyle();
		return style == null ? FALLBACK_ACCENT_COLOR
				: colorToHex(style.getHighLightColor());
	}

	private static String getFooterColor() {
		final Style style = StyleUtil.getStyle();
		if (style == null) {
			return FALLBACK_ACCENT_COLOR;
		}
		return colorToHex(mixColors(style.getForeground(),
				style.getHighLightColor(), 0.35));
	}

	private static Color mixColors(final Color primary, final Color secondary,
			final double secondaryWeight) {
		final double primaryWeight = 1.0 - secondaryWeight;
		return new Color(
				(int) Math.round(primary.getRed() * primaryWeight
						+ secondary.getRed() * secondaryWeight),
				(int) Math.round(primary.getGreen() * primaryWeight
						+ secondary.getGreen() * secondaryWeight),
				(int) Math.round(primary.getBlue() * primaryWeight
						+ secondary.getBlue() * secondaryWeight));
	}

	private static String colorToHex(final Color color) {
		return String.format(Locale.ROOT, "#%02x%02x%02x",
				color.getRed(), color.getGreen(), color.getBlue());
	}

	private static String localizeDamageType(final String value) {
		final String type = value.toLowerCase(Locale.ROOT);
		if ("light".equals(type)) {
			return "Światło";
		}
		if ("dark".equals(type)) {
			return "Mrok";
		}
		if ("fire".equals(type)) {
			return "Ogień";
		}
		if ("ice".equals(type)) {
			return "Lód";
		}
		if ("water".equals(type)) {
			return "Woda";
		}
		if ("earth".equals(type)) {
			return "Natura";
		}
		if ("cut".equals(type)) {
			return "Fizyczne";
		}
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
		if (text == null) {
			return "";
		}
		return text.replace("&", "&amp;").replace("<", "&lt;")
				.replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&#39;");
	}
}
