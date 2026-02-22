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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.EventActivityChestRewardService;
import games.stendhal.server.maps.event.MapEventConfigLoader;
import games.stendhal.server.maps.event.MapEventContributionTracker;
import games.stendhal.server.maps.event.MapEventRewardPolicy;
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
			contributionTracker.recordKillAssist(killer.getName(), 1);
			contributionTracker.recordObjectiveAction(killer.getName(), 1);
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

	private void rewardParticipants(final int defeatPercent) {
		final long now = System.currentTimeMillis();
		final double difficultyModifier = 0.85d + (Math.max(0, Math.min(100, defeatPercent)) / 100.0d * 0.25d);
		final List<EventActivityChestRewardService.QualifiedParticipant> qualifiedParticipants = new ArrayList<>();
		for (Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> entry : contributionTracker.snapshotAll().entrySet()) {
			final Player player = SingletonRepository.getRuleProcessor().getPlayer(entry.getKey());
			if (player == null) {
				continue;
			}
			final MapEventContributionTracker.ContributionSnapshot contribution = entry.getValue();
			final MapEventRewardPolicy.RewardDecision decision = rewardPolicy.evaluate(
					getEventId(),
					entry.getKey(),
					contribution,
					now);
			if (!decision.isQualified()) {
				continue;
			}
			final double eventProgress = Math.max(0.0d, Math.min(1.0d, defeatPercent / 100.0d));
			final double playerScore = Math.max(0.0d, Math.min(1.0d, decision.getTotalScore() / 35.0d));
			final double participationScore = (eventProgress * 0.6d) + (playerScore * 0.4d);
			final RandomEventRewardService.Reward reward = randomEventRewardService.grantRandomEventRewards(
					player,
					RandomEventRewardService.RandomEventType.KIKAREUKIN,
					participationScore,
					difficultyModifier * decision.getMultiplier());
			player.sendPrivateText("Za odparcie aniołów otrzymujesz +" + reward.getXp() + " PD oraz +"
					+ Math.round(reward.getKarma() * 100.0d) / 100.0d + " karmy.");
			player.sendPrivateText("Podsumowanie eventu: punkty=" + Math.max(0, (int) Math.round(decision.getTotalScore()))
					+ ", limit dzienny=" + (decision.isDailyLimitReached() ? "TAK (redukcja nagrody)" : "NIE") + ".");
			qualifiedParticipants.add(new EventActivityChestRewardService.QualifiedParticipant(
					player,
					decision.getTotalScore(),
					contribution.getDamage(),
					contribution.getKillAssists()));
		}
		EventActivityChestRewardService.awardTopActivityChests("Kikareukin", qualifiedParticipants);
	}
}
