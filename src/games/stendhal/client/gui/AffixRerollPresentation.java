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
		return " <img src='" + DataLoader.getResource("data/gui/affix_rerolled.png")
				+ "' width='12' height='12' alt='" + ClientLanguage.getText("Afiks wybrany do przemiany") + "'>";
	}

	static String labelHtml(final String label, final boolean marked, final int width) {
		return labelHtml(label, marked, width, "");
	}

	static String labelHtml(final String label, final boolean marked, final int width, final String caption) {
		final String escaped = ItemRarityPresentation.escapeHtml(label);
		return "<html><div style='width:" + width + "px'>" + ItemRarityPresentation.escapeHtml(caption) + escaped
				+ (marked ? icon() : "") + "</div></html>";
	}
}
