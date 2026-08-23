/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Lets the owner abandon an active Challenge Arena run without a refund. */
public final class ForfeitChallengeArenaAction implements ChatAction {
	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		final ChallengeArenaInfo arenaInfo = ChallengeArenaManager.getArenaInfo();
		final ChallengeArenaEngine engine = arenaInfo == null
				? null : arenaInfo.getEngine();
		if (engine == null || !engine.forfeit(player)) {
			raiser.say("Nie prowadzisz teraz walki na Arenie Wyzwań.");
		}
	}
}
