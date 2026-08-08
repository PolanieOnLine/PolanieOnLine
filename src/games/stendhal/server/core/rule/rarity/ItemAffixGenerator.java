/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;

/** Selects unique random affixes according to item rarity. */
public final class ItemAffixGenerator {
	private final ItemAffixRegistry registry;
	private final Random random;

	public ItemAffixGenerator(final Random random) {
		this(ItemAffixRegistry.getInstance(), random);
	}

	ItemAffixGenerator(final ItemAffixRegistry registry, final Random random) {
		if (registry == null) {
			throw new IllegalArgumentException("Affix registry must not be null");
		}
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		this.registry = registry;
		this.random = random;
	}

	/**
	 * Returns the target number of random affixes for one rarity tier.
	 */
	public static int getSlotCount(final ItemRarity rarity) {
		if (rarity == null) {
			return 0;
		}
		switch (rarity) {
		case RARE:
			return 1;
		case EPIC:
			return 2;
		case LEGENDARY:
			return 3;
		case COMMON:
		default:
			return 0;
		}
	}

	/**
	 * Generates random affixes for a fresh drop only. Existing persisted affix
	 * state is never rerolled. The current production registry may expose fewer
	 * eligible definitions than the rarity has slots; in that case only the
	 * available unique definitions are applied.
	 *
	 * This method is intentionally not wired into ItemRarityService yet. The
	 * first live pool must contain several real affixes so a rare sword is not
	 * forced to roll parry just because parry is currently the sole definition.
	 */
	public List<String> generate(final Item item,
			final ItemCreationContext context) {
		if (item == null || context == null
				|| context.getSource() != ItemCreationContext.Source.DROP
				|| ItemAffixState.hasAny(item)) {
			return Collections.emptyList();
		}

		final int slots = getSlotCount(item.getRarity());
		if (slots <= 0) {
			return Collections.emptyList();
		}

		final List<ItemAffixDefinition> eligible = registry.getEligible(item);
		if (eligible.isEmpty()) {
			return Collections.emptyList();
		}
		Collections.shuffle(eligible, random);

		final List<String> applied = new ArrayList<String>();
		for (final ItemAffixDefinition definition : eligible) {
			if (applied.size() >= slots) {
				break;
			}
			if (definition.apply(item, random)) {
				ItemAffixState.record(item, definition);
				applied.add(definition.getId());
			}
		}
		return Collections.unmodifiableList(applied);
	}
}
