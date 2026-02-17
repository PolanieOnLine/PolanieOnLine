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
package games.stendhal.server.maps.event;

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

public class ConfiguredMapEvent extends BaseMapEvent {
	private final Logger logger;
	private final MapEventSpawnStrategy spawnStrategy;
	private final KillThresholdTrigger killThresholdTrigger;
	private final MapEventConfig.ScalingConfig scalingConfig;
	private final Map<BaseMapEvent.EventSpawn, Integer> spawnWaveIndexes;
	private final List<Integer> waveBaseTotals;
	private final Map<Integer, WaveScaleState> waveScaleStates = new HashMap<>();
	private final Map<Creature, Integer> creatureWaveIndexes = new HashMap<>();
	private final List<Double> completedWaveClearTimesSec = new ArrayList<>();
	private volatile boolean scriptForceStartRequested;
	private volatile int activeSpawningWaveIndex = -1;

	public ConfiguredMapEvent(final Logger logger, final MapEventConfig config) {
		this(logger, config, new RandomSafeSpotSpawnStrategy(logger));
	}

	public ConfiguredMapEvent(final Logger logger, final MapEventConfig config,
			final MapEventSpawnStrategy spawnStrategy) {
		super(logger, config);
		this.logger = Objects.requireNonNull(logger, "logger");
		this.spawnStrategy = Objects.requireNonNull(spawnStrategy, "spawnStrategy");
		scalingConfig = getConfig().getScaling();
		spawnWaveIndexes = createSpawnWaveIndexes(getConfig().getWaves());
		waveBaseTotals = createWaveBaseTotals(getConfig().getWaves());
		if (getConfig().getTriggerThreshold() > 0) {
			killThresholdTrigger = new KillThresholdTrigger(
					getObserverZones(),
					circs -> !isEventActive() && getCreatureFilter().contains(circs.getVictim().getName()),
					getConfig().getTriggerThreshold(),
					this::startEventFromKills);
		} else {
			killThresholdTrigger = null;
			logger.info(getEventName() + " event configured without kill-trigger (triggerThreshold="
					+ getConfig().getTriggerThreshold() + ").");
		}
	}

	public final void registerObserverZone(final StendhalRPZone zone) {
		if (killThresholdTrigger == null) {
			return;
		}
		killThresholdTrigger.registerZoneObserver(Objects.requireNonNull(zone, "zone"));
	}

	public final void scheduleGuaranteedStart(final LocalTime time, final int intervalDays) {
		scheduleEveryDaysAt(time, intervalDays);
	}

	public final boolean forceStartEvent() {
		return startFromScript(true);
	}

	public final String getEventDisplayName() {
		return getEventName();
	}

	public final boolean startFromScript(final boolean force) {
		scriptForceStartRequested = force;
		try {
			if (!startEvent()) {
				logger.warn(getEventName() + " event already active; refusing "
						+ (force ? "forced" : "safe") + " script start.");
				return false;
			}
			return true;
		} finally {
			scriptForceStartRequested = false;
		}
	}

	protected final boolean isScriptForceStartRequested() {
		return scriptForceStartRequested;
	}

	protected String getStartAnnouncementMessage() {
		final String configuredMessage = getConfig().getStartAnnouncement();
		if (configuredMessage != null) {
			return configuredMessage;
		}
		return "Rozpoczyna się wydarzenie: " + getEventName() + ".";
	}

	protected String getStopAnnouncementMessage() {
		final String configuredMessage = getConfig().getStopAnnouncement();
		if (configuredMessage != null) {
			return configuredMessage;
		}
		return "Wydarzenie " + getEventName() + " dobiegło końca.";
	}

	@Override
	protected void onStart() {
		if (killThresholdTrigger != null) {
			killThresholdTrigger.resetCounter("event started");
		}
		waveScaleStates.clear();
		completedWaveClearTimesSec.clear();
		synchronized (creatureWaveIndexes) {
			creatureWaveIndexes.clear();
		}
		logger.info(getEventName() + " event started.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				getStartAnnouncementMessage());
	}

	@Override
	protected void onStop() {
		if (killThresholdTrigger != null) {
			killThresholdTrigger.resetCounter("event ended");
		}
		logger.info(getEventName() + " event ended.");
		waveScaleStates.clear();
		completedWaveClearTimesSec.clear();
		synchronized (creatureWaveIndexes) {
			creatureWaveIndexes.clear();
		}
		removeEventCreatures();
		stopAnnouncements();
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				getStopAnnouncementMessage());
	}

	@Override
	protected void spawnCreaturesForWave(final EventSpawn spawn) {
		final int waveIndex = spawnWaveIndexes.containsKey(spawn) ? spawnWaveIndexes.get(spawn) : -1;
		if (waveIndex < 0 || scalingConfig == null) {
			super.spawnCreaturesForWave(spawn);
			return;
		}

		final WaveScaleState waveState = waveScaleStates.computeIfAbsent(waveIndex, this::createWaveScaleState);
		final int baseTotal = Math.max(1, waveState.baseWaveTotal);
		final int nextBaseSoFar = waveState.baseAssigned + spawn.getCount();
		final int targetUntilCurrentSpawn = (int) Math.round((nextBaseSoFar / (double) baseTotal) * waveState.scaledWaveTarget);
		final int scaledSpawnCount = Math.max(0, targetUntilCurrentSpawn - waveState.scaledAssigned);
		waveState.baseAssigned = nextBaseSoFar;
		waveState.scaledAssigned += scaledSpawnCount;
		waveState.pendingCreatures += scaledSpawnCount;

		logger.debug(getEventName() + " wave " + (waveIndex + 1)
				+ " spawn scaling for creature " + spawn.getCreatureName()
				+ ": base=" + spawn.getCount()
				+ ", scaled=" + scaledSpawnCount
				+ ", totalBase=" + waveState.baseWaveTotal
				+ ", totalScaled=" + waveState.scaledWaveTarget + ".");

		activeSpawningWaveIndex = waveIndex;
		try {
			spawnCreatures(spawn.getCreatureName(), scaledSpawnCount);
		} finally {
			activeSpawningWaveIndex = -1;
		}
	}

	@Override
	protected void spawnCreatures(final String creatureName, final int count) {
		for (final String zoneName : getZones()) {
			final int requestedCount = count;
			final double spawnMultiplier = getConfig().getZoneSpawnMultiplier(zoneName);
			final int multipliedCount = (int) Math.round(requestedCount * spawnMultiplier);
			final Integer zoneSpawnCap = getConfig().getZoneSpawnCap(zoneName);
			final int finalSpawnCount = zoneSpawnCap == null ? multipliedCount : Math.min(multipliedCount, zoneSpawnCap);

			logger.debug(getEventName() + " spawn request for zone " + zoneName + ": requested=" + requestedCount
					+ ", multiplier=" + spawnMultiplier + ", multiplied=" + multipliedCount
					+ (zoneSpawnCap == null ? "" : ", cap=" + zoneSpawnCap)
					+ ", final=" + finalSpawnCount + ".");

			if (finalSpawnCount <= 0) {
				logger.debug(getEventName() + " spawn skipped in zone " + zoneName
						+ "; final spawn count is 0 after multiplier/cap.");
				continue;
			}

			final int spawningWaveIndex = activeSpawningWaveIndex;
			spawnStrategy.spawnCreatures(
					getEventName(),
					Collections.singletonList(zoneName),
					creatureName,
					finalSpawnCount,
					creature -> {
						registerEventCreature(creature);
						if (spawningWaveIndex >= 0) {
							synchronized (creatureWaveIndexes) {
								creatureWaveIndexes.put(creature, spawningWaveIndex);
							}
						}
					});
		}
	}

	@Override
	protected void onEventCreatureDeath(final CircumstancesOfDeath circs) {
		if (scalingConfig == null || circs == null || circs.getVictim() == null) {
			return;
		}
		final Integer waveIndex;
		synchronized (creatureWaveIndexes) {
			waveIndex = creatureWaveIndexes.remove(circs.getVictim());
		}
		if (waveIndex == null) {
			return;
		}
		final WaveScaleState waveState = waveScaleStates.get(waveIndex);
		if (waveState == null || waveState.pendingCreatures <= 0) {
			return;
		}
		waveState.pendingCreatures--;
		if (waveState.pendingCreatures == 0) {
			final double clearTimeSec = Math.max(1d, waveState.startedAt.until(currentInstant(), java.time.temporal.ChronoUnit.MILLIS) / 1000d);
			completedWaveClearTimesSec.add(clearTimeSec);
			logger.debug(getEventName() + " wave " + (waveIndex + 1) + " cleared in " + clearTimeSec + "s.");
		}
	}

	protected Instant currentInstant() {
		return Instant.now();
	}

	protected int countPlayersInZones() {
		if (scalingConfig != null) {
			return countActivePlayersInZones(
					scalingConfig.getOnlineZoneMinPlayerLevel(),
					scalingConfig.getOnlineZoneMaxPlayerLevel());
		}
		return countActivePlayersInZones(0, Integer.MAX_VALUE);
	}

	protected int countActivePlayersInZones(final int minLevelInclusive, final int maxLevelInclusive) {
		final int minLevel = Math.max(0, minLevelInclusive);
		final int maxLevel = Math.max(minLevel, maxLevelInclusive);
		int players = 0;
		for (final String zoneName : getZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				continue;
			}
			for (final Player player : zone.getPlayers()) {
				if (player == null || player.isGhost() || player.isDisconnected()) {
					continue;
				}
				final int level = player.getLevel();
				if (level < minLevel || level > maxLevel) {
					continue;
				}
				players++;
			}
		}
		return players;
	}

	private WaveScaleState createWaveScaleState(final int waveIndex) {
		final int baseWaveTotal = waveBaseTotals.get(waveIndex);
		final int onlinePlayers = countPlayersInZones();
		final double onlineScale = calculateOnlineScale(onlinePlayers);
		final double clearRateScale = calculateClearRateScale(waveIndex);
		final double combinedScale = onlineScale * clearRateScale;
		final int scaledWaveTarget = clampSpawnPerWave((int) Math.round(baseWaveTotal * combinedScale));
		logger.info(getEventName() + " wave " + (waveIndex + 1)
				+ " scaling: onlinePlayers=" + onlinePlayers
				+ ", onlineScale=" + onlineScale
				+ ", clearRateScale=" + clearRateScale
				+ ", combinedScale=" + combinedScale
				+ ", baseSpawn=" + baseWaveTotal
				+ ", finalSpawn=" + scaledWaveTarget + ".");
		return new WaveScaleState(baseWaveTotal, scaledWaveTarget, currentInstant());
	}

	private double calculateOnlineScale(final int onlinePlayers) {
		if (scalingConfig == null || !scalingConfig.isScaleByOnlineInZones()) {
			return 1.0d;
		}
		final int minPlayers = scalingConfig.getMinPlayers();
		if (minPlayers <= 0) {
			return 1.0d;
		}
		final int cappedPlayers = scalingConfig.getMaxPlayers() > 0
				? Math.min(onlinePlayers, scalingConfig.getMaxPlayers())
				: onlinePlayers;
		return Math.max(0.2d, cappedPlayers / (double) minPlayers);
	}

	private double calculateClearRateScale(final int waveIndex) {
		if (scalingConfig == null || completedWaveClearTimesSec.isEmpty() || waveIndex <= 0) {
			return 1.0d;
		}
		final int completedWaves = Math.min(completedWaveClearTimesSec.size(), waveIndex);
		double avgExpectedSec = 0d;
		for (int i = 0; i < completedWaves; i++) {
			avgExpectedSec += getConfig().getWaves().get(i).getIntervalSeconds();
		}
		avgExpectedSec /= completedWaves;
		double avgClearSec = 0d;
		for (int i = completedWaveClearTimesSec.size() - completedWaves; i < completedWaveClearTimesSec.size(); i++) {
			avgClearSec += completedWaveClearTimesSec.get(i);
		}
		avgClearSec /= completedWaves;
		if (avgExpectedSec <= 0d || avgClearSec <= 0d) {
			return 1.0d;
		}
		final double rawRatio = avgExpectedSec / avgClearSec;
		return 1.0d + ((rawRatio - 1.0d) * scalingConfig.getKillRateMultiplier());
	}

	private int clampSpawnPerWave(final int desiredSpawnCount) {
		if (scalingConfig == null) {
			return desiredSpawnCount;
		}
		final int minSpawn = scalingConfig.getMinSpawnPerWave();
		final int maxSpawn = scalingConfig.getMaxSpawnPerWave();
		return Math.max(minSpawn, Math.min(desiredSpawnCount, maxSpawn));
	}

	private static Map<BaseMapEvent.EventSpawn, Integer> createSpawnWaveIndexes(final List<BaseMapEvent.EventWave> waves) {
		final Map<BaseMapEvent.EventSpawn, Integer> indexes = new IdentityHashMap<>();
		for (int waveIndex = 0; waveIndex < waves.size(); waveIndex++) {
			for (BaseMapEvent.EventSpawn spawn : waves.get(waveIndex).getSpawns()) {
				indexes.put(spawn, waveIndex);
			}
		}
		return indexes;
	}

	private static List<Integer> createWaveBaseTotals(final List<BaseMapEvent.EventWave> waves) {
		final List<Integer> totals = new ArrayList<>();
		for (BaseMapEvent.EventWave wave : waves) {
			int total = 0;
			for (BaseMapEvent.EventSpawn spawn : wave.getSpawns()) {
				total += spawn.getCount();
			}
			totals.add(total);
		}
		return totals;
	}

	private void startEventFromKills() {
		if (!startEvent()) {
			logger.warn(getEventName() + " event already active; skipping duplicate start.");
		}
	}

	private static final class WaveScaleState {
		private final int baseWaveTotal;
		private final int scaledWaveTarget;
		private final Instant startedAt;
		private int baseAssigned;
		private int scaledAssigned;
		private int pendingCreatures;

		private WaveScaleState(final int baseWaveTotal, final int scaledWaveTarget, final Instant startedAt) {
			this.baseWaveTotal = baseWaveTotal;
			this.scaledWaveTarget = scaledWaveTarget;
			this.startedAt = startedAt;
		}
	}
}
