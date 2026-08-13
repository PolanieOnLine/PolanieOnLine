/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import java.awt.Point;
import java.util.BitSet;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.Entity;

/**
 * Finds a nearby reachable position for normal creature respawns without
 * launching a separate A* search for every candidate tile.
 *
 * <p>The old generic placement scans nearby tiles and may run pathfinding for
 * every free candidate. A blocked spawn point can therefore multiply the cost
 * of a single respawn. This helper performs one bounded breadth-first search on
 * the static collision map and checks dynamic occupancy only for reachable
 * candidates.</p>
 */
final class CreatureRespawnPlacement {
	static final int MAX_DISPLACEMENT = 36;
	private static final int DIAMETER = MAX_DISPLACEMENT * 2 + 1;
	private static final int MAX_CANDIDATES = 2 * MAX_DISPLACEMENT
			* (MAX_DISPLACEMENT + 1) + 1;

	private CreatureRespawnPlacement() {
	}

	/**
	 * Place a respawned creature near its authored spawn point.
	 *
	 * @param zone target zone
	 * @param entity creature instance
	 * @param x authored x coordinate
	 * @param y authored y coordinate
	 * @return true if placement succeeded
	 */
	static boolean place(final StendhalRPZone zone, final Entity entity,
			final int x, final int y) {
		if (!zone.collides(entity, x, y)) {
			return StendhalRPAction.placeat(zone, entity, x, y);
		}

		/*
		 * A spawn authored on a static collision tile is unusual. Preserve the
		 * generic placement behavior for that edge case rather than changing its
		 * semantics in this optimization.
		 */
		if (zone.simpleCollides(entity, x, y, entity.getWidth(), entity.getHeight())) {
			return StendhalRPAction.placeat(zone, entity, x, y);
		}

		final Point location = findReachableFreeLocation(zone, entity, x, y);
		if (location == null) {
			return false;
		}
		return StendhalRPAction.placeat(zone, entity, location.x, location.y);
	}

	/**
	 * Find the nearest dynamically free tile reachable through static walkable
	 * tiles. Search distance matches the generic placement limit.
	 */
	static Point findReachableFreeLocation(final StendhalRPZone zone,
			final Entity entity, final int originX, final int originY) {
		final BitSet visited = new BitSet(DIAMETER * DIAMETER);
		final int[] queue = new int[MAX_CANDIDATES];
		int read = 0;
		int write = 0;

		queue[write++] = encodeOffset(0, 0);
		visited.set(index(0, 0));

		while (read < write) {
			final int encoded = queue[read++];
			final int dx = decodeX(encoded);
			final int dy = decodeY(encoded);
			final int x = originX + dx;
			final int y = originY + dy;

			if ((dx != 0 || dy != 0) && !zone.collides(entity, x, y)) {
				return new Point(x, y);
			}

			write = enqueue(zone, entity, originX, originY,
					dx - 1, dy, queue, write, visited);
			write = enqueue(zone, entity, originX, originY,
					dx + 1, dy, queue, write, visited);
			write = enqueue(zone, entity, originX, originY,
					dx, dy - 1, queue, write, visited);
			write = enqueue(zone, entity, originX, originY,
					dx, dy + 1, queue, write, visited);
		}

		return null;
	}

	private static int enqueue(final StendhalRPZone zone, final Entity entity,
			final int originX, final int originY, final int dx, final int dy,
			final int[] queue, final int write, final BitSet visited) {
		if (Math.abs(dx) + Math.abs(dy) > MAX_DISPLACEMENT) {
			return write;
		}

		final int index = index(dx, dy);
		if (visited.get(index)) {
			return write;
		}
		visited.set(index);

		final int x = originX + dx;
		final int y = originY + dy;
		if (zone.simpleCollides(entity, x, y, entity.getWidth(), entity.getHeight())) {
			return write;
		}

		queue[write] = encodeOffset(dx, dy);
		return write + 1;
	}

	private static int encodeOffset(final int dx, final int dy) {
		return index(dx, dy);
	}

	private static int decodeX(final int encoded) {
		return encoded % DIAMETER - MAX_DISPLACEMENT;
	}

	private static int decodeY(final int encoded) {
		return encoded / DIAMETER - MAX_DISPLACEMENT;
	}

	private static int index(final int dx, final int dy) {
		return (dy + MAX_DISPLACEMENT) * DIAMETER
				+ dx + MAX_DISPLACEMENT;
	}
}
