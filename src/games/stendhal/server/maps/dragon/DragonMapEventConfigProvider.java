/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.dragon;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import games.stendhal.server.maps.event.BaseMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;
import games.stendhal.server.maps.event.MapEventConfigSupport;

public class DragonMapEventConfigProvider implements MapEventConfigProvider {
	public static final String DRAGON_LAND_DEFAULT = "dragon_land_default";
	public static final String DRAGON_PHASED_HUNT = "dragon_phased_hunt";
	public static final String DRAGON_BROOD_NEST_DEFENSE = "dragon_brood_nest_defense";
	private static final List<String> WAWELSKI_SPAWN_ZONES = Collections.unmodifiableList(Arrays.asList(
			"0_dragon_land_n",
			"0_dragon_land_s"));

	public static List<String> getWawelskiSpawnZones() {
		return WAWELSKI_SPAWN_ZONES;
	}

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<String, MapEventConfig>();
		configs.put(DRAGON_LAND_DEFAULT, createDragonLandDefaultConfig());
		configs.put(DRAGON_PHASED_HUNT, createDragonPhasedHuntConfig());
		configs.put(DRAGON_BROOD_NEST_DEFENSE, createDragonBroodNestDefenseConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createDragonLandDefaultConfig() {
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("20:00", DRAGON_LAND_DEFAULT);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(2, DRAGON_LAND_DEFAULT);

		return MapEventConfig.builder("Dragon Land")
				.duration(Duration.ofMinutes(60))
				.zones(Arrays.asList(
						"0_dragon_land_n",
						"0_dragon_land_s"))
				.observerZones(Arrays.asList(
						"0_dragon_land_n",
						"0_dragon_land_s",
						"int_dragon_house_1",
						"int_dragon_house_2",
						"int_dragon_house_3",
						"int_dragon_house_4",
						"int_dragon_house_5",
						"int_dragon_house_6",
						"int_dragon_workshop",
						"int_dragon_castle",
						"int_dragon_castle_room_1",
						"int_dragon_castle_room_2",
						"int_dragon_castle_room_3",
						"int_dragon_castle_room_4",
						"int_dragon_castle_room_5",
						"int_dragon_castle_room_6",
						"int_dragon_castle_dragon_npc",
						"int_dragon_castle_dragon",
						"-1_dragon_cave"))
				.creatureFilter(new LinkedHashSet<String>(Arrays.asList(
						"dwugłowy czarny smok",
						"dwugłowy lodowy smok",
						"dwugłowy czerwony smok",
						"dwugłowy złoty smok",
						"dwugłowy zielony smok",
						"lodowy smok",
						"pustynny smok",
						"zgniły szkielet smoka",
						"smok arktyczny",
						"zielone smoczysko",
						"niebieskie smoczysko",
						"czerwone smoczysko",
						"czarne smoczysko",
						"latający czarny smok",
						"latający złoty smok",
						"Smok Wawelski")))
				.announcements(Arrays.asList(
						"Niebo przeszywa skrzek smoków - smocze stado krąży nad krainą.",
						"Z oddali dobiega trzepot skrzydeł i syk ognia - smoki nie odpuszczają.",
						"Smocza kraina drży pod ciężarem bestii, które krążą nad ziemią."))
				.startAnnouncement("Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie.")
				.stopAnnouncement("Smocza kraina uspokaja się. Wydarzenie dobiegło końca.")
				.announcementIntervalSeconds(600)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(30,
								MapEventConfigSupport.spawn("zgniły szkielet smoka", 12),
								MapEventConfigSupport.spawn("pustynny smok", 9),
								MapEventConfigSupport.spawn("zielony smok", 9),
								MapEventConfigSupport.spawn("czerwony smok", 6),
								MapEventConfigSupport.spawn("błękitny smok", 9)),
						MapEventConfigSupport.wave(45,
								MapEventConfigSupport.spawn("lodowy smok", 9),
								MapEventConfigSupport.spawn("smok arktyczny", 7),
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 12)),
						MapEventConfigSupport.wave(60,
								MapEventConfigSupport.spawn("dwugłowy złoty smok", 8),
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 13),
								MapEventConfigSupport.spawn("dwugłowy czerwony smok", 10)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("dwugłowy złoty smok", 6),
								MapEventConfigSupport.spawn("zielone smoczysko", 7),
								MapEventConfigSupport.spawn("niebieskie smoczysko", 7)),
						MapEventConfigSupport.wave(120,
								MapEventConfigSupport.spawn("dwugłowy czarny smok", 8),
								MapEventConfigSupport.spawn("dwugłowy lodowy smok", 13)),
						MapEventConfigSupport.wave(150,
								MapEventConfigSupport.spawn("czerwone smoczysko", 7),
								MapEventConfigSupport.spawn("czarne smoczysko", 6)),
						MapEventConfigSupport.wave(180,
								MapEventConfigSupport.spawn("latający czarny smok", 3),
								MapEventConfigSupport.spawn("latający złoty smok", 3))))
				.scaling(MapEventConfig.ScalingConfig.builder()
						.scaleByOnlineInZones(true)
						.minPlayers(3)
						.maxPlayers(35)
						.onlineZoneMinPlayerLevel(35)
						.onlineZoneMaxPlayerLevel(250)
						.killRateMultiplier(0.8d)
						.minSpawnPerWave(30)
						.maxSpawnPerWave(88)
						.build())
				.weatherLock(new MapEventConfig.WeatherLockConfig("fog", false))
				.triggerThreshold(500)
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("entry", "Nalot zwiadu",
								"Smocze stado sonduje obronę krainy.", 0,
								"Smoki rozpoczynają zwiad i przeczesują pierwsze przejścia."),
						MapEventConfigSupport.phase("main", "Główna nawałnica",
								"Najcięższe bestie schodzą niżej nad pole walki.", 3,
								"Nadciąga główna fala smoków. Utrzymajcie obie strony krainy.",
								1.15d, Collections.<String, Double>emptyMap(), "Główna nawałnica"),
						MapEventConfigSupport.phase("final", "Finał smoczego szturmu",
								"Elity i smoczyska przejmują niebo nad krainą.", 6,
								"Finał szturmu smoków. Elity schodzą do ostatniego uderzenia.",
								1.25d, Collections.<String, Double>emptyMap(), "Finał smoczego szturmu")))
				.modifiers(1, 2, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("night_elites", "Nocne elity",
								"Po zmroku silniejsze bestie wzmacniają kolejne fale.", 2,
								"Nocne elity dołączają do szturmu smoków.", 1.12d, 0.10d,
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 2)),
						MapEventConfigSupport.modifier("north_breach", "Przełamanie północy",
								"Północna część krainy jest mocniej naciskana.", 4,
								"Smoki szukają luki od północy i przełamują szyki.", 1.0d,
								MapEventConfigSupport.zoneMultipliers("0_dragon_land_n", 1.40d, "0_dragon_land_s", 0.75d),
								0.08d, MapEventConfigSupport.spawn("smok arktyczny", 2))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.killTargetObjective("slay_elites", "Rozbij smocze elity",
								"Pokonaj dwugłowe zielone smoki i smoki arktyczne prowadzące główną nawałnicę nad Dragon Landem. Bez ich rozbicia południe i północ zostaną zasypane kolejną eskortą.", 2, 6, 18,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: zabij smocze elity podczas głównej nawałnicy.",
								"Cel poboczny wykonany. Elity zostały rozbite.",
								"Cel poboczny nieudany. Elity zdołały przecisnąć się dalej.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("dwugłowy zielony smok", 4),
										MapEventConfigSupport.spawn("smok arktyczny", 3),
										MapEventConfigSupport.spawn("dwugłowy lodowy smok", 2)),
								"dwugłowy zielony smok", "smok arktyczny")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("DRAGON_LAND", "Smocza Kraina", 55, 1.0d))
				.build();
	}

	private MapEventConfig createDragonPhasedHuntConfig() {
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("22:30", DRAGON_PHASED_HUNT);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(3, DRAGON_PHASED_HUNT);

		return MapEventConfig.builder("Łowy na smoczą bestię")
				.duration(Duration.ofMinutes(28))
				.zones(Arrays.asList("0_dragon_land_n", "0_dragon_land_s"))
				.observerZones(Arrays.asList("0_dragon_land_n", "0_dragon_land_s", "-1_dragon_cave"))
				.creatureFilter(new LinkedHashSet<String>(Arrays.asList(
						"smok arktyczny", "pustynny smok", "dwugłowy lodowy smok", "dwugłowy czerwony smok")))
				.announcements(Arrays.asList(
						"Tropiciele widzą smoczy ślad - bestia zmienia kierunek lotu.",
						"Smocza zdobycz zrywa się z jednego krańca krainy na drugi.",
						"Bestia przelatuje nad krainą i pozostawia za sobą kolejne gniazda obronne."))
				.startAnnouncement("Rozpoczynają się łowy na smoczą bestię. Trop podzieli walkę na kilka frontów.")
				.stopAnnouncement("Smocza bestia została odepchnięta. Łowy dobiegły końca.")
				.announcementIntervalSeconds(240)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(25,
								MapEventConfigSupport.spawn("pustynny smok", 10),
								MapEventConfigSupport.spawn("smok arktyczny", 8)),
						MapEventConfigSupport.wave(55,
								MapEventConfigSupport.spawn("smok arktyczny", 10),
								MapEventConfigSupport.spawn("dwugłowy lodowy smok", 6)),
						MapEventConfigSupport.wave(85,
								MapEventConfigSupport.spawn("pustynny smok", 10),
								MapEventConfigSupport.spawn("dwugłowy czerwony smok", 7)),
						MapEventConfigSupport.wave(120,
								MapEventConfigSupport.spawn("dwugłowy lodowy smok", 8),
								MapEventConfigSupport.spawn("dwugłowy czerwony smok", 8))))
				.scaling(MapEventConfig.ScalingConfig.builder()
						.scaleByOnlineInZones(true)
						.minPlayers(2)
						.maxPlayers(30)
						.onlineZoneMinPlayerLevel(35)
						.onlineZoneMaxPlayerLevel(250)
						.killRateMultiplier(0.75d)
						.minSpawnPerWave(18)
						.maxSpawnPerWave(58)
						.build())
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("north", "Północny trop",
								"Bestia pokazuje się nad północną częścią krainy.", 0,
								"Trop prowadzi na północ. Tam pojawia się pierwsza fala.",
								1.0d, MapEventConfigSupport.zoneMultipliers("0_dragon_land_n", 1.5d, "0_dragon_land_s", 0.0d),
								"Polowanie na północy"),
						MapEventConfigSupport.phase("south", "Południowy odwrót",
								"Bestia nurkuje nad południowe przejścia.", 2,
								"Smocza bestia zmienia kierunek. Front przenosi się na południe.",
								1.0d, MapEventConfigSupport.zoneMultipliers("0_dragon_land_n", 0.0d, "0_dragon_land_s", 1.5d),
								"Polowanie na południu"),
						MapEventConfigSupport.phase("crossfire", "Krzyżowy nalot",
								"Obie strony krainy zostają trafione jednocześnie.", 4,
								"Bestia wraca nad całą krainę. Oba fronty są aktywne.",
								1.20d, Collections.<String, Double>emptyMap(), "Krzyżowy nalot")))
				.modifiers(1, 1, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("shattered_wings", "Rozszarpane skrzydła",
								"Ranny smok wpada w furię i przyciąga eskortę.", 3,
								"Ranna bestia szarpie niebo i wzywa eskortę.", 1.10d, 0.10d,
								MapEventConfigSupport.spawn("dwugłowy lodowy smok", 3),
								MapEventConfigSupport.spawn("dwugłowy czerwony smok", 2))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.killTargetObjective("finish_hunt", "Dobić tropioną bestię",
								"Pokonaj dwugłowe lodowe i dwugłowe czerwone smoki prowadzące przelot bestii między północą i południem krainy. Jeżeli ich nie rozbijecie, finałowe przejście zamieni się w pełny nalot eskorty.", 1, 4, 12,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: zbij elity prowadzące bestię między strefami.",
								"Cel poboczny wykonany. Łowy zakończą się z dodatkową nagrodą.",
								"Cel poboczny nieudany. Bestia wyrwała się z zasadzki.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("dwugłowy lodowy smok", 3),
										MapEventConfigSupport.spawn("dwugłowy czerwony smok", 3),
										MapEventConfigSupport.spawn("smok arktyczny", 4)),
								"dwugłowy lodowy smok", "dwugłowy czerwony smok")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("DRAGON_PHASED_HUNT", "Łowy na smoczą bestię", 50, 1.05d))
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}

	private MapEventConfig createDragonBroodNestDefenseConfig() {
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("17:15",
				DRAGON_BROOD_NEST_DEFENSE);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(3,
				DRAGON_BROOD_NEST_DEFENSE);

		return MapEventConfig.builder("Obrona smoczych lęgowisk")
				.duration(Duration.ofMinutes(34))
				.zones(Arrays.asList("0_dragon_land_n", "0_dragon_land_s"))
				.observerZones(Arrays.asList("0_dragon_land_n", "0_dragon_land_s", "-1_dragon_cave"))
				.creatureFilter(new LinkedHashSet<String>(Arrays.asList(
						"smok arktyczny",
						"dwugłowy zielony smok",
						"dwugłowy czerwony smok",
						"dwugłowy lodowy smok",
						"zielone smoczysko",
						"czarne smoczysko",
						"latający czarny smok")))
				.announcements(Arrays.asList(
						"Smocze lęgowiska pękają od ruchu. Eskorty schodzą ku północy i południu krainy.",
						"Drżące jaja i trzepot skrzydeł zdradzają, że kolejne stada zbierają się nad lęgowiskami.",
						"Smoki próbują utrzymać oba lęgowiska jednocześnie. Bez podziału obrony kraina pęknie na dwa fronty."))
				.startAnnouncement("Rozpoczyna się obrona smoczych lęgowisk. Utrzymajcie północ i południe, zanim stado się rozrośnie.")
				.stopAnnouncement("Smocze lęgowiska przycichły. Wydarzenie dobiegło końca.")
				.announcementIntervalSeconds(300)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(30,
								MapEventConfigSupport.spawn("smok arktyczny", 9),
								MapEventConfigSupport.spawn("zielone smoczysko", 7)),
						MapEventConfigSupport.wave(55,
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 9),
								MapEventConfigSupport.spawn("smok arktyczny", 8)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("dwugłowy czerwony smok", 8),
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 10),
								MapEventConfigSupport.spawn("czarne smoczysko", 6)),
						MapEventConfigSupport.wave(125,
								MapEventConfigSupport.spawn("dwugłowy lodowy smok", 8),
								MapEventConfigSupport.spawn("latający czarny smok", 3),
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 8))))
				.capturePoints(Arrays.asList(
						MapEventConfigSupport.capturePoint("Północne lęgowisko", "0_dragon_land_n", 64, 26, 9),
						MapEventConfigSupport.capturePoint("Południowe lęgowisko", "0_dragon_land_s", 66, 78, 9)))
				.scaling(MapEventConfig.ScalingConfig.builder()
						.scaleByOnlineInZones(true)
						.minPlayers(3)
						.maxPlayers(35)
						.onlineZoneMinPlayerLevel(35)
						.onlineZoneMaxPlayerLevel(250)
						.killRateMultiplier(0.82d)
						.minSpawnPerWave(24)
						.maxSpawnPerWave(66)
						.build())
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("scouting", "Rozpoznanie gniazd",
								"Pierwsze smoki krążą nad oboma lęgowiskami i sprawdzają obronę.", 0,
								"Zwiad stada przeczesuje lęgowiska. Utrzymajcie oba gniazda jednocześnie."),
						MapEventConfigSupport.phase("brood_pressure", "Napór stada",
								"Elity zrzucają kolejne fale na oba lęgowiska.", 2,
								"Smocze stado naciska pełną masą. Północ i południe wymagają równoczesnej obrony.",
								1.15d, Collections.<String, Double>emptyMap(), "Napór na lęgowiska"),
						MapEventConfigSupport.phase("hatchling_fury", "Furia lęgowisk",
								"Rozjuszone elity osłaniają ostatni zryw młodych smoków.", 4,
								"Finałowy napór stada. Jeśli lęgowiska nie zostaną utrzymane, dołączą kolejne elity.",
								1.22d, Collections.<String, Double>emptyMap(), "Furia lęgowisk")))
				.modifiers(1, 1, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("ashen_hatchlings", "Rozwścieczone młode",
								"Młode smoki wypełzają z gniazd i osłaniają cięższe elity.", 2,
								"Z lęgowisk wypełzają rozwścieczone młode. Elity zyskują dodatkową osłonę.", 1.10d, 0.08d,
								MapEventConfigSupport.spawn("zielone smoczysko", 4),
								MapEventConfigSupport.spawn("czarne smoczysko", 3))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.captureObjective("hold_broods", "Utrzymaj oba lęgowiska",
								"Doprowadź północne i południowe lęgowisko do bezpiecznego poziomu obrony. Jeśli choć jedno pęknie, finał dośle ciężką eskortę dwugłowych smoków.", 1, 4, 92,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: utrzymaj oba lęgowiska przed finałowym naporem.",
								"Cel poboczny wykonany. Lęgowiska zostały zabezpieczone.",
								"Cel poboczny nieudany. Stado wzywa dodatkową eskortę do ostatniej fazy.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("dwugłowy zielony smok", 3),
										MapEventConfigSupport.spawn("dwugłowy lodowy smok", 2),
										MapEventConfigSupport.spawn("latający czarny smok", 2)),
								"Północne lęgowisko", "Południowe lęgowisko")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("DRAGON_BROOD_NEST_DEFENSE",
						"Obrona smoczych lęgowisk", 55, 1.06d))
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}
}
