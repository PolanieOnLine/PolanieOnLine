/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

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
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;

public class ClearTower extends AbstractQuest {
	private static final String QUEST_SLOT = "clear_tower";
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Czarnoksiężnika w jakieś starej wieży.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add("Odmówiłem Czarnoksiężnikowi pomocy.");
		return res;
		}
		res.add("Postanowiłem pomóc Czarnoksiężnikowi.");
		if (("start".equals(questState) && player.hasKilled("starszy gargulec") && player.hasKilled("mroczny gargulec")
				&& player.hasKilled("trujący gargulec") && player.hasKilled("gargulec")
				&& player.hasKilled("nietoperz wampir") && player.hasKilled("nietoperz")
				&& player.hasKilled("pająk") && player.hasKilled("pająk ptasznik")
				&& player.hasKilled("wściekły szczur") && player.hasKilled("krwiożerczy szczur")
				&& player.hasKilled("szczur zombie")) || "done".equals(questState)) {
			res.add("Wieża została wyczyszczona.");
		}
		if ("done".equals(questState)) {
			res.add("Czarnoksiężnik będzie miał dla mnie drugie zadanie.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Czarnoksiężnik");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Jako pierwsze twe zadanie ode mnie, masz wyczyścić wieżę z zalęgłych tutaj potworów. Zrobisz to?",
				null);

		final List<ChatAction> start = new LinkedList<ChatAction>();
		final HashMap<String, Pair<Integer, Integer>> toKill = 
			new HashMap<String, Pair<Integer, Integer>>();
			// first number is required solo kills, second is required shared kills
			toKill.put("starszy gargulec", new Pair<Integer, Integer>(0,1));
			toKill.put("mroczny gargulec", new Pair<Integer, Integer>(0,1));
			toKill.put("trujący gargulec", new Pair<Integer, Integer>(0,1));
			toKill.put("gargulec", new Pair<Integer, Integer>(0,1));
			toKill.put("nietoperz", new Pair<Integer, Integer>(0,1));
			toKill.put("nietoperz wampir", new Pair<Integer, Integer>(0,1));
			toKill.put("pająk ptasznik", new Pair<Integer, Integer>(0,1));
			toKill.put("pająk", new Pair<Integer, Integer>(0,1));
			toKill.put("wściekły szczur", new Pair<Integer, Integer>(0,1));
			toKill.put("krwiożerczy szczur", new Pair<Integer, Integer>(0,1));
			toKill.put("szczur zombie", new Pair<Integer, Integer>(0,1));

			start.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 5.0));
			start.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze. Wróć jak skończysz.",
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"No cóż... już na początku wiedziałem, że się nie nadasz.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void step_2() {
		/* Player has to kill the monsters in tower */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Czarnoksiężnik");
		final List<ChatAction> reward = new LinkedList<ChatAction>();
			reward.add(new IncreaseKarmaAction(15.0));
			reward.add(new IncreaseXPAction(10000));
			reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT,1)),
				ConversationStates.IDLE, "Dobrze, więc będziesz miał drugie #'zadanie' ode mnie...",
				new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.IDLE,
				"Jeszcze nie skończyłeś, wróć do mnie jak wybijesz &'wszystkie' potwory w wieży.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Wyczyszczenie wieży",
				"Czarnoksiężnik kazał wyczyścić wieżę, aby sprawdzić jakie podstawowe umiejętności posiadasz.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public int getMinLevel() {
		return 35;
	}
	
	@Override
	public String getName() {
		return "ClearTower";
	}
	
	@Override
	public String getNPCName() {
		return "Czarnoksiężnik";
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public String getRegion() {
		return Region.ZAKOPANE_CITY;
	}
}
