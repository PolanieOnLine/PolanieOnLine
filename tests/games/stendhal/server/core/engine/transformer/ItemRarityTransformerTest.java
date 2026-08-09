/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityModifiers;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.RPClass.ItemTestHelper;

public class ItemRarityTransformerTest {
	private static final String ITEM_NAME = "miecz zaczepny";

	@BeforeClass
	public static void setUpClasses() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void savedFinalStatsAndModifiersAreRestoredWithoutReapplication()
			throws IOException {
		final EntityManager manager = SingletonRepository.getEntityManager();
		final Item definition = manager.getItem(ITEM_NAME,
				ItemCreationContext.restore());
		final int definitionMinimumLevel = definition.has("min_level")
				? definition.getInt("min_level") : 0;
		final ItemRarityModifiers modifiers = ItemRarityModifiers.builder()
				.attackMultiplier(1.30)
				.speedMultiplier(1.20)
				.valueMultiplier(2.0)
				.build();
		final Item created = manager.getItem(ITEM_NAME, ItemRarity.LEGENDARY,
				modifiers);
		created.setID(new ID(101, "rarity_test"));
		final int attack = created.getInt("atk");
		final int rate = created.getInt("rate");
		final int value = created.getValue();
		created.put("min_level", definitionMinimumLevel + 1000);

		final Item firstLoad = new ItemTransformer().transform(serializedCopy(created));
		final Item secondLoad = new ItemTransformer().transform(serializedCopy(firstLoad));

		assertSame(ItemRarity.LEGENDARY, secondLoad.getRarity());
		assertEquals(attack, secondLoad.getInt("atk"));
		assertEquals(rate, secondLoad.getInt("rate"));
		assertEquals(value, secondLoad.getValue());
		assertEquals(created.getRarityModifiers(),
				secondLoad.getRarityModifiers());
		assertEquals(definitionMinimumLevel,
				secondLoad.has("min_level") ? secondLoad.getInt("min_level") : 0);
		assertFalse(secondLoad.isPersistent());
	}

	@Test
	public void legacyEligibleItemBecomesCommonWithoutChangingSavedValues()
			throws IOException {
		final Item legacy = SingletonRepository.getEntityManager().getItem(
				ITEM_NAME, ItemCreationContext.restore());
		legacy.setID(new ID(102, "rarity_test"));
		legacy.put("atk", 137);
		legacy.put("rate", 4);
		legacy.setValue(777);

		final Item restored = new ItemTransformer().transform(serializedCopy(legacy));

		assertSame(ItemRarity.COMMON, restored.getRarity());
		assertEquals(137, restored.getInt("atk"));
		assertEquals(4, restored.getInt("rate"));
		assertEquals(777, restored.getValue());
		assertEquals(Double.valueOf(1.0), restored.getRarityModifier("atk"));
		assertFalse(restored.isPersistent());
	}

	@Test
	public void savedStatisticSetRemainsAuthoritativeAcrossXmlChanges()
			throws IOException {
		final Item saved = SingletonRepository.getEntityManager().getItem(
				ITEM_NAME, ItemRarity.RARE,
				ItemRarityModifiers.builder().statMultiplier(1.10).build());
		saved.setID(new ID(103, "rarity_test"));

		// Simulate an old definition which had def but did not yet have rate.
		saved.put("def", 42);
		saved.remove("rate");

		final Item restored = new ItemTransformer().transform(serializedCopy(saved));

		assertEquals(42, restored.getInt("def"));
		assertFalse(restored.has("rate"));
	}

	private RPObject serializedCopy(final RPObject source) throws IOException {
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		source.writeObject(new OutputSerializer(bytes), DetailLevel.FULL);
		final RPObject copy = new RPObject();
		copy.readObject(new InputSerializer(
				new ByteArrayInputStream(bytes.toByteArray())));
		return copy;
	}
}
