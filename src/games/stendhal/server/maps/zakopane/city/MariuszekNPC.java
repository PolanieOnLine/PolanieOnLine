/***************************************************************************
 *                 (C) Copyright 2022-2023 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.zakopane.city;

import java.util.Arrays;
import java.util.Map;

import games.stendhal.common.Direction;
import games.stendhal.common.constants.Occasion;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.quest.StarterQuestLine;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.npc.ConversationPhrases;
import games.stendhal.server.entity.npc.ConversationStates;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.action.NPCEmoteAction;
import games.stendhal.server.entity.npc.action.SayTextAction;
import games.stendhal.server.entity.npc.action.SetQuestAction;
import games.stendhal.server.entity.npc.condition.AndCondition;
import games.stendhal.server.entity.npc.condition.GreetingMatchesNameCondition;
import games.stendhal.server.entity.npc.condition.QuestActiveCondition;
import games.stendhal.server.entity.npc.condition.QuestInStateCondition;
import games.stendhal.server.entity.npc.condition.QuestNotStartedCondition;

public class MariuszekNPC implements ZoneConfigurator {
	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		buildNPC(zone);
	}


	private void addNewPlayerAdvice(final SpeakerNPC npc) {
		final String help = "Początki bywają trudne, ale nie musisz błądzić. Zapytaj mnie o #broń, #jedzenie, #mikstury, "
				+ "#leczenie, #bank, #potwory lub #pieniądze. Jeśli szukasz zadania, zapytaj #'co dalej'. "
				+ "Swój aktualny cel znajdziesz też w dzienniku, w zakładce Zadania główne.";
		final ChatAction next = (player, sentence, speaker) -> {
			player.setQuest("meet_mariuszek", "done");
			speaker.say(StarterQuestLine.nextObjective(player));
		};
		for (final ConversationStates state : new ConversationStates[] {ConversationStates.ATTENDING, ConversationStates.INFORMATION_1}) {
			npc.add(state, ConversationPhrases.HELP_MESSAGES, null, ConversationStates.ATTENDING,
					help, new SetQuestAction("meet_mariuszek", "done"));
			npc.add(state, Arrays.asList("co dalej", "dalej", "zadanie", "zadania", "task", "quest"), null,
					ConversationStates.ATTENDING, null, next);
			npc.add(state, Arrays.asList("broń", "bron", "uzbrojenie", "sklep", "sklepy"), null,
					ConversationStates.ATTENDING,
					"Podstawową broń kupisz u Bogusia w szpitalu w Zakopanem S. Masz już broń na początek, "
					+ "więc warto zachować trochę monet na #jedzenie. Sklep Staska jest obok mostu w Zakopanem C, "
					+ "ale jego ofertę odblokujesz po wykonaniu zlecenia. Zapytaj #'co dalej', a wskażę Ci kolejny krok.", null);
			npc.add(state, Arrays.asList("jedzenie", "żywność", "zywnosc", "głód", "glod"), null,
					ConversationStates.ATTENDING,
					"Jedzenie kupisz u Jagny w karczmie w Zakopanem NE. Odnawia zdrowie stopniowo. "
					+ "Zabierz zapas na wyprawę, a jeśli masz mało monet, skorzystaj z bezpłatnego #leczenia.", null);
			npc.add(state, Arrays.asList("leczenie", "ulecz", "rany", "zdrowie"), null,
					ConversationStates.ATTENDING,
					"Gaździna Jadźka w szpitalu w Zakopanem S leczy bezpłatnie. Przywitaj się z nią i powiedz #'ulecz'. "
					+ "Nie musisz kupować eliksiru, żeby opatrzyć rany po powrocie do miasta.", null);
			npc.add(state, Arrays.asList("mikstury", "mikstura", "eliksir", "eliksiry", "potion"), null,
					ConversationStates.ATTENDING,
					"Eliksiry kupisz u Carmen w mieście Semos. Przejście do Semos znajdziesz w północno-wschodniej "
					+ "części Zakopanego C. Na początku możesz korzystać z #jedzenia i bezpłatnego #leczenia u Jadźki.", null);
			npc.add(state, Arrays.asList("bank", "depozyt", "schowek"), null,
					ConversationStates.ATTENDING,
					"Bank znajdziesz w Zakopanem S. Na parterze są schowki, w których możesz zostawić swoje przedmioty. "
					+ "Do zwykłego schowka nie musisz wchodzić na piętro.", null);
			npc.add(state, Arrays.asList("potwory", "potwór", "potwor", "stworzenia"), null,
					ConversationStates.ATTENDING,
					"Sabała zna tutejsze potwory. Przywitaj się z nim, powiedz #'potwór', a gdy zapyta, podaj nazwę stworzenia. "
					+ "Na początek Pietrek i Adaś wskażą Ci słabszych przeciwników. Zapytaj mnie #'co dalej'.", null);
			npc.add(state, Arrays.asList("pieniądze", "pieniadze", "zarobek", "monety", "sprzedaż"), null,
					ConversationStates.ATTENDING,
					"Boguś w szpitalu w Zakopanem S skupuje skóry i futra. Sprawdź jego zakładkę Sprzedaj, "
					+ "żeby zobaczyć, co przyjmuje. Nie kupuje każdego łupu. Część wyposażenia dostaniesz za pierwsze zadania, "
					+ "więc nie musisz od razu wydawać wszystkich monet na zakupy.", null);
		}
	}

	private void buildNPC(final StendhalRPZone zone) {
		final SpeakerNPC npc = new SpeakerNPC("Mariuszek") {
			@Override
			protected void createDialog() {
				String greet = " W czym mogę #pomóc tym razem?";
				if (Occasion.CHRISTMAS) {
					greet = " Życzę Ci wszystkiego dobrego oraz spokojnych i wesołych świąt Bożegonarodzenia!";
				}
				if (Occasion.EASTER) {
					greet = " Życzę Ci smacznego jajka na Wielkanoc, oby zajączek był dla Ciebie hojny!";
				}
				if (Occasion.MINETOWN) {
					greet = " Słyszałem, że w tych dniach to potwory świętują. Życzę Ci wesołego Halloween! Pamiętaj, cukierek albo psikus!";
				}
				if (Occasion.BIRTHDAY) {
					greet = " Wiedziałeś, że #PolanieOnLine obchodzi w najbliższych dniach swą rocznicę? Życzę Tobie smacznego torciku urodzinowego!";
				}
				if (Occasion.MOREXP) {
					greet += " Nieco podsłuchałem również rozmów Fryderyka z innymi i ponoć potwory mają niespodziankę dla graczy! Musisz koniecznie sprawdzić!";
				}
				addGreeting(null,new SayTextAction("Witaj [name]!" + greet));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition("meet_mariuszek")),
						ConversationStates.INFORMATION_1,
						"Och, witaj nieznajomy! Wyglądasz na nieco zdezorientowanego... Mógłbym Ci jakoś #pomóc?",
						new SetQuestAction("meet_mariuszek", "start"));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestInStateCondition("meet_mariuszek", "start")),
						ConversationStates.INFORMATION_1,
						"Witaj ponownie! Mogę pomóc Ci odnaleźć się w Zakopanem. Chcesz usłyszeć, od czego zacząć?",
						null);

				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.YES_MESSAGES,
						new QuestActiveCondition("meet_mariuszek"),
						ConversationStates.ATTENDING,
						null,
						(player, sentence, speaker) -> {
							player.setQuest("meet_mariuszek", "done");
							speaker.say(StarterQuestLine.nextObjective(player));
						});

				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.NO_MESSAGES,
						new QuestActiveCondition("meet_mariuszek"),
						ConversationStates.ATTENDING,
						"Jeśli nie potrzebujesz ode mnie pomocy to ja Ci przekażę dość ciekawą wiadomość. #Burmistrz Zakopane ponoć poszukuje zapasów, by uzupełnić magazyny."
						+ " Miasto potrzebuje również Twojej pomocy w jego chronieniu przed różnymi hordami stworów!",
						new NPCEmoteAction("spogląda na świerk.", false));

				add(ConversationStates.ATTENDING,
                		Arrays.asList("radę", "rada"),
                		null,
                		ConversationStates.ATTENDING,
                		"Ja Tobie mogę dać radę taką iż musisz nabrać większego doświadczenia w starciach z innymi!",
						null);

				add(ConversationStates.ATTENDING,
                		"burmistrz",
                		null,
                		ConversationStates.ATTENDING,
                		"Aktualnie nim jest #'gazda Wojtek'.",
						null);

				add(ConversationStates.ATTENDING,
                		Arrays.asList("gazda", "wojtek"),
                		null,
                		ConversationStates.ATTENDING,
                		"Znajdziesz go niedaleko... Na północ stąd, po prawej stronie od mostu stoi taki jeden ogroooomny budynek, "
                		+ "jest to ratusz miasta, a w nim się właśnie znajduje #burmistrz. Wojtkowi ponoć pomaga również #Gościrad.",
						null);

				add(ConversationStates.ATTENDING,
                		"gościrad",
                		null,
                		ConversationStates.ATTENDING,
                		"Gościrad opowie Tobie kto aktualnie w tych rejonach potrzebuje pomocy.",
						null);

				addJob("Lubię oglądać i wąchać *snif* *snif* aachhhh... Och, wybacz Ty tu jeszcze stoisz.");
				addNewPlayerAdvice(this);
				addGoodbye("Czeeść! Życzę Tobie miłego dzionka! *snif* *snif* aachhhh...");
			}

			@Override
			protected void onGoodbye(RPEntity player) {
				setDirection(Direction.UP);
			}
		};

		npc.setDescription("Oto Mariuszek. Lubi chyba spoglądać na świerki...");
		npc.setEntityClass("npcstarygoral3");
		npc.setGender("M");
		npc.setPosition(38, 27);
		npc.setDirection(Direction.UP);
		zone.add(npc);
	}
}
