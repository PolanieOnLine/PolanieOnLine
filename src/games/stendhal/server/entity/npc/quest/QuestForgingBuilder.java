/***************************************************************************
 *                   (C) Copyright 2022 - PolanieOnLine                    *
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

import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;

public class QuestForgingBuilder {
	private String greet = "Dziękuję. Zacznę pracę od zaraz!";
	private String greetBeforeConfirmation = "Widzę, że masz już wszystkie potrzebne przedmioty. Chcesz, abyśmy rozpoczeli prace nad Twoim zleceniem?";
	private String respondToReject = null;
	private String respondToAccept = null;

	public QuestForgingBuilder greet(String greet) {
		this.greet = greet;
		return this;
	}

	public QuestForgingBuilder greetBeforeConfirmation(String greetBeforeConfirmation) {
		this.greetBeforeConfirmation = greetBeforeConfirmation;
		return this;
	}

	public QuestForgingBuilder respondToReject(String respondToReject) {
		this.respondToReject = respondToReject;
		return this;
	}

	public QuestForgingBuilder respondToAccept(String respondToAccept) {
		this.respondToAccept = respondToAccept;
		return this;
	}

	public void build(SpeakerNPC npc, String questSlot, ChatCondition questCompletedCondition, ChatAction questCompleteAction, int forgingDelay) {
		if (forgingDelay > 0) {
			ChatCondition mayStartForgingCondition = new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(questSlot, "start"),
					questCompletedCondition);
			npc.registerPrioritizedGreetingTransition(mayStartForgingCondition, this);
	
			List<ChatAction> actions = new LinkedList<ChatAction>();
			if (questCompleteAction != null) {
				actions.add(questCompleteAction);
			}
			actions.add(new SetQuestAction(questSlot, "forging;" + System.currentTimeMillis()));
	
			if (respondToAccept != null) {
				buildWithConfirmation(npc, mayStartForgingCondition, actions);
			} else {
				buildWithoutConfirmation(npc, mayStartForgingCondition, actions);
			}
		}
	}

	void buildWithConfirmation(SpeakerNPC npc, ChatCondition mayStartForgingCondition, List<ChatAction> actions) {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			mayStartForgingCondition,
			ConversationStates.QUEST_ITEM_BROUGHT,
			greetBeforeConfirmation,
			null);

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			mayStartForgingCondition,
			ConversationStates.ATTENDING,
			respondToAccept,
			new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			respondToReject,
			null);
	}

	void buildWithoutConfirmation(SpeakerNPC npc, ChatCondition mayStartForgingCondition, List<ChatAction> actions) {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			mayStartForgingCondition,
			ConversationStates.ATTENDING,
			greet,
			new MultipleActions(actions));
	}
}
