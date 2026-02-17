/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
 ***************************************************************************
 ***************************************************************************
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

import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigProvider;
import games.stendhal.server.maps.event.MapEventConfigSupport;

public class TatryMapEventConfigProvider implements MapEventConfigProvider {
	public static final String TATRY_KUZNICE_BANDIT_RAID = "tatry_kuznice_bandit_raid";

	@Override
	public Map<String, MapEventConfig> loadConfigs() {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		configs.put(TATRY_KUZNICE_BANDIT_RAID, createTatryKuzniceBanditRaidConfig());
		return Collections.unmodifiableMap(configs);
	}

	private MapEventConfig createTatryKuzniceBanditRaidConfig() {
		// Balance domain note: Kuźnice should start with lighter scouting bands,
		// then escalate into mixed mountain squads that pressure both streets and interiors.
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
				.observerZones(Arrays.asList(
						"0_tatry_kuznice",
						"int_tatry_kuznice_blacksmith",
						"int_tatry_kuznice_tavern",
						"int_tatry_kuznice_chapel",
						"int_tatry_kuznice_hostel"))
				.creatureFilter(new LinkedHashSet<>(Arrays.asList(
						"zbójnik leśny",
						"zbójnik leśny oszust",
						"zbójnik leśny zwiadowca",
						"zbójnik górski",
						"zbójnik górski goniec",
						"zbójnik górski złośliwy",
						"zbójnik górski zwiadowca",
						"zbójnik górski starszy",
						"zbójnik górski herszt")))
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
								MapEventConfigSupport.spawn("zbójnik leśny oszust", 5),
								MapEventConfigSupport.spawn("zbójnik leśny zwiadowca", 4)),
						MapEventConfigSupport.wave(60,
								MapEventConfigSupport.spawn("zbójnik leśny", 8),
								MapEventConfigSupport.spawn("zbójnik górski", 6),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 4),
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 3)),
						MapEventConfigSupport.wave(95,
								MapEventConfigSupport.spawn("zbójnik górski", 8),
								MapEventConfigSupport.spawn("zbójnik górski goniec", 4),
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 5),
								MapEventConfigSupport.spawn("zbójnik górski starszy", 2)),
						MapEventConfigSupport.wave(130,
								MapEventConfigSupport.spawn("zbójnik górski", 6),
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 6),
								MapEventConfigSupport.spawn("zbójnik górski zwiadowca", 4),
								MapEventConfigSupport.spawn("zbójnik górski herszt", 1)),
						MapEventConfigSupport.wave(170,
								MapEventConfigSupport.spawn("zbójnik górski goniec", 6),
								MapEventConfigSupport.spawn("zbójnik górski złośliwy", 7),
								MapEventConfigSupport.spawn("zbójnik górski starszy", 4),
								MapEventConfigSupport.spawn("zbójnik górski herszt", 2))))
				.triggerThreshold(0)
				.defaultStartTime(defaultStartTime)
				.defaultIntervalDays(defaultIntervalDays)
				.build();
	}
}
