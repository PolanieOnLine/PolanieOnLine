/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

	Creature calculateCreature(final int targetLevel) {
		if (sortedCreatures.isEmpty()) {
			return null;
		}
		final List<Creature> candidates = new ArrayList<Creature>();
		int bestLevel = Integer.MIN_VALUE;
		for (final Creature creature : sortedCreatures) {
			final int level = creature.getLevel();
			if (level > targetLevel) {
				break;
			}
			if (level > bestLevel) {
				candidates.clear();
				bestLevel = level;
			}
			if (level == bestLevel) {
				candidates.add(creature);
			}
		}
		if (candidates.isEmpty()) {
			return sortedCreatures.get(0);
		}
		Collections.shuffle(candidates);
		return candidates.get(0);
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
		if (!StendhalRPAction.placeat(zone, creature, player.getX(), player.getY(),
				arenaInfo.getArena().getShape())) {
			logger.warn("Could not place Challenge Arena creature "
					+ creature.getName());
			return null;
		}
		spawnedCreatures.add(creature);
		return creature;
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
