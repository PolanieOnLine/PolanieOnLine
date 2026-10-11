/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.area.OnePlayerArea;
import games.stendhal.server.entity.player.Player;

/** Blocks the combat ring unless the player owns the paid arena reservation. */
public final class ChallengeArenaPlayerGate extends OnePlayerArea {
	private final String ownerName;

	public ChallengeArenaPlayerGate(final int width, final int height) {
		this(width, height, null);
	}

	public ChallengeArenaPlayerGate(final int width, final int height, final String ownerName) {
		super(width, height);
		this.ownerName = ownerName;
	}

	@Override
	public boolean isObstacle(final Entity entity) {
		if (!(entity instanceof Player)) {
			return false;
		}
		final Player player = (Player) entity;
		// Players saved in the former shared ring before upgrading can still leave.
		if (ownerName == null && contains(player)) {
			return false;
		}
		// The lobby's empty ring has no owner. Paid fights use separate maps.
		return !player.isGhost() && !player.getName().equals(ownerName);
	}
}
