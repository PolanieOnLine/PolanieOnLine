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
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;
import games.stendhal.server.maps.event.MapEventContributionTracker;
import games.stendhal.server.maps.event.MapEventRewardPolicy;
import games.stendhal.server.maps.event.MapEventRewardSettlementService;
import games.stendhal.server.maps.event.RandomEventRewardService;
import marauroa.server.game.container.PlayerEntry;
import marauroa.server.game.container.PlayerEntryContainer;

public class TatryKuzniceBanditRaidEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(TatryKuzniceBanditRaidEvent.class);
	private static final MapEventConfig EVENT_CONFIG = MapEventConfigLoader.load(MapEventConfigLoader.TATRY_KUZNICE_BANDIT_RAID);
	private static final TatryKuzniceBanditRaidEvent INSTANCE = new TatryKuzniceBanditRaidEvent();

	private static final String BANDIT_COMMANDER_NAME = "zbójnik górski herszt";
	private static final int PREPARE_DURATION_SECONDS = 90;
	private static final int FINAL_PHASE_TIMEOUT_SECONDS = 150;
	private static final int ACTIVITY_SAMPLE_INTERVAL_SECONDS = 10;
	private static final int COMMANDER_AOE_INTERVAL_SECONDS = 22;
	private static final int COMMANDER_AOE_TELEGRAPH_SECONDS = 4;
	private static final int COMMANDER_AOE_RADIUS_TILES = 3;
	private static final int COMMANDER_AOE_WARNING_RADIUS_TILES = 12;
	private static final int COMMANDER_AOE_DAMAGE = 140;
	private static final int GLOBAL_SUCCESS_THRESHOLD_PERCENT = 100;
	private static final int GLOBAL_SUCCESS_MONEY_BONUS = 300;

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
	private volatile boolean commanderAoeTelegraphPending;
	private volatile int attackWavesTotal;
	private volatile int currentAttackWave;

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
		commanderAoeTelegraphPending = false;
		attackWavesTotal = Math.max(0, getConfig().getWaves().size());
		currentAttackWave = 0;
		setWaveProgress(currentAttackWave, attackWavesTotal);
		transitionTo(EventPhase.PREPARE, "event started");
		sendPrepareWarnings();
		logAttackPlan();
		LOGGER.info(getEventName() + " active defenders (lvl 20-150) at start: " + countEligibleDefendersInEventZones() + ".");
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
		commanderAoeTelegraphPending = false;
		SingletonRepository.getTurnNotifier().dontNotify(activityTracker);
		handleSettlement("event stopped");
		super.onStop();
	}

	@Override
	protected void onEventCreatureDeath(final CircumstancesOfDeath circs) {
		super.onEventCreatureDeath(circs);
		if (circs != null && circs.getKiller() instanceof Player) {
			final Player killer = (Player) circs.getKiller();
			contributionTracker.recordKillCount(killer.getName(), 1);
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
		currentAttackWave = Math.max(currentAttackWave, waveIndex + 1);
		setWaveProgress(currentAttackWave, attackWavesTotal);
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
				"Herszt zbójników wyszedł z ukrycia w Kuźnicach! Złóżcie go w śnieg - bez niego napad się rozsypie.");
		scheduleCommanderAreaStrikeLoop();
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

	private void scheduleCommanderAreaStrikeLoop() {
		if (!isEventActive() || phase != EventPhase.FINAL) {
			return;
		}
		scheduleInSeconds(COMMANDER_AOE_INTERVAL_SECONDS, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				if (!isEventActive() || phase != EventPhase.FINAL || commanderAoeTelegraphPending) {
					return;
				}
				triggerCommanderAreaStrikeTelegraph();
			}
		});
	}

	private void triggerCommanderAreaStrikeTelegraph() {
		final Creature commander = findActiveCommander();
		if (commander == null || commander.getZone() == null) {
			scheduleCommanderAreaStrikeLoop();
			return;
		}
		commanderAoeTelegraphPending = true;
		final int maxWarningSquaredDistance = COMMANDER_AOE_WARNING_RADIUS_TILES * COMMANDER_AOE_WARNING_RADIUS_TILES;
		for (final Player player : commander.getZone().getPlayers()) {
			if (player == null || player.isGhost() || player.isDisconnected()) {
				continue;
			}
			if (commander.squaredDistance(player) > maxWarningSquaredDistance) {
				continue;
			}
			player.sendPrivateText(NotificationType.WARNING,
					"Uwaga! Herszt bierze zamach - odskocz teraz, za chwilę uderzy dookoła!");
		}
		scheduleInSeconds(COMMANDER_AOE_TELEGRAPH_SECONDS, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				executeCommanderAreaStrike();
			}
		});
	}

	private void executeCommanderAreaStrike() {
		commanderAoeTelegraphPending = false;
		if (!isEventActive() || phase != EventPhase.FINAL) {
			return;
		}
		final Creature commander = findActiveCommander();
		if (commander == null || commander.getZone() == null) {
			scheduleCommanderAreaStrikeLoop();
			return;
		}
		final int maxSquaredDistance = COMMANDER_AOE_RADIUS_TILES * COMMANDER_AOE_RADIUS_TILES;
		int hitPlayers = 0;
		for (final Player player : commander.getZone().getPlayers()) {
			if (player == null || player.isGhost() || player.isDisconnected()) {
				continue;
			}
			if (commander.squaredDistance(player) > maxSquaredDistance) {
				continue;
			}
			player.onDamaged(commander, COMMANDER_AOE_DAMAGE);
			player.sendPrivateText(NotificationType.DAMAGE,
					"Dostajesz falą uderzeniową herszta: " + COMMANDER_AOE_DAMAGE + " obrażeń.");
			hitPlayers++;
		}
		LOGGER.info(getEventName() + " commander area strike resolved: radius=" + COMMANDER_AOE_RADIUS_TILES
				+ ", damage=" + COMMANDER_AOE_DAMAGE + ", hitPlayers=" + hitPlayers + ".");
		scheduleCommanderAreaStrikeLoop();
	}

	private Creature findActiveCommander() {
		for (final Creature creature : getEventCreaturesSnapshot()) {
			if (creature != null && BANDIT_COMMANDER_NAME.equals(creature.getName()) && creature.getHP() > 0
					&& creature.getZone() != null) {
				return creature;
			}
		}
		return null;
	}

	private void handleSettlement(final String reason) {
		if (!settlementHandled.compareAndSet(false, true)) {
			return;
		}
		transitionTo(EventPhase.SETTLEMENT, reason);
		rewardParticipants();
		contributionTracker.clear();
		currentAttackWave = 0;
		setWaveProgress(currentAttackWave, attackWavesTotal);
		phase = EventPhase.PREPARE;
		phaseStartedAtMillis = 0L;
		LOGGER.info(getEventName() + " settlement completed and event state cleared.");
	}

	@Override
	protected List<String> getActivityTop() {
		return MapEventRewardSettlementService.buildActivityTop(contributionTracker);
	}

	private void rewardParticipants() {
		final int defeatPercent = getEventDefeatPercent();
		final boolean globalSuccess = defeatPercent >= GLOBAL_SUCCESS_THRESHOLD_PERCENT;
		final int awardedChests = new MapEventRewardSettlementService(
				getEventId(),
				contributionTracker,
				rewardPolicy,
				new MapEventRewardSettlementService.RewardGrantCallback() {
					@Override
					public void grant(final MapEventRewardSettlementService.RewardContext context) {
						final MapEventRewardPolicy.RewardDecision decision = context.getDecision();
						final double participationScore = resolveParticipationScore(decision, defeatPercent);
						final int points = Math.max(0, (int) Math.round(decision.getTotalScore()));
						final RewardTier rewardTier = RewardTier.fromPoints(points);
						final int baseMoney = rewardTier.getBaseMoney();
						final int globalBonusMoney = globalSuccess ? GLOBAL_SUCCESS_MONEY_BONUS : 0;
						final int awardedMoney = Math.max(0,
								(int) Math.round((baseMoney + globalBonusMoney) * decision.getMultiplier()));
						final RandomEventRewardService.Reward reward = randomEventRewardService.grantRandomEventRewards(
								context.getPlayer(),
								RandomEventRewardService.RandomEventType.KUZNICE_BANDIT_RAID,
								participationScore,
								decision.getMultiplier());
						if (awardedMoney > 0) {
							MoneyUtils.giveMoney(context.getPlayer(), awardedMoney);
						}
						context.getPlayer().sendPrivateText(NotificationType.POSITIVE,
								"Gazdowie z Kuźnic kiwają z uznaniem. Za obronę dostajesz +"
								+ reward.getXp() + " PD oraz +" + Math.round(reward.getKarma() * 100.0d) / 100.0d + " karmy.");
					}
				},
				"Kuźnice").settleRewards(MapEventRewardSettlementService.SettlementOptions.of(
						new MapEventRewardSettlementService.AccountNameResolver() {
							@Override
							public String resolve(final Player player) {
								return resolveAccountName(player);
							}
						},
						null,
						new MapEventRewardSettlementService.EligibilityDecider() {
							@Override
							public boolean isEligible(final MapEventRewardSettlementService.RewardContext context) {
								final MapEventRewardPolicy.RewardDecision decision = context.getDecision();
								if (!decision.isFullParticipation()) {
									context.getPlayer().sendPrivateText(NotificationType.WARNING,
										"Twój poziom jest zbyt niski na pełny udział w nagrodach eventu (minimum 20).");
									return false;
								}
								if (!decision.isPrimaryRewardEligible()) {
									if (decision.shouldGrantSymbolicRewardOnly()) {
										context.getPlayer().addKarma(1.0d);
										context.getPlayer().sendPrivateText(NotificationType.INFORMATION,
											"Za wsparcie obrony Kuźnic otrzymujesz symboliczną nagrodę +1 karmy.");
									}
									return false;
								}
								final int points = Math.max(0, (int) Math.round(decision.getTotalScore()));
								return RewardTier.fromPoints(points).isRewardTier();
							}
						}));
		LOGGER.info(getEventName() + " settlement rewards granted; chest rewards=" + awardedChests + ".");
	}

	private String resolveAccountName(final Player player) {
		final PlayerEntry entry = PlayerEntryContainer.getContainer().get(player);
		if (entry == null || entry.username == null) {
			return null;
		}
		return entry.username;
	}

	private double resolveParticipationScore(final MapEventRewardPolicy.RewardDecision decision, final int defeatPercent) {
		final double eventProgressScore = Math.max(0.0d, Math.min(1.0d, defeatPercent / 100.0d));
		final double playerScore = Math.max(0.0d, Math.min(1.0d, decision.getTotalScore() / 35.0d));
		return Math.max(0.0d, Math.min(1.0d, (eventProgressScore * 0.55d) + (playerScore * 0.45d)));
	}

	private void sendPrepareWarnings() {
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"Kuźnice biją na alarm! Zbóje schodzą z gór - zbierzcie się i szykujcie obronę, nim uderzą.");
		for (final String zoneName : getZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				LOGGER.warn(getEventName() + " PREPARE warning skipped; missing zone " + zoneName + ".");
				continue;
			}
			for (final Player player : zone.getPlayers()) {
				player.sendPrivateText(NotificationType.SCENE_SETTING,
						"Rogi grają… Zabezpieczcie przejścia i trzymajcie szyk. Za chwilę spadnie na nas zbójecka wataha.");
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
		// Presence time no longer contributes to reward qualification/ranking.
		// This sampling hook is intentionally kept as a no-op for future telemetry reuse.
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

	private int countEligibleDefendersInEventZones() {
		return countActivePlayersInZones(20, 150);
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

	private enum RewardTier {
		NONE("Brak", 0),
		BRONZE("Brąz", 800),
		SILVER("Srebro", 1600),
		GOLD("Złoto", 2800);

		private final String displayName;
		private final int baseMoney;

		RewardTier(final String displayName, final int baseMoney) {
			this.displayName = displayName;
			this.baseMoney = baseMoney;
		}

		private static RewardTier fromPoints(final int points) {
			if (points >= 60) {
				return GOLD;
			}
			if (points >= 30) {
				return SILVER;
			}
			if (points >= 15) {
				return BRONZE;
			}
			return NONE;
		}

		private boolean isRewardTier() {
			return this != NONE;
		}

		@SuppressWarnings("unused")
		private String getDisplayName() {
			return displayName;
		}

		private int getBaseMoney() {
			return baseMoney;
		}
	}
}
