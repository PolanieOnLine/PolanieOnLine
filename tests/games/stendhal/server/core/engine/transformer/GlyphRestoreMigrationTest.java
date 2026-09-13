/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.transformer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.DetailLevel;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.net.InputSerializer;
import marauroa.common.net.OutputSerializer;
import utilities.RPClass.ItemTestHelper;

public class GlyphRestoreMigrationTest {
	@BeforeClass
	public static void setUpClasses() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void existingGlyphRefreshesDefinitionStatsAndDropsLegacyRarity()
			throws IOException {
		final Item current = SingletonRepository.getEntityManager().getItem(
				"glif kryzysu", ItemCreationContext.restore());
		final double currentCriticalChance = current.getDouble("critical_chance");
		final String currentDescription = current.getDescription();

		final Item saved = SingletonRepository.getEntityManager().getItem(
				"glif kryzysu", ItemCreationContext.restore());
		saved.setID(new ID(201, "glyph_restore_test"));
		saved.put("critical_chance", 3.0);
		saved.put("description", "stary opis glifu");
		saved.put(Item.RARITY_ID, "rare");
		saved.put(Item.RARITY_PROFILE, "default");
		saved.put(Item.RARITY_MODIFIERS, "critical_chance", "1.10");

		final Item restored = new ItemTransformer().transform(serializedCopy(saved));

		assertEquals(currentCriticalChance,
				restored.getDouble("critical_chance"), 0.0);
		assertEquals(currentDescription, restored.getDescription());
		assertFalse(restored.has(Item.RARITY_ID));
		assertFalse(restored.hasMap(Item.RARITY_MODIFIERS));
		assertNull(restored.getRarity());
	}

	@Test
	public void strzybogRestorePublishesCurrentRateReductionWithoutRarity()
			throws IOException {
		final Item saved = SingletonRepository.getEntityManager().getItem(
				"glif Strzyboga", ItemCreationContext.restore());
		saved.setID(new ID(202, "glyph_restore_test"));
		saved.put("rate_increase", 7);
		saved.put(Item.RARITY_ID, "rare");
		saved.put(Item.RARITY_PROFILE, "default");

		final Item restored = new ItemTransformer().transform(serializedCopy(saved));

		assertEquals(1, restored.getInt("rate_increase"));
		assertEquals("1", restored.getMap(ItemTooltip.ATTRIBUTE)
				.get(ItemTooltip.RATE_INCREASE));
		assertFalse(restored.has(Item.RARITY_ID));
		assertNull(restored.getRarity());
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
