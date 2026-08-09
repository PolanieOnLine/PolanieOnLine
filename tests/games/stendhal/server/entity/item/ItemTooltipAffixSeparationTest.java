/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import utilities.RPClass.ItemTestHelper;

public class ItemTooltipAffixSeparationTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void accessoryCoreAttackExcludesRegularAndLegendaryFlatAffixes() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "9");
		attributes.put(ItemTooltip.FLAT_ATTACK_BONUS, "1");
		attributes.put(ItemTooltip.LEGENDARY_RELIC_POWER, "5");
		final Item item = new Item("mithril ring", "ring", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals("3", stat(item, ItemTooltip.ATTACK));
		assertEquals("1", stat(item, ItemTooltip.AFFIX_FLAT_ATTACK_BONUS));
		assertEquals("5", stat(item, ItemTooltip.LEGENDARY_RELIC_POWER));
		assertFalse(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
				ItemTooltip.FLAT_ATTACK_BONUS));
		assertEquals(9, item.getInt("atk"));
	}

	@Test
	public void armourCoreDefenseExcludesRegularAndLegendaryFlatAffixes() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("def", "42");
		attributes.put(ItemTooltip.FLAT_DEFENSE_BONUS, "4");
		attributes.put(ItemTooltip.LEGENDARY_BASTION_BONUS, "10");
		final Item item = new Item("legendary armour", "armor", "test", attributes);

		ItemTooltipService.update(item);

		assertEquals("28", stat(item, ItemTooltip.DEFENSE));
		assertEquals("4", stat(item, ItemTooltip.AFFIX_FLAT_DEFENSE_BONUS));
		assertEquals("10", stat(item, ItemTooltip.LEGENDARY_BASTION_BONUS));
		assertFalse(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
				ItemTooltip.FLAT_DEFENSE_BONUS));
		assertEquals(42, item.getInt("def"));
	}

	private String stat(final Item item, final String key) {
		return item.getMap(ItemTooltip.ATTRIBUTE).get(key);
	}
}
