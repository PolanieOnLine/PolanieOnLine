/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.entity.player;

/**
 * Synchronizes reborn progression with the public, client-visible badge.
 *
 * The badge value is presentation-only and volatile. RebornSystem remains the
 * only source of truth for progression and bonuses.
 */
public final class RebornDisplay {
	public static final String ATTR_REBORN_BADGE = "reborn_badge";

	private RebornDisplay() {
		// utility class
	}

	/** Updates the public badge from the permanent reborn counter. */
	public static void sync(final Player player) {
		if (player == null) {
			return;
		}

		final int reborns = RebornSystem.getRebornCount(player);
		if (reborns > 0) {
			player.put(ATTR_REBORN_BADGE, reborns);
		} else if (player.has(ATTR_REBORN_BADGE)) {
			player.remove(ATTR_REBORN_BADGE);
		}
	}
}
