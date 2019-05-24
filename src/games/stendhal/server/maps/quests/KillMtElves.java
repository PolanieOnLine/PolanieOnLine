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
import games.stendhal.server.entity.npc.action.EquipItemAction;
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
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;

public class KillMtElves extends AbstractQuest {
	private static final String QUEST_SLOT = "kill_mountain_elves";
	
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
		if ("done".equals(questState)) {
			res.add("Zabiłem wszystkie elfy górskie, które Czarnoksiężnik mi zlecił.");
		}
		return res;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Czarnoksiężnik");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(
						new QuestCompletedCondition("clear_tower"),
						new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_2_OFFERED,
				"Mniej więcej możesz mi się nadać.. Więc będę miał dla Ciebie drugie zadanie. Musisz zabić wszystkie elfy górskie! Zbuntowały się przeciwko mnie, a ja nie mogę na to pozwolić. Zrobisz to?",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestActiveCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, 
				"To nie wszystko...", null);

		final List<ChatAction> start = new LinkedList<ChatAction>();
		final HashMap<String, Pair<Integer, Integer>> toKill = 
			new HashMap<String, Pair<Integer, Integer>>();
			// first number is required solo kills, second is required shared kills
			toKill.put("elf górski maskotka", new Pair<Integer, Integer>(0,3));
			toKill.put("elf górski dama", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski strażniczka", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski kapłan", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski czarownica", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski lider", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski lord", new Pair<Integer, Integer>(0,2));
			toKill.put("elf górski czarnoksiężnik", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski wojownik", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski służka", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski król", new Pair<Integer, Integer>(0,1));
			toKill.put("elf górski królowa", new Pair<Integer, Integer>(0,1));

			start.add(new SetQuestAndModifyKarmaAction(QUEST_SLOT, "start", 10.0));
			start.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(
				ConversationStates.QUEST_2_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Dobrze. Wróć jak skończysz...",
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_2_OFFERED, ConversationPhrases.NO_MESSAGES, null,
				ConversationStates.ATTENDING,
				"No cóż... już na początku wiedziałem, że się nie nadasz.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));
	}

	private void step_2() {
		/* Player has to kill the monsters in tower */
	}

	private void step_3() {
		final SpeakerNPC npc = npcs.get("Czarnoksiężnik");
		final List<ChatAction> reward = new LinkedList<ChatAction>();
			reward.add(new EquipItemAction("hełm kolczy", 1, true));
			reward.add(new IncreaseKarmaAction(20.0));
			reward.add(new IncreaseXPAction(20000));
			reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new KilledForQuestCondition(QUEST_SLOT,1)),
				ConversationStates.IDLE, "Dobrze, więc będziesz miał kolejne, ostatnie #'zadanie' ode mnie...",
				new MultipleActions(reward));

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
						new QuestInStateCondition(QUEST_SLOT, 0, "start"), new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.IDLE,
				"Jeszcze nie skończyłeś, wróć do mnie jak wybijesz &'wszystkie' elfy górskie!",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabicie elfów górskich",
				"Jako drugie zadanie, Czarnoksiężnik chce abyśmy zabili elfów górskich, a to niby wszystko dla nauki...",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public int getMinLevel() {
		return 70;
	}
	
	@Override
	public String getName() {
		return "KillMTElves";
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