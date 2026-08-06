/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import utilities.RPClass.ItemTestHelper;

public class ItemTooltipServiceTest {
	@BeforeClass
	public static void setUpClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void testWeaponPublishesCategoryAndTiming() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "2");
		final Item item = new Item("test axe", "axe", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals(ItemTooltip.CATEGORY_WEAPON,
				stat(item, ItemTooltip.CATEGORY));
		assertEquals(0.6, Double.parseDouble(stat(item,
				ItemTooltip.ATTACK_INTERVAL_SECONDS)), 0.0001);
		assertEquals(5.0 / 3.0, Double.parseDouble(stat(item,
				ItemTooltip.ATTACKS_PER_SECOND)), 0.0001);
	}

	@Test
	public void testPluralBeltsClassIsArmour() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("def", "21");
		final Item item = new Item("test belt", "belts", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals(ItemTooltip.CATEGORY_ARMOUR,
				stat(item, ItemTooltip.CATEGORY));
	}

	@Test
	public void testRingAndNecklaceAreAccessories() {
		assertCategory("ring", ItemTooltip.CATEGORY_ACCESSORY);
		assertCategory("necklace", ItemTooltip.CATEGORY_ACCESSORY);
	}

	@Test
	public void testUnknownClassIsOther() {
		assertCategory("container", ItemTooltip.CATEGORY_OTHER);
	}

	private void assertCategory(final String itemClass,
			final String expectedCategory) {
		final Item item = new Item("test item", itemClass, "test", null);
		ItemTooltipService.update(item);
		assertEquals(expectedCategory, stat(item, ItemTooltip.CATEGORY));
	}

	private String stat(final Item item, final String key) {
		return item.getMap(ItemTooltip.ATTRIBUTE).get(key);
	}
}
