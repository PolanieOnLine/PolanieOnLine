/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import games.stendhal.server.maps.quests.trail.TrailQuestLine.Step;

/**
 * Brief letters for newly offered quests; ongoing objectives stay in the
 * journal.
 */
public final class TrailQuestInvitations {
	private TrailQuestInvitations() {
	}

	public static String forStep(Step step) {
		switch (step) {
		case DELIVERY:
			return "Wieści od Staska: Znalazłem w skrzyni z opałem dziwny kwit z pieczęcią ratusza. Wstąp do mojego sklepu w Zakopanem C i zapytaj o dostawę.";
		case GUEST:
			return "Wieści od Gościrada: Ten woźnica mógł zatrzymać się u Jagny. Zajrzyj do ratusza w centrum Zakopanego, trzeba jeszcze coś sprawdzić.";
		case SEAL:
			return "Wieści od Brzezdoma: Ślady prowadzą do składu za drzewami. Czeka tam strażnik. Znajdź mnie na polanie we wschodnim Kościelisku.";
		case DEBT:
			return "Wieści od Gazdy Wojtka: Chcę ustalić, komu zabrano te zapasy. Wstąp z papierami do ratusza w Zakopanem C, nim poślę ludzi po towar.";
		case SEMOS_LETTER:
			return "Wieści od Drilenuna: Wojtek wspominał o twojej pomocy. Pokaż mi list znaleziony w składzie. Czekam w ratuszu w Semos.";
		case SEMOS_SIGN:
			return "Wieści od Dagoberta: Znak z listu pojawia się też w moich księgach. Zajrzyj do banku Semos i zapytaj o trzy kółka.";
		case SEMOS_PICKUP:
			return "Wieści od Margaret: Ktoś ma odebrać skrzynię spod gospody. Przyjdź do mnie w Semos, zanim się tu zjawi. Może dowiemy się, dla kogo pracuje.";
		case RADAN_PASSAGE:
			return "Wieści od Drilenuna: Mam trop prowadzący do kryjówki Radana pod Semos. Zajrzyj do ratusza, zanim jego ludzie wywiozą resztę zapasów.";
		case RADAN_RESCUE:
		case RADAN_TRANSPORT:
		case RADAN_FINALE:
			// These checkpoints start automatically during the accepted
			// expedition.
			return null;
		case MINE_LEDGER:
			return "Wieści od Drilenuna: Przejrzałem księgę Radana. Jedna z dostaw miała trafić do kopalni, ale coś mi tu nie pasuje. Zajrzyj do ratusza w Semos.";
		case MINE_TRACKS:
			return "Wieści od Barbarusa: Nikt u mnie nie pracuje po zmroku, a jednak w tunelach zostają ślady. Zajrzyj do mnie w kopalni Semos, pokażę ci gdzie.";
		case MINE_VOICES:
			return "Wieści od Barbarusa: Z głębi kopalni znowu dochodziły głosy. To nie brzmiało jak rozmowa górników. Wróć do mnie, trzeba to sprawdzić.";
		case MINE_DISPATCH:
			return "Wieści od Phalka: Widziałem kogoś z pakietem rozkazów przy bocznym tunelu. Znajdziesz mnie przy zablokowanym przejściu w kopalni Semos.";
		case MOUNTAIN_MINERS:
			return "Wieści od Barbarusa: Borzym i Oskar nie wrócili z bocznego tunelu. Martwię się o nich. Zejdź do mnie do kopalni Semos, potrzebuję pomocy.";
		case MOUNTAIN_THREATS:
			return "Wieści od Gazdy Wojtka: Stasek i Jagna dostają pogróżki. Zdaje się, że wspólnicy Radana nadal tu są. Zajrzyj do ratusza w Zakopanem C.";
		case MOUNTAIN_MEETING:
			return "Wieści od Brzezdoma: Obcy przywlekli na polanę woźnicę i skrępowali mu ręce. Przyjdź do mnie we wschodnim Kościelisku. Sam go nie uwolnię.";
		case MOUNTAIN_CARGO:
			return "Wieści od Staska: Podobno w skrzyni przy woźnicy była broń. Mogę pomóc ustalić, skąd ją wzięli. Wstąp do mojego sklepu w Zakopanem C.";
		case MOUNTAIN_BATTLE:
			return "Wieści od Brzezdoma: Ludzie Radana zbierają się na północy polany. Lepiej zatrzymać ich przed wymarszem. Czekam we wschodnim Kościelisku.";
		case MOUNTAIN_RETURN:
			return "Wieści od Gazdy Wojtka: Brzezdom przysłał słowo, że walka skończona. Chcę usłyszeć resztę od ciebie. Zajrzyj do ratusza w Zakopanem C.";
		case KEEP_TRAIL:
			return "Wieści od Juliusa: Widziano orków schodzących od starej warowni. Kupcy boją się napadu. Odwiedź mnie przy głównym wejściu do Ados.";
		case KEEP_WITNESSES:
			return "Wieści od Juliusa: Te ślady nie wyglądają na przygotowania do najazdu. Znam kogoś, kto może wiedzieć więcej. Wróć do mnie pod bramę Ados.";
		case KEEP_INSCRIPTIONS:
			return "Wieści od Wikipediana: Stare znaki z warowni mogą wyjaśnić zachowanie strażnika. Przynieś ich odrys do biblioteki w Ados, przyjrzymy się im razem.";
		case KEEP_GUARDIAN:
			return "Wieści od Zogfanga: Kamienny strażnik znów ruszył na naszych ludzi. Trzeba go zatrzymać. Czekam w południowo-zachodniej części warowni pod Ados.";
		case KEEP_RETURN:
			return "Wieści od Juliusa: Słyszałem, że strażnik został pokonany. Muszę jeszcze wiedzieć, czy droga jest bezpieczna. Wstąp do mnie pod bramę Ados.";
		case OATH_WATCH:
			return "Wieści od Wagabundusa: Na starym trakcie stoją wojownicy, których dawno pochowano. Przyjdź na południe od Krakowa. Nie wiem, kogo wypatrują.";
		case OATH_NAMES:
			return "Wieści od Amileusza: Mogę rozpoznać znak z uzbrojenia tych wartowników. Odwiedź mnie w Krakowie, może uda się przywrócić im imiona.";
		case OATH_GRAVE:
			return "Wieści od Wagabundusa: Przypomniałem sobie samotny grób przy trakcie. Może spoczywa tam ich dowódca. Wróć do mnie na południe od Krakowa.";
		case OATH_TRUTH:
			return "Wieści od Amileusza: Z tą opowieścią o zdradzie coś się nie zgadza. Przyjdź do mnie w Krakowie, zanim uznamy ją za prawdę.";
		case OATH_BANNER:
			return "Wieści od Wszebora: Nie zostawię dowódcy bez chorągwi jego oddziału. Wróć z rozkazem na dawny trakt pod Krakowem. Muszę ci coś powiedzieć.";
		case OATH_BURIAL:
			return "Wieści od Wagabundusa: Znamy już prawdę i mamy chorągiew. Pozostało dotrzymać słowa poległym. Czekam przy trakcie na południe od Krakowa.";
		case WARSAW_GATE:
			return "Wieści od Marka: Książę szykuje szturm, a w warowni zostali ranni. Potrzebuję twojej pomocy. Przyjdź do obozu pod zamkiem we wschodniej Warszawie.";
		case WARSAW_PAY:
			return "Wieści od księcia: Załoga twierdzi, że nie dostała żołdu, choć wysłałem pieniądze. Przyjdź do obozu pod warszawskim zamkiem. Chcę poznać prawdę.";
		case WARSAW_WITNESSES:
			return "Wieści od Marka: Księgi to jedno, ale musimy wysłuchać ludzi w środku. Wróć do obozu pod warszawskim zamkiem, zanim postawimy Radomirowi zarzuty.";
		case WARSAW_FIRE:
			return "Wieści od Dobrosławy: Do magazynu znoszą beczki z olejem. Boję się, że Radomir chce podpalić warownię. Przyjdź do lazaretu, nim będzie za późno.";
		case WARSAW_STEWARD:
			return "Wieści od księcia: Radomir musi odpowiedzieć za to, co przygotował. Wróć do obozu pod warszawskim zamkiem i powiedz mi wszystko.";
		case WARSAW_TRUCE:
			return "Wieści od Marka: Radomir już nam nie zagrozi, ale na murach nadal stoją żołnierze. Przyjdź do obozu. Spróbujmy wyprowadzić rannych bez kolejnej walki.";
		case COAST_WRECK:
			return "Wieści od Staszka: Mewa nie weszła do portu. Morze wyrzuca na brzeg jej ładunek. Zajrzyj do mnie przy przystani w Gdańsku, może zdołamy pomóc rozbitkom.";
		case COAST_LIGHTS:
			return "Wieści od Lubora: To światło nie powinno było świecić od tamtej strony. Wróć do mnie na gdańską plażę. Ktoś celowo skierował nas na mieliznę.";
		case COAST_CARGO:
			return "Wieści od Malteisa: Ludzie widzieli mokre skrzynie noszone do składu. Mogą pochodzić z Mewy. Zajrzyj do mojej kwatery w Gdańsku.";
		case COAST_WRECKERS:
			return "Wieści od Malteisa: Herszt tych rabusiów wciąż ukrywa się w składzie. Trzeba go zatrzymać przed kolejnym napadem. Wróć do mojej kwatery w Gdańsku.";
		case COAST_LAMPS:
			return "Wieści od Staszka: Jaskółka zbliża się do Gdańska. Musimy przywrócić bezpieczny sygnał na brzegu. Przyjdź do mnie przy przystani.";
		case COAST_RETURN:
			return "Wieści od Staszka: Lampy są gotowe, ale ktoś jeszcze kręci się przy brzegu. Zajrzyj do mnie przy gdańskiej przystani. Nie możemy teraz zostawić Jaskółki.";
		case SALT_SHIFT:
			return "Wieści od Zbigniewa: Dwóch górników nie wróciło po wstrząsie. Radost potrzebuje pomocy. Wstąp do mnie do ratusza w Wieliczce.";
		case SALT_RESCUE:
			return "Wieści od Radosta: Wiemy już, gdzie szukać Bogdana i Wojmira. Trzeba zabezpieczyć przejście. Zejdź do mnie pod szyb w Wieliczce.";
		case SALT_SAMPLE:
			return "Wieści od Radosta: Ludzie są bezpieczni, ale ta woda wciąż napływa. Musimy dowiedzieć się, co niesie ze sobą. Wróć do mnie pod szyb w Wieliczce.";
		case SALT_SOURCE:
			return "Wieści od Agrypina: W próbce solanki dzieje się coś dziwnego. Podejrzewam stary zbiornik. Zajrzyj do mojego domu w Wieliczce, chcę ci coś pokazać.";
		case SALT_CREATURE:
			return "Wieści od Radosta: Dopóki to stworzenie pilnuje zbiornika, nie zamkniemy dopływu wody. Zejdź pod szyb w Wieliczce, musimy ustalić, jak je zatrzymać.";
		case SALT_RETURN:
			return "Wieści od Radosta: Zostało osuszyć chodniki i sprawdzić drogę do szybu. Wróć do mnie w kopalni Wieliczki. Nie puszczę tam ludzi bez pewności, że jest bezpiecznie.";
		case EMBER_CAMP:
			return "Wieści od Racirada: Smoki spaliły obóz, lecz nie wszystko wygląda na zwykły napad. Przyjdź do mnie przy południowym wejściu na Smocze Ziemie.";
		case EMBER_TRANSPORT:
			return "Wieści od Racirada: Ta skorupa nie znalazła się w obozie przypadkiem. Ktoś przywiózł tu smocze jaja. Wróć do mnie przy południowym wejściu na Smocze Ziemie.";
		case EMBER_SCENT:
			return "Wieści od Wizariusza: Podobno płótno z transportu posmarowano dziwną mazią. Odwiedź mnie na szczycie wieży na Smoczych Ziemiach, może rozpoznam ten zapach.";
		case EMBER_GUIDE:
			return "Wieści od Racirada: Handlarze przetrzymują przewodnika. Może wiedzieć, skąd zabrali jaja. Wróć do mojego obozu przy południowym wejściu na Smocze Ziemie.";
		case EMBER_EGGS:
			return "Wieści od Ostrobora: Herszt trzyma resztę jaj w głębi obozu. Przyjdź do mnie do obozowiska handlarzy na Smoczych Ziemiach, zanim znów je gdzieś wywiozą.";
		case EMBER_RETURN:
			return "Wieści od Ostrobora: Jaja wciąż pachną przynętą, a przy gnieździe leżą pułapki. Wróć do mnie w obozowisku na Smoczych Ziemiach. Jeszcze nie możemy ich oddać.";
		case BELL_RETURNED:
			return "Wieści od Festris: Z wyprawy na wyspy wrócił tylko jeden chłopak. Twierdzi, że pozostali nie chcą odejść od starego dzwonu. Odwiedź mnie w Kikareukin, na czwartym piętrze.";
		case BELL_WOUNDED:
			return "Wieści od Witosława: Moi ludzie ledwo stoją, ale nie zostawię brata we mgle. Przyjdź do obozu na północno-wschodniej wyspie Kikareukin. Może ciebie wysłuchają.";
		case BELL_VOICE:
			return "Wieści od Borzęty: Witosław słyszy brata. Ja słyszę kogoś zupełnie innego. Przyjdź do naszego obozu na wyspach Kikareukin. Coś tu jest bardzo nie w porządku.";
		case BELL_GUARD:
			return "Wieści od Witosława: Wiem już, czyj głos słyszałem. Nie pójdę za nim drugi raz. Wróć do obozu na wyspach Kikareukin. Musimy uciszyć dzwon.";
		case BELL_ESCAPE:
			return "Wieści od Witosława: Ranni są gotowi do drogi. Czekamy na ciebie w obozie na wyspach Kikareukin. Bez osłony nie przejdziemy przez kładkę.";
		default:
			throw new IllegalArgumentException(
					"Missing invitation for " + step);
		}
	}
}
