/***************************************************************************
 *                 (C) Copyright 2019-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.challenges;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class ChallengeGreedy extends ChallengeQuests {
	public static final String QUEST_SLOT = "challenge_greedy";
	// Core NPC
	private final SpeakerNPC npc = npcs.get("Chciwurak");
	// Items to record
	private static final String[] moneyToRecord = { "money" };

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Wyzwanie: Zachłanność";
	}

	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public List<String> getHistory(Player player) {
		final String[] states = player.getQuest(QUEST_SLOT).split(";");
		final String quest_state = states[0];

		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}

		res.add(npc.getName() + " zapronował mi wykonanie wyzwania na grabież monet ze zwłok.");
		if (quest_state.equals("rejected")) {
			res.add("Cóż za obrzydliwe wyzwanie... Także "
				+ Grammar.genderVerb(player.getGender(), "odrzuciłem")
				+ " propozycje tego durnego wyzwania.");
		} else {
			res.add("Z przyjemnością " + Grammar.genderVerb(player.getGender(), "zaakceptowałem")
				+ " to specyficzne wyzwanie. Będę " + Grammar.genderVerb(player.getGender(), "notował")
				+ " każdą zdobytą monetę z potworów!");
			if (quest_state.equals("done")) {
				res.add("Udało mi się ukończyć to niezwykły wyzwanie od Chciwuraka.");
			}
		}

		if (quest_state.equals("collecting")) {
			res.add(howManyWereLooted(player));
		}

		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			getName(),
			npc.getName() + ", niezwykle wydająca się tajemnicza osoba rzuciła mi dziwne wyzwanie na grabież monet.",
			false
		);
		prepareRequestStep();
		prepareCompleteStep();
	}

	private void prepareRequestStep() {
		npc.add(ConversationStates.ATTENDING,
			triggers,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Odnośnie wyzwania... Proponuję tobie zgrabowanie #'1 MILION' monet ze zwłok potworów. Godzisz się na to?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.IDLE,
			"W takim razie wróć gdy już zbogacisz się!",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					player.setQuest(QUEST_SLOT, "collecting;1000000;"+getActualLooted(player, getEntitiesToRecord()));
				}
			});

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.NO_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.ATTENDING,
			"Wróć gdy się już zastanowisz nad moim wyzwaniem.",
			null);
	}

	private void prepareCompleteStep() {
		final ChatCondition Condition = new AndCondition(
			new QuestActiveCondition(QUEST_SLOT),
			new ChatCondition() {
				@Override
				public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
					return getGreaterLoots(player);
				}
			});

		final ChatCondition NotCondition = new AndCondition(
			new QuestActiveCondition(QUEST_SLOT),
			new ChatCondition() {
				@Override
				public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
					return getLessLoots(player);
				}
			});

		final List<ChatAction> actions = new LinkedList<ChatAction>();
			actions.add(new SetQuestAction(QUEST_SLOT, "done"));
			actions.add(new IncreaseXPAction(250000));
			actions.add(new IncreaseKarmaAction(25.0));
			actions.add(new SayTextAction("No... no... [name]. Ten czyn nie zostanie zapomniany, a teraz może dałbyś mi nieco monet za współudział? Nie? Szkoda..."));

		npc.add(ConversationStates.ATTENDING,
			triggers,
			Condition,
			ConversationStates.ATTENDING,
			null,
			new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING,
			triggers,
			NotCondition,
			ConversationStates.IDLE,
			null,
			new SayTextAction("Przepraszam cię [name], ale moja intuicja co do skarbów jest niezawodna i podpowiada mi iż nie udało ci się jeszcze dokończyć wyzwania..."));
	}

	private String howManyWereLooted(final Player player) {
		int looted = getPurposeValue(player) - getDiffLoot(player);

		if (looted > 0) {
			return "Wciąż muszę nazbierać " + Integer.toString(looted) + " money z potworów do zakończenia wyzwania."; 
		}
		return "Udało mi się zakończyć wyzwanie! Może " + Grammar.genderVerb(player.getGender(), "powinienem") + " się teraz udać do niego z powrotem...";
	}

	@Override
	public String getQuestSlot(final Player player, int index) {
		return player.getQuest(QUEST_SLOT, index);
	}

	@Override
	public String[] getEntitiesToRecord() {
		return moneyToRecord;
	}
}
