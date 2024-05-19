/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.NotCondition;
import games.stendhal.server.entity.npc.condition.PlayerHasItemWithHimCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * @author zekkeq
 */
public class ZlotaCiupagaTrzyWasy extends AbstractQuest {
	private static final String QUEST_SLOT = "ciupaga_trzy_wasy";
	private final SpeakerNPC npc = npcs.get("Hadrin");

	private static final String FRIEND_1 = "goralski_kolekcjoner3";
	private static final String FRIEND_2 = "belts_collector";
	private static final String FRIEND_3 = "gloves_collector";
	private static final String FRIEND_4 = "krolewski_plaszcz";

	private static final int REQUIRED_HOURS = 24;

	private static Logger logger = Logger.getLogger(ZlotaCiupagaTrzyWasy.class);

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isQuestCompleted(FRIEND_1)) {
						if (player.isQuestCompleted(FRIEND_2)) {
							if (player.isQuestCompleted(FRIEND_3)) {
								if (player.isQuestCompleted(FRIEND_4)) {
									if(player.getLevel() >= 350) {
										if(player.getKarma() >= 2000) {
											if(player.hasKilled("azazel")) {
												if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
													raiser.say("Musisz być dzielnym wojownikiem skoro " + player.getGenderVerb("dotarłeś") + " aż tu. Mam dla ciebie zadanie, czy jesteś gotów?");
												} else if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
													raiser.say("Już się Ciebie pytałem czy chcesz ulepszyć złotą ciupagę!");
												} else if (player.isQuestCompleted(QUEST_SLOT)) {
													raiser.say("Już ulepszyłem dla Ciebie złotą ciupagę.");
													raiser.setCurrentState(ConversationStates.ATTENDING);
												} else {
													raiser.say("Dlaczego zawracasz mi głowę skoro nie " + player.getGenderVerb("ukończyłeś") + " zadania?");
													raiser.setCurrentState(ConversationStates.ATTENDING);
												}
											} else {
												npc.say("Rozmawiam tylko z osobami, które wykazały się w walce zabijając azazela.");
												raiser.setCurrentState(ConversationStates.ATTENDING);
											}
										} else {
											npc.say("Twoja karma jest zbyt słaba aby podołać temu zadaniu. Postaraj się, aby była 2000 lub więcej!");
											raiser.setCurrentState(ConversationStates.ATTENDING);
										}
									} else {
										npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 350 poziom.");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say("Jeśli pomożesz Królowi Krakowi, ja jako pierwszy o tym się dowiem.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Poprosiła mnie o pomoc Anastazja, dobrze się składa. Ty jej pomożesz.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Wesprzyj moją przyjaciółkę Eltefię w jej marzeniu.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Musisz pierw udowodnić swe dobre zamiary. Pomóż mojemu pierwszemu znajomemu, Gazdzie Bartkowi, rozwiązać jego problem.");
						raiser.setCurrentState(ConversationStates.ATTENDING);
					}
				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, null,
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("W takim razie oto potrzebna lista przedmiotów, gdzie umożliwi mi ulepszenie twej złotej ciupagi.\n"
							+"Do jej udoskonalenia potrzebuję:\n"
							+"#'1 pazur arktycznego smoka'\n"
							+"#'1 pazur niebieskiego smoka'\n"
							+"#'1 pazur czarnego smoka'\n"
							+"#'1 pazur złotego smoka'\n"
							+"#'1 złota ciupaga z dwoma wąsami'\n"
							+"#'240 sztabek złota'\n"
							+"#'2500000 money'\n"
							+"#'70 polan' oraz\n"
							+"#'3 pióro azazela'\n"
							+"#'10 magiczna krew'\n"
							+"#'5 smocza krew'\n"
							+"#'1 cudowna krew'\n"
							+"Proszę przynieś mi to wszystko naraz. Jeżeli zapomnisz co masz przynieść to powiedz #'przypomnij'. Dziękuję!");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Twoja strata.",
			new SetQuestAction(QUEST_SLOT, "rejected"));

	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Hadrin");
		final List<ChatAction> ciupagaactions = new LinkedList<ChatAction>();
		ciupagaactions.add(new DropItemAction("pazur arktycznego smoka", 1));
		ciupagaactions.add(new DropItemAction("pazur niebieskiego smoka", 1));
		ciupagaactions.add(new DropItemAction("pazur czarnego smoka", 1));
		ciupagaactions.add(new DropItemAction("pazur złotego smoka", 1));
		ciupagaactions.add(new DropItemAction("złota ciupaga z dwoma wąsami", 1));
		ciupagaactions.add(new DropItemAction("sztabka złota", 240));
		ciupagaactions.add(new DropItemAction("money", 2500000));
		ciupagaactions.add(new DropItemAction("polano", 70));
		ciupagaactions.add(new DropItemAction("pióro azazela", 3));
		ciupagaactions.add(new DropItemAction("magiczna krew", 10));
		ciupagaactions.add(new DropItemAction("smocza krew", 5));
		ciupagaactions.add(new DropItemAction("cudowna krew", 1));
		ciupagaactions.add(new SetQuestAction(QUEST_SLOT, "forging;" + System.currentTimeMillis()));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmioty", "przypomnij", "ciupaga"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								 new PlayerHasItemWithHimCondition("pazur arktycznego smoka", 1),
								 new PlayerHasItemWithHimCondition("pazur niebieskiego smoka", 1),
								 new PlayerHasItemWithHimCondition("pazur czarnego smoka", 1),
								 new PlayerHasItemWithHimCondition("pazur złotego smoka", 1),
								 new PlayerHasItemWithHimCondition("złota ciupaga z dwoma wąsami", 1),
								 new PlayerHasItemWithHimCondition("sztabka złota", 240),
								 new PlayerHasItemWithHimCondition("money", 2500000),
								 new PlayerHasItemWithHimCondition("polano", 70),
								 new PlayerHasItemWithHimCondition("pióro azazela", 3),
								 new PlayerHasItemWithHimCondition("magiczna krew", 10),
								 new PlayerHasItemWithHimCondition("smocza krew", 5),
								 new PlayerHasItemWithHimCondition("cudowna krew", 1)),
				ConversationStates.IDLE, "Widzę, że masz wszystko o co cię prosiłem. Wróć za 24 godzin, a złota ciupaga z trzema wąsami będzie gotowa. Przypomnij mi mówiąc #'nagroda'.",
				new MultipleActions(ciupagaactions));

		npc.add(ConversationStates.ATTENDING, "przypomnij",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								 new NotCondition(
								 new AndCondition(
										 new PlayerHasItemWithHimCondition("pazur arktycznego smoka", 1),
										 new PlayerHasItemWithHimCondition("pazur niebieskiego smoka", 1),
										 new PlayerHasItemWithHimCondition("pazur czarnego smoka", 1),
										 new PlayerHasItemWithHimCondition("pazur złotego smoka", 1),
										 new PlayerHasItemWithHimCondition("złota ciupaga z dwoma wąsami", 1),
										 new PlayerHasItemWithHimCondition("sztabka złota", 240),
										 new PlayerHasItemWithHimCondition("money", 2500000),
										 new PlayerHasItemWithHimCondition("polano", 70),
										 new PlayerHasItemWithHimCondition("pióro azazela", 3),
										 new PlayerHasItemWithHimCondition("magiczna krew", 10),
										 new PlayerHasItemWithHimCondition("smocza krew", 5),
										 new PlayerHasItemWithHimCondition("cudowna krew", 1)))),
				ConversationStates.IDLE, "Do jej udoskonalenia potrzebuję:\n"
								+"#'1 pazur arktycznego smoka'\n"
								+"#'1 pazur niebieskiego smoka'\n"
								+"#'1 pazur czarnego smoka'\n"
								+"#'1 pazur złotego smoka'\n"
								+"#'1 złota ciupaga z dwoma wąsami'\n"
								+"#'240 sztabek złota'\n"
								+"#'2500000 money'\n"
								+"#'70 polan' oraz\n"
								+"#'3 pióro azazela'\n"
								+"#'10 magiczna krew'\n"
								+"#'5 smocza krew'\n"
								+"#'1 cudowna krew'\n"
								+"Proszę przynieś mi to wszystko naraz. Jeżeli zapomnisz co masz przynieść to powiedz #'przypomnij'. Dziękuję!", null);
	}

	private void step_3() { 
		final int delay = REQUIRED_HOURS * TimeUtil.SECONDS_IN_MINUTE;

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("złota", "ciupaga", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, delay))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Wciąż pracuje nad ulepszeniem twojej złotej ciupagi. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("złota", "ciupaga", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new TimePassedCondition(QUEST_SLOT, 1, delay)),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Warto było czekać. A oto i ona, czyż nie jest wspaniała?!");
					player.addXP(1000000);
					player.addKarma(200);
					final Item zlotaCiupagaZTrzemaWasami = SingletonRepository.getEntityManager().getItem("złota ciupaga z trzema wąsami");
					zlotaCiupagaZTrzemaWasami.setBoundTo(player.getName());
					player.equipOrPutOnGround(zlotaCiupagaZTrzemaWasami);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Złota Ciupaga z Trzema Wąsami",
			"Hadrin wzmocni twoją Złotą Ciupagę z Dwoma Wąsami.",
			true);
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
		final String questState = player.getQuest(QUEST_SLOT);
		res.add(player.getGenderVerb("Spotkałem") + " się z Hadrinem.");
		res.add("Hadrin może ulepszyć moją złotą ciupagę.");
		if ("rejected".equals(questState)) {
			res.add("Nie zamierzam ulepszać swej złotej ciupagi.");
			return res;
		}
		res.add(player.getGenderVerb("Udałem") + " się do Hadrina w celu ulepszenia ciupagi. Kazał mi przynnieść kilka przedmiotów. Gdybym " + player.getGenderVerb("zapomniał") + " co mam przynieść mam mu powiedzieć: przypomnij."); 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add(player.getGenderVerb("Dostarczyłem") + " potrzebne przedmioty! Hadrin zabrał się za ulepszenie mojej ciupagi.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT,1,REQUIRED_HOURS).fire(player, null, null)) {
				res.add("Podobno Hadrin skończył moją ciupagę. Hasło: nagroda.");
			} else {
				res.add("Po ciupagę mam zgłosić się za 24 godzin. Hasło: nagroda.");
			}
			return res;
		} 
		res.add("Warto było czekać na ciupagę z trzema wąsami. Ta potężna i piękna broń należy teraz do mnie.");
		if (isCompleted(player)) {
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
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
		return "Złota Ciupaga z Trzema Wąsami";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
