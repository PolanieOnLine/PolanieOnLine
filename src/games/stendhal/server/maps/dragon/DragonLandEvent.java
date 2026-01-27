/***************************************************************************
 *                    Copyright © 2024 - PolanieOnLine                    *
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.ZoneAttributes;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.core.rp.WeatherUpdater;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.WeatherEntity;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.util.Observable;
import games.stendhal.server.util.Observer;
import marauroa.common.Pair;

public class DragonLandEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);

	private static final Duration EVENT_DURATION = Duration.ofMinutes(45);
	private static final AtomicBoolean SCHEDULED = new AtomicBoolean(false);
	private static final AtomicBoolean EVENT_ACTIVE = new AtomicBoolean(false);
	private static final AtomicInteger DRAGON_KILL_COUNT = new AtomicInteger(0);
	private static final int DRAGON_KILL_THRESHOLD = 25;
	private static final List<String> DRAGON_TYPES = Arrays.asList(
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
	);
	private static final Set<String> DRAGON_ZONES = new HashSet<>(Arrays.asList(
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
	));
	private static final List<String> DRAGON_LAND_ZONES = Arrays.asList(
			"0_dragon_land_n",
			"0_dragon_land_s"
	);
	private static final DragonDeathObserver DRAGON_DEATH_OBSERVER = new DragonDeathObserver();
	private static final EventDragonObserver EVENT_DRAGON_OBSERVER = new EventDragonObserver();
	private static final List<Creature> EVENT_DRAGONS = Collections.synchronizedList(new ArrayList<>());
	private static final Map<String, Pair<String, Boolean>> STORED_WEATHER = new HashMap<>();
	private static final int FOG_REFRESH_INTERVAL_SECONDS = 60;
	private static final AtomicInteger FOG_REFRESH_TOKEN = new AtomicInteger(0);
	private static final int SPAWN_ATTEMPTS_PER_CREATURE = 20;
	private static final List<DragonWave> DRAGON_WAVES = Arrays.asList(
			new DragonWave(30, Arrays.asList(
					new DragonSpawn("zgniły szkielet smoka", 6),
					new DragonSpawn("pustynny smok", 4),
					new DragonSpawn("zielony smok", 4),
					new DragonSpawn("czerwony smok", 2),
					new DragonSpawn("błękitny smok", 4)
			)),
			new DragonWave(45, Arrays.asList(
					new DragonSpawn("lodowy smok", 4),
					new DragonSpawn("smok arktyczny", 3),
					new DragonSpawn("dwugłowy zielony smok", 6)
			)),
			new DragonWave(60, Arrays.asList(
					new DragonSpawn("dwugłowy złoty smok", 4),
					new DragonSpawn("dwugłowy zielony smok", 8),
					new DragonSpawn("dwugłowy czerwony smok", 6)
			)),
			new DragonWave(90, Arrays.asList(
					new DragonSpawn("dwugłowy złoty smok", 2),
					new DragonSpawn("zielone smoczysko", 3),
					new DragonSpawn("niebieskie smoczysko", 3)
			)),
			new DragonWave(120, Arrays.asList(
					new DragonSpawn("dwugłowy czarny smok", 4),
					new DragonSpawn("dwugłowy lodowy smok", 8)
			)),
			new DragonWave(150, Arrays.asList(
					new DragonSpawn("czerwone smoczysko", 3),
					new DragonSpawn("czarne smoczysko", 2)
			)),
			new DragonWave(180, Arrays.asList(
					new DragonSpawn("latający czarny smok", 1),
					new DragonSpawn("latający złoty smok", 1)
			)),
			new DragonWave(240, Arrays.asList(
					new DragonSpawn("Smok Wawelski", 1)
			))
	);

	private static volatile LocalTime scheduledTime;

	public static void registerZoneObserver(final StendhalRPZone zone) {
		Objects.requireNonNull(zone, "zone");
		if (!DRAGON_ZONES.contains(zone.getName())) {
			return;
		}
		for (CreatureRespawnPoint respawnPoint : zone.getRespawnPointList()) {
			if (respawnPoint != null
					&& DRAGON_TYPES.contains(respawnPoint.getPrototypeCreature().getName())) {
				respawnPoint.addObserver(DRAGON_DEATH_OBSERVER);
			}
		}
	}

	public static void scheduleDailyAt(LocalTime time) {
		Objects.requireNonNull(time, "time");
		if (!SCHEDULED.compareAndSet(false, true)) {
			LOGGER.debug("Dragon Land event already scheduled; skipping duplicate schedule.");
			return;
		}
		scheduledTime = time;
		scheduleNextRun(time);
	}

	private static void scheduleNextRun(LocalTime time) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startTime = LocalDate.now().atTime(time);
		if (startTime.isBefore(now)) {
			startTime = startTime.plusDays(1);
		}
		int waitSec = (int) Duration.between(now, startTime).getSeconds();
		LOGGER.info("Dragon Land event scheduled in " + waitSec + " seconds at " + startTime + ".");
		SingletonRepository.getTurnNotifier().notifyInSeconds(waitSec, currentTurn -> startEvent());
	}

	private static void startEvent() {
		if (!EVENT_ACTIVE.compareAndSet(false, true)) {
			LOGGER.warn("Dragon Land event already active; skipping duplicate start.");
			return;
		}
		startEventInternal();
	}

	public static boolean forceStart() {
		if (!EVENT_ACTIVE.compareAndSet(false, true)) {
			LOGGER.warn("Dragon Land event already active; refusing forced start.");
			return false;
		}
		startEventInternal();
		return true;
	}

	private static void startEventInternal() {
		resetKillCounter("event started");
		LOGGER.info("Dragon Land event started.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie."
		);
		forceFoggyWeather();
		scheduleWaves();
		int seconds = (int) EVENT_DURATION.getSeconds();
		SingletonRepository.getTurnNotifier().notifyInSeconds(seconds, currentTurn -> endEvent());
	}

	private static void endEvent() {
		if (!EVENT_ACTIVE.compareAndSet(true, false)) {
			return;
		}
		stopFogRefresh();
		resetKillCounter("event ended");
		LOGGER.info("Dragon Land event ended.");
		removeEventDragons();
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Smocza kraina uspokaja się. Wydarzenie dobiegło końca."
		);
		restoreWeather();
		scheduleNextRun(scheduledTime);
	}

	private static void resetKillCounter(final String reason) {
		int previous = DRAGON_KILL_COUNT.getAndSet(0);
		LOGGER.debug("Dragon Land kill counter reset (" + reason + ") from " + previous + ".");
	}

	private static void recordDragonDeath(final CircumstancesOfDeath circs) {
		if (EVENT_ACTIVE.get()) {
			return;
		}
		if (!DRAGON_ZONES.contains(circs.getZone().getName())) {
			return;
		}
		final String victimName = circs.getVictim().getName();
		if (!DRAGON_TYPES.contains(victimName)) {
			return;
		}
		int current = DRAGON_KILL_COUNT.incrementAndGet();
		if (current >= DRAGON_KILL_THRESHOLD) {
			LOGGER.info("Dragon Land kill threshold reached: " + current + ".");
			startEvent();
		}
	}

	private static void forceFoggyWeather() {
		STORED_WEATHER.clear();
		final WeatherUpdater updater = WeatherUpdater.get();
		final int refreshToken = FOG_REFRESH_TOKEN.incrementAndGet();
		for (final String zoneName : DRAGON_LAND_ZONES) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				LOGGER.warn("Dragon Land zone not found for weather update: " + zoneName + ".");
				continue;
			}
			final ZoneAttributes attributes = zone.getAttributes();
			final WeatherEntity weatherEntity = zone.getWeatherEntity();
			if (attributes == null || weatherEntity == null) {
				LOGGER.warn("Dragon Land zone lacks weather support: " + zoneName + ".");
				continue;
			}
			final String currentWeather = attributes.get("weather");
			STORED_WEATHER.put(zoneName, new Pair<>(currentWeather, weatherEntity.isThundering()));
			updater.updateAndNotify(zone, new Pair<>("fog", Boolean.FALSE));
		}
		scheduleFogRefresh(refreshToken);
	}

	private static void restoreWeather() {
		if (STORED_WEATHER.isEmpty()) {
			return;
		}
		final WeatherUpdater updater = WeatherUpdater.get();
		for (final Map.Entry<String, Pair<String, Boolean>> entry : STORED_WEATHER.entrySet()) {
			final String zoneName = entry.getKey();
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				LOGGER.warn("Dragon Land zone not found for weather restore: " + zoneName + ".");
				continue;
			}
			if (zone.getWeatherEntity() == null) {
				LOGGER.warn("Dragon Land zone lacks weather support: " + zoneName + ".");
				continue;
			}
			updater.updateAndNotify(zone, entry.getValue());
		}
		STORED_WEATHER.clear();
	}

	private static void scheduleFogRefresh(final int refreshToken) {
		SingletonRepository.getTurnNotifier().notifyInSeconds(
				FOG_REFRESH_INTERVAL_SECONDS,
				currentTurn -> refreshFoggyWeather(refreshToken)
		);
	}

	private static void refreshFoggyWeather(final int refreshToken) {
		if (!EVENT_ACTIVE.get() || refreshToken != FOG_REFRESH_TOKEN.get()) {
			return;
		}
		final WeatherUpdater updater = WeatherUpdater.get();
		for (final String zoneName : DRAGON_LAND_ZONES) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				LOGGER.warn("Dragon Land zone not found for fog refresh: " + zoneName + ".");
				continue;
			}
			if (zone.getWeatherEntity() == null) {
				LOGGER.warn("Dragon Land zone lacks weather support for fog refresh: " + zoneName + ".");
				continue;
			}
			updater.updateAndNotify(zone, new Pair<>("fog", Boolean.FALSE));
		}
		scheduleFogRefresh(refreshToken);
	}

	private static void stopFogRefresh() {
		FOG_REFRESH_TOKEN.incrementAndGet();
	}

	private static void scheduleWaves() {
		int delaySeconds = 0;
		for (DragonWave wave : DRAGON_WAVES) {
			delaySeconds += wave.intervalSeconds;
			final int scheduledDelay = delaySeconds;
			SingletonRepository.getTurnNotifier().notifyInSeconds(
					scheduledDelay,
					currentTurn -> summonWave(wave)
			);
		}
	}

	private static void summonWave(final DragonWave wave) {
		if (!EVENT_ACTIVE.get()) {
			return;
		}
		for (DragonSpawn spawn : wave.spawns) {
			summonCreatures(spawn.creatureName, spawn.count);
		}
	}

	private static void summonCreatures(final String creatureName, final int count) {
		final int zoneCount = DRAGON_LAND_ZONES.size();
		if (zoneCount == 0) {
			LOGGER.warn("Dragon Land zones list is empty; cannot spawn " + creatureName + ".");
			return;
		}
		for (int zoneIndex = 0; zoneIndex < zoneCount; zoneIndex++) {
			summonCreaturesInZone(DRAGON_LAND_ZONES.get(zoneIndex), creatureName, count);
		}
	}

	private static void summonCreaturesInZone(final String zoneName, final String creatureName, final int count) {
		for (int i = 0; i < count; i++) {
			final Creature template = SingletonRepository.getEntityManager().getCreature(creatureName);
			if (template == null) {
				LOGGER.warn("Dragon Land event missing creature template: " + creatureName + ".");
				continue;
			}
			final Creature creature = new Creature(template.getNewInstance());
			creature.registerObjectsForNotification(EVENT_DRAGON_OBSERVER);
			if (placeCreatureInRandomSafeSpot(creature, zoneName)) {
				EVENT_DRAGONS.add(creature);
				LOGGER.debug("Dragon Land spawned " + creature.getName() + " in zone " + zoneName + ".");
			}
		}
	}

	private static boolean placeCreatureInRandomSafeSpot(final Creature creature, final String zoneName) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
		if (zone == null) {
			LOGGER.warn("Dragon Land zone not found for spawn: " + zoneName + ".");
			return false;
		}
		final int[] centerAnchor = zone.getName().startsWith("0")
				? findNearestPassableCenter(zone, creature)
				: null;
		if (zone.getName().startsWith("0") && centerAnchor == null) {
			LOGGER.warn("Dragon Land spawn path anchor not found; skipping path validation for " + zoneName + ".");
		}
		for (int attempt = 0; attempt < SPAWN_ATTEMPTS_PER_CREATURE; attempt++) {
			final int x = Rand.rand(zone.getWidth());
			final int y = Rand.rand(zone.getHeight());
			if (zone.collides(creature, x, y)) {
				continue;
			}
			if (centerAnchor != null) {
				final List<Node> path = Path.searchPath(zone, x, y, centerAnchor[0], centerAnchor[1], (64 + 64) * 2);
				if (path == null || path.isEmpty()) {
					continue;
				}
			}
			if (StendhalRPAction.placeat(zone, creature, x, y)) {
				return true;
			}
		}
		LOGGER.debug("Dragon Land spawn failed after attempts for " + creature.getName() + ".");
		return false;
	}

	private static int[] findNearestPassableCenter(final StendhalRPZone zone, final Creature creature) {
		final int centerX = zone.getWidth() / 2;
		final int centerY = zone.getHeight() / 2;
		if (!zone.collides(creature, centerX, centerY)) {
			return new int[] { centerX, centerY };
		}
		final int maxRadius = Math.max(zone.getWidth(), zone.getHeight());
		for (int radius = 1; radius <= maxRadius; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				int x = centerX + dx;
				int y = centerY - radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
				y = centerY + radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
			}
			for (int dy = -radius + 1; dy <= radius - 1; dy++) {
				int y = centerY + dy;
				int x = centerX - radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
				x = centerX + radius;
				if (isPassableAt(zone, creature, x, y)) {
					return new int[] { x, y };
				}
			}
		}
		return null;
	}

	private static boolean isPassableAt(final StendhalRPZone zone, final Creature creature, final int x, final int y) {
		if (x < 0 || y < 0 || x >= zone.getWidth() || y >= zone.getHeight()) {
			return false;
		}
		return !zone.collides(creature, x, y);
	}

	private static void removeEventDragons() {
		synchronized (EVENT_DRAGONS) {
			for (Creature creature : EVENT_DRAGONS) {
				if (creature == null || creature.getZone() == null) {
					continue;
				}
				creature.stopAttack();
				creature.clearDropItemList();
				creature.getZone().remove(creature);
			}
			EVENT_DRAGONS.clear();
		}
	}

	private static class DragonDeathObserver implements Observer {
		@Override
		public void update(Observable obj, Object arg) {
			if (arg instanceof CircumstancesOfDeath) {
				recordDragonDeath((CircumstancesOfDeath) arg);
			}
		}
	}

	private static class EventDragonObserver implements Observer {
		@Override
		public void update(Observable obj, Object arg) {
			if (!(arg instanceof CircumstancesOfDeath)) {
				return;
			}
			final CircumstancesOfDeath circs = (CircumstancesOfDeath) arg;
			EVENT_DRAGONS.remove(circs.getVictim());
		}
	}

	private static class DragonSpawn {
		private final String creatureName;
		private final int count;

		private DragonSpawn(final String creatureName, final int count) {
			this.creatureName = creatureName;
			this.count = count;
		}
	}

	private static class DragonWave {
		private final int intervalSeconds;
		private final List<DragonSpawn> spawns;

		private DragonWave(final int intervalSeconds, final List<DragonSpawn> spawns) {
			this.intervalSeconds = intervalSeconds;
			this.spawns = spawns;
		}
	}
}
