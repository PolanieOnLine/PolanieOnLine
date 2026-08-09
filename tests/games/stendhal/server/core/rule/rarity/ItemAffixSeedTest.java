/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.Definition;
import marauroa.common.game.RPClass;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixSeedTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void explicitSeedIsPersistedOnGeneratedItem() {
		final Item item = sword(ItemRarity.RARE);
		final ItemCreationContext context = ItemCreationContext.builder(
				ItemCreationContext.Source.ADMIN)
				.generateAffixes(true).withAffixSeed(123456789L).build();

		new ItemAffixGenerator(new Random(1L)).generate(item, context);

		assertEquals(Long.valueOf(123456789L), ItemAffixState.getSeed(item));
	}

	@Test
	public void automaticSeedCanReplayTheCompleteAffixRoll() {
		final Item first = sword(ItemRarity.RARE);
		new ItemAffixGenerator(new Random(77L)).generate(first,
				ItemCreationContext.drop());
		final Long seed = ItemAffixState.getSeed(first);
		assertNotNull(seed);

		final Item second = sword(ItemRarity.RARE);
		final ItemCreationContext replay = ItemCreationContext.builder(
				ItemCreationContext.Source.ADMIN).generateAffixes(true)
				.withAffixSeed(seed.longValue()).build();
		new ItemAffixGenerator(new Random(999L)).generate(second, replay);

		assertEquals(seed, ItemAffixState.getSeed(second));
		assertEquals(ItemAffixState.getValues(first),
				ItemAffixState.getValues(second));
	}

	@Test
	public void restorePreservesAffixSeedWithoutRerolling() {
		final Item saved = sword(ItemRarity.RARE);
		ItemAffixState.setSeed(saved, -987654321L);
		final Item restored = sword(ItemRarity.RARE);

		ItemAffixState.restore(restored, saved);

		assertEquals(Long.valueOf(-987654321L), ItemAffixState.getSeed(restored));
	}

	@Test
	public void affixSeedIsDeclaredOnItemRPClass() {
		final Set<String> names = new HashSet<String>();
		for (final Definition definition
				: RPClass.getRPClass("item").getDefinitions()) {
			names.add(definition.getName());
		}
		assertTrue(names.contains(ItemAffixState.SEED_ATTRIBUTE));
	}

	private Item sword(final ItemRarity rarity) {
		final Item item = new Item("seed test sword", "sword", "test", null);
		item.setRarity(rarity);
		return item;
	}
}
