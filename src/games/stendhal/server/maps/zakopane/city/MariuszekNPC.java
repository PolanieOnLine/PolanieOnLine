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
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

public class MariuszekNPC implements ZoneConfigurator {
	private String nextStoryAdvice(final Player player) {
		if (!player.isQuestCompleted("meet_pietrek")) {
			return "Porozmawiaj z Pietrkiem w domku bohaterów. Ma swoje lata, ale wciąż potrafi nauczyć czegoś młodych. Dokończ z nim naukę, nim ruszysz dalej.";
		}
		if (!player.isQuestCompleted("pomoc_adasiowi")) {
			return "Pogadasz z Adasiem w domku bohaterów? Biedak boi się zejść do piwnicy, odkąd zalęgły się tam szczury i węże. Jego rodzina potrzebuje miejsca na zimowe zapasy.";
		}
		if (!player.isQuestCompleted("scythe_fryderyk")) {
			return "Zajrzyj do Fryderyka w Zakopanem S. Próbuje zdążyć ze żniwami przed mrozami, a nie ma czym kosić. Przydałoby się zdobyć dla niego kosę.";
		}
		if (!player.isQuestCompleted("wegiel_na_opal")) {
			return "U Staska w sklepie obok mostu brakuje opału. Pomóż mu z węglem, zanim śnieg zasypie drogi. Wieści o twojej pomocy sąsiadom już do niego dotarły.";
		}
		for (Step step : Step.values()) {
			if (player.isQuestCompleted(step.slot))
				continue;
			switch (step) {
			case DELIVERY:
				return "Zajrzyj do Staska. Opał już ma, ale ostatnio zamartwia się jakąś dostawą. Może tobie powie więcej.";
			case GUEST:
				return "Gościrad szuka woźnicy od tego fałszywego zamówienia. Znajdziesz go w ratuszu w centrum Zakopanego.";
			case SEAL:
				return "Brzezdom wypatrzył skład na polanie we wschodnim Kościelisku. Porozmawiaj z nim, zanim zaczniesz się tam rozglądać.";
			case DEBT:
				return "Gazda Wojtek w ratuszu chce ustalić, do kogo należą znalezione zapasy. Zanieś mu wieści od Staska i Jagny.";
			case SEMOS_LETTER:
				return "Ten list warto pokazać porucznikowi Drilenunowi w ratuszu Semos. Wojtek nie dopadnie sam ludzi, którzy odbierają tam transporty.";
			case SEMOS_SIGN:
				return "Dagobert w banku Semos może coś wiedzieć o znaku trzech kółek. Pokaż mu kwit.";
			case SEMOS_PICKUP:
				return "Przy gospodzie Margaret w Semos umawiano odbiór skrzyń. Ona zna swoich gości, może pomoże ci odnaleźć tragarza.";
			case RADAN_PASSAGE:
				return "Drilenun w Semos trafił chyba na kryjówkę Radana. Jeśli chcesz pomóc, porozmawiaj z nim o chodniku pod miastem.";
			case RADAN_RESCUE:
			case RADAN_TRANSPORT:
			case RADAN_FINALE:
				return "Drilenun czeka w ratuszu Semos na wieści z kryjówki. Uważaj na siebie pod ziemią. Ci ludzie nie oddadzą zapasów po dobroci.";
			case MINE_LEDGER:
				return "Drilenuna niepokoi jeszcze dostawa do kopalni Semos. Zajrzyj do niego w ratuszu, ale nie spiesz się z wyprawą pod ziemię. Tam łatwo wpaść w kłopoty.";
			case MINE_TRACKS:
			case MINE_VOICES:
				return "Barbarus z kopalni Semos widział ślady obcych ludzi w tunelach. Lepiej porozmawiać z nim, zanim wejdziesz głębiej.";
			case MINE_DISPATCH:
				return "Phalk pilnuje przejścia w kopalni Semos. Jeśli szukasz tych, którzy kręcą się tam po nocy, on powinien wiedzieć najwięcej.";
			case MOUNTAIN_MINERS:
				return "Barbarus z kopalni Semos martwi się o dwóch górników. Warto do niego zajrzeć, jeśli masz już za sobą sprawę nocnej zmiany.";
			case MOUNTAIN_THREATS:
				return "Wojtek w ratuszu w centrum Zakopanego słyszał o pogróżkach wobec kupców. Porozmawiaj z nim, zanim znowu ruszysz w las.";
			case MOUNTAIN_MEETING:
			case MOUNTAIN_BATTLE:
				return "Brzezdom widział obcych we wschodnim Kościelisku. Zna polanę jak własną kieszeń, powinien wiedzieć, gdzie ich znaleźć.";
			case MOUNTAIN_CARGO:
				return "Stasek zna się na broni i kupcach, którzy ją tu wożą. Pokaż mu, co znaleźliście przy woźnicy.";
			case MOUNTAIN_RETURN:
				return "Wojtek czeka w ratuszu w centrum Zakopanego na wieści z lasu. Nie zapomnij też o Drilenunie i Barbarusie, martwili się razem z nim.";
			case KEEP_TRAIL:
			case KEEP_WITNESSES:
			case KEEP_RETURN:
				return "Julius pilnuje głównego wejścia do Ados. Słyszałem, że niepokoją go wieści ze starej warowni w górach. Może przyda mu się twoja pomoc.";
			case KEEP_INSCRIPTIONS:
				return "Wikipedian w bibliotece Ados zna dawne dzieje. Pokaż mu znaki z kamiennych płyt, może zdoła je odczytać.";
			case KEEP_GUARDIAN:
				return "Zogfang przebywa w południowo-zachodniej części pierwszego piętra warowni pod górami Ados. On wie, gdzie szukać kamiennego strażnika.";
			case OATH_WATCH:
			case OATH_GRAVE:
			case OATH_BURIAL:
				return "Wagabundus wędruje traktem na południe od Krakowa. Niepokoją go dawne mogiły i wartownicy, którzy nie schodzą z posterunku.";
			case OATH_NAMES:
			case OATH_TRUTH:
				return "Amileusz, archeolog w Krakowie, pomoże ci rozpoznać dawne wojskowe znaki i zapiski.";
			case OATH_BANNER:
				return "Wszebor czeka na polach na północny zachód od Krakowa. Chce odzyskać chorągiew swoich towarzyszy.";
			case WARSAW_GATE:
			case WARSAW_WITNESSES:
			case WARSAW_TRUCE:
				return "Rycerz Mark stoi przy obozie pod zamkiem we wschodniej Warszawie. Próbuje pomóc ludziom, którzy zostali w warowni. Porozmawiaj z nim, zanim wmieszasz się w ten spór.";
			case WARSAW_PAY:
			case WARSAW_STEWARD:
				return "Zajrzyj do księcia przy obozie pod warszawskim zamkiem. Nie wszystkie wieści z warowni do niego dochodzą. Warto, żeby usłyszał je także od ciebie.";
			case WARSAW_FIRE:
				return "Dobrosława opatruje rannych w lazarecie warszawskiej warowni. Słyszała coś niepokojącego. Nie zostawiaj jej samej z tą sprawą.";
			case COAST_WRECK:
			case COAST_RETURN:
				return "Staszek na przystani promu w północno-zachodnim Gdańsku martwi się o statki, które nie wracają do portu. Zajrzyj do niego, jeśli masz już za sobą sprawę warszawskiej warowni.";
			case COAST_LIGHTS:
			case COAST_LAMPS:
				return "Rozbitek Lubor odpoczywa na plaży na wschód od przystani promu w Gdańsku. Zna ten brzeg i może wyjaśnić, dlaczego statki trafiają na mieliznę.";
			case COAST_CARGO:
			case COAST_WRECKERS:
				return "Malteis pilnuje Gdańska. Znajdziesz go w siedzibie rycerzy, w północno-wschodniej części miasta. Zanieś mu wieści o rabusiach wybrzeża.";
			case SALT_SHIFT:
				return "Zbigniew w ratuszu Wieliczki szuka pomocy dla górników. Po wstrząsie dwaj nie wrócili ze zmiany. Zajrzyj do niego, jeśli masz już za sobą sprawę gdańskiego wybrzeża.";
			case SALT_RESCUE:
			case SALT_SAMPLE:
			case SALT_CREATURE:
			case SALT_RETURN:
				return "Radost pilnuje szybu w centralnej części kopalni Wieliczki. Czeka na wieści o górnikach i wodzie, która zalewa chodniki.";
			case SALT_SOURCE:
				return "Agrypin w swoim domu w Wieliczce bada próbkę wody z kopalni. Może już wie, co otworzył pod ziemią ostatni wstrząs.";
			case EMBER_CAMP:
			case EMBER_TRANSPORT:
			case EMBER_GUIDE:
				return "Racirad przy południowym wejściu na Smocze Ziemie szuka przyczyny ataku na obóz. Zanieś mu wieści, zanim jego ludzie ruszą z pochodniami na gniazda.";
			case EMBER_SCENT:
				return "Wizariusz mieszka na szczycie wieży w północno-zachodniej części Smoczych Ziem. Pokaż mu płótno z porzuconego transportu.";
			case EMBER_EGGS:
			case EMBER_RETURN:
				return "Ostrobor czeka przy obozowisku handlarzy w północno-wschodniej części Smoczych Ziem N. Zna drogę do skradzionych jaj.";
			case BELL_RETURNED:
				return "Festris w Kikareukin martwi się o wyprawę na wyspy. Znajdziesz ją na czwartym piętrze. Zapytaj o zaginioną wyprawę.";
			case BELL_WOUNDED:
			case BELL_VOICE:
			case BELL_GUARD:
			case BELL_ESCAPE:
				return "Wyprawa czeka na pomoc na północno-wschodniej wyspie Kikareukin. Witosław i Borzęta powiedzą ci, czego potrzeba rannym.";
			default:
				throw new IllegalStateException("Unknown story step");
			}
		}
		return "Gościrad w ratuszu wie, komu jeszcze potrzeba pomocy. Zajrzyj do niego albo popytaj sąsiadów. W Zakopanem zawsze jest coś do zrobienia.";
	}

	@Override
	public void configureZone(final StendhalRPZone zone,
			final Map<String, String> attributes) {
		buildNPC(zone);
	}

	private void addNewPlayerAdvice(final SpeakerNPC npc) {
		final String help = "Chcesz wiedzieć, gdzie kupić #broń, #jedzenie albo #mikstury? Mogę też powiedzieć coś o tym, "
				+ "jak działa #leczenie, gdzie jest #bank, jakie są tu #potwory i skąd brać #pieniądze. Nie wiesz, za co się zabrać? Zapytaj #'co dalej'. "
				+ "Zajrzyj też do dziennika, do zadań głównych.";
		final ChatAction next = (player, sentence, speaker) -> {
			player.setQuest("meet_mariuszek", "done");
			speaker.say(nextStoryAdvice(player));
		};
		for (final ConversationStates state : new ConversationStates[] {
				ConversationStates.ATTENDING,
				ConversationStates.INFORMATION_1 }) {
			npc.add(state, ConversationPhrases.HELP_MESSAGES, null,
					ConversationStates.ATTENDING, help,
					new SetQuestAction("meet_mariuszek", "done"));
			npc.add(state,
					Arrays.asList("co dalej", "dalej", "zadanie", "zadania",
							"task", "quest"),
					null, ConversationStates.ATTENDING, null, next);
			npc.add(state,
					Arrays.asList(
							"broń", "bron", "uzbrojenie", "sklep", "sklepy"),
					null, ConversationStates.ATTENDING,
					"Po broń zajrzyj do Bogusia, do szpitala na południu Zakopanego. Na razie masz czym walczyć, "
							+ "więc zostaw sobie trochę monet na #jedzenie. Do Staska też możesz zajrzeć. Ma sklep obok mostu w centrum Zakopanego, "
							+ "ale zanim coś ci sprzeda, trzeba mu pomóc. Zapytaj #'co dalej', to powiem ci, od czego zacząć.",
					null);
			npc.add(state,
					Arrays.asList(
							"jedzenie", "żywność", "zywnosc", "głód", "glod"),
					null, ConversationStates.ATTENDING,
					"Po jedzenie idź do Jagny. Jej karczma jest na północnym wschodzie Zakopanego. Jedzenie pomaga odzyskać zdrowie, ale trzeba trochę poczekać. "
							+ "Weź coś na drogę. A jeśli nie masz monet, zapytaj mnie o #leczenie.",
					null);
			npc.add(state,
					Arrays.asList("leczenie", "ulecz", "rany", "zdrowie"), null,
					ConversationStates.ATTENDING,
					"Gaździna Jadźka leczy za darmo. Znajdziesz ją w szpitalu na południu Zakopanego. Przywitaj się i powiedz #'ulecz'. "
							+ "Po walce warto do niej zajrzeć, zanim znów ruszysz w drogę.",
					null);
			npc.add(state,
					Arrays.asList("mikstury", "mikstura", "eliksir", "eliksiry",
							"potion"),
					null, ConversationStates.ATTENDING,
					"Eliksiry sprzedaje Carmen w Semos. Przejście do tego miasta jest na północnym wschodzie "
							+ "centrum Zakopanego. Korzystaj też z #jedzenia i #leczenia u Jadźki.",
					null);
			npc.add(state, Arrays.asList("bank", "depozyt", "schowek"), null,
					ConversationStates.ATTENDING,
					"Bank jest na południu miasta. Na parterze znajdziesz schowki. Zostaw tam rzeczy, których nie chcesz nosić ze sobą. "
							+ "Nie musisz iść na piętro, żeby z nich korzystać.",
					null);
			npc.add(state,
					Arrays.asList("potwory", "potwór", "potwor", "stworzenia"),
					null, ConversationStates.ATTENDING,
					"O potworach pogadaj z Sabałą. Przywitaj się, powiedz #'potwór', a potem podaj nazwę tego, o którego chcesz zapytać. "
							+ "A jeśli dopiero zaczynasz, zajrzyj najpierw do Pietrka i Adasia. Zapytaj mnie #'co dalej', to podpowiem.",
					null);
			npc.add(state,
					Arrays.asList("pieniądze", "pieniadze", "zarobek", "monety",
							"sprzedaż"),
					null, ConversationStates.ATTENDING,
					"Boguś w szpitalu kupuje skóry i futra. Zajrzyj do niego i wybierz Sprzedaj, "
							+ "to zobaczysz, co możesz mu oddać. Nie bierze wszystkiego. Trochę wyposażenia dostaniesz za pierwsze zadania, "
							+ "więc nie wydawaj od razu wszystkich monet.",
					null);
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
				addGreeting(null, new SayTextAction("Witaj [name]!" + greet));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
								new GreetingMatchesNameCondition(getName()),
								new QuestNotStartedCondition("meet_mariuszek")),
						ConversationStates.INFORMATION_1,
						"Witaj! Pierwszy raz w Zakopanem? Mogę ci jakoś #pomóc?",
						new SetQuestAction("meet_mariuszek", "start"));

				add(ConversationStates.IDLE,
						ConversationPhrases.GREETING_MESSAGES,
						new AndCondition(
								new GreetingMatchesNameCondition(getName()),
								new QuestInStateCondition("meet_mariuszek",
										"start")),
						ConversationStates.INFORMATION_1,
						"O, znowu jesteś! Chcesz, żebym podpowiedział ci, od czego zacząć?",
						null);

				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.YES_MESSAGES,
						new QuestActiveCondition("meet_mariuszek"),
						ConversationStates.ATTENDING, null,
						(player, sentence, speaker) -> {
							player.setQuest("meet_mariuszek", "done");
							speaker.say(nextStoryAdvice(player));
						});

				add(ConversationStates.INFORMATION_1,
						ConversationPhrases.NO_MESSAGES,
						new QuestActiveCondition("meet_mariuszek"),
						ConversationStates.ATTENDING,
						"W porządku. Gdyby coś, znajdziesz mnie tutaj. A jeśli szukasz zajęcia, #burmistrz zawsze potrzebuje pomocy."
								+ " Zapytaj o niego, to powiem ci, gdzie go szukać.",
						new NPCEmoteAction("spogląda na świerk.", false));

				add(ConversationStates.ATTENDING, Arrays.asList("radę", "rada"),
						null, ConversationStates.ATTENDING,
						"Na początek nie rzucaj się na silne potwory. Zajrzyj do Pietrka i Adasia. Zapytaj mnie #'co dalej', jeśli nie wiesz, za co się zabrać.",
						null);

				add(ConversationStates.ATTENDING, "burmistrz", null,
						ConversationStates.ATTENDING,
						"Naszym burmistrzem jest #'gazda Wojtek'.", null);

				add(ConversationStates.ATTENDING,
						Arrays.asList("gazda", "wojtek"), null,
						ConversationStates.ATTENDING,
						"Idź na północ. Po prawej stronie mostu stoi duży budynek. "
								+ "To ratusz. Tam znajdziesz Wojtka i jego pomocnika, #Gościrada.",
						null);

				add(ConversationStates.ATTENDING, "gościrad", null,
						ConversationStates.ATTENDING,
						"Gościrad wie, kto w okolicy szuka pomocy. Znajdziesz go w ratuszu.",
						null);

				addJob("Lubię oglądać i wąchać *snif* *snif* aachhhh... Och, wybacz Ty tu jeszcze stoisz.");
				addNewPlayerAdvice(this);
				addGoodbye(
						"Czeeść! Życzę Tobie miłego dzionka! *snif* *snif* aachhhh...");
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
