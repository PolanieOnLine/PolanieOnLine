/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.rule.damage.WallOfGordService;
import utilities.RPClass.ItemTestHelper;

public class WallOfGordTooltipStateTest {
	@BeforeClass
	public static void setUpClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void wallOfGordMarkerIsPublishedWithoutChangingDisplayedDefense() {
		final HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("def", "50");
		final Item armor = new Item("wall of the gord tooltip", "armor", "test",
				attributes);
		armor.put(WallOfGordService.ATTRIBUTE, 1.0);

		ItemTooltipService.update(armor);

		assertEquals("50", armor.getMap(ItemTooltip.ATTRIBUTE).get(ItemTooltip.DEFENSE));
		assertEquals("1.0", armor.getMap(ItemTooltip.ATTRIBUTE).get(
				ItemTooltip.LEGENDARY_WALL_OF_GORD));
	}
}
