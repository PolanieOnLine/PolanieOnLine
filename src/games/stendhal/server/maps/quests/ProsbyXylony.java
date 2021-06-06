/***************************************************************************
 *                   (C) Copyright 2018-2021 - Stendhal                    *
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
import java.util.HashMap;
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
import games.stendhal.server.entity.npc.action.IncreaseKarmaAction;
import games.stendhal.server.entity.npc.action.MultipleActions;
import games.stendhal.server.entity.npc.action.SayTimeRemainingAction;
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
import games.stendhal.server.entity.npc.condition.QuestStateStartsWithCondition;
import games.stendhal.server.entity.npc.condition.TimePassedCondition;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Pair;

public class ProsbyXylony extends AbstractQuest {
	private static final String QUEST_SLOT = "prosby_wiedzmy";
	private final SpeakerNPC npc = npcs.get("Xylona");

	private static final int DELAY_IN_MINUTES = 60*1;

	private static Logger logger = Logger.getLogger(ProsbyXylony.class);

	private void step1() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING,
				"Dziękuję, że się pytasz ale wykonałeś już swoje zadanie.",
				null);

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"Denerwuje mnie pewny smok, który znajduje się obok mojej chatki. Czy mógłbyś mi pomóc?",
				null);

		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
				toKill.put("błękitny smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
				actions.add(new IncreaseKarmaAction(5.0));
				actions.add(new SetQuestAction(QUEST_SLOT, 0, "start"));
				actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Och, wspaniale! Tak jak powiedziałam wcześniej... denerwuje mnie #'błękitny smok', który znajduje się obok mojej chatki, nie daje mi wyjść z niej."
				+ " Jakbyś mógł to zabij go i przynieś mi conajmniej #'1 skórę niebieskiego smoka' w dowód tego, że go zabiłeś!",
				new MultipleActions(actions));

		// Player says no, they've lost karma.
		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"No cóż, może doczekam się odpowiedniego dzielnego wojownika w tej krainie.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10.0));
	}

	private void step2() {
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "start"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nawet nie postanowiełeś wyzwać błękitnego smoka na walkę. Dlaczego mnie oszukujesz? Nie zabiłeś go jeszcze.");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"start"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś tego przerośniętego gada to jak mam Tobie zaufać? Masz mi przynieść conajmniej #'1 skórę niebieskiego smoka', abym mogła Tobie zaufać.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"start"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 1)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Świetnie, teraz w końcu będę mogła wyjść z tej mojej chatki i pozbierać co niektóre potrzebne mi składniki. Właśnie... będę jeszcze potrzebowała twojej pomocy, przyjdź do mnie za godzinę i powiedz mi #'składniki'.");
						player.drop("skóra niebieskiego smoka", 1);
						player.addXP(5000);
						player.addKarma(10);
						player.setQuest(QUEST_SLOT, "czas_składniki;"+System.currentTimeMillis());
					}
				});
	}

	private void czas1() {
		final String extraTrigger = "składniki";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_składniki"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie mineła godzina. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_składniki"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"O wróciłeś... Potrzebuję pewnych składników, których ja sama nie mogę zdobyć. #'Pomożesz'?",
				new SetQuestAction(QUEST_SLOT,"dwuglowyzielonysmok?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("dwugłowy zielony smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "dwuglowyzielonysmok"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomogę", "pomoge", "tak"),
					new QuestInStateCondition(QUEST_SLOT, "dwuglowyzielonysmok?"),
					ConversationStates.ATTENDING,
					"Wspaniale... Musisz pójść zabić dla mnie #'dwugłowego zielonego smoka' i przynieść mi potrzebny materiał."
					+ " Chodzi mi dokładnie, abyś przyniósł dla mnie #'5 skór zielonego smoka'. Potrzebuję, aby wykonać amulet by te stworzenia słuchały się mnie."
					+ " Pamiętaj, że musisz zabić go samodzielnie!",
				new MultipleActions(actions));
	}

	private void step3() {
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "dwuglowyzielonysmok"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie postanowiłeś zabić samodzielnie dwugłowego zielonego smoka, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwuglowyzielonysmok"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(new PlayerHasItemWithHimCondition("skóra zielonego smoka", 5))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś dwugłowego zielonego smok to musisz przynieść mi dowód tego, że go ubiłeś. Proszę, przynieś mi #'5 skór zielonego smoka'.");
					}
				});
		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"dwuglowyzielonysmok"),
							 new KilledForQuestCondition(QUEST_SLOT, 1),
							 new PlayerHasItemWithHimCondition("skóra zielonego smoka", 5)),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Och, pokonałeś go! Dziękuję! Wróć za godzinę po następne zadanie. Jak wrócisz powiedz mi #'smok'.");
					player.drop("skóra zielonego smoka", 5);
					player.addXP(7500);
					player.addKarma(20);
					player.setQuest(QUEST_SLOT, "czas_kiel;"+System.currentTimeMillis());
				}
			});
	}

	private void czas2() {
		final String extraTrigger = "smok";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_kiel"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła godzina. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_kiel"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Dobrze więc... Tym razem potrzebuję nowych składników. #Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT,"kly?"));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "kielsmoka"));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomogę", "pomoge", "tak"),
				new QuestInStateCondition(QUEST_SLOT, "kly?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Same skóry zielonego smoka nic nie pomogą, potrzebuję coś, żeby wszystkie smoki mnie słuchały."
				+ " Aaaah... już wiem.. musisz przynieść mi conajmniej #'50 kłów smoka'. Powinno to wystarczyć aby wyrobić idealny amulet.",
				new MultipleActions(actions));
	}

	private void step4() {
		//player nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"kielsmoka"),
							 new NotCondition(new PlayerHasItemWithHimCondition("kieł smoka", 50))),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Co mnie oszukujesz? Przecież wiem, że nie masz przy sobie jeszcze wystarczającej liczny kłów smoka.");
				}
			});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
			new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"kielsmoka"),
							 new PlayerHasItemWithHimCondition("kieł smoka", 50)),
			ConversationStates.ATTENDING, null,
			new ChatAction() {
				@Override
				public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
					raiser.say("Dziękuję, że przyniosłeś te kły dla mnie! Wróć proszę za 1 godzinę, powiem ci, czy te składniki pomogły. Przypomnij mi mówiąc #'kieł smoka'.");
					player.drop("kieł smoka", 50);
					player.addXP(10000);
					player.addKarma(20);
					player.setQuest(QUEST_SLOT, "czas_czarnysmok;"+System.currentTimeMillis());
				}
			});
	}

	private void czas3() {
		final String extraTrigger = "kieł smoka";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czarnysmok"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła godzina. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czarnysmok"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1,DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Amulet zadziałał, lecz tylko na dwa olbrzymie smoki. Potrzebuję nadal twojej pomocy. #'Pomożesz'?",
				new SetQuestAction(QUEST_SLOT,"dużesmoki?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("czarne smoczysko", new Pair<Integer, Integer>(1,0));
							toKill.put("smok arktyczny", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "czarnesmoczysko"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomogę", "pomoge", "tak"),
				new QuestInStateCondition(QUEST_SLOT, "dużesmoki?"),
				ConversationStates.ATTENDING,
				"Dobrze. Niestety amulet zadziałał jedynie na #'czarne smoczysko' oraz #'smoka arktycznego'."
				+ " Proszę, musisz zabić te dwa smoki, czuję, że ten mój nowy amulet jest nadal niestabilny."
				+ " Nie wiem jak długo będą się mnie słuchać, więc wolę jednak mieć na chwilę spokoju od nich. Przynieś mi jeszcze #'3 skóry czarnego smoka' i #'3 skóry arktycznego smoka'.",
				new MultipleActions(actions));

	}

	private void step5() {
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "czarnesmoczysko"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nie udało Ci się jeszcze zabić czarnego smoczyska i smoka arktycznego. Masz ich zabić!");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czarnesmoczysko"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(
								 new AndCondition(
													new PlayerHasItemWithHimCondition("skóra czarnego smoka", 3),
													new PlayerHasItemWithHimCondition("skóra arktycznego smoka", 3)))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli ubiłeś te dwa smoki to jak mam Tobie jeszcze zaufać? Przynieś mi #'3 skóry czarnego smoka' jak i również #'3 skóry arktycznego smoka'.");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"czarnesmoczysko"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra czarnego smoka", 3),
								 new PlayerHasItemWithHimCondition("skóra arktycznego smoka", 3)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Udało Ci się! Dziękuję! Wróć proszę za 1 godzinę po kolejne zadanie. Przypomnij mi mówiąc #'duże smoki'.");
						player.drop("skóra czarnego smoka", 3);
						player.drop("skóra arktycznego smoka", 3);
						player.addXP(12500);
						player.addKarma(20);
						player.setQuest(QUEST_SLOT, "czas_krewsmoka;"+System.currentTimeMillis());
					}
				});
	}

	private void czas4() {
		final String extraTrigger = "duże smoki";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_krewsmoka"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła godzina. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_krewsmoka"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Jeszcze nie sprawdzałam czy mój amulet jest już wstanie kontrolować inne smoki, więc wolę nie ryzykować jeszcze. Przeczytałam w swej magicznej książce, aby udoskonalić swój amulet potrzebuję jeszcze kilku składników. #'Pomożesz'?",
				new SetQuestAction(QUEST_SLOT,"krew?"));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "krewsmoka"));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomogę", "pomoge", "tak"),
				new QuestInStateCondition(QUEST_SLOT, "krew?"),
				ConversationStates.ATTENDING,
				"Och, wspaniale! Potrzebuję jeszcze sporo składników, tym razem nie powinnam się pomylić!"
				+ " Żebyś się nie męczył długo zbieraniem ich to podziele je na kilka zadań. Niektóre składniki również uda mi się samemu zdobyć."
				+ " Wpierw musisz mi przynieść conajmniej #'30 krwi smoków', abym mogła skroplić swój amulet pewnym specyfikiem, który wykonam.",
				new MultipleActions(actions));
	}

	private void step6() {
		//player nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"krewsmoka"),
								 new NotCondition(new PlayerHasItemWithHimCondition("krew smoka", 30))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś czerwonego smoka, na pewno odwiedzi mnie ponownie w koszmarze jeśli nie będę miała amuletu. Proszę, dostarcz mi skóry czerwonego smoka.");
					}
		});
		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"krewsmoka"),
								 new PlayerHasItemWithHimCondition("krew smoka", 30)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Dziękuję za krew smoka. Za chwilę będę się brała do pracy, wykonam pewny specyfik, którym będę mogła skroplić nim trochę swój amulet. Przyjdź do mnie dopiero za godzine i powiedz mi #'krew'.");
						player.drop("krew smoka", 30);
						player.addXP(15000);
						player.addKarma(20);
						player.setQuest(QUEST_SLOT, "czas_glodna;"+System.currentTimeMillis());
					}
				});
	}

	private void czas5() {
		final String extraTrigger = "krew";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_glodna"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie skończyłam robić swojego specyfiku. Nie przeszkadzaj mi w tym momencie. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_glodna"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Od pewnego momentu trochę zgłodniałam, a w tej jaskini nie ma żadnej tawerny! #'Pomożesz'?",
				new SetQuestAction(QUEST_SLOT,"glodna?"));

		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "glodna"));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomogę", "pomoge", "tak"),
				new QuestInStateCondition(QUEST_SLOT, "glodna?"),
				ConversationStates.ATTENDING,
				"Wiesz.. Ciężko mi się pracuje jak jest się głonym. Proszę przynieś mi conajmniej #'100 mięsa', #'40 chleba', #'40 sera' oraz #'20 bukłaków z wodą', abym miała zapas na kolejny rok.",
				new MultipleActions(actions));
	}

	private void step7() {
		//player nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"glodna"),
								 new NotCondition(
								 new AndCondition(
													new PlayerHasItemWithHimCondition("mięso", 100),
													new PlayerHasItemWithHimCondition("chleb", 40),
													new PlayerHasItemWithHimCondition("ser", 40),
													new PlayerHasItemWithHimCondition("bukłak z wodą", 20)))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nie przyniosłeś mi jeszcze mojego jedzenia! Potrzebuję go, abym mogła dalej pracować! Proszę przynieś mi je.");
					}
				});
		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"glodna"),
								new PlayerHasItemWithHimCondition("mięso", 100),
								new PlayerHasItemWithHimCondition("chleb", 40),
								new PlayerHasItemWithHimCondition("ser", 40),
								new PlayerHasItemWithHimCondition("bukłak z wodą", 20)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Świetnie! W końcu będę mogła porządnie najeść się i pracować dalej! Przyjdź do mnie za godzinę i powiedz mi #'jedzenie' wtedy otrzymasz kolejne zadanie ode mnie.");
						player.drop("mięso", 100);
						player.drop("chleb", 40);
						player.drop("ser", 40);
						player.drop("bukłak z wodą", 20);
						player.addXP(20000);
						player.addKarma(40);
						player.setQuest(QUEST_SLOT, "czas_ostatnie;"+System.currentTimeMillis());
					}
				});
	}

	private void czas6() {
		final String extraTrigger = "jedzenie";
		List<String> questTrigger;
		questTrigger = new LinkedList<String>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_ostatnie"),
								 // jeszcze nie minal czas
								 new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła godzina. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(new QuestStateStartsWithCondition(QUEST_SLOT, "czas_ostatnie"),
								 // czas minal, dalszy krok
								 new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)),
				ConversationStates.ATTENDING,
				"Już prawie mój amulet do kontrolowania smoków jest skończony. #'Pomógłbyś' mi ponownie?",
				new SetQuestAction(QUEST_SLOT,"ostatnie?"));

		//kogo ma zabic w nastepnym kroku
		final HashMap<String, Pair<Integer, Integer>> toKill = new HashMap<String, Pair<Integer, Integer>>();
							toKill.put("czerwony smok", new Pair<Integer, Integer>(1,0));
							toKill.put("zielony smok", new Pair<Integer, Integer>(1,0));
							toKill.put("dwugłowy czerwony smok", new Pair<Integer, Integer>(1,0));
							toKill.put("dwugłowy czarny smok", new Pair<Integer, Integer>(1,0));
							toKill.put("dwugłowy niebieski smok", new Pair<Integer, Integer>(1,0));
							toKill.put("złoty smok", new Pair<Integer, Integer>(1,0));
		final List<ChatAction> actions = new LinkedList<ChatAction>();
							actions.add(new SetQuestAction(QUEST_SLOT, 0, "ostatnie"));
							actions.add(new StartRecordingKillsAction(QUEST_SLOT, 1, toKill));

		// gracz zgadza sie wykonac prozbe
		npc.add(ConversationStates.ATTENDING, Arrays.asList("pomożesz", "pomozesz", "pomogę", "pomoge", "tak"),
				new QuestInStateCondition(QUEST_SLOT, "ostatnie?"),
				ConversationStates.ATTENDING,
				"Wspaniale! Abym mogła ukończyć swój amulet musisz zabić:\n"
				+ "#'dwugłowy czarny smok',\n"
				+ "#'dwugłowy czerwony smok',\n"
				+ "#'dwugłowy niebieski smok',\n"
				+ "#'czerwony smok',\n"
				+ "#'zielony smok' oraz #'złoty smok'.\n"
				+ " Oczywiście samo zabicie nic nie da, musisz przynieść mi conajmniej 10 skór każdego rodzaju.",
				new MultipleActions(actions));
	}

	private void step8() {
		//player nie zabil smoka
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0, "ostatnie"),
								 new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Jeszcze nie zabiłeś samodzielnie smoków, dlaczego mnie oszukujesz?");
					}
				});

		//player zabil smoka ale nie ma potrzebnych resurce
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"ostatnie"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new NotCondition(
								 new AndCondition(
												new PlayerHasItemWithHimCondition("skóra złotego smoka", 10),
												new PlayerHasItemWithHimCondition("skóra czarnego smoka", 10),
												new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 10),
												new PlayerHasItemWithHimCondition("skóra zielonego smoka", 10),
												new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 10)))),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Nawet jeśli zabiłeś smoki, na pewno nie zaufam Tobie gdy nie przyniesiesz mi ich skór! Pamiętaj conajmniej 10 skór każdego rodzaju, masz mi to przynieść!");
					}
				});

		//player wykonal obydwa zlecenia
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(new QuestInStateCondition(QUEST_SLOT, 0,"ostatnie"),
								 new KilledForQuestCondition(QUEST_SLOT, 1),
								 new PlayerHasItemWithHimCondition("skóra złotego smoka", 10),
							     new PlayerHasItemWithHimCondition("skóra czarnego smoka", 10),
								 new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 10),
								 new PlayerHasItemWithHimCondition("skóra zielonego smoka", 10),
								 new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 10)),
				ConversationStates.ATTENDING, null,
				new ChatAction() {
					@Override
					public void fire(final Player player, final Sentence sentence, final EventRaiser raiser) {
						raiser.say("Och, widzę, że w końcu przyniosłeś ostatnie potrzebne mi składniki do skończenia mojego nowego amuletu. Powinno to zadziałać, przynajmniej tak w książce pisze..."
						+ "Proszę, przyjmij mój stary amulet... dostałam go od swej zmarłej już babci, która była również czarownicą tak jak ja."
						+ "Od teraz pozwalam Ci korzystać z mej magicznej kuli. Zabierze Ciebie ona w bardzo ciekawe miejsce!");
						player.drop("skóra złotego smoka", 10);
						player.drop("skóra czarnego smoka", 10);
						player.drop("skóra czerwonego smoka", 10);
						player.drop("skóra zielonego smoka", 10);
						player.drop("skóra niebieskiego smoka", 10);
						final Item pazurek = SingletonRepository.getEntityManager().getItem("pazur niebieskiego smoka");
						pazurek.setBoundTo(player.getName());
						player.equipOrPutOnGround(pazurek);
						player.setBaseHP(20 + player.getBaseHP());
						player.heal(20, true);
						player.addXP(35000);
						player.addKarma(100);
						player.setQuest(QUEST_SLOT, "done");
					}
				});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Prośby Xylony",
				"Xylona chce kontrolować wszystkie smoki swoim nowym amuletem. Czy to jest w ogólne możliwe?",
				false);

		step1();
		step2();
		czas1();
		step3();
		czas2();
		step4();
		czas3();
		step5();
		czas4();
		step6();
		czas5();
		step7();
		czas6();
		step8();
	}

	@Override
	public List<String> getHistory(final Player player) {
		final List<String> res = new ArrayList<String>();
		if (!player.hasQuest(QUEST_SLOT)) {
			return res;
		}
		final String questState = player.getQuest(QUEST_SLOT);
		res.add("Spotkałem Xylone, która mieszka w jaskini w swej chatce.");
		if ("rejected".equals(questState)) {
			res.add("Nie mam ochoty wykonywać próśb czarownicy - Xylony.");
			return res;
		}
		res.add("Xylona jej pierwsza prośba była, abym zabił błękitnego smoka, który znajduje się gdzieś w okolicy jej chatki.");
		if ("start".equals(questState)) {
			return res;
		}
		res.add("Zabiłem błękitnego smoka i zaniosłem Xylonie jego skórę w dowód, że pokonałem go samodzielnie!");
		if (questState.startsWith("czas_składniki")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Xylone, aby dała mi kolejne zlecenie. Hasło: składniki");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: składniki");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dwuglowyzielonysmok?".equals(questState)) {
			return res;
		}
		res.add("Xylona kazała mi zabić dwugłowego zielonego smoka i przynieść 5 skór zielonego smoka.");
		if ("dwuglowyzielonysmok".equals(questState)) {
			return res;
		}
		res.add("Zabiłem dwugłowego zielonego smoka i zaniosłem Xylonie potrzebne jej skóry!");
		if (questState.startsWith("czas_kiel")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Xylone, aby dała mi kolejne zlecenie. Hasło: smok");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: smok");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("kly?".equals(questState)) {
			return res;
		}
		res.add("Xylona tym razem nie chciała abym coś dla niej zabił. Mam jej przynieść tylko 50 kłów smoka.");
		if ("kielsmoka".equals(questState)) {
			return res;
		}
		res.add("Zaniosłem potrzebne Xylonie kły smoków!");
		if (questState.startsWith("czas_czarnysmok")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Xylone, aby dała mi kolejne zlecenie. Hasło: kieł smoka");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: kieł smoka");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dużesmoki?".equals(questState)) {
			return res;
		}
		res.add("Xylona poprosiła mnie, abym zabił czarne smoczysko i smoka arktycznego. Chciała również 3 skóry czarnego smoka oraz 3 skóry arktycznego smoka.");
		if ("czarnesmoczysko".equals(questState)) {
			return res;
		}
		res.add("Zabiłem czarne smoczysko i smoka arktycznego oraz zaniosłem jej potrzebne materiały!");
		if (questState.startsWith("czas_krewsmoka")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Xylone, aby dała mi kolejne zlecenie. Hasło: duże smoki");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: duże smoki");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("krew?".equals(questState)) {
			return res;
		}
		res.add("Xylona ponownie nie chciała, abym coś dla niej zabił. Jedynie co mam zrobić to przynieść 30 krwi smoków.");
		if ("krewsmoka".equals(questState)) {
			return res;
		}
		res.add("Zaniosłem potrzebną krew smoka Xylonie!");
		if (questState.startsWith("czas_glodna")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Xylone, aby dała mi kolejne zlecenie. Hasło: krew");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: krew");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("glodna?".equals(questState)) {
			return res;
		}
		res.add("Xylona poprosiła mnie, abym przyniósł dla niej trochę jedzenia, ponieważ zgłodniała. Mam przynieść: 100 mięsa, 40 chleba, 40 sera, 20 bukłaków z wodą.");
		if ("glodna".equals(questState)) {
			return res;
		}
		res.add("Przyniosłem jedzenie dla Xylony tak jak mnie o to prosiła.");
		if (questState.startsWith("czas_ostatnie")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Xylone, aby dała mi kolejne zlecenie. Hasło: jedzenie");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: jedzenie");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("ostatnie?".equals(questState)) {
			return res;
		}
		res.add("Xylona dała mi swoje ostatnie zadanie. Mam zabić resztę smoków, o które prosi oraz przynieść mi ich skóry. "
		+ "Dokładnie mam zabić: dwugłowy czarny smok, dwugłowy czerwony smok, dwugłowy niebieski smok, czerwony smok, zielony smok oraz złoty smok."
		+ "Mam przynieść: 10 skór czarnego, czerwonego, niebieskiego, zielonego oraz złotego smoka.");
		if ("ostatnie".equals(questState)) {
			return res;
		}
		res.add("To było ostatnie zadanie Xylony. Oddała mi ostatnią pamiątkę po swej zmarłej babci, był to amulet - pazur niebieskiego smoka. Powiedziała mi również, że od teraz mogę korzystać z jej magicznej kuli, która leży na stole.");
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
		return "Prośby Xylony";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
