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
package games.stendhal.server.maps.dragon;

import java.time.LocalTime;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;
import games.stendhal.server.maps.event.MapEventContributionTracker;
import games.stendhal.server.maps.event.MapEventRewardPolicy;
import games.stendhal.server.maps.event.RandomEventRewardService;

public class DragonLandEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);
	private static final String WAWELSKI_DRAGON_NAME = "Smok Wawelski";
	private static final MapEventConfig EVENT_CONFIG = MapEventConfigLoader
			.load(MapEventConfigLoader.DRAGON_LAND_DEFAULT);
	private static final DragonLandEvent INSTANCE = new DragonLandEvent();
	private static final int MAX_WAVE_CYCLES = 2;
	private static final int RESTART_DEFEAT_PERCENT_THRESHOLD = 60;
	private static final int WAWELSKI_SPAWN_DEFEAT_PERCENT_THRESHOLD = 80;
	private static final int ACTIVITY_SAMPLE_INTERVAL_SECONDS = 10;
	private static final int MIN_DEFEAT_PERCENT_FOR_REWARD = 60;
	private static final EventSpawn WAWELSKI_LAST_WAVE_SPAWN = new EventSpawn(WAWELSKI_DRAGON_NAME, 1);

	private final AtomicBoolean wawelAnnounced = new AtomicBoolean(false);
	private final AtomicBoolean wavesRestartTriggered = new AtomicBoolean(false);
	private final AtomicBoolean isLastWaveActive = new AtomicBoolean(false);
	private final AtomicBoolean wawelskiSpawned = new AtomicBoolean(false);
	private final AtomicInteger totalSpawnedWithoutWawelski = new AtomicInteger(0);
	private final AtomicInteger defeatedWithoutWawelski = new AtomicInteger(0);
	private final Set<EventSpawn> lastWaveSpawns;
	private final List<String> wawelskiSpawnZones;
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
	private volatile SpawnReason spawnReason = SpawnReason.NONE;
	private volatile String wawelskiSpawnZone;
	private volatile String zoneOverride;
	private volatile int currentCycle = 1;

	private enum SpawnReason {
		NONE,
		LAST_WAVE_80_PERCENT
	}

	private DragonLandEvent() {
		super(LOGGER, EVENT_CONFIG);
		lastWaveSpawns = resolveLastWaveSpawns();
		wawelskiSpawnZones = DragonMapEventConfigProvider.getWawelskiSpawnZones();
	}

	public static DragonLandEvent getInstance() {
		return INSTANCE;
	}

	public static void registerZoneObserver(final StendhalRPZone zone) {
		INSTANCE.registerObserverZone(zone);
	}

	public static void scheduleEveryTwoDaysAt(final LocalTime time) {
		INSTANCE.scheduleGuaranteedStart(time, EVENT_CONFIG.getDefaultIntervalDays());
	}

	public static void scheduleGuaranteedEveryDaysAt(final LocalTime time, final int intervalDays) {
		INSTANCE.scheduleGuaranteedStart(time, intervalDays);
	}

	public static boolean forceStart() {
		return INSTANCE.forceStartEvent();
	}

	@Override
	protected void onStart() {
		wawelAnnounced.set(false);
		wavesRestartTriggered.set(false);
		isLastWaveActive.set(false);
		wawelskiSpawned.set(false);
		totalSpawnedWithoutWawelski.set(0);
		defeatedWithoutWawelski.set(0);
		spawnReason = SpawnReason.NONE;
		currentCycle = 1;
		wawelskiSpawnZone = null;
		zoneOverride = null;
		contributionTracker.clear();
		super.onStart();
		scheduleActivityTracker();
	}

	@Override
	protected void onStop() {
		SingletonRepository.getTurnNotifier().dontNotify(activityTracker);
		final int defeatPercent = getEventDefeatPercent();
		if (defeatPercent >= MIN_DEFEAT_PERCENT_FOR_REWARD) {
			rewardParticipants(defeatPercent, resolveDifficultyModifier(defeatPercent));
		}

		wavesRestartTriggered.set(false);
		isLastWaveActive.set(false);
		wawelskiSpawned.set(false);
		spawnReason = SpawnReason.NONE;
		totalSpawnedWithoutWawelski.set(0);
		defeatedWithoutWawelski.set(0);
		currentCycle = 1;
		wawelskiSpawnZone = null;
		zoneOverride = null;
		contributionTracker.clear();
		super.onStop();
	}

	@Override
	protected List<String> getZones() {
		if (zoneOverride != null) {
			return Collections.singletonList(zoneOverride);
		}
		return super.getZones();
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		if (lastWaveSpawns.contains(spawn)) {
			isLastWaveActive.set(true);
		}

		if (!WAWELSKI_DRAGON_NAME.equals(spawn.getCreatureName())) {
			totalSpawnedWithoutWawelski.addAndGet(Math.max(0, spawn.getCount()));
			return;
		}
		if (!wawelAnnounced.compareAndSet(false, true) || !isEventActive()) {
			return;
		}
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"W oddali słychać ryk… Smok Wawelski pojawił się w Smoczej Krainie!");
	}

	@Override
	protected void onEventCreatureDeath(final CircumstancesOfDeath circs) {
		super.onEventCreatureDeath(circs);
		if (circs != null && circs.getKiller() instanceof Player) {
			final Player killer = (Player) circs.getKiller();
			contributionTracker.recordKillAssist(killer.getName(), 1);
			contributionTracker.recordObjectiveAction(killer.getName(), 1);
		}
		if (circs != null && circs.getVictim() != null && !WAWELSKI_DRAGON_NAME.equals(circs.getVictim().getName())) {
			defeatedWithoutWawelski.incrementAndGet();
		}
		tryTriggerWawelskiSpawn();
		tryTriggerWaveRestart();
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

	private void rewardParticipants(final int defeatPercent, final double difficultyModifier) {
		final long now = System.currentTimeMillis();
		for (Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> entry : contributionTracker.snapshotAll().entrySet()) {
			final Player player = SingletonRepository.getRuleProcessor().getPlayer(entry.getKey());
			if (player == null) {
				continue;
			}
			final MapEventRewardPolicy.RewardDecision decision = rewardPolicy.evaluate(
					getEventId(),
					entry.getKey(),
					entry.getValue(),
					now);
			if (!decision.isQualified()) {
				continue;
			}
			final double participationScore = resolveParticipationScore(decision, defeatPercent);
			final RandomEventRewardService.Reward reward = randomEventRewardService.grantRandomEventRewards(
					player,
					RandomEventRewardService.RandomEventType.DRAGON_LAND,
					participationScore,
					difficultyModifier * decision.getMultiplier());
			player.sendPrivateText("Za obronę Smoczej Krainy otrzymujesz +" + reward.getXp()
					+ " PD oraz +" + Math.round(reward.getKarma() * 100.0d) / 100.0d + " karmy.");
		}
	}

	private double resolveParticipationScore(final MapEventRewardPolicy.RewardDecision decision, final int defeatPercent) {
		final double eventProgressScore = Math.max(0.0d, Math.min(1.0d, defeatPercent / 100.0d));
		final double playerScore = Math.max(0.0d, Math.min(1.0d, decision.getTotalScore() / 35.0d));
		return Math.max(0.0d, Math.min(1.0d, (eventProgressScore * 0.6d) + (playerScore * 0.4d)));
	}

	private double resolveDifficultyModifier(final int defeatPercent) {
		final double cycleDifficulty = 1.0d + ((Math.max(1, currentCycle) - 1) * 0.1d);
		final double progressDifficulty = 0.85d + (Math.max(0, Math.min(100, defeatPercent)) / 100.0d * 0.25d);
		return cycleDifficulty * progressDifficulty;
	}

	private void tryTriggerWawelskiSpawn() {
		if (!isEventActive() || currentCycle != 1 || !isLastWaveActive.get()) {
			return;
		}
		if (getDefeatPercentWithoutWawelski() < WAWELSKI_SPAWN_DEFEAT_PERCENT_THRESHOLD) {
			return;
		}
		if (!wawelskiSpawned.compareAndSet(false, true)) {
			return;
		}

		wawelskiSpawnZone = pickWawelskiSpawnZone();
		if (wawelskiSpawnZone == null) {
			LOGGER.warn(getEventName() + " no valid Wawelski spawn zone found. Falling back to default event zones.");
		}

		spawnReason = SpawnReason.LAST_WAVE_80_PERCENT;
		zoneOverride = wawelskiSpawnZone;
		try {
			spawnCreaturesForWave(WAWELSKI_LAST_WAVE_SPAWN);
		} finally {
			zoneOverride = null;
		}
	}

	private String pickWawelskiSpawnZone() {
		if (wawelskiSpawnZones == null || wawelskiSpawnZones.isEmpty()) {
			LOGGER.warn(getEventName() + " Wawelski spawn zones list is empty.");
			return null;
		}

		final long seed = System.nanoTime() ^ getCurrentEventRunId() ^ defeatedWithoutWawelski.get();
		final Random random = new Random(seed);
		final int roll = random.nextInt(wawelskiSpawnZones.size());
		LOGGER.debug(getEventName() + " Wawelski zone roll: seed=" + seed + ", roll=" + roll
				+ ", candidates=" + wawelskiSpawnZones + ".");

		for (int offset = 0; offset < wawelskiSpawnZones.size(); offset++) {
			final int candidateIndex = (roll + offset) % wawelskiSpawnZones.size();
			final String candidateZone = wawelskiSpawnZones.get(candidateIndex);
			if (isZoneAvailable(candidateZone)) {
				if (offset > 0) {
					LOGGER.debug(getEventName() + " Wawelski spawn fallback used: selected unavailable zone index="
							+ roll + ", chosen=" + candidateZone + ".");
				}
				LOGGER.debug(getEventName() + " Wawelski spawn zone chosen=" + candidateZone + ".");
				return candidateZone;
			}
			LOGGER.debug(getEventName() + " Wawelski spawn zone unavailable=" + candidateZone + ".");
		}

		return null;
	}

	private boolean isZoneAvailable(final String zoneName) {
		return zoneName != null
				&& !zoneName.trim().isEmpty()
				&& SingletonRepository.getRPWorld().getZone(zoneName) != null;
	}

	private void tryTriggerWaveRestart() {
		if (!isEventActive() || currentCycle >= MAX_WAVE_CYCLES) {
			return;
		}
		if (!isLastWaveActive.get() || getDefeatPercentWithoutWawelski() < RESTART_DEFEAT_PERCENT_THRESHOLD) {
			return;
		}
		if (currentCycle == 1 && !isWawelskiSpawnedFromLastWave80Percent()) {
			return;
		}
		if (!wavesRestartTriggered.compareAndSet(false, true)) {
			return;
		}

		currentCycle++;
		isLastWaveActive.set(false);
		scheduleWaveCycleRestart(currentCycle, getCurrentEventRunId());
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"Obrońcy, przygotujcie się! Nadciąga kolejna fala bestii!");
	}

	private boolean isWawelskiSpawnedFromLastWave80Percent() {
		return wawelskiSpawned.get() && spawnReason == SpawnReason.LAST_WAVE_80_PERCENT;
	}

	private void scheduleWaveCycleRestart(final int cycle, final long runId) {
		int delaySeconds = 0;
		for (final EventWave wave : getWaves()) {
			delaySeconds += wave.getIntervalSeconds();
			final EventWave scheduledWave = wave;
			final int scheduledCycle = cycle;
			final int scheduledDelay = delaySeconds;
			SingletonRepository.getTurnNotifier().notifyInSeconds(scheduledDelay, currentTurn -> {
				if (!isRunActive(runId) || scheduledCycle != currentCycle) {
					return;
				}
				for (final EventSpawn spawn : scheduledWave.getSpawns()) {
					spawnCreaturesForWave(spawn);
				}
			});
		}
	}

	private int getDefeatPercentWithoutWawelski() {
		final int total = Math.max(0, totalSpawnedWithoutWawelski.get());
		if (total <= 0) {
			return 0;
		}
		final int defeated = Math.min(total, Math.max(0, defeatedWithoutWawelski.get()));
		return Math.max(0, Math.min(100, (int) Math.round((defeated * 100.0d) / total)));
	}

	private Set<EventSpawn> resolveLastWaveSpawns() {
		final List<EventWave> waves = getWaves();
		if (waves.isEmpty()) {
			return Collections.emptySet();
		}
		final Set<EventSpawn> lastWaveSpawnSet = Collections.newSetFromMap(new IdentityHashMap<EventSpawn, Boolean>());
		lastWaveSpawnSet.addAll(waves.get(waves.size() - 1).getSpawns());
		return Collections.unmodifiableSet(lastWaveSpawnSet);
	}
}
