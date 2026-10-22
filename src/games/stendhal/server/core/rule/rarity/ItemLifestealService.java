/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import games.stendhal.server.entity.item.Item;

/** Life-steal units and calculation; unrelated glyph bonuses stay separate. */
public final class ItemLifestealService {
	public static final String EFFECTIVENESS = "lifesteal_effectiveness";
	public static final String VERSION_ATTRIBUTE = "lifesteal_version";
	public static final String LEGACY_ATTRIBUTE = "lifesteal_legacy";
	public static final int VERSION = 1;

	private ItemLifestealService() { }

	public static boolean isUnscaled(final String attribute) {
		return "lifesteal".equals(attribute) || EFFECTIVENESS.equals(attribute);
	}

	/** Returns a fraction, not percentage points, without early rounding. */
	public static double getEffectiveLifesteal(final Item item) {
		if (item == null || !item.has("lifesteal")) return 0.0;
		final double fraction = item.getDouble("lifesteal");
		if (fraction <= 0.0 || !item.has(EFFECTIVENESS)) return fraction;
		return fraction * (1.0 + item.getInt(EFFECTIVENESS) / 100.0);
	}
}
