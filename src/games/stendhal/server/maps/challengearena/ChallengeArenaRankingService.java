/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.dbcommand.WriteHallOfFamePointsCommand;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.command.DBCommandPriority;
import marauroa.server.db.command.DBCommandQueue;

/** Stores a ranking ordered by highest completed stake and then fastest time. */
public final class ChallengeArenaRankingService {
	public static final String FAME_TYPE = "CA";
	private static final int TIER_BLOCK = 10000000;
	private static final int MAX_TIME_SCORE = TIER_BLOCK - 1;
	private static final int TIME_QUANTUM_MILLIS = 100;

	private ChallengeArenaRankingService() {
	}

	public static void updateRanking(final Player player) {
		if (player == null) {
			return;
		}
		final ChallengeArenaTier tier = ChallengeArenaTier.forStake(
				parseInt(player.getQuest(ChallengeArenaRewardService.STATS_SLOT, 1)));
		final long bestTime = parseLong(
				player.getQuest(ChallengeArenaRewardService.STATS_SLOT, 2));
		if (tier == null || bestTime <= 0L) {
			return;
		}

		final int score = calculateScore(tier, bestTime);
		DBCommandQueue.get().enqueue(new WriteHallOfFamePointsCommand(
				player.getName(), FAME_TYPE, score, false), DBCommandPriority.LOW);

		SingletonRepository.getTurnNotifier().notifyInSeconds(3,
				new TurnListener() {
					@Override
					public void onTurnReached(final int currentTurn) {
						ChallengeArenaManager.refreshRankingSign();
					}
				});
	}

	/** Higher tier always outranks a lower tier. Faster time wins within a tier. */
	static int calculateScore(final ChallengeArenaTier tier,
			final long durationMillis) {
		if (tier == null || durationMillis < 0L) {
			return 0;
		}
		final int tierScore = (tier.ordinal() + 1) * TIER_BLOCK;
		final long elapsedQuanta = durationMillis / TIME_QUANTUM_MILLIS;
		final int timeScore = (int) Math.max(0L,
				MAX_TIME_SCORE - Math.min((long) MAX_TIME_SCORE, elapsedQuanta));
		return tierScore + timeScore;
	}

	private static int parseInt(final String value) {
		if (value == null || value.length() == 0) {
			return 0;
		}
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	private static long parseLong(final String value) {
		if (value == null || value.length() == 0) {
			return 0L;
		}
		try {
			return Long.parseLong(value);
		} catch (final NumberFormatException e) {
			return 0L;
		}
	}
}
