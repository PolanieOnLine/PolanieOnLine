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

import games.stendhal.server.maps.event.BaseMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;

public class DragonMapEventConfigProvider implements MapEventConfigProvider {
	public static final String DRAGON_LAND_DEFAULT = "dragon_land_default";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(DRAGON_LAND_DEFAULT, createDragonLandDefaultConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createDragonLandDefaultConfig() {
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
}
