/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Item;

/** Formats item internals for readable administrator diagnostics. */
final class ItemInspectionFormatter {
	private static final int DISPLAY_SCALE = 2;

	private ItemInspectionFormatter() {
		// utility class
	}

	static String format(final Item item) {
		if (item == null) {
			return "null";
		}

		final StringBuilder out = new StringBuilder();
		out.append("Nazwa: ").append(item.getName());
		if (item.has("class")) {
			out.append("\nKlasa: ").append(item.get("class"));
		}
		if (item.has("subclass")) {
			out.append(" / ").append(item.get("subclass"));
		}
		if (item.has(Item.RARITY_ID)) {
			out.append("\nRzadkość: ").append(item.get(Item.RARITY_ID));
		}
		final Long seed = ItemAffixState.getSeed(item);
		if (seed != null) {
			out.append("\nSeed afiksów: ").append(seed.longValue());
		}

		out.append("\n\nAtrybuty:");
		final Set<String> attributes = new TreeSet<String>();
		for (final String key : item) {
			attributes.add(key);
		}
		for (final String key : attributes) {
			if (ItemAffixState.SEED_ATTRIBUTE.equals(key)) {
				continue;
			}
			out.append("\n  ").append(key).append('=').append(
					formatValue(item.get(key)));
		}

		appendMap(out, item, ItemAffixState.ATTRIBUTE, "Afiksy");
		appendMap(out, item, Item.RARITY_MODIFIERS, "Modyfikatory rzadkości");
		appendMap(out, item, ItemTooltip.ATTRIBUTE, "Dane tooltipa");
		return out.toString();
	}

	private static void appendMap(final StringBuilder out, final Item item,
			final String attribute, final String title) {
		if (!item.hasMap(attribute)) {
			return;
		}
		out.append("\n\n").append(title).append(':');
		final Map<String, String> map = item.getMap(attribute);
		for (final String key : new TreeSet<String>(map.keySet())) {
			out.append("\n  ").append(key).append('=').append(
					formatValue(map.get(key)));
		}
	}

	static String formatValue(final String value) {
		if (value == null || value.length() == 0) {
			return value;
		}
		try {
			final BigDecimal number = new BigDecimal(value);
			if (number.scale() <= 0) {
				return number.toPlainString();
			}
			return number.setScale(DISPLAY_SCALE, RoundingMode.HALF_UP)
					.stripTrailingZeros().toPlainString();
		} catch (final NumberFormatException e) {
			return value;
		}
	}
}
