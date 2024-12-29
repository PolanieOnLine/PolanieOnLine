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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
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

public class ProsbyWiedzmy extends AbstractQuest {
	private static Logger logger = Logger.getLogger(ProsbyWiedzmy.class);

	private static final String QUEST_SLOT = "prosby_wiedzmy";
	private final SpeakerNPC npc = npcs.get("Benigna");
	private static final int DELAY_IN_MINUTES = 60;
	private static final List<String> HELP_MESSAGES = Arrays.asList("pomożesz", "pomozesz", "pomogę", "pomoge", "tak");

	private void giveRewards(Player player, Map<String, Integer> items, int xp, int karma) {
		Optional.ofNullable(player).ifPresent(p -> {
			items.forEach(p::drop);
			p.addXP(xp);
			p.addKarma(karma);
		});
	}

	private void step1() {
		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestCompletedCondition(QUEST_SLOT),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Już " + player.getGenderVerb("wypełniłeś") + " moje zadanie. Idź w pokoju.");
				});

		npc.add(ConversationStates.ATTENDING,
				ConversationPhrases.QUEST_MESSAGES,
				new QuestNotStartedCondition(QUEST_SLOT),
				ConversationStates.QUEST_OFFERED,
				"W mej chacie, wśród pradawnych ksiąg, odkryłam sekret kontroli smoków. Jednakże potrzebuję twojej pomocy."
					+ " W okolicy krąży błękitny smok. Czy wesprzesz mnie w walce z nim?",
				null);

		Map<String, Pair<Integer, Integer>> toKill = Map.of("błękitny smok", new Pair<>(1, 0));
		List<ChatAction> actions = List.of(
			new IncreaseKarmaAction(5),
			new SetQuestAction(QUEST_SLOT, 0, "start"),
			new StartRecordingKillsAction(QUEST_SLOT, 1, toKill)
		);

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.YES_MESSAGES, null,
				ConversationStates.ATTENDING,
				"Smok ów, jak mówiłam, nawiedza me ziemie. Jeśli go zgładzisz, przynieś mi jego skórę na dowód twej odwagi.",
				new MultipleActions(actions));

		npc.add(ConversationStates.QUEST_OFFERED,
				ConversationPhrases.NO_MESSAGES, null, ConversationStates.IDLE,
				"Niech inny śmiałek podejmie to wyzwanie. Nie będę cię zatrzymywać.",
				new SetQuestAndModifyKarmaAction(QUEST_SLOT, "rejected", -10));
	}

	private void step2() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "start"),
					new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Czyżbyś " + player.getGenderVerb("lękał") + " się zmierzyć z błękitnym smokiem? Nie zawiedź mnie.");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "start"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new NotCondition(new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 1))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Przynieś mi dowód swego zwycięstwa, abym mogła uwierzyć w twe czyny.");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "start"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 1)
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Twoja odwaga mnie nie zawiodła. Wróć za godzinę, a przygotuję kolejne zadanie.");
					giveRewards(player, Map.of("skóra niebieskiego smoka", 1), 5000, 10);
					player.setQuest(QUEST_SLOT, "czas_składniki;" + System.currentTimeMillis());
				});
	}

	private void czas1() {
		final String extraTrigger = "składniki";
		List<String> questTrigger = new LinkedList<>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_składniki"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))
				),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Czas jeszcze nie nadszedł. Wróć za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_składniki"),
					new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)
				),
				ConversationStates.ATTENDING,
				"Witaj ponownie, śmiałku. Nadszedł czas na kolejną próbę. Muszę zdobyć materiały do stworzenia amuletu kontroli smoków. Pomożesz mi?",
				new SetQuestAction(QUEST_SLOT, "dwuglowyzielonysmok?"));

		Map<String, Pair<Integer, Integer>> toKill = Map.of("dwugłowy zielony smok", new Pair<>(1, 0));
		List<ChatAction> actions = List.of(
			new SetQuestAction(QUEST_SLOT, 0, "dwuglowyzielonysmok"),
			new StartRecordingKillsAction(QUEST_SLOT, 1, toKill)
		);

		npc.add(ConversationStates.ATTENDING, HELP_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "dwuglowyzielonysmok?"),
				ConversationStates.ATTENDING,
				"Twa odwaga jest godna podziwu, a czy idzie ona w patrze z honorem? Poskrom proszę dwugłowego zielonego smoka oraz przynieść mi pięć skór w dowód jego pokonania.",
				new MultipleActions(actions));
	}

	private void step3() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "dwuglowyzielonysmok"),
					new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Czyżby dwugłowy zielony smok przerósł twoją odwagę? Nie cofaj się w tej chwili próby!");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "dwuglowyzielonysmok"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new NotCondition(new PlayerHasItemWithHimCondition("skóra zielonego smoka", 5))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say(player.getGenderVerb("Pokonałeś") + " smoka, lecz potrzebuję pięciu skór zielonego smoka. Przynieś je, bym mogła kontynuować swe dzieło.");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "dwuglowyzielonysmok"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new PlayerHasItemWithHimCondition("skóra zielonego smoka", 5)
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Twoja determinacja godna jest pochwały! Wróć za godzinę, a dam ci nowe wyzwanie.");
					giveRewards(player, Map.of("skóra zielonego smoka", 5), 7500, 20);
					player.setQuest(QUEST_SLOT, "czas_kiel;" + System.currentTimeMillis());
				});
	}

	private void czas2() {
		final String extraTrigger = "smok";
		List<String> questTrigger = new LinkedList<>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_kiel"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))
				),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze trochę cierpliwości, powróć za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_kiel"),
					new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)
				),
				ConversationStates.ATTENDING,
				"Ach, cieszę się, że jesteś znowu tutaj! Potrzebuję teraz kłów smoków, by zakończyć pewien magiczny rytuał. Przynieś ich pięćdziesiąt!",
				new SetQuestAction(QUEST_SLOT, "kielsmoka"));
	}

	private void step4() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "kielsmoka"),
					new NotCondition(new PlayerHasItemWithHimCondition("kieł smoka", 50))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Nie mam jeszcze wszystkich kłów smoka, a czas nagli. Przyspiesz swe działania!");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "kielsmoka"),
					new PlayerHasItemWithHimCondition("kieł smoka", 50)
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Znakomicie! Teraz mogę kontynuować swój rytuał. Wróć za godzinę, a dowiesz się, co dalej.");
					giveRewards(player, Map.of("kieł smoka", 50), 10000, 20);
					player.setQuest(QUEST_SLOT, "czas_czarnysmok;" + System.currentTimeMillis());
				});
	}

	private void czas3() {
		final String extraTrigger = "kieł smoka";
		List<String> questTrigger = new LinkedList<>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czarnysmok"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))
				),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze czas nie nadszedł. Wróć za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_czarnysmok"),
					new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)
				),
				ConversationStates.ATTENDING,
				"Dobrze więc, smocze kły nieco pomogły podczas odprawienia rytuału. Jednak potrzebuję czegoś więcej. Pomożesz?",
				new SetQuestAction(QUEST_SLOT, "dużesmoki?"));

		Map<String, Pair<Integer, Integer>> toKill = Map.of(
				"czarne smoczysko", new Pair<>(1, 0),
				"smok arktyczny", new Pair<>(1, 0)
			);
		List<ChatAction> actions = List.of(
			new SetQuestAction(QUEST_SLOT, 0, "czarnesmoczysko"),
			new StartRecordingKillsAction(QUEST_SLOT, 1, toKill)
		);

		npc.add(ConversationStates.ATTENDING, HELP_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "dużesmoki?"),
				ConversationStates.ATTENDING,
				"Dobrze. Niestety wykonany rytuał, aby wzmocnić amulet nie podziałał na #'czarne smoczysko' oraz #'smoka arktycznego'."
					+ " Pokonaj te oba przerośnięte gady oraz przynieś jeszcze po #'3 skóry czarnego smoka' i #'3 skóry arktycznego smoka'.",
				new MultipleActions(actions));
	}

	private void step5() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "czarnesmoczysko"),
					new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Nie podjąłeś jeszcze starcia z czarnym smoczyskiem i smokiem arktycznym? Odwagi!");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "czarnesmoczysko"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new NotCondition(new AndCondition(
						new PlayerHasItemWithHimCondition("skóra czarnego smoka", 3),
						new PlayerHasItemWithHimCondition("skóra arktycznego smoka", 3)
					))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Choć " + player.getGenderVerb("pokonałeś") + " smoki, ale brakuje mi jeszcze odpowiednich skór. Przynieś trzy skóry czarnego i arktycznego smoka.");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "czarnesmoczysko"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new PlayerHasItemWithHimCondition("skóra czarnego smoka", 3),
					new PlayerHasItemWithHimCondition("skóra arktycznego smoka", 3)
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Znakomicie! Dzięki tobie mogę dalej kontynuować. Wróć za godzinę, a dowiesz się, co dalej.");
					giveRewards(player, Map.of(
						"skóra czarnego smoka", 3,
						"skóra arktycznego smoka", 3
					), 12500, 20);
					player.setQuest(QUEST_SLOT, "czas_krewsmoka;" + System.currentTimeMillis());
				});
	}

	private void czas4() {
		final String extraTrigger = "duże smoki";
		List<String> questTrigger = new LinkedList<>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_krewsmoka"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))
				),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Nie przeszkadzaj mi teraz, powróć za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_krewsmoka"),
					new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)
				),
				ConversationStates.ATTENDING,
				"Smoki to potężne istoty, a ich moc jest trudna do ujarzmienia. Aby udoskonalić mój amulet, potrzebuję pomocy. Czy podejmiesz się tego zadania?",
				new SetQuestAction(QUEST_SLOT, "krew?"));

		npc.add(ConversationStates.ATTENDING, HELP_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "krew?"),
				ConversationStates.ATTENDING,
				"Twoja odwaga jest godna podziwu. Przynieś mi 30 fiolek z krwią smoków, abym mogła wzmocnić amulet i zabezpieczyć się przed ich gniewem.",
				new SetQuestAction(QUEST_SLOT, 0, "krewsmoka"));
	}

	private void step6() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "krewsmoka"),
					new NotCondition(new AndCondition(
						new PlayerHasItemWithHimCondition("smocza krew", 30)
					))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Smocza krew jest kluczem do ujarzmienia ich mocy. Przynieś ją, abyśmy mogli uczynić amulet niezwyciężonym.");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "krewsmoka"),
					new PlayerHasItemWithHimCondition("smocza krew", 30)
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Twoja pomoc jest bezcenna. Wróć za godzinę, a zdradzę ci, co musimy uczynić dalej.");
					giveRewards(player, Map.of("smocza krew", 30), 15000, 20);
					player.setQuest(QUEST_SLOT, "czas_glodna;" + System.currentTimeMillis());
				});
	}

	private void czas5() {
		final String extraTrigger = "krew";
		List<String> questTrigger = new LinkedList<>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_glodna"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))
				),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Nie przeszkadzaj mi w tym momencie. Powróć za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_glodna"),
					new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)
				),
				ConversationStates.ATTENDING,
				"Czuję, że czas wielkiego rytuału zbliża się, lecz nie mogę działać o pustym żołądku. Pomożesz mi zdobyć zapasy, abym miała siłę na dalsze przygotowania?",
				new SetQuestAction(QUEST_SLOT, "glodna?"));

		npc.add(ConversationStates.ATTENDING, HELP_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "glodna?"),
				ConversationStates.ATTENDING,
				"Przynieś mi 100 kawałków mięsa, 40 bochenków chleba, 40 kawałków sera oraz 20 bukłaków z wodą. To da mi siłę na kolejne wyzwania.",
				new SetQuestAction(QUEST_SLOT, 0, "glodna"));
	}

	private void step7() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "glodna"),
					new NotCondition(new AndCondition(
						new PlayerHasItemWithHimCondition("mięso", 100),
						new PlayerHasItemWithHimCondition("chleb", 40),
						new PlayerHasItemWithHimCondition("ser", 40),
						new PlayerHasItemWithHimCondition("bukłak z wodą", 20)
					))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Nie " + player.getGenderVerb("przyniosłeś") + " jeszcze zapasów, których potrzebuję. Bez nich nie dokończę przygotowań.");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "glodna"),
					new PlayerHasItemWithHimCondition("mięso", 100),
					new PlayerHasItemWithHimCondition("chleb", 40),
					new PlayerHasItemWithHimCondition("ser", 40),
					new PlayerHasItemWithHimCondition("bukłak z wodą", 20)
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Wspaniale! Teraz mogę skupić się na naszym celu. Wróć za godzinę, a dam ci nowe zadanie.");
					giveRewards(player, Map.of(
						"mięso", 100,
						"chleb", 40,
						"ser", 40,
						"bukłak z wodą", 20
					), 20000, 40);
					player.setQuest(QUEST_SLOT, "czas_ostatnie;" + System.currentTimeMillis());
				});
	}

	private void czas6() {
		final String extraTrigger = "jedzenie";
		List<String> questTrigger = new LinkedList<>(ConversationPhrases.QUEST_MESSAGES);
		questTrigger.add(extraTrigger);

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_ostatnie"),
					new NotCondition(new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES))
				),
				ConversationStates.ATTENDING, null,
				new SayTimeRemainingAction(QUEST_SLOT, 1, DELAY_IN_MINUTES, " Jeszcze nie minęła godzina. Wróć proszę za "));

		npc.add(ConversationStates.ATTENDING, questTrigger,
				new AndCondition(
					new QuestStateStartsWithCondition(QUEST_SLOT, "czas_ostatnie"),
					new TimePassedCondition(QUEST_SLOT, 1, DELAY_IN_MINUTES)
				),
				ConversationStates.ATTENDING,
				"Już prawie mój amulet jest ukończony, lecz potrzebuję twojej pomocy, by dokończyć dzieła. Podejmiesz wyzwanie?",
				new SetQuestAction(QUEST_SLOT, "ostatnie?"));

		//kogo ma zabic w nastepnym kroku
		final Map<String, Pair<Integer, Integer>> toKill = Map.of(
			"czerwony smok", new Pair<>(1, 0),
			"zielony smok", new Pair<>(1, 0),
			"dwugłowy czerwony smok", new Pair<>(1, 0),
			"dwugłowy czarny smok", new Pair<>(1, 0),
			"dwugłowy niebieski smok", new Pair<>(1, 0),
			"złoty smok", new Pair<>(1, 0)
		);
		final List<ChatAction> actions = List.of(
			new SetQuestAction(QUEST_SLOT, 0, "ostatnie"),
			new StartRecordingKillsAction(QUEST_SLOT, 1, toKill)
		);

		npc.add(ConversationStates.ATTENDING, HELP_MESSAGES,
				new QuestInStateCondition(QUEST_SLOT, "ostatnie?"),
				ConversationStates.ATTENDING,
				"Musisz pokonać najpotężniejsze smoki tego świata: dwugłowy czarny smok, dwugłowy czerwony smok, dwugłowy niebieski smok, czerwony smok, zielony smok oraz złoty smok. Przynieś po 10 skór każdego z nich, abyśmy mogli zakończyć dzieło!",
				new MultipleActions(actions));
	}

	private void step8() {
		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "ostatnie"),
					new NotCondition(new KilledForQuestCondition(QUEST_SLOT, 1))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Nie " + player.getGenderVerb("pokonałeś") + " jeszcze smoków. Musisz działać szybko, aby udowodnić swą wartość!");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "ostatnie"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new NotCondition(new AndCondition(
						new PlayerHasItemWithHimCondition("skóra złotego smoka", 10),
						new PlayerHasItemWithHimCondition("skóra czarnego smoka", 10),
						new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 10),
						new PlayerHasItemWithHimCondition("skóra zielonego smoka", 10),
						new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 10)
					))
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Choć " + player.getGenderVerb("pokonałeś") + " smoki, brakuje mi jeszcze ich skór. Przynieś po 10 skór każdego rodzaju, abyśmy mogli zakończyć nasz rytuał.");
				});

		npc.add(ConversationStates.IDLE, ConversationPhrases.GREETING_MESSAGES,
				new AndCondition(
					new QuestInStateCondition(QUEST_SLOT, 0, "ostatnie"),
					new KilledForQuestCondition(QUEST_SLOT, 1),
					new PlayerHasItemWithHimCondition("skóra złotego smoka", 10),
					new PlayerHasItemWithHimCondition("skóra czarnego smoka", 10),
					new PlayerHasItemWithHimCondition("skóra czerwonego smoka", 10),
					new PlayerHasItemWithHimCondition("skóra zielonego smoka", 10),
					new PlayerHasItemWithHimCondition("skóra niebieskiego smoka", 10)
				),
				ConversationStates.ATTENDING, null,
				(player, sentence, raiser) -> {
					raiser.say("Znakomicie! Teraz mogę ukończyć amulet, który ujarzmi smoki. Przyjmij tę nagrodę jako znak mojej wdzięczności i używaj jej mądrze.");
					giveRewards(player, Map.of(
						"skóra złotego smoka", 10,
						"skóra czarnego smoka", 10,
						"skóra czerwonego smoka", 10,
						"skóra zielonego smoka", 10,
						"skóra niebieskiego smoka", 10
					), 35000, 100);
					final Item pazurek = SingletonRepository.getEntityManager().getItem("pazur niebieskiego smoka");
					pazurek.setBoundTo(player.getName());
					player.equipOrPutOnGround(pazurek);
					player.setBaseHP(20 + player.getBaseHP());
					player.heal(20, true);
					player.setQuest(QUEST_SLOT, "done");
				});
	}

	@Override
	public void addToWorld() {
		fillQuestInfo(
				"Prośby Wiedźmy",
				"Benigna chce kontrolować wszystkie smoki swoim nowym amuletem. Czy to jest w ogólne możliwe?",
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
		res.add(player.getGenderVerb("Spotkałem") + " Benigne, która mieszka w jaskini w swej chatce.");
		if ("rejected".equals(questState)) {
			res.add("Nie mam ochoty wykonywać próśb czarownicy - Benigny.");
			return res;
		}
		res.add("Pierwsza prośba Benigny była, abym " + player.getGenderVerb("zabił") + " błękitnego smoka, który znajduje się gdzieś w okolicy jej chatki.");
		if (questState.startsWith("start")) {
			return res;
		}
		res.add(player.getGenderVerb("Zabiłem") + " błękitnego smoka i " + player.getGenderVerb("zaniosłem") + " Benignie jego skórę w dowód, że pokonałem go samodzielnie!");
		if (questState.startsWith("czas_składniki")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Benigne, aby dała mi kolejne zlecenie. Hasło: składniki");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: składniki");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dwuglowyzielonysmok?".equals(questState)) {
			return res;
		}
		res.add("Benigna kazała mi zabić dwugłowego zielonego smoka i przynieść 5 skór zielonego smoka.");
		if (questState.startsWith("dwuglowyzielonysmok")) {
			return res;
		}
		res.add(player.getGenderVerb("Zabiłem") + " dwugłowego zielonego smoka i " + player.getGenderVerb("zaniosłem") + " Benignie potrzebne jej skóry!");
		if (questState.startsWith("czas_kiel")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Benigne, aby dała mi kolejne zlecenie. Hasło: smok");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: smok");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("kly?".equals(questState)) {
			return res;
		}
		res.add("Benigna tym razem nie chciała abym coś dla niej zabił. Mam jej przynieść tylko 50 kłów smoka.");
		if (questState.startsWith("kielsmoka")) {
			return res;
		}
		res.add(player.getGenderVerb("Zaniosłem") + " potrzebne Benignie kły smoków!");
		if (questState.startsWith("czas_czarnysmok")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Benigne, aby dała mi kolejne zlecenie. Hasło: kieł smoka");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: kieł smoka");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("dużesmoki?".equals(questState)) {
			return res;
		}
		res.add("Benigna poprosiła mnie, abym " + player.getGenderVerb("zabił") + " czarne smoczysko i smoka arktycznego. Chciała również 3 skóry czarnego smoka oraz 3 skóry arktycznego smoka.");
		if (questState.startsWith("czarnesmoczysko")) {
			return res;
		}
		res.add(player.getGenderVerb("Zabiłem") + " czarne smoczysko i smoka arktycznego oraz " + player.getGenderVerb("zaniosłem") + " jej potrzebne materiały!");
		if (questState.startsWith("czas_krewsmoka")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Benigne, aby dała mi kolejne zlecenie. Hasło: duże smoki");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: duże smoki");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("krew?".equals(questState)) {
			return res;
		}
		res.add("Benigna ponownie nie chciała, abym coś dla niej zabił. Jedynie co mam zrobić to przynieść 30 krwi smoków.");
		if (questState.startsWith("krewsmoka")) {
			return res;
		}
		res.add(player.getGenderVerb("Zaniosłem") + " potrzebną krew smoka Benignie!");
		if (questState.startsWith("czas_glodna")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Benigne, aby dała mi kolejne zlecenie. Hasło: krew");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: krew");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("glodna?".equals(questState)) {
			return res;
		}
		res.add("Benigna poprosiła mnie, abym przyniósł dla niej trochę jedzenia, ponieważ zgłodniała. Mam przynieść: 100 mięsa, 40 chleba, 40 sera, 20 bukłaków z wodą.");
		if (questState.startsWith("glodna")) {
			return res;
		}
		res.add(player.getGenderVerb("Przyniosłem") + " jedzenie dla Benigny tak jak mnie o to prosiła.");
		if (questState.startsWith("czas_ostatnie")) {
			if (new TimePassedCondition(QUEST_SLOT,1,DELAY_IN_MINUTES).fire(player, null, null)) {
				res.add("Mam ponownie odwiedzić Benigne, aby dała mi kolejne zlecenie. Hasło: jedzenie");
			} else {
				res.add("Mam wrócić za " + DELAY_IN_MINUTES + " minut. Hasło: jedzenie");
			}
			return res;
		}
		res.add("Powiedz: pomoge.");
		if ("ostatnie?".equals(questState)) {
			return res;
		}
		res.add("Benigna dała mi swoje ostatnie zadanie. Mam zabić resztę smoków, o które prosi oraz przynieść mi ich skóry. "
		+ "Dokładnie mam zabić: dwugłowy czarny smok, dwugłowy czerwony smok, dwugłowy niebieski smok, czerwony smok, zielony smok oraz złoty smok."
		+ "Mam przynieść: 10 skór czarnego, czerwonego, niebieskiego, zielonego oraz złotego smoka.");
		if (questState.startsWith("ostatnie")) {
			return res;
		}
		res.add("To było ostatnie zadanie Benigny. Oddała mi ostatnią pamiątkę po swej zmarłej babce, był to amulet - pazur niebieskiego smoka. Powiedziała mi również, że od teraz mogę korzystać z jej magicznej kuli, która leży na stole.");
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
		return "Prośby Wiedźmy";
	}

	@Override
	public String getNPCName() {
		return npc.getName();
	}
}
