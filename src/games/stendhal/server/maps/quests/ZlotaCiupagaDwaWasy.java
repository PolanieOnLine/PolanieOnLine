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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
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

	private static final int REQUIRED_MINUTES = 30;

	private static final String QUEST_SLOT = "ciupaga_dwa_wasy";

	private static final String KRASNOLUD_QUEST_SLOT = "krasnolud";

	private static Logger logger = Logger.getLogger(ZlotaCiupagaDwaWasy.class);

	@Override
	public String getSlotName() {
		return QUEST_SLOT;
	}

	private void step_1() {
		final SpeakerNPC npc = npcs.get("Władca Smoków");

		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isQuestCompleted(KRASNOLUD_QUEST_SLOT)) {
						if(player.getLevel() >= 350) {
							if(player.getKarma() >= 1000) {
								if(player.hasKilled("serafin")) {
									if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
										raiser.say("Musisz być dzielnym wojownikiem skoro dotarłeś aż tu. Mam dla ciebie zadanie, czy jesteś gotów?");
									} else if (player.getQuest(QUEST_SLOT).startsWith("done;")) {
										if (player.isQuestCompleted(QUEST_SLOT)) {
											raiser.say("Jestem bardzo wdzięczny za pomoc. Moje smoki w końcu mnie słuchają.");
											raiser.setCurrentState(ConversationStates.ATTENDING);
										} else {
											raiser.say("Dlaczego zawracasz mi głowę skoro nie ukończyłeś zadania?");
											raiser.setCurrentState(ConversationStates.ATTENDING);
										}
									} else if (player.getQuest(QUEST_SLOT).startsWith("zbroja;")) {
										raiser.say("Dlaczego zawracasz mi głowę skoro nie ukończyłeś zadania u Krasnoluda?");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say("Rozmawiam tylko z osobami, które wykazały się w walce zabijając serafina.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Twoja karma jest zbyt słaba aby podołać temu zadaniu. Postaraj się aby była 1000 lub więcej");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 350 lvl.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Nim dam ci zadanie, udowodnij swe męstwo pomagając Krasnoludowi.");
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

					raiser.say("Sporo smoków zbuntowało się przeciwko mi. Chcę abyś zabił każdego z nich, którego spotkasz na swej drodze."
					+ " Dam ci też dobrą radę zbieraj ich pazury. Mój stary znajomy Krasnolud zbiera je. Słyszałem iż w zamian za nie "
					+ "zrobi ci wspaniałą broń. Powiedz mu tylko moje #imie. Miłego polowania.");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(10);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Obyś smoka wawelskiego spotkał na swej drodze.",
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
				ConversationStates.ATTENDING, "Widzę, że masz wszystko o co cię prosiłem. Wróć za 30 godzin a złota ciupaga z dwoma wąsami będzie gotowa. Przypomnij mi mówiąc #/nagroda/.",
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
				ConversationStates.ATTENDING, "Tak wiem Władca Smoków mówił mi o tobie. Zajmuję się udoskonalaniem złotej ciupagi.\n"
									+"Do jej udoskonalenia potrzebuję:\n"
									+"#'1 pazur zielonego smoka'\n"
									+"#'1 pazur czerwonego smoka'\n"
									+"#'1 złota ciupaga z wąsem'\n"
									+"#'150 sztabek złota'\n"
									+"#'1200000 money'\n"
									+"#'10 polan' oraz\n"
									+"#'2 pióra serafina'\n"
									+"Proszę przynieś mi to wszystko naraz. Jeżeli zapomnisz co masz przynieść to powiedz #'przypomnij'. Dziękuję!", null);

	}

	private void step_3() { 
		final SpeakerNPC npc = npcs.get("Krasnolud");

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
			new QuestStateStartsWithCondition(QUEST_SLOT, "forging;")),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {

					final String[] tokens = player.getQuest(QUEST_SLOT).split(";");

					final long delay = REQUIRED_MINUTES * MathHelper.SECONDS_IN_ONE_MINUTE; 
					final long timeRemaining = (Long.parseLong(tokens[1]) + delay)
							- System.currentTimeMillis();

					if (timeRemaining > 0L) {
						raiser.say("Wciąż pracuje nad twoim zleceniem. Wróć za "
							+ TimeUtil.approxTimeUntil((int) (timeRemaining / 1000L))
							+ ", aby odebrać ciupagę.");
						return;
					}

					raiser.say("Warto było czekać. A oto i ona, czyż nie jest wspaniała!");
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
			"Złota Ciupaga Dwa Wąsy",
			"Krasnolud wzmocni twoją Złotą Ciupagę z Jednym Wąsem.",
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
		res.add("Spotkałem się z Władcą Smoków.");
		res.add("Poprosił mnie abym zabijał wszystkie smoki, które wejdą mi w drogę. Zdradził mi też tajemnice. Podobno Krasnalud ulepsza złote ciupagi.");
		if ("rejected".equals(questState)) {
			res.add("Mam stracha przed smokami więc będę schodził im z drogi.");
			return res;
		}
		res.add("Udałem się do Krasnoluda w celu ulepszenia ciupagi. Kazał mi przynnieść kilka przedmiotów. Gdybym zapomniał co mam przynieść mam mu powiedzieć: przypomnij."); 
		if ("start".equals(questState)) {
			return res;
		} 
		res.add("Dostarczyłem potrzebne przedmioty! Krasnal zabrał się za ulepszenie mojej ciupagi.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT,1,REQUIRED_MINUTES).fire(player, null, null)) {
				res.add("Podobno Krasnalud skończył moją ciupagę. Hasło: nagroda.");
			} else {
				res.add("Po ciupagę mam zgłosić się za 30 godzin. Hasło: ciupaga.");
			}
			return res;
		} 
		res.add("Warto było czekać na ciupagę z dwoma wąsami. Ta potężna i piękna broń należy teraz do mnie.");
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
	public String getName() {
		return "ZlotaCiupagaDwaWasy";
	}

	@Override
	public String getNPCName() {
		return "Władca Smoków";
	}
}