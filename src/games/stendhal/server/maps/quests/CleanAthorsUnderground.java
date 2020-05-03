/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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

import games.stendhal.common.MathHelper;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import marauroa.common.Pair;

/**
 * QUEST: Clean Athors underground
 *
 * PARTICIPANTS: <ul>
 * <li> NPC on Athor island
 * <li> one of each creature in Athor underground
 * </ul>
 *
 * STEPS:<ul>
 * <li> John on Athor island asks players to kill some creatures of the dungeon for him, cause he can't explore it otherwise
 * <li> Kill them for him and go back to the NPC to get your reward
 * </ul>
 * 
 *
 * REWARD:<ul>
 * <li> 80000 XP
 * <li> 10 greater potion
 * <li> some karma
 * </ul>
 *
 * REPETITIONS: <ul><li>once in a week</ul>
 * 
 * @author Vanessa Julius, idea by anoyyou

 */

public class CleanAthorsUnderground extends AbstractQuest {

	private static final String QUEST_SLOT = "clean_athors_underground";
	private static final int WEEK_IN_MINUTES = MathHelper.MINUTES_IN_ONE_HOUR * 24 * 7;
	
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
		
	}
	
	private void step_1() {
		final SpeakerNPC npc = npcs.get("John");

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Moja żona Jane i ja jesteśmy na wakacjach na wyspie Athor. #Niestety nie możemy zwiedzić całej wyspy ponieważ okropne #potwory uniemożliwiają nam to za każdym razem. Możesz nam pomóc zabijając parę z nich, aby uprzyjemnić nam wakacje?",
				null);

		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("Unfortunately", "Niestety"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Tak niestety. Chcieliśmy spedzić wspaniale czas, ale jedyne co zrobiliśmy to spędziliśmy czas na plaży.",
				null);
		
		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("creatures", "potwory", "potworów"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Chcemy zwiedzić pierwszą część podziemi, która wygląda na bardzo interesującą, ale te okropne coś tam rzucają się na nas, nawet mumie!",
				null);
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)), new QuestStateStartsWithCondition(QUEST_SLOT, "killed")),
				ConversationStates.ATTENDING,
				null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, WEEK_IN_MINUTES, "Te #potwory nie wrócą szybko i dzięki temu możemy zobaczyć wspaniałe miejsca. Wróć za"));
		
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Te #potwory wróciły od tamtego czasu, gdy nam pomogłeś. Możesz znów nam pomóc?",
				null);

	

		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		toKill.put("mumia", new Pair<Integer, Integer>(0,1));
		toKill.put("mumia królewska", new Pair<Integer, Integer>(0,1));
		toKill.put("mnich",new Pair<Integer, Integer>(0,1));
		toKill.put("mnich ciemności",new Pair<Integer, Integer>(0,1));
		toKill.put("nietoperz",new Pair<Integer, Integer>(0,1));
		toKill.put("brązowy glut",new Pair<Integer, Integer>(0,1));
		toKill.put("zielony glut",new Pair<Integer, Integer>(0,1));
		toKill.put("czarny glut",new Pair<Integer, Integer>(0,1));
		toKill.put("minotaur",new Pair<Integer, Integer>(0,1));
		toKill.put("błękitny smok",new Pair<Integer, Integer>(0,1));
		toKill.put("kamienny golem",new Pair<Integer, Integer>(0,1));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
		actions.add(new SetQuestAction(QUEST_SLOT, "start"));
		actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Cudownie! Nie możemy się doczekać na twój powrót. Zabij po jednym z tych potworów w podziemiach wyspy Athor. Założe się, że dostaniesz je wszystkie!",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Oh nie ważne. W takim razie pójdziemy dalej się opalać. Nie dlatego, że jesteśmy zmęczeni tym...",
				new SetQuestAction(QUEST_SLOT, "rejected"));
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {

		final SpeakerNPC npc = npcs.get("John");


		final List<ChatAction> actions = new LinkedList<ChatAction>();
	    actions.add(new EquipItemAction("wielki eliksir", 20));
		actions.add(new IncreaseXPAction(80000));
		actions.add(new SetQuestAction(QUEST_SLOT, "killed;1"));
		actions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));
		actions.add(new IncreaseKarmaAction(100.0));

		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Wspaniale! Jak widzę zabiłeś te okropne potwory! Mam nadzieję, że nie wrócą zbyt szybko, bo nie będziemy mieli szansy na zwiedzenie paru miejsc."  + " Proszę weż te duże eliksiry jako nagrodę za twoją pomoc.",
				new MultipleActions(actions));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Proszę uwolnij te wspaniałe miejsca od tych okropnych potworów!",
				null);
		
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Posprzątaj podziemia Athor",
				"John i jego żona Jane chcą zwiedzić podziemia Athor podczas swoich wakacji, ale niestety nie mogą.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public List<String> getHistory(final Player player) {
			final List<String> res = new ArrayList<String>();
			if (!player.hasQuest(QUEST_SLOT)) {
				return res;
			}
			if (!isCompleted(player)) {
				res.add("W podziemiach Athor muszę zabić potwora z każdego rodzaju, aby John i Jane mieli miłe wakacje!");
			} else if(isRepeatable(player)){
				res.add("Minęło sporo czasu, gdy spotkałem Johna i Jane na wyspie Athor. Może wciąż potrzebują mojej pomocy.");
			} else {
				res.add("Zabiłem pare potworów, a John i Jane mogą w końcu cieszyć się swoimi wakacjami! Nie będą potrzebowali mojej pomocy przez kilka następnych dni.");
			}
			return res;
	}


	@Override
	public String getName() {
		return "CleanAthorsUnderground";

	}
	
	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}

	@Override
	public String getNPCName() {
		return "John";
	}

	@Override
	public String getRegion() {
		return Region.ATHOR_ISLAND;
	}
}
