/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.kikareukin;

import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventConfigLoader;
import games.stendhal.server.maps.event.MapEventContributionTracker;
import games.stendhal.server.maps.event.MapEventRewardPolicy;
import games.stendhal.server.maps.event.MapEventRewardSettlementService;
import games.stendhal.server.maps.event.RandomEventRewardService;

public final class KikareukinAngelEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(KikareukinAngelEvent.class);
	private static final int ACTIVITY_SAMPLE_INTERVAL_SECONDS = 10;
	private static final int MIN_DEFEAT_PERCENT_FOR_REWARD = 60;

	private final MapEventContributionTracker contributionTracker = new MapEventContributionTracker();
	private final MapEventRewardPolicy rewardPolicy = MapEventRewardPolicy.defaultEscortPolicy();
	private final RandomEventRewardService randomEventRewardService = new RandomEventRewardService();
	private final TurnListener activityTracker = new TurnListener() {
		@Override
		public void onTurnReached(final int currentTurn) {
			recordPlayersInEventZones();
			scheduleActivityTracker();
		}
	};

	public KikareukinAngelEvent() {
		super(LOGGER, MapEventConfigLoader.load(MapEventConfigLoader.KIKAREUKIN_ANGEL_PREVIEW));
	}

	@Override
	protected void onStart() {
		contributionTracker.clear();
		super.onStart();
		scheduleActivityTracker();
	}

	@Override
	protected void onStop() {
		SingletonRepository.getTurnNotifier().dontNotify(activityTracker);
		final int defeatPercent = getEventDefeatPercent();
		if (defeatPercent >= MIN_DEFEAT_PERCENT_FOR_REWARD) {
			rewardParticipants(defeatPercent);
		}
		contributionTracker.clear();
		super.onStop();
	}

	@Override
	protected void onEventCreatureDeath(final CircumstancesOfDeath circs) {
		super.onEventCreatureDeath(circs);
		if (circs != null && circs.getKiller() instanceof Player) {
			final Player killer = (Player) circs.getKiller();
			contributionTracker.recordKillCount(killer.getName(), 1);
		}
	}

	private void scheduleActivityTracker() {
		if (!isEventActive()) {
			return;
		}
		SingletonRepository.getTurnNotifier().notifyInSeconds(ACTIVITY_SAMPLE_INTERVAL_SECONDS, activityTracker);
	}

	private void recordPlayersInEventZones() {
		// Presence time no longer contributes to reward qualification/ranking.
		// This sampling hook is intentionally kept as a no-op for future telemetry reuse.
	}

	@Override
	protected List<String> getActivityTop() {
		return MapEventRewardSettlementService.buildActivityTop(contributionTracker);
	}

	private void rewardParticipants(final int defeatPercent) {
		final double difficultyModifier = 0.85d + (Math.max(0, Math.min(100, defeatPercent)) / 100.0d * 0.25d);
		new MapEventRewardSettlementService(getEventId(), contributionTracker, rewardPolicy,
				new MapEventRewardSettlementService.RewardGrantCallback() {
					@Override
					public void grant(final MapEventRewardSettlementService.RewardContext context) {
						final double eventProgress = Math.max(0.0d, Math.min(1.0d, defeatPercent / 100.0d));
						final double playerScore = Math.max(0.0d,
								Math.min(1.0d, context.getDecision().getTotalScore() / 35.0d));
						final double participationScore = (eventProgress * 0.6d) + (playerScore * 0.4d);
						final RandomEventRewardService.Reward reward = randomEventRewardService.grantRandomEventRewards(
								context.getPlayer(),
								RandomEventRewardService.RandomEventType.KIKAREUKIN,
								participationScore,
								difficultyModifier * context.getDecision().getMultiplier());
						context.getPlayer().sendPrivateText(NotificationType.POSITIVE, "Za odparcie aniołów otrzymujesz +" + reward.getXp()
								+ " PD oraz +" + Math.round(reward.getKarma() * 100.0d) / 100.0d + " karmy.");
					}
				}, "Kikareukin").settleRewards(MapEventRewardSettlementService.SettlementOptions.defaultOptions());
	}
}
