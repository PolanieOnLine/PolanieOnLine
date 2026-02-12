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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.SpeakerNPC;

public final class KoscieliskoGiantEscortEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(KoscieliskoGiantEscortEvent.class);
	private static final String GIANT_NPC_NAME = "Wielkolud";
	private static final int GIANT_EVENT_HP = 50000;
	private static final int GIANT_FAIL_HP_THRESHOLD = 100;
	private static final int HEALTH_CHECK_INTERVAL_SECONDS = 1;
	private static final int AGGRO_TICK_INTERVAL_SECONDS = 1;
	private static final int GIANT_TARGET_RANGE = 18;
	private static final int PLAYER_FALLBACK_RANGE = 14;
	private static final int LEASH_RANGE = 24;
	private static final int MAX_GIANT_PATH_LENGTH = 96;

	private final TurnListener giantHealthMonitor = new TurnListener() {
		@Override
		public void onTurnReached(final int currentTurn) {
			monitorGiantHealth();
		}
	};

	private final TurnListener escortAggroController = new TurnListener() {
		@Override
		public void onTurnReached(final int currentTurn) {
			applyEscortAggroLogic();
		}
	};

	private volatile SpeakerNPC giantNpc;
	private volatile GiantSnapshot snapshot;
	private volatile boolean failedByGiantHealth;
	private volatile boolean escortSuccess;
	private final Map<Integer, SpawnAnchor> creatureAnchors = new HashMap<>();
	private final Set<Integer> firstTargetLogged = new HashSet<>();

	public KoscieliskoGiantEscortEvent() {
		super(LOGGER, MapEventConfigLoader.load(MapEventConfigLoader.KOSCIELISKO_GIANT_ESCORT));
	}

	@Override
	protected void onStart() {
		failedByGiantHealth = false;
		escortSuccess = false;
		snapshot = null;
		creatureAnchors.clear();
		firstTargetLogged.clear();
		giantNpc = SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		super.onStart();

		if (giantNpc == null) {
			LOGGER.warn(getEventName() + " cannot start objective tracking: NPC '" + GIANT_NPC_NAME + "' not found.");
			failedByGiantHealth = true;
			endEvent();
			return;
		}

		snapshot = GiantSnapshot.capture(giantNpc);
		giantNpc.setBaseHP(GIANT_EVENT_HP);
		giantNpc.setHP(GIANT_EVENT_HP);
		scheduleHealthMonitor();
		scheduleAggroController();
	}

	@Override
	protected void onStop() {
		SingletonRepository.getTurnNotifier().dontNotify(giantHealthMonitor);
		SingletonRepository.getTurnNotifier().dontNotify(escortAggroController);

		final SpeakerNPC currentGiant = giantNpc;
		escortSuccess = !failedByGiantHealth
				&& currentGiant != null
				&& currentGiant.getHP() > GIANT_FAIL_HP_THRESHOLD;

		if (currentGiant != null && snapshot != null) {
			snapshot.restore(currentGiant);
		}
		snapshot = null;
		giantNpc = null;
		creatureAnchors.clear();
		firstTargetLogged.clear();

		super.onStop();
	}

	@Override
	protected String getStopAnnouncementMessage() {
		if (escortSuccess) {
			return "Eskorta zakończona sukcesem! Wielkolud przetrwał napór wrogów.";
		}
		return "Eskorta zakończona porażką - Wielkolud został ciężko ranny.";
	}

	private void monitorGiantHealth() {
		if (!isEventActive()) {
			return;
		}

		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		giantNpc = currentGiant;
		if (currentGiant == null || currentGiant.getHP() <= GIANT_FAIL_HP_THRESHOLD) {
			failedByGiantHealth = true;
			endEvent();
			return;
		}

		scheduleHealthMonitor();
	}

	private void scheduleHealthMonitor() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(HEALTH_CHECK_INTERVAL_SECONDS, giantHealthMonitor);
	}

	private void scheduleAggroController() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(AGGRO_TICK_INTERVAL_SECONDS, escortAggroController);
	}

	private void applyEscortAggroLogic() {
		if (!isEventActive()) {
			return;
		}

		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		giantNpc = currentGiant;

		for (final Creature creature : getEventCreaturesSnapshot()) {
			if (creature == null || creature.getZone() == null) {
				continue;
			}

			final int creatureId = creature.getID().getObjectID();
			creatureAnchors.computeIfAbsent(creatureId, ignored -> new SpawnAnchor(creature.getZone().getName(), creature.getX(), creature.getY()));

			if (isOutsideEscortZone(creature, creatureAnchors.get(creatureId))) {
				resetCreatureTarget(creature);
				continue;
			}

			if (isBeyondLeashRange(creature, creatureAnchors.get(creatureId))) {
				resetCreatureTarget(creature);
				continue;
			}

			if (canPrioritizeGiant(creature, currentGiant)) {
				applyTargetIfNeeded(creature, currentGiant, "giant_priority");
				continue;
			}

			if (creature.getAttackTarget() == currentGiant) {
				creature.stopAttack();
			}

			final RPEntity fallbackTarget = creature.getNearestEnemy(PLAYER_FALLBACK_RANGE);
			if (fallbackTarget != null) {
				applyTargetIfNeeded(creature, fallbackTarget, "player_fallback");
			}
		}

		scheduleAggroController();
	}

	private boolean canPrioritizeGiant(final Creature creature, final SpeakerNPC giant) {
		if (giant == null || giant.getZone() == null || giant.getHP() <= 0) {
			return false;
		}
		if (creature.getZone() != giant.getZone()) {
			return false;
		}
		if (creature.squaredDistance(giant) > GIANT_TARGET_RANGE * GIANT_TARGET_RANGE) {
			return false;
		}
		if (!creature.hasLineOfSight(giant)) {
			return false;
		}
		return Path.searchPath(
				creature.getZone(),
				creature.getX(),
				creature.getY(),
				giant.getX(),
				giant.getY(),
				MAX_GIANT_PATH_LENGTH) != null;
	}

	private void applyTargetIfNeeded(final Creature creature, final RPEntity target, final String source) {
		if (target == null) {
			return;
		}
		if (creature.getAttackTarget() != target) {
			creature.setTarget(target);
		}
		if (firstTargetLogged.add(creature.getID().getObjectID()) && LOGGER.isDebugEnabled()) {
			LOGGER.debug(getEventName() + " first target for event creature '" + creature.getName() + "' (id="
					+ creature.getID().getObjectID() + ") => " + target.getName() + " via " + source + ".");
		}
	}

	private void resetCreatureTarget(final Creature creature) {
		creature.stopAttack();
		creature.clearPath();
		creature.stop();
	}

	private static boolean isOutsideEscortZone(final Creature creature, final SpawnAnchor anchor) {
		return anchor != null && !anchor.zoneName.equals(creature.getZone().getName());
	}

	private static boolean isBeyondLeashRange(final Creature creature, final SpawnAnchor anchor) {
		if (anchor == null) {
			return false;
		}
		final int dx = creature.getX() - anchor.x;
		final int dy = creature.getY() - anchor.y;
		return dx * dx + dy * dy > LEASH_RANGE * LEASH_RANGE;
	}

	private static final class GiantSnapshot {
		private final int hp;
		private final int baseHp;
		private final int atk;
		private final int ratk;
		private final int def;

		private GiantSnapshot(final int hp, final int baseHp, final int atk, final int ratk, final int def) {
			this.hp = hp;
			this.baseHp = baseHp;
			this.atk = atk;
			this.ratk = ratk;
			this.def = def;
		}

		private static GiantSnapshot capture(final SpeakerNPC giant) {
			return new GiantSnapshot(giant.getHP(), giant.getBaseHP(), giant.getAtk(), giant.getRatk(), giant.getDef());
		}

		private void restore(final SpeakerNPC giant) {
			giant.setAtk(atk);
			giant.setRatk(ratk);
			giant.setDef(def);
			giant.setBaseHP(baseHp);
			giant.setHP(Math.min(hp, baseHp));
		}
	}

	private static final class SpawnAnchor {
		private final String zoneName;
		private final int x;
		private final int y;

		private SpawnAnchor(final String zoneName, final int x, final int y) {
			this.zoneName = zoneName;
			this.x = x;
			this.y = y;
		}
	}
}
