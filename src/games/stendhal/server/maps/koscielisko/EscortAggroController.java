package games.stendhal.server.maps.koscielisko;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.SpeakerNPC;

final class EscortAggroController {
	private final Logger logger;
	private final String eventName;
	private final int giantTargetRange;
	private final int playerFallbackRange;
	private final int leashRange;
	private final int maxGiantPathLength;
	private final int giantChaseGraceTicks;
	private final Map<Integer, SpawnAnchor> creatureAnchors = new HashMap<>();
	private final Map<Integer, Integer> lostGiantSightTicks = new HashMap<>();
	private final Set<Integer> firstTargetLogged = new HashSet<>();

	EscortAggroController(final Logger logger,
			final String eventName,
			final int giantTargetRange,
			final int playerFallbackRange,
			final int leashRange,
			final int maxGiantPathLength,
			final int giantChaseGraceTicks) {
		this.logger = logger;
		this.eventName = eventName;
		this.giantTargetRange = giantTargetRange;
		this.playerFallbackRange = playerFallbackRange;
		this.leashRange = leashRange;
		this.maxGiantPathLength = maxGiantPathLength;
		this.giantChaseGraceTicks = giantChaseGraceTicks;
	}

	void reset() {
		creatureAnchors.clear();
		lostGiantSightTicks.clear();
		firstTargetLogged.clear();
	}

	void apply(final List<Creature> creatures, final SpeakerNPC giant, final boolean giantOnlyAggro) {
		for (final Creature creature : creatures) {
			if (creature == null || creature.getZone() == null) {
				continue;
			}
			final int creatureId = creature.getID().getObjectID();
			creatureAnchors.computeIfAbsent(creatureId,
					ignored -> new SpawnAnchor(creature.getZone().getName(), creature.getX(), creature.getY()));

			if (isOutsideEscortZone(creature, creatureAnchors.get(creatureId))
					|| isBeyondLeashRange(creature, creatureAnchors.get(creatureId))) {
				resetCreatureTarget(creature, "leash_or_zone", true);
				continue;
			}

			final GiantAggroState giantAggroState = evaluateGiantAggroState(creature, giant);
			if (giantAggroState == GiantAggroState.READY_TO_ATTACK) {
				lostGiantSightTicks.remove(creatureId);
				applyTargetIfNeeded(creature, giant, "giant_priority");
				continue;
			}
			if (giantAggroState == GiantAggroState.CRITICAL_GIANT_UNAVAILABLE) {
				resetCreatureTarget(creature, "giant_unavailable", true);
				continue;
			}
			if (giantAggroState == GiantAggroState.TEMPORARY_ATTACK_BLOCKED && giantOnlyAggro
					&& creature.getAttackTarget() == giant) {
				final int lostTicks = lostGiantSightTicks.getOrDefault(creatureId, 0) + 1;
				lostGiantSightTicks.put(creatureId, lostTicks);
				if (lostTicks <= giantChaseGraceTicks) {
					continue;
				}
				resetCreatureTarget(creature, "giant_chase_grace_expired", false);
				continue;
			}

			lostGiantSightTicks.remove(creatureId);
			if (creature.getAttackTarget() == giant) {
				creature.stopAttack();
			}
			if (giantOnlyAggro) {
				resetCreatureTarget(creature, "giant_only_aggro_no_valid_target", false);
				continue;
			}
			final RPEntity fallbackTarget = creature.getNearestEnemy(playerFallbackRange);
			if (fallbackTarget != null) {
				applyTargetIfNeeded(creature, fallbackTarget, "player_fallback");
			}
		}
	}

	private GiantAggroState evaluateGiantAggroState(final Creature creature, final SpeakerNPC giant) {
		if (giant == null || giant.getZone() == null || giant.getHP() <= 0) {
			return GiantAggroState.CRITICAL_GIANT_UNAVAILABLE;
		}
		if (creature.getZone() != giant.getZone()) {
			return GiantAggroState.NOT_LOGICALLY_VALID;
		}
		if (creature.squaredDistance(giant) > giantTargetRange * giantTargetRange) {
			return GiantAggroState.NOT_LOGICALLY_VALID;
		}
		if (creature.getX() == giant.getX() && creature.getY() == giant.getY()) {
			return GiantAggroState.READY_TO_ATTACK;
		}
		if (!creature.hasLineOfSight(giant)) {
			return GiantAggroState.TEMPORARY_ATTACK_BLOCKED;
		}
		return Path.searchPath(creature.getZone(), creature.getX(), creature.getY(), giant.getX(), giant.getY(), maxGiantPathLength) != null
				? GiantAggroState.READY_TO_ATTACK : GiantAggroState.TEMPORARY_ATTACK_BLOCKED;
	}

	private void applyTargetIfNeeded(final Creature creature, final RPEntity target, final String source) {
		if (target == null) {
			return;
		}
		if (creature.getAttackTarget() != target) {
			creature.setTarget(target);
		}
		lostGiantSightTicks.remove(creature.getID().getObjectID());
		if (firstTargetLogged.add(creature.getID().getObjectID()) && logger.isDebugEnabled()) {
			logger.debug(eventName + " first target for event creature '" + creature.getName() + "' => " + target.getName()
					+ " via " + source + ".");
		}
	}

	private void resetCreatureTarget(final Creature creature, final String reason, final boolean forced) {
		lostGiantSightTicks.remove(creature.getID().getObjectID());
		if (logger.isDebugEnabled()) {
			logger.debug(eventName + " resetting target for event creature '" + creature.getName() + "', reason="
					+ reason + ", forced=" + forced + ".");
		}
		creature.stopAttack();
		creature.clearPath();
		creature.stop();
	}

	private static boolean isOutsideEscortZone(final Creature creature, final SpawnAnchor anchor) {
		return anchor != null && !anchor.zoneName.equals(creature.getZone().getName());
	}

	private boolean isBeyondLeashRange(final Creature creature, final SpawnAnchor anchor) {
		if (anchor == null) {
			return false;
		}
		final int dx = creature.getX() - anchor.x;
		final int dy = creature.getY() - anchor.y;
		return dx * dx + dy * dy > leashRange * leashRange;
	}

	private enum GiantAggroState {
		READY_TO_ATTACK,
		TEMPORARY_ATTACK_BLOCKED,
		NOT_LOGICALLY_VALID,
		CRITICAL_GIANT_UNAVAILABLE
	}

	static final class SpawnAnchor {
		private final String zoneName;
		private final int x;
		private final int y;

		SpawnAnchor(final String zoneName, final int x, final int y) {
			this.zoneName = zoneName;
			this.x = x;
			this.y = y;
		}
	}
}
