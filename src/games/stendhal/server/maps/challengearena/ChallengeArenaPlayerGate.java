/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.area.OnePlayerArea;
import games.stendhal.server.entity.player.Player;

/** Blocks the combat ring unless the player owns the paid arena reservation. */
public final class ChallengeArenaPlayerGate extends OnePlayerArea {
	public ChallengeArenaPlayerGate(final int width, final int height) {
		super(width, height);
	}

	@Override
	public boolean isObstacle(final Entity entity) {
		if (!(entity instanceof Player)) {
			return false;
		}
		final Player player = (Player) entity;
		if (player.isGhost() || contains(player)) {
			return false;
		}
		if (!ChallengeArenaManager.isReservedBy(player.getName())) {
			return true;
		}
		return super.isObstacle(entity);
	}
}
