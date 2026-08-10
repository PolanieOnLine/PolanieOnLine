package games.stendhal.server.actions.query;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class LookActionItemTest {
	@BeforeClass
	public static void setUpClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void itemLookShowsOnlyConfiguredDescription() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("description", "Stary miecz z wyszczerbionym ostrzem.");
		attributes.put("atk", "99");
		attributes.put("def", "12");
		final Item item = new Item("miecz testowy", "sword", "test", attributes);
		item.setRarity(ItemRarity.LEGENDARY);
		item.setValue(12345);

		assertEquals("Stary miecz z wyszczerbionym ostrzem.",
				LookAction.getLookDescription(item));
	}

	@Test
	public void itemWithoutDescriptionGetsNeutralFallbackOnly() {
		final Item item = new Item("miecz testowy", "sword", "test", null);
		item.setRarity(ItemRarity.RARE);
		item.setValue(999);

		assertEquals("Oto miecz testowy.", LookAction.getLookDescription(item));
	}
}
