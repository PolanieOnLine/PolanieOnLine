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
package games.stendhal.server.maps.event;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.MapEventStatusEvent;

/**
 * Publishes map event status snapshots for clients.
 */
public final class MapEventStatusPublisher {
	private static final String FALLBACK_EVENT_ID = "none";
	private static final String FALLBACK_EVENT_NAME = "";

	private MapEventStatusPublisher() {
		// utility class
	}

	public static void broadcastActiveEventStatus(final BaseMapEvent event) {
		if (event == null || !event.isEventActivePublic()) {
			return;
		}
		final List<String> allowedZones = resolveAllowedZones(event);
		if (allowedZones.isEmpty()) {
			return;
		}
		for (Player player : SingletonRepository.getRuleProcessor().getOnlinePlayers().getAllPlayers()) {
			if (shouldReceive(player, allowedZones)) {
				player.addEvent(createActiveSnapshot(event, allowedZones));
			}
		}
	}

	public static void broadcastInactiveEventStatus(final BaseMapEvent event) {
		if (event == null) {
			return;
		}
		final List<String> allowedZones = resolveAllowedZones(event);
		for (Player player : SingletonRepository.getRuleProcessor().getOnlinePlayers().getAllPlayers()) {
			if (shouldReceive(player, allowedZones)) {
				player.addEvent(createInactiveSnapshot(event));
			}
		}
	}

	public static void sendImmediateSnapshot(final Player player) {
		if (player == null || player.getZone() == null) {
			return;
		}
		boolean sentAnyActiveEvent = false;
		for (ConfiguredMapEvent event : MapEventRegistry.listEvents()) {
			if (!event.isEventActivePublic()) {
				continue;
			}
			final List<String> allowedZones = resolveAllowedZones(event);
			if (shouldReceive(player, allowedZones)) {
				player.addEvent(createActiveSnapshot(event, allowedZones));
				sentAnyActiveEvent = true;
			}
		}
		if (!sentAnyActiveEvent) {
			player.addEvent(createGlobalFallbackSnapshot());
		}
	}

	private static MapEventStatusEvent createActiveSnapshot(final BaseMapEvent event, final List<String> allowedZones) {
		final int totalDurationSeconds = event.getTotalDurationSeconds();
		final int remainingSeconds = event.getRemainingSeconds();
		return new MapEventStatusEvent(
				event.getEventId(),
				event.getEventNamePublic(),
				true,
				Integer.valueOf(remainingSeconds),
				Integer.valueOf(totalDurationSeconds),
				Integer.valueOf(event.getEventTotalSpawnedCreatures()),
				Integer.valueOf(event.getEventDefeatedCreatures()),
				Integer.valueOf(event.getEventDefeatPercent()),
				Integer.valueOf(event.getCurrentWave()),
				Integer.valueOf(event.getTotalWaves()),
				event.getDefenseStatus(),
				event.getActivityTop(),
				allowedZones);
	}

	private static MapEventStatusEvent createInactiveSnapshot(final BaseMapEvent event) {
		return new MapEventStatusEvent(event.getEventId(), event.getEventNamePublic(), false,
				null, null, Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0),
				Integer.valueOf(0), Integer.valueOf(0), "Wydarzenie zakończone",
				Collections.<String>emptyList(), Collections.<String>emptyList());
	}

	private static MapEventStatusEvent createGlobalFallbackSnapshot() {
		return new MapEventStatusEvent(FALLBACK_EVENT_ID, FALLBACK_EVENT_NAME, false,
				null, null, Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0),
				Integer.valueOf(0), Integer.valueOf(0), "Wydarzenie zakończone",
				Collections.<String>emptyList(), Collections.<String>emptyList());
	}

	private static boolean shouldReceive(final Player player, final List<String> allowedZones) {
		if (player == null || player.getZone() == null) {
			return false;
		}
		return allowedZones.contains(player.getZone().getName());
	}

	private static List<String> resolveAllowedZones(final BaseMapEvent event) {
		final Set<String> zones = new LinkedHashSet<>();
		zones.addAll(event.getZonesPublic());

		final StendhalRPWorld world = SingletonRepository.getRPWorld();
		final Set<String> regions = findRegionsForZones(world, zones);
		for (String region : regions) {
			final Collection<StendhalRPZone> regionalZones = world.getAllZonesFromRegion(region, null, null, null);
			for (StendhalRPZone zone : regionalZones) {
				zones.add(zone.getName());
			}
		}

		final List<String> ordered = new ArrayList<>(zones);
		Collections.sort(ordered);
		return ordered;
	}

	private static Set<String> findRegionsForZones(final StendhalRPWorld world, final Set<String> zoneNames) {
		final Set<String> matchedRegions = new LinkedHashSet<>();
		if (zoneNames.isEmpty()) {
			return matchedRegions;
		}
		for (String region : world.getRegions()) {
			for (StendhalRPZone zone : world.getAllZonesFromRegion(region, null, null, null)) {
				if (zoneNames.contains(zone.getName())) {
					matchedRegions.add(region);
					break;
				}
			}
		}
		return matchedRegions;
	}
}
