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
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;

public class DragonLandEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(DragonLandEvent.class);
	private static final MapEventConfig EVENT_CONFIG = MapEventConfigLoader
			.load(MapEventConfigLoader.DRAGON_LAND_DEFAULT);
	private static final DragonLandEvent INSTANCE = new DragonLandEvent();

	private final AtomicBoolean wawelAnnounced = new AtomicBoolean(false);

	private DragonLandEvent() {
		super(LOGGER, EVENT_CONFIG);
	}

	public static DragonLandEvent getInstance() {
		return INSTANCE;
	}

	public static void registerZoneObserver(final StendhalRPZone zone) {
		INSTANCE.registerObserverZone(zone);
	}

	public static void scheduleEveryTwoDaysAt(final LocalTime time) {
		INSTANCE.scheduleGuaranteedStart(time, EVENT_CONFIG.getGuaranteedIntervalDays());
	}

	public static void scheduleGuaranteedEveryDaysAt(final LocalTime time, final int intervalDays) {
		INSTANCE.scheduleGuaranteedStart(time, intervalDays);
	}

	public static boolean forceStart() {
		return INSTANCE.forceStartEvent();
	}

	@Override
	protected String getStartAnnouncementMessage() {
		return "Smocza kraina budzi się do życia! Rozpoczyna się wydarzenie.";
	}

	@Override
	protected String getStopAnnouncementMessage() {
		return "Smocza kraina uspokaja się. Wydarzenie dobiegło końca.";
	}

	@Override
	protected void onStart() {
		wawelAnnounced.set(false);
		super.onStart();
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		if (!"Smok Wawelski".equals(spawn.getCreatureName())) {
			return;
		}
		if (!wawelAnnounced.compareAndSet(false, true) || !isEventActive()) {
			return;
		}
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"W oddali słychać ryk… Smok Wawelski pojawił się w Smoczej Krainie!");
	}
}
