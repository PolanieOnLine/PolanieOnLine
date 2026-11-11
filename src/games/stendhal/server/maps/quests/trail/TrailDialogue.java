/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Spoken reminders are separate from the journal's directions and requirements.
 */
final class TrailDialogue {
	private TrailDialogue() {
	}

	static String reminder(final Player player, final Step step,
			final String npc) {
		if (player.isQuestCompleted(step.slot)) {
			return "Dobrze, że udało się to wyjaśnić. Dziękuję za pomoc.";
		}
		if (!player.isQuestCompleted(step.previousSlot())) {
			return beforePreviousStep(step, npc);
		}
		if (!player.hasQuest(step.slot)
				&& player.getLevel() < step.minimumLevel) {
			if (!"Lieutenant Drilenun".equals(npc)) {
				return "Drilenun w koszarach w Ados zbiera wieści o dostawach do kopalni. Porozmawiaj z nim, zanim zaczniesz zaglądać w boczne tunele.";
			}
			return "Niepokoi mnie ten wpis o kopalni, ale nie wyślę cię teraz pod ziemię. "
					+ "Tam nawet górnicy nie chodzą sami. Nabierz jeszcze doświadczenia i wróć do mnie.";
		}
		final String stage = player.getQuest(step.slot, 0);
		switch (step) {
		case DELIVERY:
			if ("inspect".equals(stage))
				return "Stasek".equals(npc)
						? "Kwit został w skrzyni u mnie w sklepie. Obejrzyj go, może zauważysz coś, co mi umknęło."
						: "Stasek zostawił kwit w skrzyni w swoim sklepie. Przynieś go, muszę obejrzeć pieczęć.";
			if ("Gościrad".equals(npc))
				return "Moja księga leży na małym stoliku obok mnie. Jeśli to nasze zamówienie, musi być tam zapisane.";
			return "Gościrad powinien poznać własną pieczęć. Znajdziesz go w ratuszu w centrum Zakopanego.";
		case GUEST:
			if ("jagna".equals(stage))
				return "Jagna przyjmuje podróżnych w karczmie na północnym wschodzie Zakopanego. Może pamięta tego woźnicę.";
			if ("brzezdom".equals(stage))
				return "Brzezdom chodzi po polanie we wschodnim Kościelisku. Jeśli wóz tamtędy przejeżdżał, pewnie go widział.";
			if ("found".equals(stage))
				return "Brzezdom".equals(npc)
						? "Udało ci się obejrzeć skrzynkę? Opowiedz mi o tym #woźnicy, może połączymy te ślady."
						: "Ślad ze skrzynki trzeba pokazać Brzezdomowi. Zapytaj go o #woźnicę.";
			if ("Brzezdom".equals(npc))
				return "Szukaj samotnej skrzynki na północny wschód stąd, na południe od obozowiska obcych ludzi. Ciekaw jestem, czy poznasz na niej ten znak.";
			return "Brzezdom widział więcej ode mnie. Warto sprawdzić ślady przy polanie, o której mówił.";
		case SEAL:
			if ("guard".equals(stage))
				return "Ten człowiek wciąż pilnuje składu na północy polany. Uważaj na niego, nie wygląda na zwykłego woźnicę.";
			if ("documents".equals(stage))
				return "Przy workach leżały jakieś papiery. Może z nich dowiemy się, czyje zapasy tam trzymają.";
			return "Udało ci się zajrzeć do składu? Pokaż, co było w tych papierach.";
		case DEBT:
			if ("Gazda Wojtek".equals(npc))
				return "Stasek powinien poznać swoje worki, a Jagna zamówioną mąkę. Chcę mieć pewność, zanim poślę ludzi po zapasy.";
			return "Powiedz Wojtkowi, co tu usłyszysz. On zajmie się sprowadzeniem zapasów do miasta.";
		case SEMOS_LETTER:
			if ("Dagobert".equals(npc))
				return "Zaliczka jest zapisana w księdze przy mojej ladzie. Nie ruszę tych pieniędzy, dopóki nie wyjaśnicie, kto za tym stoi.";
			if ("checked".equals(stage))
				return "Masz wieści z banku? Opowiedz, co udało ci się znaleźć.";
			return "Dagobert zapisuje każdą wpłatę. Jeśli ktoś zapłacił za ten transport, ślad został w jego księdze.";
		case SEMOS_SIGN:
			if ("Ceryl".equals(npc))
				return "library".equals(stage)
						? "Zacznijmy od znaku. Pokaż mi te #'trzy kółka', może przypomnę sobie, gdzie już je widziałem."
						: "Księga przewoźników leży na stole w południowo-zachodnim rogu biblioteki. Poszukaj znaku i dopisku przy nim. Może wyjaśni te litery.";
			return "Ceryl w bibliotece zna te stare kupieckie znaki lepiej ode mnie. Pokaż mu trzy kółka i litery z kwitu.";
		case SEMOS_PICKUP:
			if ("Lieutenant Drilenun".equals(npc))
				return "Margaret obiecała mieć oko na skrzynie przy gospodzie. Porozmawiaj z nią, zanim podejdziesz do tragarza.";
			if ("wood".equals(stage))
				return "Przynieś dwa #polana, a przygotuję skrzynię. Pod płótnem nikt nie odróżni ich od prawdziwego ładunku.";
			if ("return".equals(stage))
				return "Skoro na kwicie jest nazwisko, porucznik będzie wiedział, kogo szukać. Zanieś mu te wieści do koszar w Ados.";
			return "Skrzynia czeka na zewnątrz, po prawej stronie wejścia. Olek też tam jest. Pamiętaj, oczekuje umówionego hasła.";
		case RADAN_PASSAGE:
			if ("brace".equals(stage))
				return "Bez podparcia ta belka nie wytrzyma. Weź dwa polana i wzmocnij wejście, zanim spróbujesz przejść.";
			return "Wejście jest w bocznym chodniku, na zachód od schodów do podziemi Semos. Uważaj na belki. Radan nie dbał o to, żeby było tam bezpiecznie.";
		case RADAN_RESCUE:
			return "Jeśli trzymają tam kogoś wbrew jego woli, nie możemy zostawić go ludziom Radana. Najpierw trzeba go uwolnić.";
		case RADAN_TRANSPORT:
			return "Bartek zna ten wyciąg. Posłuchaj go, zanim ruszysz wózek. Nie chcę, żeby ktoś zginął przez uciekający ładunek.";
		case RADAN_FINALE:
			if ("return".equals(stage))
				return "Masz wieści o Radanie? Pokaż, co udało ci się wynieść z kryjówki.";
			return "Zatrzymany transport to jeszcze nie koniec. Potrzebujemy księgi Radana, inaczej nie ustalimy, komu sprzedawał zapasy.";
		case MINE_LEDGER:
			if ("Barbarus".equals(npc))
				return "Mój rejestr leży obok, po zachodniej stronie. Każdy wywieziony wózek jest zapisany. Zobaczysz, że po zmroku nic stąd nie wyjeżdżało.";
			return "Barbarus zna każdego, kto pracuje w kopalni. Pokaż mu wpis Radana. On pierwszy powinien wiedzieć o nocnej zmianie.";
		case MINE_TRACKS:
			if ("report".equals(stage))
				return "I co tam było? Pokaż mi te ślady, może poznam, kto tędy chodził.";
			return "Skrzynka po racjach leży przy zakręcie na północny wschód stąd. Dalej na wschód są odciski butów. Obejrzyj jedno i drugie, zanim ktoś je zatrze.";
		case MINE_VOICES:
			if ("Zynn Iwuhos".equals(npc))
				return "Okrzyki w kopalni? Musiałbym usłyszeć dokładne słowa. Sam odgłos kroków może nas zwieść.";
			if ("history".equals(stage))
				return "Zynn w bibliotece zna dawne dzieje Semos. Powtórz mu te słowa spod skały. Może zrozumie więcej niż ja.";
			return "Przy starej obudowie szybu, niedaleko Phalka, słychać je najwyraźniej. Posłuchaj przez belki, ale nie przechodź za blokadę.";
		case MINE_DISPATCH:
			if ("Lieutenant Drilenun".equals(npc))
				return "Phalk strzeże przejścia w kopalni. Pokaż mu to, co znajdziesz. Powinien rozpoznać, kto wydaje tam rozkazy.";
			if ("return".equals(stage))
				return "Zanieś rozkaz Drilenunowi do koszar w Ados. Ja zostanę przy przejściu. Ktoś musi ich pilnować.";
			if ("orders".equals(stage))
				return "Sprawdź pakiet przy zakręcie. Może łącznik zostawił tam rozkazy.";
			if ("identified".equals(stage))
				return "Masz już ten pergamin? Daj mi spojrzeć na pieczęć.";
			return "Łącznik przechodzi przy zakręcie na południe stąd. Pilnuj pakietu, jeśli uda ci się go pokonać. Bez rozkazu nie dowiemy się, co szykują.";
		default:
			throw new IllegalStateException("Unknown story step");
		}
	}

	private static String beforePreviousStep(final Step step,
			final String npc) {
		switch (step) {
		case DELIVERY:
			return "Stasek".equals(npc)
					? "Najpierw pomóż mi zgromadzić opał. Bez niego będziemy tu marznąć całą zimę."
					: "Zajrzyj najpierw do Staska. Czeka na opał, a bez niego nie przygotuje sklepu na zimę.";
		case GUEST:
			return "Najpierw sprawdźmy to zamówienie. Nie chcę oskarżać woźnicy, jeśli to zwykła pomyłka.";
		case SEAL:
			return "Zanim pójdziemy dalej, obejrzyjmy ślady wozu. Nie wiemy jeszcze, czy to ten sam człowiek.";
		case DEBT:
			return "Same podejrzenia nie wystarczą. Wojtek potrzebuje papierów ze składu, żeby wiedzieć, co tam wywozili.";
		case SEMOS_LETTER:
			return "Wojtek powinien najpierw wyjaśnić, co zginęło w Zakopanem. Bez tego trudno będzie mi pomóc.";
		case SEMOS_SIGN:
			return "Sprawdźmy najpierw wpłatę. Jeśli to ta sama dostawa, znak powinien się powtórzyć.";
		case SEMOS_PICKUP:
			return "Ten tragarz nie wyda niczego obcemu. Najpierw trzeba ustalić, jak umawiają odbiór.";
		case RADAN_PASSAGE:
			return "Margaret pilnuje skrzyń przy gospodzie. Zanim zaczniemy przeszukiwać podziemia, trzeba sprawdzić, kto miał je odebrać.";
		case RADAN_RESCUE:
			return "Radan ma kryjówkę w bocznym chodniku. Najpierw trzeba znaleźć bezpieczne wejście.";
		case RADAN_TRANSPORT:
			return "Nie zostawimy Bartka związanego pod ziemią. Wózkiem zajmiemy się, gdy będzie wolny.";
		case RADAN_FINALE:
			return "Najpierw zatrzymajmy wózek. Inaczej Radan wywiezie zapasy, zanim do niego dotrzemy.";
		case MINE_LEDGER:
			return "Najpierw dokończmy sprawę Radana. Dopóki nie mamy jego księgi, o dostawach do kopalni wiemy za mało.";
		case MINE_TRACKS:
			return "Najpierw zajrzyjmy do rejestru. Chcę wiedzieć, czy rzeczywiście ktoś pracował tu po nocy.";
		case MINE_VOICES:
			return "Barbarus".equals(npc)
					? "Obejrzyj najpierw skrzynkę i ślady w tunelu. Może mają związek z tym, co słyszałem."
					: "Barbarus wspominał o śladach obcych ludzi w tunelach. Może to ma związek z tym, co słyszał. Warto najpierw je obejrzeć.";
		case MINE_DISPATCH:
			return "W kopalni niosą się różne odgłosy. Zynn powinien najpierw usłyszeć te słowa i powiedzieć, czy to rzeczywiście rozkazy.";
		default:
			throw new IllegalStateException("Unknown story step");
		}
	}
}
