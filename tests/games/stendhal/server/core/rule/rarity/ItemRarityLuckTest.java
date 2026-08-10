/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

/** Regression coverage for source-specific rarity luck. */
public class ItemRarityLuckTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void keepsBestTierFromTwoIndependentRolls() {
		final SequenceRandom random = new SequenceRandom(0.10, 0.95);
		final Item item = combatItem("default");

		new ItemRarityService(random).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT)
						.withRarityRolls(2)
						.randomizeModifiers(false)
						.build());

		assertSame(ItemRarity.EPIC, item.getRarity());
		assertEquals(2, random.calls);
	}

	@Test
	public void bonusRollsRespectItemSpecificRarityProfile() {
		final SequenceRandom random = new SequenceRandom(0.05, 0.50);
		final ItemRarityService service = new ItemRarityService(random);
		service.registerProfile(ItemRarityProfile.builder("elite-test")
				.tier(ItemRarity.COMMON, 10, 1.00, 1.00, 1.00)
				.tier(ItemRarity.RARE, 10, 1.05, 1.10, 1.20)
				.tier(ItemRarity.EPIC, 10, 1.10, 1.20, 1.50)
				.tier(ItemRarity.LEGENDARY, 70, 1.20, 1.35, 2.00)
				.build());
		final Item item = combatItem("elite-test");

		service.initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.DROP)
						.withRarityRolls(2)
						.randomizeModifiers(false)
						.generateAffixes(false)
						.build());

		assertSame(ItemRarity.LEGENDARY, item.getRarity());
		assertEquals("elite-test", item.get(Item.RARITY_PROFILE));
		assertEquals(2, random.calls);
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsNonPositiveRarityRollCount() {
		ItemCreationContext.builder(ItemCreationContext.Source.DROP)
				.withRarityRolls(0);
	}

	private Item combatItem(final String profile) {
		final HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "100");
		attributes.put("def", "50");
		attributes.put("rate", "10");
		final Item item = new Item("test sword", "sword", "test", attributes);
		item.setEquipableSlots(Arrays.asList("rhand", "bag"));
		item.configureRarity(null, profile, 1000);
		return item;
	}

	private static final class SequenceRandom extends Random {
		private static final long serialVersionUID = 1L;
		private final double[] values;
		private int index;
		private int calls;

		private SequenceRandom(final double... values) {
			this.values = values;
		}

		@Override
		public double nextDouble() {
			calls++;
			if (index >= values.length) {
				throw new AssertionError("Unexpected extra random roll");
			}
			return values[index++];
		}
	}
}
