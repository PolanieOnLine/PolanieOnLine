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
package games.stendhal.server.maps.koscielisko;

import java.time.Duration;
import java.util.ArrayList;
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

public class KoscieliskoMapEventConfigProvider implements MapEventConfigProvider {
	public static final String KOSCIELISKO_GIANT_ESCORT = "koscielisko_giant_escort";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(KOSCIELISKO_GIANT_ESCORT, createKoscieliskoGiantEscortConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createKoscieliskoGiantEscortConfig() {
		// Balance domain note: Kościelisko escort should keep steady lane pressure so teams
		// can rotate around the giant, with a harsher final stretch near event timeout.
		final Duration duration = Duration.ofMinutes(20);
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("20:00", KOSCIELISKO_GIANT_ESCORT);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(2, KOSCIELISKO_GIANT_ESCORT);

		return MapEventConfig.builder("Eskorta Wielkoluda")
				// Core config
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
				// Messages
				.startAnnouncement("Halny niesie zgrzyt stali - Wielkolud rusza przez Kościelisko. Trzymajcie szlak.")
				.stopAnnouncement("Szlak cichnie. Los Wielkoluda został przesądzony.")
				.announcementIntervalSeconds(180)
				// Waves
				.waves(buildKoscieliskoEscortWaves(duration))
				// Triggers / scheduler
				.escortSettings(MapEventConfig.EscortSettings.builder()
						.giantHp(32000)
						.giantDefBonus(220)
						.resistance(40)
						.hardCap(34)
						.waveBudgetBase(10)
						.waveBudgetPerStage(4)
						.cooldownMinutes(45)
						.build())
				.giantOnlyAggro(true)
				// This escort event starts from scheduler/script only, so kill-trigger stays disabled.
				.triggerThreshold(0)
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}

	private List<BaseMapEvent.EventWave> buildKoscieliskoEscortWaves(final Duration eventDuration) {
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
						MapEventConfigSupport.spawn("elf górski maskotka", maskotka),
						MapEventConfigSupport.spawn("elf górski służka", sluzka),
						MapEventConfigSupport.spawn("elf górski dama", dama),
						MapEventConfigSupport.spawn("elf górski strażniczka", strazniczka),
						MapEventConfigSupport.spawn("elf górski wojownik", wojownik),
						MapEventConfigSupport.spawn("elf górski lider", lider),
						MapEventConfigSupport.spawn("elf górski czarownica", czarownica)
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
						MapEventConfigSupport.spawn("elf górski służka", sluzka),
						MapEventConfigSupport.spawn("elf górski dama", dama),
						MapEventConfigSupport.spawn("elf górski strażniczka", strazniczka),
						MapEventConfigSupport.spawn("elf górski wojownik", wojownik),
						MapEventConfigSupport.spawn("elf górski lider", lider),
						MapEventConfigSupport.spawn("elf górski kapłan", kaplan),
						MapEventConfigSupport.spawn("elf górski czarownica", czarownica),
						MapEventConfigSupport.spawn("elf górski czarnoksiężnik", czarnoksieznik),
						MapEventConfigSupport.spawn("pokutnik z bagien", pokutnikBagna),
						MapEventConfigSupport.spawn("pokutnik z wrzosowisk", pokutnikWrzosy),
						MapEventConfigSupport.spawn("pokutnik z łąk", pokutnikLaki),
						MapEventConfigSupport.spawn("pokutnik poranny", pokutnikPoranny)
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
						MapEventConfigSupport.spawn("pokutnik z bagien", pokutnikBagna),
						MapEventConfigSupport.spawn("pokutnik z wrzosowisk", pokutnikWrzosy),
						MapEventConfigSupport.spawn("pokutnik z łąk", pokutnikLaki),
						MapEventConfigSupport.spawn("pokutnik poranny", pokutnikPoranny),
						MapEventConfigSupport.spawn("pokutnik z gór", pokutnikZGor),
						MapEventConfigSupport.spawn("pokutnik nocny", pokutnikNocny),
						MapEventConfigSupport.spawn("pokutnik wieczorny", pokutnikWieczorny),
						MapEventConfigSupport.spawn("Jožin z lesa", jozinLesa),
						MapEventConfigSupport.spawn("Jožin z bažin", jozinBazin),
						MapEventConfigSupport.spawn("lawina", lawina)
				);
			}
			waves.add(MapEventConfigSupport.wave(intervalSeconds, spawns.toArray(new BaseMapEvent.EventSpawn[0])));
		}

		return waves;
	}
}
