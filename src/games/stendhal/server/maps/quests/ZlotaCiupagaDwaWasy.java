/***************************************************************************
 *                 (C) Copyright 2003-2024 - PolanieOnLine                 *
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
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
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

public class ZlotaCiupagaDwaWasy extends AbstractQuest {
	private static final String QUEST_SLOT = "ciupaga_dwa_wasy";

	private static final String KRASNOLUD_QUEST_SLOT = "krasnolud";

	private static final int REQUIRED_HOURS = 12;

	private static Logger logger = Logger.getLogger(ZlotaCiupagaDwaWasy.class);

	private ChatCondition canAcceptQuest() {
		return new AndCondition(new QuestCompletedCondition(KRASNOLUD_QUEST_SLOT),
				new LevelGreaterThanCondition(249), new NotCondition(new KarmaLessThanCondition(1000)),
				new KilledCondition("serafin"),
				new OrCondition(new QuestNotStartedCondition(QUEST_SLOT), new QuestCompletedCondition(QUEST_SLOT)));
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Władca Smoków");
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(canAcceptQuest(), new QuestNotStartedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Wiatry nad chmurami szepczą o tobie. Chcesz zebrać trofea potrzebne do dodania drugiego wąsa twojej ciupadze?", null);
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new AndCondition(canAcceptQuest(), new QuestCompletedCondition(QUEST_SLOT)),
				ConversationStates.QUEST_OFFERED,
				"Mogę pomóc przy kolejnej ciupadze z dwoma wąsami. Ponownie zbierz wszystkie trofea i materiały dla Krasnoluda. Podejmiesz się?", null);
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				new QuestActiveCondition(QUEST_SLOT), ConversationStates.ATTENDING,
				"Twoja wyprawa już trwa. Zanieś komplet materiałów Krasnoludowi. Jeśli oddałeś je do kuźni, zapytaj go o #nagrodę.", null);
		npc.add(ConversationStates.ATTENDING, ConversationPhrases.QUEST_MESSAGES,
				null, ConversationStates.ATTENDING,
				"Najpierw pomóż Krasnoludowi, osiągnij 250 poziom i 1000 karmy oraz pokonaj serafina.", null);

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.YES_MESSAGES, canAcceptQuest(),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Zbierz smocze pazury i zanieś je Krasnoludowi w kuźni przy piaskach."
							+ " Zapytaj go o #ciupagę albo powiedz: #przypomnij. Poda ci pełną listę materiałów.");
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
			"Jak chcesz, wiatr rozwieje tę rozmowę.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, 0, "rejected", -50.0));

	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Krasnolud");
		final List<ChatAction> ciupagaactions = new LinkedList<ChatAction>();
		ciupagaactions.add(new CaptureItemUpgradeAction(QUEST_SLOT, "złota ciupaga z wąsem"));
		ciupagaactions.add(new DropItemAction("pazur zielonego smoka",1));
		ciupagaactions.add(new DropItemAction("pazur czerwonego smoka",1));
		ciupagaactions.add(new DropItemAction("złota ciupaga z wąsem",1));
		ciupagaactions.add(new DropItemAction("sztabka złota",150));
		ciupagaactions.add(new DropItemAction("money",1200000));
		ciupagaactions.add(new DropItemAction("polano",10));
		ciupagaactions.add(new DropItemAction("pióro serafina",2));
		ciupagaactions.add(new SetQuestAction(QUEST_SLOT, 0, "forging"));
		ciupagaactions.add(new SetQuestToTimeStampAction(QUEST_SLOT, 1));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmioty", "przypomnij", "ciupaga", "ciupagę"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
								 new PlayerHasItemWithHimCondition("pazur zielonego smoka",1),
								 new PlayerHasItemWithHimCondition("pazur czerwonego smoka",1),
								 new PlayerHasItemWithHimCondition("złota ciupaga z wąsem",1),
								 new PlayerHasItemWithHimCondition("sztabka złota",150),
								 new PlayerHasItemWithHimCondition("money",1200000),
								 new PlayerHasItemWithHimCondition("polano",10),
								 new PlayerHasItemWithHimCondition("pióro serafina",2)),
				ConversationStates.IDLE, "Widzę, że wciąż pachniesz wiatrem i żarem. Wracaj za dwanaście godzin, a ciupaga z dwoma wąsami zabłyśnie jak skrzydło smoka. Przypomnij mi mówiąc #'nagroda'.",
				new MultipleActions(ciupagaactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przypomnij", "ciupaga", "ciupagę", "przedmioty", "Władca Smoków", "władca", "smok"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("pazur zielonego smoka",1),
												  new PlayerHasItemWithHimCondition("pazur czerwonego smoka",1),
												  new PlayerHasItemWithHimCondition("złota ciupaga z wąsem",1),
												  new PlayerHasItemWithHimCondition("sztabka złota",150),
												  new PlayerHasItemWithHimCondition("money",1200000),
												  new PlayerHasItemWithHimCondition("polano",10),
												  new PlayerHasItemWithHimCondition("pióro serafina",2)))),
				ConversationStates.IDLE, "Władca Smoków posłał wieść z chmur. Jeśli chcesz, by ciupaga zyskała drugi wąs, przynieś mi wszystko naraz:\n"
									+"#'1 pazur zielonego smoka'\n"
									+"#'1 pazur czerwonego smoka'\n"
									+"#'1 złota ciupaga z wąsem'\n"
									+"#'150 sztabek złota'\n"
									+"#'1200000 money'\n"
									+"#'10 polan' oraz\n"
									+"#'2 pióra serafina'\n"
									+"Jeżeli zapomnisz, powiedz #'przypomnij'.", null);

	}

	private void step_3() { 
		final SpeakerNPC npc = npcs.get("Krasnolud");
		final int delay = REQUIRED_HOURS * TimeUtil.MINUTES_IN_HOUR;

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("złota", "ciupaga", "nagroda", "nagrodę"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, delay))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Młot jeszcze tańczy w rytmie wiatru. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("złota", "ciupaga", "nagroda", "nagrodę"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new TimePassedCondition(QUEST_SLOT, 1, delay)),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					new EquipUpgradedItemAction(QUEST_SLOT, "złota ciupaga z dwoma wąsami", ItemRarity.EPIC)
							.fire(player, sentence, raiser);
					raiser.say("Żar ostygł. Oto ciupaga z dwoma wąsami, nasycona oddechem chmur.");
					player.addXP(500000);
					player.addKarma(100);
					player.setQuest(QUEST_SLOT, 2, Integer.toString(getCompletedCount(player) + 1));
					player.setQuest(QUEST_SLOT, 0, "done");
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Złota Ciupaga z Dwoma Wąsami",
			"Krasnolud doda drugi wąs twojej złotej ciupadze, jeśli przyniesiesz mu smocze trofea.",
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
		res.add(player.getGenderVerb("Spotkałem") + " Władcę Smoków na tarasie ponad chmurami.");
		res.add("Kazał mi ścinać każdego smoka wichrzącego ponad chmurami i zbierać pazury dla krasnoludzkiego mistrza.");
		if ("rejected".equals(questState)) {
			res.add("Wycofałem się; niech smoki same się gryzą.");
			return res;
		}
		res.add(player.getGenderVerb("Udałem") + " się do kuźni Krasnoluda. Muszę przynieść mu wszystkie dary naraz i w razie potrzeby poprosić: przypomnij."); 
		if (player.isQuestInState(QUEST_SLOT, 0, "start")) {
			return res;
		} 
		res.add(player.getGenderVerb("Dostarczyłem") + " mu wszystko. Krasnolud zaczął wyginać metal.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT, 1, REQUIRED_HOURS * TimeUtil.MINUTES_IN_HOUR).fire(player, null, null)) {
				res.add("Kuźnia już ucichła. Hasło: nagroda.");
			} else {
				res.add("Mam wrócić po ciupagę za dwanaście godzin. Hasło: nagroda.");
			}
			return res;
		} 
		res.add("Ciupaga z dwoma wąsami błyszczy jak świeżo zbita łuska. Jest moja.");
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
		return "Złota Ciupaga z Dwoma Wąsami";
	}

	@Override
	public String getNPCName() {
		return "Władca Smoków";
	}
}
