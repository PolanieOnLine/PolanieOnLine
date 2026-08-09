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
	private final LegendaryItemAffixRegistry legendaryRegistry;
	private final Random random;

	public ItemAffixGenerator(final Random random) {
		this(ItemAffixRegistry.getInstance(),
				LegendaryItemAffixRegistry.getInstance(), random);
	}

	ItemAffixGenerator(final ItemAffixRegistry registry, final Random random) {
		this(registry,
				new LegendaryItemAffixRegistry(
						Collections.<ItemAffixDefinition>emptyList()),
				random);
	}

	ItemAffixGenerator(final ItemAffixRegistry registry,
			final LegendaryItemAffixRegistry legendaryRegistry,
			final Random random) {
		if (registry == null) {
			throw new IllegalArgumentException("Affix registry must not be null");
		}
		if (legendaryRegistry == null) {
			throw new IllegalArgumentException(
					"Legendary affix registry must not be null");
		}
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		this.registry = registry;
		this.legendaryRegistry = legendaryRegistry;
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
	 * state is never rerolled. Legendary items first try to reserve one of their
	 * three slots for a signature legendary-only effect; if no signature is
	 * eligible for the item class yet, the slot falls back to the normal pool.
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

		final List<String> applied = new ArrayList<String>();
		if (item.getRarity() == ItemRarity.LEGENDARY) {
			applyLegendarySignature(item, applied);
		}

		final List<ItemAffixDefinition> eligible = registry.getEligible(item);
		Collections.shuffle(eligible, random);
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

	private void applyLegendarySignature(final Item item,
			final List<String> applied) {
		final List<ItemAffixDefinition> eligible =
				legendaryRegistry.getEligible(item);
		Collections.shuffle(eligible, random);
		for (final ItemAffixDefinition definition : eligible) {
			if (definition.apply(item, random)) {
				ItemAffixState.record(item, definition);
				applied.add(definition.getId());
				return;
			}
		}
	}
}
