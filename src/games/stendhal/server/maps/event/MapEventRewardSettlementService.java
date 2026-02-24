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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

/**
 * Shared settlement pipeline for map event participation rewards.
 */
public final class MapEventRewardSettlementService {
	private final String eventId;
	private final MapEventContributionTracker contributionTracker;
	private final MapEventRewardPolicy rewardPolicy;
	private final RewardGrantCallback rewardGrantCallback;
	private final String chestEventName;

	public MapEventRewardSettlementService(final String eventId,
			final MapEventContributionTracker contributionTracker,
			final MapEventRewardPolicy rewardPolicy,
			final RewardGrantCallback rewardGrantCallback,
			final String chestEventName) {
		this.eventId = eventId;
		this.contributionTracker = contributionTracker;
		this.rewardPolicy = rewardPolicy;
		this.rewardGrantCallback = rewardGrantCallback;
		this.chestEventName = chestEventName;
	}

	public int settleRewards(final SettlementOptions options) {
		final SettlementOptions effectiveOptions = options != null ? options : SettlementOptions.defaultOptions();
		final long now = System.currentTimeMillis();
		final List<EventActivityChestRewardService.QualifiedParticipant> qualifiedParticipants =
				new ArrayList<EventActivityChestRewardService.QualifiedParticipant>();
		for (Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> entry : contributionTracker.snapshotAll().entrySet()) {
			final String playerName = entry.getKey();
			final Player player = SingletonRepository.getRuleProcessor().getPlayer(playerName);
			if (player == null) {
				continue;
			}
			final MapEventContributionTracker.ContributionSnapshot contribution = entry.getValue();
			final MapEventRewardPolicy.RewardDecision decision = rewardPolicy.evaluate(
					eventId,
					playerName,
					effectiveOptions.resolveAccountName(player),
					contribution,
					effectiveOptions.resolvePlayerLevel(player),
					now);
			final RewardContext context = new RewardContext(playerName, player, contribution, decision);
			effectiveOptions.onDecision(context);
			if (!decision.isQualified() || !effectiveOptions.isEligible(context)) {
				continue;
			}
			rewardGrantCallback.grant(context);
			qualifiedParticipants.add(new EventActivityChestRewardService.QualifiedParticipant(
					player,
					decision.getTotalScore(),
					contribution.getDamage(),
					contribution.getKillAssists()));
		}
		return EventActivityChestRewardService.awardTopActivityChests(chestEventName, qualifiedParticipants);
	}

	public static List<String> buildActivityTop(final MapEventContributionTracker contributionTracker) {
		final List<Map.Entry<String, MapEventContributionTracker.ContributionSnapshot>> entries =
				new ArrayList<Map.Entry<String, MapEventContributionTracker.ContributionSnapshot>>(contributionTracker.snapshotAll().entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, MapEventContributionTracker.ContributionSnapshot>>() {
			@Override
			public int compare(final Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> first,
					final Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> second) {
				return Integer.compare(
						MapEventContributionTracker.resolveActivityPoints(second.getValue()),
						MapEventContributionTracker.resolveActivityPoints(first.getValue()));
			}
		});
		final List<String> top = new ArrayList<String>();
		for (Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> entry : entries) {
			if (top.size() >= 10) {
				break;
			}
			top.add(entry.getKey() + "::" + MapEventContributionTracker.resolveActivityPoints(entry.getValue()));
		}
		return top;
	}

	public interface RewardGrantCallback {
		void grant(RewardContext context);
	}

	public interface AccountNameResolver {
		String resolve(Player player);
	}

	public interface DecisionObserver {
		void onDecision(RewardContext context);
	}

	public interface EligibilityDecider {
		boolean isEligible(RewardContext context);
	}

	public static final class SettlementOptions {
		private static final AccountNameResolver DEFAULT_ACCOUNT_NAME_RESOLVER = new AccountNameResolver() {
			@Override
			public String resolve(final Player player) {
				return null;
			}
		};
		private static final DecisionObserver DEFAULT_DECISION_OBSERVER = new DecisionObserver() {
			@Override
			public void onDecision(final RewardContext context) {
				// default: no-op
			}
		};
		private static final EligibilityDecider DEFAULT_ELIGIBILITY_DECIDER = new EligibilityDecider() {
			@Override
			public boolean isEligible(final RewardContext context) {
				return true;
			}
		};

		private final AccountNameResolver accountNameResolver;
		private final DecisionObserver decisionObserver;
		private final EligibilityDecider eligibilityDecider;

		private SettlementOptions(final AccountNameResolver accountNameResolver,
				final DecisionObserver decisionObserver,
				final EligibilityDecider eligibilityDecider) {
			this.accountNameResolver = accountNameResolver != null ? accountNameResolver : DEFAULT_ACCOUNT_NAME_RESOLVER;
			this.decisionObserver = decisionObserver != null ? decisionObserver : DEFAULT_DECISION_OBSERVER;
			this.eligibilityDecider = eligibilityDecider != null ? eligibilityDecider : DEFAULT_ELIGIBILITY_DECIDER;
		}

		public static SettlementOptions defaultOptions() {
			return new SettlementOptions(null, null, null);
		}

		public static SettlementOptions of(final AccountNameResolver accountNameResolver,
				final DecisionObserver decisionObserver,
				final EligibilityDecider eligibilityDecider) {
			return new SettlementOptions(accountNameResolver, decisionObserver, eligibilityDecider);
		}

		private int resolvePlayerLevel(final Player player) {
			return player != null ? player.getLevel() : 1;
		}

		private String resolveAccountName(final Player player) {
			return accountNameResolver.resolve(player);
		}

		private void onDecision(final RewardContext context) {
			decisionObserver.onDecision(context);
		}

		private boolean isEligible(final RewardContext context) {
			return eligibilityDecider.isEligible(context);
		}
	}

	public static final class RewardContext {
		private final String playerName;
		private final Player player;
		private final MapEventContributionTracker.ContributionSnapshot contribution;
		private final MapEventRewardPolicy.RewardDecision decision;

		private RewardContext(final String playerName, final Player player,
				final MapEventContributionTracker.ContributionSnapshot contribution,
				final MapEventRewardPolicy.RewardDecision decision) {
			this.playerName = playerName;
			this.player = player;
			this.contribution = contribution;
			this.decision = decision;
		}

		public String getPlayerName() {
			return playerName;
		}

		public Player getPlayer() {
			return player;
		}

		public MapEventContributionTracker.ContributionSnapshot getContribution() {
			return contribution;
		}

		public MapEventRewardPolicy.RewardDecision getDecision() {
			return decision;
		}
	}
}
