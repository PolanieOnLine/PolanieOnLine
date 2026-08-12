/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemRollMaterializationTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void randomRarityModifierAndFloatingResultUseTwoDecimalPrecision() {
		final Item item = combatItem();
		item.put("lifesteal", 0.10);
		final ItemRarityService service =
				new ItemRarityService(new FixedDoubleRandom(1.0 / 3.0));

		service.initialize(item, ItemCreationContext
				.builder(ItemCreationContext.Source.DEFAULT)
				.withRarity(ItemRarity.RARE).build());

		assertEquals(1.07, item.getRarityModifier("atk").doubleValue(), 0.0);
		assertEquals(1.07, item.getRarityModifier("def").doubleValue(), 0.0);
		assertEquals(1.07, item.getRarityModifier("rate").doubleValue(), 0.0);
		assertEquals(1.07, item.getRarityModifier("lifesteal").doubleValue(), 0.0);
		assertEquals(0.11, item.getDouble("lifesteal"), 0.0);
		assertEquals("1.07", item.getMap(Item.RARITY_MODIFIERS).get("lifesteal"));
		assertEquals("0.11", ItemAffixState.getValues(item).get("lifesteal"));
	}

	@Test
	public void fixedModifierIsRoundedBeforeItIsAppliedAndPersisted() {
		final Item item = combatItem();
		item.put("lifesteal", 0.10);
		final ItemRarityModifiers modifiers = ItemRarityModifiers.builder()
				.statMultiplier(1.2695127077879125).build();

		new ItemRarityService(new Random(1L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withRarity(ItemRarity.RARE)
						.withModifiers(modifiers).build());

		assertEquals(127, item.getInt("atk"));
		assertEquals(1.27, item.getRarityModifier("atk").doubleValue(), 0.0);
		assertEquals(1.27, item.getRarityModifier("lifesteal").doubleValue(), 0.0);
		assertEquals(0.13, item.getDouble("lifesteal"), 0.0);
	}

	@Test
	public void commonSuppressesPositiveIntrinsicLifesteal() {
		final Item item = combatItem();
		final double intrinsic = 0.12512031523088826;
		item.put("lifesteal", intrinsic);

		new ItemRarityService(new Random(1L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withRarity(ItemRarity.COMMON).build());

		assertFalse(item.has("lifesteal"));
		assertFalse(item.getRarityModifiers().containsKey("lifesteal"));
	}

	private Item combatItem() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "100");
		attributes.put("def", "50");
		attributes.put("rate", "10");
		final Item item = new Item("roll precision sword", "sword", "test", attributes);
		item.setEquipableSlots(Arrays.asList("rhand", "bag"));
		item.configureRarity(null, "default", 1000);
		return item;
	}

	private static final class FixedDoubleRandom extends Random {
		private static final long serialVersionUID = 1L;
		private final double value;

		private FixedDoubleRandom(final double value) {
			this.value = value;
		}

		@Override
		public double nextDouble() {
			return value;
		}
	}
}
