/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.maps.Region;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DeathMatchCreature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

/** Persistent, personal objectives of the Zakopane delivery investigation. */
public final class TrailQuestLine {
	public static final String CHAPTER = "Niepokój na trakcie";
	public static final String MINE_CHAPTER = "Druga zmiana";
	public static final String MOUNTAIN_CHAPTER = "Ludzie Radana";
	public static final String KEEP_CHAPTER = "Stara warownia";
	public static final String OATH_CHAPTER = "Dług wobec zmarłych";
	public static final String WARSAW_CHAPTER = "Pod murami Warszawy";
	public static final String COAST_CHAPTER = "Światła na mieliźnie";
	public static final String SALT_CHAPTER = "Zalane chodniki";
	public static final String DRAGON_CHAPTER = "Popiół pod łuskami";
	public static final String DUNGEON_CHAPTER = "Pod miastem";
	public static final String SEMOS_CHAPTER = "Ślad trzech kółek";
	public static final String FOREST = "0_koscielisko_e";
	public static final String GUARD = "strażnik składu";
	public static final int STOCK_X = 62;
	public static final int STOCK_Y = 61;

	public enum Step {
		DELIVERY(
				"trail_delivery",
				"Dostawa, której nikt nie zamawiał",
				"Stasek",
				"inspect",
				1200,
				25,
				"W skrzyni Staska znalazł się kwit z pieczęcią ratusza. Trzeba sprawdzić, kto zamówił tę dostawę.",
				"Odwiedź Staska w sklepie w Zakopanem C i zapytaj o dostawę.",
				CHAPTER,
				1,
				Region.ZAKOPANE_CITY),
		GUEST(
				"trail_guest",
				"Ostatni gość Jagny",
				"Gościrad",
				"jagna",
				1800,
				40,
				"Zamówienie okazało się fałszywe. Jagna może pamiętać woźnicę, który odebrał zapasy.",
				"Zapytaj Gościrada w ratuszu Zakopanego C o woźnicę.",
				CHAPTER,
				2,
				Region.ZAKOPANE_CITY),
		SEAL(
				"trail_seal",
				"Za cudzą pieczęcią",
				"Brzezdom",
				"guard",
				3000,
				60,
				"Ślady wozu prowadzą do ukrytego składu. Pilnuje go strażnik. Warto sprawdzić leżące tam papiery.",
				"Odnajdź Brzezdoma na polanie we wschodnim Kościelisku i zapytaj o skład.",
				CHAPTER,
				3,
				Region.ZAKOPANE_CITY),
		DEBT(
				"trail_debt",
				"Dług wobec Zakopanego",
				"Gazda Wojtek",
				"witnesses;0",
				2000,
				100,
				"Wszystko wskazuje na to, że zapasy pochodzą z Zakopanego. Wojtek chce jeszcze usłyszeć, co powiedzą Stasek i Jagna.",
				"Przedstaw dowody Gazdzie Wojtkowi w ratuszu Zakopanego C. Powiedz: dowody.",
				CHAPTER,
				4,
				Region.ZAKOPANE_CITY),
		SEMOS_LETTER(
				"trail_semos_letter",
				"List z Zakopanego",
				"Lieutenant Drilenun",
				"bank",
				1800,
				40,
				"List znaleziony w składzie prowadzi do Semos. Porucznik Drilenun powinien wiedzieć, gdzie szukać odbiorcy.",
				"Idź do ratusza w Semos i pokaż list porucznikowi Drilenunowi. Powiedz: list.",
				SEMOS_CHAPTER,
				1,
				Region.SEMOS_CITY),
		SEMOS_SIGN(
				"trail_semos_sign",
				"Skąd ten znak",
				"Dagobert",
				"library",
				2200,
				50,
				"W księdze wpłat jest taki sam znak jak na zamówieniu Staska. Ceryl może znaleźć coś o nim w starych księgach.",
				"Zapytaj Dagoberta w banku Semos o trzy kółka.",
				SEMOS_CHAPTER,
				2,
				Region.SEMOS_CITY),
		SEMOS_PICKUP(
				"trail_semos_pickup",
				"Odbiór przy gospodzie",
				"Margaret",
				"wood",
				3200,
				80,
				"Odbiorca ma przyjść pod gospodę. Margaret chce podstawić mu skrzynię z polanami i sprawdzić, dla kogo pracuje.",
				"Porozmawiaj z Margaret w gospodzie w Semos. Zapytaj o odbiór.",
				SEMOS_CHAPTER,
				3,
				Region.SEMOS_CITY),
		RADAN_PASSAGE(
				"radan_passage",
				"Zamknięty chodnik",
				"Lieutenant Drilenun",
				"search",
				1200,
				0,
				"Radan ukrywa skradzione zapasy pod Semos. Drilenun wskazał boczny chodnik w podziemiach.",
				"Zapytaj porucznika Drilenuna w ratuszu Semos o chodnik.",
				DUNGEON_CHAPTER,
				1,
				Region.SEMOS_CITY),
		RADAN_RESCUE(
				"radan_rescue",
				"Nie wszyscy z własnej woli",
				"Bartek",
				"guards;0",
				1800,
				0,
				"W kryjówce jest związany robotnik. Najpierw trzeba rozprawić się z pilnującymi go ludźmi.",
				"Wejdź do bocznego chodnika pod Semos i odszukaj więźnia.",
				DUNGEON_CHAPTER,
				2,
				Region.SEMOS_CITY),
		RADAN_TRANSPORT(
				"radan_transport",
				"Ostatni transport",
				"Bartek",
				"brake",
				1800,
				0,
				"Radan przygotował wózek ze skradzionym towarem. Bartek wie, jak go zatrzymać.",
				"Porozmawiaj z uwolnionym Bartkiem o wózku.",
				DUNGEON_CHAPTER,
				3,
				Region.SEMOS_CITY),
		RADAN_FINALE(
				"radan_finale",
				"Rachunek Radana",
				"Lieutenant Drilenun",
				"guard",
				5000,
				150,
				"Transport jest zatrzymany. Pozostało pokonać zbrojnego i porozmawiać z Radanem.",
				"Odszukaj Radana w ostatnim pomieszczeniu kryjówki.",
				DUNGEON_CHAPTER,
				4,
				Region.SEMOS_CITY),
		MINE_LEDGER(
				"trail_mine_ledger",
				"Dopisek w księdze",
				"Lieutenant Drilenun",
				"barbarus",
				3500,
				60,
				"W księdze Radana jest zamówienie na zapasy dla nocnej zmiany w kopalni Semos. Drilenun chce sprawdzić, kto je odbierał.",
				"Zapytaj porucznika Drilenuna w ratuszu Semos o nocną zmianę.",
				MINE_CHAPTER,
				1,
				Region.SEMOS_MINES,
				30),
		MINE_TRACKS(
				"trail_mine_tracks",
				"Kto schodzi po zmroku",
				"Barbarus",
				"clues;0",
				4500,
				80,
				"Barbarus nie zatrudnia nikogo na noc. W tunelach zostały jednak opakowania po racjach i ślady ciężkich butów.",
				"Zapytaj Barbarusa w kopalni Semos o ślady nocnej zmiany.",
				MINE_CHAPTER,
				2,
				Region.SEMOS_MINES,
				30),
		MINE_VOICES(
				"trail_mine_voices",
				"Głosy spod skały",
				"Barbarus",
				"listen",
				5500,
				100,
				"Ślady prowadzą w głąb kopalni. Barbarus słyszał tam rozkazy. Trzeba sprawdzić, skąd dochodzą.",
				"Porozmawiaj z Barbarusem o głosach z głębi kopalni. Powiedz: głosy spod skały.",
				MINE_CHAPTER,
				3,
				Region.SEMOS_MINES,
				30),
		MINE_DISPATCH(
				"trail_mine_dispatch",
				"Rozkaz na pergaminie",
				"Phalk",
				"guard",
				10000,
				200,
				"Zynn rozpoznał dawne komendy wojskowe. Phalk może wiedzieć, kto przenosi rozkazy między tunelami.",
				"Odszukaj Phalka przy zablokowanym przejściu w kopalni Semos. Zapytaj o łącznika.",
				MINE_CHAPTER,
				4,
				Region.SEMOS_MINES,
				30),
		MOUNTAIN_MINERS(
				"trail_missing_miners",
				"Nie wrócili ze zmiany",
				"Barbarus",
				"search",
				6000,
				100,
				"Dwaj górnicy nie wrócili z bocznego tunelu. Barbarus obawia się, że spotkali ludzi zaopatrujących duergarów.",
				"Porozmawiaj z Barbarusem w kopalni Semos o zaginionych górnikach.",
				MOUNTAIN_CHAPTER,
				1,
				Region.SEMOS_MINES,
				50),
		MOUNTAIN_THREATS(
				"trail_threatened_witnesses",
				"Za długi język",
				"Gazda Wojtek",
				"witnesses;0",
				3500,
				80,
				"Staskowi grożono za rozpoznanie skradzionych zapasów. Ktoś wypytuje także o Jagnę.",
				"Zapytaj Gazdę Wojtka w ratuszu Zakopanego C o pogróżki.",
				MOUNTAIN_CHAPTER,
				2,
				Region.ZAKOPANE_CITY,
				50),
		MOUNTAIN_MEETING(
				"trail_forest_meeting",
				"Obcy na polanie",
				"Brzezdom",
				"guards;0",
				8000,
				150,
				"Brzezdom widział spotkanie duergara z człowiekiem Radana. Przetrzymują woźnicę we wschodnim Kościelisku.",
				"Zapytaj Brzezdoma we wschodnim Kościelisku o obcych na polanie.",
				MOUNTAIN_CHAPTER,
				3,
				Region.ZAKOPANE_CITY,
				50),
		MOUNTAIN_CARGO(
				"trail_hidden_weapons",
				"Pod przykryciem dostawy",
				"Stasek",
				"cargo",
				4500,
				120,
				"Woźnica miał przewieźć broń ukrytą w zwykłym ładunku. Trzeba ustalić, dla kogo ją przygotowano.",
				"Porozmawiaj ze Staskiem w sklepie w Zakopanem C o ukrytej broni.",
				MOUNTAIN_CHAPTER,
				4,
				Region.ZAKOPANE_CITY,
				50),
		MOUNTAIN_BATTLE(
				"trail_before_departure",
				"Przed wymarszem",
				"Brzezdom",
				"battle;0",
				14000,
				250,
				"Oddział zbiera się w północnej części polany w Kościelisku. Jego dowódca szykuje napad na transporty do obu miast.",
				"Zapytaj Brzezdoma we wschodnim Kościelisku o zbiórkę oddziału.",
				MOUNTAIN_CHAPTER,
				5,
				Region.ZAKOPANE_CITY,
				50),
		MOUNTAIN_RETURN(
				"trail_news_for_towns",
				"Wieści dla obu miast",
				"Gazda Wojtek",
				"reports;0",
				6000,
				300,
				"Oddział został rozbity. Wojtek i Drilenun muszą poznać wynik wyprawy, a Barbarus los swoich górników.",
				"Wróć do Gazdy Wojtka w ratuszu Zakopanego C z wieściami z lasu.",
				MOUNTAIN_CHAPTER,
				6,
				Region.ZAKOPANE_CITY,
				50),
		KEEP_TRAIL(
				"trail_keep_tracks",
				"Co wyszło z warowni",
				"Julius",
				"clues;0",
				8500,
				150,
				"Julius słyszał o orkach schodzących z gór. Trzeba sprawdzić, czy szykują napad, czy opuszczają warownię z innego powodu.",
				"Porozmawiaj z Juliusem przy głównym wejściu do Ados o starej warowni.",
				KEEP_CHAPTER,
				1,
				Region.ADOS_CITY,
				75),
		KEEP_WITNESSES(
				"trail_keep_witnesses",
				"Po drugiej stronie sporu",
				"Julius",
				"witnesses;0",
				10000,
				200,
				"Ślady wskazują na pośpieszną ucieczkę. Hagnurk i Zogfang mogą wiedzieć, co dzieje się na pierwszym piętrze warowni.",
				"Zapytaj Juliusa przy wejściu do Ados o uciekinierów.",
				KEEP_CHAPTER,
				2,
				Region.ADOS_SURROUNDS,
				75),
		KEEP_INSCRIPTIONS(
				"trail_keep_inscriptions",
				"Rozkaz wyryty w kamieniu",
				"Wikipedian",
				"inscriptions;0",
				14000,
				200,
				"W warowni ożył kamienny strażnik. Stare napisy mogą wyjaśnić, dlaczego atakuje jej mieszkańców.",
				"Pokaż Wikipedianowi w bibliotece Ados odrysowane znaki. Powiedz: stare napisy.",
				KEEP_CHAPTER,
				3,
				Region.ADOS_SURROUNDS,
				75),
		KEEP_GUARDIAN(
				"trail_keep_guardian",
				"Strażnik bez pana",
				"Zogfang",
				"fight",
				22000,
				500,
				"Dawny strażnik nie rozpoznaje już nikogo z załogi. Zogfang chce go zatrzymać, zanim znów uderzy na mieszkańców.",
				"Porozmawiaj z Zogfangiem w południowo-zachodniej części pierwszego piętra warowni o kamiennym strażniku.",
				KEEP_CHAPTER,
				4,
				Region.ADOS_SURROUNDS,
				75),
		KEEP_RETURN(
				"trail_keep_road",
				"Droga do Ados",
				"Julius",
				"merchant",
				8000,
				650,
				"Strażnik został pokonany. Trzeba sprawdzić u Hagnurka, czy mieszkańcy znów mogą podróżować, i zdać sprawę Juliusowi.",
				"Wróć do Juliusa przy wejściu do Ados. Powiedz: droga do Ados.",
				KEEP_CHAPTER,
				5,
				Region.ADOS_CITY,
				75),
		OATH_WATCH(
				"trail_oath_watch",
				"Wartownicy na trakcie",
				"Wagabundus",
				"fight",
				10000,
				150,
				"Na dawnym trakcie pod Krakowem stoją polegli wojownicy. Wagabundus chce wiedzieć, kogo wypatrują.",
				"Zapytaj Wagabundusa na południe od Krakowa o poległych wartowników.",
				OATH_CHAPTER,
				1,
				Region.KRAKOW_CITY,
				100),
		OATH_NAMES(
				"trail_oath_names",
				"Imiona pod rdzą",
				"Amileusz",
				"clues;0",
				13000,
				200,
				"Wszebor nie zna losu swojego dowódcy. Amileusz może rozpoznać znaki na uzbrojeniu oddziału.",
				"Pokaż Amileuszowi w Krakowie znak oddziału. Powiedz: znak oddziału.",
				OATH_CHAPTER,
				2,
				Region.KRAKOW_CITY,
				100),
		OATH_GRAVE(
				"trail_oath_grave",
				"Pochowany jako zdrajca",
				"Wagabundus",
				"grave",
				15000,
				250,
				"Dowódcę oddziału pochowano osobno i oskarżono o ucieczkę. Wagabundus pamięta samotny grób przy trakcie.",
				"Zapytaj Wagabundusa o samotny grób.",
				OATH_CHAPTER,
				3,
				Region.KRAKOW_CITY,
				100),
		OATH_TRUTH(
				"trail_oath_truth",
				"Ostatni rozkaz",
				"Amileusz",
				"proof",
				16000,
				300,
				"Ślady przy grobie nie pasują do opowieści o zdradzie. Trzeba odnaleźć rozkaz pozostawiony przy dawnej strażnicy.",
				"Porozmawiaj z Amileuszem o ostatnim rozkazie.",
				OATH_CHAPTER,
				4,
				Region.KRAKOW_CITY,
				100),
		OATH_BANNER(
				"trail_oath_banner",
				"Chorągiew oddziału",
				"Wszebor",
				"fight",
				26000,
				500,
				"Wszebor rozpoznał dowódcę. Do pochówku potrzebna jest chorągiew, której wciąż broni dawny towarzysz.",
				"Zanieś Wszeborowi rozkaz i zapytaj o chorągiew oddziału.",
				OATH_CHAPTER,
				5,
				Region.KRAKOW_CITY,
				100),
		OATH_BURIAL(
				"trail_oath_burial",
				"Dotrzymane słowo",
				"Wagabundus",
				"remains",
				12000,
				600,
				"Poległym obiecano wspólny pochówek. Znamy już prawdę i mamy chorągiew. Pora dotrzymać danego im słowa.",
				"Wróć do Wagabundusa. Powiedz: wspólny pochówek.",
				OATH_CHAPTER,
				6,
				Region.KRAKOW_CITY,
				100),
		WARSAW_GATE(
				"trail_warsaw_gate",
				"Zanim ruszy szturm",
				"rycerz Mark",
				"fight",
				20000,
				500,
				"Mark obawia się o ludzi pozostających w warszawskiej warowni. Zanim książę wyda rozkaz szturmu, chce poznać ich los.",
				"Porozmawiaj z rycerzem Markiem przy obozie pod zamkiem we wschodniej Warszawie. Powiedz: zanim ruszy szturm.",
				WARSAW_CHAPTER,
				1,
				Region.WARSZAWA,
				150),
		WARSAW_PAY(
				"trail_warsaw_pay",
				"Żołd, którego nie wypłacono",
				"Książę",
				"clues;0",
				25000,
				500,
				"Załoga twierdzi, że od miesięcy nie dostaje żołdu. Książę utrzymuje, że wysłał pieniądze. Odpowiedź może być w kancelarii.",
				"Zapytaj księcia w obozie pod warszawskim zamkiem o niewypłacony żołd.",
				WARSAW_CHAPTER,
				2,
				Region.WARSZAWA,
				150),
		WARSAW_WITNESSES(
				"trail_warsaw_witnesses",
				"Ci, którzy zostali w środku",
				"rycerz Mark",
				"witnesses;0",
				22000,
				400,
				"Zapiski obciążają zarządcę Radomira. Mark chce poznać relacje załogi i rannych, zanim oskarży dawnego towarzysza.",
				"Porozmawiaj z Markiem o ludziach w warowni. Powiedz: ludzie w środku.",
				WARSAW_CHAPTER,
				3,
				Region.WARSZAWA,
				150),
		WARSAW_FIRE(
				"trail_warsaw_fire",
				"Rozkaz podpalenia",
				"Dobrosława",
				"orders",
				30000,
				700,
				"Dobrosława usłyszała o beczkach z olejem wniesionych do magazynu. Radomir może chcieć spalić warownię razem z dowodami.",
				"Odwiedź Dobrosławę w lazarecie warowni. Zapytaj o beczki z olejem.",
				WARSAW_CHAPTER,
				4,
				Region.WARSZAWA,
				150),
		WARSAW_STEWARD(
				"trail_warsaw_steward",
				"Rachunek zarządcy",
				"Książę",
				"fight",
				55000,
				1200,
				"Olej został usunięty. Radomir zabarykadował się w sali dowodzenia. Trzeba go pokonać i odzyskać jego zapiski.",
				"Zanieś księciu wieści o przygotowanym podpaleniu. Powiedz: zarządca Radomir.",
				WARSAW_CHAPTER,
				5,
				Region.WARSZAWA,
				150),
		WARSAW_TRUCE(
				"trail_warsaw_truce",
				"Brama dla ocalałych",
				"rycerz Mark",
				"truce",
				28000,
				1700,
				"Radomir nie zagrozi już załodze. Pozostało uzgodnić wyjście rannych i przekonać księcia, żeby oszczędził zwykłych żołnierzy.",
				"Wróć do Marka i porozmawiaj o ocaleniu załogi.",
				WARSAW_CHAPTER,
				6,
				Region.WARSZAWA,
				150),
		COAST_WRECK(
				"trail_coast_wreck",
				"Mewa nie weszła do portu",
				"Staszek",
				"traces;0",
				35000,
				700,
				"Statek Mewa nie dotarł do Gdańska. Staszek boi się o jego załogę. Na brzegu na wschód od przystani znaleziono szczątki ładunku.",
				"Zapytaj Staszka na przystani promu w północno-zachodnim Gdańsku o zaginioną Mewę.",
				COAST_CHAPTER,
				1,
				Region.GDANSK_CITY,
				200),
		COAST_LIGHTS(
				"trail_coast_lights",
				"Światło po złej stronie",
				"Lubor",
				"lookout",
				45000,
				900,
				"Lubor przeżył rozbicie Mewy. Twierdzi, że załoga płynęła w stronę światła, którego nie powinno tam być.",
				"Porozmawiaj z Luborem na plaży na wschód od przystani promu. Powiedz: fałszywe światło.",
				COAST_CHAPTER,
				2,
				Region.GDANSK_CITY,
				200),
		COAST_CARGO(
				"trail_coast_cargo",
				"Łup przyniesiony za dnia",
				"Malteis",
				"cargo",
				40000,
				1000,
				"Przy fałszywym palenisku znaleziono znak gdańskiego składu. Malteis chce odnaleźć towary z rozbitego statku.",
				"Odwiedź Malteisa w siedzibie rycerzy w północno-wschodnim Gdańsku. Zapytaj o ładunek z Mewy.",
				COAST_CHAPTER,
				3,
				Region.GDANSK_CITY,
				200),
		COAST_WRECKERS(
				"trail_coast_wreckers",
				"Ci, którzy czekają na wraki",
				"Malteis",
				"fight",
				85000,
				1800,
				"Rosa rozpoznała ludzi sprzedających rzeczy rozbitków. Ich herszt ukrywa się w głębi piwnicy składu.",
				"Wróć do Malteisa z wieściami od Rosy. Powiedz: rabusie wybrzeża.",
				COAST_CHAPTER,
				4,
				Region.GDANSK_CITY,
				200),
		COAST_LAMPS(
				"trail_coast_lamps",
				"Znaki bezpiecznego brzegu",
				"Lubor",
				"rigging",
				40000,
				1100,
				"Kolejny statek ma wejść do portu. Lubor zna sygnał, który przeprowadzi go obok mielizny. Najpierw trzeba naprawić lampy.",
				"Pokaż Luborowi plan rabusiów. Zapytaj o bezpieczny brzeg.",
				COAST_CHAPTER,
				5,
				Region.GDANSK_CITY,
				200),
		COAST_RETURN(
				"trail_coast_return",
				"Jaskółka wraca do portu",
				"Staszek",
				"fight",
				55000,
				2500,
				"Lampy są gotowe, lecz ostatni z rabusiów chce zniszczyć sygnał. Jaskółka nie może podzielić losu Mewy.",
				"Zawiadom Staszka o przygotowanych lampach. Powiedz: ratunek dla Jaskółki.",
				COAST_CHAPTER,
				6,
				Region.GDANSK_CITY,
				200),
		SALT_SHIFT(
				"trail_salt_shift",
				"Zmiana, która nie wróciła",
				"Zbigniew",
				"foreman",
				45000,
				800,
				"Po wstrząsie z kopalni nie wrócili dwaj górnicy. Zbigniew chce poznać ich los, zanim zamknie wejście.",
				"Porozmawiaj ze Zbigniewem w ratuszu Wieliczki o zaginionej zmianie.",
				SALT_CHAPTER,
				1,
				Region.WIELICZKA,
				250),
		SALT_RESCUE(
				"trail_salt_rescue",
				"Po drugiej stronie zawału",
				"Radost",
				"brace",
				70000,
				1200,
				"Radost odnalazł ślady zaginionych. Najpierw trzeba podeprzeć naruszony strop i usunąć stworzenie z drogi do górników.",
				"Zapytaj sztygara Radosta przy wejściu do kopalni o odciętych górników.",
				SALT_CHAPTER,
				2,
				Region.WIELICZKA,
				250),
		SALT_SAMPLE(
				"trail_salt_sample",
				"Woda spod soli",
				"Radost",
				"sample",
				55000,
				1000,
				"Górnicy są bezpieczni, ale pod stropem nadal zbiera się solanka. Agrypin może rozpoznać to, co przyniosła ze sobą.",
				"Porozmawiaj z Radostem o próbce solanki.",
				SALT_CHAPTER,
				3,
				Region.WIELICZKA,
				250),
		SALT_SOURCE(
				"trail_salt_source",
				"Dokąd płynie solanka",
				"Agrypin",
				"channels;0",
				65000,
				1500,
				"W próbce coś się porusza. Agrypin podejrzewa naruszenie starego zbiornika. Trzeba ustalić, którędy wypływa z niego woda.",
				"Zapytaj Agrypina w jego domu w Wieliczce o stary zbiornik.",
				SALT_CHAPTER,
				4,
				Region.WIELICZKA,
				250),
		SALT_CREATURE(
				"trail_salt_creature",
				"To, co wyszło ze zbiornika",
				"Radost",
				"fight",
				145000,
				2500,
				"Wypływu pilnuje żywiołak solanki. Dopóki pozostaje przy szczelinie, nie da się zamknąć dopływu do chodników.",
				"Opowiedz Radostowi o źródle wypływu. Powiedz: żywiołak solanki.",
				SALT_CHAPTER,
				5,
				Region.WIELICZKA,
				250),
		SALT_RETURN(
				"trail_salt_return",
				"Droga na powierzchnię",
				"Radost",
				"drain",
				70000,
				3000,
				"Źródło wypływu zostało zamknięte. Pozostało odprowadzić nagromadzoną wodę i sprawdzić drogę do szybu.",
				"Zapytaj Radosta o bezpieczny szyb.",
				SALT_CHAPTER,
				6,
				Region.WIELICZKA,
				250),
		EMBER_CAMP(
				"trail_ember_camp",
				"Obóz w popiele",
				"Racirad",
				"traces;0",
				140000,
				2500,
				"Smoki zaatakowały obóz przy wejściu na Smocze Ziemie. Racirad zbiera ludzi do odwetu, lecz nie wszystko pasuje do zwykłego napadu.",
				"Zapytaj Racirada przy południowym wejściu na Smocze Ziemie o spalony obóz.",
				DRAGON_CHAPTER,
				1,
				Region.DRAGON_LANDS,
				400),
		EMBER_TRANSPORT(
				"trail_ember_transport",
				"Ładunek pod płótnem",
				"Racirad",
				"cargo",
				160000,
				3000,
				"W obozie znaleziono fragment smoczej skorupy. Racirad chce ustalić, kto przywiózł jaja między ludzi.",
				"Porozmawiaj z Raciradem o porzuconym transporcie.",
				DRAGON_CHAPTER,
				2,
				Region.DRAGON_LANDS,
				400),
		EMBER_SCENT(
				"trail_ember_scent",
				"Trop, którego nie widać",
				"Wizariusz",
				"mixture",
				170000,
				3500,
				"Robercik widział, jak woźnica smarował płótno ciemną mazią. Wizariusz może rozpoznać substancję, która zwabia smoki.",
				"Odwiedź Wizariusza na szczycie wieży w północno-zachodniej części Smoczych Ziem. Zapytaj o zapach jaj.",
				DRAGON_CHAPTER,
				3,
				Region.DRAGON_LANDS,
				400),
		EMBER_GUIDE(
				"trail_ember_guide",
				"Człowiek, który znał drogę",
				"Racirad",
				"fight",
				210000,
				4000,
				"Ślady prowadzą do obozowiska handlarzy. Więziony przewodnik może wiedzieć, skąd zabrano jaja.",
				"Pokaż Raciradowi ustalenia Wizariusza. Powiedz: pojmany przewodnik.",
				DRAGON_CHAPTER,
				4,
				Region.DRAGON_LANDS,
				400),
		EMBER_EGGS(
				"trail_ember_eggs",
				"Cena cudzego lęgu",
				"Ostrobor",
				"fight",
				340000,
				5000,
				"Ostrobor wskazał kryjówkę herszta. Pozostałe jaja wciąż tam są, gotowe do następnej prowokacji.",
				"Zapytaj Ostrobora w obozowisku handlarzy o skradziony lęg.",
				DRAGON_CHAPTER,
				5,
				Region.DRAGON_LANDS,
				400),
		EMBER_RETURN(
				"trail_ember_return",
				"Zanim zapłoną gniazda",
				"Ostrobor",
				"wash",
				180000,
				7000,
				"Jaja są odzyskane. Trzeba usunąć z nich przynętę, rozbroić pułapki i oddać lęg, zanim ludzie Racirada ruszą z pochodniami.",
				"Porozmawiaj z Ostroborem o powrocie do gniazda.",
				DRAGON_CHAPTER,
				6,
				Region.DRAGON_LANDS,
				400);

		public final String slot, title, npc, initial, description, hint,
				chapter, region;
		public final int xp, coins, order, minimumLevel;

		Step(String slot, String title, String npc, String initial, int xp,
				int coins, String description, String hint, String chapter,
				int order, String region) {
			this(slot, title, npc, initial, xp, coins, description, hint,
					chapter, order, region, 0);
		}

		Step(String slot, String title, String npc, String initial, int xp,
				int coins, String description, String hint, String chapter,
				int order, String region, int minimumLevel) {
			this.minimumLevel = minimumLevel;
			this.slot = slot;
			this.title = title;
			this.npc = npc;
			this.initial = initial;
			this.xp = xp;
			this.coins = coins;
			this.description = description;
			this.hint = hint;
			this.chapter = chapter;
			this.order = order;
			this.region = region;
		}

		public boolean available(final Player player) {
			return player.isQuestCompleted(previousSlot())
					&& player.getLevel() >= minimumLevel;
		}

		String previousSlot() {
			return ordinal() == 0 ? "wegiel_na_opal"
					: values()[ordinal() - 1].slot;
		}
	}

	private TrailQuestLine() {
	}

	/**
	 * The old magic-bag greeting must not close a conversation needed by the
	 * main story.
	 */
	public static boolean dragonScholarNeeded(Player player) {
		return player.isQuestCompleted(Step.EMBER_TRANSPORT.slot)
				&& !player.isQuestCompleted(Step.EMBER_SCENT.slot)
				&& (player.getLevel() >= 400
						|| player.hasQuest(Step.EMBER_SCENT.slot));
	}

	public static String nextObjective(final Player player) {
		for (final Step step : Step.values()) {
			if (!player.isQuestCompleted(step.slot))
				return objective(player, step.slot);
		}
		return null;
	}

	public static String objective(final Player player, final String slot) {
		final String stage = player.getQuest(slot, 0);
		for (final Step step : Step.values()) {
			if (!step.slot.equals(slot))
				continue;
			if (!player.isQuestCompleted(step.previousSlot())) {
				return step.ordinal() == 0 ? "Najpierw pomóż Staskowi z węglem."
						: objective(player,
								Step.values()[step.ordinal() - 1].slot);
			}
			if (player.getLevel() < step.minimumLevel && stage == null) {
				return "Zdobądź " + step.minimumLevel + ". poziom. Potem "
						+ step.hint.substring(0, 1).toLowerCase()
						+ step.hint.substring(1);
			}
			if (stage == null || "rejected".equals(stage))
				return step.hint;
			switch (step) {
			case DELIVERY:
				if ("inspect".equals(stage))
					return "Sprawdź skrzynię z dostawą w sklepie Staska w Zakopanem C.";
				if ("archive".equals(stage))
					return "Przejrzyj rejestr zamówień na małym stoliku obok Gościrada w ratuszu w centrum Zakopanego (Zakopane C).";
				return "Porozmawiaj z Gościradem o zamówieniu. Powiedz: zamówienie.";
			case GUEST:
				if ("jagna".equals(stage))
					return "Zapytaj Jagnę w karczmie w Zakopanem NE o woźnicę.";
				if ("brzezdom".equals(stage))
					return "Zapytaj Brzezdoma na polanie we wschodnim Kościelisku o woźnicę.";
				if ("tracks".equals(stage))
					return "Sprawdź rozbitą skrzynkę na północno-wschodnim skraju polany we wschodnim Kościelisku, na północ od Brzezdoma.";
				return "Wróć do Brzezdoma i opowiedz o znalezionej skrzynce. Powiedz: woźnica.";
			case SEAL:
				if ("guard".equals(stage))
					return "Pokonaj strażnika składu w północnej części polany we wschodnim Kościelisku. Jeśli go nie ma, poczekaj na jego powrót.";
				if ("documents".equals(stage))
					return "Przeszukaj dokumenty przy workach w ukrytym składzie we wschodnim Kościelisku.";
				return "Wróć do Brzezdoma z dokumentami ze składu. Powiedz: skład.";
			case DEBT:
				if ("report".equals(stage))
					return "Wróć do Gazdy Wojtka w ratuszu Zakopanego C i przedstaw dowody. Powiedz: dowody.";
				final int witnesses = witnesses(player);
				return "Pokaż zapiski "
						+ ((witnesses & 1) == 0
								? "Staskowi w sklepie w Zakopanem C"
								: "")
						+ (witnesses == 0 ? " oraz " : "")
						+ ((witnesses & 2) == 0
								? "Jagnie w karczmie w Zakopanem NE"
								: "")
						+ ". Zapytaj o dowody.";
			case SEMOS_LETTER:
				if ("bank".equals(stage))
					return "Pokaż kwit Dagobertowi w banku Semos i sprawdź księgę wpłat przy jego ladzie. Zapytaj: kwit.";
				return "Wróć do porucznika Drilenuna w ratuszu Semos i porozmawiaj o liście.";
			case SEMOS_SIGN:
				if ("library".equals(stage))
					return "Zapytaj Ceryla w bibliotece Semos o trzy kółka.";
				if ("shelf".equals(stage))
					return "Przejrzyj starą księgę przewoźników na stole przy południowo-zachodnim rogu biblioteki Semos.";
				return "Porozmawiaj z Cerylem o słowach znalezionych w księdze. Powiedz: suchy ładunek.";
			case SEMOS_PICKUP:
				if ("wood".equals(stage))
					return "Przynieś Margaret w gospodzie Semos 2 polana. Powiedz: przygotuj skrzynię.";
				if ("crate".equals(stage))
					return "Sprawdź skrzynię obok gospody w Semos. Stoi na zewnątrz, po prawej stronie wejścia.";
				if ("contact".equals(stage))
					return "Porozmawiaj z tragarzem Olkiem obok skrzyni pod gospodą. Użyj hasła z księgi: suchy ładunek.";
				if ("question".equals(stage))
					return "Poproś tragarza Olka o kwit na odbiór. Powiedz: kwit.";
				return "Zanieś wiadomość porucznikowi Drilenunowi w ratuszu Semos. Zapytaj o tragarza.";
			case RADAN_PASSAGE:
			case RADAN_RESCUE:
			case RADAN_TRANSPORT:
			case RADAN_FINALE:
				return RadanDungeonProgress.objective(player, step);
			case MINE_LEDGER:
			case MINE_TRACKS:
			case MINE_VOICES:
			case MINE_DISPATCH:
				return MineTrailProgress.objective(player, step);
			case MOUNTAIN_MINERS:
			case MOUNTAIN_THREATS:
			case MOUNTAIN_MEETING:
			case MOUNTAIN_CARGO:
			case MOUNTAIN_BATTLE:
			case MOUNTAIN_RETURN:
				return MountainTrailProgress.objective(player, step);
			case KEEP_TRAIL:
			case KEEP_WITNESSES:
			case KEEP_INSCRIPTIONS:
			case KEEP_GUARDIAN:
			case KEEP_RETURN:
				return AdosKeepProgress.objective(player, step);
			case OATH_WATCH:
			case OATH_NAMES:
			case OATH_GRAVE:
			case OATH_TRUTH:
			case OATH_BANNER:
			case OATH_BURIAL:
				return OathTrailProgress.objective(player, step);
			case WARSAW_GATE:
			case WARSAW_PAY:
			case WARSAW_WITNESSES:
			case WARSAW_FIRE:
			case WARSAW_STEWARD:
			case WARSAW_TRUCE:
				return WarsawFortressProgress.objective(player, step);
			case COAST_WRECK:
			case COAST_LIGHTS:
			case COAST_CARGO:
			case COAST_WRECKERS:
			case COAST_LAMPS:
			case COAST_RETURN:
				return CoastTrailProgress.objective(player, step);
			case SALT_SHIFT:
			case SALT_RESCUE:
			case SALT_SAMPLE:
			case SALT_SOURCE:
			case SALT_CREATURE:
			case SALT_RETURN:
				return SaltMineProgress.objective(player, step);
			case EMBER_CAMP:
			case EMBER_TRANSPORT:
			case EMBER_SCENT:
			case EMBER_GUIDE:
			case EMBER_EGGS:
			case EMBER_RETURN:
				return DragonTrailProgress.objective(player, step);
			default:
				throw new IllegalStateException("Unknown chapter step");
			}
		}
		return null;
	}

	public static List<String> history(final Player player, final Step step) {
		final List<String> entries = new ArrayList<>();
		final String stage = player.getQuest(step.slot, 0);
		if (stage == null)
			return entries;
		final boolean done = player.isQuestCompleted(step.slot);
		switch (step) {
		case DELIVERY:
			entries.add(
					"Stasek znalazł dziwne zamówienie w skrzyni z opałem. Chce wiedzieć, czy naprawdę pochodzi z ratusza.");
			if (!"inspect".equals(stage))
				entries.add(
						"Na dokumencie są pieczęć ratusza i trzy kółka w miejscu podpisu.");
			if ("discrepancy".equals(stage) || done)
				entries.add(
						"W księdze nie ma tego zamówienia. Nie brakuje żadnych stron, a na pozostałych dokumentach są inny podpis i pieczęć.");
			if (done)
				entries.add(
						"Gościrad nie wystawił tego zamówienia. Trzeba znaleźć woźnicę, który odebrał zapasy.");
			break;
		case GUEST:
			entries.add(
					"Gościrad radzi zapytać Jagnę o woźnicę. Mógł zatrzymać się w jej karczmie.");
			if (!"jagna".equals(stage))
				entries.add(
						"Jagna zapamiętała, że woźnica mówił o kopalni, ale pytał o boczną drogę i Brzezdoma.");
			if (!"jagna".equals(stage) && !"brzezdom".equals(stage))
				entries.add(
						"Brzezdom widział wóz skręcający przy lesie. Wskazał rozbitą skrzynkę na północno-wschodnim skraju polany.");
			if ("found".equals(stage) || done)
				entries.add(
						"Skrzynka ma taki sam znak jak zamówienie. Między deskami został sznurek ubrudzony woskiem. Ślady wozu prowadzą w głąb polany.");
			if (done)
				entries.add(
						"Brzezdom wskazał ukryty skład w północnej części polany. Zapasy są pilnowane.");
			break;
		case SEAL:
			entries.add(
					"Brzezdom prosi o sprawdzenie składu. Najpierw trzeba pokonać strażnika. Samych zapasów nie należy zabierać.");
			if (!"guard".equals(stage))
				entries.add(
						"Walka ze strażnikiem za mną. Można sprawdzić papiery przy workach.");
			if ("recovered".equals(stage) || done)
				entries.add(
						"W spisie są węgiel Staska i mąka dla karczmy. Jest też list o następnym transporcie do Semos.");
			if (done)
				entries.add(
						"Brzezdom radzi pokazać znalezisko Gazdzie Wojtkowi w ratuszu w Zakopanem C.");
			break;
		case DEBT:
			entries.add(
					"Wojtek chce wiedzieć, czy Stasek i Jagna poznają swoje towary. Trzeba pokazać im zapiski.");
			final int confirmed = done || "report".equals(stage) ? 3
					: witnesses(player);
			if ((confirmed & 1) != 0)
				entries.add(
						"Stasek poznaje swoje worki z węglem. Nie wysyłał ich do Kościeliska.");
			if ((confirmed & 2) != 0)
				entries.add(
						"Jagna poznaje zamówioną mąkę. To ten sam woźnica miał ją przywieźć do karczmy.");
			if (done)
				entries.add(
						"Wojtek pośle ludzi po zapasy i uprzedzi kupców o fałszywych zamówieniach. Został jeszcze list ze znakiem trzech kółek. Następny transport miał trafić do Semos.");
			break;
		case SEMOS_LETTER:
			entries.add(
					"Drilenun chce sprawdzić, kto zapłacił za transport z Zakopanego. Dagobert prowadzi księgę wpłat w banku Semos.");
			if (!"bank".equals(stage))
				entries.add(
						"W księdze jest zaliczka na dostawę, ale zamiast nazwiska ktoś narysował trzy kółka. Kwit ma numer 17. Odbiór ma się odbyć przy gospodzie w Semos.");
			if (done)
				entries.add(
						"Drilenun poprosił Dagoberta, żeby wstrzymał wypłatę. Trzeba jeszcze ustalić, kto posługuje się tym znakiem.");
			break;
		case SEMOS_SIGN:
			entries.add("Dagobert radzi zapytać Ceryla o znak trzech kółek.");
			if (!"library".equals(stage))
				entries.add(
						"Ceryl pamięta taki znak ze starej księgi przewoźników. Wskazał ją na stole przy wejściu do biblioteki.");
			if ("phrase".equals(stage) || done)
				entries.add(
						"W księdze zapisano znak przewoźnika i dopisek: suchy ładunek. Ten sam zwrot jest na kwicie numer 17.");
			if (done)
				entries.add(
						"Ceryl odczytał miejsce odbioru: obok gospody w Semos. Margaret powinna wiedzieć, kto tam przychodzi.");
			break;
		case SEMOS_PICKUP:
			entries.add(
					"Margaret zgodziła się przygotować skrzynię z polanami zamiast prawdziwego towaru.");
			if (!"wood".equals(stage))
				entries.add(
						"Margaret dostała dwa polana. Skrzynia czeka obok wejścia do gospody.");
			if ("contact".equals(stage) || "question".equals(stage)
					|| "return".equals(stage) || done)
				entries.add(
						"Skrzynia jest gotowa do odbioru. Olek czeka obok niej na umówione hasło.");
			if ("question".equals(stage) || "return".equals(stage) || done)
				entries.add(
						"Olek rozpoznał hasło: suchy ładunek. Można poprosić go o kwit.");
			if ("return".equals(stage) || done)
				entries.add(
						"Na kwicie jest nazwisko Radan. Olek twierdzi, że dostał pieniądze i instrukcję odbioru, ale nie znał zawartości skrzyni.");
			if (done)
				entries.add(
						"Drilenun dostał odpis kwitu. Bank wstrzyma wypłatę, a straż sprawdzi człowieka podpisanego jako Radan. Olek nie będzie już odbierał tych przesyłek.");
			break;
		case RADAN_PASSAGE:
		case RADAN_RESCUE:
		case RADAN_TRANSPORT:
		case RADAN_FINALE:
			return RadanDungeonProgress.history(player, step);
		case MINE_LEDGER:
		case MINE_TRACKS:
		case MINE_VOICES:
		case MINE_DISPATCH:
			return MineTrailProgress.history(player, step);
		case MOUNTAIN_MINERS:
		case MOUNTAIN_THREATS:
		case MOUNTAIN_MEETING:
		case MOUNTAIN_CARGO:
		case MOUNTAIN_BATTLE:
		case MOUNTAIN_RETURN:
			return MountainTrailProgress.history(player, step);
		case KEEP_TRAIL:
		case KEEP_WITNESSES:
		case KEEP_INSCRIPTIONS:
		case KEEP_GUARDIAN:
		case KEEP_RETURN:
			return AdosKeepProgress.history(player, step);
		case OATH_WATCH:
		case OATH_NAMES:
		case OATH_GRAVE:
		case OATH_TRUTH:
		case OATH_BANNER:
		case OATH_BURIAL:
			return OathTrailProgress.history(player, step);
		case WARSAW_GATE:
		case WARSAW_PAY:
		case WARSAW_WITNESSES:
		case WARSAW_FIRE:
		case WARSAW_STEWARD:
		case WARSAW_TRUCE:
			return WarsawFortressProgress.history(player, step);
		case COAST_WRECK:
		case COAST_LIGHTS:
		case COAST_CARGO:
		case COAST_WRECKERS:
		case COAST_LAMPS:
		case COAST_RETURN:
			return CoastTrailProgress.history(player, step);
		case SALT_SHIFT:
		case SALT_RESCUE:
		case SALT_SAMPLE:
		case SALT_SOURCE:
		case SALT_CREATURE:
		case SALT_RETURN:
			return SaltMineProgress.history(player, step);
		case EMBER_CAMP:
		case EMBER_TRANSPORT:
		case EMBER_SCENT:
		case EMBER_GUIDE:
		case EMBER_EGGS:
		case EMBER_RETURN:
			return DragonTrailProgress.history(player, step);
		default:
			throw new IllegalStateException("Unknown chapter step");
		}
		return entries;
	}

	static int witnesses(final Player player) {
		final String value = player.getQuest(Step.DEBT.slot, 1);
		if ("1".equals(value))
			return 1;
		if ("2".equals(value))
			return 2;
		return "3".equals(value) ? 3 : 0;
	}

	/**
	 * Record nearby story kills only from the expected world respawn points.
	 */
	public static void recordGuardKill(final Player player,
			final RPEntity victim) {
		MineTrailProgress.recordCourierKill(player, victim);
		MountainTrailProgress.recordKill(player, victim);
		AdosKeepProgress.recordGuardianKill(player, victim);
		OathTrailProgress.recordKill(player, victim);
		WarsawFortressProgress.recordKill(player, victim);
		CoastTrailProgress.recordKill(player, victim);
		SaltMineProgress.recordKill(player, victim);
		DragonTrailProgress.recordKill(player, victim);
		if (player == null || !player.isQuestInState(Step.SEAL.slot, "guard")
				|| !(victim instanceof Creature)
				|| victim instanceof ArenaCreature
				|| victim instanceof DeathMatchCreature
				|| !GUARD.equals(victim.getName()) || victim.getZone() == null
				|| player.getZone() != victim.getZone()
				|| !FOREST.equals(victim.getZone().getName())
				|| !player.nextTo(victim, 16))
			return;
		final CreatureRespawnPoint origin = ((Creature) victim).getRespawnPoint();
		if (origin == null || origin.getZone() != victim.getZone()
				|| origin.getX() != STOCK_X || origin.getY() != STOCK_Y)
			return;
		player.setQuest(Step.SEAL.slot, "documents");
		player.sendPrivateText(
				"Strażnik już ci nie przeszkodzi. Sprawdź papiery przy workach.");
	}
}
