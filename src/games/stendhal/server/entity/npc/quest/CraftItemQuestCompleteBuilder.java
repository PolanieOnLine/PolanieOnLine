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

import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.IncrementQuestAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;

public class CraftItemQuestCompleteBuilder extends QuestCompleteBuilder {
	private String greet = "Dzięki za skorzystanie z moich usług i proszę, oto twój sprzęt!";
	private String respondToReject = null;
	private String respondToAccept = null;
	private String respondToMissingItem = "Wróć z przedmiotem potrzebnym do ukończenia zlecenia.";
	private List<ChatAction> rewardWith = new LinkedList<>();
	private List<Pair<String, Integer>> requiredItem = new LinkedList<>();

	// hide constructor
	CraftItemQuestCompleteBuilder() {
		super();
	}

	public CraftItemQuestCompleteBuilder greet(String greet) {
		this.greet = greet;
		return this;
	}

	public CraftItemQuestCompleteBuilder respondToReject(String respondToReject) {
		this.respondToReject = respondToReject;
		return this;
	}

	public CraftItemQuestCompleteBuilder respondToAccept(String respondToAccept) {
		this.respondToAccept = respondToAccept;
		return this;
	}

	public CraftItemQuestCompleteBuilder rewardWith(ChatAction action) {
		this.rewardWith.add(action);
		return this;
	}

	public CraftItemQuestCompleteBuilder requiredItem(int quantity, String name) {
		requiredItem.add(new Pair<String, Integer>(name, quantity));
		return this;
	}

	public CraftItemQuestCompleteBuilder respondToMissingItem(String respondToMissingItem) {
		this.respondToMissingItem = respondToMissingItem;
		return this;
	}

	private ChatCondition requiredItemsCondition() {
		List<ChatCondition> conditions = new LinkedList<>();
		for (Pair<String, Integer> item : requiredItem) {
			conditions.add(new PlayerHasItemWithHimCondition(item.first(), item.second()));
		}
		return new AndCondition(conditions);
	}

	@Override
	void build(SpeakerNPC npc, String questSlot, ChatCondition questCompletedCondition, ChatAction questCompleteAction) {
		ChatCondition readyCondition = new AndCondition(
				new GreetingMatchesNameCondition(npc.getName()),
				questCompletedCondition);
		ChatCondition requiredItemsCondition = requiredItemsCondition();
		ChatCondition mayCompleteCondition = new AndCondition(
				readyCondition,
				requiredItemsCondition);
		npc.registerPrioritizedGreetingTransition(readyCondition, this);

		if (!requiredItem.isEmpty()) {
			npc.add(
					ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new AndCondition(readyCondition, new NotCondition(requiredItemsCondition)),
					ConversationStates.ATTENDING,
					respondToMissingItem,
					null);
		}

		List<ChatAction> actions = new LinkedList<ChatAction>();
		for (Pair<String, Integer> item : requiredItem) {
			actions.add(new DropItemAction(item.first(), item.second()));
		}
		if (questCompleteAction != null) {
			actions.add(questCompleteAction);
		}
		actions.add(new SetQuestAction(questSlot, 0, "done"));
		actions.add(new SetQuestToTimeStampAction(questSlot, 1));
		actions.add(new IncrementQuestAction(questSlot, 2, 1));
		actions.addAll(rewardWith);

		if (respondToAccept != null || respondToReject != null) {
			buildWithConfirmation(npc, mayCompleteCondition, actions);
		} else {
			buildWithoutConfirmation(npc, mayCompleteCondition, actions);
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

	void buildWithoutConfirmation(SpeakerNPC npc, ChatCondition mayCompleteCondition, List<ChatAction> actions) {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				mayCompleteCondition,
				ConversationStates.ATTENDING,
				greet,
				new MultipleActions(actions));
	}

	@Override
	void simulate(String npc, QuestSimulator simulator) {
	}
}
