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
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.damage.ParryService;
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
	public void productionRegistryCanFillLegendarySwordSlots() {
		final Item item = item("sword", ItemRarity.LEGENDARY);
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(17L));

		final List<String> applied = generator.generate(item,
				ItemCreationContext.drop());

		assertEquals(3, applied.size());
		assertTrue(applied.contains(ParryService.PARRY_CHANCE_ATTRIBUTE));
		assertTrue(applied.contains(WeaponAffixService.LIFESTEAL_ATTRIBUTE));
		assertTrue(applied.contains(WeaponAffixService.ACCURACY_ATTRIBUTE));
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
	public void nonDropAndCommonContextsDoNotGenerate() {
		final Item rare = item("sword", ItemRarity.RARE);
		final Item common = item("sword", ItemRarity.COMMON);
		final ItemAffixGenerator generator = new ItemAffixGenerator(new Random(3L));

		assertTrue(generator.generate(rare,
				ItemCreationContext.defaultCreation()).isEmpty());
		assertTrue(generator.generate(rare,
				ItemCreationContext.restore()).isEmpty());
		assertTrue(generator.generate(common,
				ItemCreationContext.drop()).isEmpty());
		assertFalse(ItemAffixState.hasAny(rare));
		assertFalse(ItemAffixState.hasAny(common));
	}

	@Test
	public void legendarySelectsThreeUniqueAffixesWhenPoolAllowsIt() {
		final Item item = item("sword", ItemRarity.LEGENDARY);
		final ItemAffixRegistry registry = new ItemAffixRegistry(Arrays.asList(
				new FixedAffix("first", "accuracy_bonus"),
				new FixedAffix("second", "critical_chance"),
				new FixedAffix("third", "lifesteal")));
		final ItemAffixGenerator generator =
				new ItemAffixGenerator(registry, new Random(19L));

		final List<String> applied = generator.generate(item,
				ItemCreationContext.drop());

		assertEquals(3, applied.size());
		assertEquals(3, new HashSet<String>(applied).size());
		assertEquals(3, ItemAffixState.getValues(item).size());
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
