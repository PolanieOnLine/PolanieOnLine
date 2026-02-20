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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.ZoneAttributes;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.rp.WeatherUpdater;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.WeatherEntity;
import games.stendhal.server.util.Observable;
import games.stendhal.server.util.Observer;
import marauroa.common.Pair;

public abstract class BaseMapEvent {
	private final Logger logger;
	private final MapEventConfig config;
	private final AtomicBoolean scheduled = new AtomicBoolean(false);
	private final AtomicBoolean eventActive = new AtomicBoolean(false);
	private final AtomicLong eventRunId = new AtomicLong(0L);
	private final Map<String, Pair<String, Boolean>> storedWeather = new HashMap<>();
	private final Map<String, String> storedWeatherLock = new HashMap<>();
	private final List<Creature> eventCreatures = Collections.synchronizedList(new ArrayList<>());
	private final Map<Creature, Long> eventCreatureRunIds = Collections.synchronizedMap(new HashMap<Creature, Long>());
	private final AtomicInteger eventTotalSpawnedCreatures = new AtomicInteger(0);
	private final AtomicInteger eventDefeatedCreatures = new AtomicInteger(0);
	private final AtomicInteger currentWave = new AtomicInteger(0);
	private final AtomicInteger totalWaves = new AtomicInteger(0);
	private final Observer eventCreatureObserver = new EventCreatureObserver();
	private volatile TurnListener activeAnnouncer;
	private volatile LocalTime scheduledTime;
	private volatile LocalDate lastGuaranteedStartDate;
	private volatile int guaranteedIntervalDays;
	private volatile long scheduledEndEpochSeconds;
	private volatile String eventId;

	protected BaseMapEvent(final Logger logger, final MapEventConfig config) {
		this.logger = Objects.requireNonNull(logger, "logger");
		this.config = Objects.requireNonNull(config, "config");
	}

	protected String getEventName() {
		return config.getEventName();
	}

	final String getEventNamePublic() {
		return getEventName();
	}

	protected Duration getEventDuration() {
		return config.getDuration();
	}

	protected List<String> getZones() {
		return config.getZones();
	}

	final List<String> getZonesPublic() {
		return getZones();
	}

	protected List<String> getObserverZones() {
		return config.getObserverZones();
	}

	protected Set<String> getCreatureFilter() {
		return config.getCreatureFilter();
	}

	protected List<EventWave> getWaves() {
		return config.getWaves();
	}

	protected List<String> getAnnouncements() {
		return config.getAnnouncements();
	}

	protected abstract void onStart();

	protected abstract void onStop();

	protected abstract void spawnCreatures(String creatureName, int count);

	protected void spawnCreaturesForWave(final EventSpawn spawn) {
		onWaveSpawn(spawn);
		spawnCreatures(spawn.creatureName, spawn.count);
	}

	protected void onWaveSpawn(final EventSpawn spawn) {
		// default no-op
	}

	protected final MapEventConfig getConfig() {
		return config;
	}

	protected int getAnnouncementIntervalSeconds() {
		return config.getAnnouncementIntervalSeconds();
	}

	protected final boolean isEventActive() {
		return eventActive.get();
	}

	final boolean isEventActivePublic() {
		return isEventActive();
	}

	final int getTotalDurationSeconds() {
		return (int) getEventDuration().getSeconds();
	}

	final int getRemainingSeconds() {
		if (!isEventActive()) {
			return 0;
		}
		final long now = Instant.now().getEpochSecond();
		return (int) Math.max(0, scheduledEndEpochSeconds - now);
	}

	final int getCurrentWave() {
		return Math.max(0, currentWave.get());
	}

	final int getTotalWaves() {
		return Math.max(0, totalWaves.get());
	}

	final String getDefenseStatus() {
		if (!isEventActive()) {
			return "Wydarzenie zakończone";
		}
		if (getEventDefeatPercent() >= 100) {
			return "Obrona zakończona sukcesem";
		}
		if (getRemainingSeconds() <= 30) {
			return "Końcowe starcie";
		}
		return "Obrona w toku";
	}

	protected List<String> getActivityTop() {
		return Collections.emptyList();
	}

	protected void onStatusTick() {
		// default no-op
	}

	protected String getCapturePointsStatusPayload() {
		return null;
	}

	protected final void setWaveProgress(final int current, final int total) {
		currentWave.set(Math.max(0, current));
		totalWaves.set(Math.max(0, total));
	}

	final void setEventIdIfMissing(final String candidateEventId) {
		if (candidateEventId == null || candidateEventId.trim().isEmpty() || eventId != null) {
			return;
		}
		eventId = candidateEventId;
	}

	protected final String getEventId() {
		return eventId == null ? getEventName().toLowerCase().replace(' ', '_') : eventId;
	}

	protected final boolean startEvent() {
		if (!eventActive.compareAndSet(false, true)) {
			return false;
		}
		startEventInternal();
		return true;
	}

	protected final boolean startGuaranteedEvent() {
		if (!eventActive.compareAndSet(false, true)) {
			return false;
		}
		lastGuaranteedStartDate = LocalDate.now();
		logger.info(getEventName() + " guaranteed event start recorded for " + lastGuaranteedStartDate + ".");
		startEventInternal();
		return true;
	}

	protected final void endEvent() {
		endEvent(eventRunId.get());
	}

	private void endEvent(final long runId) {
		if (!isCurrentRunActive(runId)) {
			return;
		}
		if (!eventActive.compareAndSet(true, false)) {
			return;
		}
		activeAnnouncer = null;
		scheduledEndEpochSeconds = 0L;
		onStop();
		MapEventStatusPublisher.broadcastInactiveEventStatus(this);
		restoreWeatherFromConfig();
		if (scheduledTime != null) {
			scheduleNextGuaranteedCheck();
		}
	}

	protected final boolean scheduleEveryDaysAt(final LocalTime time, final int intervalDays) {
		Objects.requireNonNull(time, "time");
		if (!scheduled.compareAndSet(false, true)) {
			logger.debug(getEventName() + " event already scheduled; skipping duplicate schedule.");
			return false;
		}
		scheduledTime = time;
		guaranteedIntervalDays = intervalDays;
		scheduleNextGuaranteedCheck();
		return true;
	}

	protected final void scheduleNextGuaranteedCheck() {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime nextCheckTime = nextOccurrence(scheduledTime, now);
		int waitSec = (int) Duration.between(now, nextCheckTime).getSeconds();
		logger.info(getEventName() + " guaranteed event check scheduled in " + waitSec + " seconds at " + nextCheckTime + ".");
		SingletonRepository.getTurnNotifier().notifyInSeconds(waitSec, currentTurn -> attemptGuaranteedStart());
	}

	protected final void lockWeather(final String weather, final boolean thundering) {
		storedWeather.clear();
		storedWeatherLock.clear();
		for (final String zoneName : getZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				logger.warn(getEventName() + " zone not found for weather update: " + zoneName + ".");
				continue;
			}
			final ZoneAttributes attributes = zone.getAttributes();
			final WeatherEntity weatherEntity = zone.getWeatherEntity();
			if (attributes == null || weatherEntity == null) {
				logger.warn(getEventName() + " zone lacks weather support: " + zoneName + ".");
				continue;
			}
			final String currentWeather = attributes.get("weather");
			storedWeather.put(zoneName, new Pair<>(currentWeather, weatherEntity.isThundering()));
			storedWeatherLock.put(zoneName, attributes.get(WeatherUpdater.WEATHER_LOCK_ATTRIBUTE));
			attributes.put(WeatherUpdater.WEATHER_LOCK_ATTRIBUTE, "true");
			applyWeather(zone, attributes, weatherEntity, weather, thundering);
		}
	}

	protected final void restoreWeather() {
		if (storedWeather.isEmpty()) {
			return;
		}
		for (final Map.Entry<String, Pair<String, Boolean>> entry : storedWeather.entrySet()) {
			final String zoneName = entry.getKey();
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				logger.warn(getEventName() + " zone not found for weather restore: " + zoneName + ".");
				continue;
			}
			final ZoneAttributes attributes = zone.getAttributes();
			final WeatherEntity weatherEntity = zone.getWeatherEntity();
			if (attributes == null || weatherEntity == null) {
				logger.warn(getEventName() + " zone lacks weather support: " + zoneName + ".");
				continue;
			}
			final String storedLock = storedWeatherLock.get(zoneName);
			if (storedLock == null) {
				attributes.remove(WeatherUpdater.WEATHER_LOCK_ATTRIBUTE);
			} else {
				attributes.put(WeatherUpdater.WEATHER_LOCK_ATTRIBUTE, storedLock);
			}
			applyWeather(zone, attributes, weatherEntity, entry.getValue().first(), entry.getValue().second());
		}
		storedWeather.clear();
		storedWeatherLock.clear();
	}

	protected final void registerEventCreature(final Creature creature) {
		if (creature == null) {
			return;
		}
		if (!isEventActive()) {
			return;
		}
		final long currentRunId = eventRunId.get();
		creature.registerObjectsForNotification(eventCreatureObserver);
		eventCreatures.add(creature);
		eventCreatureRunIds.put(creature, Long.valueOf(currentRunId));
		eventTotalSpawnedCreatures.incrementAndGet();
	}

	protected final int getEventTotalSpawnedCreatures() {
		return Math.max(0, eventTotalSpawnedCreatures.get());
	}

	protected final int getEventDefeatedCreatures() {
		return Math.max(0, eventDefeatedCreatures.get());
	}

	protected final int getEventDefeatPercent() {
		final int total = getEventTotalSpawnedCreatures();
		if (total <= 0) {
			return 0;
		}
		final int defeated = Math.min(total, getEventDefeatedCreatures());
		return Math.max(0, Math.min(100, (int) Math.round((defeated * 100.0d) / total)));
	}

	protected void onEventCreatureDeath(final CircumstancesOfDeath circs) {
		// default no-op
	}

	protected final void removeEventCreatures() {
		synchronized (eventCreatures) {
			for (Creature creature : eventCreatures) {
				if (creature == null || creature.getZone() == null) {
					continue;
				}
				creature.stopAttack();
				creature.clearDropItemList();
				creature.getZone().remove(creature);
			}
			eventCreatures.clear();
		}
		eventCreatureRunIds.clear();
	}

	protected final List<Creature> getEventCreaturesSnapshot() {
		synchronized (eventCreatures) {
			return new ArrayList<>(eventCreatures);
		}
	}

	protected final void stopAnnouncements() {
		final TurnListener announcerToStop = activeAnnouncer;
		if (announcerToStop != null) {
			SingletonRepository.getTurnNotifier().dontNotify(announcerToStop);
		}
	}

	private void startEventInternal() {
		final long runId = eventRunId.incrementAndGet();
		eventTotalSpawnedCreatures.set(0);
		eventDefeatedCreatures.set(0);
		eventCreatureRunIds.clear();
		currentWave.set(0);
		totalWaves.set(Math.max(0, getWaves().size()));
		final long startedEpochSeconds = Instant.now().getEpochSecond();
		scheduledEndEpochSeconds = startedEpochSeconds + getEventDuration().getSeconds();
		lockWeatherFromConfig();
		onStart();
		broadcastStatusTick(runId);
		scheduleAnnouncement(runId);
		scheduleWaves(runId);
		int seconds = (int) getEventDuration().getSeconds();
		SingletonRepository.getTurnNotifier().notifyInSeconds(seconds, currentTurn -> endEvent(runId));
	}

	private void scheduleWaves(final long runId) {
		int delaySeconds = 0;
		for (EventWave wave : getWaves()) {
			delaySeconds += wave.intervalSeconds;
			final int scheduledDelay = delaySeconds;
			SingletonRepository.getTurnNotifier().notifyInSeconds(
					scheduledDelay,
					currentTurn -> handleWave(wave, runId)
			);
		}
	}

	private void handleWave(final EventWave wave, final long runId) {
		if (!isCurrentRunActive(runId)) {
			return;
		}
		currentWave.incrementAndGet();
		for (EventSpawn spawn : wave.spawns) {
			spawnCreaturesForWave(spawn);
		}
	}

	private void scheduleAnnouncement(final long runId) {
		if (!isCurrentRunActive(runId)) {
			return;
		}
		if (getAnnouncements().isEmpty()) {
			return;
		}
		final int intervalSeconds = getAnnouncementIntervalSeconds();
		if (intervalSeconds <= 0) {
			return;
		}
		final TurnListener announcer = new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				sendAnnouncement(runId);
				scheduleAnnouncement(runId);
			}
		};
		activeAnnouncer = announcer;
		SingletonRepository.getTurnNotifier().notifyInSeconds(
				intervalSeconds,
				announcer
		);
	}

	private void scheduleStatusBroadcast(final long runId) {
		if (!isCurrentRunActive(runId)) {
			return;
		}
		SingletonRepository.getTurnNotifier().notifyInSeconds(1, currentTurn -> broadcastStatusTick(runId));
	}

	private void broadcastStatusTick(final long runId) {
		if (!isCurrentRunActive(runId)) {
			return;
		}
		onStatusTick();
		MapEventStatusPublisher.broadcastActiveEventStatus(this);
		scheduleStatusBroadcast(runId);
	}

	private void lockWeatherFromConfig() {
		final MapEventConfig.WeatherLockConfig weatherLock = config.getWeatherLock();
		if (weatherLock == null) {
			return;
		}
		lockWeather(weatherLock.getWeather(), weatherLock.isThundering());
	}

	private void restoreWeatherFromConfig() {
		if (config.getWeatherLock() == null) {
			return;
		}
		restoreWeather();
	}

	private void sendAnnouncement(final long runId) {
		if (!isCurrentRunActive(runId)) {
			return;
		}
		final List<String> announcements = getAnnouncements();
		if (announcements.isEmpty()) {
			return;
		}
		final String message = announcements.get(Rand.rand(announcements.size()));
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG, message);
	}

	private boolean isCurrentRunActive(final long runId) {
		return eventActive.get() && eventRunId.get() == runId;
	}

	protected final long getCurrentEventRunId() {
		return eventRunId.get();
	}

	protected final boolean isRunActive(final long runId) {
		return isCurrentRunActive(runId);
	}

	private void attemptGuaranteedStart() {
		if (eventActive.get()) {
			logger.info(getEventName() + " guaranteed event skipped; event already active.");
			scheduleNextGuaranteedCheck();
			return;
		}
		LocalDate today = LocalDate.now();
		if (lastGuaranteedStartDate != null) {
			long daysSince = ChronoUnit.DAYS.between(lastGuaranteedStartDate, today);
			if (daysSince < guaranteedIntervalDays) {
				logger.info(getEventName() + " guaranteed event deferred; last start was " + lastGuaranteedStartDate + ".");
				scheduleNextGuaranteedCheck();
				return;
			}
		}
		logger.info(getEventName() + " guaranteed event starting at " + LocalDateTime.now() + ".");
		if (!startGuaranteedEvent()) {
			logger.warn(getEventName() + " guaranteed event already active; skipping guaranteed start.");
			scheduleNextGuaranteedCheck();
		}
	}

	private static LocalDateTime nextOccurrence(final LocalTime time, final LocalDateTime reference) {
		LocalDateTime occurrence = reference.toLocalDate().atTime(time);
		if (occurrence.isBefore(reference)) {
			occurrence = occurrence.plusDays(1);
		}
		return occurrence;
	}

	private static void applyWeather(final StendhalRPZone zone, final ZoneAttributes attributes,
			final WeatherEntity weatherEntity, final String weather, final boolean thundering) {
		weatherEntity.setThunder(thundering);
		final String oldWeather = attributes.get("weather");
		if (!Objects.equals(weather, oldWeather)) {
			if (weather != null) {
				attributes.put("weather", weather);
			} else {
				attributes.remove("weather");
			}
			zone.notifyOnlinePlayers();
		}
	}

	public static class EventSpawn {
		private final String creatureName;
		private final int count;

		public EventSpawn(final String creatureName, final int count) {
			this.creatureName = creatureName;
			this.count = count;
		}

		public String getCreatureName() {
			return creatureName;
		}

		public int getCount() {
			return count;
		}
	}

	public static class EventWave {
		private final int intervalSeconds;
		private final List<EventSpawn> spawns;

		public EventWave(final int intervalSeconds, final List<EventSpawn> spawns) {
			this.intervalSeconds = intervalSeconds;
			this.spawns = spawns;
		}

		public int getIntervalSeconds() {
			return intervalSeconds;
		}

		public List<EventSpawn> getSpawns() {
			return spawns;
		}
	}

	private class EventCreatureObserver implements Observer {
		@Override
		public void update(Observable obj, Object arg) {
			if (!(arg instanceof CircumstancesOfDeath)) {
				return;
			}
			final CircumstancesOfDeath circs = (CircumstancesOfDeath) arg;
			final RPEntity victim = circs.getVictim();
			if (!(victim instanceof Creature)) {
				return;
			}
			final Creature victimCreature = (Creature) victim;
			eventCreatures.remove(victimCreature);
			final Long creatureRunId = eventCreatureRunIds.remove(victimCreature);
			if (creatureRunId != null && creatureRunId.longValue() == eventRunId.get()) {
				eventDefeatedCreatures.incrementAndGet();
			}
			onEventCreatureDeath(circs);
		}
	}
}
