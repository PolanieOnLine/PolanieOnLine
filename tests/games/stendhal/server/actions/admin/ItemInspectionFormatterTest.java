/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemInspectionFormatterTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void roundsDiagnosticNumbersWithoutChangingStoredValues() {
		final Item item = new Item("inspection axe", "axe", "test", null);
		item.put("lifesteal", 0.12512031523088826);
		item.put(Item.RARITY_MODIFIERS, "lifesteal", "1.2512031523088827");
		item.put(ItemAffixState.ATTRIBUTE, "armor_penetration", "0.16666");
		item.put(ItemTooltip.ATTRIBUTE, "attacks_per_second",
				"1.6666666666666667");
		ItemAffixState.setSeed(item, 5837491283751L);

		final String text = ItemInspectionFormatter.format(item);

		assertTrue(text.contains("Seed afiksów: 5837491283751"));
		assertTrue(text.contains("lifesteal=0.13"));
		assertTrue(text.contains("lifesteal=1.25"));
		assertTrue(text.contains("armor_penetration=0.17"));
		assertTrue(text.contains("attacks_per_second=1.67"));
		assertEquals(0.12512031523088826, item.getDouble("lifesteal"), 0.0);
		assertEquals("1.2512031523088827",
				item.getMap(Item.RARITY_MODIFIERS).get("lifesteal"));
	}

	@Test
	public void leavesTextAndIntegerValuesReadable() {
		assertEquals("legendary", ItemInspectionFormatter.formatValue("legendary"));
		assertEquals("31", ItemInspectionFormatter.formatValue("31"));
		assertEquals("-42", ItemInspectionFormatter.formatValue("-42"));
		assertEquals("1.2", ItemInspectionFormatter.formatValue("1.2000"));
	}
}
