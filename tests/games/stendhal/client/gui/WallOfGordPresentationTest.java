/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class WallOfGordPresentationTest {
	@Test
	public void wallOfGordShowsExactTriggerCooldownAndReduction() {
		final RPObject object = ItemTestHelper.createItem("wall of the gord tooltip");
		object.put(ItemTooltip.ATTRIBUTE, ItemTooltip.LEGENDARY_WALL_OF_GORD, "1.0");

		final String html = LegendaryAffixPresentation.build(object);

		assertTrue(html.contains("Wał grodu:"));
		assertTrue(html.contains("8 s"));
		assertTrue(html.contains("10%"));
		assertTrue(html.contains("35%"));
		assertTrue(html.contains("bezpośrednie trafienie stworzenia"));
	}
}
