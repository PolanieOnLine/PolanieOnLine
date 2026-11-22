/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import games.stendhal.client.ClientLanguage;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.client.sprite.DataLoader;
import marauroa.common.game.RPObject;

/** One small, shared marker for the item's permanently chosen affix slot. */
final class AffixRerollPresentation {
	private AffixRerollPresentation() { }

	static String icon(final RPObject item, final String key) {
		return key.equals(WeaponPerformanceCalculator.getTooltipValue(item, ItemTooltip.REROLLED_AFFIX)) ? icon() : "";
	}

	static String icon() {
		return "<img src='" + DataLoader.getResource("data/gui/affix_rerolled.png")
				+ "' width='12' height='12' alt='" + ClientLanguage.getText("Afiks wybrany do przemiany") + "'>&nbsp;";
	}

	static String labelHtml(final String label, final boolean marked, final int width) {
		return labelHtml(label, marked, width, "", false);
	}

	static String labelHtml(final String label, final boolean marked, final int width,
			final String caption, final boolean legendary) {
		return "<html><div style='width:" + width + "px'>" + ItemRarityPresentation.escapeHtml(caption)
				+ (marked ? icon() : "") + content(label, legendary) + "</div></html>";
	}

	static String listLabelHtml(final String label, final boolean marked, final int width,
			final boolean separator, final boolean legendary) {
		return "<html><div style='width:" + width + "px'>" + (marked ? icon() : "") + content(label, legendary)
				+ (separator ? ItemRarityPresentation.dividerHtml() : "") + "</div></html>";
	}

	static String offerLabelHtml(final String label, final int width, final boolean legendary) {
		return "<html><div style='width:" + width + "px'><font color='#9bd9f4'><b>"
				+ ItemRarityPresentation.escapeHtml(ClientLanguage.getText("NOWY"))
				+ "</b></font>&nbsp; " + content(label, legendary) + "</div></html>";
	}

	private static String content(final String label, final boolean legendary) {
		if (legendary) return LegendaryAffixPresentation.labelHtml(label);
		final int split = label.indexOf(": ");
		return split < 0 ? ItemRarityPresentation.escapeHtml(label)
				: "<b>" + ItemRarityPresentation.escapeHtml(label.substring(0, split + 1))
						+ "</b> " + ItemRarityPresentation.escapeHtml(label.substring(split + 2));
	}
}
