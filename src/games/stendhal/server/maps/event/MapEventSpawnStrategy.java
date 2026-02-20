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

import games.stendhal.server.entity.creature.Creature;

public interface MapEventSpawnStrategy {
	void spawnCreatures(String eventName, List<String> zones, String creatureName, int count,
			SpawnedCreatureHandler spawnedCreatureHandler);

	default void spawnCreatures(final String eventName, final List<String> zones, final String creatureName,
			final int count, final SpawnAnchor spawnAnchor,
			final SpawnedCreatureHandler spawnedCreatureHandler) {
		spawnCreatures(eventName, zones, creatureName, count, spawnedCreatureHandler);
	}

	final class SpawnAnchor {
		private final String zone;
		private final int centerX;
		private final int centerY;
		private final int minRadius;
		private final int maxRadius;

		public SpawnAnchor(final String zone, final int centerX, final int centerY,
				final int minRadius, final int maxRadius) {
			if (zone == null || zone.trim().isEmpty()) {
				throw new IllegalArgumentException("zone must not be blank");
			}
			if (minRadius < 0) {
				throw new IllegalArgumentException("minRadius must be >= 0");
			}
			if (maxRadius < minRadius) {
				throw new IllegalArgumentException("maxRadius must be >= minRadius");
			}
			this.zone = zone;
			this.centerX = centerX;
			this.centerY = centerY;
			this.minRadius = minRadius;
			this.maxRadius = maxRadius;
		}

		public String getZone() {
			return zone;
		}

		public int getCenterX() {
			return centerX;
		}

		public int getCenterY() {
			return centerY;
		}

		public int getMinRadius() {
			return minRadius;
		}

		public int getMaxRadius() {
			return maxRadius;
		}
	}

	interface SpawnedCreatureHandler {
		void onSpawned(Creature creature);
	}
}
