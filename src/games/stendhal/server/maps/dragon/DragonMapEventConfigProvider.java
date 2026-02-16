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
package games.stendhal.server.maps.dragon;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;
import games.stendhal.server.maps.event.MapEventConfigSupport;

public class DragonMapEventConfigProvider implements MapEventConfigProvider {
	public static final String DRAGON_LAND_DEFAULT = "dragon_land_default";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(DRAGON_LAND_DEFAULT, createDragonLandDefaultConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createDragonLandDefaultConfig() {
		// Balance domain note: Dragon Land should ramp from broad pressure to elite dragons,
		// then finish with short boss windows to preserve raid pacing and map identity.
		final String defaultStartTime = MapEventConfigSupport.validatedDefaultStartTime("20:00", DRAGON_LAND_DEFAULT);
		final int defaultIntervalDays = MapEventConfigSupport.validatedDefaultIntervalDays(2, DRAGON_LAND_DEFAULT);

		return MapEventConfig.builder("Dragon Land")
				// Core config
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
				// Messages
				.announcements(Arrays.asList(
						"Niebo przeszywa skrzek smoków - smocze stado krąży nad krainą.",
						"Z oddali dobiega trzepot skrzydeł i syk ognia - smoki nie odpuszczają.",
						"Smocza kraina drży pod ciężarem bestii, które krążą nad ziemią."
				))
				.startAnnouncement("Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie.")
				.stopAnnouncement("Smocza kraina uspokaja się. Wydarzenie dobiegło końca.")
				.announcementIntervalSeconds(600)
				// Waves
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(30,
								MapEventConfigSupport.spawn("zgniły szkielet smoka", 6),
								MapEventConfigSupport.spawn("pustynny smok", 4),
								MapEventConfigSupport.spawn("zielony smok", 4),
								MapEventConfigSupport.spawn("czerwony smok", 2),
								MapEventConfigSupport.spawn("błękitny smok", 4)),
						MapEventConfigSupport.wave(45,
								MapEventConfigSupport.spawn("lodowy smok", 4),
								MapEventConfigSupport.spawn("smok arktyczny", 3),
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 6)),
						MapEventConfigSupport.wave(60,
								MapEventConfigSupport.spawn("dwugłowy złoty smok", 4),
								MapEventConfigSupport.spawn("dwugłowy zielony smok", 8),
								MapEventConfigSupport.spawn("dwugłowy czerwony smok", 6)),
						MapEventConfigSupport.wave(90,
								MapEventConfigSupport.spawn("dwugłowy złoty smok", 2),
								MapEventConfigSupport.spawn("zielone smoczysko", 3),
								MapEventConfigSupport.spawn("niebieskie smoczysko", 3)),
						MapEventConfigSupport.wave(120,
								MapEventConfigSupport.spawn("dwugłowy czarny smok", 4),
								MapEventConfigSupport.spawn("dwugłowy lodowy smok", 8)),
						MapEventConfigSupport.wave(150,
								MapEventConfigSupport.spawn("czerwone smoczysko", 3),
								MapEventConfigSupport.spawn("czarne smoczysko", 2)),
						MapEventConfigSupport.wave(180,
								MapEventConfigSupport.spawn("latający czarny smok", 1),
								MapEventConfigSupport.spawn("latający złoty smok", 1))))
				// Triggers / scheduler
				.weatherLock(new MapEventConfig.WeatherLockConfig("fog", false))
				.triggerThreshold(500)
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}
}
