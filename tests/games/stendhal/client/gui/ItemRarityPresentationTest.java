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

		assertTrue(tooltip.contains("sword &lt;prototype&gt;"));
	}

	private void assertRarityToolTip(final String rarityId, final String color,
			final String displayName) {
		final RPObject object = ItemTestHelper.createItem("test sword");
		object.put("rarity_id", rarityId);
		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains(color));
		assertTrue(tooltip.contains("test sword"));
		assertTrue(tooltip.contains("Rzadkość: " + displayName));
	}
}
