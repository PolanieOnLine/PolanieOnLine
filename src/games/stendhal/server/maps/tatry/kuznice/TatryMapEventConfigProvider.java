/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.tatry.kuznice;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import games.stendhal.server.maps.event.BaseMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;
import games.stendhal.server.maps.event.MapEventConfigSupport;

public class TatryMapEventConfigProvider implements MapEventConfigProvider {
	public static final String TATRY_KUZNICE_BANDIT_RAID = "tatry_kuznice_bandit_raid";
	public static final String TATRY_SUPPLY_DEFENSE = "tatry_supply_defense";
	public static final String TATRY_CONVOY_ESCORT = "tatry_convoy_escort";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<String, MapEventConfig>();
		configs.put(TATRY_KUZNICE_BANDIT_RAID, createTatryKuzniceBanditRaidConfig());
		configs.put(TATRY_SUPPLY_DEFENSE, createTatrySupplyDefenseConfig());
		configs.put(TATRY_CONVOY_ESCORT, createTatryConvoyEscortConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createTatryKuzniceBanditRaidConfig() {
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("18:30",
				TATRY_KUZNICE_BANDIT_RAID);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(1,
				TATRY_KUZNICE_BANDIT_RAID);

		return MapEventConfig.builder("Napad zbójników na Kuźnice")
				.duration(Duration.ofMinutes(35))
				.zones(Arrays.asList(
						"0_tatry_kuznice",
						"int_tatry_kuznice_blacksmith",
						"int_tatry_kuznice_tavern",
						"int_tatry_kuznice_chapel",
						"int_tatry_kuznice_hostel"))
				.announcements(Arrays.asList(
						"Zbójnickie rogi niosą się po Kuźnicach - zwiad napastników schodzi ze szlaku.",
						"Bandy zbójników przenikają między kuźnią i karczmą - mieszkańcy potrzebują obrony.",
						"Herszt zbiera ludzi pod Tatrami. Utrzymajcie Kuźnice i nie oddajcie przełęczy."))
				.startAnnouncement("Alarm w Kuźnicach! Rozpoczyna się napad górskich zbójników.")
				.stopAnnouncement("Kuźnice odetchnęły - napad zbójników został odparty.")
				.announcementIntervalSeconds(300)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(35,
								MapEventConfigSupport.spawn("zbójnik leśny", 10),
								MapEventConfigSupport.spawn("zbójnik leśny oszust", 6),
								MapEventConfigSupport.spawn("zbójnik leśny zwiadowca", 5)),
						MapEventConfigSupport.wave(55,
								MapEventConfigSupport.spawn("zbójnik leśny", 9),
								MapEventConfigSupport.spawn("zbójnik górski", 8),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 5),
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 4)),
						MapEventConfigSupport.wave(85,
								MapEventConfigSupport.spawn("zbójnik górski", 11),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 7),
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 8),
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 5)),
						MapEventConfigSupport.wave(115,
								MapEventConfigSupport.spawn("zbójnik górski", 12),
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 10),
								MapEventConfigSupport.spawn("zbójnik górski starszy", 7),
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 6))))
				.scaling(MapEventConfig.ScalingConfig.builder()
						.scaleByOnlineInZones(true)
						.minPlayers(2)
						.maxPlayers(28)
						.onlineZoneMinPlayerLevel(20)
						.onlineZoneMaxPlayerLevel(150)
						.killRateMultiplier(0.7d)
						.minSpawnPerWave(20)
						.maxSpawnPerWave(52)
						.build())
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("entry", "Zwiad bandytów",
								"Pierwsze grupy badają obronę Kuźnic.", 0,
								"Zbójnicy wypuszczają zwiad i badają obronę Kuźnic."),
						MapEventConfigSupport.phase("pressure", "Naprężenie frontu",
								"Kolejne grupy próbują wejść do zabudowań.", 2,
								"Napastnicy naciskają coraz mocniej od kuźni i gospody.", 1.10d,
								Collections.<String, Double>emptyMap(), "Napad w toku"),
						MapEventConfigSupport.phase("counter", "Kontratak herszta",
								"Herszt wysyła starszych i złośliwych zbójników.", 4,
								"Herszt wypuszcza starszych wojów. To finałowy nacisk na Kuźnice.", 1.20d,
								Collections.<String, Double>emptyMap(), "Kontratak herszta")))
				.modifiers(1, 2, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("mountain_fog", "Górska mgła",
								"Napastnicy skrywają ruchy w mgle schodzącej z Tatr.", 1,
								"Górska mgła utrudnia obronę. Bandyci przesuwają szyki w cieniu.", 1.08d, 0.08d,
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 3)),
						MapEventConfigSupport.modifier("blacksmith_breach", "Nacisk na kuźnię",
								"Większa część napastników schodzi na rejon kuźni.", 3,
								"Napastnicy koncentrują szturm na rejonie kuźni.", 1.0d,
								MapEventConfigSupport.zoneMultipliers("0_tatry_kuznice", 1.25d, "int_tatry_kuznice_blacksmith", 1.5d),
								0.10d, MapEventConfigSupport.spawn("zbójnik górski starszy", 2))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.killTargetObjective("cut_runners", "Odetnij gońców",
								"Wybij zbójników górskich gońców i zwiadowców, zanim ściągną kolejne oddziały na Kuźnice. Samo czyszczenie głównej fali nie wystarczy - trzeba ich aktywnie wyłapywać między kuźnią i gospodą.", 1, 4, 18,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: odetnij gońców i zwiadowców zbójnickich.",
								"Cel poboczny wykonany. Zbójnicy tracą łączność i impet.",
								"Cel poboczny nieudany. Gońcy rozciągnęli obronę.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("zbójnik górski goniec", 4),
										MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 4),
										MapEventConfigSupport.spawn("zbójnik górski starszy", 2)),
								"zbójnik górski goniec", "zbójnik górski zwiadowca")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("KUZNICE_BANDIT_RAID", "Napad na Kuźnice", 55, 1.0d))
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}

	private MapEventConfig createTatrySupplyDefenseConfig() {
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("19:45",
				TATRY_SUPPLY_DEFENSE);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(2,
				TATRY_SUPPLY_DEFENSE);

		return MapEventConfig.builder("Obrona dostaw do Kuźnic")
				.duration(Duration.ofMinutes(32))
				.zones(Arrays.asList("0_tatry_kuznice", "int_tatry_kuznice_blacksmith", "int_tatry_kuznice_tavern"))
				.announcements(Arrays.asList(
						"Wozy z zaopatrzeniem stają się celem. Zbójnicy szukają słabych punktów.",
						"Napastnicy rozdzielają siły i próbują przerwać dostawy.",
						"Obrona wozów wymaga podziału ludzi między kilka wejść."))
				.startAnnouncement("Rozpoczyna się obrona dostaw do Kuźnic. Utrzymajcie kluczowe punkty miasta.")
				.stopAnnouncement("Dostawy do Kuźnic zostały zabezpieczone. Event dobiegł końca.")
				.announcementIntervalSeconds(240)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(25,
								MapEventConfigSupport.spawn("zbójnik leśny", 11),
								MapEventConfigSupport.spawn("zbójnik leśny zwiadowca", 8)),
						MapEventConfigSupport.wave(55,
								MapEventConfigSupport.spawn("zbójnik górski", 12),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 7)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 11),
								MapEventConfigSupport.spawn("zbójnik górski starszy", 7)),
						MapEventConfigSupport.wave(125,
								MapEventConfigSupport.spawn("zbójnik górski", 12),
								MapEventConfigSupport.spawn("zbójnik górski starszy", 8),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 5))))
				.capturePoints(Arrays.asList(
						MapEventConfigSupport.capturePoint("Plac dostaw", "0_tatry_kuznice", 31, 22, 7),
						MapEventConfigSupport.capturePoint("Kuźnia", "int_tatry_kuznice_blacksmith", 8, 8, 5),
						MapEventConfigSupport.capturePoint("Karczma", "int_tatry_kuznice_tavern", 7, 8, 5)))
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("split", "Podział obrony",
								"Napastnicy rozchodzą się na kilka punktów jednocześnie.", 0,
								"Zbójnicy uderzają na kilka punktów. Trzeba rozdzielić obronę."),
						MapEventConfigSupport.phase("focused", "Nacisk na centrum",
								"Główna masa napastników schodzi na plac dostaw.", 3,
								"Główna siła przeciwnika naciska plac dostaw.", 1.15d,
								MapEventConfigSupport.zoneMultipliers("0_tatry_kuznice", 1.5d), "Obrona wielu punktów"),
						MapEventConfigSupport.phase("last_push", "Finalny szturm",
								"Ostatnia fala próbuje wyrwać zaopatrzenie z miasta.", 4,
								"Finalny szturm na zaopatrzenie Kuźnic.", 1.25d,
								Collections.<String, Double>emptyMap(), "Finalny szturm")))
				.modifiers(1, 1, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("wagon_panic", "Panika tragarzy",
								"Chaos wokół dostaw przyciąga kolejnych napastników.", 2,
								"Tragarze wpadają w panikę. Bandyci wykorzystują zamieszanie.", 1.12d, 0.10d,
								MapEventConfigSupport.spawn("zbójnik górski goniec", 3))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.captureObjective("hold_supply", "Utrzymaj plac dostaw",
								"Zabezpiecz plac dostaw i utrzymaj tam niemal pełną przewagę obrony przed finałowym szturmem. Jeżeli plac zostanie odpuszczony, bandyci przerzucą świeże siły na centrum i karczmę.", 1, 4, 96,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: przywróć bezpieczeństwo na placu dostaw.",
								"Cel poboczny wykonany. Plac dostaw został umocniony.",
								"Cel poboczny nieudany. Plac pozostaje zagrożony.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("zbójnik górski", 4),
										MapEventConfigSupport.spawn("zbójnik górski starszy", 3),
										MapEventConfigSupport.spawn("zbójnik górski goniec", 3)),
								"Plac dostaw")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("TATRY_SUPPLY_DEFENSE", "Obrona dostaw do Kuźnic", 50, 1.05d))
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}

	private MapEventConfig createTatryConvoyEscortConfig() {
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("17:45",
				TATRY_CONVOY_ESCORT);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(3,
				TATRY_CONVOY_ESCORT);

		return MapEventConfig.builder("Eskorta konwoju przez Kuźnice")
				.duration(Duration.ofMinutes(30))
				.zones(Arrays.asList("0_tatry_kuznice", "int_tatry_kuznice_blacksmith", "int_tatry_kuznice_tavern"))
				.creatureFilter(new LinkedHashSet<String>(Arrays.asList(
						"zbójnik leśny",
						"zbójnik leśny zwiadowca",
						"zbójnik górski",
						"zbójnik górski goniec",
						"zbójnik górski złośliwy",
						"zbójnik górski starszy")))
				.announcements(Arrays.asList(
						"Konwój zaopatrzeniowy próbuje przebić się przez Kuźnice. Bandyci szykują zasadzki na kolejnych odcinkach.",
						"Napastnicy przecinają trasę przejazdu i chcą odizolować kuźnię od placu dostaw.",
						"Trzeba czyścić naciski na trasie, inaczej konwój ugrzęźnie pod finałowym szturmem."))
				.startAnnouncement("Rozpoczyna się eskorta konwoju przez Kuźnice. Utrzymajcie trasę przejazdu i punkty osłony.")
				.stopAnnouncement("Konwój przedarł się przez Kuźnice. Wydarzenie dobiegło końca.")
				.announcementIntervalSeconds(240)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(25,
								MapEventConfigSupport.spawn("zbójnik leśny", 10),
								MapEventConfigSupport.spawn("zbójnik leśny zwiadowca", 6)),
						MapEventConfigSupport.wave(55,
								MapEventConfigSupport.spawn("zbójnik górski", 10),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 6),
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 4)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 10),
								MapEventConfigSupport.spawn("zbójnik górski starszy", 6),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 5)),
						MapEventConfigSupport.wave(125,
								MapEventConfigSupport.spawn("zbójnik górski", 12),
								MapEventConfigSupport.spawn("zbójnik górski starszy", 8),
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 8))))
				.capturePoints(Arrays.asList(
						MapEventConfigSupport.capturePoint("Brama północna", "0_tatry_kuznice", 24, 19, 7),
						MapEventConfigSupport.capturePoint("Plac dostaw", "0_tatry_kuznice", 31, 22, 7),
						MapEventConfigSupport.capturePoint("Kuźnia", "int_tatry_kuznice_blacksmith", 8, 8, 5)))
				.scaling(MapEventConfig.ScalingConfig.builder()
						.scaleByOnlineInZones(true)
						.minPlayers(2)
						.maxPlayers(28)
						.onlineZoneMinPlayerLevel(20)
						.onlineZoneMaxPlayerLevel(150)
						.killRateMultiplier(0.74d)
						.minSpawnPerWave(18)
						.maxSpawnPerWave(48)
						.build())
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("departure", "Wyjazd konwoju",
								"Pierwsza część eskorty musi oczyścić bramę i plac dostaw.", 0,
								"Konwój rusza z Kuźnic. Trzeba oczyścić bramę i utrzymać plac dostaw."),
						MapEventConfigSupport.phase("route_pressure", "Nacisk na trasę",
								"Napastnicy rozciągają front między placem i kuźnią.", 2,
								"Bandyci odcinają trasę przejazdu. Obrona musi rozdzielić siły między dwa punkty.",
								1.12d, Collections.<String, Double>emptyMap(), "Nacisk na trasę"),
						MapEventConfigSupport.phase("final_breakthrough", "Finalne przebicie",
								"Ostatnie oddziały próbują rozbić konwój przed kuźnią.", 4,
								"Finałowy szturm na eskortę konwoju. Jeżeli trasa nie zostanie utrzymana, bandyci doślą starszych wojów.",
								1.20d, Collections.<String, Double>emptyMap(), "Finalne przebicie")))
				.modifiers(1, 1, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("route_saboteurs", "Sabotażyści szlaku",
								"Gońcy i zwiadowcy przenikają bokami i rozrywają osłonę konwoju.", 2,
								"Sabotażyści szlaku uderzają z boku. Konwój potrzebuje szybkiej osłony.", 1.08d, 0.08d,
								MapEventConfigSupport.spawn("zbójnik górski goniec", 3),
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 3))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.killTargetObjective("break_ambush", "Rozbij zasadzkę",
								"Wybij zbójników górskich gońców i starszych dowodzących zasadzką na trasie konwoju. Jeśli przeżyją, finałowa fala dostanie świeże posiłki.", 1, 4, 14,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: rozbij dowódców zasadzki zanim domkną trasę.",
								"Cel poboczny wykonany. Zasadzka została rozbita przed finałowym przebiciem.",
								"Cel poboczny nieudany. Bandyci ściągają dodatkowe posiłki na trasę.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("zbójnik górski starszy", 3),
										MapEventConfigSupport.spawn("zbójnik górski goniec", 4),
										MapEventConfigSupport.spawn("zbójnik górski złośliwy", 3)),
								"zbójnik górski goniec", "zbójnik górski starszy")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("TATRY_CONVOY_ESCORT",
						"Eskorta konwoju przez Kuźnice", 50, 1.05d))
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}
}
