/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.maps.quests.trail;

import java.awt.Rectangle;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

/**
 * Return abandoned encounters to their post rather than leave them defenceless.
 */
final class TrailGuardReturn {
	private TrailGuardReturn() {
	}

	static void idle(Creature guard) {
		guard.clearPath();
		guard.stopAttack();
		guard.stop();
		CreatureRespawnPoint home = guard.getRespawnPoint();
		StendhalRPZone zone = guard.getZone();
		if (home != null && zone != null && home.getZone() == zone
				&& zone.has(guard.getID())
				&& (Math.abs(guard.getX() - home.getX()) > 2
						|| Math.abs(guard.getY() - home.getY()) > 2)) {
			// An occupied post must not send the guard beyond its encounter
			// area.
			StendhalRPAction.placeat(zone, guard, home.getX(), home.getY(),
					new Rectangle(home.getX() - 2, home.getY() - 2, 5, 5));
		}
	}
}
