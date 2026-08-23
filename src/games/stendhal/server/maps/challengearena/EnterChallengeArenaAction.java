/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Moves a visitor from Krakow to the Challenge Arena lobby. */
public final class EnterChallengeArenaAction implements ChatAction {
	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		final ChallengeArenaInfo arenaInfo = ChallengeArenaManager.getArenaInfo();
		if (arenaInfo == null) {
			raiser.say("Arena nie jest teraz dostępna.");
			return;
		}
		if (ChallengeArenaManager.isReservedBy(player.getName())) {
			raiser.say("Najpierw dokończ rozpoczętą walkę na Arenie Wyzwań.");
			return;
		}
		if (!arenaInfo.teleportToLobby(player)) {
			raiser.say("Nie udało się teraz wejść na Arenę Wyzwań.");
		}
	}
}
