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

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.maps.event.BaseMapEvent;
import games.stendhal.server.maps.event.KillThresholdTrigger;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;

public class DragonLandEvent extends BaseMapEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);
	private static final MapEventConfig EVENT_CONFIG = MapEventConfigLoader.load(MapEventConfigLoader.DRAGON_LAND_DEFAULT);
	private static final DragonLandEvent INSTANCE = new DragonLandEvent();

	private final AtomicBoolean wawelAnnounced = new AtomicBoolean(false);
	private final KillThresholdTrigger killThresholdTrigger;
	private static final int SPAWN_ATTEMPTS_PER_CREATURE = 20;

	private DragonLandEvent() {
		super(LOGGER, EVENT_CONFIG);
		killThresholdTrigger = new KillThresholdTrigger(
				getObserverZones(),
				circs -> !isEventActive() && getCreatureFilter().contains(circs.getVictim().getName()),
				getConfig().getTriggerThreshold(),
				this::startEventFromKills);
	}

	public static void registerZoneObserver(final StendhalRPZone zone) {
		INSTANCE.registerZoneObserverInternal(zone);
	}

	public static void scheduleEveryTwoDaysAt(LocalTime time) {
		INSTANCE.scheduleEveryDaysAt(time, EVENT_CONFIG.getGuaranteedIntervalDays());
	}

	public static void scheduleGuaranteedEveryDaysAt(final LocalTime time, final int intervalDays) {
		INSTANCE.scheduleEveryDaysAt(time, intervalDays);
	}

	public static boolean forceStart() {
		return INSTANCE.forceStartInternal();
	}

	private void registerZoneObserverInternal(final StendhalRPZone zone) {
		killThresholdTrigger.registerZoneObserver(Objects.requireNonNull(zone, "zone"));
	}

	private boolean forceStartInternal() {
		if (!startEvent()) {
			LOGGER.warn("Dragon Land event already active; refusing forced start.");
			return false;
		}
		return true;
	}

	private void startEventFromKills() {
		if (!startEvent()) {
			LOGGER.warn("Dragon Land event already active; skipping duplicate start.");
		}
	}

	private void summonCreatures(final String creatureName, final int count) {
		final int zoneCount = getZones().size();
		if (zoneCount == 0) {
			LOGGER.warn("Dragon Land zones list is empty; cannot spawn " + creatureName + ".");
			return;
		}
		for (int zoneIndex = 0; zoneIndex < zoneCount; zoneIndex++) {
			summonCreaturesInZone(getZones().get(zoneIndex), creatureName, count);
		}
	}

	private void summonCreaturesInZone(final String zoneName, final String creatureName, final int count) {
		for (int i = 0; i < count; i++) {
			final Creature template = SingletonRepository.getEntityManager().getCreature(creatureName);
			if (template == null) {
				LOGGER.warn("Dragon Land event missing creature template: " + creatureName + ".");
				continue;
			}
			final Creature creature = new Creature(template.getNewInstance());
			if (placeCreatureInRandomSafeSpot(creature, zoneName)) {
				registerEventCreature(creature);
				LOGGER.debug("Dragon Land spawned " + creature.getName() + " in zone " + zoneName + ".");
			}
		}
	}

	private boolean placeCreatureInRandomSafeSpot(final Creature creature, final String zoneName) {
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

	private void announceWawelSpawn() {
		if (!isEventActive()) {
			return;
		}
		if (!wawelAnnounced.compareAndSet(false, true)) {
			return;
		}
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"W oddali słychać ryk… Smok Wawelski pojawił się w Smoczej Krainie!"
		);
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

	@Override
	protected void onStart() {
		wawelAnnounced.set(false);
		killThresholdTrigger.resetCounter("event started");
		LOGGER.info("Dragon Land event started.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie."
		);
	}

	@Override
	protected void onStop() {
		killThresholdTrigger.resetCounter("event ended");
		LOGGER.info("Dragon Land event ended.");
		removeEventCreatures();
		stopAnnouncements();
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Smocza kraina uspokaja się. Wydarzenie dobiegło końca."
		);
	}

	@Override
	protected void spawnCreatures(final String creatureName, final int count) {
		summonCreatures(creatureName, count);
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		if ("Smok Wawelski".equals(spawn.getCreatureName())) {
			announceWawelSpawn();
		}
	}
}
