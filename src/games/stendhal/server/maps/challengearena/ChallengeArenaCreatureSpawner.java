/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.core.rule.creature.EliteCreatureService;
import games.stendhal.server.entity.creature.ArenaCreature;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.LevelBasedComparator;
import games.stendhal.server.entity.player.Player;

/** Spawns no-drop monsters for one Challenge Arena run. */
public final class ChallengeArenaCreatureSpawner {
	private static final Logger logger = Logger.getLogger(
			ChallengeArenaCreatureSpawner.class);
	private static final int MIN_SPAWN_DISTANCE = 4;
	private static final int MAX_SPAWN_DISTANCE = 14;
	private static final int[][] DIRECTIONS = {
		{1, 0}, {-1, 0}, {0, 1}, {0, -1}
	};

	private final List<Creature> sortedCreatures = new LinkedList<Creature>();
	private final List<Creature> spawnedCreatures = new ArrayList<Creature>();

	public ChallengeArenaCreatureSpawner() {
		final Collection<Creature> creatures = SingletonRepository.getEntityManager()
				.getCreatures();
		for (final Creature creature : creatures) {
			if (isUsableTemplate(creature)) {
				sortedCreatures.add(creature);
			}
		}
		Collections.sort(sortedCreatures, new LevelBasedComparator());
	}

	private boolean isUsableTemplate(final Creature creature) {
		if (creature == null || creature.isAbnormal()) {
			return false;
		}
		if (creature.has("immortal") || creature.has("unnamed")
				|| creature.has("no_hpbar")) {
			return false;
		}
		return !creature.getAIProfiles().containsKey("boss")
				&& !creature.getAIProfiles().containsKey("no_elite")
				&& (!creature.has("title_type")
						|| "enemy".equals(creature.get("title_type")));
	}

	/** Selects the configured creature level nearest to the requested level. */
	Creature calculateCreature(final int targetLevel) {
		if (sortedCreatures.isEmpty()) {
			return null;
		}
		final List<Creature> candidates = new ArrayList<Creature>();
		int bestDistance = Integer.MAX_VALUE;
		int bestLevel = Integer.MIN_VALUE;
		for (final Creature creature : sortedCreatures) {
			final int level = creature.getLevel();
			final int distance = Math.abs(level - targetLevel);
			if (distance < bestDistance
					|| (distance == bestDistance && level > bestLevel)) {
				candidates.clear();
				bestDistance = distance;
				bestLevel = level;
			}
			if (distance == bestDistance && level == bestLevel) {
				candidates.add(creature);
			}
		}
		Collections.shuffle(candidates);
		return candidates.isEmpty() ? sortedCreatures.get(0) : candidates.get(0);
	}

	Creature spawn(final Player player, final ChallengeArenaInfo arenaInfo,
			final int targetLevel, final boolean forceElite,
			final boolean finalChampion, final ChallengeArenaTier tier,
			final List<ChallengeArenaModifier> modifiers) {
		final Creature template = calculateCreature(targetLevel);
		if (template == null) {
			return null;
		}

		final ArenaCreature creature = new ArenaCreature(template.getNewInstance(),
				arenaInfo.getArena().getShape());
		creature.clearDropItemList();
		if (forceElite || finalChampion) {
			EliteCreatureService.promote(creature);
		}
		if (modifiers != null) {
			for (final ChallengeArenaModifier modifier : modifiers) {
				if (modifier != null) {
					modifier.apply(creature);
				}
			}
		}
		if (finalChampion) {
			ChallengeArenaChampionService.promote(creature, tier);
		}
		creature.setTarget(player);

		final StendhalRPZone zone = arenaInfo.getZone();
		final List<Point> spawnTiles = findReachableSpawnTiles(zone,
				arenaInfo.getArena().getShape(), player.getX(), player.getY());
		Collections.shuffle(spawnTiles);
		for (final Point point : spawnTiles) {
			if (zone.collides(creature, point.x, point.y)) {
				continue;
			}
			if (StendhalRPAction.placeat(zone, creature, point.x, point.y,
					arenaInfo.getArena().getShape())) {
				spawnedCreatures.add(creature);
				return creature;
			}
		}

		logger.warn("Could not place Challenge Arena creature "
				+ creature.getName() + " near " + player.getX() + "," + player.getY());
		return null;
	}

	/**
	 * Flood-fills walkable tiles from the player's current position. Static
	 * collision and the combat-area boundary stop expansion, so candidates can
	 * never be selected on the other side of a wall.
	 */
	static List<Point> findReachableSpawnTiles(final StendhalRPZone zone,
			final Shape allowedArea, final int originX, final int originY) {
		final List<Point> result = new ArrayList<Point>();
		if (zone == null || allowedArea == null
				|| zone.collisionMap.collides(originX, originY)) {
			return result;
		}

		final Deque<Point> queue = new ArrayDeque<Point>();
		final Set<Long> visited = new HashSet<Long>();
		queue.add(new Point(originX, originY));
		visited.add(pointKey(originX, originY));

		while (!queue.isEmpty()) {
			final Point point = queue.removeFirst();
			final int distance = Math.abs(point.x - originX)
					+ Math.abs(point.y - originY);
			if (distance >= MIN_SPAWN_DISTANCE) {
				result.add(point);
			}
			if (distance >= MAX_SPAWN_DISTANCE) {
				continue;
			}

			for (final int[] direction : DIRECTIONS) {
				final int x = point.x + direction[0];
				final int y = point.y + direction[1];
				final long key = pointKey(x, y);
				if (visited.contains(key)) {
					continue;
				}
				visited.add(key);
				if (!allowedArea.contains(x + 0.5, y + 0.5)
						|| zone.collisionMap.collides(x, y)) {
					continue;
				}
				queue.addLast(new Point(x, y));
			}
		}
		return result;
	}

	private static long pointKey(final int x, final int y) {
		return (((long) x) << 32) ^ (y & 0xffffffffL);
	}

	boolean areAllCreaturesDead() {
		for (final Creature creature : spawnedCreatures) {
			if (creature.getHP() > 0) {
				return false;
			}
		}
		return true;
	}

	void removeAllCreatures() {
		for (final Creature creature : spawnedCreatures) {
			final StendhalRPZone zone = creature.getZone();
			if (zone != null) {
				zone.remove(creature);
			}
		}
		spawnedCreatures.clear();
	}
}
