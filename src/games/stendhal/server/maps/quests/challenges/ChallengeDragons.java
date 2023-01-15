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
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.LevelLessThanCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

public class ChallengeDragons extends ChallengeQuests {
	public static final String QUEST_SLOT = "challenge_dragons";
	// Required level to start quest
	private static final int min_level = 250;
	// Core NPC
	private final SpeakerNPC npc = npcs.get("Racirad");

	// Monsters to record
	private static final String[] creaturesToRecord = {
			"szkielet smoka", "zgniły szkielet smoka", "złoty smok", "zielony smok", "błękitny smok",
			"czerwony smok", "pustynny smok", "czarny smok", "czarne smoczysko", "smok arktyczny",
			"dwugłowy zielony smok", "dwugłowy czerwony smok", "dwugłowy niebieski smok", "dwugłowy czarny smok",
			"dwugłowy lodowy smok", "lodowy smok", "latający czarny smok", "latający złoty smok", "Smok Wawelski",
			"purpurowy smok", "czerwone smoczysko", "zielone smoczysko", "niebieskie smoczysko"
	};

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Wyzwanie: Smokobójca";
	}

	@Override
	public String getRegion() {
		return Region.DRAGON_LANDS;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public int getMinLevel() {
		return min_level;
	}

	@Override
	public List<String> getHistory(Player player) {
		final String[] states = player.getQuest(QUEST_SLOT).split(";");
		final String quest_state = states[0];

		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}

		res.add(npc.getName() + " zapronował mi wykonanie wyzwania na smoki.");
		if (quest_state.equals("rejected")) {
			res.add("Nie mam czasu na zabawy ze smokami, także "
				+ Grammar.genderVerb(player.getGender(), "odrzuciłem")
				+ " propozycje tego durnego wyzwania.");
		} else {
			res.add("Z przyjemnością " + Grammar.genderVerb(player.getGender(), "zaakceptowałem")
				+ " to specyficzne wyzwanie. Będę " + Grammar.genderVerb(player.getGender(), "notował")
				+ " każdego poskromionego smoka!");
			if (quest_state.equals("done")) {
				res.add("Udało mi się ukończyć to niezwykły wyzwanie od Racirada.");
			}
		}

		if (quest_state.equals("hunting")) {
			res.add(howManyWereKilled(player));
		}

		return res;
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			getName(),
			npc.getName() + " strażnik, który ma wyzwanie dla najdzielniejszych rycerzy.",
			false
		);
		prepareRequestStep();
		prepareCompleteStep();
	}

	private void prepareRequestStep() {
		npc.add(ConversationStates.ATTENDING,
			triggers,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelLessThanCondition(min_level)),
			ConversationStates.IDLE,
			"Wybacz rycerzu, ale zdaje mi się iż niezrozumieliśmy się... Nabierz więcej doświadczenia podczas walk na swoim poziomie! Trzymaj się na drodze...",
			null);

		npc.add(ConversationStates.ATTENDING,
			triggers,
			new AndCondition(
				new QuestNotStartedCondition(QUEST_SLOT),
				new LevelGreaterThanCondition(min_level - 1)),
			ConversationStates.ATTENDING,
			"Odnośnie mojego wyzwania... Chciałbym abyś poskromił łącznie 500 smoków! Nie ważne jakich, jakiekolwiek, liczy się łączny wynik poskromionych smoków. Jesteś na to gotów?",
			null);

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.YES_MESSAGES,
			new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.IDLE,
			"W takim razie życzę powodzenia w polowaniu!",
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					player.setQuest(QUEST_SLOT, "hunting;500;"+getActualKills(player, getEntitiesToRecord()));
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
					return getGreaterKills(player);
				}
			});

		final ChatCondition NotCondition = new AndCondition(
			new QuestActiveCondition(QUEST_SLOT),
			new ChatCondition() {
				@Override
				public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
					return getLessKills(player);
				}
			});

		final List<ChatAction> actions = new LinkedList<ChatAction>();
			actions.add(new SetQuestAction(QUEST_SLOT, "done"));
			actions.add(new IncreaseXPAction(500000));
			actions.add(new IncreaseKarmaAction(50.0));
			actions.add(new SayTextAction("Dobra robota rycerzu [name]! Zasługujesz na odpowiednie wynagrodzenie swych czynów."));

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
			new SayTextAction("Wybacz mi proszę rycerzu [name], ale nie wykonałeś jeszcze wyzwania do końca! Zajmij się swym zadaniem, ja muszę strzec przejścia..."));
	}

	private String howManyWereKilled(final Player player) {
		int killed = getPurposeValue(player) - getDiffKills(player);

		String verb = "smoków";
		if (killed == 1) {
			verb = "smoka";
		} else if (killed > 1 && killed < 5) {
			verb = "smoki";
		}

		if (killed > 0) {
			return "Wciąż muszę poskromić " + Integer.toString(killed) + " " + verb + " do zakończenia wyzwania."; 
		}
		return "Udało mi się zakończyć wyzwanie! Może " + Grammar.genderVerb(player.getGender(), "powinienem") + " się teraz udać do niego z powrotem...";
	}

	@Override
	public String getQuestSlot(final Player player, int index) {
		return player.getQuest(QUEST_SLOT, index);
	}

	@Override
	public String[] getEntitiesToRecord() {
		return creaturesToRecord;
	}
}
