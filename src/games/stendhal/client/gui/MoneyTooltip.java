/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client.gui;

import games.stendhal.client.ClientLanguage;
import games.stendhal.common.grammar.Grammar;

/** Shared money description for the carried-money panel and individual stacks. */
public final class MoneyTooltip {
	private MoneyTooltip() {
		// utility class
	}

	public static String format(final long amount) {
		final long quantity = Math.max(0L, amount);
		// Beyond the int range, retain the last two digits without creating a singular.
		final int grammarQuantity = quantity <= Integer.MAX_VALUE
				? (int) quantity : 100 + (int) (quantity % 100);
		final String coins = ClientLanguage.getText(quantity + " "
				+ Grammar.polishQuantity("money", grammarQuantity));
		return ClientLanguage.getText("Łącznie: " + coins);
	}
}
