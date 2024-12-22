/***************************************************************************
 *                   (C) Copyright 2010-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.EquipItemAction;
import games.stendhal.server.entity.npc.action.IncreaseXPAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;

public class PierscienMagnata extends AbstractQuest {
	private static final String QUEST_SLOT = "pierscien_magnata";
	private final SpeakerNPC npc = npcs.get("Jubiler Zdzichu");

	private static final String CLUB_THORNS_QUEST_SLOT = "club_thorns";
	private static final String KILL_DRAGONS_QUEST_SLOT = "kill_dragons"; 
	private static final String VAMPIRE_SWORD_QUEST_SLOT = "vs_quest"; 
	private static final String IMMORTAL_SWORD_QUEST_SLOT = "immortalsword_quest";
	private static final String FIND_RAT_KIDS_QUEST_SLOT = "find_rat_kids";
	private static final String FIND_GHOSTS_QUEST_SLOT = "find_ghosts";
	private static final String SAD_SCIENTIST_QUEST_SLOT = "sad_scientist"; 
	private static final String PIERSCIEN_BARONA_QUEST_SLOT = "pierscien_barona";

	private static Logger logger = Logger.getLogger(PierscienMagnata.class);

	private void checkLevelHelm() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isBadBoy()){ 
						raiser.say("Zbrodniarz, który na brata swego rękę podniósł, nie znajdzie spokoju w moich progach! Odejdź, nim złorzeczenia sprowadzą na ciebie gniew bogów. Oczyść swe imię, a może cię wysłucham.");
					} else {
						if (player.isQuestCompleted(PIERSCIEN_BARONA_QUEST_SLOT)) {
							if (player.getLevel() >= 500) {
								if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
									raiser.say("Pragniesz zdobyć pierścień magnata, symbol potęgi, roztropności i łaski bogów? Niechaj twe czyny przemówią, czy jesteś godzien.");
								} else if (player.isQuestCompleted(QUEST_SLOT)) {
									raiser.say("Pierścień został już powierzony w twe ręce. Odejdź w pokoju, wędrowcze, i pamiętaj: moc to brzemię, nie przywilej.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Widzę, że nosisz pierścień barona. To znak, że przebyłeś trudną ścieżkę. Lecz powiadam ci, młodzieńcze, że na mądrość i potęgę Magnata wciąż musisz zasłużyć. Powróć, gdy twe czyny zyskają uznanie bogów, a twe doświadczenie sięgnie 500 cykli księżyca.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Nie widzę pierścienia barona w twoim posiadaniu. Niechaj twe serce cię prowadzi, by zdobyć go w imię chwały i odwagi. Wróć, gdy sprostasz temu wyzwaniu.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					}
				}
			}); 

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Ale wpierw sprawdzę czy masz wszystkie zadania zrobione nim dostaniesz #pierścień.");
					player.addKarma(10);
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Nie to nie.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void checkCollectingQuests() {
		npc.add(ConversationStates.IDLE,
			ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
							 new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT)),
			ConversationStates.ATTENDING,
			"Witaj, wędrowcze! Widzę, że dzielność twa została uwieńczona pierścieniem barona. Czy masz w sobie odwagę i mądrość, by sięgnąć po #pierścień magnata, symbol władzy i szacunku tej ziemi?",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"), 
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new OrCondition(new QuestNotCompletedCondition(CLUB_THORNS_QUEST_SLOT),
							 new QuestNotCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING, 
			"Twoja podróż wiedzie przez jeszcze inne próby. W Kotochu czekają #wyzwania, które musisz przezwyciężyć, zanim znów stanę przed tobą. Bogowie niechaj cię prowadzą!",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"), 
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new OrCondition(new QuestNotCompletedCondition(KILL_DRAGONS_QUEST_SLOT),
							 new QuestNotCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT))),
			ConversationStates.ATTENDING, 
			"Nie przyniosłeś pomocy małej Alicji lub nie zdobyłeś przeklętego miecza wampira. Powróć, gdy wypełnisz te powinności.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new OrCondition(new QuestNotCompletedCondition(FIND_RAT_KIDS_QUEST_SLOT),
							 	 	 	 	 new QuestNotCompletedCondition(FIND_GHOSTS_QUEST_SLOT))),
			ConversationStates.ATTENDING, 
			"Musisz odnaleźć błąkające się duchy, które utraciły drogę, lub dzieci Agnus, które wciąż pozostają w rozpaczy.",
			null);

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
							 new QuestNotStartedCondition(QUEST_SLOT),
							 new QuestNotCompletedCondition(SAD_SCIENTIST_QUEST_SLOT)),
			ConversationStates.ATTENDING, 
			"Twoje wysiłki są godne pochwały, lecz czarne spodnie, symbol smutku i uporu, wciąż ci umykają. Powróć, gdy je zdobędziesz.",
			null);
	}

	private void requestItem() {
		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(
					new QuestCompletedCondition(PIERSCIEN_BARONA_QUEST_SLOT),
					new QuestNotStartedCondition(QUEST_SLOT),
					new QuestCompletedCondition(SAD_SCIENTIST_QUEST_SLOT),
					new QuestCompletedCondition(FIND_GHOSTS_QUEST_SLOT),
					new QuestCompletedCondition(FIND_RAT_KIDS_QUEST_SLOT),
					new QuestCompletedCondition(VAMPIRE_SWORD_QUEST_SLOT),
					new QuestCompletedCondition(KILL_DRAGONS_QUEST_SLOT),
					new QuestCompletedCondition(CLUB_THORNS_QUEST_SLOT),
					new QuestCompletedCondition(IMMORTAL_SWORD_QUEST_SLOT)),
			ConversationStates.ATTENDING, "Niech słowa me będą #wyzwaniem, które bogowie ci zsyłają. Aby zdobyć #pierścień magnata, przynieś mi dary godne tej ziemi:"
					+ "#'pierścień barona' – dowód twej odwagi\n"
					+ "#'70 sztabek srebra' – łzy Peruna\n" 
					+ "#'70 sztabek mithrilu' – oddech Swaroga\n"
					+ "#'250 sztabek złota' – blask Dziwii\n"
					+ "#'100 bryłek mithrilu' – dar Łady\n"
					+ "#'200 bryłek złota' – ofiara dla Mokoszy\n"
					+ "#'500,000 monet' – dziedzictwo twego rodu\n",
			new SetQuestAction(QUEST_SLOT, "start"));

		final List<ChatAction> reward = new LinkedList<ChatAction>();
		reward.add(new DropItemAction("sztabka srebra",70));
		reward.add(new DropItemAction("sztabka mithrilu",70));
		reward.add(new DropItemAction("sztabka złota",250)); 
		reward.add(new DropItemAction("bryłka mithrilu",100));
		reward.add(new DropItemAction("bryłka złota",200));
		reward.add(new DropItemAction("money",500000));
		reward.add(new DropItemAction("pierścień barona",1));
		reward.add(new EquipItemAction("pierścień magnata", 1, true));
		reward.add(new IncreaseXPAction(500000));
		reward.add(new SetQuestAction(QUEST_SLOT, "done"));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new PlayerHasItemWithHimCondition("sztabka srebra",70),
					new PlayerHasItemWithHimCondition("sztabka mithrilu",70),
					new PlayerHasItemWithHimCondition("sztabka złota",250),
					new PlayerHasItemWithHimCondition("bryłka mithrilu",100),
					new PlayerHasItemWithHimCondition("bryłka złota",200),
					new PlayerHasItemWithHimCondition("money",500000),
					new PlayerHasItemWithHimCondition("pierścień barona",1)),
			ConversationStates.ATTENDING, "Twoje wysiłki zostały docenione. Przyjmij pierścień magnata – symbol chwały, mądrości i opieki bogów. Noś go z honorem, wędrowcze.",
			new MultipleActions(reward));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("challenge", "wyzwanie", "pierścień", "pierscien"),
			new AndCondition(
					new QuestInStateCondition(QUEST_SLOT,"start"),
					new NotCondition(
							new AndCondition(new PlayerHasItemWithHimCondition("sztabka srebra",70),
							new PlayerHasItemWithHimCondition("sztabka mithrilu", 70),
							new PlayerHasItemWithHimCondition("sztabka złota", 250),
							new PlayerHasItemWithHimCondition("bryłka mithrilu", 100),
							new PlayerHasItemWithHimCondition("bryłka złota", 200),
							new PlayerHasItemWithHimCondition("money", 500000),
							new PlayerHasItemWithHimCondition("pierścień barona", 1)))),
			ConversationStates.ATTENDING, "Wciąż brakuje mi darów, które ci wymieniłem:\n"
					+ " - 70 sztabek srebra – łzy Peruna\n" 
					+ " - 70 sztabek mithrilu – oddech Swaroga\n"
					+ " - 250 sztabek złota – blask Dziwii\n"
					+ " - 100 bryłek mithrilu – dar Łady\n"
					+ " - 200 bryłek złota – ofiara dla Mokoszy\n"
					+ " - 500,000 monet – dziedzictwo twego rodu\n"
					+ " - pierścień barona – dowód twej odwagi\n"
					+ "Niech twe kroki poprowadzi mądrość. Czekam tu na ciebie, młody śmiałku.", null);
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Status Społeczny: Magnat",
			"Zdobądź pierścień magnata, przechodząc próby wyznaczone przez Zdzicha, mistrza jubilerstwa i strażnika pradawnych tradycji.",
			true);

		checkLevelHelm(); 
		checkCollectingQuests();
		requestItem();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		res.add(player.getGenderVerb("Spotkałem") + " Zdzicha, mistrza jubilerstwa i strażnika starodawnych tradycji.");
		res.add("Zaprosił mnie do podjęcia próby zdobycia pierścienia godnego magnata.");
		final String questState = player.getQuest(QUEST_SLOT);
		if (questState.equals("rejected")) {
			res.add(player.getGenderVerb("Odrzuciłem") + " propozycję Zdzicha, nie ulegając pokusie błyskotek.");
			return res;
		} 
		if (questState.equals("start")) {
			return res;
		} 
		res.add("Zdzichu poprosił, bym " + player.getGenderVerb("przyniósł") + " mu kilka cennych przedmiotów. Kiedy je zgromadzę, mam wypowiedzieć słowo: pierścień.");
		if (questState.equals("start")) {
			return res;
		} 
		res.add("Po spełnieniu próśb Zdzicha " + player.getGenderVerb("otrzymałem") + " pierścień magnata – symbol władzy i szacunku.");

		if (isCompleted(player)) {
			return res;
		} 

		// Jeśli stan zadania nie pasuje do powyższych warunków, dodaj informacje debugujące
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	@Override
	public String getName() {
		return "Pierścień Magnata";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
