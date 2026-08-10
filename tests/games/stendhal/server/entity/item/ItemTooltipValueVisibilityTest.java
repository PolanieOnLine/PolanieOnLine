package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemTooltip;
import utilities.RPClass.ItemTestHelper;

public class ItemTooltipValueVisibilityTest {
	@BeforeClass
	public static void setUpClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void valueRemainsStoredButIsNotPublishedToTooltip() {
		final Item item = new Item("test item", "sword", "test", null);
		item.setValue(12345);

		ItemTooltipService.update(item);

		assertEquals(12345, item.getValue());
		assertFalse(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
				ItemTooltip.VALUE));
	}
}
