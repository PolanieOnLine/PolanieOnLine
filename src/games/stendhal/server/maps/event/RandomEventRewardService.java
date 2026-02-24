/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import games.stendhal.common.Rand;
import games.stendhal.server.entity.player.Player;

/**
 * Grants EXP and karma for random map events.
 */
public class RandomEventRewardService {
	public enum RandomEventType {
		GIANT_ESCORT,
		DRAGON_LAND,
		KIKAREUKIN,
		KUZNICE_BANDIT_RAID,
		HELL_CAPTURE_ASSAULT
	}

	private static final int ESCORT_XP_MIN = 80000;
	private static final int ESCORT_XP_MAX = 220000;
	private static final double ESCORT_KARMA_MIN = 7.0d;
	private static final double ESCORT_KARMA_MAX = 20.0d;

	public Reward grantRandomEventRewards(final Player player, final RandomEventType eventType,
			final double participationScore) {
		return grantRandomEventRewards(player, eventType, participationScore, 1.0d);
	}

	public Reward grantRandomEventRewards(final Player player, final RandomEventType eventType,
			final double participationScore, final double difficultyModifier) {
		if (player == null || eventType == null) {
			return Reward.NONE;
		}

		final double normalizedParticipation = clamp01(participationScore);
		final double clampedDifficulty = Math.max(0.1d, Math.min(2.0d, difficultyModifier));

		final RewardRange escortBaseRange = buildEscortRewardRange(normalizedParticipation);
		final RewardRange finalRange;
		if (eventType == RandomEventType.GIANT_ESCORT) {
			finalRange = escortBaseRange.scale(clampedDifficulty);
		} else {
			final double eventScale = resolveEventScale(eventType, normalizedParticipation);
			finalRange = escortBaseRange.scale(eventScale * clampedDifficulty);
		}

		final int xpReward = randomBetween(finalRange.minXp, finalRange.maxXp);
		final double karmaReward = randomBetween(finalRange.minKarma, finalRange.maxKarma);

		player.addXP(Math.max(1, xpReward));
		player.addKarma(Math.max(0.0d, karmaReward));

		return new Reward(Math.max(1, xpReward), Math.max(0.0d, karmaReward));
	}

	private RewardRange buildEscortRewardRange(final double participationScore) {
		final int centerXp = interpolateInt(ESCORT_XP_MIN, ESCORT_XP_MAX, participationScore);
		final int minXp = Math.max(1, (int) Math.round(centerXp * 0.90d));
		final int maxXp = Math.max(minXp, (int) Math.round(centerXp * 1.10d));

		final double centerKarma = interpolateDouble(ESCORT_KARMA_MIN, ESCORT_KARMA_MAX, participationScore);
		final double minKarma = Math.max(0.0d, centerKarma * 0.90d);
		final double maxKarma = Math.max(minKarma, centerKarma * 1.10d);
		return new RewardRange(minXp, maxXp, minKarma, maxKarma);
	}

	private double resolveEventScale(final RandomEventType eventType, final double participationScore) {
		if (eventType == RandomEventType.HELL_CAPTURE_ASSAULT) {
			return 0.80d + (0.30d * participationScore);
		}
		return 0.70d + (0.30d * participationScore);
	}

	private static int interpolateInt(final int min, final int max, final double ratio) {
		return (int) Math.round(min + ((max - min) * clamp01(ratio)));
	}

	private static double interpolateDouble(final double min, final double max, final double ratio) {
		return min + ((max - min) * clamp01(ratio));
	}

	private static int randomBetween(final int min, final int max) {
		if (max <= min) {
			return min;
		}
		return min + Rand.rand((max - min) + 1);
	}

	private static double randomBetween(final double min, final double max) {
		if (max <= min) {
			return min;
		}
		final int precision = 100;
		final int minScaled = (int) Math.round(min * precision);
		final int maxScaled = Math.max(minScaled, (int) Math.round(max * precision));
		return randomBetween(minScaled, maxScaled) / (double) precision;
	}

	private static double clamp01(final double value) {
		return Math.max(0.0d, Math.min(1.0d, value));
	}

	public static final class Reward {
		static final Reward NONE = new Reward(0, 0.0d);
		private final int xp;
		private final double karma;

		Reward(final int xp, final double karma) {
			this.xp = xp;
			this.karma = karma;
		}

		public int getXp() {
			return xp;
		}

		public double getKarma() {
			return karma;
		}
	}

	private static final class RewardRange {
		private final int minXp;
		private final int maxXp;
		private final double minKarma;
		private final double maxKarma;

		private RewardRange(final int minXp, final int maxXp, final double minKarma, final double maxKarma) {
			this.minXp = minXp;
			this.maxXp = maxXp;
			this.minKarma = minKarma;
			this.maxKarma = maxKarma;
		}

		private RewardRange scale(final double factor) {
			final int scaledMinXp = Math.max(1, (int) Math.round(minXp * factor));
			final int scaledMaxXp = Math.max(scaledMinXp, (int) Math.round(maxXp * factor));
			final double scaledMinKarma = Math.max(0.0d, minKarma * factor);
			final double scaledMaxKarma = Math.max(scaledMinKarma, maxKarma * factor);
			return new RewardRange(scaledMinXp, scaledMaxXp, scaledMinKarma, scaledMaxKarma);
		}
	}
}
