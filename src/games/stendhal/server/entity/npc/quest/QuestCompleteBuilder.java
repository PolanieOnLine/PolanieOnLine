/***************************************************************************
 *                   (C) Copyright 2022 - Faiumoni e.V.                    *
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
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;

/**
 * defines how the NPC react after the player completes the quest
 *
 * @author hendrik
 */
public class QuestCompleteBuilder {
	private String greet = "Dziękuję";
	private String respondToReject = null;
	private String respondToAccept = null;
	private List<ChatAction> rewardWith = new LinkedList<>();

	public QuestCompleteBuilder greet(String greet) {
		this.greet = greet;
		return this;
	}

	public QuestCompleteBuilder respondToReject(String respondToReject) {
		this.respondToReject = respondToReject;
		return this;
	}

	public QuestCompleteBuilder respondToAccept(String respondToAccept) {
		this.respondToAccept = respondToAccept;
		return this;
	}

	public QuestCompleteBuilder rewardWith(ChatAction action) {
		this.rewardWith.add(action);
		return this;
	}

	void simulate(String npc, QuestSimulator simulator) {
		simulator.playerSays("hi");
		simulator.npcSays(npc, greet);

		if (respondToReject != null || respondToAccept != null) {
			simulator.playerSays("no");
			simulator.npcSays(npc, respondToReject);
			simulator.playerSays("bye");
			simulator.info("");

			simulator.playerSays("hi");
			simulator.npcSays(npc, greet);
			simulator.playerSays("yes");
			simulator.npcSays(npc, respondToAccept);

		}
		simulator.info("Player was rewarded with " + this.rewardWith.toString());
		simulator.playerSays("bye");
		simulator.info("");
	}

	void build(SpeakerNPC npc, String questSlot, ChatCondition questCompletedCondition, ChatAction questCompleteAction, int forgingDelay, boolean repeatable) {
		List<ChatCondition> mayCompleteCondition = new LinkedList<ChatCondition>();
		mayCompleteCondition.add(new GreetingMatchesNameCondition(npc.getName()));
		if (forgingDelay > 0) {
			mayCompleteCondition.add(new QuestStateStartsWithCondition(questSlot, "forging;"));
			mayCompleteCondition.add(new TimePassedCondition(questSlot, 1, forgingDelay));
		} else {
			mayCompleteCondition.add(new QuestActiveCondition(questSlot));
			mayCompleteCondition.add(questCompletedCondition);
		}
		npc.registerPrioritizedGreetingTransition(new AndCondition(mayCompleteCondition), this);

		List<ChatAction> actions = new LinkedList<ChatAction>();
		if (forgingDelay == 0 && questCompleteAction != null) {
			actions.add(questCompleteAction);
		}
		if (!repeatable) {
			actions.add(new SetQuestAction(questSlot, "done"));
		} else {
			actions.add(new SetQuestAction(questSlot, 0, "done"));
			actions.add(new SetQuestToTimeStampAction(questSlot, 1));
			actions.add(new IncrementQuestAction(questSlot, 2, 1));
		}
		actions.addAll(rewardWith);

		if (respondToAccept != null) {
			buildWithConfirmation(npc, new AndCondition(mayCompleteCondition), actions);
		} else {
			buildWithoutConfirmation(npc, questSlot, new AndCondition(mayCompleteCondition), actions, forgingDelay);
		}
	}

	void buildWithConfirmation(SpeakerNPC npc, ChatCondition mayCompleteCondition, List<ChatAction> actions) {
		// player returns while quest is still active
		npc.add(
			ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			mayCompleteCondition,
			ConversationStates.QUEST_ITEM_BROUGHT,
			greet,
			null);

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.YES_MESSAGES,
			// make sure the player isn't cheating by putting the armor
			// away and then saying "yes"
			mayCompleteCondition,
			ConversationStates.ATTENDING,
			respondToAccept,
			new MultipleActions(actions));

		npc.add(
			ConversationStates.QUEST_ITEM_BROUGHT,
			ConversationPhrases.NO_MESSAGES,
			null,
			ConversationStates.ATTENDING,
			respondToReject,
			null);
	}

	void buildWithoutConfirmation(SpeakerNPC npc, String questSlot, ChatCondition mayCompleteCondition, List<ChatAction> actions, int forgingDelay) {
		if (forgingDelay > 0) {
			ChatCondition timeNotCompleteCondition = new AndCondition(
					new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(questSlot, "forging;"),
					new NotCondition(new TimePassedCondition(questSlot, 1, forgingDelay)));
			npc.registerPrioritizedGreetingTransition(timeNotCompleteCondition, this);

			npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				timeNotCompleteCondition,
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(questSlot, 1, forgingDelay, "Wciąż pracuję nad Twoim zleceniem. Wróć za "));

			npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				mayCompleteCondition,
				ConversationStates.ATTENDING,
				greet,
				new MultipleActions(actions));
		} else {
			npc.add(ConversationStates.IDLE,
				ConversationPhrases.GREETING_MESSAGES,
				mayCompleteCondition,
				ConversationStates.ATTENDING,
				greet,
				new MultipleActions(actions));
		}
	}
}
