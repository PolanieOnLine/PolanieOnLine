/***************************************************************************
 *                   (C) Copyright 2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import org.apache.log4j.Logger;

import games.stendhal.common.constants.Events;
import marauroa.common.game.Definition;
import marauroa.common.game.Definition.DefinitionClass;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

/**
 * Event requesting the client to open the producer window.
 */
public class ProducerWindowEvent extends RPEvent {
	private static final Logger logger = Logger.getLogger(ProducerWindowEvent.class);

	private static final String NPC = "npc";
	private static final String TITLE = "title";

	/**
	 * Creates the rpclass definition.
	 */
	public static void generateRPClass() {
		try {
			RPClass rpclass = new RPClass(Events.PRODUCER_WINDOW);
			rpclass.add(DefinitionClass.ATTRIBUTE, NPC, Type.STRING, Definition.PRIVATE);
			rpclass.add(DefinitionClass.ATTRIBUTE, TITLE, Type.STRING, Definition.PRIVATE);
		} catch (SyntaxException e) {
			logger.error("cannot generate RPClass for ProducerWindowEvent", e);
		}
	}

	/**
	 * Creates a new event.
	 *
	 * @param npcName
	 * 		name of the producer NPC.
	 * @param npcTitle
	 * 		title of the producer NPC.
	 */
	public ProducerWindowEvent(String npcName, String npcTitle) {
		super(Events.PRODUCER_WINDOW);
		if (npcName != null && !npcName.isEmpty()) {
			put(NPC, npcName);
		}
		if (npcTitle != null && !npcTitle.isEmpty()) {
			put(TITLE, npcTitle);
		}
	}
}
