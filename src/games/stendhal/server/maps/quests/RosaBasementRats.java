/***************************************************************************
 *                   (C) Copyright 2003-2011 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
// Based on CleanStorageSpace.
package games.stendhal.server.maps.quests;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import marauroa.common.Pair;

/**
 * QUEST: CleanStorageSpace
 * <p>
 * PARTICIPANTS:
 * <li> Eonna
 * <p>
 * STEPS:
 * <li> Eonna asks you to clean her storage space.
 * <li> You go kill at least a rat, a cave rat and a cobra.
 * <li> Eonna checks your kills and then thanks you.
 * <p>
 * REWARD:
 * <li> 550 XP, karma
 * <p>
 * REPETITIONS:
 * <li> None.
 */
public class RosaBasementRats extends AbstractQuest {
	private static final String QUEST_SLOT = "pomoc_w_tawernie_rosa";

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Rose w karczmie Gdańskiej.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("Odmówiłem Rosie pomocy.");
		return res;
		}
		res.add("Postanowiłem pomóc Rosie.");
		if (("start".equals(questState) && player.hasKilled("rat") && player.hasKilled("szczur jaskiniowy") && player.hasKilled("wąż")) || "done".equals(questState)) {
			res.add("Piwnica została oczyszczona z gryzoni i węży.");
		}
		if ("done".equals(questState)) {
			res.add("Rosa jest zadowolona.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Rosa");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Właściciel tej tawerny kazał mi poszukać jakiegoś bohatera aby mógł się pozbyć tych gryzoni i węży w naszej #'piwnicy', które się tam zalęgły. Pomożesz mi?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"Pięknie dziękuję! Myślę, że na dole jest nadal czysto.", null);

		final List<ChatAction> start = new LinkedList<ChatAction>();
				
		final HashMap<String, Pair<Integer, Integer>> toKill = 
			new HashMap<String, Pair<Integer, Integer>>();
		// first number is required solo kills, second is required shared kills
		toKill.put("szczur", new Pair<Integer, Integer>(0,8));
		toKill.put("szczur jaskiniowy", new Pair<Integer, Integer>(0,5));
		toKill.put("wąż", new Pair<Integer, Integer>(0,1));

		start.add(new IncreaseKarmaAction(2.0));
		start.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		start.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze, będę tutaj na Ciebie oczekiwała jak wrócisz. ",
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Cóż może ktoś inny będzie moim bohaterem...",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("piwnica", "piwnicy", "basement", "storage space"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Zejście do naszej piwnicy w tawernie znajduje się po prawej stronie, schodami w dół... Powinieneś tam uważać, jest tam strasznie dużo szczurów i bodajże widziałam węże... pomożesz mi?",
				null);
	}

	private void step_2() {
		// Go kill at least a rat, a cave rat and a snake.
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("Rosa");
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new IncreaseKarmaAction(10.0));
		reward.add(new IncreaseXPAction(550));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		// the player returns to Eonna after having started the quest.
		// Eonna checks if the player has killed one of each animal race.
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT,1)),
				ConversationStates.ATTENDING, "Nareszcie bohater! Dziękuję!",
				new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.QUEST_STARTED,
				"Nie pamiętasz, obiecałeś oczyścić tą #piwnice ze szczurów?",
				null);

		npc.add(
				ConversationStates.QUEST_STARTED,
				Arrays.asList("basement", "piwnicy", "piwnica"),
				null,
				ConversationStates.ATTENDING,
				"Tak jak powiedziałam w dół schodami. Proszę wyczyść ze wszystkich szczurów i zobacz czy nie ma tam węża!",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Pomoc Rosie",
				"Piwnica w karczmie Gdańskiej jest pełna szczurów. Rosa poprosiła mnie abym pozbył się tych szczurów.",
				false);
		step_1();
		step_2();
		step_3();
	}

	@Override
	public String getName() {
		return "RosaBasementRats";
	}

		@Override
	public int getMinLevel() {
		return 0;
	}

	@Override
	public String getRegion() {
		return Region.GDANSK_CITY;
	}
	@Override
	public String getNPCName() {
		return "Adaś";
	}
}
