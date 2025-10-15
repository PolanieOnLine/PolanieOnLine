/***************************************************************************
 *                 (C) Copyright 2024 - PolanieOnLine                 *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.events;

import static games.stendhal.common.constants.Events.QUEST_CRAFTING;

import org.apache.log4j.Logger;

import marauroa.common.game.Definition;
import marauroa.common.game.Definition.Type;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPEvent;
import marauroa.common.game.SyntaxException;

/**
 * Event opening crafting preview dialog for quest based productions.
 */
public class QuestCraftingEvent extends RPEvent {
	private static final Logger LOGGER = Logger.getLogger(QuestCraftingEvent.class);

	private static final String TITLE = "title";
	private static final String NPC = "npc";
	private static final String QUEST = "quest";
	private static final String PRODUCT = "product";
	private static final String REQUIRED = "required";
	private static final String WAITING_TIME = "waiting_time";
	private static final String CAN_CRAFT = "can_craft";
	private static final String BUTTON = "button";

	/**
	 * Creates the rpclass definition.
	 */
	public static void generateRPClass() {
		try {
			final RPClass rpclass = new RPClass(QUEST_CRAFTING);
			rpclass.addAttribute(TITLE, Type.STRING, Definition.PRIVATE);
			rpclass.addAttribute(NPC, Type.STRING, Definition.PRIVATE);
			rpclass.addAttribute(QUEST, Type.STRING, Definition.PRIVATE);
			rpclass.addAttribute(PRODUCT, Type.LONG_STRING, Definition.PRIVATE);
			rpclass.addAttribute(REQUIRED, Type.LONG_STRING, Definition.PRIVATE);
			rpclass.addAttribute(WAITING_TIME, Type.INT, Definition.PRIVATE);
			rpclass.addAttribute(CAN_CRAFT, Type.FLAG, Definition.PRIVATE);
			rpclass.addAttribute(BUTTON, Type.STRING, Definition.PRIVATE);
		} catch (final SyntaxException e) {
			LOGGER.error("cannot generate QuestCraftingEvent RPClass", e);
		}
	}

	/**
	 * Creates new crafting preview event.
	 *
	 * @param title title displayed on dialog
	 * @param npcName NPC name tied to crafting
	 * @param questSlot quest slot identifier
	 * @param product encoded product description
	 * @param required encoded required items list
	 * @param waitingTime waiting time in minutes
	 * @param canCraft flag if player currently meets requirements
	 * @param buttonLabel label for craft button
	 */
	public QuestCraftingEvent(String title, String npcName, String questSlot,
			String product, String required, int waitingTime,
			boolean canCraft, String buttonLabel) {
		super(QUEST_CRAFTING);
		put(TITLE, title);
		put(NPC, npcName);
		put(QUEST, questSlot);
		put(PRODUCT, product);
		put(REQUIRED, required);
		put(WAITING_TIME, waitingTime);
		if (canCraft) {
			put(CAN_CRAFT, "");
		}
		put(BUTTON, buttonLabel);
	}
}
