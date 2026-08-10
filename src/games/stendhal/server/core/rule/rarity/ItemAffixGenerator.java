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
	private static final String EXCLUDED_MISSILE_CLASS = "missile";

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

	/** Returns the target number of regular random affixes for one rarity tier. */
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

	/** Legendary rarity always owns one extra signature slot. */
	public static int getLegendarySlotCount(final ItemRarity rarity) {
		return rarity == ItemRarity.LEGENDARY ? 1 : 0;
	}

	/**
	 * Generates affixes for fresh creation contexts which explicitly enable the
	 * affix layer. Administrator-created legendary items are the one deliberate
	 * exception: if an admin summon resolves to legendary rarity, its three
	 * regular affixes and mandatory signature are generated even when rarity was
	 * not explicitly forced by the command. Existing persisted state is never
	 * rerolled. Missile-class projectiles are ammunition and never participate
	 * in the item affix system, even if a rarity was assigned manually.
	 */
	public List<String> generate(final Item item,
			final ItemCreationContext context) {
		if (item == null || EXCLUDED_MISSILE_CLASS.equals(item.getItemClass())
				|| context == null || !shouldGenerate(item, context)
				|| ItemAffixState.hasAny(item)) {
			return Collections.emptyList();
		}

		final int regularSlots = getSlotCount(item.getRarity());
		final int legendarySlots = getLegendarySlotCount(item.getRarity());
		if (regularSlots <= 0 && legendarySlots <= 0) {
			return Collections.emptyList();
		}

		final long seed = context.getAffixSeed() == null
				? random.nextLong() : context.getAffixSeed().longValue();
		ItemAffixState.setSeed(item, seed);
		final Random rollRandom = new Random(seed);
		final List<String> applied = new ArrayList<String>();
		applyRegularAffixes(item, regularSlots, applied, rollRandom);

		if (legendarySlots > 0
				&& !applyLegendarySignature(item, applied, rollRandom)) {
			throw new IllegalStateException(
					"Legendary item has no eligible signature affix: "
							+ item.getItemClass() + "/" + item.getName());
		}
		return Collections.unmodifiableList(applied);
	}

	private boolean shouldGenerate(final Item item,
			final ItemCreationContext context) {
		return context.isGenerateAffixes()
				|| (context.getSource() == ItemCreationContext.Source.ADMIN
						&& item.getRarity() == ItemRarity.LEGENDARY);
	}

	private void applyRegularAffixes(final Item item, final int slots,
			final List<String> applied, final Random rollRandom) {
		if (slots <= 0) {
			return;
		}
		final List<ItemAffixDefinition> eligible = registry.getEligible(item);
		Collections.shuffle(eligible, rollRandom);
		int regularApplied = 0;
		for (final ItemAffixDefinition definition : eligible) {
			if (regularApplied >= slots) {
				break;
			}
			if (definition.apply(item, rollRandom)) {
				ItemAffixState.record(item, definition);
				applied.add(definition.getId());
				regularApplied++;
			}
		}
	}

	private boolean applyLegendarySignature(final Item item,
			final List<String> applied, final Random rollRandom) {
		final List<ItemAffixDefinition> eligible =
				legendaryRegistry.getEligible(item);
		Collections.shuffle(eligible, rollRandom);
		for (final ItemAffixDefinition definition : eligible) {
			if (definition.apply(item, rollRandom)) {
				ItemAffixState.record(item, definition);
				applied.add(definition.getId());
				return true;
			}
		}
		return false;
	}
}
