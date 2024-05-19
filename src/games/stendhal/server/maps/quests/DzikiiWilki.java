/***************************************************************************
 *                   (C) Copyright 2016-2021 - Stendhal                    *
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
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
import games.stendhal.server.util.TimeUtil;
import marauroa.common.Pair;

/**
 * QUEST: DzikiiWilki
 * <p>
 * PARTICIPANTS:
 * <li> Farmer Mścisław
 * <p>
 * STEPS:
 * <li> **
 * <p>
 * REWARD:
 * <li> 450 XP, 15 karma
 * <li> part of vegetables farmer's
 * <p>
 * REPETITIONS:
 * <li> after 2 days
 */
public class DzikiiWilki extends AbstractQuest {
	private static final String QUEST_SLOT = "dzikie_zwierzyny";
	private final SpeakerNPC npc = npcs.get("Farmer Mścisław");

	private static final int WEEK_IN_MINUTES = TimeUtil.MINUTES_IN_DAY;

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Ja to dobry farmer jestem wiesz? W Gdańsku występuje tylko jedna jedyna farma, czyli moja. Zawsze w nocy przychodzą tutaj dziki i wilki czasami nawet lisy... Pomożesz mi?",
				null);
				
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
						 new TimePassedCondition(QUEST_SLOT, 1, WEEK_IN_MINUTES)),
				ConversationStates.QUEST_OFFERED,
				"Te dzikie zwierzęta po nocach wyjadają moje żniwa. Pomożesz?",
				null);
				
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				null,
				ConversationStates.QUEST_OFFERED,
				null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						if (player.getQuest(QUEST_SLOT).startsWith("killed;")) {
							final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
							final long delay = WEEK_IN_MINUTES * TimeUtil.SECONDS_IN_DAY;
							final long timeRemaining = Long.parseLong(tokens[1]) + delay - System.currentTimeMillis();
							if (timeRemaining > 0) {
								raiser.say("Boję się, że ponownie zwierzęta te wrócą na moją farmę, proszę.. wróć za #'" + TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L)) + "'.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
								return;
							} 
							raiser.say("Znowu potrzebuję twojej pomocy w sprawie tych dzikich zwierząt. Pomógłbyś mi ponownie?");
						}
					}
				});
				
		final Map<String, Pair<Integer, Integer>> toKill = new TreeMap<String, Pair<Integer, Integer>>();
		// first number is required solo kills, second is required shared kills
		toKill.put("wilk", new Pair<Integer, Integer>(0,1));
		toKill.put("dzik", new Pair<Integer, Integer>(0,1));
		toKill.put("lisica", new Pair<Integer, Integer>(0,1));

		final List<ChatAction> start = new LinkedList<ChatAction>();
		start.add(new IncreaseKarmaAction(15.0));
		start.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
		start.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(
				ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES,
				null,
				ConversationStates.ATTENDING,
				"Świetnie. Poczekam tutaj na Ciebie. Pamiętaj, musisz ubić co najmniej jednego wilka, dzika i lisice!",
				new MultipleActions(start));

		npc.add(ConversationStates.QUEST_OFFERED, 
				ConversationPhrases.NO_MESSAGES, 
				null,
				ConversationStates.ATTENDING,
				"Może i masz racje... Chyba nie powinniśmy znęcać się nad tymi biednymi zwierzętami.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -15.0));
			
	}

	private void step_2() {
		/* Player has to kill the creatures*/
	}

	private void step_3() {
		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new EquipItemAction("marchew", 3));
		reward.add(new EquipItemAction("szpinak", 3));
		reward.add(new EquipItemAction("kapusta", 3));
		reward.add(new EquipItemAction("kalafior", 3));
		reward.add(new EquipItemAction("sałata", 3));
		reward.add(new EquipItemAction("brokuł", 3));
		reward.add(new EquipItemAction("cebula", 3));
		reward.add(new EquipItemAction("cukinia", 3));
		reward.add(new EquipItemAction("por", 3));
		reward.add(new IncreaseKarmaAction(15.0));
		reward.add(new IncreaseXPAction(450));
		reward.add(new SetQuestAction(QUEST_SLOT, "killed;1"));
		reward.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1)),
				ConversationStates.ATTENDING, 
				"Dziękuję! Proszę weź część moich żniw w dowód uznania.",
				new MultipleActions(reward));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(
						new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, 
				"Nie pamiętasz? Obiecałeś mi ubić parę dzikich zwierząt z okolic mojej farmy.",
				null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Dzikie Zwierzyny",
				"Farmer Mścisław poprosił mnie o pomoc w pozbyciu się paru dzików i wilków z okolic jego farmy. Cały czas wyjadają jego warzywa.",
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
		res.add(player.getGenderVerb("Spotkałem") + " Mścisława tuż obok swojej farmy.");
		final String questState = player.getQuest(QUEST_SLOT, 0);
		if ("rejected".equals(questState)) {
			res.add(player.getGenderVerb("Odmówiłem") + " farmerowi pomocy.");
		return res;
		}
		res.add(player.getGenderVerb("Postanowiłem") + " pomóc farmerowi Mścisławowi.");
		if (("start".equals(questState) && player.hasKilled("wilk") && player.hasKilled("dzik") && player.hasKilled("lisicia")) || "done".equals(questState)) {
			res.add("Okolice farmy Mścisława zostały oczyszczone od dzikich zwierząt.");
		} else if(isRepeatable(player)){
			res.add("Farmer Mścisław pewnie znowu potrzebuje mojej pomocy w sprawie tych dzikich zwierząt. Pójdę sprawdzić.");
		}
		if ("done".equals(questState)) {
			res.add("Mścisław jest zadowolony i oddał mi część swoich żniw.");
		}
		return res;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Dzikie Zwierzyny";
	}

	@Override
	public int getMinLevel() {
		return 5;
	}

	@Override
	public String getRegion() {
		return Region.GDANSK_CITY;
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT,"killed"),
				 new TimePassedCondition(QUEST_SLOT, 1, TimeUtil.MINUTES_IN_HOUR*24*2)).fire(player,null, null);
	}
	
	@Override
	public boolean isCompleted(final Player player) {
		return new QuestStateStartsWithCondition(QUEST_SLOT,"killed").fire(player, null, null);
	}
}
