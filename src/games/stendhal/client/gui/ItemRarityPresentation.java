/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
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
	private static final String BETTER_COLOR = "#62d26f";
	private static final String WORSE_COLOR = "#ef6a62";
	private static final String RARITY_GLOW_MARKER = "item-rarity-glow:";
	private static final DecimalFormat ONE_DECIMAL = createDecimalFormat("0.0");
	private static final DecimalFormat TWO_DECIMALS = createDecimalFormat("0.00");

	/* Compatibility fallback for old servers that do not publish a tooltip
	 * category. New clients should prefer ItemTooltip.CATEGORY. */
	private static final Set<String> LEGACY_ARMOUR_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"armor", "shield", "helmet", "cloak", "boots", "glove", "gloves",
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
		return buildItemToolTip(entity,
				EquipmentComparisonResolver.resolve(entity.getRPObject()));
	}

	static String buildItemToolTip(final IEntity entity,
			final RPObject equippedItem) {
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
		appendComparisonHeader(tooltip, equippedItem);
		if (performance != null) {
			appendWeaponPerformance(tooltip, object, performance, equippedItem);
		} else if (armour) {
			appendArmourPerformance(tooltip, object, equippedItem);
		}
		appendCoreStats(tooltip, object, equippedItem, weapon);
		appendBonuses(tooltip, object, equippedItem, weapon, armour);
		final String legendaryAffix = LegendaryAffixPresentation.build(object);
		if (!legendaryAffix.isEmpty()) {
			appendDivider(tooltip);
			tooltip.append(legendaryAffix);
		}
		appendFooter(tooltip, object, scrollDestination);
		tooltip.append("</font></td></tr></table></html>");
		return tooltip.toString();
	}

	static String resolveCategory(final RPObject object) {
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

	private static void appendComparisonHeader(final StringBuilder tooltip,
			final RPObject equippedItem) {
		if (equippedItem == null) {
			return;
		}
		final String name = equippedItem.has("title") ? equippedItem.get("title")
				: equippedItem.has("name") ? equippedItem.get("name") : null;
		if (name != null && !name.isEmpty()) {
			tooltip.append("<br><font size='-1' color='")
					.append(getAccentColor()).append("'>Porównanie z: ")
					.append(escapeHtml(name)).append("</font>");
		}
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
			final RPObject object, final WeaponPerformance performance,
			final RPObject equippedItem) {
		final WeaponPerformance equipped = WeaponPerformanceCalculator.calculate(
				equippedItem);
		appendDivider(tooltip);
		appendPrimaryValue(tooltip,
				formatOneDecimal(performance.getBaseDps())
						+ " pkt. obrażeń na sekundę"
						+ (equipped == null ? "" : delta(
								performance.getBaseDps(), equipped.getBaseDps(), 1)), null);

		tooltip.append("<table cellpadding='0' cellspacing='0'>");
		appendTreeDetail(tooltip, true,
				"[" + performance.getDamageMin() + "–"
						+ performance.getDamageMax()
						+ "] pkt. obrażeń za trafienie",
				equipped == null ? "" : damageRangeDelta(performance, equipped));
		appendTreeDetail(tooltip, false,
				formatTwoDecimals(performance.getAttacksPerSecond())
						+ " ataku na sekundę ("
						+ getWeaponSpeedLabel(performance.getAttacksPerSecond()) + ")",
				equipped == null ? "" : delta(performance.getAttacksPerSecond(),
						equipped.getAttacksPerSecond(), 2));
		tooltip.append("</table>");

		final int range = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.RANGE);
		final int equippedRange = WeaponPerformanceCalculator.getInt(equippedItem,
				ItemTooltip.RANGE);
		final String damageType = WeaponPerformanceCalculator.getTooltipValue(
				object, ItemTooltip.DAMAGE_TYPE);
		final String statuses = WeaponPerformanceCalculator.getTooltipValue(
				object, ItemTooltip.STATUS_ATTACK);
		if (range > 0 || equippedRange > 0 || damageType != null
				|| (statuses != null && !statuses.isEmpty())) {
			tooltip.append("<div style='margin-top:3px'><font size='-1' color='")
					.append(getTextColor()).append("'>");
			if (range > 0 || equippedRange > 0) {
				tooltip.append("Zasięg: ").append(range)
						.append(delta(range, equippedRange, 0));
			}
			if (damageType != null) {
				if (range > 0 || equippedRange > 0) {
					tooltip.append("<br>");
				}
				tooltip.append("Typ obrażeń: ")
						.append(escapeHtml(localizeDamageType(damageType)));
			}
			if (statuses != null && !statuses.isEmpty()) {
				if (range > 0 || equippedRange > 0 || damageType != null) {
					tooltip.append("<br>");
				}
				tooltip.append("Efekty trafienia: ")
						.append(escapeHtml(statuses.replace(';', ',')));
			}
			tooltip.append("</font></div>");
		}
	}

	private static void appendArmourPerformance(final StringBuilder tooltip,
			final RPObject object, final RPObject equippedItem) {
		final int armour = WeaponPerformanceCalculator.getInt(object,
				ItemTooltip.DEFENSE);
		if (armour <= 0) {
			return;
		}
		appendDivider(tooltip);
		final int equippedArmour = WeaponPerformanceCalculator.getInt(equippedItem,
				ItemTooltip.DEFENSE);
		appendPrimaryValue(tooltip, armour + " pkt. pancerza"
				+ (equippedItem == null ? ""
						: delta(armour, equippedArmour, 0)), null);
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
			final boolean branchContinues, final String details,
			final String comparison) {
		tooltip.append("<tr><td valign='top'><font size='-1' color='")
				.append(getAccentColor()).append("'>")
				.append(branchContinues
						? "&#9500;&#9472;&#9670;&nbsp;" : "&#9492;&#9472;&#9670;&nbsp;")
				.append("</font></td><td><font size='-1' color='")
				.append(getTextColor()).append("'>")
				.append(escapeHtml(details))
				.append(comparison)
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
			final RPObject object, final RPObject equippedItem,
			final boolean weapon) {
		final StringBuilder stats = new StringBuilder();
		if (weapon) {
			appendPlainStat(stats, "Pancerz",
					WeaponPerformanceCalculator.getInt(object, ItemTooltip.DEFENSE),
					WeaponPerformanceCalculator.getInt(equippedItem,
							ItemTooltip.DEFENSE), equippedItem != null);
		}

		appendPlainStat(stats, "Siła ataku",
				WeaponPerformanceCalculator.getInt(object, ItemTooltip.SKILL_ATTACK),
				WeaponPerformanceCalculator.getInt(equippedItem,
						ItemTooltip.SKILL_ATTACK), equippedItem != null);
		if (stats.length() > 0) {
			appendDivider(tooltip);
			tooltip.append("<font size='-1'>").append(stats).append("</font>");
		}
	}

	private static void appendBonuses(final StringBuilder tooltip,
			final RPObject object, final RPObject equippedItem,
			final boolean weapon, final boolean armour) {
		final StringBuilder coreBonuses = new StringBuilder();
		final StringBuilder resistances = new StringBuilder();
		final StringBuilder specialBonuses = new StringBuilder();

		/* Flat equipment values are the most important secondary properties and
		 * stay directly below the item's primary attack/armour block. */
		if (!weapon) {
			final int attack = Math.max(
					WeaponPerformanceCalculator.getInt(object, ItemTooltip.ATTACK),
					WeaponPerformanceCalculator.getInt(object,
							ItemTooltip.RANGED_ATTACK));
			final int equippedAttack = Math.max(
					WeaponPerformanceCalculator.getInt(equippedItem,
							ItemTooltip.ATTACK),
					WeaponPerformanceCalculator.getInt(equippedItem,
							ItemTooltip.RANGED_ATTACK));
			appendComparableBonusLine(coreBonuses, attack, equippedAttack,
					"ataku", equippedItem != null);
		}

		/* Accessories and miscellaneous equipment keep DEF beside other bonuses.
		 * Only real armour receives the large primary armour block. */
		if (!weapon && !armour) {
			final int defense = WeaponPerformanceCalculator.getInt(object,
					ItemTooltip.DEFENSE);
			appendComparableBonusLine(coreBonuses, defense,
					WeaponPerformanceCalculator.getInt(equippedItem,
							ItemTooltip.DEFENSE), "pancerza", equippedItem != null);
		}
		appendIntegerBonus(coreBonuses, object, equippedItem, ItemTooltip.HEALTH,
				"zdrowia");
		appendFlatAffixBonus(coreBonuses, object, equippedItem,
				ItemTooltip.AFFIX_FLAT_ATTACK_BONUS,
				ItemTooltip.FLAT_ATTACK_BONUS, "dodatkowego ataku",
				"ataku z affixu");
		appendFlatAffixBonus(coreBonuses, object, equippedItem,
				ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS,
				ItemTooltip.FLAT_DEFENSE_BONUS, "dodatkowego pancerza",
				"pancerza z affixu");

		/* Resistances form their own visual block so the player can separate
		 * elemental protection from the item's defining flat statistics. */
		appendResistance(resistances, object, equippedItem, "light", "światło");
		appendResistance(resistances, object, equippedItem, "dark", "mrok");
		appendResistance(resistances, object, equippedItem, "fire", "ogień");
		appendResistance(resistances, object, equippedItem, "ice", "lód");
		appendResistance(resistances, object, equippedItem, "earth", "naturę");
		appendResistance(resistances, object, equippedItem, "water", "wodę");
		appendResistance(resistances, object, equippedItem, "cut",
				"obrażenia fizyczne");

		/* Percentage and proc-like bonuses are the final detail layer and stay
		 * visually separate from both flat stats and resistances. */
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.ATTACK_BONUS,
				"bonusu ataku", false);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.ACCURACY_BONUS,
				"bonusu precyzji", false);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.CRITICAL_CHANCE,
				"szansy na trafienie krytyczne", false);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.CRITICAL_DAMAGE_BONUS,
				"obrażeń trafienia krytycznego", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.PARRY_CHANCE,
				"szansy na parowanie", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.ARMOR_PENETRATION,
				"penetracji pancerza", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.BLEED_ON_HIT,
				"szansy na krwawienie", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.EXECUTE_DAMAGE,
				"obrażeń poniżej 25% PW celu", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.POISON_ON_HIT,
				"szansy na zatrucie", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.DISTANCE_DAMAGE,
				"obrażeń z dystansu", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.RESIST_POISONED,
				"odporności na zatrucie", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.RESIST_BLEEDING,
				"odporności na krwawienie", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.RESIST_SHOCKED,
				"odporności na szok", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.RESIST_CONFUSED,
				"odporności na dezorientację", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.RESIST_HEAVY,
				"odporności na spowolnienie", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.CRITICAL_BONUS,
				"obrażeń krytycznych", false);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.LIFESTEAL,
				"kradzieży życia", true);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.LIFESTEAL_INCREASE,
				"zwiększonej kradzieży życia", false);
		appendPercentageBonus(specialBonuses, object, equippedItem,
				ItemTooltip.DEFENSE_BONUS,
				"bonusu pancerza", false);

		appendBonusSection(tooltip, coreBonuses);
		appendBonusSection(tooltip, resistances);
		appendBonusSection(tooltip, specialBonuses);
	}

	private static void appendBonusSection(final StringBuilder tooltip,
			final StringBuilder section) {
		if (section.length() == 0) {
			return;
		}
		appendDivider(tooltip);
		tooltip.append("<font size='-1'>").append(section).append("</font>");
	}

	private static void appendResistance(final StringBuilder bonuses,
			final RPObject object, final RPObject equippedItem,
			final String nature, final String label) {
		final Double modifier = getResistanceModifier(object, nature);
		final Double equippedModifier = getResistanceModifier(equippedItem, nature);
		if (modifier == null || (modifier.doubleValue() == 0.0
				&& (equippedModifier == null
						|| equippedModifier.doubleValue() == 0.0))) {
			return;
		}
		appendBonusLine(bonuses, signed(formatCompact(modifier.doubleValue()))
				+ "% odporności na " + label,
				equippedItem == null ? "" : delta(modifier.doubleValue(),
						equippedModifier == null ? 0.0
								: equippedModifier.doubleValue(), 1));
	}

	private static void appendPercentageBonus(final StringBuilder bonuses,
			final RPObject object, final RPObject equippedItem,
			final String attribute, final String label, final boolean fraction) {
		final double value = percentageValue(object, attribute, fraction);
		final double equippedValue = percentageValue(equippedItem, attribute,
				fraction);
		if (value == 0.0 && (equippedItem == null || equippedValue == 0.0)) {
			return;
		}
		appendBonusLine(bonuses, signed(formatCompact(value)) + "% " + label,
				equippedItem == null ? "" : delta(value, equippedValue, 1));
	}

	private static void appendIntegerBonus(final StringBuilder bonuses,
			final RPObject object, final RPObject equippedItem,
			final String attribute, final String label) {
		final int value = WeaponPerformanceCalculator.getInt(object, attribute);
		final int equippedValue = WeaponPerformanceCalculator.getInt(equippedItem,
				attribute);
		appendComparableBonusLine(bonuses, value, equippedValue, label,
				equippedItem != null);
	}

	private static void appendFlatAffixBonus(final StringBuilder bonuses,
			final RPObject object, final RPObject equippedItem,
			final String attribute, final String legacyAttribute,
			final String label, final String legacyLabel) {
		final boolean published = hasTooltipValue(object, attribute)
				|| hasTooltipValue(equippedItem, attribute);
		final int value = tooltipIntWithFallback(object, attribute,
				legacyAttribute);
		final int equippedValue = tooltipIntWithFallback(equippedItem, attribute,
				legacyAttribute);
		appendComparableBonusLine(bonuses, value, equippedValue,
				published ? label : legacyLabel, equippedItem != null);
	}

	private static int tooltipIntWithFallback(final RPObject object,
			final String attribute, final String fallback) {
		return hasTooltipValue(object, attribute)
				? WeaponPerformanceCalculator.getInt(object, attribute)
				: WeaponPerformanceCalculator.getInt(object, fallback);
	}

	private static boolean hasTooltipValue(final RPObject object,
			final String attribute) {
		return WeaponPerformanceCalculator.getTooltipValue(object, attribute) != null;
	}

	private static void appendPlainStat(final StringBuilder stats,
			final String label, final int value, final int equippedValue,
			final boolean comparing) {
		if (value != 0 || (comparing && equippedValue != 0)) {
			if (stats.length() > 0) {
				stats.append("<br>");
			}
			stats.append(escapeHtml(label + ": " + value));
			if (comparing) {
				stats.append(delta(value, equippedValue, 0));
			}
		}
	}

	private static void appendComparableBonusLine(final StringBuilder bonuses,
			final int value, final int equippedValue, final String label,
			final boolean comparing) {
		if (value == 0 && (!comparing || equippedValue == 0)) {
			return;
		}
		appendBonusLine(bonuses, signed(Integer.toString(value)) + " " + label,
				comparing ? delta(value, equippedValue, 0) : "");
	}

	private static void appendBonusLine(final StringBuilder bonuses,
			final String line) {
		appendBonusLine(bonuses, line, "");
	}

	private static void appendBonusLine(final StringBuilder bonuses,
			final String line, final String comparison) {
		if (bonuses.length() > 0) {
			bonuses.append("<br>");
		}
		bonuses.append("&#9670; ").append(escapeHtml(line)).append(comparison);
	}

	private static Double getResistanceModifier(final RPObject object,
			final String nature) {
		final String value = WeaponPerformanceCalculator.getTooltipValue(object,
				ItemTooltip.RESISTANCE_PREFIX + nature);
		if (value == null) {
			return Double.valueOf(0.0);
		}
		try {
			return Double.valueOf(Double.parseDouble(value) - 100.0);
		} catch (final NumberFormatException ignored) {
			return null;
		}
	}

	private static double percentageValue(final RPObject object,
			final String attribute, final boolean fraction) {
		double value = WeaponPerformanceCalculator.getDouble(object, attribute);
		if (fraction && Math.abs(value) <= 1.0) {
			value *= 100.0;
		}
		return value;
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

	private static String damageRangeDelta(final WeaponPerformance current,
			final WeaponPerformance equipped) {
		final int minimum = current.getDamageMin() - equipped.getDamageMin();
		final int maximum = current.getDamageMax() - equipped.getDamageMax();
		if (minimum == 0 && maximum == 0) {
			return "";
		}
		return " (" + coloredDeltaValue(minimum, 0) + "–"
				+ coloredDeltaValue(maximum, 0) + ")";
	}

	private static String delta(final double current, final double equipped,
			final int precision) {
		final double difference = current - equipped;
		if (isRoundedZero(difference, precision)) {
			return "";
		}
		return " (" + coloredDeltaValue(difference, precision) + ")";
	}

	private static String coloredDeltaValue(final double difference,
			final int precision) {
		if (isRoundedZero(difference, precision)) {
			return "0";
		}
		final String formatted;
		if (precision == 0) {
			formatted = Long.toString(Math.round(difference));
		} else if (precision == 1) {
			formatted = formatOneDecimal(difference);
		} else {
			formatted = formatTwoDecimals(difference);
		}
		return "<font color='" + (difference > 0.0 ? BETTER_COLOR : WORSE_COLOR)
				+ "'>" + signed(formatted) + "</font>";
	}

	private static boolean isRoundedZero(final double value,
			final int precision) {
		final double threshold = precision == 0 ? 0.5
				: precision == 1 ? 0.05 : 0.005;
		return Math.abs(value) < threshold;
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
