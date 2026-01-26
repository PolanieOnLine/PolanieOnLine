/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************
 **************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPWorld;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;

/**
 * Handles Dragon Mist event logic based on dragon kills in Dragon Land.
 */
public class DragonMistEventManager implements TurnListener {
	private static final Logger logger = Logger.getLogger(DragonMistEventManager.class);

	private static final int CHECK_INTERVAL_SECONDS = 60;
	private static final int KILL_THRESHOLD = 25;
	private static final int ROLL_CHANCE_PERCENT = 25;
	private static final int COOLDOWN_SECONDS = 1800;
	private static final int ELITE_SPAWN_COUNT = 3;
	private static final int BOSS_SPAWN_COUNT = 1;

	private static final List<String> DRAGON_LAND_ZONES = Collections.unmodifiableList(Arrays.asList(
			"0_dragon_land_n",
			"0_dragon_land_s",
			"0_dragon_land_nw",
			"0_dragon_land_ne",
			"0_dragon_land_sw",
			"0_dragon_land_se"));

	private static final Set<String> DRAGON_SUBCLASSES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
			"green_dragon",
			"bone_dragon",
			"blue_dragon",
			"red_dragon",
			"black_dragon",
			"flying_golden_dragon",
			"twin_head_dragon",
			"rysin_dragon")));

	private static final List<String> ELITE_CREATURES = Collections.unmodifiableList(Arrays.asList(
			"zielony smok",
			"błękitny smok",
			"czerwony smok"));

	private static final List<String> BOSS_CREATURES = Collections.unmodifiableList(Arrays.asList(
			"czarny smok",
			"złoty smok",
			"dwugłowy niebieski smok"));

	private static final String GLOBAL_MESSAGE = "Smocza mgła spowiła Smoczą Krainę! Pojawiają się elity i bossy.";

	private static DragonMistEventManager instance;

	private final Object lock = new Object();
	private final int cooldownTurns;
	private int dragonDeathCount;
	private int lastEventTurn = Integer.MIN_VALUE;

	public static DragonMistEventManager create() {
		DragonMistEventManager manager = new DragonMistEventManager();
		TurnNotifier.get().notifyInSeconds(CHECK_INTERVAL_SECONDS, manager);
		return manager;
	}

	public static DragonMistEventManager get() {
		if (instance == null) {
			instance = DragonMistEventManager.create();
		}
		return instance;
	}

	private DragonMistEventManager() {
		cooldownTurns = StendhalRPWorld.get().getTurnsInSeconds(COOLDOWN_SECONDS);
	}

	public void recordDragonDeath(final RPEntity entity) {
		if (!(entity instanceof Creature)) {
			return;
		}

		final StendhalRPZone zone = entity.getZone();
		if (zone == null || !DRAGON_LAND_ZONES.contains(zone.getName())) {
			return;
		}

		if (!isDragonCreature(entity)) {
			return;
		}

		synchronized (lock) {
			dragonDeathCount++;
		}
	}

	public void forceEvent() {
		synchronized (lock) {
			lastEventTurn = TurnNotifier.get().getCurrentTurnForDebugging();
			dragonDeathCount = 0;
		}
		triggerEvent();
	}

	@Override
	public void onTurnReached(final int currentTurn) {
		boolean shouldTrigger = false;

		synchronized (lock) {
			if (dragonDeathCount >= KILL_THRESHOLD && currentTurn - lastEventTurn >= cooldownTurns) {
				final int roll = Rand.roll1D100();
				if (roll <= ROLL_CHANCE_PERCENT) {
					shouldTrigger = true;
					lastEventTurn = currentTurn;
					dragonDeathCount = 0;
				}
			}
		}

		if (shouldTrigger) {
			triggerEvent();
		}

		TurnNotifier.get().notifyInSeconds(CHECK_INTERVAL_SECONDS, this);
	}

	private boolean isDragonCreature(final RPEntity entity) {
		if (!entity.has("class") || !entity.has("subclass")) {
			return false;
		}

		final String clazz = String.valueOf(entity.get("class"));
		final String subclass = String.valueOf(entity.get("subclass"));
		return "dragon".equals(clazz) && DRAGON_SUBCLASSES.contains(subclass);
	}

	private void triggerEvent() {
		broadcastGlobalMessage(GLOBAL_MESSAGE);
		spawnCreatures(ELITE_CREATURES, ELITE_SPAWN_COUNT);
		spawnCreatures(BOSS_CREATURES, BOSS_SPAWN_COUNT);
	}

	private void broadcastGlobalMessage(final String message) {
		for (final IRPZone zone : SingletonRepository.getRPWorld()) {
			if (zone instanceof StendhalRPZone) {
				for (final Player player : ((StendhalRPZone) zone).getPlayers()) {
					player.sendPrivateText(NotificationType.TELLALL, message);
				}
			}
		}
	}

	private void spawnCreatures(final List<String> creatureNames, final int count) {
		final EntityManager entityManager = SingletonRepository.getEntityManager();
		final List<String> zones = new ArrayList<String>(DRAGON_LAND_ZONES);

		for (int i = 0; i < count; i++) {
			final String creatureName = Rand.rand(creatureNames);
			final String zoneName = Rand.rand(zones);
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				logger.warn("Dragon Mist spawn skipped, zone not found: " + zoneName);
				continue;
			}

			final Creature creature = entityManager.getCreature(creatureName);
			if (creature == null) {
				logger.warn("Dragon Mist spawn skipped, creature not found: " + creatureName);
				continue;
			}

			final Point position = zone.getRandomSpawnPosition(creature);
			if (position == null) {
				logger.warn("Dragon Mist spawn skipped, no free position in zone: " + zoneName);
				continue;
			}

			creature.setPosition(position.x, position.y);
			zone.add(creature);
		}
	}
}
