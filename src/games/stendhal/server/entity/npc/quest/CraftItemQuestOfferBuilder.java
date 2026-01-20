/***************************************************************************
 *                 (C) Copyright 2023-2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.npc.quest;

import java.util.Arrays;
import java.util.LinkedList;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;

public class CraftItemQuestOfferBuilder extends QuestOfferBuilder<CraftItemQuestOfferBuilder> {
	protected String respondToCraftIssue = "Wybacz, ale musisz zasłużyć na zaufanie zanim rozpoczę nad takim zleceniem prace...";

	CraftItemQuestOfferBuilder() {
		super();
	}

	public CraftItemQuestOfferBuilder respondToCraftIssue(String respondToCraftIssue) {
		this.respondToCraftIssue = respondToCraftIssue;
		return this;
	}

	@Override
	public void build(SpeakerNPC npc, String questSlot, QuestTaskBuilder task, ChatCondition questCompletedCondition, int repeatableAfterMinutes) {
		CraftItemTask craftTask = (CraftItemTask) task;
		ChatAction startQuestAction = craftTask.buildStartQuestAction(questSlot, respondToAccept, respondToReject);
		ChatAction rejectQuestAction = task.buildRejectQuestAction(questSlot);

		ChatCondition beforeForgingConditions = craftTask.requiredConditionsBeforeForge();
		ChatCondition requiredItemsToForge = craftTask.requeredItemsToStartForging();
		int productionTime = craftTask.getProductionTime();

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
					new QuestNotStartedCondition(questSlot),
					beforeForgingConditions),
				ConversationStates.QUEST_OFFERED,
				respondToRequest,
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
					new QuestNotStartedCondition(questSlot),
					new NotCondition(beforeForgingConditions)),
				ConversationStates.ATTENDING,
				respondToCraftIssue,
				null);

		ChatAction startAction = new SetQuestAction(questSlot, 0, "start");
		if (startQuestAction != null) {
			startAction = startQuestAction;
		}

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				null,
				startAction);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				respondToReject,
				new MultipleActions(
						new SetQuestAndModifyKarmaAction(questSlot, 0, "rejected", -1 * rejectionKarmaPenalty),
						rejectQuestAction));

		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);
		triggers.addAll(Arrays.asList("forge","stwórz","wytwórz"));

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
					new QuestInStateCondition(questSlot, 0, "start"),
					beforeForgingConditions,
					new NotCondition(requiredItemsToForge)),
				ConversationStates.ATTENDING,
				remind,
				null);

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
					new QuestInStateCondition(questSlot, 0, "start"),
					new NotCondition(beforeForgingConditions),
					new NotCondition(requiredItemsToForge)),
				ConversationStates.ATTENDING,
				respondToCraftIssue,
				null);

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
					new QuestInStateCondition(questSlot, 0, "forging"),
					new NotCondition(questCompletedCondition)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(questSlot, 1, productionTime, "Proszę... Nie poganiaj mnie! Wciąż pracuję nad Twoim zleceniem. Wróc za "));

		ChatAction showCraftingWindow = craftTask.buildShowCraftingWindowAction(questSlot, npc.getName());

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
					new QuestInStateCondition(questSlot, 0, "start"),
					beforeForgingConditions,
					requiredItemsToForge),
				ConversationStates.ATTENDING,
				null,
				showCraftingWindow);

		if (repeatableAfterMinutes > -1) {
			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new AndCondition(
						new QuestCompletedCondition(questSlot),
						new TimePassedCondition(questSlot, 1, repeatableAfterMinutes)),
					ConversationStates.QUEST_OFFERED,
					respondToRepeatedRequest,
					null);

			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new AndCondition(
							new QuestCompletedCondition(questSlot),
							new NotCondition(new TimePassedCondition(questSlot, 1, repeatableAfterMinutes))),
					ConversationStates.ATTENDING,
					null,
					new SayTimeRemainingAction(questSlot, 1, repeatableAfterMinutes, respondToUnrepeatableRequest, true));
		} else {
			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new QuestCompletedCondition(questSlot),
					ConversationStates.ATTENDING,
					respondToUnrepeatableRequest,
					null);
		}
	}
}
