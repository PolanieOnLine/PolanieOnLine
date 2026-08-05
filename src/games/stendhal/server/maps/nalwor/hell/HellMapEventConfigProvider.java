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
package games.stendhal.server.maps.nalwor.hell;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import games.stendhal.server.maps.event.BaseMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;
import games.stendhal.server.maps.event.MapEventConfigSupport;

public class HellMapEventConfigProvider implements MapEventConfigProvider {
	public static final String HELL_CAPTURE_ASSAULT = "hell_capture_assault";
	public static final String HELL_RITUAL_DISRUPTION = "hell_ritual_disruption";
	public static final String HELL_PORTAL_SEAL = "hell_portal_seal";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<String, MapEventConfig>();
		configs.put(HELL_CAPTURE_ASSAULT, createHellCaptureAssaultConfig());
		configs.put(HELL_RITUAL_DISRUPTION, createHellRitualDisruptionConfig());
		configs.put(HELL_PORTAL_SEAL, createHellPortalSealConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createHellCaptureAssaultConfig() {
		final Map<String, Double> zoneSpawnMultipliers = new LinkedHashMap<String, Double>();
		zoneSpawnMultipliers.put("hell", 1.0d);
		final Map<String, Integer> zoneSpawnCaps = new LinkedHashMap<String, Integer>();
		zoneSpawnCaps.put("hell", 200);
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("21:00", HELL_CAPTURE_ASSAULT);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(2, HELL_CAPTURE_ASSAULT);

		return MapEventConfig.builder("Szturm Piekła")
				.duration(Duration.ofMinutes(35))
				.zones(Arrays.asList("hell"))
				.announcements(Arrays.asList(
						"Piekielny punkt oporu rośnie w siłę - utrzymajcie napór.",
						"Wrota piekieł drżą, kolejne zastępy nadchodzą.",
						"Punkt piekielny niemal przejęty - przygotujcie się na kontratak."))
				.startAnnouncement("Szturm Piekła rozpoczęty! Przejmijcie punkt i przetrwajcie fale.")
				.stopAnnouncement("Szturm Piekła zakończony. Piekielne zastępy wycofują się.")
				.announcementIntervalSeconds(420)
				.zoneSpawnMultipliers(zoneSpawnMultipliers)
				.zoneSpawnCaps(zoneSpawnCaps)
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.capturePoints(Arrays.asList(
						MapEventConfigSupport.capturePoint("hell_core", "hell", 50, 50, 9),
						MapEventConfigSupport.capturePoint("hell_reaper_gate", "hell", 64, 76, 7),
						MapEventConfigSupport.capturePoint("hell_blood_crossroads", "hell", 30, 30, 7),
						MapEventConfigSupport.capturePoint("hell_crystal_front", "hell", 112, 100, 8)))
				.captureProgressWaves(MapEventConfigSupport.captureProgressWaves(
						MapEventConfigSupport.captureProgressWave(20,
								MapEventConfigSupport.spawn("czart", 9),
								MapEventConfigSupport.spawn("śmierć", 4)),
						MapEventConfigSupport.captureProgressWave(40,
								MapEventConfigSupport.spawn("kostucha różowa", 8),
								MapEventConfigSupport.spawn("kostucha", 4),
								MapEventConfigSupport.spawn("czart", 4)),
						MapEventConfigSupport.captureProgressWave(60,
								MapEventConfigSupport.spawn("kostucha różowa", 9),
								MapEventConfigSupport.spawn("kostucha", 5),
								MapEventConfigSupport.spawn("kostucha złota", 3),
								MapEventConfigSupport.spawn("chaos lord", 4)),
						MapEventConfigSupport.captureProgressWave(80,
								MapEventConfigSupport.spawn("kostucha wielka", 8),
								MapEventConfigSupport.spawn("kostucha złota", 4),
								MapEventConfigSupport.spawn("kostucha różowa wielka", 4),
								MapEventConfigSupport.spawn("chaos lord", 5),
								MapEventConfigSupport.spawn("złota śmierć", 2)),
						MapEventConfigSupport.captureProgressWave(100,
								MapEventConfigSupport.spawn("kostucha wielka", 10),
								MapEventConfigSupport.spawn("kostucha złota wielka", 6),
								MapEventConfigSupport.spawn("kostucha różowa wielka", 6),
								MapEventConfigSupport.spawn("kostucha różowa", 10),
								MapEventConfigSupport.spawn("chaos lord", 6),
								MapEventConfigSupport.spawn("złota śmierć", 5))))
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("breach", "Przełamanie piekła",
								"Pierwsze zastępy piekielne sprawdzają siłę obrony.", 0,
								"Wrota piekieł pękają. Rozpoczyna się przełamanie."),
						MapEventConfigSupport.phase("siege", "Oblężenie punktów",
								"Wróg wzmacnia presję wokół punktów kontroli.", 2,
								"Piekielne szeregi zaciskają oblężenie wokół punktów.", 1.10d,
								Collections.<String, Double>emptyMap(), "Oblężenie punktów"),
						MapEventConfigSupport.phase("cataclysm", "Kataklizm",
								"Nadchodzi finałowy kontratak elit piekielnych.", 4,
								"Kataklizm piekła. Elity ruszają do ostatecznego kontrataku.", 1.18d,
								Collections.<String, Double>emptyMap(), "Kataklizm piekła")))
				.modifiers(1, 2, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("ashen_reinforcements", "Popielate posiłki",
								"Większa liczba czartów i kostuch zjawia się z popiołu.", 1,
								"Popielate posiłki wychodzą z rozdarcia piekła.", 1.08d, 0.08d,
								MapEventConfigSupport.spawn("czart", 4)),
						MapEventConfigSupport.modifier("reaper_guard", "Gwardia żniwiarzy",
								"Na polu walki pojawiają się dodatkowe elity.", 3,
								"Gwardia żniwiarzy osłania piekielne punkty oporu.", 1.10d, 0.10d,
								MapEventConfigSupport.spawn("chaos lord", 2),
								MapEventConfigSupport.spawn("złota śmierć", 1))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.captureObjective("secure_core", "Zabezpiecz rdzeń",
								"Utrzymaj główny rdzeń piekła i doprowadź go do niemal pełnego poziomu przejęcia. Jeśli obrona odpuści rdzeń, kataklizm dośle dodatkowych żniwiarzy i chaos lordów na finał.", 1, 5, 96,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: zabezpiecz rdzeń piekła przed kontratakiem.",
								"Cel poboczny wykonany. Rdzeń piekła został zdestabilizowany.",
								"Cel poboczny nieudany. Rdzeń nadal pulsuje mocą.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("chaos lord", 3),
										MapEventConfigSupport.spawn("złota śmierć", 2),
										MapEventConfigSupport.spawn("kostucha złota wielka", 2)),
								"hell_core")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("HELL_CAPTURE_ASSAULT", "Szturm Piekła", 55, 1.0d))
				.build();
	}

	private MapEventConfig createHellRitualDisruptionConfig() {
		final Map<String, Double> zoneSpawnMultipliers = new LinkedHashMap<String, Double>();
		zoneSpawnMultipliers.put("hell", 1.0d);
		final Map<String, Integer> zoneSpawnCaps = new LinkedHashMap<String, Integer>();
		zoneSpawnCaps.put("hell", 220);
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("23:15",
				HELL_RITUAL_DISRUPTION);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(3,
				HELL_RITUAL_DISRUPTION);

		return MapEventConfig.builder("Przerwanie piekielnego rytuału")
				.duration(Duration.ofMinutes(30))
				.zones(Arrays.asList("hell"))
				.announcements(Arrays.asList(
						"Rytuał piekielny postępuje. Elity osłaniają demonicznych celebransów.",
						"Krwawe runy rozświetlają piekło. Rytuał trzeba przerwać przed finałem.",
						"Coraz więcej sług piekła chroni celebransów i rdzeń rytuału."))
				.startAnnouncement("Rozpoczyna się przerwanie piekielnego rytuału. Wyszukaj celebransów i rozetnij eskortę.")
				.stopAnnouncement("Piekielny rytuał ucichł. Zagrożenie zostało stłumione.")
				.announcementIntervalSeconds(240)
				.zoneSpawnMultipliers(zoneSpawnMultipliers)
				.zoneSpawnCaps(zoneSpawnCaps)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(25,
								MapEventConfigSupport.spawn("czart", 15),
								MapEventConfigSupport.spawn("kostucha", 8)),
						MapEventConfigSupport.wave(55,
								MapEventConfigSupport.spawn("kostucha różowa", 11),
								MapEventConfigSupport.spawn("chaos lord", 4)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("kostucha wielka", 9),
								MapEventConfigSupport.spawn("chaos lord", 6)),
						MapEventConfigSupport.wave(125,
								MapEventConfigSupport.spawn("złota śmierć", 4),
								MapEventConfigSupport.spawn("chaos lord", 8),
								MapEventConfigSupport.spawn("kostucha złota wielka", 5))))
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("approach", "Rozpoznanie rytuału",
								"Pierwsza osłona celebransów schodzi na pole walki.", 0,
								"Piekielni celebransi rozpoczęli rytuał. Znajdźcie osłonę."),
						MapEventConfigSupport.phase("disruption", "Rozcinanie ochrony",
								"Trzeba wybić elitarnych strażników rytuału.", 2,
								"Rytuał przyspiesza. Trzeba wyciąć elitarnych strażników.", 1.12d,
								Collections.<String, Double>emptyMap(), "Rozcinanie ochrony"),
						MapEventConfigSupport.phase("collapse", "Załamanie rytuału",
								"Ostatnie sekundy na rozbicie rdzenia rytualnego.", 4,
								"Ostatnia szansa. Rytuał wchodzi w finałową fazę.", 1.20d,
								Collections.<String, Double>emptyMap(), "Załamanie rytuału")))
				.modifiers(1, 1, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("blood_echo", "Krwawy rezonans",
								"Rytuał wysyła fale wzmacniające eskortę.", 3,
								"Krwawy rezonans wzmacnia eskortę rytuału.", 1.10d, 0.12d,
								MapEventConfigSupport.spawn("chaos lord", 2))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.killTargetObjective("slay_ritualists", "Zabij celebransów",
								"Wytnij chaos lordów prowadzących rytuał, zanim piekielna osłona się ustabilizuje. Jeżeli celebransi przetrwają, rytuał wypchnie dodatkową elitarną eskortę do finału.", 1, 4, 12,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: zabij celebransów rytuału.",
								"Cel poboczny wykonany. Rytuał został rozerwany od środka.",
								"Cel poboczny nieudany. Celebransi utrzymali część mocy rytuału.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("chaos lord", 4),
										MapEventConfigSupport.spawn("złota śmierć", 2),
										MapEventConfigSupport.spawn("kostucha złota wielka", 2)),
								"chaos lord")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("HELL_RITUAL_DISRUPTION",
						"Przerwanie piekielnego rytuału", 50, 1.08d))
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}

	private MapEventConfig createHellPortalSealConfig() {
		final Map<String, Double> zoneSpawnMultipliers = new LinkedHashMap<String, Double>();
		zoneSpawnMultipliers.put("hell", 1.0d);
		final Map<String, Integer> zoneSpawnCaps = new LinkedHashMap<String, Integer>();
		zoneSpawnCaps.put("hell", 220);
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("23:45",
				HELL_PORTAL_SEAL);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(4, HELL_PORTAL_SEAL);

		return MapEventConfig.builder("Zamknięcie piekielnych portali")
				.duration(Duration.ofMinutes(32))
				.zones(Arrays.asList("hell"))
				.zoneSpawnMultipliers(zoneSpawnMultipliers)
				.zoneSpawnCaps(zoneSpawnCaps)
				.creatureFilter(new java.util.LinkedHashSet<String>(Arrays.asList(
						"czart",
						"śmierć",
						"kostucha",
						"kostucha różowa",
						"kostucha wielka",
						"kostucha złota",
						"kostucha złota wielka",
						"chaos lord",
						"złota śmierć")))
				.announcements(Arrays.asList(
						"Piekielne portale otwierają się na kilku frontach. Każdy pozostawiony portal wzmacnia finał.",
						"Runy przejść pulsują coraz mocniej. Trzeba domykać kolejne ogniska napływu wrogów.",
						"Portale piekielne zsynchronizowały się. Bez szybkiej reakcji elity zaleją środek piekła."))
				.startAnnouncement("Rozpoczyna się zamknięcie piekielnych portali. Oczyśćcie ogniska napływu zanim piekło przejdzie do kontrataku.")
				.stopAnnouncement("Piekielne portale zostały zamknięte. Wydarzenie dobiegło końca.")
				.announcementIntervalSeconds(240)
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(25,
								MapEventConfigSupport.spawn("czart", 13),
								MapEventConfigSupport.spawn("śmierć", 5)),
						MapEventConfigSupport.wave(55,
								MapEventConfigSupport.spawn("kostucha", 9),
								MapEventConfigSupport.spawn("kostucha różowa", 8),
								MapEventConfigSupport.spawn("chaos lord", 3)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("kostucha wielka", 8),
								MapEventConfigSupport.spawn("kostucha złota", 5),
								MapEventConfigSupport.spawn("chaos lord", 5)),
						MapEventConfigSupport.wave(125,
								MapEventConfigSupport.spawn("kostucha złota wielka", 5),
								MapEventConfigSupport.spawn("złota śmierć", 3),
								MapEventConfigSupport.spawn("chaos lord", 6))))
				.capturePoints(Arrays.asList(
						MapEventConfigSupport.capturePoint("Portal północny", "hell", 64, 76, 7),
						MapEventConfigSupport.capturePoint("Portal zachodni", "hell", 30, 30, 7),
						MapEventConfigSupport.capturePoint("Portal wschodni", "hell", 112, 100, 8)))
				.captureProgressWaves(MapEventConfigSupport.captureProgressWaves(
						MapEventConfigSupport.captureProgressWave(25,
								MapEventConfigSupport.spawn("czart", 5),
								MapEventConfigSupport.spawn("śmierć", 2)),
						MapEventConfigSupport.captureProgressWave(50,
								MapEventConfigSupport.spawn("kostucha", 4),
								MapEventConfigSupport.spawn("kostucha różowa", 4),
								MapEventConfigSupport.spawn("chaos lord", 2)),
						MapEventConfigSupport.captureProgressWave(75,
								MapEventConfigSupport.spawn("kostucha wielka", 4),
								MapEventConfigSupport.spawn("chaos lord", 3),
								MapEventConfigSupport.spawn("złota śmierć", 1)),
						MapEventConfigSupport.captureProgressWave(100,
								MapEventConfigSupport.spawn("chaos lord", 4),
								MapEventConfigSupport.spawn("złota śmierć", 2),
								MapEventConfigSupport.spawn("kostucha złota wielka", 2))))
				.phases(MapEventConfigSupport.phases(
						MapEventConfigSupport.phase("outbreak", "Otwarcie portali",
								"Trzy ogniska napływu wrogów otwierają się jednocześnie.", 0,
								"Portale piekielne otwierają się na kilku frontach. Obrona musi rozdzielić siły."),
						MapEventConfigSupport.phase("seal_pressure", "Napór na pieczęcie",
								"Elity próbują utrzymać przejścia i rozedrzeć linie obrony.", 2,
								"Elity osłaniają portale. Jeśli pieczęcie nie zostaną domknięte, finał przybierze na sile.",
								1.12d, Collections.<String, Double>emptyMap(), "Napór na pieczęcie"),
						MapEventConfigSupport.phase("cataclysm_gate", "Kataklizm przejść",
								"Ostatni kontratak spływa z niedomkniętych przejść.", 4,
								"Finałowe domykanie portali. Każde zaniedbane przejście wzmacnia kontratak.",
								1.20d, Collections.<String, Double>emptyMap(), "Kataklizm przejść")))
				.modifiers(1, 1, MapEventConfigSupport.modifiers(
						MapEventConfigSupport.modifier("soul_leak", "Wyciek dusz",
								"Portale wyrzucają dodatkowe duchy i żniwiarzy.", 2,
								"Przez portale przeciska się dodatkowy wyciek dusz. Obrona pęka na kilku odcinkach.", 1.10d, 0.08d,
								MapEventConfigSupport.spawn("śmierć", 3),
								MapEventConfigSupport.spawn("kostucha różowa", 3))))
				.secondaryObjectives(MapEventConfigSupport.secondaryObjectives(
						MapEventConfigSupport.killTargetObjective("slay_gatekeepers", "Zabij strażników portali",
								"Wytnij chaos lordów i złote śmierci pilnujące przejść. Jeśli strażnicy przetrwają, do końca wydarzenia portale wypchną dodatkowy kontratak elit.", 1, 4, 10,
								MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
								0.0d, "Dodatkowa złota skrzynia po ukończeniu wydarzenia dla kwalifikowanych uczestników.",
								"Cel poboczny: zabij strażników pilnujących piekielnych portali.",
								"Cel poboczny wykonany. Strażnicy portali zostali wybici, a przejścia osłabły.",
								"Cel poboczny nieudany. Strażnicy utrzymali przejścia i przywołują elitarny kontratak.",
								Collections.<BaseMapEvent.EventSpawn>emptyList(),
								Arrays.asList(
										MapEventConfigSupport.spawn("chaos lord", 3),
										MapEventConfigSupport.spawn("złota śmierć", 2),
										MapEventConfigSupport.spawn("kostucha złota wielka", 2)),
								"chaos lord", "złota śmierć")))
				.rewardSettings(MapEventConfigSupport.rewardSettings("HELL_PORTAL_SEAL",
						"Zamknięcie piekielnych portali", 55, 1.08d))
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}
}
