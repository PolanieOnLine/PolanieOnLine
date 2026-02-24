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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.util.Observable;
import games.stendhal.server.util.Observer;

/**
 * Generic observer-based trigger that fires once after a kill threshold is reached.
 */
public class KillThresholdTrigger {
	private static final Logger LOGGER = Logger.getLogger(KillThresholdTrigger.class);

	private final Set<String> monitoredZones;
	private final Predicate<CircumstancesOfDeath> victimFilter;
	private final int killThreshold;
	private final Runnable onThresholdReached;
	private final Set<CreatureRespawnPoint> observedRespawnPoints = Collections.synchronizedSet(new HashSet<>());
	private final AtomicInteger killCount = new AtomicInteger(0);
	private final AtomicBoolean fired = new AtomicBoolean(false);
	private final DeathObserver deathObserver = new DeathObserver();

	public KillThresholdTrigger(
			final Collection<String> monitoredZones,
			final Predicate<CircumstancesOfDeath> victimFilter,
			final int killThreshold,
			final Runnable onThresholdReached) {
		this.monitoredZones = Collections.unmodifiableSet(new HashSet<>(Objects.requireNonNull(monitoredZones, "monitoredZones")));
		this.victimFilter = Objects.requireNonNull(victimFilter, "victimFilter");
		if (killThreshold <= 0) {
			throw new IllegalArgumentException("killThreshold must be greater than zero");
		}
		this.killThreshold = killThreshold;
		this.onThresholdReached = Objects.requireNonNull(onThresholdReached, "onThresholdReached");
	}

	public KillThresholdTrigger(
			final Collection<String> monitoredZones,
			final Collection<String> victimNames,
			final int killThreshold,
			final Runnable onThresholdReached) {
		this(monitoredZones, createVictimNameFilter(victimNames), killThreshold, onThresholdReached);
	}

	public void registerZoneObserver(final StendhalRPZone zone) {
		Objects.requireNonNull(zone, "zone");
		if (!monitoredZones.contains(zone.getName())) {
			return;
		}

		for (CreatureRespawnPoint respawnPoint : zone.getRespawnPointList()) {
			if (respawnPoint == null || !observedRespawnPoints.add(respawnPoint)) {
				continue;
			}
			respawnPoint.addObserver(deathObserver);
		}
	}

	public void resetCounter(final String reason) {
		int previous = killCount.getAndSet(0);
		fired.set(false);
		LOGGER.debug("Kill threshold trigger reset (" + reason + ") from " + previous + ".");
	}

	private void recordKill(final CircumstancesOfDeath circumstances) {
		if (!monitoredZones.contains(circumstances.getZone().getName())) {
			return;
		}
		if (!victimFilter.test(circumstances)) {
			return;
		}

		int current = killCount.incrementAndGet();
		if (current >= killThreshold && fired.compareAndSet(false, true)) {
			LOGGER.info("Kill threshold reached: " + current + ".");
			onThresholdReached.run();
		}
	}

	private static Predicate<CircumstancesOfDeath> createVictimNameFilter(final Collection<String> victimNames) {
		final Set<String> names = new HashSet<>(Objects.requireNonNull(victimNames, "victimNames"));
		return circumstances -> names.contains(circumstances.getVictim().getName());
	}

	private class DeathObserver implements Observer {
		@Override
		public void update(final Observable obj, final Object arg) {
			if (arg instanceof CircumstancesOfDeath) {
				recordKill((CircumstancesOfDeath) arg);
			}
		}
	}
}
