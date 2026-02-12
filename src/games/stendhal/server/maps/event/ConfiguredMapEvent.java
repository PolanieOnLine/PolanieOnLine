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

import java.time.LocalTime;
import java.util.Collections;
import java.util.Objects;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;

public class ConfiguredMapEvent extends BaseMapEvent {
	private final Logger logger;
	private final MapEventSpawnStrategy spawnStrategy;
	private final KillThresholdTrigger killThresholdTrigger;
	private volatile boolean scriptForceStartRequested;

	public ConfiguredMapEvent(final Logger logger, final MapEventConfig config) {
		this(logger, config, new RandomSafeSpotSpawnStrategy(logger));
	}

	public ConfiguredMapEvent(final Logger logger, final MapEventConfig config,
			final MapEventSpawnStrategy spawnStrategy) {
		super(logger, config);
		this.logger = Objects.requireNonNull(logger, "logger");
		this.spawnStrategy = Objects.requireNonNull(spawnStrategy, "spawnStrategy");
		killThresholdTrigger = new KillThresholdTrigger(
				getObserverZones(),
				circs -> !isEventActive() && getCreatureFilter().contains(circs.getVictim().getName()),
				getConfig().getTriggerThreshold(),
				this::startEventFromKills);
	}

	public final void registerObserverZone(final StendhalRPZone zone) {
		killThresholdTrigger.registerZoneObserver(Objects.requireNonNull(zone, "zone"));
	}

	public final void scheduleGuaranteedStart(final LocalTime time, final int intervalDays) {
		scheduleEveryDaysAt(time, intervalDays);
	}

	public final boolean forceStartEvent() {
		return startFromScript(true);
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
		killThresholdTrigger.resetCounter("event started");
		logger.info(getEventName() + " event started.");
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				getStartAnnouncementMessage());
	}

	@Override
	protected void onStop() {
		killThresholdTrigger.resetCounter("event ended");
		logger.info(getEventName() + " event ended.");
		removeEventCreatures();
		stopAnnouncements();
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				getStopAnnouncementMessage());
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

			spawnStrategy.spawnCreatures(
					getEventName(),
					Collections.singletonList(zoneName),
					creatureName,
					finalSpawnCount,
					this::registerEventCreature);
		}
	}

	private void startEventFromKills() {
		if (!startEvent()) {
			logger.warn(getEventName() + " event already active; skipping duplicate start.");
		}
	}
}
