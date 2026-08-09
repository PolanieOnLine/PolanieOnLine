/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class LegendaryAffixPresentationTest {
	@Test
	public void deepWoundsUsesPolishOrangeTextCreamNumbersAndNoDiamond() {
		final RPObject object = ItemTestHelper.createItem("legendary sword");
		put(object, ItemTooltip.LEGENDARY_DEEP_WOUNDS, "1.0");

		final String html = LegendaryAffixPresentation.build(object);

		assertTrue(html.contains("Głębokie Rany:"));
		assertTrue(html.contains("15%"));
		assertTrue(html.contains("35%"));
		assertTrue(html.contains("#f28c28"));
		assertTrue(html.contains("#f3e2b8"));
		assertFalse(html.contains("&#9670;"));
		assertFalse(html.contains("◆"));
	}

	@Test
	public void allLegendaryTitlesArePolish() {
		final RPObject object = ItemTestHelper.createItem("legendary set");
		put(object, ItemTooltip.LEGENDARY_DEEP_WOUNDS, "1.0");
		put(object, ItemTooltip.LEGENDARY_ARMOR_BREAKER, "1.0");
		put(object, ItemTooltip.LEGENDARY_LONGSHOT, "1.0");
		put(object, ItemTooltip.LEGENDARY_EXECUTIONER, "1.0");
		put(object, ItemTooltip.LEGENDARY_BASTION_BONUS, "24");
		put(object, ItemTooltip.LEGENDARY_RELIC_POWER, "6");

		final String html = LegendaryAffixPresentation.build(object);

		assertTrue(html.contains("Głębokie Rany:"));
		assertTrue(html.contains("Łamacz Pancerzy:"));
		assertTrue(html.contains("Dalekosiężność:"));
		assertTrue(html.contains("Egzekutor:"));
		assertTrue(html.contains("Niezłomny Bastion:"));
		assertTrue(html.contains("Relikt Mocy:"));
		assertTrue(html.contains("+24"));
		assertTrue(html.contains("+6"));
	}

	private void put(final RPObject object, final String key, final String value) {
		object.put(ItemTooltip.ATTRIBUTE, key, value);
	}
}
