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
package games.stendhal.server.maps.event;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MapEventConfigLoader {
	public static final String DRAGON_LAND_DEFAULT = "dragon_land_default";
	public static final String KIKAREUKIN_ANGEL_PREVIEW = "kikareukin_angel_preview";
	public static final String KOSCIELISKO_GIANT_ESCORT = "koscielisko_giant_escort";

	private static final Map<String, MapEventConfig> CONFIGS = createConfigs();

	private MapEventConfigLoader() {
		// utility class
	}

	public static MapEventConfig load(final String configId) {
		if (configId == null) {
			throw new IllegalArgumentException("Map event config id cannot be null.");
		}
		final MapEventConfig config = CONFIGS.get(configId);
		if (config == null) {
			throw new IllegalArgumentException("Unknown map event config: " + configId
					+ ". Available configIds: " + availableConfigIds() + ".");
		}
		return config;
	}

	public static boolean hasConfigId(final String configId) {
		return configId != null && CONFIGS.containsKey(configId);
	}

	public static Set<String> availableConfigIds() {
		return Collections.unmodifiableSet(CONFIGS.keySet());
	}

	private static Map<String, MapEventConfig> createConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(DRAGON_LAND_DEFAULT, createDragonLandDefaultConfig());
		configs.put(KIKAREUKIN_ANGEL_PREVIEW, createKikareukinPreviewConfig());
		configs.put(KOSCIELISKO_GIANT_ESCORT, createKoscieliskoGiantEscortConfig());
		return configs;
	}

	private static MapEventConfig createKoscieliskoGiantEscortConfig() {
		final Duration duration = Duration.ofMinutes(20);
		return MapEventConfig.builder("Eskorta Wielkoluda")
				.duration(duration)
				.zones(Arrays.asList("0_koscielisko_ne"))
				.observerZones(Arrays.asList("0_koscielisko_ne"))
				.creatureFilter(new LinkedHashSet<>(Arrays.asList(
						"elf górski maskotka",
						"elf górski służka",
						"elf górski dama",
						"pokutnik z gór",
						"pokutnik nocny",
						"pokutnik wieczorny",
						"lawina"
				)))
				.waves(buildKoscieliskoEscortWaves(duration))
				.startAnnouncement("Halny niesie zgrzyt stali - Wielkolud rusza przez Kościelisko. Trzymajcie szlak.")
				.stopAnnouncement("Szlak cichnie. Los Wielkoluda został przesądzony.")
				.announcementIntervalSeconds(180)
				.giantOnlyAggro(true)
				// This escort event starts from scheduler/script only, so kill-trigger stays disabled.
				.triggerThreshold(0)
				.defaultStartTime("20:00")
				.defaultIntervalDays(2)
				.build();
	}

	private static List<BaseMapEvent.EventWave> buildKoscieliskoEscortWaves(final Duration eventDuration) {
		final int intervalSeconds = 20;
		final long durationSeconds = eventDuration.getSeconds();
		final int waveCount = (int) ((durationSeconds - 1L) / intervalSeconds);
		final List<BaseMapEvent.EventWave> waves = new ArrayList<>(waveCount);

		for (int waveNumber = 1; waveNumber <= waveCount; waveNumber++) {
			final double progress = (double) waveNumber / waveCount;
			final int escalationLevel = (waveNumber - 1) / 4;
			final List<BaseMapEvent.EventSpawn> spawns;
			if (progress <= 0.35d) {
				final int maskotka = Math.max(2, 6 - (escalationLevel / 2));
				final int sluzka = Math.max(2, 4 - (escalationLevel / 3));
				final int dama = 2 + (escalationLevel / 4);
				final int strazniczka = 1 + (escalationLevel / 3);
				final int wojownik = 1 + (escalationLevel / 3);
				final int lider = 1 + (escalationLevel / 4);
				final int czarownica = 1 + (escalationLevel / 5);
				spawns = Arrays.asList(
						new BaseMapEvent.EventSpawn("elf górski maskotka", maskotka),
						new BaseMapEvent.EventSpawn("elf górski służka", sluzka),
						new BaseMapEvent.EventSpawn("elf górski dama", dama),
						new BaseMapEvent.EventSpawn("elf górski strażniczka", strazniczka),
						new BaseMapEvent.EventSpawn("elf górski wojownik", wojownik),
						new BaseMapEvent.EventSpawn("elf górski lider", lider),
						new BaseMapEvent.EventSpawn("elf górski czarownica", czarownica)
				);
			} else if (progress <= 0.70d) {
				final int sluzka = Math.max(1, 3 - (escalationLevel / 5));
				final int dama = 2 + (escalationLevel / 4);
				final int strazniczka = 2 + (escalationLevel / 4);
				final int wojownik = 2 + (escalationLevel / 3);
				final int lider = 1 + (escalationLevel / 4);
				final int kaplan = 1 + (escalationLevel / 5);
				final int czarownica = 1 + (escalationLevel / 5);
				final int czarnoksieznik = 1 + (escalationLevel / 6);
				final int pokutnikBagna = 2 + (escalationLevel / 2);
				final int pokutnikWrzosy = 2 + (escalationLevel / 2);
				final int pokutnikLaki = 1 + (escalationLevel / 3);
				final int pokutnikPoranny = 1 + (escalationLevel / 4);
				spawns = Arrays.asList(
						new BaseMapEvent.EventSpawn("elf górski służka", sluzka),
						new BaseMapEvent.EventSpawn("elf górski dama", dama),
						new BaseMapEvent.EventSpawn("elf górski strażniczka", strazniczka),
						new BaseMapEvent.EventSpawn("elf górski wojownik", wojownik),
						new BaseMapEvent.EventSpawn("elf górski lider", lider),
						new BaseMapEvent.EventSpawn("elf górski kapłan", kaplan),
						new BaseMapEvent.EventSpawn("elf górski czarownica", czarownica),
						new BaseMapEvent.EventSpawn("elf górski czarnoksiężnik", czarnoksieznik),
						new BaseMapEvent.EventSpawn("pokutnik z bagien", pokutnikBagna),
						new BaseMapEvent.EventSpawn("pokutnik z wrzosowisk", pokutnikWrzosy),
						new BaseMapEvent.EventSpawn("pokutnik z łąk", pokutnikLaki),
						new BaseMapEvent.EventSpawn("pokutnik poranny", pokutnikPoranny)
				);
			} else {
				final int pokutnikBagna = 3 + (escalationLevel / 3);
				final int pokutnikWrzosy = 3 + (escalationLevel / 3);
				final int pokutnikLaki = 3 + (escalationLevel / 3);
				final int pokutnikPoranny = 2 + (escalationLevel / 4);
				final int pokutnikZGor = 4 + (escalationLevel / 3);
				final int pokutnikNocny = 3 + (escalationLevel / 4);
				final int pokutnikWieczorny = 3 + (escalationLevel / 4);
				final int jozinLesa = 1 + (escalationLevel / 6);
				final int jozinBazin = Math.min(2, 1 + (escalationLevel / 9));
				final int lawina = Math.min(2, 1 + (escalationLevel / 8));
				spawns = Arrays.asList(
						new BaseMapEvent.EventSpawn("pokutnik z bagien", pokutnikBagna),
						new BaseMapEvent.EventSpawn("pokutnik z wrzosowisk", pokutnikWrzosy),
						new BaseMapEvent.EventSpawn("pokutnik z łąk", pokutnikLaki),
						new BaseMapEvent.EventSpawn("pokutnik poranny", pokutnikPoranny),
						new BaseMapEvent.EventSpawn("pokutnik z gór", pokutnikZGor),
						new BaseMapEvent.EventSpawn("pokutnik nocny", pokutnikNocny),
						new BaseMapEvent.EventSpawn("pokutnik wieczorny", pokutnikWieczorny),
						new BaseMapEvent.EventSpawn("Jožin z lesa", jozinLesa),
						new BaseMapEvent.EventSpawn("Jožin z bažin", jozinBazin),
						new BaseMapEvent.EventSpawn("lawina", lawina)
				);
			}
			waves.add(new BaseMapEvent.EventWave(intervalSeconds, spawns));
		}

		return waves;
	}

	private static MapEventConfig createDragonLandDefaultConfig() {
		return MapEventConfig.builder("Dragon Land")
				.duration(Duration.ofMinutes(60))
				.zones(Arrays.asList(
						"0_dragon_land_n",
						"0_dragon_land_s"
				))
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
						"-1_dragon_cave"
				))
				.creatureFilter(new LinkedHashSet<>(Arrays.asList(
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
						"Smok Wawelski"
				)))
				.waves(Arrays.asList(
						new BaseMapEvent.EventWave(30, Arrays.asList(
								new BaseMapEvent.EventSpawn("zgniły szkielet smoka", 6),
								new BaseMapEvent.EventSpawn("pustynny smok", 4),
								new BaseMapEvent.EventSpawn("zielony smok", 4),
								new BaseMapEvent.EventSpawn("czerwony smok", 2),
								new BaseMapEvent.EventSpawn("błękitny smok", 4)
						)),
						new BaseMapEvent.EventWave(45, Arrays.asList(
								new BaseMapEvent.EventSpawn("lodowy smok", 4),
								new BaseMapEvent.EventSpawn("smok arktyczny", 3),
								new BaseMapEvent.EventSpawn("dwugłowy zielony smok", 6)
						)),
						new BaseMapEvent.EventWave(60, Arrays.asList(
								new BaseMapEvent.EventSpawn("dwugłowy złoty smok", 4),
								new BaseMapEvent.EventSpawn("dwugłowy zielony smok", 8),
								new BaseMapEvent.EventSpawn("dwugłowy czerwony smok", 6)
						)),
						new BaseMapEvent.EventWave(90, Arrays.asList(
								new BaseMapEvent.EventSpawn("dwugłowy złoty smok", 2),
								new BaseMapEvent.EventSpawn("zielone smoczysko", 3),
								new BaseMapEvent.EventSpawn("niebieskie smoczysko", 3)
						)),
						new BaseMapEvent.EventWave(120, Arrays.asList(
								new BaseMapEvent.EventSpawn("dwugłowy czarny smok", 4),
								new BaseMapEvent.EventSpawn("dwugłowy lodowy smok", 8)
						)),
						new BaseMapEvent.EventWave(150, Arrays.asList(
								new BaseMapEvent.EventSpawn("czerwone smoczysko", 3),
								new BaseMapEvent.EventSpawn("czarne smoczysko", 2)
						)),
						new BaseMapEvent.EventWave(180, Arrays.asList(
								new BaseMapEvent.EventSpawn("latający czarny smok", 1),
								new BaseMapEvent.EventSpawn("latający złoty smok", 1)
						)),
						new BaseMapEvent.EventWave(240, Arrays.asList(
								new BaseMapEvent.EventSpawn("Smok Wawelski", 1)
						))
				))
				.announcements(Arrays.asList(
						"Niebo przeszywa skrzek smoków - smocze stado krąży nad krainą.",
						"Z oddali dobiega trzepot skrzydeł i syk ognia - smoki nie odpuszczają.",
						"Smocza kraina drży pod ciężarem bestii, które krążą nad ziemią."
				))
				.startAnnouncement("Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie.")
				.stopAnnouncement("Smocza kraina uspokaja się. Wydarzenie dobiegło końca.")
				.announcementIntervalSeconds(600)
				.weatherLock(new MapEventConfig.WeatherLockConfig("fog", false))
				.triggerThreshold(500)
				.defaultStartTime("20:00")
				.defaultIntervalDays(2)
				.build();
	}

	private static MapEventConfig createKikareukinPreviewConfig() {
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
