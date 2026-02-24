/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
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

import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;
import games.stendhal.server.maps.event.MapEventConfigSupport;

public class HellMapEventConfigProvider implements MapEventConfigProvider {
	public static final String HELL_CAPTURE_ASSAULT = "hell_capture_assault";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(HELL_CAPTURE_ASSAULT, createHellCaptureAssaultConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createHellCaptureAssaultConfig() {
		final Map<String, Double> zoneSpawnMultipliers = new LinkedHashMap<>();
		zoneSpawnMultipliers.put("hell", 1.0d);
		final Map<String, Integer> zoneSpawnCaps = new LinkedHashMap<>();
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
						// Soft waves: maintain constant pressure from the first threshold.
						MapEventConfigSupport.captureProgressWave(20,
								MapEventConfigSupport.spawn("czart", 7),
								MapEventConfigSupport.spawn("śmierć", 3)),
						// Medium waves: +30-60% population versus previous tuning.
						MapEventConfigSupport.captureProgressWave(40,
								MapEventConfigSupport.spawn("kostucha różowa", 6),
								MapEventConfigSupport.spawn("kostucha", 3),
								MapEventConfigSupport.spawn("czart", 3)),
						MapEventConfigSupport.captureProgressWave(60,
								MapEventConfigSupport.spawn("kostucha różowa", 7),
								MapEventConfigSupport.spawn("kostucha", 4),
								MapEventConfigSupport.spawn("kostucha złota", 2),
								MapEventConfigSupport.spawn("chaos lord", 3)),
						// Hard wave: larger mixed packs with extra elites.
						MapEventConfigSupport.captureProgressWave(80,
								MapEventConfigSupport.spawn("kostucha wielka", 6),
								MapEventConfigSupport.spawn("kostucha złota", 3),
								MapEventConfigSupport.spawn("kostucha różowa wielka", 3),
								MapEventConfigSupport.spawn("chaos lord", 4),
								MapEventConfigSupport.spawn("złota śmierć", 1)),
						// Finale: explicit difficulty spike with heavy elite composition.
						MapEventConfigSupport.captureProgressWave(100,
								MapEventConfigSupport.spawn("kostucha wielka", 8),
								MapEventConfigSupport.spawn("kostucha złota wielka", 5),
								MapEventConfigSupport.spawn("kostucha różowa wielka", 5),
								MapEventConfigSupport.spawn("kostucha różowa", 8),
								MapEventConfigSupport.spawn("chaos lord", 5),
								MapEventConfigSupport.spawn("złota śmierć", 4))))
				.build();
	}
}
