package games.stendhal.server.core.rule.rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Status resistances share one quarter of ordinary selections while alternatives exist. */
final class OrdinaryAffixSelection {
	private OrdinaryAffixSelection() { }

	static List<ItemAffixDefinition> order(final List<ItemAffixDefinition> eligible, final Random random) {
		final List<ItemAffixDefinition> resistances = new ArrayList<>();
		final List<ItemAffixDefinition> others = new ArrayList<>();
		for (final ItemAffixDefinition definition : eligible) {
			(definition.getId().startsWith("resist_") ? resistances : others).add(definition);
		}
		final List<ItemAffixDefinition> ordered = new ArrayList<>();
		while (!resistances.isEmpty() || !others.isEmpty()) {
			final boolean resistance = others.isEmpty()
					|| (!resistances.isEmpty() && random.nextInt(4) == 0);
			final List<ItemAffixDefinition> family = resistance ? resistances : others;
			ordered.add(family.remove(random.nextInt(family.size())));
		}
		return ordered;
	}
}
