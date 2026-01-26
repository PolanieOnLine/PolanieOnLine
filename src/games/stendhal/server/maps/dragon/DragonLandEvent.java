/***************************************************************************
 *                    Copyright © 2024 - PolanieOnLine                    *
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

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.util.Observable;
import games.stendhal.server.util.Observer;

public class DragonLandEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);

	private static final Duration EVENT_DURATION = Duration.ofMinutes(45);
	private static final AtomicBoolean SCHEDULED = new AtomicBoolean(false);
	private static final AtomicBoolean EVENT_ACTIVE = new AtomicBoolean(false);
	private static final AtomicInteger DRAGON_KILL_COUNT = new AtomicInteger(0);
	private static final int DRAGON_KILL_THRESHOLD = 25;
	private static final List<String> DRAGON_TYPES = Arrays.asList(
			"dwugłowy czarny smok",
			"dwugłowy lodowy smok",
			"dwugłowy czerwony smok",
			"dwugłowy złoty smok",
			"dwugłowy zielony smok",
			"lodowy smok",
			"pustynny smok",
			"zgniły szkielet smoka",
			"smok arktyczny",
			"zielone smoczysko",
			"niebieskie smoczysko",
			"czerwone smoczysko",
			"czarne smoczysko",
			"latający czarny smok",
			"latający złoty smok",
			"Smok Wawelski"
	);
	private static final Set<String> DRAGON_ZONES = Set.of("0_dragon_land_n", "0_dragon_land_s");
	private static final DragonDeathObserver DRAGON_DEATH_OBSERVER = new DragonDeathObserver();

	private static volatile LocalTime scheduledTime;

	public static void registerZoneObserver(final StendhalRPZone zone) {
		Objects.requireNonNull(zone, "zone");
		if (!DRAGON_ZONES.contains(zone.getName())) {
			return;
		}
		for (CreatureRespawnPoint respawnPoint : zone.getRespawnPointList()) {
			if (respawnPoint != null
					&& DRAGON_TYPES.contains(respawnPoint.getPrototypeCreature().getName())) {
				respawnPoint.addObserver(DRAGON_DEATH_OBSERVER);
			}
		}
	}

	public static void scheduleDailyAt(LocalTime time) {
		Objects.requireNonNull(time, "time");
		if (!SCHEDULED.compareAndSet(false, true)) {
			LOGGER.debug("Dragon Land event already scheduled; skipping duplicate schedule.");
			return;
		}
		scheduledTime = time;
		scheduleNextRun(time);
	}

	private static void scheduleNextRun(LocalTime time) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startTime = LocalDate.now().atTime(time);
		if (startTime.isBefore(now)) {
			startTime = startTime.plusDays(1);
		}
		int waitSec = (int) Duration.between(now, startTime).getSeconds();
		LOGGER.info("Dragon Land event scheduled in " + waitSec + " seconds at " + startTime + ".");
		SingletonRepository.getTurnNotifier().notifyInSeconds(waitSec, currentTurn -> startEvent());
	}

	private static void startEvent() {
		if (!EVENT_ACTIVE.compareAndSet(false, true)) {
			LOGGER.warn("Dragon Land event already active; skipping duplicate start.");
			return;
		}
		resetKillCounter("event started");
		LOGGER.info("Dragon Land event started.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie."
		);
		int seconds = (int) EVENT_DURATION.getSeconds();
		SingletonRepository.getTurnNotifier().notifyInSeconds(seconds, currentTurn -> endEvent());
	}

	private static void endEvent() {
		if (!EVENT_ACTIVE.compareAndSet(true, false)) {
			return;
		}
		resetKillCounter("event ended");
		LOGGER.info("Dragon Land event ended.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Smocza kraina uspokaja się. Wydarzenie dobiegło końca."
		);
		scheduleNextRun(scheduledTime);
	}

	private static void resetKillCounter(final String reason) {
		int previous = DRAGON_KILL_COUNT.getAndSet(0);
		LOGGER.debug("Dragon Land kill counter reset (" + reason + ") from " + previous + ".");
	}

	private static void recordDragonDeath(final CircumstancesOfDeath circs) {
		if (EVENT_ACTIVE.get()) {
			return;
		}
		if (!DRAGON_ZONES.contains(circs.getZone().getName())) {
			return;
		}
		final String victimName = circs.getVictim().getName();
		if (!DRAGON_TYPES.contains(victimName)) {
			return;
		}
		int current = DRAGON_KILL_COUNT.incrementAndGet();
		if (current >= DRAGON_KILL_THRESHOLD) {
			LOGGER.info("Dragon Land kill threshold reached: " + current + ".");
			startEvent();
		}
	}

	private static class DragonDeathObserver implements Observer {
		@Override
		public void update(Observable obj, Object arg) {
			if (arg instanceof CircumstancesOfDeath) {
				recordDragonDeath((CircumstancesOfDeath) arg);
			}
		}
	}
}
