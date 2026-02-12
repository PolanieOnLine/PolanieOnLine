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
package games.stendhal.server.maps.kikareukin;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import games.stendhal.server.maps.event.BaseMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;

public class KikareukinMapEventConfigProvider implements MapEventConfigProvider {
	public static final String KIKAREUKIN_ANGEL_PREVIEW = "kikareukin_angel_preview";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(KIKAREUKIN_ANGEL_PREVIEW, createKikareukinPreviewConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createKikareukinPreviewConfig() {
		final Map<String, Double> zoneSpawnMultipliers = new LinkedHashMap<>();
		zoneSpawnMultipliers.put("6_kikareukin_islands", 1.0d);
		zoneSpawnMultipliers.put("7_kikareukin_clouds", 0.4d);

		return MapEventConfig.builder("Kikareukin Angel Incursion")
				.duration(Duration.ofMinutes(45))
				.zones(Arrays.asList(
						"6_kikareukin_islands",
						"7_kikareukin_clouds"
				))
				.observerZones(Arrays.asList(
						"6_kikareukin_islands",
						"7_kikareukin_clouds"
				))
				.creatureFilter(new LinkedHashSet<>(Arrays.asList(
						"aniołek",
						"anioł",
						"upadły anioł",
						"anioł ciemności",
						"archanioł",
						"archanioł ciemności"
				)))
				.waves(Arrays.asList(
						new BaseMapEvent.EventWave(35, Arrays.asList(
								new BaseMapEvent.EventSpawn("aniołek", 12),
								new BaseMapEvent.EventSpawn("anioł", 4)
						)),
						new BaseMapEvent.EventWave(60, Arrays.asList(
								new BaseMapEvent.EventSpawn("aniołek", 10),
								new BaseMapEvent.EventSpawn("anioł", 6),
								new BaseMapEvent.EventSpawn("upadły anioł", 4)
						)),
						new BaseMapEvent.EventWave(90, Arrays.asList(
								new BaseMapEvent.EventSpawn("anioł", 8),
								new BaseMapEvent.EventSpawn("upadły anioł", 6),
								new BaseMapEvent.EventSpawn("anioł ciemności", 3)
						)),
						new BaseMapEvent.EventWave(120, Arrays.asList(
								new BaseMapEvent.EventSpawn("upadły anioł", 8),
								new BaseMapEvent.EventSpawn("anioł ciemności", 4),
								new BaseMapEvent.EventSpawn("archanioł", 1)
						)),
						new BaseMapEvent.EventWave(160, Arrays.asList(
								new BaseMapEvent.EventSpawn("upadły anioł", 10),
								new BaseMapEvent.EventSpawn("anioł ciemności", 5),
								new BaseMapEvent.EventSpawn("archanioł", 2),
								new BaseMapEvent.EventSpawn("archanioł ciemności", 1),
								new BaseMapEvent.EventSpawn("anioł", 4)
						))
				))
				.announcements(Arrays.asList(
						"Nad chmurami Kikareukin krążą ciemne skrzydła - trzymajcie linię.",
						"Na wyspach słychać hymn i szczęk stali; anielskie zastępy schodzą coraz niżej.",
						"Chmury gęstnieją od blasku archaniołów, a wyspy proszą o wsparcie.",
						"Szlak między wyspami i chmurami płonie od starcia - nie oddawajcie terenu."
				))
				.startAnnouncement("Nad Kikareukin pękły chmury - anielskie zastępy schodzą na wyspy!")
				.stopAnnouncement("Nad Kikareukin zapadła cisza; chmury i wyspy są na chwilę bezpieczne.")
				.announcementIntervalSeconds(300)
				.weatherLock(new MapEventConfig.WeatherLockConfig("fog", false))
				.zoneSpawnMultipliers(zoneSpawnMultipliers)
				.triggerThreshold(120)
				.defaultStartTime("20:00")
				.defaultIntervalDays(3)
				.build();
	}
}
