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

import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;
import games.stendhal.server.maps.event.MapEventConfigSupport;

public class KikareukinMapEventConfigProvider implements MapEventConfigProvider {
	public static final String KIKAREUKIN_ANGEL_PREVIEW = "kikareukin_angel_preview";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(KIKAREUKIN_ANGEL_PREVIEW, createKikareukinPreviewConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createKikareukinPreviewConfig() {
		// Balance domain note: Kikareukin is a mobility-heavy map, so pressure escalates with
		// mixed air/ground angel packs but keeps room for regrouping between bursts.
		final Map<String, Double> zoneSpawnMultipliers = new LinkedHashMap<>();
		zoneSpawnMultipliers.put("6_kikareukin_islands", 1.0d);
		zoneSpawnMultipliers.put("7_kikareukin_clouds", 0.4d);
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("20:00", KIKAREUKIN_ANGEL_PREVIEW);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(3, KIKAREUKIN_ANGEL_PREVIEW);

		return MapEventConfig.builder("Kikareukin Angel Incursion")
				// Core config
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
				// Messages
				.announcements(Arrays.asList(
						"Nad chmurami Kikareukin krążą ciemne skrzydła - trzymajcie linię.",
						"Na wyspach słychać hymn i szczęk stali; anielskie zastępy schodzą coraz niżej.",
						"Chmury gęstnieją od blasku archaniołów, a wyspy proszą o wsparcie.",
						"Szlak między wyspami i chmurami płonie od starcia - nie oddawajcie terenu."
				))
				.startAnnouncement("Nad Kikareukin pękły chmury - anielskie zastępy schodzą na wyspy!")
				.stopAnnouncement("Nad Kikareukin zapadła cisza; chmury i wyspy są na chwilę bezpieczne.")
				.announcementIntervalSeconds(300)
				// Waves
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(35,
								MapEventConfigSupport.spawn("aniołek", 12),
								MapEventConfigSupport.spawn("anioł", 4)),
						MapEventConfigSupport.wave(60,
								MapEventConfigSupport.spawn("aniołek", 10),
								MapEventConfigSupport.spawn("anioł", 6),
								MapEventConfigSupport.spawn("upadły anioł", 4)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("anioł", 8),
								MapEventConfigSupport.spawn("upadły anioł", 6),
								MapEventConfigSupport.spawn("anioł ciemności", 3)),
						MapEventConfigSupport.wave(120,
								MapEventConfigSupport.spawn("upadły anioł", 8),
								MapEventConfigSupport.spawn("anioł ciemności", 4),
								MapEventConfigSupport.spawn("archanioł", 1)),
						MapEventConfigSupport.wave(160,
								MapEventConfigSupport.spawn("upadły anioł", 10),
								MapEventConfigSupport.spawn("anioł ciemności", 5),
								MapEventConfigSupport.spawn("archanioł", 2),
								MapEventConfigSupport.spawn("archanioł ciemności", 1),
								MapEventConfigSupport.spawn("anioł", 4))))
				// Triggers / scheduler
				.weatherLock(new MapEventConfig.WeatherLockConfig("fog", false))
				.zoneSpawnMultipliers(zoneSpawnMultipliers)
				.triggerThreshold(120)
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}
}
