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
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.DropItemAction;
import games.stendhal.server.entity.npc.action.CaptureItemUpgradeAction;
import games.stendhal.server.entity.npc.action.EquipUpgradedItemAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.action.SetQuestToTimeStampAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.KarmaLessThanCondition;
import games.stendhal.server.entity.npc.condition.KilledCondition;
import games.stendhal.server.entity.npc.condition.LevelGreaterThanCondition;
import games.stendhal.server.entity.npc.condition.OrCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestCompletedCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;
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

	private ChatCondition canAcceptQuest() {
		return new AndCondition(new QuestCompletedCondition(FRIEND_1), new QuestCompletedCondition(FRIEND_2),
				new QuestCompletedCondition(FRIEND_3), new QuestCompletedCondition(FRIEND_4),
				new LevelGreaterThanCondition(349), new NotCondition(new KarmaLessThanCondition(2000)),
				new KilledCondition("azazel"),
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestCompletedCondition(QUEST_SLOT)));
	}

	private void step_1() {
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(canAcceptQuest(), new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Żar i piach nie są ci obce. Chcesz zebrać trofea potrzebne do dodania trzeciego wąsa twojej ciupadze?", null);
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(canAcceptQuest(), new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Mogę dodać trzeci wąs kolejnej ciupadze. Ponownie przynieś wszystkie trofea i materiały. Podejmiesz się?", null);
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT), ConversationStates.ATTENDING,
				"Już pracujemy nad twoją ciupagą. Przynieś komplet materiałów albo zapytaj o #nagrodę, jeśli już je oddałeś.", null);
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				null, ConversationStates.ATTENDING,
				"Najpierw pomóż góralskiemu kolekcjonerowi, kolekcjonerom pasów i rękawic oraz Królowi Krakowi. Potrzebujesz też 350 poziomu, 2000 karmy i zwycięstwa nad Azazelem.", null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, canAcceptQuest(),
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
					final int completed = getCompletedCount(player);
					player.setQuest(QUEST_SLOT, "start;;" + completed);
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED, ConversationPhrases.NO_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT), ConversationStates.ATTENDING,
				"Dobrze. Wróć, gdy zechcesz przekuć kolejną ciupagę.", null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, new QuestNotStartedCondition(QUEST_SLOT),
			ConversationStates.IDLE,
			"Jak chcesz, moje kowadło poczeka na innego.",
			new SetQuestAction(QUEST_SLOT, 0, "rejected"));

	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Hadrin");
		final List<ChatAction> ciupagaactions = new LinkedList<ChatAction>();
		ciupagaactions.add(new CaptureItemUpgradeAction(QUEST_SLOT, "złota ciupaga z dwoma wąsami"));
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
		ciupagaactions.add(new SetQuestAction(QUEST_SLOT, 0, "forging"));
		ciupagaactions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmioty", "przypomnij", "ciupaga", "ciupagę"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
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
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
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
		final int delay = REQUIRED_HOURS * TimeUtil.MINUTES_IN_HOUR;

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("złota", "ciupaga", "nagroda", "nagrodę"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, delay))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Młoty nadal biją nad twoją ciupagą. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("złota", "ciupaga", "nagroda", "nagrodę"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new TimePassedCondition(QUEST_SLOT, 1, delay)),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					new EquipUpgradedItemAction(QUEST_SLOT, "złota ciupaga z trzema wąsami", ItemRarity.EPIC)
							.fire(player, sentence, raiser);
					raiser.say("Żar ostygł. Oto ciupaga z trzema wąsami, nasiąknięta smoczym szeptem.");
					player.addXP(1000000);
					player.addKarma(200);
					player.setQuest(QUEST_SLOT, 2, Integer.toString(getCompletedCount(player) + 1));
					player.setQuest(QUEST_SLOT, 0, "done");
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Złota Ciupaga z Trzema Wąsami",
			"Hadrin osadzi trzeci wąs w twojej złotej ciupadze, jeśli dostarczysz mu rzadkie trofea.",
			true);
		questInfo.setCompletionsIndexes(2);
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
		if (player.isQuestInState(QUEST_SLOT, 0, "start")) {
			return res;
		} 
		res.add(player.getGenderVerb("Dostarczyłem") + " wszystkie trofea. Hadrin rozżarzył miechy.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_HOURS * TimeUtil.MINUTES_IN_HOUR).fire(player, null, null)) {
				res.add("Kuźnia Hadrina już ucichła. Hasło: nagroda.");
			} else {
				res.add("Mam wrócić po ciupagę za dwadzieścia cztery godziny. Hasło: nagroda.");
			}
			return res;
		} 
		res.add("Ciupaga z trzema wąsami błyszczy jak rozgrzane złoto. Jest moja.");
		if (isCompleted(player)) {
			res.add("Mogę ponownie podjąć zadanie i przekuć kolejną ciupagę za pełny komplet materiałów.");
			return res;
		}
		// if things have gone wrong and the quest state didn't match any of the above, debug a bit:
		final List<String> debug = new ArrayList<String>();
		debug.add("Stan zadania to: " + questState);
		logger.error("Historia nie pasuje do stanu poszukiwania " + questState);
		return debug;
	}

	@Override
	public boolean isRepeatable(final Player player) {
		return isCompleted(player);
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
