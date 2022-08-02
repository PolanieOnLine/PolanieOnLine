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

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KarmaGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.KarmaLessThanCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasKilledNumberOfCreaturesCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import marauroa.common.Pair;

/**
 * defines how the NPC offers the player the quest when the player says "quest"
 *
 * @author hendrik
 */
public class QuestOfferBuilder {
	private List<String> needQuestsCompleted = null;
	private Pair<String, Integer> needLevelCondition = null;
	private Pair<String, Integer> needKarmaCondition = null;
	private List<String> needKilledCondition = null;
	private String respondToUnstartable = "Wybacz, ale musisz zasłużyć na zaufanie zanim podejmiesz się mojego wyzwania.";
	private String respondToRequest = null;
	private String respondToUnrepeatableRequest = "Pozwól mi podziękować za wcześniejszą twoją pracę. Mam teraz dla ciebie nowe zadanie.";
	private String respondToRepeatedRequest = null;
	private String respondToAccept = "Dziękuję!";
	private String respondToReject = "Och. To niezbyt dobrze.";
	private String remind = "Proszę, dotrzymaj swojej obietnicy.";
	private double acceptationKarmaReward = 2.0;
	private double rejectionKarmaPenalty = 2.0;
	private List<String> lastRespondTo = null;
	private Map<List<String>, String> additionalReplies = new HashMap<>();

	public QuestOfferBuilder needQuestsCompleted(String... needQuestsCompleted) {
		this.needQuestsCompleted = Arrays.asList(needQuestsCompleted);
		return this;
	}

	public QuestOfferBuilder needLevelCondition(String greaterOrLess, int level) {
		this.needLevelCondition = new Pair<String, Integer>(greaterOrLess, level);
		return this;
	}

	public QuestOfferBuilder needKarmaCondition(String greaterOrLess, int karma) {
		this.needKarmaCondition = new Pair<String, Integer>(greaterOrLess, karma);
		return this;
	}

	public QuestOfferBuilder needKilledCondition(String... needKilledCondition) {
		this.needKilledCondition = Arrays.asList(needKilledCondition);
		return this;
	}

	public QuestOfferBuilder respondToUnstartable(String respondToUnstartable) {
		this.respondToUnstartable = respondToUnstartable;
		return this;
	}

	public QuestOfferBuilder respondToRequest(String respondToRequest) {
		this.respondToRequest = respondToRequest;
		if (this.respondToRepeatedRequest == null) {
			this.respondToRepeatedRequest = respondToRequest;
		}
		return this;
	}

	public QuestOfferBuilder respondToUnrepeatableRequest(String respondToUnrepeatableRequest) {
		this.respondToUnrepeatableRequest = respondToUnrepeatableRequest;
		return this;
	}

	public QuestOfferBuilder respondToRepeatedRequest(String respondToRepeatedRequest) {
		this.respondToRepeatedRequest = respondToRepeatedRequest;
		return this;
	}

	public QuestOfferBuilder respondToAccept(String respondToAccept) {
		this.respondToAccept = respondToAccept;
		return this;
	}

	public QuestOfferBuilder respondToReject(String respondToReject) {
		this.respondToReject = respondToReject;
		return this;
	}

	public QuestOfferBuilder respondTo(String... respondTo) {
		this.lastRespondTo = Arrays.asList(respondTo);
		return this;
	}

	public QuestOfferBuilder saying(String reply) {
		additionalReplies.put(lastRespondTo, reply);
		return this;
	}

	public QuestOfferBuilder remind(String remind) {
		this.remind = remind;
		return this;
	}

	public QuestOfferBuilder acceptationKarmaReward(double acceptationKarmaReward) {
		this.acceptationKarmaReward = acceptationKarmaReward;
		return this;
	}

	public QuestOfferBuilder rejectionKarmaPenalty(double rejectionKarmaPenalty) {
		this.rejectionKarmaPenalty = rejectionKarmaPenalty;
		return this;
	}

	void simulateFirst(String npc, QuestSimulator simulator) {
		simulator.playerSays("hi");
		simulator.playerSays("quest");
		simulator.npcSays(npc, respondToRequest);
		simulator.playerSays("no");
		simulator.npcSays(npc, respondToReject);
		simulator.playerSays("bye");
		simulator.info("");

		simulator.playerSays("hi");
		simulator.playerSays("quest");
		simulator.npcSays(npc, respondToRequest);
		simulator.playerSays("yes");
		simulator.npcSays(npc, respondToAccept);
		simulator.playerSays("bye");
		simulator.info("");

		simulator.playerSays("hi");
		simulator.playerSays("quest");
		simulator.npcSays(npc, remind);
		simulator.info("");
	}

	void simulateNotRepeatable(String npc, QuestSimulator simulator) {
		simulator.playerSays("hi");
		simulator.playerSays("quest");
		simulator.npcSays(npc, respondToUnrepeatableRequest);
		simulator.playerSays("bye");
		simulator.info("");
	}

	void simulateRepeat(String npc, QuestSimulator simulator) {
		simulator.playerSays("hi");
		simulator.playerSays("quest");
		simulator.npcSays(npc, respondToRepeatedRequest);
		simulator.playerSays("bye");
		simulator.info("");
	}

	ChatCondition someNeedsToStartCondition() {
		List<ChatCondition> conditions = new LinkedList<>();
		if (needQuestsCompleted != null) {
			for (String questName : needQuestsCompleted) {
				conditions.add(new QuestCompletedCondition(questName));
			}
			return new AndCondition(conditions);
		}
		if (needLevelCondition != null) {
			if (needLevelCondition.first() == "greater") {
				conditions.add(new LevelGreaterThanCondition(needLevelCondition.second()));
			} else {
				conditions.add(new LevelLessThanCondition(needLevelCondition.second()));
			}
			return new AndCondition(conditions);
		}
		if (needKarmaCondition != null) {
			if (needKarmaCondition.first() == "greater") {
				conditions.add(new KarmaGreaterThanCondition(needKarmaCondition.second()));
			} else {
				conditions.add(new KarmaLessThanCondition(needKarmaCondition.second()));
			}
			return new AndCondition(conditions);
		}
		if (needKilledCondition != null) {
			for (String monster : needKilledCondition) {
				conditions.add(new PlayerHasKilledNumberOfCreaturesCondition(1, monster));
			}
			return new AndCondition(conditions);
		}
		return null;
	}

	void build(SpeakerNPC npc, String questSlot,
				ChatAction startQuestAction, ChatCondition questCompletedCondition,
				int repeatableAfterMinutes, int forgingDelay) {

		if (needQuestsCompleted != null || needLevelCondition != null || needKarmaCondition != null || needKilledCondition != null) {
			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new AndCondition(
						new QuestNotStartedCondition(questSlot),
						someNeedsToStartCondition()),
					ConversationStates.QUEST_OFFERED,
					respondToRequest,
					null);

			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new AndCondition(
						new QuestNotStartedCondition(questSlot),
						new NotCondition(someNeedsToStartCondition())),
					ConversationStates.ATTENDING,
					respondToUnstartable,
					null);
		} else {
			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new QuestNotStartedCondition(questSlot),
					ConversationStates.QUEST_OFFERED,
					respondToRequest,
					null);
		}

		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);
		if (forgingDelay > 0) {
			npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
					new QuestActiveCondition(questSlot),
					new NotCondition(questCompletedCondition)),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(questSlot, 1, forgingDelay, "Proszę... Nie poganiaj mnie! Wciąż pracuję nad Twoim zleceniem. Wróć za "));
		} else {
			npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
					new QuestActiveCondition(questSlot),
					new NotCondition(questCompletedCondition)),
				ConversationStates.ATTENDING,
				remind,
				null);
		}

		if (repeatableAfterMinutes > 0) {

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
					respondToUnrepeatableRequest,
					null);

		} else {

			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new QuestCompletedCondition(questSlot),
					ConversationStates.ATTENDING,
					respondToUnrepeatableRequest,
					null);
		}

		final List<ChatAction> start = new LinkedList<ChatAction>();
		start.add(new SetQuestAction(questSlot, 0, "start"));
		start.add(new IncreaseKarmaAction(acceptationKarmaReward));
		if (startQuestAction != null) {
			start.add(startQuestAction);
		}

		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				respondToAccept,
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				respondToReject,
				new SetQuestAndModifyKarmaAction(questSlot, "rejected", -1 * rejectionKarmaPenalty));

		for (Map.Entry<List<String>, String> entry : additionalReplies.entrySet()) {
			npc.add(
					ConversationStates.QUEST_OFFERED,
					entry.getKey(),
					null,
					ConversationStates.QUEST_OFFERED,
					entry.getValue(),
					null);
		}
	}
}
