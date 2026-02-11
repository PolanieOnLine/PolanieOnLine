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
import java.util.Objects;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;

public class ConfiguredMapEvent extends BaseMapEvent {
	private final Logger logger;
	private final MapEventSpawnStrategy spawnStrategy;
	private final KillThresholdTrigger killThresholdTrigger;

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
		if (!startEvent()) {
			logger.warn(getEventName() + " event already active; refusing forced start.");
			return false;
		}
		return true;
	}

	protected String getStartAnnouncementMessage() {
		return "Rozpoczyna się wydarzenie: " + getEventName() + ".";
	}

	protected String getStopAnnouncementMessage() {
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
		spawnStrategy.spawnCreatures(getEventName(), getZones(), creatureName, count, this::registerEventCreature);
	}

	private void startEventFromKills() {
		if (!startEvent()) {
			logger.warn(getEventName() + " event already active; skipping duplicate start.");
		}
	}
}
