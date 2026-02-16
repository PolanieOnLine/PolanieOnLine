/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Policy for map event reward qualification based on contribution and anti-abuse checks.
 */
public final class MapEventRewardPolicy {
	private static final Map<String, Deque<Long>> PLAYER_REWARD_HISTORY = new ConcurrentHashMap<>();

	private final int minDamage;
	private final int minKillAssists;
	private final int minObjectiveActions;
	private final int minTimeInZoneSeconds;
	private final int antiAfkWindowSeconds;
	private final double minScorePerWindow;
	private final double damageWeight;
	private final double assistWeight;
	private final double objectiveWeight;
	private final double zoneTimeWeight;
	private final Duration diminishingWindow;
	private final double diminishingFactorPerExtraRun;
	private final double minRewardMultiplier;

	private MapEventRewardPolicy(final Builder builder) {
		minDamage = builder.minDamage;
		minKillAssists = builder.minKillAssists;
		minObjectiveActions = builder.minObjectiveActions;
		minTimeInZoneSeconds = builder.minTimeInZoneSeconds;
		antiAfkWindowSeconds = builder.antiAfkWindowSeconds;
		minScorePerWindow = builder.minScorePerWindow;
		damageWeight = builder.damageWeight;
		assistWeight = builder.assistWeight;
		objectiveWeight = builder.objectiveWeight;
		zoneTimeWeight = builder.zoneTimeWeight;
		diminishingWindow = builder.diminishingWindow;
		diminishingFactorPerExtraRun = builder.diminishingFactorPerExtraRun;
		minRewardMultiplier = builder.minRewardMultiplier;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static MapEventRewardPolicy defaultEscortPolicy() {
		return builder()
				.minDamage(300)
				.minKillAssists(2)
				.minObjectiveActions(8)
				.minTimeInZoneSeconds(120)
				.antiAfkWindowSeconds(30)
				.minScorePerWindow(1.25d)
				.damageWeight(0.01d)
				.assistWeight(2.0d)
				.objectiveWeight(1.0d)
				.zoneTimeWeight(0.05d)
				.diminishingWindow(Duration.ofMinutes(30))
				.diminishingFactorPerExtraRun(0.25d)
				.minRewardMultiplier(0.35d)
				.build();
	}

	public RewardDecision evaluate(final String eventId, final String playerName,
			final MapEventContributionTracker.ContributionSnapshot contribution, final long nowMillis) {
		Objects.requireNonNull(eventId, "eventId");
		Objects.requireNonNull(playerName, "playerName");
		Objects.requireNonNull(contribution, "contribution");

		final boolean reachedHardThresholds = contribution.getDamage() >= minDamage
				|| contribution.getKillAssists() >= minKillAssists
				|| contribution.getObjectiveActions() >= minObjectiveActions
				|| contribution.getTimeInZoneSeconds() >= minTimeInZoneSeconds;

		final double totalScore = (contribution.getDamage() * damageWeight)
				+ (contribution.getKillAssists() * assistWeight)
				+ (contribution.getObjectiveActions() * objectiveWeight)
				+ (contribution.getTimeInZoneSeconds() * zoneTimeWeight);
		final int windows = Math.max(1, contribution.getTimeInZoneSeconds() / Math.max(1, antiAfkWindowSeconds));
		final double scorePerWindow = totalScore / windows;
		final boolean antiAfkPassed = scorePerWindow >= minScorePerWindow;
		final boolean qualified = reachedHardThresholds && antiAfkPassed;

		final int recentRuns = cleanupAndCountRuns(eventId, playerName, nowMillis);
		double multiplier = 1.0d;
		if (recentRuns > 0) {
			multiplier = Math.max(minRewardMultiplier, 1.0d - (recentRuns * diminishingFactorPerExtraRun));
		}
		if (qualified) {
			recordRun(eventId, playerName, nowMillis);
		}

		return new RewardDecision(qualified, multiplier, totalScore, scorePerWindow, recentRuns, antiAfkPassed);
	}

	private int cleanupAndCountRuns(final String eventId, final String playerName, final long nowMillis) {
		final Deque<Long> history = PLAYER_REWARD_HISTORY.computeIfAbsent(historyKey(eventId, playerName),
				ignored -> new ArrayDeque<>());
		synchronized (history) {
			final long threshold = nowMillis - diminishingWindow.toMillis();
			while (!history.isEmpty() && history.peekFirst() < threshold) {
				history.removeFirst();
			}
			return history.size();
		}
	}

	private void recordRun(final String eventId, final String playerName, final long nowMillis) {
		final Deque<Long> history = PLAYER_REWARD_HISTORY.computeIfAbsent(historyKey(eventId, playerName),
				ignored -> new ArrayDeque<>());
		synchronized (history) {
			history.addLast(nowMillis);
		}
	}

	private static String historyKey(final String eventId, final String playerName) {
		return eventId.trim().toLowerCase() + "::" + playerName.trim().toLowerCase();
	}

	public static final class RewardDecision {
		private final boolean qualified;
		private final double multiplier;
		private final double totalScore;
		private final double scorePerWindow;
		private final int recentRuns;
		private final boolean antiAfkPassed;

		private RewardDecision(final boolean qualified, final double multiplier, final double totalScore,
				final double scorePerWindow, final int recentRuns, final boolean antiAfkPassed) {
			this.qualified = qualified;
			this.multiplier = multiplier;
			this.totalScore = totalScore;
			this.scorePerWindow = scorePerWindow;
			this.recentRuns = recentRuns;
			this.antiAfkPassed = antiAfkPassed;
		}

		public boolean isQualified() {
			return qualified;
		}

		public double getMultiplier() {
			return multiplier;
		}

		public double getTotalScore() {
			return totalScore;
		}

		public double getScorePerWindow() {
			return scorePerWindow;
		}

		public int getRecentRuns() {
			return recentRuns;
		}

		public boolean isAntiAfkPassed() {
			return antiAfkPassed;
		}
	}

	public static final class Builder {
		private int minDamage;
		private int minKillAssists;
		private int minObjectiveActions;
		private int minTimeInZoneSeconds;
		private int antiAfkWindowSeconds = 30;
		private double minScorePerWindow;
		private double damageWeight = 1.0d;
		private double assistWeight = 1.0d;
		private double objectiveWeight = 1.0d;
		private double zoneTimeWeight = 1.0d;
		private Duration diminishingWindow = Duration.ofMinutes(30);
		private double diminishingFactorPerExtraRun = 0.2d;
		private double minRewardMultiplier = 0.4d;

		public Builder minDamage(final int minDamage) {
			this.minDamage = minDamage;
			return this;
		}

		public Builder minKillAssists(final int minKillAssists) {
			this.minKillAssists = minKillAssists;
			return this;
		}

		public Builder minObjectiveActions(final int minObjectiveActions) {
			this.minObjectiveActions = minObjectiveActions;
			return this;
		}

		public Builder minTimeInZoneSeconds(final int minTimeInZoneSeconds) {
			this.minTimeInZoneSeconds = minTimeInZoneSeconds;
			return this;
		}

		public Builder antiAfkWindowSeconds(final int antiAfkWindowSeconds) {
			this.antiAfkWindowSeconds = antiAfkWindowSeconds;
			return this;
		}

		public Builder minScorePerWindow(final double minScorePerWindow) {
			this.minScorePerWindow = minScorePerWindow;
			return this;
		}

		public Builder damageWeight(final double damageWeight) {
			this.damageWeight = damageWeight;
			return this;
		}

		public Builder assistWeight(final double assistWeight) {
			this.assistWeight = assistWeight;
			return this;
		}

		public Builder objectiveWeight(final double objectiveWeight) {
			this.objectiveWeight = objectiveWeight;
			return this;
		}

		public Builder zoneTimeWeight(final double zoneTimeWeight) {
			this.zoneTimeWeight = zoneTimeWeight;
			return this;
		}

		public Builder diminishingWindow(final Duration diminishingWindow) {
			this.diminishingWindow = Objects.requireNonNull(diminishingWindow, "diminishingWindow");
			return this;
		}

		public Builder diminishingFactorPerExtraRun(final double diminishingFactorPerExtraRun) {
			this.diminishingFactorPerExtraRun = diminishingFactorPerExtraRun;
			return this;
		}

		public Builder minRewardMultiplier(final double minRewardMultiplier) {
			this.minRewardMultiplier = minRewardMultiplier;
			return this;
		}

		public MapEventRewardPolicy build() {
			if (antiAfkWindowSeconds <= 0) {
				throw new IllegalArgumentException("antiAfkWindowSeconds must be > 0");
			}
			if (minScorePerWindow < 0.0d) {
				throw new IllegalArgumentException("minScorePerWindow must be >= 0");
			}
			if (diminishingFactorPerExtraRun < 0.0d || diminishingFactorPerExtraRun > 1.0d) {
				throw new IllegalArgumentException("diminishingFactorPerExtraRun must be in range [0,1]");
			}
			if (minRewardMultiplier < 0.0d || minRewardMultiplier > 1.0d) {
				throw new IllegalArgumentException("minRewardMultiplier must be in range [0,1]");
			}
			return new MapEventRewardPolicy(this);
		}
	}
}
