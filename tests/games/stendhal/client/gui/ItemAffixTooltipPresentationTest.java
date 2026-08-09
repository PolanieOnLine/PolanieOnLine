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

public class ItemAffixTooltipPresentationTest {
	@Test
	public void expandedWeaponAffixesUseReadablePercentLabels() {
		final RPObject object = ItemTestHelper.createItem("affix tooltip sword");
		object.put("class", "sword");
		put(object, ItemTooltip.CATEGORY, ItemTooltip.CATEGORY_WEAPON);
		put(object, ItemTooltip.ATTACK, "30");
		put(object, ItemTooltip.DAMAGE_MIN, "27");
		put(object, ItemTooltip.DAMAGE_MAX, "33");
		put(object, ItemTooltip.ATTACKS_PER_SECOND, "1.0");
		put(object, ItemTooltip.CRITICAL_DAMAGE_BONUS, "0.20");
		put(object, ItemTooltip.BLEED_ON_HIT, "0.10");
		put(object, ItemTooltip.EXECUTE_DAMAGE, "0.25");
		put(object, ItemTooltip.POISON_ON_HIT, "0.08");
		put(object, ItemTooltip.DISTANCE_DAMAGE, "0.15");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains("+20% obrażeń trafienia krytycznego"));
		assertTrue(tooltip.contains("+10% szansy na krwawienie"));
		assertTrue(tooltip.contains("+25% obrażeń poniżej 25% PW celu"));
		assertTrue(tooltip.contains("+8% szansy na zatrucie"));
		assertTrue(tooltip.contains("+15% obrażeń z dystansu"));
	}

	private void put(final RPObject object, final String key,
			final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
