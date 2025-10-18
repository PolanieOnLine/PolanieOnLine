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
													raiser.say("Skoro " + player.getGenderVerb("dotarłeś") + " aż do mojej kuźni, żar i piach nie są ci obce. Mam dla ciebie dzieło, podejmiesz się?");
												} else if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
													raiser.say("Już pytałem, czy pragniesz nadać ciupadze trzeci wąs!");
												} else if (player.isQuestCompleted(QUEST_SLOT)) {
													raiser.say("Twoja ciupaga już lśni trzema wąsami. Nie mam tu dla ciebie kolejnej roboty.");
													raiser.setCurrentState(ConversationStates.ATTENDING);
												} else {
													raiser.say("Nie wracaj, dopóki nie " + player.getGenderVerb("ukończyłeś") + " tego, o co cię prosiłem.");
													raiser.setCurrentState(ConversationStates.ATTENDING);
												}
											} else {
												npc.say("Wróć, gdy Azazel poczuje ostrze twojej ciupagi.");
												raiser.setCurrentState(ConversationStates.ATTENDING);
											}
										} else {
											npc.say("Twoje imię brzmi jeszcze zbyt lekko; niech twoja karma sięgnie dwóch tysięcy.");
											raiser.setCurrentState(ConversationStates.ATTENDING);
										}
									} else {
										npc.say("Najpierw osiągnij trzysta pięćdziesiąty krąg doświadczenia, potem pogadamy.");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say("Dopóki Król Krak czeka na pomoc, moje kowadło nie ruszy dla ciebie.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Anastazja liczy na ciebie. Pomóż jej, a wtedy wróć.");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Najpierw spełnij marzenie Eltefii.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Najpierw pomóż Gazdzie Bartkowi i pokaż, że serce masz po właściwej stronie.");
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
					raiser.say("Słuchaj uważnie. To lista darów, dzięki którym ciupaga otrzyma trzeci wąs.\n"
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
                                                        +"#'1 wywar wąsatych smoków'\n"
                                                        +"Wywar uwarz w kotle Draconii w kuźni Józka.\n"
                                                        +"Przynieś wszystko naraz. Jeśli zapomnisz, powiedz #'przypomnij'.");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Jak chcesz, moje kowadło poczeka na innego.",
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
                ciupagaactions.add(new DropItemAction("wywar wąsatych smoków", 1));
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
                                                                 new PlayerHasItemWithHimCondition("cudowna krew", 1),
                                                                 new PlayerHasItemWithHimCondition("wywar wąsatych smoków", 1)),
				ConversationStates.IDLE, "Widzę, że przyniosłeś wszystko naraz. Wróć za dwadzieścia cztery godziny, a ciupaga z trzema wąsami będzie czekać. Przypomnij mi mówiąc #'nagroda'.",
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
                                                                                 new PlayerHasItemWithHimCondition("cudowna krew", 1),
                                                                                 new PlayerHasItemWithHimCondition("wywar wąsatych smoków", 1)))),
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
                                                                +"#'1 wywar wąsatych smoków'\n"
                                                                +"Wywar uwarzysz w kotle Draconii obok mnie.\n"
                                                                +"Przynieś wszystko naraz. Jeśli zapomnisz, powiedz #'przypomnij'.", null);
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
			new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Młoty nadal biją nad twoją ciupagą. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("złota", "ciupaga", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new TimePassedCondition(QUEST_SLOT, 1, delay)),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Żar ostygł. Oto ciupaga z trzema wąsami, nasiąknięta smoczym szeptem.");
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
			"Hadrin osadzi trzeci wąs w twojej złotej ciupadze, jeśli dostarczysz mu rzadkie trofea.",
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
		res.add(player.getGenderVerb("Spotkałem") + " Hadrina w kuźni pachnącej smołą.");
		res.add("Obiecał osadzić trzeci wąs w mojej ciupadze, jeśli przyniosę wszystkie dary.");
		if ("rejected".equals(questState)) {
			res.add("Odłożyłem to dzieło; niech Hadrin kuje dla innego wojownika.");
			return res;
		}
		res.add(player.getGenderVerb("Udałem") + " się do Hadrina z jego listą darów. Gdy zapomnę, mam powiedzieć: przypomnij."); 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add(player.getGenderVerb("Dostarczyłem") + " wszystkie trofea. Hadrin rozżarzył miechy.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT,1,REQUIRED_HOURS).fire(player, null, null)) {
				res.add("Kuźnia Hadrina już ucichła. Hasło: nagroda.");
			} else {
				res.add("Mam wrócić po ciupagę za dwadzieścia cztery godziny. Hasło: nagroda.");
			}
			return res;
		} 
		res.add("Ciupaga z trzema wąsami błyszczy jak rozgrzane złoto. Jest moja.");
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
