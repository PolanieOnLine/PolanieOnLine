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
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;

public class DragonLandEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);

	private static final Duration EVENT_DURATION = Duration.ofMinutes(45);
	private static final AtomicBoolean SCHEDULED = new AtomicBoolean(false);
	private static final AtomicBoolean EVENT_ACTIVE = new AtomicBoolean(false);

	private static volatile LocalTime scheduledTime;

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
		LOGGER.info("Dragon Land event ended.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Smocza kraina uspokaja się. Wydarzenie dobiegło końca."
		);
		scheduleNextRun(scheduledTime);
	}
}
