/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.Direction;
import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Leaves the Challenge Arena and returns the visitor to Tarnow. */
public final class LeaveChallengeArenaAction implements ChatAction {
	private static final int TARNOW_RETURN_X = 44;
	private static final int TARNOW_RETURN_Y = 64;

	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		final ChallengeArenaInfo arenaInfo = ChallengeArenaManager.getArenaInfo();
		if (arenaInfo != null && ChallengeArenaManager.isReservedBy(player.getName())) {
			final ChallengeArenaEngine engine = arenaInfo.getEngine();
			if (engine != null) {
				engine.forfeit(player);
			}
		}

		if (!player.teleport(ChallengeArenaZone.TARNOW_CITY_ZONE,
				TARNOW_RETURN_X, TARNOW_RETURN_Y,
				Direction.DOWN, null)) {
			raiser.say("Nie udało się teraz wrócić do Tarnowa.");
		}
	}
}
