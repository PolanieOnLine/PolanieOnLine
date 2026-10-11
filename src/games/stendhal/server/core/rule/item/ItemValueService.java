package games.stendhal.server.core.rule.item;

import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.entity.item.Item;

/** Refreshes prices after XML value changes without rerolling saved equipment. */
public final class ItemValueService {
	private ItemValueService() { }

	public static void refresh(final Item item) {
		final Double saved = item.getRarityModifier(ItemRarityModifiers.VALUE);
		final double multiplier = saved != null && Double.isFinite(saved.doubleValue())
				&& saved.doubleValue() > 0 ? saved.doubleValue() : 1.0;
		item.setValue((int) Math.min(Integer.MAX_VALUE,
				Math.max(0L, Math.round(item.getDefinitionValue() * multiplier))));
	}
}
