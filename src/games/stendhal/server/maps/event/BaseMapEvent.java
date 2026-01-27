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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.ZoneAttributes;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.rp.WeatherUpdater;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.mapstuff.WeatherEntity;
import games.stendhal.server.util.Observable;
import games.stendhal.server.util.Observer;
import marauroa.common.Pair;

public abstract class BaseMapEvent {
	private final Logger logger;
	private final AtomicBoolean scheduled = new AtomicBoolean(false);
	private final AtomicBoolean eventActive = new AtomicBoolean(false);
	private final Map<String, Pair<String, Boolean>> storedWeather = new HashMap<>();
	private final Map<String, String> storedWeatherLock = new HashMap<>();
	private final List<Creature> eventCreatures = Collections.synchronizedList(new ArrayList<>());
	private final Observer eventCreatureObserver = new EventCreatureObserver();
	private final TurnListener announcer = new TurnListener() {
		@Override
		public void onTurnReached(final int currentTurn) {
			sendAnnouncement();
			scheduleAnnouncement();
		}
	};
	private volatile LocalTime scheduledTime;
	private volatile LocalDate lastGuaranteedStartDate;
	private volatile int guaranteedIntervalDays;

	protected BaseMapEvent(final Logger logger) {
		this.logger = Objects.requireNonNull(logger, "logger");
	}

	protected abstract String getEventName();

	protected abstract Duration getEventDuration();

	protected abstract List<String> getZones();

	protected abstract List<EventWave> getWaves();

	protected abstract List<String> getAnnouncements();

	protected abstract void onStart();

	protected abstract void onStop();

	protected abstract void spawnCreatures(String creatureName, int count);

	protected void onWaveSpawn(final EventSpawn spawn) {
		// default no-op
	}

	protected int getAnnouncementIntervalSeconds() {
		return 600;
	}

	protected final boolean isEventActive() {
		return eventActive.get();
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
		if (!eventActive.compareAndSet(true, false)) {
			return;
		}
		onStop();
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
		creature.registerObjectsForNotification(eventCreatureObserver);
		eventCreatures.add(creature);
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
	}

	protected final void stopAnnouncements() {
		SingletonRepository.getTurnNotifier().dontNotify(announcer);
	}

	private void startEventInternal() {
		onStart();
		scheduleAnnouncement();
		scheduleWaves();
		int seconds = (int) getEventDuration().getSeconds();
		SingletonRepository.getTurnNotifier().notifyInSeconds(seconds, currentTurn -> endEvent());
	}

	private void scheduleWaves() {
		int delaySeconds = 0;
		for (EventWave wave : getWaves()) {
			delaySeconds += wave.intervalSeconds;
			final int scheduledDelay = delaySeconds;
			SingletonRepository.getTurnNotifier().notifyInSeconds(
					scheduledDelay,
					currentTurn -> handleWave(wave)
			);
		}
	}

	private void handleWave(final EventWave wave) {
		if (!eventActive.get()) {
			return;
		}
		for (EventSpawn spawn : wave.spawns) {
			onWaveSpawn(spawn);
			spawnCreatures(spawn.creatureName, spawn.count);
		}
	}

	private void scheduleAnnouncement() {
		if (!eventActive.get()) {
			return;
		}
		if (getAnnouncements().isEmpty()) {
			return;
		}
		SingletonRepository.getTurnNotifier().notifyInSeconds(
				getAnnouncementIntervalSeconds(),
				announcer
		);
	}

	private void sendAnnouncement() {
		if (!eventActive.get()) {
			return;
		}
		final List<String> announcements = getAnnouncements();
		if (announcements.isEmpty()) {
			return;
		}
		final String message = announcements.get(Rand.rand(announcements.size()));
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG, message);
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
	}

	private class EventCreatureObserver implements Observer {
		@Override
		public void update(Observable obj, Object arg) {
			if (!(arg instanceof CircumstancesOfDeath)) {
				return;
			}
			final CircumstancesOfDeath circs = (CircumstancesOfDeath) arg;
			eventCreatures.remove(circs.getVictim());
		}
	}
}
