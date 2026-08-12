/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import games.stendhal.server.entity.creature.Creature;

/**
 * Controlled runtime updates for creature respawn points.
 */
public final class CreatureRespawnPointUpdater {
	private CreatureRespawnPointUpdater() {
		// utility class
	}

	/**
	 * Replaces the prototype used for future spawns without killing creatures
	 * which are already alive.
	 *
	 * @param point spawn point to update
	 * @param creature new prototype
	 */
	public static void replacePrototype(final CreatureRespawnPoint point,
			final Creature creature) {
		if (point == null || creature == null) {
			throw new IllegalArgumentException("point and creature must not be null");
		}
		point.prototypeCreature = creature;
		point.setRespawnTime(creature.getRespawnTime());
	}
}
