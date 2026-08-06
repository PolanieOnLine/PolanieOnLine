/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.factory.EntityFactory;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemRarityPresentationTest {
	@Test
	public void testEveryRarityHasColorAndTextLabel() {
		assertRarityToolTip("common", "#9e9e9e", "Zwykły");
		assertRarityToolTip("rare", "#4a90e2", "Rzadki");
		assertRarityToolTip("epic", "#9b59b6", "Epicki");
		assertRarityToolTip("legendary", "#ff8c00", "Legendarny");
	}

	@Test
	public void testMissingAndUnknownRarityAreSafe() {
		RPObject object = ItemTestHelper.createItem("legacy item");
		IEntity item = EntityFactory.createEntity(object);
		assertNull(ItemRarityPresentation.buildItemToolTip(item));

		object = ItemTestHelper.createItem("unknown item");
		object.put("rarity_id", "mythical");
		item = EntityFactory.createEntity(object);
		assertNull(ItemRarityPresentation.buildItemToolTip(item));
	}

	@Test
	public void testRarityToolTipEscapesItemName() {
		final RPObject object = ItemTestHelper.createItem("sword <prototype>");
		object.put("rarity_id", "rare");
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("SWORD &lt;PROTOTYPE&gt;"));
	}

	@Test
	public void testWeaponToolTipShowsStructuredPerformanceAndBonuses() {
		final RPObject object = ItemTestHelper.createItem("miecz próbny");
		object.put("rarity_id", "legendary");
		object.put("atk", 30);
		object.put("rate", 5);
		object.put("range", 1);
		object.put("critical_chance", 4.0);
		object.put("lifesteal", 2.0);
		object.put("health", 15);
		object.put("min_level", 20);
		object.put("value", 382);

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("Bazowy DPS"));
		assertTrue(tooltip.contains("20,0"));
		assertTrue(tooltip.contains("30 pkt. ataku"));
		assertTrue(tooltip.contains("0,67 ataku na sekundę"));
		assertTrue(tooltip.contains("1,50 s między atakami"));
		assertTrue(tooltip.contains("+4% szansy na trafienie krytyczne"));
		assertTrue(tooltip.contains("+2% kradzieży życia"));
		assertTrue(tooltip.contains("+15 zdrowia"));
		assertTrue(tooltip.contains("Wymagany poziom: 20"));
		assertTrue(tooltip.contains("Wartość: 382"));
	}

	@Test
	public void testWeaponWithoutRarityStillGetsPerformanceToolTip() {
		final RPObject object = ItemTestHelper.createItem("zwykły miecz");
		object.put("atk", 15);
		object.put("rate", 5);

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("Bazowy DPS"));
		assertTrue(tooltip.contains("10,0"));
	}

	private void assertRarityToolTip(final String rarityId, final String color,
			final String displayName) {
		final RPObject object = ItemTestHelper.createItem("test sword");
		object.put("rarity_id", rarityId);
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains(color));
		assertTrue(tooltip.contains("TEST SWORD"));
		assertTrue(tooltip.contains(displayName));
	}
}
