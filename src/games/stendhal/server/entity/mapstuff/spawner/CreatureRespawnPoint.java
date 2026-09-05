/***************************************************************************
 *                      (C) Copyright 2003 - Marauroa                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.MathHelper;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.rule.creature.EliteCreatureService;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.util.Observer;

/**
 * RespawnPoints are points at which creatures can appear. Several creatures can
 * be spawned, until a maximum has been reached (note that this maximum is
 * usually 1); then the RespawnPoint will stop spawning creatures until at least
 * one of the creatures has died. It will then continue to spawn creatures. A
 * certain time must pass between respawning creatures; this respawn time is
 * usually dependent of the type of the creatures that are spawned.
 *
 * Each respawn point can only spawn one type of creature. The Prototype design
 * pattern is used; the <i>prototypeCreature</i> will be copied to create new
 * creatures.
 */
public class CreatureRespawnPoint implements TurnListener {
	private static final int MAX_RESPAWN_TIME = 200 * 60 * 24 * 30 * 6;
	private static final int MIN_RESPAWN_TIME = 33;
	// Keep the diagnostic focused on meaningful stalls rather than one-off
	// placement/JIT spikes well below a single 300 ms game turn.
	private static final long SLOW_RESPAWN_NANOS = 50L * 1000L * 1000L;
	private static final Logger logger = Logger.getLogger(CreatureRespawnPoint.class);

	protected final StendhalRPZone zone;
	private LinkedList<Observer> observers = new LinkedList<Observer>();
	protected final int x;
	protected final int y;
	private final int maximum;
	protected Creature prototypeCreature;
	protected final List<Creature> creatures;
	protected boolean respawning;
	private int respawnTime;

	public CreatureRespawnPoint(final StendhalRPZone zone, final int x, final int y,
			final Creature creature, final int maximum) {
		this.zone = zone;
		this.x = x;
		this.y = y;
		this.prototypeCreature = creature;
		this.maximum = maximum;
		this.respawnTime = creature.getRespawnTime();
		this.creatures = new LinkedList<Creature>();
		respawning = true;
		SingletonRepository.getTurnNotifier().notifyInTurns(calculateNextRespawnTurn(), this);
	}

	public CreatureRespawnPoint(StendhalRPZone zone, int x,
			int y, Creature creature, int maximum, final Observer observer) {
		this(zone, x, y, creature, maximum);
		this.observers.add(observer);
	}

	public Creature getPrototypeCreature() {
		return prototypeCreature;
	}

	public void setRespawnTime(final int respawnTime) {
		this.respawnTime = respawnTime;
	}

	public void notifyDead(final Creature dead) {
		if (!respawning) {
			respawning = true;
			SingletonRepository.getTurnNotifier().notifyInTurns(
					calculateNextRespawnTurn(), this);
		}
		creatures.remove(dead);
	}

	@Override
	public void onTurnReached(final int currentTurn) {
		respawn();
		if (creatures.size() == maximum) {
			respawning = false;
		} else {
			SingletonRepository.getTurnNotifier().notifyInTurns(
					calculateNextRespawnTurn(), this);
		}
	}

	protected int calculateNextRespawnTurn() {
		return MathHelper.clamp(Rand.randExponential(respawnTime), MIN_RESPAWN_TIME, MAX_RESPAWN_TIME);
	}

	int rollRespawnCombatStat(final int baseValue) {
		return Rand.randGaussian(baseValue, baseValue / 10);
	}

	static int clampRespawnCombatStat(final int value) {
		return MathHelper.clamp(value, 0, Short.MAX_VALUE);
	}

	public int size() {
		return creatures.size();
	}

	public int getX() {
		return this.x;
	}

	public int getY() {
		return this.y;
	}

	public void setPrototypeCreature(final Creature creature) {
		this.prototypeCreature = creature;
	}

	public void addObserver(final Observer observer) {
		observers.add(observer);
	}

	public void removeObserver(final Observer observer) {
		observers.remove(observer);
	}

	public StendhalRPZone getZone() {
		return this.zone;
	}

	protected void respawn() {
		final long respawnStartNanos = System.nanoTime();
		long cloneNanos = 0L;
		long variantNanos = 0L;
		long observerNanos = 0L;
		long placementNanos = 0L;
		long initNanos = 0L;
		boolean placed = false;

		try {
			long stepStartNanos = System.nanoTime();
			final Creature newentity = prototypeCreature.getNewInstance();
			cloneNanos = System.nanoTime() - stepStartNanos;

			stepStartNanos = System.nanoTime();
			newentity.setAtk(clampRespawnCombatStat(
					rollRespawnCombatStat(newentity.getAtk())));
			newentity.setDef(clampRespawnCombatStat(
					rollRespawnCombatStat(newentity.getDef())));
			EliteCreatureService.maybePromote(newentity);
			variantNanos = System.nanoTime() - stepStartNanos;

			stepStartNanos = System.nanoTime();
			newentity.registerObjectsForNotification(observers);
			observerNanos = System.nanoTime() - stepStartNanos;

			stepStartNanos = System.nanoTime();
			placed = CreatureRespawnPlacement.place(zone, newentity, x, y);
			placementNanos = System.nanoTime() - stepStartNanos;
			if (placed) {
				stepStartNanos = System.nanoTime();
				newentity.init();
				newentity.setRespawnPoint(this);
				creatures.add(newentity);
				initNanos = System.nanoTime() - stepStartNanos;
			} else {
				notifyDead(newentity);
				logger.warn("Could not respawn " + newentity.getName() + " near "
						+ zone.getName() + " " + x + " " + y);
			}
		} catch (final Exception e) {
			logger.error("error respawning entity " + prototypeCreature, e);
		} finally {
			final long elapsedNanos = System.nanoTime() - respawnStartNanos;
			if (elapsedNanos >= SLOW_RESPAWN_NANOS) {
				logger.warn("Slow creature respawn [creature="
						+ prototypeCreature.getName() + ", zone=" + zone.getName()
						+ ", x=" + x + ", y=" + y + ", placed=" + placed
						+ ", elapsedMs=" + elapsedNanos / 1000000L
						+ ", cloneUs=" + cloneNanos / 1000L
						+ ", variantUs=" + variantNanos / 1000L
						+ ", observersUs=" + observerNanos / 1000L
						+ ", placementUs=" + placementNanos / 1000L
						+ ", initUs=" + initNanos / 1000L + "]");
			}
		}
	}

	public void spawnNow() {
		if (creatures.size() < maximum) {
			SingletonRepository.getTurnNotifier().dontNotify(this);
			onTurnReached(0);
		}
	}
}
