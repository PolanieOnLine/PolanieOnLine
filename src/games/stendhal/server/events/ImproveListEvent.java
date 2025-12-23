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

import static games.stendhal.common.constants.Events.IMPROVE_LIST;

import org.apache.log4j.Logger;

import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

public class ImproveListEvent extends RPEvent {
	private static final Logger logger = Logger.getLogger(ImproveListEvent.class);

	private static final String NPC = "npc";
	private static final String ITEMS = "items";

	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(IMPROVE_LIST);
			rpclass.addAttribute(NPC, Type.STRING, Definition.PRIVATE);
			rpclass.addAttribute(ITEMS, Type.VERY_LONG_STRING, Definition.PRIVATE);
		} catch (final SyntaxException e) {
			logger.error("cannot generateRPClass", e);
		}
	}

	public ImproveListEvent(final String npc, final String items) {
		super(IMPROVE_LIST);

		put(NPC, npc);
		put(ITEMS, items);
	}
}
