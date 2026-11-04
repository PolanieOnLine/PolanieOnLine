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
		final String help = "Chcesz wiedzieć, gdzie kupić #broń, #jedzenie albo #mikstury? Mogę też powiedzieć coś o tym, "
				+ "jak działa #leczenie, gdzie jest #bank, jakie są tu #potwory i skąd brać #pieniądze. Nie wiesz, za co się zabrać? Zapytaj #'co dalej'. "
				+ "Zajrzyj też do dziennika, do zadań głównych.";
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
					"Po broń zajrzyj do Bogusia, do szpitala na południu Zakopanego. Na razie masz czym walczyć, "
					+ "więc zostaw sobie trochę monet na #jedzenie. Do Staska też możesz zajrzeć. Ma sklep obok mostu w centrum Zakopanego, "
					+ "ale zanim coś ci sprzeda, trzeba mu pomóc. Zapytaj #'co dalej', to powiem ci, od czego zacząć.", null);
			npc.add(state, Arrays.asList("jedzenie", "żywność", "zywnosc", "głód", "glod"), null,
					ConversationStates.ATTENDING,
					"Po jedzenie idź do Jagny. Jej karczma jest na północnym wschodzie Zakopanego. Jedzenie pomaga odzyskać zdrowie, ale trzeba trochę poczekać. "
					+ "Weź coś na drogę. A jeśli nie masz monet, zapytaj mnie o #leczenie.", null);
			npc.add(state, Arrays.asList("leczenie", "ulecz", "rany", "zdrowie"), null,
					ConversationStates.ATTENDING,
					"Gaździna Jadźka leczy za darmo. Znajdziesz ją w szpitalu na południu Zakopanego. Przywitaj się i powiedz #'ulecz'. "
					+ "Po walce warto do niej zajrzeć, zanim znów ruszysz w drogę.", null);
			npc.add(state, Arrays.asList("mikstury", "mikstura", "eliksir", "eliksiry", "potion"), null,
					ConversationStates.ATTENDING,
					"Eliksiry sprzedaje Carmen w Semos. Przejście do tego miasta jest na północnym wschodzie "
					+ "centrum Zakopanego. Korzystaj też z #jedzenia i #leczenia u Jadźki.", null);
			npc.add(state, Arrays.asList("bank", "depozyt", "schowek"), null,
					ConversationStates.ATTENDING,
					"Bank jest na południu miasta. Na parterze znajdziesz schowki. Zostaw tam rzeczy, których nie chcesz nosić ze sobą. "
					+ "Nie musisz iść na piętro, żeby z nich korzystać.", null);
			npc.add(state, Arrays.asList("potwory", "potwór", "potwor", "stworzenia"), null,
					ConversationStates.ATTENDING,
					"O potworach pogadaj z Sabałą. Przywitaj się, powiedz #'potwór', a potem podaj nazwę tego, o którego chcesz zapytać. "
					+ "A jeśli dopiero zaczynasz, zajrzyj najpierw do Pietrka i Adasia. Zapytaj mnie #'co dalej', to podpowiem.", null);
			npc.add(state, Arrays.asList("pieniądze", "pieniadze", "zarobek", "monety", "sprzedaż"), null,
					ConversationStates.ATTENDING,
					"Boguś w szpitalu kupuje skóry i futra. Zajrzyj do niego i wybierz Sprzedaj, "
					+ "to zobaczysz, co możesz mu oddać. Nie bierze wszystkiego. Trochę wyposażenia dostaniesz za pierwsze zadania, "
					+ "więc nie wydawaj od razu wszystkich monet.", null);
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
						"Witaj! Pierwszy raz w Zakopanem? Mogę ci jakoś #pomóc?",
						new SetQuestAction("meet_mariuszek", "start"));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(new GreetingMatchesNameCondition(getName()),
								new QuestInStateCondition("meet_mariuszek", "start")),
						ConversationStates.INFORMATION_1,
						"O, znowu jesteś! Chcesz, żebym podpowiedział ci, od czego zacząć?",
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
						"W porządku. Gdyby coś, znajdziesz mnie tutaj. A jeśli szukasz zajęcia, #burmistrz zawsze potrzebuje pomocy."
						+ " Zapytaj o niego, to powiem ci, gdzie go szukać.",
						new NPCEmoteAction("spogląda na świerk.", false));

				add(ConversationStates.ATTENDING,
						Arrays.asList("radę", "rada"),
						null,
						ConversationStates.ATTENDING,
						"Na początek nie rzucaj się na silne potwory. Zajrzyj do Pietrka i Adasia. Zapytaj mnie #'co dalej', jeśli nie wiesz, za co się zabrać.",
						null);

				add(ConversationStates.ATTENDING,
						"burmistrz",
						null,
						ConversationStates.ATTENDING,
						"Naszym burmistrzem jest #'gazda Wojtek'.",
						null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("gazda", "wojtek"),
						null,
						ConversationStates.ATTENDING,
						"Idź na północ. Po prawej stronie mostu stoi duży budynek. "
						+ "To ratusz. Tam znajdziesz Wojtka i jego pomocnika, #Gościrada.",
						null);

				add(ConversationStates.ATTENDING,
						"gościrad",
						null,
						ConversationStates.ATTENDING,
						"Gościrad wie, kto w okolicy szuka pomocy. Znajdziesz go w ratuszu.",
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
