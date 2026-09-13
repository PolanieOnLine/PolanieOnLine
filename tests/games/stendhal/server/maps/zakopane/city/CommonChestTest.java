/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.city;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.mapstuff.chest.Chest;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import utilities.RPClass.ChestTestHelper;
import utilities.RPClass.ItemTestHelper;

public class CommonChestTest {
	@BeforeClass
	public static void setUpClasses() {
		ChestTestHelper.generateRPClasses();
		ItemTestHelper.generateRPClasses();
		MockStendlRPWorld.get();
	}

	@Test
	public void starterEquipmentIsAlwaysCommon() {
		final StendhalRPZone zone = new StendhalRPZone("0_zakopane_s", 200, 100);
		new CommonChest().configureZone(zone, Collections.<String, String>emptyMap());

		final Chest chest = (Chest) zone.getEntityAt(110, 45);
		assertNotNull(chest);
		final Map<String, Item> items = new HashMap<String, Item>();
		for (final RPObject object : chest.getSlot("content")) {
			if (object instanceof Item) {
				final Item item = (Item) object;
				items.put(item.getName(), item);
			}
		}

		assertCommon(items, "mieczyk");
		assertCommon(items, "drewniana tarcza");
		assertCommon(items, "skórzana zbroja");
	}

	private void assertCommon(final Map<String, Item> items, final String name) {
		final Item item = items.get(name);
		assertNotNull(name, item);
		assertSame(name, ItemRarity.COMMON, item.getRarity());
	}
}
