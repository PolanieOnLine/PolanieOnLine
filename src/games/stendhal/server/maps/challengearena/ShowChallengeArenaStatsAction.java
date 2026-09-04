/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.npc.ChatAction;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;

/** Shows one player's persistent Challenge Arena results. */
public final class ShowChallengeArenaStatsAction implements ChatAction {
	@Override
	public void fire(final Player player, final Sentence sentence,
			final EventRaiser raiser) {
		final int wins = ChallengeArenaRewardService.getWins(player);
		if (wins <= 0) {
			raiser.say("Nie masz jeszcze zwycięstwa na Arenie Wyzwań.");
			return;
		}

		final int bestStake = ChallengeArenaRewardService.getBestStake(player);
		final long bestTime = ChallengeArenaRewardService.getBestTimeMillis(player);
		final long totalSpent = ChallengeArenaRewardService.getTotalSpent(player);
		final long seconds = Math.max(0L, Math.round(bestTime / 1000.0));

		raiser.say("Masz " + wins + " zwycięstw. Najwyższa ukończona stawka to "
				+ bestStake + " sztuk złota. Najlepszy czas na tej stawce to "
				+ seconds + " sekund. Łącznie wydałeś na arenę "
				+ totalSpent + " sztuk złota.");
	}
}
