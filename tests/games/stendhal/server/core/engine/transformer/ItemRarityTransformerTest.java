/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
		final int damageMin = created.getInt("damage_min");
		final int damageMax = created.getInt("damage_max");
		final int rate = created.getInt("rate");
		final int value = created.getValue();
		created.put("min_level", definitionMinimumLevel + 1000);

		final Item firstLoad = new ItemTransformer().transform(serializedCopy(created));
		final Item secondLoad = new ItemTransformer().transform(serializedCopy(firstLoad));

		assertSame(ItemRarity.LEGENDARY, secondLoad.getRarity());
		assertEquals(attack, secondLoad.getInt("atk"));
		assertEquals(damageMin, secondLoad.getInt("damage_min"));
		assertEquals(damageMax, secondLoad.getInt("damage_max"));
		assertEquals(rate, secondLoad.getInt("rate"));
		assertEquals(value, secondLoad.getValue());
		assertEquals(created.getRarityModifiers(),
				secondLoad.getRarityModifiers());
		assertEquals(definitionMinimumLevel,
				secondLoad.has("min_level") ? secondLoad.getInt("min_level") : 0);
		assertFalse(secondLoad.isPersistent());
	}

	@Test
	public void legacyEligibleItemBecomesCommonAndReceivesDamageRange()
			throws IOException {
		final Item legacy = SingletonRepository.getEntityManager().getItem(
				ITEM_NAME, ItemCreationContext.restore());
		legacy.setID(new ID(102, "rarity_test"));
		legacy.put("atk", 137);
		legacy.put("rate", 4);
		legacy.setValue(777);

		final Item restored = new ItemTransformer().transform(serializedCopy(legacy));
		final int minimum = restored.getInt("damage_min");
		final int maximum = restored.getInt("damage_max");
		final Item secondLoad = new ItemTransformer().transform(serializedCopy(restored));

		assertSame(ItemRarity.COMMON, secondLoad.getRarity());
		assertEquals(137, secondLoad.getInt("atk"));
		assertTrue(minimum < 137);
		assertTrue(maximum > 137);
		assertEquals(137.0, restored.getAverageDamage(), 0.0);
		assertEquals(minimum, secondLoad.getInt("damage_min"));
		assertEquals(maximum, secondLoad.getInt("damage_max"));
		assertEquals(4, secondLoad.getInt("rate"));
		assertEquals(777, secondLoad.getValue());
		assertEquals(Double.valueOf(1.0), secondLoad.getRarityModifier("atk"));
		assertEquals(Double.valueOf(1.0),
				secondLoad.getRarityModifier("damage_min"));
		assertEquals(Double.valueOf(1.0),
				secondLoad.getRarityModifier("damage_max"));
		assertFalse(secondLoad.isPersistent());
	}

	@Test
	public void oldRarityItemWithoutRangeIsMigratedAndThenRemainsStable()
			throws IOException {
		final Item saved = SingletonRepository.getEntityManager().getItem(
				ITEM_NAME, ItemRarity.RARE,
				ItemRarityModifiers.builder().statMultiplier(1.10).build());
		saved.setID(new ID(104, "rarity_test"));
		saved.remove("damage_min");
		saved.remove("damage_max");

		final Item firstLoad = new ItemTransformer().transform(serializedCopy(saved));
		final int minimum = firstLoad.getInt("damage_min");
		final int maximum = firstLoad.getInt("damage_max");
		final Item secondLoad = new ItemTransformer().transform(serializedCopy(firstLoad));

		assertTrue(minimum < firstLoad.getInt("atk"));
		assertTrue(maximum > firstLoad.getInt("atk"));
		assertTrue(Math.abs(firstLoad.getAverageDamage()
				- firstLoad.getInt("atk")) <= 1.0);
		assertEquals(minimum, secondLoad.getInt("damage_min"));
		assertEquals(maximum, secondLoad.getInt("damage_max"));
		assertEquals(firstLoad.getRarityModifier("atk"),
				secondLoad.getRarityModifier("damage_min"));
		assertEquals(firstLoad.getRarityModifier("atk"),
				secondLoad.getRarityModifier("damage_max"));
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
