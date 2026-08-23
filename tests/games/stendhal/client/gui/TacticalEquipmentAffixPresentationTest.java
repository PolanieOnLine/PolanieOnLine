/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class TacticalEquipmentAffixPresentationTest {
	@Test
	public void tacticalArmorAffixesHaveReadablePolishDescriptions() {
		final RPObject object = ItemTestHelper.createItem("tactical tooltip armour");
		object.put("class", "armor");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_ARMOUR);
		put(object, ItemTooltip.DEFENSE, "45");
		put(object, ItemTooltip.SPIKED_PLATING, "0.03");
		put(object, ItemTooltip.HUNTER_MARK, "1.0");
		put(object, ItemTooltip.GIANT_SLAYER, "1.0");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("Kolczaste okucie"));
		assertTrue(tooltip.contains("3,0%"));
		assertTrue(tooltip.contains("Znak łowcy"));
		assertTrue(tooltip.contains("6 s"));
		assertTrue(tooltip.contains("Łowca olbrzymów"));
		assertTrue(tooltip.contains("50 poziomów"));
		assertTrue(tooltip.contains("10%"));
	}

	private void put(final RPObject object, final String key,
			final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
