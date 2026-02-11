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
package games.stendhal.server.entity.item;

import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.Definition.DefinitionClass;
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
		final RPClass entity = new RPClass("item_information");
		entity.isA("item");

		// Some things may have a textual description
		entity.addAttribute("description_info", Type.LONG_STRING);

		// used for show_item_list events used as shop signs.
		entity.addAttribute("price", Type.INT, Definition.VOLATILE);
		entity.addAttribute("price_copper", Type.INT, Definition.VOLATILE);
		entity.addAttribute("stackable", Type.INT, Definition.VOLATILE);
		entity.addAttribute("flavor_text", Type.LONG_STRING, Definition.VOLATILE);
	}

	public static void ensureShopAttributes() {
		final RPClass entity = RPClass.getRPClass("item_information");
		if (entity == null) {
			return;
		}

		addAttributeIfMissing(entity, "price_copper", Type.INT, Definition.VOLATILE);
		addAttributeIfMissing(entity, "stackable", Type.INT, Definition.VOLATILE);
		addAttributeIfMissing(entity, "flavor_text", Type.LONG_STRING, Definition.VOLATILE);
	}

	private static void addAttributeIfMissing(final RPClass entity, final String name,
			final Type type, final byte flags) {
		if (entity.getDefinition(DefinitionClass.ATTRIBUTE, name) == null) {
			entity.addAttribute(name, type, flags);
		}
	}
}
