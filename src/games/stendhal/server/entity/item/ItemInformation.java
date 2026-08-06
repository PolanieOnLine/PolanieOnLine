/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import games.stendhal.common.constants.ItemTooltip;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;

public class ItemInformation extends Item {


	/**
	 * copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public ItemInformation(final Item item) {
		super(item);
		setRPClass("item_information");
	}


	public static void generateRPClass() {
		/* ItemInformation is registered immediately after Item while RP classes
		 * are still mutable. Add one presentation-only map to the parent item
		 * class so normal inventory items can expose selected hidden statistics
		 * without making the internal attributes themselves public. */
		RPClass.getRPClass("item").addAttribute(ItemTooltip.ATTRIBUTE,
				Type.MAP, Definition.VOLATILE);

		final RPClass entity = new RPClass("item_information");
		entity.isA("item");

		// Some things may have a textual description
		entity.addAttribute("description_info", Type.LONG_STRING);

		// used for show_item_list events used as shop signs.
		entity.addAttribute("price", Type.INT, Definition.VOLATILE);
	}
}
