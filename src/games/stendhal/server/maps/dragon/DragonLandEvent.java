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
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;

public class DragonLandEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);
	private static final MapEventConfig EVENT_CONFIG = MapEventConfigLoader
			.load(MapEventConfigLoader.DRAGON_LAND_DEFAULT);
	private static final DragonLandEvent INSTANCE = new DragonLandEvent();
	private static final int MAX_WAVE_CYCLES = 2;
	private static final int RESTART_DEFEAT_PERCENT_THRESHOLD = 60;

	private final AtomicBoolean wawelAnnounced = new AtomicBoolean(false);
	private final AtomicBoolean wavesRestartTriggered = new AtomicBoolean(false);
	private final AtomicBoolean isLastWaveActive = new AtomicBoolean(false);
	private final Set<EventSpawn> lastWaveSpawns;
	private volatile int currentCycle = 1;

	private DragonLandEvent() {
		super(LOGGER, EVENT_CONFIG);
		lastWaveSpawns = resolveLastWaveSpawns();
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
		currentCycle = 1;
		super.onStart();
	}

	@Override
	protected void onStop() {
		wavesRestartTriggered.set(false);
		isLastWaveActive.set(false);
		currentCycle = 1;
		super.onStop();
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		if (lastWaveSpawns.contains(spawn)) {
			isLastWaveActive.set(true);
		}

		if (!"Smok Wawelski".equals(spawn.getCreatureName())) {
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
		tryTriggerWaveRestart();
	}

	private void tryTriggerWaveRestart() {
		if (!isEventActive() || currentCycle >= MAX_WAVE_CYCLES) {
			return;
		}
		if (!isLastWaveActive.get() || getEventDefeatPercent() < RESTART_DEFEAT_PERCENT_THRESHOLD) {
			return;
		}
		if (!wavesRestartTriggered.compareAndSet(false, true)) {
			return;
		}

		currentCycle++;
		isLastWaveActive.set(false);
		scheduleWaveCycleRestart(currentCycle, getCurrentEventRunId());
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"Smocza Kraina: rozpoczyna się kolejna faza obrony! Nadciąga druga sekwencja fal.");
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

	private Set<EventSpawn> resolveLastWaveSpawns() {
		final List<EventWave> waves = getWaves();
		if (waves.isEmpty()) {
			return Collections.emptySet();
		}
		final Set<EventSpawn> lastWaveSpawnSet = Collections.newSetFromMap(new IdentityHashMap<>());
		lastWaveSpawnSet.addAll(waves.get(waves.size() - 1).getSpawns());
		return Collections.unmodifiableSet(lastWaveSpawnSet);
	}
}
