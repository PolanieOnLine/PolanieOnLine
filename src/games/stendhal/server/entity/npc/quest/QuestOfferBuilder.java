/***************************************************************************
 *                 (C) Copyright 2022-2023 - Faiumoni e.V.                 *
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
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;

/**
 * defines how the NPC offers the player the quest when the player says "quest"
 *
 * @author hendrik
 */
public class QuestOfferBuilder<T extends QuestOfferBuilder<T>> {
	protected String begOnGreeting = null;
	protected String respondToRequest = null;
	protected String respondToPreconditionIssue = "Najpierw musisz ukończyć wcześniejsze zadania.";
	protected String respondToUnrepeatableRequest = "Dziękuję za pomoc. Nie mam dla ciebie nowego zadania.";
	protected String respondToRepeatedRequest = null;
	protected String respondToAccept = "Dziękuję.";
	protected String respondToReject = "Och. Szkoda.";
	protected String remind = "Proszę dotrzymać obietnicy.";
	protected double rejectionKarmaPenalty = 2.0;
	protected List<String> lastRespondTo = null;
	protected Map<List<String>, String> additionalReplies = new HashMap<>();
	protected List<ChatAction> acceptWith = new LinkedList<>();
	protected ConversationStates offerState = ConversationStates.QUEST_OFFERED;

	// hide constructor
	QuestOfferBuilder() {
		super();
	}

	/**
	 * Selects the conversation state used while this quest offer waits for an
	 * answer. NPCs which offer more than one quest must use distinct states so
	 * their yes/no transitions cannot overlap.
	 *
	 * @param offerState state used by this offer
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public T offerState(final ConversationStates offerState) {
		if (offerState == null) {
			throw new IllegalArgumentException("offerState must not be null");
		}
		this.offerState = offerState;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T begOnGreeting(String begOnGreeting) {
		this.begOnGreeting = begOnGreeting;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T respondToRequest(String respondToRequest) {
		this.respondToRequest = respondToRequest;
		if (this.respondToRepeatedRequest == null) {
			this.respondToRepeatedRequest = respondToRequest;
		}
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T respondToPreconditionIssue(String respondToPreconditionIssue) {
		this.respondToPreconditionIssue = respondToPreconditionIssue;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T respondToUnrepeatableRequest(String respondToUnrepeatableRequest) {
		this.respondToUnrepeatableRequest = respondToUnrepeatableRequest;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T respondToRepeatedRequest(String respondToRepeatedRequest) {
		this.respondToRepeatedRequest = respondToRepeatedRequest;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T respondToAccept(String respondToAccept) {
		this.respondToAccept = respondToAccept;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T respondToReject(String respondToReject) {
		this.respondToReject = respondToReject;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T acceptWith(ChatAction action) {
		this.acceptWith.add(action);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T respondTo(String... respondTo) {
		this.lastRespondTo = Arrays.asList(respondTo);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T saying(String reply) {
		additionalReplies.put(lastRespondTo, reply);
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T remind(String remind) {
		this.remind = remind;
		return (T) this;
	}

	@SuppressWarnings("unchecked")
	public T rejectionKarmaPenalty(double rejectionKarmaPenalty) {
		this.rejectionKarmaPenalty = rejectionKarmaPenalty;
		return (T) this;
	}

	void simulateFirst(String npc, QuestSimulator simulator) {
		simulator.playerSays("hi");
		if (begOnGreeting == null) {
			simulator.playerSays("quest");
		}
		simulator.npcSays(npc, respondToRequest);
		simulator.playerSays("no");
		simulator.npcSays(npc, respondToReject);
		simulator.playerSays("bye");
		simulator.info("");

		simulator.playerSays("hi");
		if (begOnGreeting == null) {
			simulator.playerSays("quest");
		}
		simulator.npcSays(npc, respondToRequest);
		simulator.playerSays("yes");
		simulator.npcSays(npc, respondToAccept);
		simulator.playerSays("bye");
		simulator.info("");

		if (begOnGreeting == null) {
			simulator.playerSays("hi");
			simulator.playerSays("quest");
			simulator.npcSays(npc, remind);
			simulator.info("");
		}
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

	public void build(SpeakerNPC npc, String questSlot, QuestTaskBuilder task, ChatCondition questCompletedCondition, int repeatableAfterMinutes) {
		ChatCondition questPreCondition = task.buildQuestPreCondition(questSlot);
		ChatAction startQuestAction = task.buildStartQuestAction(questSlot);
		ChatAction rejectQuestAction = task.buildRejectQuestAction(questSlot);

		if (begOnGreeting != null) {
			npc.add(ConversationStates.IDLE,
					ConversationPhrases.GREETING_MESSAGES,
					new OrCondition(
							new AndCondition(
									new QuestNotStartedCondition(questSlot),
									questPreCondition),
							new AndCondition(
									new QuestActiveCondition(questSlot),
									new NotCondition(questCompletedCondition)
							)
					),
					offerState,
					begOnGreeting,
					null);
		}

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotStartedCondition(questSlot),
						questPreCondition),
				offerState,
				respondToRequest,
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestNotStartedCondition(questSlot),
						new NotCondition(questPreCondition)),
				ConversationStates.ATTENDING,
				respondToPreconditionIssue,
				null);

		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);

		npc.add(ConversationStates.ATTENDING,
				triggers,
				new AndCondition(
					new QuestActiveCondition(questSlot),
					new NotCondition(questCompletedCondition)),
				ConversationStates.ATTENDING,
				remind,
				null);

		if (repeatableAfterMinutes > -1) {
			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new AndCondition(
						new QuestCompletedCondition(questSlot),
						new TimePassedCondition(questSlot, 1, repeatableAfterMinutes),
						questPreCondition),
					offerState,
					respondToRepeatedRequest,
					null);

			npc.add(ConversationStates.ATTENDING,
					ConversationPhrases.QUEST_MESSAGES,
					new AndCondition(
						new QuestCompletedCondition(questSlot),
						new TimePassedCondition(questSlot, 1, repeatableAfterMinutes),
						new NotCondition(questPreCondition)),
					ConversationStates.ATTENDING,
					respondToPreconditionIssue,
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

		final List<ChatAction> start = new LinkedList<ChatAction>();
		start.add(new SetQuestAction(questSlot, 0, "start"));
		if (startQuestAction != null) {
			start.add(startQuestAction);
		}
		start.addAll(acceptWith);

		npc.add(
				offerState,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				respondToAccept,
				new MultipleActions(start));

		npc.add(offerState,
				ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				respondToReject,
				new MultipleActions(
						new SetQuestAndModifyKarmaAction(questSlot, 0, "rejected", -1 * rejectionKarmaPenalty),
						rejectQuestAction));

		for (Map.Entry<List<String>, String> entry : additionalReplies.entrySet()) {
			npc.add(
					offerState,
					entry.getKey(),
					null,
					offerState,
					entry.getValue(),
					null);
		}
	}
}
