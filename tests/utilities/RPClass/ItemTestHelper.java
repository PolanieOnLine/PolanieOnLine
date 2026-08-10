/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package utilities.RPClass;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemInformation;
import games.stendhal.server.entity.item.StackableItem;
import marauroa.common.game.RPClass;

public class ItemTestHelper {

	public static Item createItem() {
		ItemTestHelper.generateRPClasses();
		return new Item("item", "itemclass", "subclass", null);
	}

	public static Item createItem(final String name) {
		ItemTestHelper.generateRPClasses();
		return new Item(name, "itemclass", "subclass", null);
	}

	public static Item createItem(final String name, final int quantity) {
		ItemTestHelper.generateRPClasses();
		final StackableItem item = new StackableItem(name, "itemclass", "subclass", null);
		item.setQuantity(quantity);
		return item;
	}

	public static void generateRPClasses() {
		EntityTestHelper.generateRPClasses();

		if (!RPClass.hasRPClass("item")) {
			Item.generateRPClass();
		}
		/* ItemInformation registers the client-visible tooltip map on the item
		 * RPClass. Tests that create items directly need the same RPClass setup
		 * as the production object factory. */
		if (!RPClass.hasRPClass("item_information")) {
			ItemInformation.generateRPClass();
		}
	}

}
