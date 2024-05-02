/***************************************************************************
 *                   (C) Copyright 2019-2021 - Stendhal                    *
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
import games.stendhal.common.grammar.Grammar;
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

/**
 * @author zekkeq
 */
public class ZlotyPierscien extends AbstractQuest {
	private static final String QUEST_SLOT = "zloty_pierscien";
	private final SpeakerNPC npc = npcs.get("Kowal Wincenty");

	private static final String ZAMOWIENIE_STRAZY = "zamowienie_strazy";

	private static final int REQUIRED_HOURS = 1;

	private static Logger logger = Logger.getLogger(ZlotyPierscien.class);

	private void step_1() {
		npc.add(ConversationStates.ATTENDING,
			ConversationPhrases.QUEST_MESSAGES, null,
			ConversationStates.QUEST_OFFERED, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					if (player.isQuestCompleted(ZAMOWIENIE_STRAZY)) {
						if(player.getLevel() >= 75) {
							if(player.getKarma() >= 50) {
								if(player.hasKilled("kostucha złota")) {
									if (!player.hasQuest(QUEST_SLOT) || "rejected".equals(player.getQuest(QUEST_SLOT))) {
										raiser.say("Chcesz abym wykonał dla Ciebie złoty pierścień?");
									} else if (player.getQuest(QUEST_SLOT, 0).equals("start")) {
										raiser.say("Już się Ciebie pytałem czy chcesz złoty pierścień!");
									} else if (player.isQuestCompleted(QUEST_SLOT)) {
										raiser.say("Masz już jeden złoty pierścień ode mnie...");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									} else {
										raiser.say("Dlaczego zawracasz mi głowę skoro nie ukończyłeś zadania?");
										raiser.setCurrentState(ConversationStates.ATTENDING);
									}
								} else {
									npc.say("Rozmawiam tylko z osobami, które wykazały się w walce zabijając kustuchę złotą.");
									raiser.setCurrentState(ConversationStates.ATTENDING);
								}
							} else {
								npc.say("Twoja karma jest zbyt słaba aby podołać temu zadaniu. Postaraj się aby była 50 lub więcej");
								raiser.setCurrentState(ConversationStates.ATTENDING);
							}
						} else {
							npc.say("Twój stan społeczny jest zbyt niski aby podjąć te zadanie. Wróć gdy zdobędziesz 75 lvl.");
							raiser.setCurrentState(ConversationStates.ATTENDING);
						}
					} else {
						npc.say("Ukończ pierw zadanie na srebrny pierścień! Spytaj się asystenta kowala w Krakowie.");
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
					raiser.say("W takim razie oto potrzebna lista składników, gdzie umożliwi mi wykonanie złotego pierścienia.\n"
							+"Składniki:\n"
							+"#'20 sztabek złota'\n"
							+"#'10 polan' oraz\n"
							+"#'100000 money'\n"
							+"Proszę przynieś mi to wszystko naraz. Jeżeli zapomnisz co masz przynieść to powiedz #'przypomnij'. Dziękuję!");
					player.setQuest(QUEST_SLOT, "start");
					player.addKarma(5);

				}
			});

		npc.add(ConversationStates.QUEST_OFFERED,
			ConversationPhrases.NO_MESSAGES, null,
			ConversationStates.IDLE,
			"Trudno, może jeszcze zmienisz zdanie...",
			new SetQuestAction(QUEST_SLOT, "rejected"));

	}

	private void step_2() {
		final List<ChatAction> nagroda = new LinkedList<ChatAction>();
		nagroda.add(new DropItemAction("sztabka złota",20));
		nagroda.add(new DropItemAction("money",100000));
		nagroda.add(new DropItemAction("polano",10));
		nagroda.add(new SetQuestAction(QUEST_SLOT, "forging;" + System.currentTimeMillis()));

		npc.add(ConversationStates.ATTENDING, Arrays.asList("przedmioty", "przypomnij"),
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								 new PlayerHasItemWithHimCondition("sztabka złota",20),
								 new PlayerHasItemWithHimCondition("money",100000),
								 new PlayerHasItemWithHimCondition("polano",10)),
				ConversationStates.ATTENDING, "Widzę, że masz wszystko o co cię prosiłem. Wróć za 1 godzinę, a złoty pierścień będzie gotowy. Przypomnij mi mówiąc #/pierścień/.",
				new MultipleActions(nagroda));

		npc.add(ConversationStates.ATTENDING, "przypomnij",
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, "start"),
								 new NotCondition(
								 new AndCondition(new PlayerHasItemWithHimCondition("sztabka złota",20),
												  new PlayerHasItemWithHimCondition("money",100000),
												  new PlayerHasItemWithHimCondition("polano",10)))),
				ConversationStates.ATTENDING, "Oto lista potrzebnych mi przedmiotów:\n"
								+"#'20 sztabek złota'\n"
								+"#'10 polan' oraz\n"
								+"#'100000 money'\n"
								+"Proszę przynieś mi to wszystko naraz. Jeżeli zapomnisz co masz przynieść to powiedz #'przypomnij'. Dziękuję!", null);
	}

	private void step_3() {
		final int delay = REQUIRED_HOURS * MathHelper.SECONDS_IN_ONE_MINUTE;

		npc.add(ConversationStates.ATTENDING, 
			Arrays.asList("pierścień", "złoty", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, delay))),
			ConversationStates.IDLE, 
			null, 
			new SayTimeRemainingAction(QUEST_SLOT, 1, delay, "Wciąż pracuje nad twoim pierścieniem. Wróć za "));

		npc.add(ConversationStates.ATTENDING,
			Arrays.asList("pierścień", "złoty", "nagroda"),
			new AndCondition(new GreetingMatchesNameCondition(npc.getName()),
					new QuestStateStartsWithCondition(QUEST_SLOT, "forging;"),
					new TimePassedCondition(QUEST_SLOT, 1, delay)),
			ConversationStates.IDLE, null, new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("A oto i piękny, błyszczący się złoty pierścień! Niech Ci służy...");
					player.addXP(55000);
					player.addKarma(25);
					final Item pierscien = SingletonRepository.getEntityManager().getItem("złoty pierścień");
					pierscien.setBoundTo(player.getName());
					player.equipOrPutOnGround(pierscien);
					player.notifyWorldAboutChanges();
					player.setQuest(QUEST_SLOT, "done");
				}
			});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
			"Złoty Pierścień",
			"Kowal Wincenty może wykonać złoty pierścień.",
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
		res.add(player.getGenderVerb("Spotkałem") + " się z Kowalem Wincentym.");
		res.add("Kowal Wincenty może wykonać dla mnie złoty pierścień.");
		if ("rejected".equals(questState)) {
			res.add("Nie zamierzam robić złotego pierścienia.");
			return res;
		}
		res.add(player.getGenderVerb("Udałem") + " się do Kowala Wincenta w celu wykonania złotego pierścienia. Kazał mi przynnieść kilka przedmiotów. Gdybym " + player.getGenderVerb("zapomniał") + " co mam przynieść mam mu powiedzieć: przypomnij.");
		if ("start".equals(questState)) {
			return res;
		}
		res.add(player.getGenderVerb("Dostarczyłem") + " potrzebne przedmioty! Kowal Wincenty zabrał się za wykuwanie pierścienia.");
		if (questState.startsWith("forging")) {
			if (new TimePassedCondition(QUEST_SLOT,1,REQUIRED_HOURS).fire(player, null, null)) {
				res.add("Podobno Kowal Wincenty skończył swoją robotę. Hasło: pierścień.");
			} else {
				res.add("Po pierścień mam zgłosić się za 1 godzinę. Hasło: pierścień.");
			}
			return res;
		}
		res.add("Warto było czekać na złoty pierścień.");
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
		return "Złoty Pierścień";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
