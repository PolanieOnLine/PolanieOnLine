/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixGeneratorTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void slotCountMatchesRarity() {
		assertEquals(0, ItemAffixGenerator.getSlotCount(null));
		assertEquals(0, ItemAffixGenerator.getSlotCount(ItemRarity.COMMON));
		assertEquals(1, ItemAffixGenerator.getSlotCount(ItemRarity.RARE));
		assertEquals(2, ItemAffixGenerator.getSlotCount(ItemRarity.EPIC));
		assertEquals(3, ItemAffixGenerator.getSlotCount(ItemRarity.LEGENDARY));
		assertEquals(0, ItemAffixGenerator.getLegendarySlotCount(ItemRarity.EPIC));
		assertEquals(1, ItemAffixGenerator.getLegendarySlotCount(ItemRarity.LEGENDARY));
	}

	@Test
	public void productionRegistryMaterializesOneAffixForRareSword() {
		final Item item = item("sword", ItemRarity.RARE);
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(7L));

		final List<String> applied = generator.generate(item,
				ItemCreationContext.drop());

		assertEquals(1, applied.size());
		assertEquals(1, ItemAffixState.getValues(item).size());
		assertTrue(ItemAffixRegistry.getInstance().get(applied.get(0)) != null);
	}

	@Test
	public void productionPoolsCoverEverySupportedLegendaryFamily() {
		final ItemAffixRegistry regular = ItemAffixRegistry.getInstance();
		final LegendaryItemAffixRegistry legendary =
				LegendaryItemAffixRegistry.getInstance();
		final String[] classes = {"sword", "dagger", "axe", "club", "ranged",
				"wand", "whip", "armor", "shield", "helmet", "cloak",
				"boots", "glove", "legs", "belt", "ring", "necklace"};
		for (final String itemClass : classes) {
			final Item item = item(itemClass, ItemRarity.LEGENDARY);
			assertTrue("regular pool missing for " + itemClass,
					!regular.getEligible(item).isEmpty());
			assertTrue("legendary pool missing for " + itemClass,
					!legendary.getEligible(item).isEmpty());
		}
	}

	@Test
	public void missileIsOutsideRegularLegendaryAndGenerationPools() {
		final Item item = item("missile", ItemRarity.LEGENDARY);
		assertTrue(ItemAffixRegistry.getInstance().getEligible(item).isEmpty());
		assertTrue(LegendaryItemAffixRegistry.getInstance().getEligible(item).isEmpty());

		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(31L));
		assertTrue(generator.generate(item, ItemCreationContext.drop()).isEmpty());
		assertFalse(ItemAffixState.hasAny(item));
		assertFalse(ItemAffixState.hasSeed(item));
	}

	@Test
	public void legendarySwordGetsThreeRegularAndOneSignatureAffix() {
		assertLegendaryGetsFour("sword", 17L);
	}

	@Test
	public void legendaryArmourGetsThreeRegularAndOneSignatureAffix() {
		assertLegendaryGetsFour("armor", 23L);
	}

	@Test
	public void legendaryRingGetsThreeRegularAndOneSignatureAffix() {
		assertLegendaryGetsFour("ring", 29L);
	}

	@Test
	public void adminLegendaryGetsAffixesWithoutExplicitAffixFlag() {
		final Item item = item("sword", ItemRarity.LEGENDARY);
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(37L));

		final List<String> applied = generator.generate(item,
				ItemCreationContext.admin());

		assertEquals(4, applied.size());
		assertEquals(4, ItemAffixState.getValues(item).size());
	}

	@Test
	public void seededAffixGenerationIsReproducible() {
		final Item first = item("dagger", ItemRarity.LEGENDARY);
		final Item second = item("dagger", ItemRarity.LEGENDARY);
		final ItemCreationContext context = ItemCreationContext.builder(
				ItemCreationContext.Source.ADMIN)
				.generateAffixes(true).withAffixSeed(123456789L).build();
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(1L));

		generator.generate(first, context);
		generator.generate(second, context);

		assertEquals(ItemAffixState.getValues(first), ItemAffixState.getValues(second));
	}

	@Test
	public void existingAffixStateIsNeverRerolled() {
		final Item item = item("sword", ItemRarity.RARE);
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(11L));
		final List<String> first = generator.generate(item,
				ItemCreationContext.drop());
		final String affixId = first.get(0);
		final String original = ItemAffixState.getValues(item).get(affixId);

		assertTrue(generator.generate(item, ItemCreationContext.drop()).isEmpty());
		assertEquals(original, ItemAffixState.getValues(item).get(affixId));
	}

	@Test
	public void disabledAdminAndRestoreContextsDoNotGenerate() {
		final Item rare = item("sword", ItemRarity.RARE);
		final Item restored = item("sword", ItemRarity.RARE);
		final Item common = item("sword", ItemRarity.COMMON);
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(3L));

		assertTrue(generator.generate(rare, ItemCreationContext.admin()).isEmpty());
		assertTrue(generator.generate(restored,
				ItemCreationContext.restore()).isEmpty());
		assertTrue(generator.generate(common,
				ItemCreationContext.drop()).isEmpty());
		assertFalse(ItemAffixState.hasAny(rare));
		assertFalse(ItemAffixState.hasAny(restored));
		assertFalse(ItemAffixState.hasAny(common));
	}

	@Test
	public void injectedRegistriesProduceThreePlusOne() {
		final Item item = item("sword", ItemRarity.LEGENDARY);
		final ItemAffixRegistry regular = new ItemAffixRegistry(Arrays.asList(
				new FixedAffix("first", "accuracy_bonus"),
				new FixedAffix("second", "critical_chance"),
				new FixedAffix("third", "lifesteal")));
		final LegendaryItemAffixRegistry legendary =
				new LegendaryItemAffixRegistry(Arrays.<ItemAffixDefinition>asList(
						new FixedAffix("signature", "legendary_test")));
		final ItemAffixGenerator generator =
				new ItemAffixGenerator(regular, legendary, new Random(19L));

		final List<String> applied = generator.generate(item,
				ItemCreationContext.drop());

		assertEquals(4, applied.size());
		assertEquals(4, new HashSet<String>(applied).size());
		assertEquals(4, ItemAffixState.getValues(item).size());
		assertTrue(applied.contains("signature"));
	}

	private void assertLegendaryGetsFour(final String itemClass, final long seed) {
		final Item item = item(itemClass, ItemRarity.LEGENDARY);
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(seed));

		final List<String> applied = generator.generate(item,
				ItemCreationContext.drop());
		final Map<String, String> values = ItemAffixState.getValues(item);
		int regularCount = 0;
		int legendaryCount = 0;
		for (final String id : applied) {
			if (ItemAffixRegistry.getInstance().get(id) != null) {
				regularCount++;
			}
			if (LegendaryItemAffixRegistry.getInstance().get(id) != null) {
				legendaryCount++;
			}
		}

		assertEquals(4, applied.size());
		assertEquals(4, new HashSet<String>(applied).size());
		assertEquals(4, values.size());
		assertEquals(3, regularCount);
		assertEquals(1, legendaryCount);
	}

	private Item item(final String itemClass, final ItemRarity rarity) {
		final Item item = new Item("affix test item", itemClass, "test", null);
		item.setRarity(rarity);
		return item;
	}

	private static final class FixedAffix implements ItemAffixDefinition {
		private final String id;
		private final String attribute;

		FixedAffix(final String id, final String attribute) {
			this.id = id;
			this.attribute = attribute;
		}

		@Override
		public String getId() {
			return id;
		}

		@Override
		public String getAttribute() {
			return attribute;
		}

		@Override
		public boolean isEligible(final Item item) {
			return item != null && !item.has(attribute);
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			if (!isEligible(item)) {
				return false;
			}
			item.put(attribute, 1.0);
			return true;
		}
	}
}
