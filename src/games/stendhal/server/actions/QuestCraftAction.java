/***************************************************************************
 *                 (C) Copyright 2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.QUEST_CRAFT;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.quest.CraftItemTask;
import games.stendhal.server.entity.npc.quest.CraftItemTask.CraftDefinition;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPAction;

public class QuestCraftAction implements ActionListener {
	private static final Logger LOGGER = Logger.getLogger(QuestCraftAction.class);

	public static void register() {
		CommandCenter.register(QUEST_CRAFT, new QuestCraftAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final String questSlot = action.get("quest");
		final String npcName = action.get("npc");
		if (questSlot == null || npcName == null) {
			LOGGER.warn("Received quest craft action without quest or npc identifier");
			return;
		}

		CraftDefinition definition = CraftItemTask.getDefinition(npcName, questSlot);
		if (definition == null) {
			LOGGER.warn("No crafting definition registered for npc=" + npcName + " quest=" + questSlot);
			return;
		}

		SpeakerNPC npc = SingletonRepository.getNPCList().get(npcName);
		if (npc == null) {
			LOGGER.warn("Unable to locate NPC " + npcName + " for quest craft action");
			return;
		}

		CraftItemTask task = definition.getTask();
		String questState = player.getQuest(questSlot, 0);
		if (questState == null || !questState.startsWith("start")) {
			npc.say(task.getRespondToCraftReject());
			return;
		}

		if (!task.playerHaveConditions(player)) {
			npc.say(task.getRespondToCraftReject());
			return;
		}

		if (!task.playerHasAllRequiredItems(player)) {
			npc.say(task.getRespondToCraftReject());
			return;
		}

		EventRaiser raiser = new EventRaiser(npc);
		task.buildForgeQuestAction(questSlot).fire(player, null, raiser);
		String updatedQuestState = player.getQuest(questSlot, 0);
		if (updatedQuestState != null && updatedQuestState.startsWith("forging")) {
			task.dropRequeredItemsToForge().fire(player, null, raiser);
		}
	}
}
