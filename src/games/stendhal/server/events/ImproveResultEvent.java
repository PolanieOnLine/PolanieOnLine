/***************************************************************************
*                      (C) Copyright 2024 - PolanieOnLine                 *
***************************************************************************/
/***************************************************************************
*                                                                         *
*   This program is free software; you can redistribute it and/or modify  *
*   it under the terms of the GNU General Public License as published by  *
*   the Free Software Foundation; either version 2 of the License, or     *
*   (at your option) any later version.                                   *
*                                                                         *
***************************************************************************/
package games.stendhal.server.events;

import static games.stendhal.common.constants.Events.IMPROVE_RESULT;

import org.apache.log4j.Logger;

import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

public class ImproveResultEvent extends RPEvent {
	private static final Logger logger = Logger.getLogger(ImproveResultEvent.class);

	private static final String NPC = "npc";
	private static final String SUCCESS = "success";
	private static final String MESSAGE = "message";
	private static final String ITEM_ID = "itemid";
	private static final String ITEM_NAME = "itemname";
	private static final String ICON = "icon";

	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(IMPROVE_RESULT);
			rpclass.addAttribute(NPC, Type.STRING, Definition.PRIVATE);
			rpclass.addAttribute(SUCCESS, Type.FLAG, Definition.PRIVATE);
			rpclass.addAttribute(MESSAGE, Type.LONG_STRING, Definition.PRIVATE);
			rpclass.addAttribute(ITEM_ID, Type.INT, Definition.PRIVATE);
			rpclass.addAttribute(ITEM_NAME, Type.STRING, Definition.PRIVATE);
			rpclass.addAttribute(ICON, Type.STRING, Definition.PRIVATE);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public ImproveResultEvent(final String npc, final boolean success, final String message,
	final int itemId, final String itemName, final String icon) {
		super(IMPROVE_RESULT);

		put(NPC, npc);
		put(SUCCESS, success);
		put(MESSAGE, message);
		put(ITEM_ID, itemId);
		put(ITEM_NAME, itemName);
		if (icon != null) {
			put(ICON, icon);
		}
	}
}
