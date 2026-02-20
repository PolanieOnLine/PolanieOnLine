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

import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.pathfinder.Node;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.Creature;

public class RandomSafeSpotSpawnStrategy implements MapEventSpawnStrategy {
	private static final int SPAWN_ATTEMPTS_PER_CREATURE = 20;
	private static final int MAX_PATH_LENGTH = (64 + 64) * 2;

	private final Logger logger;

	public RandomSafeSpotSpawnStrategy(final Logger logger) {
		this.logger = logger;
	}

	@Override
	public void spawnCreatures(final String eventName, final List<String> zones, final String creatureName,
			final int count, final SpawnedCreatureHandler spawnedCreatureHandler) {
		spawnCreatures(eventName, zones, creatureName, count, null, spawnedCreatureHandler);
	}

	@Override
	public void spawnCreatures(final String eventName, final List<String> zones, final String creatureName,
			final int count, final SpawnAnchor spawnAnchor, final SpawnedCreatureHandler spawnedCreatureHandler) {
		if (zones.isEmpty()) {
			logger.warn(eventName + " zones list is empty; cannot spawn " + creatureName + ".");
			return;
		}
		for (final String zoneName : zones) {
			final SpawnAnchor zoneAnchor = spawnAnchor != null && zoneName.equals(spawnAnchor.getZone())
					? spawnAnchor
					: null;
			spawnCreaturesInZone(eventName, zoneName, creatureName, count, zoneAnchor, spawnedCreatureHandler);
		}
	}

	private void spawnCreaturesInZone(final String eventName, final String zoneName, final String creatureName,
			final int count, final SpawnAnchor spawnAnchor, final SpawnedCreatureHandler spawnedCreatureHandler) {
		for (int i = 0; i < count; i++) {
			final Creature template = SingletonRepository.getEntityManager().getCreature(creatureName);
			if (template == null) {
				logger.warn(eventName + " missing creature template: " + creatureName + ".");
				continue;
			}

			final Creature creature = new Creature(template.getNewInstance());
			if (placeCreatureInRandomSafeSpot(eventName, creature, zoneName, spawnAnchor)) {
				spawnedCreatureHandler.onSpawned(creature);
				logger.debug(eventName + " spawned " + creature.getName() + " in zone " + zoneName + ".");
			}
		}
	}

	private boolean placeCreatureInRandomSafeSpot(final String eventName, final Creature creature, final String zoneName,
			final SpawnAnchor spawnAnchor) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
		if (zone == null) {
			logger.warn(eventName + " zone not found for spawn: " + zoneName + ".");
			return false;
		}

		final int[] centerAnchor = zone.getName().startsWith("0") ? findNearestPassableCenter(zone, creature) : null;
		if (zone.getName().startsWith("0") && centerAnchor == null) {
			logger.warn(eventName + " spawn path anchor not found; skipping path validation for " + zoneName + ".");
		}

		if (spawnAnchor != null) {
			for (int attempt = 0; attempt < SPAWN_ATTEMPTS_PER_CREATURE; attempt++) {
				final int[] anchoredPoint = pickAnchoredPoint(zone, spawnAnchor);
				if (anchoredPoint == null) {
					break;
				}
				if (tryPlaceCreature(zone, creature, anchoredPoint[0], anchoredPoint[1], centerAnchor)) {
					return true;
				}
			}
		}

		for (int attempt = 0; attempt < SPAWN_ATTEMPTS_PER_CREATURE; attempt++) {
			final int x = Rand.rand(zone.getWidth());
			final int y = Rand.rand(zone.getHeight());
			if (tryPlaceCreature(zone, creature, x, y, centerAnchor)) {
				return true;
			}
		}
		logger.debug(eventName + " spawn failed after attempts for " + creature.getName() + ".");
		return false;
	}

	private static int[] pickAnchoredPoint(final StendhalRPZone zone, final SpawnAnchor spawnAnchor) {
		if (zone.getWidth() <= 0 || zone.getHeight() <= 0) {
			return null;
		}
		final int minRadius = Math.max(0, spawnAnchor.getMinRadius());
		final int maxRadius = Math.max(minRadius, spawnAnchor.getMaxRadius());
		if (maxRadius == 0) {
			return new int[] { spawnAnchor.getCenterX(), spawnAnchor.getCenterY() };
		}

		final double minRadiusSquared = minRadius * (double) minRadius;
		final double maxRadiusSquared = maxRadius * (double) maxRadius;
		final double randomRadius = Math.sqrt(minRadiusSquared + (Rand.rand() * (maxRadiusSquared - minRadiusSquared)));
		final double randomAngle = Rand.rand() * Math.PI * 2d;
		final int x = spawnAnchor.getCenterX() + (int) Math.round(Math.cos(randomAngle) * randomRadius);
		final int y = spawnAnchor.getCenterY() + (int) Math.round(Math.sin(randomAngle) * randomRadius);
		return new int[] { x, y };
	}

	private static boolean tryPlaceCreature(final StendhalRPZone zone, final Creature creature,
			final int x, final int y, final int[] centerAnchor) {
		if (x < 0 || y < 0 || x >= zone.getWidth() || y >= zone.getHeight()) {
			return false;
		}
		if (zone.collides(creature, x, y)) {
			return false;
		}
		if (centerAnchor != null) {
			final List<Node> path = Path.searchPath(zone, x, y, centerAnchor[0], centerAnchor[1], MAX_PATH_LENGTH);
			if (path == null || path.isEmpty()) {
				return false;
			}
		}
		return StendhalRPAction.placeat(zone, creature, x, y);
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
}
