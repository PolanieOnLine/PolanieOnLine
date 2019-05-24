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
 /* QUEST CREATED BY KARAJUSS */
package games.stendhal.server.maps.quests;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.action.StartRecordingKillsAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KilledForQuestCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.Region;
import marauroa.common.Pair;

public class SkorySmokow extends AbstractQuest {
	private static final String QUEST_SLOT = "aligern_key";
	
	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add("Spotkałem Aligerna w chatce na plaży w Gdańsku.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add("Nie chcę pomagać Aligern'owi.");
		}
		if (questState.equals("start") ||  questState.equals("done")) {
			res.add("Chcę pomóc Aligern'owi w sprawie zabicia smoków. Muszę zabić smoki i przynieść ich skóry.");
		}
		if (player.isEquipped("skóra zielonego smoka") && player.isEquipped("skóra czerwonego smoka") && player.isEquipped("skóra niebieskiego smoka") && questState.equals("start")
				|| questState.equals("done")) {
			res.add("Posiadam wszystkie zebrane skóry.");
		}
		if (questState.equals("done")) {
			res.add("Zabiłem smoki jak i zaniosłem potrzebne Aligern'owi skóry smoków. W podzięce dał mi swój klucz. Powiedział, że służy on do otwarcia drzwi, które znadują się po prawej stronie i dalej można zejść do jego Wielkiego Mistrza.");
		}
		return res;
	}
	
	private void step_1(){
		final SpeakerNPC npc = npcs.get("Aligern");
		
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję, że mi pomogłeś w sprawie zabicia smoków, dziękuję również za to, że przyniosłeś do mnie skóry tych strasznych bestii. W końcu moja kolekcja się poszerzyła.",
				null);
				
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES, 
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Jak już przybyłeś tutaj do mnie to chciałbym, abyś zrobił coś dla mnie, dobrze? Pomógłbyś mi zabijając parę #'potworów'?",
				null);
			
		final Map<String, Pair<Integer, Integer>> zabic = new TreeMap<String, Pair<Integer, Integer>>();
			zabic.put("zielony smok",new Pair<Integer, Integer>(0,1));
			zabic.put("czerwony smok",new Pair<Integer, Integer>(0,1));
			zabic.put("błękitny smok",new Pair<Integer, Integer>(0,1));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
			actions.add(new SetQuestAction(QUEST_SLOT, "start"));
			actions.add(new IncreaseKarmaAction(5.0));
			actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, zabic));
		
		npc.add(
				ConversationStates.QUEST_OFFERED,
				Arrays.asList("creatures", "potwory", "potworów"),
				null,
				ConversationStates.QUEST_OFFERED,
				"Chcę, abyś zabił dla mnie smoki, a dokładnie:\n " 
				+ "zielonego smoka,\n"
				+ " czerwonego smoka,\n"
				+ " błękitnego smoka.\n"
				+ " Musisz przynieść również mi ich skóry, potrzebuję je do mojej kolekcji.",
				null);
		
		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dziękuję bardzo. Będę czekać na twój powrót! Pamiętaj, że masz zabić te smoki i przynieść do mnie ich skóry!\n"
			+"Zabij następujące smoki:"
			+"#'zielony smok', "
			+"#'czerwony smok' oraz"
			+"#' błękitny smok'. Pamiętaj również, abyś przyniósł do mnie ich skóry:\n"
			+"#'1 skóra zielonego smoka',\n"
			+"#'1 skóra czerwonego smoka',\n"
			+"#'1 skóra niebieskiego smoka'.",
			new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.ATTENDING,
			"Dobrze. Mam dla ciebie nagrodę...",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -5.0));
	}
	
	private void step_2(){
	/* Player has to kill the creatures*/
	}
	
	private void step_3(){
		final SpeakerNPC npc = npcs.get("Aligern");
				
		final List<ChatAction> reward = new LinkedList<ChatAction>();
	    reward.add(new DropItemAction("skóra zielonego smoka", 1));
		reward.add(new DropItemAction("skóra czerwonego smoka", 1));
		reward.add(new DropItemAction("skóra niebieskiego smoka", 1));
	    reward.add(new EquipItemAction("kluczyk Aligerna", 1, true));
	    reward.add(new EquipItemAction("wielki eliksir", 10));
		reward.add(new IncreaseXPAction(75000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));
		reward.add(new IncreaseKarmaAction(35.0));
		
		LinkedList<String> triggers = new LinkedList<String>();
		triggers.addAll(ConversationPhrases.FINISH_MESSAGES);
		triggers.addAll(ConversationPhrases.QUEST_MESSAGES);		
		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new KilledForQuestCondition(QUEST_SLOT, 1),
						new PlayerHasItemWithHimCondition("skóra zielonego smoka",1),
						new PlayerHasItemWithHimCondition("skóra czerwonego smoka",1),
						new PlayerHasItemWithHimCondition("skóra niebieskiego smoka",1)),
				ConversationStates.ATTENDING, 
				"Wspaniale! Widzę, że zabiłeś te okropne potwory! Mam nadzieję, że nie wrócą zbyt szybko."  + " Proszę weź te wielkie eliksiry oraz mój klucz, który służy do otwarcia drzwi po prawej stronie w chatce jako nagrodę za twą pomoc.",
				new MultipleActions(reward));

		npc.add(ConversationStates.ATTENDING, 
				triggers,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
						new NotCondition(
						new AndCondition(new KilledForQuestCondition(QUEST_SLOT, 1),
								new PlayerHasItemWithHimCondition("skóra zielonego smoka",1),
								new PlayerHasItemWithHimCondition("skóra czerwonego smoka",1),
								new PlayerHasItemWithHimCondition("skóra niebieskiego smoka",1)))),
				ConversationStates.ATTENDING, 
				"Musisz te straszne bestie zgładzić!\n"
				+"Proszę zabij te smoki:\n"
				+"#'zielony smok',\n"
				+"#'czerwony smok',\n"
				+"#'błękitny smok'.\n"
				+" Potrzebuję również ich skór do swojej kolekcji. Proszę przynieść mi je wszystkie:\n"
				+"#'1 skóra zielonego smoka',\n"
				+"#'1 skóra czerwonego smoka',\n"
				+"#'1 skóra niebieskiego smoka'.", null);
		
	}
	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Zabij smoki dla Aligern'a",
				"Mam zabić smoki i zdobyć rzadke skóry smoków dla Aligerna. Pewnie mu chodziło o zieloną, czerwoną i niebieską skórę smoka.",
				false);
		step_1();
		step_2();
		step_3();
	}
	
	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}
	
	@Override
	public String getName() {
		return "AligernQuest";
	}
	
	@Override
	public int getMinLevel() {
		return 200;
	}
	
	@Override
	public String getNPCName() {
		return "Aligern";
	}

	@Override
	public String getRegion() {
		return Region.GDANSK_CITY;
	}
	
}