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
	private static final Map<String, Integer> PLAYER_DAILY_FULL_REWARD_USAGE = new ConcurrentHashMap<>();
	private static final Map<String, Integer> ACCOUNT_DAILY_FULL_REWARD_USAGE = new ConcurrentHashMap<>();

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
	private final int dailyFullRewardLimit;
	private final double dailyReducedRewardMultiplier;
	private final int minFullParticipationLevel;
	private final int highLevelPenaltyStart;
	private final double lowLevelScoreMultiplier;
	private final double highLevelScoreMultiplier;
	private final int maxPrimaryRewardLevel;
	private final boolean grantSymbolicRewardWhenPrimaryBlocked;

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
		dailyFullRewardLimit = builder.dailyFullRewardLimit;
		dailyReducedRewardMultiplier = builder.dailyReducedRewardMultiplier;
		minFullParticipationLevel = builder.minFullParticipationLevel;
		highLevelPenaltyStart = builder.highLevelPenaltyStart;
		lowLevelScoreMultiplier = builder.lowLevelScoreMultiplier;
		highLevelScoreMultiplier = builder.highLevelScoreMultiplier;
		maxPrimaryRewardLevel = builder.maxPrimaryRewardLevel;
		grantSymbolicRewardWhenPrimaryBlocked = builder.grantSymbolicRewardWhenPrimaryBlocked;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static MapEventRewardPolicy defaultEscortPolicy() {
		return builder()
				.minDamage(300)
				.minKillAssists(2)
				.minObjectiveActions(8)
				.minTimeInZoneSeconds(0)
				.antiAfkWindowSeconds(30)
				.minScorePerWindow(1.25d)
				.damageWeight(0.01d)
				.assistWeight(2.0d)
				.objectiveWeight(1.0d)
				.zoneTimeWeight(0.0d)
				.diminishingWindow(Duration.ofMinutes(30))
				.diminishingFactorPerExtraRun(0.25d)
				.minRewardMultiplier(0.35d)
				.dailyFullRewardLimit(2)
				.dailyReducedRewardMultiplier(0.25d)
				.minFullParticipationLevel(20)
				.highLevelPenaltyStart(150)
				.lowLevelScoreMultiplier(0.30d)
				.highLevelScoreMultiplier(0.15d)
				.maxPrimaryRewardLevel(150)
				.grantSymbolicRewardWhenPrimaryBlocked(true)
				.build();
	}

	public RewardDecision evaluate(final String eventId, final String playerName,
			final MapEventContributionTracker.ContributionSnapshot contribution, final long nowMillis) {
		return evaluate(eventId, playerName, null, contribution, minFullParticipationLevel, nowMillis);
	}

	public RewardDecision evaluate(final String eventId, final String playerName,
			final MapEventContributionTracker.ContributionSnapshot contribution, final int playerLevel,
			final long nowMillis) {
		return evaluate(eventId, playerName, null, contribution, playerLevel, nowMillis);
	}

	public RewardDecision evaluate(final String eventId, final String playerName, final String accountName,
			final MapEventContributionTracker.ContributionSnapshot contribution, final int playerLevel,
			final long nowMillis) {
		Objects.requireNonNull(eventId, "eventId");
		Objects.requireNonNull(playerName, "playerName");
		Objects.requireNonNull(contribution, "contribution");

		final int combinedKillActivity = resolveCombinedKillActivity(contribution);
		final int killActivityPoints = MapEventConfig.resolveKillActivityPoints(combinedKillActivity);
		final double rawTotalScore = (contribution.getDamage() * damageWeight)
				+ (killActivityPoints * assistWeight)
				+ (contribution.getObjectiveActions() * objectiveWeight)
				+ (contribution.getTimeInZoneSeconds() * zoneTimeWeight);
		final double levelScoreMultiplier = MapEventContributionTracker.resolveLevelContributionModifier(
				playerLevel,
				minFullParticipationLevel,
				highLevelPenaltyStart,
				lowLevelScoreMultiplier,
				highLevelScoreMultiplier);
		final double adjustedTotalScore = rawTotalScore * levelScoreMultiplier;

		final boolean reachedHardThresholds = adjustedContributionValue(contribution.getDamage(), levelScoreMultiplier) >= minDamage
				|| adjustedContributionValue(combinedKillActivity, levelScoreMultiplier) >= minKillAssists
				|| adjustedContributionValue(contribution.getObjectiveActions(), levelScoreMultiplier) >= minObjectiveActions;
		final int actionableContributionUnits = Math.max(0, contribution.getDamage())
				+ (Math.max(0, combinedKillActivity) * 100)
				+ (Math.max(0, contribution.getObjectiveActions()) * 100);
		final int windows = Math.max(1, actionableContributionUnits / Math.max(1, antiAfkWindowSeconds));
		final double scorePerWindow = adjustedTotalScore / windows;
		final boolean antiAfkPassed = actionableContributionUnits > 0 && scorePerWindow >= minScorePerWindow;
		final boolean qualified = reachedHardThresholds && antiAfkPassed;
		final boolean fullParticipation = playerLevel >= minFullParticipationLevel;
		final boolean primaryRewardEligible = playerLevel <= maxPrimaryRewardLevel;

		final int recentRuns = cleanupAndCountRuns(eventId, playerName, nowMillis);
		double multiplier = 1.0d;
		if (recentRuns > 0) {
			multiplier = Math.max(minRewardMultiplier, 1.0d - (recentRuns * diminishingFactorPerExtraRun));
		}

		final long dayBucket = resolveDayBucket(nowMillis);
		final int dailyPlayerFullRewards = getDailyFullRewardCount(PLAYER_DAILY_FULL_REWARD_USAGE, eventId, playerName,
				dayBucket);
		final int dailyAccountFullRewards = getDailyFullRewardCount(ACCOUNT_DAILY_FULL_REWARD_USAGE, eventId, accountName,
				dayBucket);
		final int effectiveDailyFullRewards = Math.max(dailyPlayerFullRewards, dailyAccountFullRewards);
		final boolean dailyLimitReached = effectiveDailyFullRewards >= dailyFullRewardLimit;
		final double dailyLimitMultiplier = dailyLimitReached ? dailyReducedRewardMultiplier : 1.0d;
		if (qualified) {
			recordRun(eventId, playerName, nowMillis);
			if (!dailyLimitReached) {
				recordDailyFullRewardUsage(PLAYER_DAILY_FULL_REWARD_USAGE, eventId, playerName, dayBucket);
				recordDailyFullRewardUsage(ACCOUNT_DAILY_FULL_REWARD_USAGE, eventId, accountName, dayBucket);
			}
		}

		return new RewardDecision(qualified, multiplier * dailyLimitMultiplier, multiplier, dailyLimitMultiplier,
				dailyLimitReached, dailyPlayerFullRewards, dailyAccountFullRewards,
				adjustedTotalScore, rawTotalScore, scorePerWindow, recentRuns,
				antiAfkPassed, levelScoreMultiplier, fullParticipation, primaryRewardEligible,
				grantSymbolicRewardWhenPrimaryBlocked);
	}

	private static long resolveDayBucket(final long nowMillis) {
		return Math.floorDiv(nowMillis, Duration.ofDays(1).toMillis());
	}

	private static int getDailyFullRewardCount(final Map<String, Integer> usageMap, final String eventId,
			final String identity, final long dayBucket) {
		final String key = dailyUsageKey(eventId, identity, dayBucket);
		if (key == null) {
			return 0;
		}
		return usageMap.getOrDefault(key, Integer.valueOf(0));
	}

	private static void recordDailyFullRewardUsage(final Map<String, Integer> usageMap, final String eventId,
			final String identity, final long dayBucket) {
		final String key = dailyUsageKey(eventId, identity, dayBucket);
		if (key == null) {
			return;
		}
		usageMap.merge(key, Integer.valueOf(1), Integer::sum);
	}

	private static String dailyUsageKey(final String eventId, final String identity, final long dayBucket) {
		if (identity == null || identity.trim().isEmpty()) {
			return null;
		}
		return dayBucket + "::" + historyKey(eventId, identity);
	}

	private static double adjustedContributionValue(final int value, final double multiplier) {
		return Math.max(0, value) * multiplier;
	}

	private static int resolveCombinedKillActivity(final MapEventContributionTracker.ContributionSnapshot contribution) {
		return Math.max(0, contribution.getKillCount()) + Math.max(0, contribution.getKillAssists());
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
		private final double antiFarmMultiplier;
		private final double dailyLimitMultiplier;
		private final boolean dailyLimitReached;
		private final int dailyCharacterFullRewardsUsed;
		private final int dailyAccountFullRewardsUsed;
		private final double totalScore;
		private final double rawTotalScore;
		private final double scorePerWindow;
		private final int recentRuns;
		private final boolean antiAfkPassed;
		private final double levelScoreMultiplier;
		private final boolean fullParticipation;
		private final boolean primaryRewardEligible;
		private final boolean symbolicRewardEnabled;

		private RewardDecision(final boolean qualified, final double multiplier, final double antiFarmMultiplier,
				final double dailyLimitMultiplier, final boolean dailyLimitReached,
				final int dailyCharacterFullRewardsUsed, final int dailyAccountFullRewardsUsed,
				final double totalScore, final double rawTotalScore, final double scorePerWindow, final int recentRuns,
				final boolean antiAfkPassed, final double levelScoreMultiplier, final boolean fullParticipation,
				final boolean primaryRewardEligible, final boolean symbolicRewardEnabled) {
			this.qualified = qualified;
			this.multiplier = multiplier;
			this.antiFarmMultiplier = antiFarmMultiplier;
			this.dailyLimitMultiplier = dailyLimitMultiplier;
			this.dailyLimitReached = dailyLimitReached;
			this.dailyCharacterFullRewardsUsed = dailyCharacterFullRewardsUsed;
			this.dailyAccountFullRewardsUsed = dailyAccountFullRewardsUsed;
			this.totalScore = totalScore;
			this.rawTotalScore = rawTotalScore;
			this.scorePerWindow = scorePerWindow;
			this.recentRuns = recentRuns;
			this.antiAfkPassed = antiAfkPassed;
			this.levelScoreMultiplier = levelScoreMultiplier;
			this.fullParticipation = fullParticipation;
			this.primaryRewardEligible = primaryRewardEligible;
			this.symbolicRewardEnabled = symbolicRewardEnabled;
		}

		public boolean isQualified() {
			return qualified;
		}

		public double getMultiplier() {
			return multiplier;
		}

		public double getAntiFarmMultiplier() {
			return antiFarmMultiplier;
		}

		public double getDailyLimitMultiplier() {
			return dailyLimitMultiplier;
		}

		public boolean isDailyLimitReached() {
			return dailyLimitReached;
		}

		public int getDailyCharacterFullRewardsUsed() {
			return dailyCharacterFullRewardsUsed;
		}

		public int getDailyAccountFullRewardsUsed() {
			return dailyAccountFullRewardsUsed;
		}

		public double getTotalScore() {
			return totalScore;
		}

		public double getRawTotalScore() {
			return rawTotalScore;
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

		public double getLevelScoreMultiplier() {
			return levelScoreMultiplier;
		}

		public boolean isFullParticipation() {
			return fullParticipation;
		}

		public boolean isPrimaryRewardEligible() {
			return primaryRewardEligible;
		}

		public boolean shouldGrantSymbolicRewardOnly() {
			return qualified && !primaryRewardEligible && symbolicRewardEnabled;
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
		private double zoneTimeWeight = 0.0d;
		private Duration diminishingWindow = Duration.ofMinutes(30);
		private double diminishingFactorPerExtraRun = 0.2d;
		private double minRewardMultiplier = 0.4d;
		private int dailyFullRewardLimit = 2;
		private double dailyReducedRewardMultiplier = 0.25d;
		private int minFullParticipationLevel = 20;
		private int highLevelPenaltyStart = 150;
		private double lowLevelScoreMultiplier = 0.30d;
		private double highLevelScoreMultiplier = 0.15d;
		private int maxPrimaryRewardLevel = 150;
		private boolean grantSymbolicRewardWhenPrimaryBlocked = true;

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

		public Builder dailyFullRewardLimit(final int dailyFullRewardLimit) {
			this.dailyFullRewardLimit = dailyFullRewardLimit;
			return this;
		}

		public Builder dailyReducedRewardMultiplier(final double dailyReducedRewardMultiplier) {
			this.dailyReducedRewardMultiplier = dailyReducedRewardMultiplier;
			return this;
		}

		public Builder minFullParticipationLevel(final int minFullParticipationLevel) {
			this.minFullParticipationLevel = minFullParticipationLevel;
			return this;
		}

		public Builder highLevelPenaltyStart(final int highLevelPenaltyStart) {
			this.highLevelPenaltyStart = highLevelPenaltyStart;
			return this;
		}

		public Builder lowLevelScoreMultiplier(final double lowLevelScoreMultiplier) {
			this.lowLevelScoreMultiplier = lowLevelScoreMultiplier;
			return this;
		}

		public Builder highLevelScoreMultiplier(final double highLevelScoreMultiplier) {
			this.highLevelScoreMultiplier = highLevelScoreMultiplier;
			return this;
		}

		public Builder maxPrimaryRewardLevel(final int maxPrimaryRewardLevel) {
			this.maxPrimaryRewardLevel = maxPrimaryRewardLevel;
			return this;
		}

		public Builder grantSymbolicRewardWhenPrimaryBlocked(final boolean grantSymbolicRewardWhenPrimaryBlocked) {
			this.grantSymbolicRewardWhenPrimaryBlocked = grantSymbolicRewardWhenPrimaryBlocked;
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
			if (dailyFullRewardLimit < 0) {
				throw new IllegalArgumentException("dailyFullRewardLimit must be >= 0");
			}
			if (dailyReducedRewardMultiplier < 0.0d || dailyReducedRewardMultiplier > 1.0d) {
				throw new IllegalArgumentException("dailyReducedRewardMultiplier must be in range [0,1]");
			}
			if (minFullParticipationLevel < 0) {
				throw new IllegalArgumentException("minFullParticipationLevel must be >= 0");
			}
			if (highLevelPenaltyStart < minFullParticipationLevel) {
				throw new IllegalArgumentException("highLevelPenaltyStart must be >= minFullParticipationLevel");
			}
			if (lowLevelScoreMultiplier < 0.0d || lowLevelScoreMultiplier > 1.0d) {
				throw new IllegalArgumentException("lowLevelScoreMultiplier must be in range [0,1]");
			}
			if (highLevelScoreMultiplier < 0.0d || highLevelScoreMultiplier > 1.0d) {
				throw new IllegalArgumentException("highLevelScoreMultiplier must be in range [0,1]");
			}
			if (maxPrimaryRewardLevel < 0) {
				throw new IllegalArgumentException("maxPrimaryRewardLevel must be >= 0");
			}
			return new MapEventRewardPolicy(this);
		}
	}
}
