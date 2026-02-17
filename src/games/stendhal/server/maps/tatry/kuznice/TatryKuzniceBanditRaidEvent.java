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
package games.stendhal.server.maps.tatry.kuznice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.EventActivityChestRewardService;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;
import games.stendhal.server.maps.event.MapEventContributionTracker;
import games.stendhal.server.maps.event.MapEventRewardPolicy;
import games.stendhal.server.maps.event.RandomEventRewardService;

public class TatryKuzniceBanditRaidEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(TatryKuzniceBanditRaidEvent.class);
	private static final MapEventConfig EVENT_CONFIG = MapEventConfigLoader.load(MapEventConfigLoader.TATRY_KUZNICE_BANDIT_RAID);
	private static final TatryKuzniceBanditRaidEvent INSTANCE = new TatryKuzniceBanditRaidEvent();
	private static final String BANDIT_COMMANDER_NAME = "zbójnik górski herszt";
	private static final int PREPARE_DURATION_SECONDS = 90;
	private static final int FINAL_PHASE_TIMEOUT_SECONDS = 150;
	private static final int ACTIVITY_SAMPLE_INTERVAL_SECONDS = 10;

	private final MapEventContributionTracker contributionTracker = new MapEventContributionTracker();
	private final MapEventRewardPolicy rewardPolicy = MapEventRewardPolicy.defaultEscortPolicy();
	private final RandomEventRewardService randomEventRewardService = new RandomEventRewardService();
	private final AtomicBoolean settlementHandled = new AtomicBoolean(false);
	private final Map<Integer, TurnListener> scheduledListeners = new ConcurrentHashMap<>();
	private final TurnListener activityTracker = new TurnListener() {
		@Override
		public void onTurnReached(final int currentTurn) {
			recordPlayersInEventZones();
			scheduleActivityTracker();
		}
	};

	private volatile EventPhase phase = EventPhase.SETTLEMENT;
	private volatile long phaseStartedAtMillis;

	public enum EventPhase {
		PREPARE,
		ATTACK,
		FINAL,
		SETTLEMENT
	}

	private TatryKuzniceBanditRaidEvent() {
		super(LOGGER, EVENT_CONFIG);
	}

	public static TatryKuzniceBanditRaidEvent getInstance() {
		return INSTANCE;
	}

	@Override
	protected List<EventWave> getWaves() {
		return Collections.emptyList();
	}

	@Override
	protected void onStart() {
		contributionTracker.clear();
		settlementHandled.set(false);
		clearScheduledListeners();
		super.onStart();
		transitionTo(EventPhase.PREPARE, "event started");
		sendPrepareWarnings();
		logAttackPlan();
		scheduleActivityTracker();
		scheduleInSeconds(PREPARE_DURATION_SECONDS, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				startAttackPhase();
			}
		});
	}

	@Override
	protected void onStop() {
		clearScheduledListeners();
		SingletonRepository.getTurnNotifier().dontNotify(activityTracker);
		handleSettlement("event stopped");
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
		if (!isEventActive() || circs == null || circs.getVictim() == null) {
			return;
		}
		if (phase == EventPhase.FINAL && BANDIT_COMMANDER_NAME.equals(circs.getVictim().getName())) {
			LOGGER.info(getEventName() + " phase FINAL objective completed: commander defeated, ending event early.");
			endEvent();
		}
	}

	private void startAttackPhase() {
		if (!isEventActive()) {
			return;
		}
		transitionTo(EventPhase.ATTACK, "prepare phase completed");
		final List<EventWave> attackWaves = getConfig().getWaves();
		int accumulatedDelay = 0;
		for (int waveIndex = 0; waveIndex < attackWaves.size(); waveIndex++) {
			final EventWave wave = attackWaves.get(waveIndex);
			accumulatedDelay += wave.getIntervalSeconds();
			final int scheduledWaveIndex = waveIndex;
			scheduleInSeconds(accumulatedDelay, new TurnListener() {
				@Override
				public void onTurnReached(final int currentTurn) {
					spawnAttackWave(scheduledWaveIndex, wave);
				}
			});
		}
		scheduleInSeconds(accumulatedDelay, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				startFinalPhase();
			}
		});
	}

	private void spawnAttackWave(final int waveIndex, final EventWave wave) {
		if (!isEventActive() || phase != EventPhase.ATTACK) {
			return;
		}
		LOGGER.info(getEventName() + " phase ATTACK spawning wave " + (waveIndex + 1)
				+ "/" + getConfig().getWaves().size() + " (interval=" + wave.getIntervalSeconds() + "s).");
		for (EventSpawn spawn : wave.getSpawns()) {
			spawnCreaturesForWave(spawn);
		}
	}

	private void startFinalPhase() {
		if (!isEventActive()) {
			return;
		}
		transitionTo(EventPhase.FINAL, "all attack waves spawned");
		LOGGER.info(getEventName() + " phase FINAL spawning commander " + BANDIT_COMMANDER_NAME + ".");
		spawnCreaturesForWave(new EventSpawn(BANDIT_COMMANDER_NAME, 1));
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"Dowódca zbójników został dostrzeżony w Kuźnicach! Pokonajcie go, aby zakończyć napad.");
		scheduleInSeconds(FINAL_PHASE_TIMEOUT_SECONDS, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				if (!isEventActive() || phase != EventPhase.FINAL) {
					return;
				}
				LOGGER.info(getEventName() + " phase FINAL timeout reached; forcing settlement.");
				endEvent();
			}
		});
	}

	private void handleSettlement(final String reason) {
		if (!settlementHandled.compareAndSet(false, true)) {
			return;
		}
		transitionTo(EventPhase.SETTLEMENT, reason);
		rewardParticipants();
		contributionTracker.clear();
		phase = EventPhase.PREPARE;
		phaseStartedAtMillis = 0L;
		LOGGER.info(getEventName() + " settlement completed and event state cleared.");
	}

	private void rewardParticipants() {
		final long now = System.currentTimeMillis();
		final int defeatPercent = getEventDefeatPercent();
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
			final double participationScore = resolveParticipationScore(decision, defeatPercent);
			final RandomEventRewardService.Reward reward = randomEventRewardService.grantRandomEventRewards(
					player,
					RandomEventRewardService.RandomEventType.KUZNICE_BANDIT_RAID,
					participationScore,
					decision.getMultiplier());
			player.sendPrivateText("Za obronę Kuźnic otrzymujesz +" + reward.getXp()
					+ " PD oraz +" + Math.round(reward.getKarma() * 100.0d) / 100.0d + " karmy.");
			qualifiedParticipants.add(new EventActivityChestRewardService.QualifiedParticipant(
					player,
					decision.getTotalScore(),
					contribution.getDamage(),
					contribution.getKillAssists()));
		}
		final int awardedChests = EventActivityChestRewardService.awardTopActivityChests("Kuźnice", qualifiedParticipants);
		LOGGER.info(getEventName() + " settlement rewards granted for " + qualifiedParticipants.size()
				+ " qualified players; chest rewards=" + awardedChests + ".");
	}

	private double resolveParticipationScore(final MapEventRewardPolicy.RewardDecision decision, final int defeatPercent) {
		final double eventProgressScore = Math.max(0.0d, Math.min(1.0d, defeatPercent / 100.0d));
		final double playerScore = Math.max(0.0d, Math.min(1.0d, decision.getTotalScore() / 35.0d));
		return Math.max(0.0d, Math.min(1.0d, (eventProgressScore * 0.55d) + (playerScore * 0.45d)));
	}

	private void sendPrepareWarnings() {
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"Uwaga! Faza przygotowania do obrony Kuźnic właśnie się rozpoczęła.");
		for (final String zoneName : getZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				LOGGER.warn(getEventName() + " PREPARE warning skipped; missing zone " + zoneName + ".");
				continue;
			}
			for (final Player player : zone.getPlayers()) {
				player.sendPrivateText("[Kuźnice] Faza PREPARE: zabezpieczcie wejścia i przygotujcie obronę.");
			}
		}
	}

	private void scheduleActivityTracker() {
		if (!isEventActive()) {
			return;
		}
		SingletonRepository.getTurnNotifier().notifyInSeconds(ACTIVITY_SAMPLE_INTERVAL_SECONDS, activityTracker);
	}

	private void recordPlayersInEventZones() {
		if (!isEventActive()) {
			return;
		}
		for (final String zoneName : getZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				continue;
			}
			for (final Player player : zone.getPlayers()) {
				contributionTracker.recordTimeInZone(player.getName(), ACTIVITY_SAMPLE_INTERVAL_SECONDS);
			}
		}
	}

	private void transitionTo(final EventPhase nextPhase, final String reason) {
		if (phase == nextPhase) {
			LOGGER.info(getEventName() + " phase " + nextPhase + " already active (reason=" + reason + ").");
			return;
		}
		if (phaseStartedAtMillis > 0L) {
			final long durationSeconds = Math.max(0L, (System.currentTimeMillis() - phaseStartedAtMillis) / 1000L);
			LOGGER.info(getEventName() + " phase " + phase + " stopped after " + durationSeconds + "s.");
		}
		LOGGER.info(getEventName() + " phase transition " + phase + " -> " + nextPhase + " (reason=" + reason + ").");
		phase = nextPhase;
		phaseStartedAtMillis = System.currentTimeMillis();
	}

	private void scheduleInSeconds(final int delaySeconds, final TurnListener listener) {
		if (!isEventActive()) {
			return;
		}
		scheduledListeners.put(System.identityHashCode(listener), listener);
		SingletonRepository.getTurnNotifier().notifyInSeconds(delaySeconds, listener);
	}

	private void clearScheduledListeners() {
		for (final TurnListener listener : scheduledListeners.values()) {
			SingletonRepository.getTurnNotifier().dontNotify(listener);
		}
		scheduledListeners.clear();
	}

	private void logAttackPlan() {
		final List<EventWave> attackWaves = getConfig().getWaves();
		int totalAttackSeconds = 0;
		for (EventWave wave : attackWaves) {
			totalAttackSeconds += Math.max(0, wave.getIntervalSeconds());
		}
		LOGGER.info(getEventName() + " ATTACK plan: waves=" + attackWaves.size()
				+ ", totalDuration=" + totalAttackSeconds + "s (required 480-720s).");
	}
}
