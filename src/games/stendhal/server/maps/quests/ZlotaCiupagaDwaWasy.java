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
import games.stendhal.server.entity.npc.action.SetQuestAndModifyKarmaAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
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

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Władca Smoków");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isQuestCompleted(KRASNOLUD_QUEST_SLOT)) {
						if(player.getLevel() >= 250) {
							if(player.getKarma() >= 1000) {
								if(player.hasKilled("serafin")) {
									if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
						raiser.say("Przemaszerowałeś przez kurz i ogień, skoro " + player.getGenderVerb("dotarłeś") + " aż do mego skalnego kręgu. Mam dla ciebie zadanie, podejmiesz się?");
									} else if (player.getQuest(QUEST_SLOT).startsWith("done;")) {
										if (player.isQuestCompleted(QUEST_SLOT)) {
						raiser.say("Dzięki twojej pomocy smoki znów krążą spokojnie wokół moich skał.");
											raiser.setCurrentState(ConversationStates.ATTENDING);
										} else {
						raiser.say("Nie wracaj, dopóki nie " + player.getGenderVerb("ukończyłeś") + " tego, o co cię prosiłem.");
											raiser.setCurrentState(ConversationStates.ATTENDING);
										}
									} else if (player.getQuest(QUEST_SLOT).startsWith("zbroja;")) {
						raiser.say("Najpierw domknij robotę u Krasnoluda, potem wróć z wieściami.");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
						npc.say("Wróć, gdy serafin padnie pod twoją ciupagą.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
						npc.say("Twoje imię brzmi zbyt lekko. Zadbaj o karmę równą tysiącowi albo większą");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
						npc.say("Najpierw wznieś się na dwusetny pięćdziesiąty poziom, potem porozmawiamy o smoczych pazurach.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Najpierw wesprzyj Krasnoluda i pokaż, że nie boisz się kuźnianego dymu.");
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
					raiser.say("Smocze gniazda syczą niespokojnie. Chcę, byś " + player.getGenderVerb("ścinał") + " każdego buntownika, którego spotkasz."
							+ " Zbieraj ich pazury i zanieś mojemu towarzyszowi Krasnoludowi. Wystarczy, że wyszepczesz mu moje #imie."
							+ " Niech łowy będą dla ciebie łaskawe.");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Jak chcesz, piasek zasypie tę rozmowę.",
			new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -50.0));

	}

	private void step_2() {
		final SpeakerNPC npc = npcs.get("Krasnolud");
		final List<ChatAction> ciupagaactions = new LinkedList<ChatAction>();
		ciupagaactions.add(new DropItemAction("pazur zielonego smoka",1));
		ciupagaactions.add(new DropItemAction("pazur czerwonego smoka",1));
		ciupagaactions.add(new DropItemAction("złota ciupaga z wąsem",1));
		ciupagaactions.add(new DropItemAction("sztabka złota",150));
		ciupagaactions.add(new DropItemAction("money",1200000));
		ciupagaactions.add(new DropItemAction("polano",10));
		ciupagaactions.add(new DropItemAction("pióro serafina",2));
		ciupagaactions.add(new SetQuestAction(QUEST_SLOT, "forging;" + System.currentTimeMillis()));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmioty", "przypomnij", "ciupaga"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								 new PlayerHasItemWithHimCondition("pazur zielonego smoka",1),
								 new PlayerHasItemWithHimCondition("pazur czerwonego smoka",1),
								 new PlayerHasItemWithHimCondition("złota ciupaga z wąsem",1),
								 new PlayerHasItemWithHimCondition("sztabka złota",150),
								 new PlayerHasItemWithHimCondition("money",1200000),
								 new PlayerHasItemWithHimCondition("polano",10),
								 new PlayerHasItemWithHimCondition("pióro serafina",2)),
				ConversationStates.IDLE, "Widzę, że przyniosłeś wszystko naraz. Wróć za dwanaście godzin, a ciupaga z dwoma wąsami zabłyśnie. Przypomnij mi mówiąc #'nagroda'.",
				new MultipleActions(ciupagaactions));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przypomnij", "Władca Smoków", "władca", "smok"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("pazur zielonego smoka",1),
												  new PlayerHasItemWithHimCondition("pazur czerwonego smoka",1),
												  new PlayerHasItemWithHimCondition("złota ciupaga z wąsem",1),
												  new PlayerHasItemWithHimCondition("sztabka złota",150),
												  new PlayerHasItemWithHimCondition("money",1200000),
												  new PlayerHasItemWithHimCondition("polano",10),
												  new PlayerHasItemWithHimCondition("pióro serafina",2)))),
				ConversationStates.IDLE, "Władca Smoków szepnął mi o tobie. Jeśli chcesz, by ciupaga nabrała nowego wąsa, przynieś mi wszystko naraz:\n"
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
		final int delay = REQUIRED_HOURS * TimeUtil.SECONDS_IN_MINUTE;

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("złota", "ciupaga", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, delay))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Młot jeszcze tańczy nad twoją ciupagą. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("złota", "ciupaga", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new TimePassedCondition(QUEST_SLOT, 1, delay)),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Żar ostygł. Oto ciupaga z dwoma wąsami, pachnąca smoczą łuską.");
					player.addXP(500000);
					player.addKarma(100);
					final Item zlotaCiupagaZDwomaWasami = SingletonRepository.getEntityManager().getItem("złota ciupaga z dwoma wąsami");
					zlotaCiupagaZDwomaWasami.setBoundTo(player.getName());
					player.equipOrPutOnGround(zlotaCiupagaZDwomaWasami);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Złota Ciupaga z Dwoma Wąsami",
			"Krasnolud doda drugi wąs twojej złotej ciupadze, jeśli przyniesiesz mu smocze trofea.",
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
		res.add(player.getGenderVerb("Spotkałem") + " Władcę Smoków w zadymionej grocie.");
		res.add("Kazał mi ścinać każdego smoka, który stanie na ścieżce, i zbierać pazury dla krasnoludzkiego mistrza.");
		if ("rejected".equals(questState)) {
			res.add("Wycofałem się; niech smoki same się gryzą.");
			return res;
		}
		res.add(player.getGenderVerb("Udałem") + " się do kuźni Krasnoluda. Muszę przynieść mu wszystkie dary naraz i w razie potrzeby poprosić: przypomnij."); 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add(player.getGenderVerb("Dostarczyłem") + " mu wszystko. Krasnolud zaczął wyginać metal.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT,1,REQUIRED_HOURS).fire(player, null, null)) {
				res.add("Kuźnia już ucichła. Hasło: nagroda.");
			} else {
				res.add("Mam wrócić po ciupagę za dwanaście godzin. Hasło: nagroda.");
			}
			return res;
		} 
		res.add("Ciupaga z dwoma wąsami błyszczy jak świeżo zbita łuska. Jest moja.");
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
		return "Złota Ciupaga z Dwoma Wąsami";
	}

	@Override
	public String getNPCName() {
		return "Władca Smoków";
	}
}
