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

public class GlyphTooltipPresentationTest {
	@Test
	public void strzybogGlyphExplainsItsAttackRateReductionInFullTooltip() {
		final RPObject object = ItemTestHelper.createItem("glif Strzyboga");
		object.put("class", "glyph");
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.CATEGORY,
				ItemTooltip.CATEGORY_OTHER);
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.RATE_INCREASE, "1");

		final String tooltip = ItemRarityPresentation.buildItemToolTip(
				EntityFactory.createEntity(object));

		assertTrue(tooltip.contains(
				"&#9670; -1 do wagi broni"));
	}
}
