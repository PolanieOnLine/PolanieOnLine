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
package games.stendhal.server.maps.koscielisko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.IntPredicate;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.Task;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.EventActivityChestRewardService;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;
import games.stendhal.server.maps.event.MapEventContributionTracker;
import games.stendhal.server.maps.event.MapEventRewardPolicy;
import games.stendhal.server.maps.event.RandomEventRewardService;
import games.stendhal.server.maps.event.RandomSafeSpotSpawnStrategy;

public final class KoscieliskoGiantEscortEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(KoscieliskoGiantEscortEvent.class);
	private static final String GIANT_NPC_NAME = "Wielkolud";
	private static final int MAX_ENTITY_HP_SHORT = 32767;
	private static final int GIANT_FAIL_HP_THRESHOLD = 100;
	private static final int GIANT_CRITICAL_HP_THRESHOLD = 10000;
	private static final int GIANT_HEAL_CAP_PER_SECOND = 0;
	private static final int HEALTH_CHECK_INTERVAL_SECONDS = 1;
	private static final int AGGRO_TICK_INTERVAL_SECONDS = 1;
	private static final int ACTIVITY_SAMPLE_INTERVAL_SECONDS = 10;
	private static final int MIN_PLAYERS_TO_START = 3;
	private static final int MAX_LOW_ACTIVITY_TICKS = 12;
	private static final int GIANT_TARGET_RANGE = 18;
	private static final int PLAYER_FALLBACK_RANGE = 14;
	private static final int LEASH_RANGE = 24;
	private static final int MAX_GIANT_PATH_LENGTH = 96;
	private static final int GIANT_CHASE_GRACE_TICKS = 4;
	private static final int ESCORT_RING_MIN_RADIUS = 4;
	private static final int ESCORT_RING_MAX_RADIUS = 10;
	private static final int ESCORT_SPAWN_ATTEMPTS = 30;
	private static final int LOW_PRESSURE_ACTIVE_CREATURE_THRESHOLD = 7;
	private static final int WAVE_STATUS_MIN_INTERVAL_SECONDS = 50;
	private static final int CRITICAL_STATUS_MIN_INTERVAL_SECONDS = 15;
	private static final int HP_STATUS_DELTA_PERCENT_THRESHOLD = 5;
	private static final int[] WAVE_PROGRESS_MILESTONES = { 25, 50, 75, 90 };
	private static volatile long lastEventFinishedAtMillis;

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

	private final TurnListener activityTracker = new TurnListener() {
		@Override
		public void onTurnReached(final int currentTurn) {
			samplePlayerActivity();
		}
	};

	private final MapEventConfig.EscortSettings escortSettings;
	private final int giantDefBonus;
	private final int giantResistance;
	private final int eventCooldownMinutes;
	private final int eventActiveCreatureHardCap;
	private final int waveBudgetBase;
	private final int waveBudgetPerStage;

	private volatile SpeakerNPC giantNpc;
	private volatile GiantSnapshot snapshot;
	private volatile boolean failedByGiantHealth;
	private volatile boolean failedByLowActivity;
	private volatile boolean blockedByCooldown;
	private volatile boolean criticalHealthAnnounced;
	private volatile boolean halfProgressAnnounced;
	private volatile boolean escortSuccess;
	private volatile long eventStartedAtMillis;
	private volatile int giantEventHp;
	private volatile int giantHpBeforeTick;
	private volatile int lowActivityTicks;
	private volatile double finalHealthRatio;
	private volatile int lastAnnouncedHpPercent;
	private final Map<Integer, SpawnAnchor> creatureAnchors = new HashMap<>();
	private final Map<Integer, Integer> lostGiantSightTicks = new HashMap<>();
	private final Set<Integer> firstTargetLogged = new HashSet<>();
	private final Map<String, PlayerSnapshot> playerSnapshots = new HashMap<>();
	private final Map<String, Integer> playerActivityTicks = new HashMap<>();
	private final MapEventContributionTracker contributionTracker = new MapEventContributionTracker();
	private final MapEventRewardPolicy rewardPolicy = MapEventRewardPolicy.defaultEscortPolicy();
	private final RandomEventRewardService randomEventRewardService = new RandomEventRewardService();
	private final Set<Integer> announcedWaveOffsets = new HashSet<>();
	private final Set<Integer> announcedWaveMilestones = new HashSet<>();
	private final RandomSafeSpotSpawnStrategy fallbackSpawnStrategy = new RandomSafeSpotSpawnStrategy(LOGGER);
	private final BroadcastRateLimiter operationalBroadcastLimiter = new BroadcastRateLimiter(
			TimeUnit.SECONDS.toMillis(WAVE_STATUS_MIN_INTERVAL_SECONDS));
	private final BroadcastRateLimiter criticalBroadcastLimiter = new BroadcastRateLimiter(
			TimeUnit.SECONDS.toMillis(CRITICAL_STATUS_MIN_INTERVAL_SECONDS));
	private volatile int currentWaveOffsetSeconds = -1;
	private volatile int currentWaveBudget = 0;

	public KoscieliskoGiantEscortEvent() {
		this(MapEventConfigLoader.load(MapEventConfigLoader.KOSCIELISKO_GIANT_ESCORT));
	}

	private KoscieliskoGiantEscortEvent(final MapEventConfig config) {
		super(LOGGER, config);
		escortSettings = config.getEscortSettings();
		giantDefBonus = resolveConfiguredInt(
			escortSettings != null ? escortSettings.getGiantDefBonus() : null,
			220,
			value -> value >= 0);
		giantResistance = resolveConfiguredInt(
			escortSettings != null ? escortSettings.getResistance() : null,
			40,
			value -> value >= 0);
		eventCooldownMinutes = resolveConfiguredInt(
			escortSettings != null ? escortSettings.getCooldownMinutes() : null,
			45,
			value -> value >= 0);
		eventActiveCreatureHardCap = resolveConfiguredInt(
			escortSettings != null ? escortSettings.getHardCap() : null,
			34,
			value -> value > 0);
		waveBudgetBase = resolveConfiguredInt(
			escortSettings != null ? escortSettings.getWaveBudgetBase() : null,
			10,
			value -> value >= 0);
		waveBudgetPerStage = resolveConfiguredInt(
			escortSettings != null ? escortSettings.getWaveBudgetPerStage() : null,
			4,
			value -> value >= 0);
	}

	@Override
	protected void onStart() {
		failedByGiantHealth = false;
		failedByLowActivity = false;
		blockedByCooldown = false;
		criticalHealthAnnounced = false;
		halfProgressAnnounced = false;
		escortSuccess = false;
		finalHealthRatio = 0.0d;
		lastAnnouncedHpPercent = -1;
		snapshot = null;
		eventStartedAtMillis = 0L;
		giantEventHp = resolveGiantEventHp();
		giantHpBeforeTick = giantEventHp;
		lowActivityTicks = 0;
		creatureAnchors.clear();
		lostGiantSightTicks.clear();
		firstTargetLogged.clear();
		playerSnapshots.clear();
		playerActivityTicks.clear();
		contributionTracker.clear();
		announcedWaveOffsets.clear();
		announcedWaveMilestones.clear();
		operationalBroadcastLimiter.clear();
		criticalBroadcastLimiter.clear();
		currentWaveOffsetSeconds = -1;
		currentWaveBudget = 0;

		if (shouldValidateStartGates() && !validateStartGates()) {
			endEvent();
			return;
		}

		giantNpc = SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		super.onStart();

		if (giantNpc == null) {
			LOGGER.warn(getEventName() + " cannot start objective tracking: NPC '" + GIANT_NPC_NAME + "' not found.");
			failedByGiantHealth = true;
			endEvent();
			return;
		}

		snapshot = GiantSnapshot.capture(giantNpc);
		giantEventHp = resolveGiantEventHp();
		LOGGER.info(getEventName() + " escort start: giant resistance before buff=" + snapshot.resistance
				+ ", event resistance=" + giantResistance + ".");
		giantNpc.setBaseHP(giantEventHp);
		giantNpc.setHP(giantEventHp);
		giantNpc.setResistance(giantResistance);
		applyEscortSurvivabilityBuff(giantNpc, snapshot);
		eventStartedAtMillis = System.currentTimeMillis();
		giantHpBeforeTick = giantEventHp;
		announceProgressStatus("START", 0);
		scheduleHealthMonitor();
		scheduleAggroController();
		scheduleActivityTracker();
	}

	@Override
	protected void onStop() {
		SingletonRepository.getTurnNotifier().dontNotify(giantHealthMonitor);
		SingletonRepository.getTurnNotifier().dontNotify(escortAggroController);
		SingletonRepository.getTurnNotifier().dontNotify(activityTracker);

		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		try {
			escortSuccess = !failedByGiantHealth
					&& !failedByLowActivity
					&& currentGiant != null
					&& currentGiant.getHP() > GIANT_FAIL_HP_THRESHOLD;

			if (currentGiant != null) {
				finalHealthRatio = Math.max(0.0d, Math.min(1.0d, (double) currentGiant.getHP() / giantEventHp));
			}

			if (escortSuccess) {
				rewardParticipants();
			}
		} finally {
			if (snapshot != null && currentGiant != null) {
				final int resistanceBeforeRestore = currentGiant.getResistance();
				snapshot.restore(currentGiant);
				LOGGER.info(getEventName() + " escort stop: giant resistance before restore=" + resistanceBeforeRestore
						+ ", restored resistance=" + currentGiant.getResistance() + ".");
			} else {
				LOGGER.info(getEventName() + " escort stop: snapshot restore skipped (giantFound="
						+ (currentGiant != null) + ", snapshotPresent=" + (snapshot != null) + ").");
			}

			snapshot = null;
			giantNpc = null;
			creatureAnchors.clear();
			lostGiantSightTicks.clear();
			firstTargetLogged.clear();
			playerSnapshots.clear();
			playerActivityTicks.clear();
			contributionTracker.clear();
			announcedWaveOffsets.clear();
			announcedWaveMilestones.clear();
			operationalBroadcastLimiter.clear();
			criticalBroadcastLimiter.clear();
			currentWaveOffsetSeconds = -1;
			currentWaveBudget = 0;
			lastEventFinishedAtMillis = System.currentTimeMillis();

			super.onStop();
		}
	}

	@Override
	protected String getStopAnnouncementMessage() {
		if (escortSuccess) {
			return "Wielkolud ocalał. Szlak utrzymany (siły: " + Math.round(finalHealthRatio * 100.0d) + "%).";
		}
		if (blockedByCooldown) {
			return "Szlak jeszcze zamknięty. Dajcie ludziom odetchnąć przed kolejną eskortą.";
		}
		if (failedByLowActivity) {
			return "Eskorta padła. Za mało obrońców na szlaku.";
		}
		return "Eskorta padła. Wielkolud nie utrzymał marszu.";
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		final Integer waveOffset = findWaveOffset(spawn);
		if (waveOffset == null) {
			return;
		}

		initializeWaveBudgetIfNeeded(waveOffset);

		if (!announcedWaveOffsets.add(waveOffset)) {
			return;
		}
		final int durationSeconds = (int) getEventDuration().getSeconds();
		final int progress = durationSeconds <= 0 ? 0 : (int) Math.round((waveOffset * 100.0d) / durationSeconds);
		final int progressPercent = Math.min(progress, 100);
		final Integer milestone = findWaveMilestone(progressPercent);
		if (milestone == null || !announcedWaveMilestones.add(milestone)) {
			return;
		}
		if (!operationalBroadcastLimiter.tryAcquire("FALA_" + milestone, System.currentTimeMillis())) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(getEventName() + " wave status skipped by limiter at " + progressPercent
						+ "% progress (milestone=" + milestone + "%).");
			}
			return;
		}
		announceProgressStatus("FALA_" + milestone, milestone);
	}

	private Integer findWaveMilestone(final int progressPercent) {
		for (final int milestone : WAVE_PROGRESS_MILESTONES) {
			if (progressPercent >= milestone) {
				return milestone;
			}
		}
		return null;
	}

	@Override
	protected void spawnCreatures(final String creatureName, final int count) {
		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		giantNpc = currentGiant;

		int nearGiantSpawns = 0;
		int fallbackSpawns = 0;
		int plannedSpawn = 0;
		int cappedByLimit = 0;
		int activeCreatures = countActiveEventCreatures();

		for (final String zoneName : getZones()) {
			final int requestedCount = count;
			final double spawnMultiplier = getConfig().getZoneSpawnMultiplier(zoneName);
			final int multipliedCount = (int) Math.round(requestedCount * spawnMultiplier);
			final Integer zoneSpawnCap = getConfig().getZoneSpawnCap(zoneName);
			final int finalSpawnCount = zoneSpawnCap == null ? multipliedCount : Math.min(multipliedCount, zoneSpawnCap);
			plannedSpawn += Math.max(0, finalSpawnCount);

			if (finalSpawnCount <= 0) {
				continue;
			}

			final int hardCapSlots = Math.max(0, eventActiveCreatureHardCap - activeCreatures);
			int budgetSlots = getCurrentWaveBudget();
			if (activeCreatures < LOW_PRESSURE_ACTIVE_CREATURE_THRESHOLD && hardCapSlots > 0 && budgetSlots <= 0) {
				budgetSlots = 1;
			}

			final int allowedByLimits = Math.min(finalSpawnCount, Math.min(hardCapSlots, budgetSlots));
			if (allowedByLimits <= 0) {
				cappedByLimit += finalSpawnCount;
				continue;
			}

			cappedByLimit += Math.max(0, finalSpawnCount - allowedByLimits);

			if (currentGiant != null && currentGiant.getZone() != null
					&& currentGiant.getZone().getName().equals(zoneName)) {
				final SpawnStats stats = spawnAroundGiantInZone(currentGiant, creatureName, allowedByLimits);
				nearGiantSpawns += stats.nearGiant;
				fallbackSpawns += stats.fallback;
				final int spawnedNow = stats.nearGiant + stats.fallback;
				consumeWaveBudget(spawnedNow);
				activeCreatures += spawnedNow;
				continue;
			}

			LOGGER.warn(getEventName() + " cannot resolve giant NPC in zone " + zoneName
					+ "; falling back to RandomSafeSpotSpawnStrategy for " + creatureName + ".");
			final int fallbackSpawned = spawnWithFallback(zoneName, creatureName, allowedByLimits);
			fallbackSpawns += fallbackSpawned;
			consumeWaveBudget(fallbackSpawned);
			activeCreatures += fallbackSpawned;
		}

		if (LOGGER.isDebugEnabled()) {
			final int spawned = nearGiantSpawns + fallbackSpawns;
			LOGGER.debug(getEventName() + " spawn budget for '" + creatureName + "': plannedSpawn=" + plannedSpawn
					+ ", spawned=" + spawned + ", cappedByLimit=" + cappedByLimit + ", active="
					+ countActiveEventCreatures() + ", remainingWaveBudget=" + getCurrentWaveBudget() + ".");
			LOGGER.debug(getEventName() + " spawn summary for '" + creatureName + "': near giant=" + nearGiantSpawns
					+ ", fallback=" + fallbackSpawns + ".");
		}
	}
	
	private synchronized void initializeWaveBudgetIfNeeded(final int waveOffset) {
		if (waveOffset == currentWaveOffsetSeconds) {
			return;
		}
		currentWaveOffsetSeconds = waveOffset;
		final int stage = resolveDifficultyStage(waveOffset);
		currentWaveBudget = Math.min(eventActiveCreatureHardCap, waveBudgetBase + ((stage - 1) * waveBudgetPerStage));
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getEventName() + " wave budget initialized: waveOffset=" + waveOffset + "s, stage=" + stage
					+ ", budget=" + currentWaveBudget + ", active=" + countActiveEventCreatures() + ", hardCap="
					+ eventActiveCreatureHardCap + ".");
		}
	}

	private int resolveDifficultyStage(final int waveOffsetSeconds) {
		final int durationSeconds = (int) Math.max(1, getEventDuration().getSeconds());
		final double progress = Math.max(0.0d, Math.min(1.0d, waveOffsetSeconds / (double) durationSeconds));
		if (progress <= 0.25d) {
			return 1;
		}
		if (progress <= 0.50d) {
			return 2;
		}
		if (progress <= 0.75d) {
			return 3;
		}
		return 4;
	}

	private synchronized int getCurrentWaveBudget() {
		return currentWaveBudget;
	}

	private synchronized void consumeWaveBudget(final int spawned) {
		if (spawned <= 0) {
			return;
		}
		currentWaveBudget = Math.max(0, currentWaveBudget - spawned);
	}

	private int resolveGiantEventHp() {
		final int configuredHp = resolveConfiguredInt(
			escortSettings != null ? escortSettings.getGiantHp() : null,
			32000,
			value -> value > 0);
		return clampEntityHpToShort(configuredHp, "escortSettings.giantHp");
	}

	private int resolveConfiguredInt(final Integer configuredValue,
			final int fallbackValue,
			final IntPredicate validator) {
		if (configuredValue == null) {
			return fallbackValue;
		}
		if (!validator.test(configuredValue)) {
			LOGGER.warn(getEventName() + " invalid escort setting value=" + configuredValue
					+ "; falling back to " + fallbackValue + ".");
			return fallbackValue;
		}
		return configuredValue;
	}

	private int countActiveEventCreatures() {
		int active = 0;
		for (final Creature creature : getEventCreaturesSnapshot()) {
			if (creature != null && creature.getZone() != null && creature.getHP() > 0) {
				active++;
			}
		}
		return active;
	}

	private SpawnStats spawnAroundGiantInZone(final SpeakerNPC currentGiant, final String creatureName, final int count) {
		int successfulSpawns = 0;
		int fallbackSpawns = 0;
		for (int i = 0; i < count; i++) {
			final Creature template = SingletonRepository.getEntityManager().getCreature(creatureName);
			if (template == null) {
				LOGGER.warn(getEventName() + " missing creature template: " + creatureName + ".");
				break;
			}

			final Creature creature = new Creature(template.getNewInstance());
			if (placeCreatureNearGiant(currentGiant, creature)) {
				registerEventCreature(creature);
				successfulSpawns++;
				continue;
			}

			fallbackSpawns += spawnWithFallback(currentGiant.getZone().getName(), creatureName, 1);
		}
		return new SpawnStats(successfulSpawns, fallbackSpawns);
	}

	private int spawnWithFallback(final String zoneName, final String creatureName, final int count) {
		final int before = getEventCreaturesSnapshot().size();
		fallbackSpawnStrategy.spawnCreatures(
				getEventName(),
				Collections.singletonList(zoneName),
				creatureName,
				count,
				this::registerEventCreature);
		return Math.max(0, getEventCreaturesSnapshot().size() - before);
	}

	private boolean placeCreatureNearGiant(final SpeakerNPC currentGiant, final Creature creature) {
		final StendhalRPZone zone = currentGiant.getZone();
		if (zone == null) {
			return false;
		}

		for (int attempt = 0; attempt < ESCORT_SPAWN_ATTEMPTS; attempt++) {
			final int radius = ESCORT_RING_MIN_RADIUS + Rand.rand(ESCORT_RING_MAX_RADIUS - ESCORT_RING_MIN_RADIUS + 1);
			final double angle = Rand.rand(3600) / 10.0d;
			final int x = currentGiant.getX() + (int) Math.round(radius * Math.cos(Math.toRadians(angle)));
			final int y = currentGiant.getY() + (int) Math.round(radius * Math.sin(Math.toRadians(angle)));

			if (!isPassableAt(zone, creature, x, y)) {
				continue;
			}
			if (StendhalRPAction.placeat(zone, creature, x, y)) {
				return true;
			}
		}

		return false;
	}

	private static boolean isPassableAt(final StendhalRPZone zone, final Creature creature, final int x, final int y) {
		if (x < 0 || y < 0 || x >= zone.getWidth() || y >= zone.getHeight()) {
			return false;
		}
		return !zone.collides(creature, x, y);
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

		limitHealingPerTick(currentGiant);
		announceMilestones(currentGiant);

		scheduleHealthMonitor();
	}

	private void scheduleHealthMonitor() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(HEALTH_CHECK_INTERVAL_SECONDS, giantHealthMonitor);
	}

	private void scheduleAggroController() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(AGGRO_TICK_INTERVAL_SECONDS, escortAggroController);
	}

	private void scheduleActivityTracker() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(ACTIVITY_SAMPLE_INTERVAL_SECONDS, activityTracker);
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
				resetCreatureTarget(creature, "outside_escort_zone", true);
				continue;
			}

			if (isBeyondLeashRange(creature, creatureAnchors.get(creatureId))) {
				resetCreatureTarget(creature, "beyond_leash_range", true);
				continue;
			}

			final GiantAggroState giantAggroState = evaluateGiantAggroState(creature, currentGiant);
			if (giantAggroState == GiantAggroState.READY_TO_ATTACK) {
				lostGiantSightTicks.remove(creatureId);
				applyTargetIfNeeded(creature, currentGiant, "giant_priority");
				continue;
			}

			if (giantAggroState == GiantAggroState.CRITICAL_GIANT_UNAVAILABLE) {
				resetCreatureTarget(creature, "giant_unavailable", true);
				continue;
			}

			if (giantAggroState == GiantAggroState.TEMPORARY_ATTACK_BLOCKED && getConfig().isGiantOnlyAggro()
					&& creature.getAttackTarget() == currentGiant) {
				final int lostTicks = lostGiantSightTicks.getOrDefault(creatureId, 0) + 1;
				lostGiantSightTicks.put(creatureId, lostTicks);
				if (lostTicks <= GIANT_CHASE_GRACE_TICKS) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(getEventName() + " preserving giant chase for '" + creature.getName() + "' (id="
								+ creatureId + ") tick=" + lostTicks + "/" + GIANT_CHASE_GRACE_TICKS + ".");
					}
					continue;
				}
				resetCreatureTarget(creature, "giant_chase_grace_expired", false);
				continue;
			}

			lostGiantSightTicks.remove(creatureId);

			if (creature.getAttackTarget() == currentGiant) {
				creature.stopAttack();
			}

			if (getConfig().isGiantOnlyAggro()) {
				resetCreatureTarget(creature, "giant_only_aggro_no_valid_target", false);
				continue;
			}

			final RPEntity fallbackTarget = creature.getNearestEnemy(PLAYER_FALLBACK_RANGE);
			if (fallbackTarget != null) {
				applyTargetIfNeeded(creature, fallbackTarget, "player_fallback");
			}
		}

		scheduleAggroController();
	}

	private GiantAggroState evaluateGiantAggroState(final Creature creature, final SpeakerNPC giant) {
		if (giant == null || giant.getZone() == null || giant.getHP() <= 0) {
			return GiantAggroState.CRITICAL_GIANT_UNAVAILABLE;
		}
		if (creature.getZone() != giant.getZone()) {
			return GiantAggroState.NOT_LOGICALLY_VALID;
		}
		if (creature.squaredDistance(giant) > GIANT_TARGET_RANGE * GIANT_TARGET_RANGE) {
			return GiantAggroState.NOT_LOGICALLY_VALID;
		}
		if (creature.getX() == giant.getX() && creature.getY() == giant.getY()) {
			return GiantAggroState.READY_TO_ATTACK;
		}
		if (!creature.hasLineOfSight(giant)) {
			return GiantAggroState.TEMPORARY_ATTACK_BLOCKED;
		}
		if (Path.searchPath(
				creature.getZone(),
				creature.getX(),
				creature.getY(),
				giant.getX(),
				giant.getY(),
				MAX_GIANT_PATH_LENGTH) != null) {
			return GiantAggroState.READY_TO_ATTACK;
		}
		return GiantAggroState.TEMPORARY_ATTACK_BLOCKED;
	}

	private void applyTargetIfNeeded(final Creature creature, final RPEntity target, final String source) {
		if (target == null) {
			return;
		}
		if (creature.getAttackTarget() != target) {
			if (LOGGER.isDebugEnabled()) {
				final RPEntity previousTarget = creature.getAttackTarget();
				LOGGER.debug(getEventName() + " target change for event creature '" + creature.getName() + "' (id="
						+ creature.getID().getObjectID() + "): "
						+ (previousTarget == null ? "none" : previousTarget.getName()) + " -> " + target.getName()
						+ " via " + source + ".");
			}
			creature.setTarget(target);
		}
		lostGiantSightTicks.remove(creature.getID().getObjectID());
		if (firstTargetLogged.add(creature.getID().getObjectID()) && LOGGER.isDebugEnabled()) {
			LOGGER.debug(getEventName() + " first target for event creature '" + creature.getName() + "' (id="
					+ creature.getID().getObjectID() + ") => " + target.getName() + " via " + source + ".");
		}
	}

	private void resetCreatureTarget(final Creature creature, final String reason, final boolean forced) {
		lostGiantSightTicks.remove(creature.getID().getObjectID());
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getEventName() + " resetting target for event creature '" + creature.getName() + "' (id="
					+ creature.getID().getObjectID() + "), reason=" + reason + ", forced=" + forced + ".");
		}
		creature.stopAttack();
		creature.clearPath();
		creature.stop();
	}

	private enum GiantAggroState {
		READY_TO_ATTACK,
		TEMPORARY_ATTACK_BLOCKED,
		NOT_LOGICALLY_VALID,
		CRITICAL_GIANT_UNAVAILABLE
	}

	private void samplePlayerActivity() {
		if (!isEventActive()) {
			return;
		}

		final SpeakerNPC currentGiant = giantNpc;
		if (currentGiant == null || currentGiant.getZone() == null) {
			scheduleActivityTracker();
			return;
		}

		int movedPlayers = 0;
		final List<Player> players = playersInObserverZones();
		for (final Player player : players) {
			if (player == null || player.getZone() == null || player.getZone() != currentGiant.getZone()) {
				continue;
			}
			contributionTracker.recordTimeInZone(player.getName(), ACTIVITY_SAMPLE_INTERVAL_SECONDS);

			final PlayerSnapshot previous = playerSnapshots.get(player.getName());
			final PlayerSnapshot current = new PlayerSnapshot(player.getX(), player.getY(), System.currentTimeMillis());
			playerSnapshots.put(player.getName(), current);

			if (isNearObjective(currentGiant, player)) {
				contributionTracker.recordObjectiveAction(player.getName(), 1);
			}

			if (previous != null && previous.hasMovedTo(current)) {
				movedPlayers++;
				final int ticks = playerActivityTicks.getOrDefault(player.getName(), 0) + 1;
				playerActivityTicks.put(player.getName(), ticks);
				contributionTracker.recordObjectiveAction(player.getName(), 1);
			}
		}

		if (movedPlayers < MIN_PLAYERS_TO_START) {
			lowActivityTicks++;
			if (lowActivityTicks >= MAX_LOW_ACTIVITY_TICKS) {
				failedByLowActivity = true;
				endEvent();
				return;
			}
		} else {
			lowActivityTicks = 0;
		}

		scheduleActivityTracker();
	}

	private boolean isNearObjective(final SpeakerNPC currentGiant, final Player player) {
		if (currentGiant == null || player == null || currentGiant.getZone() == null || player.getZone() == null
				|| currentGiant.getZone() != player.getZone()) {
			return false;
		}
		final int dx = currentGiant.getX() - player.getX();
		final int dy = currentGiant.getY() - player.getY();
		return (dx * dx + dy * dy) <= (PLAYER_FALLBACK_RANGE * PLAYER_FALLBACK_RANGE);
	}

	@Override
	protected void onEventCreatureDeath(final CircumstancesOfDeath circs) {
		if (circs == null || !(circs.getVictim() instanceof RPEntity)) {
			return;
		}

		final RPEntity victim = (RPEntity) circs.getVictim();
		for (Entry<Entity, Integer> entry : victim.getDamageReceivedSnapshot().entrySet()) {
			final Player player = asOnlinePlayer(entry.getKey());
			if (player == null) {
				continue;
			}
			contributionTracker.recordDamage(player.getName(), entry.getValue());
		}

		final Player killer = circs.getKiller() instanceof Entity
				? asOnlinePlayer((Entity) circs.getKiller())
				: null;
		final String killerName = killer == null ? null : killer.getName();
		for (Entry<Entity, Integer> entry : victim.getDamageReceivedSnapshot().entrySet()) {
			final Player player = asOnlinePlayer(entry.getKey());
			if (player == null || player.getName().equals(killerName) || entry.getValue() <= 0) {
				continue;
			}
			contributionTracker.recordKillAssist(player.getName(), 1);
		}
	}

	private void limitHealingPerTick(final SpeakerNPC currentGiant) {
		final int currentHp = currentGiant.getHP();
		if (currentHp > giantHpBeforeTick + GIANT_HEAL_CAP_PER_SECOND) {
			currentGiant.setHP(giantHpBeforeTick + GIANT_HEAL_CAP_PER_SECOND);
		}
		giantHpBeforeTick = currentGiant.getHP();
	}

	private void announceMilestones(final SpeakerNPC currentGiant) {
		if (!halfProgressAnnounced && eventStartedAtMillis > 0L) {
			final long elapsedMillis = System.currentTimeMillis() - eventStartedAtMillis;
			if (elapsedMillis >= getEventDuration().toMillis() / 2) {
				halfProgressAnnounced = true;
				announceProgressStatus("50%", 50);
			}
		}

		if (!criticalHealthAnnounced && currentGiant.getHP() <= GIANT_CRITICAL_HP_THRESHOLD) {
			final int hpPercent = Math.max(0, Math.round((currentGiant.getHP() * 100.0f) / giantEventHp));
			if (criticalBroadcastLimiter.tryAcquire("GIANT_CRITICAL_HP", System.currentTimeMillis())) {
				criticalHealthAnnounced = true;
				SingletonRepository.getRuleProcessor().tellAllPlayers(
						NotificationType.PRIVMSG,
						"[Kościelisko] Wielkolud słabnie! Zostało mu " + hpPercent + "% sił.");
			}
		}
	}

	private void applyEscortSurvivabilityBuff(final SpeakerNPC giant, final GiantSnapshot baseline) {
		if (giant == null || baseline == null) {
			return;
		}

		giant.setDef(baseline.def + giantDefBonus);
	}

	private void announceProgressStatus(final String stage, final int progressPercent) {
		final SpeakerNPC currentGiant = giantNpc;
		final int hpPercent = currentGiant == null ? 0 : Math.max(0, Math.round((currentGiant.getHP() * 100.0f) / giantEventHp));
		final boolean includeHp = shouldAttachHp(stage, hpPercent);
		final String hpSuffix = includeHp ? " (siły: " + hpPercent + "%)." : ".";
		final String message;
		switch (stage) {
			case "START":
				message = "[Kościelisko] Wielkolud ruszył. Pilnujcie szlaku" + hpSuffix;
				break;
			case "50%":
				message = "[Kościelisko] Pół drogi za nami. Trzymajcie szyk" + hpSuffix;
				break;
			case "FALA_25":
				message = "[Kościelisko] Pierwsza linia pęka. Odepchnijcie zejście" + hpSuffix;
				break;
			case "FALA_50":
				message = "[Kościelisko] Presja rośnie. Trzymajcie środek szlaku" + hpSuffix;
				break;
			case "FALA_75":
				message = "[Kościelisko] Szturm się zagęszcza. Osłońcie Wielkoluda" + hpSuffix;
				break;
			case "FALA_90":
				message = "[Kościelisko] Ostatni napór! Utrzymajcie marsz do końca" + hpSuffix;
				break;
			default:
				message = "[Kościelisko] Marsz trwa (" + progressPercent + "%)" + hpSuffix;
				break;
		}
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				message);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getEventName() + " operational broadcast sent: stage=" + stage + ", progress="
					+ progressPercent + "%, hp=" + hpPercent + "%, includeHp=" + includeHp + ".");
		}
		notifyAdminsDebug("[Kościelisko][admin] status=" + stage + ", progress=" + progressPercent
				+ "%, hp=" + hpPercent + "%, includeHp=" + includeHp + ".");
	}

	private synchronized boolean shouldAttachHp(final String stage, final int hpPercent) {
		if ("START".equals(stage)) {
			lastAnnouncedHpPercent = hpPercent;
			return true;
		}
		if (lastAnnouncedHpPercent < 0) {
			lastAnnouncedHpPercent = hpPercent;
			return true;
		}
		if ((lastAnnouncedHpPercent - hpPercent) >= HP_STATUS_DELTA_PERCENT_THRESHOLD) {
			lastAnnouncedHpPercent = hpPercent;
			return true;
		}
		return false;
	}

	private void notifyAdminsDebug(final String message) {
		SingletonRepository.getRuleProcessor().getOnlinePlayers().forAllPlayersExecute(new Task<Player>() {
			@Override
			public void execute(final Player player) {
				if (player.getAdminLevel() > 0) {
					player.sendPrivateText(NotificationType.SUPPORT, message);
				}
			}
		});
	}

	static final class BroadcastRateLimiter {
		private final long minIntervalMillis;
		private final Map<String, Long> lastBroadcastAtMillis = new HashMap<>();

		BroadcastRateLimiter(final long minIntervalMillis) {
			this.minIntervalMillis = Math.max(0L, minIntervalMillis);
		}

		synchronized boolean tryAcquire(final String key, final long nowMillis) {
			final Long previous = lastBroadcastAtMillis.get(key);
			if (previous != null && (nowMillis - previous) < minIntervalMillis) {
				return false;
			}
			lastBroadcastAtMillis.put(key, nowMillis);
			return true;
		}

		synchronized void clear() {
			lastBroadcastAtMillis.clear();
		}
	}

	private boolean isCooldownReady() {
		if (lastEventFinishedAtMillis <= 0L) {
			return true;
		}
		final long cooldownMillis = TimeUnit.MINUTES.toMillis(eventCooldownMinutes);
		final long readyAt = lastEventFinishedAtMillis + cooldownMillis;
		if (System.currentTimeMillis() >= readyAt) {
			return true;
		}
		final long remainingMinutes = Math.max(1L, TimeUnit.MILLISECONDS.toMinutes(readyAt - System.currentTimeMillis()));
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Szlak po ostatniej walce jeszcze niegotów. Następna eskorta za około " + remainingMinutes + " min.");
		return false;
	}

	private boolean shouldValidateStartGates() {
		return !isScriptForceStartRequested();
	}

	private boolean validateStartGates() {
		if (!isCooldownReady()) {
			failedByGiantHealth = true;
			blockedByCooldown = true;
			return false;
		}

		if (countPlayersInObserverZones() < MIN_PLAYERS_TO_START) {
			SingletonRepository.getRuleProcessor().tellAllPlayers(
					NotificationType.PRIVMSG,
					"Za mało ludzi na szlaku. Do eskorty potrzeba co najmniej " + MIN_PLAYERS_TO_START + " obrońców.");
			failedByLowActivity = true;
			return false;
		}

		return true;
	}

	private int countPlayersInObserverZones() {
		int count = 0;
		for (final String zoneName : getObserverZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone != null) {
				count += zone.getPlayers().size();
			}
		}
		return count;
	}

	private List<Player> playersInObserverZones() {
		final List<Player> players = new ArrayList<>();
		for (final String zoneName : getObserverZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone != null) {
				players.addAll(zone.getPlayers());
			}
		}
		return players;
	}

	private Integer findWaveOffset(final EventSpawn eventSpawn) {
		int cumulative = 0;
		for (final EventWave wave : getWaves()) {
			cumulative += wave.getIntervalSeconds();
			for (final EventSpawn spawn : wave.getSpawns()) {
				if (spawn == eventSpawn) {
					return cumulative;
				}
			}
		}
		return null;
	}

	private Player asOnlinePlayer(final Entity entity) {
		if (!(entity instanceof Player)) {
			return null;
		}
		Player player = (Player) entity;
		if (player.isDisconnected()) {
			player = SingletonRepository.getRuleProcessor().getPlayer(player.getName());
		}
		return player;
	}

	@Override
	protected List<String> getActivityTop() {
		final List<Map.Entry<String, MapEventContributionTracker.ContributionSnapshot>> entries =
				new ArrayList<Map.Entry<String, MapEventContributionTracker.ContributionSnapshot>>(contributionTracker.snapshotAll().entrySet());
		Collections.sort(entries, new Comparator<Map.Entry<String, MapEventContributionTracker.ContributionSnapshot>>() {
			@Override
			public int compare(final Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> first,
					final Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> second) {
				return Integer.compare(
						MapEventContributionTracker.resolveActivityPoints(second.getValue()),
						MapEventContributionTracker.resolveActivityPoints(first.getValue()));
			}
		});
		final List<String> top = new ArrayList<String>();
		for (Map.Entry<String, MapEventContributionTracker.ContributionSnapshot> entry : entries) {
			if (top.size() >= 10) {
				break;
			}
			top.add(entry.getKey() + "::" + MapEventContributionTracker.resolveActivityPoints(entry.getValue()));
		}
		return top;
	}

	private void rewardParticipants() {
		if (finalHealthRatio < 0.2d) {
			return;
		}

		final long now = System.currentTimeMillis();
		final Map<String, MapEventContributionTracker.ContributionSnapshot> contributions = contributionTracker.snapshotAll();
		final List<EventActivityChestRewardService.QualifiedParticipant> qualifiedParticipants = new ArrayList<>();
		for (Entry<String, MapEventContributionTracker.ContributionSnapshot> entry : contributions.entrySet()) {
			final String playerName = entry.getKey();
			final Player player = SingletonRepository.getRuleProcessor().getPlayer(playerName);
			if (player == null) {
				continue;
			}

			final MapEventContributionTracker.ContributionSnapshot contribution = entry.getValue();
			final MapEventRewardPolicy.RewardDecision decision = rewardPolicy.evaluate(
					getEventId(),
					playerName,
					contribution,
					now);
			final String audit = getEventName() + " reward audit: player=" + playerName
					+ ", dmg=" + contribution.getDamage()
					+ ", assists=" + contribution.getKillAssists()
					+ ", obj=" + contribution.getObjectiveActions()
					+ ", zoneSec=" + contribution.getTimeInZoneSeconds()
					+ ", score=" + Math.round(decision.getTotalScore() * 100.0d) / 100.0d
					+ ", score/window=" + Math.round(decision.getScorePerWindow() * 100.0d) / 100.0d
					+ ", antiAfk=" + decision.isAntiAfkPassed()
					+ ", qualified=" + decision.isQualified()
					+ ", recentRuns=" + decision.getRecentRuns()
					+ ", multiplier=" + Math.round(decision.getMultiplier() * 100.0d) / 100.0d + ".";
			LOGGER.info(audit);
			notifyAdminsDebug("[Kościelisko][audit] " + audit);

			if (!decision.isQualified()) {
				continue;
			}

			final RandomEventRewardService.Reward reward = randomEventRewardService.grantRandomEventRewards(
					player,
					RandomEventRewardService.RandomEventType.GIANT_ESCORT,
					finalHealthRatio,
					decision.getMultiplier());
			final int xpReward = reward.getXp();
			final double karmaReward = reward.getKarma();
			player.sendPrivateText("Za obronę szlaku otrzymujesz +" + xpReward + " PD oraz +"
					+ Math.round(karmaReward * 100.0d) / 100.0d + " karmy.");
			player.sendPrivateText("Podsumowanie eventu: punkty=" + Math.max(0, (int) Math.round(decision.getTotalScore()))
					+ ", limit dzienny=" + (decision.isDailyLimitReached() ? "TAK (redukcja nagrody)" : "NIE") + ".");
			qualifiedParticipants.add(new EventActivityChestRewardService.QualifiedParticipant(
					player,
					decision.getTotalScore(),
					contribution.getDamage(),
					contribution.getKillAssists()));
		}

		EventActivityChestRewardService.awardTopActivityChests("Eskorta Wielkoluda", qualifiedParticipants);
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

	private int clampEntityHpToShort(final int configuredHp, final String sourceName) {
		if (configuredHp > MAX_ENTITY_HP_SHORT) {
			LOGGER.warn(getEventName() + " received out-of-range HP from " + sourceName + "=" + configuredHp
					+ "; clamping to " + MAX_ENTITY_HP_SHORT + " (SHORT max).");
			return MAX_ENTITY_HP_SHORT;
		}
		return configuredHp;
	}

	private static final class GiantSnapshot {
		private final int hp;
		private final int baseHp;
		private final int atk;
		private final int ratk;
		private final int def;
		private final int resistance;

		private GiantSnapshot(final int hp, final int baseHp, final int atk, final int ratk, final int def, final int resistance) {
			this.hp = hp;
			this.baseHp = baseHp;
			this.atk = atk;
			this.ratk = ratk;
			this.def = def;
			this.resistance = resistance;
		}

		private static GiantSnapshot capture(final SpeakerNPC giant) {
			return new GiantSnapshot(
					giant.getHP(),
					giant.getBaseHP(),
					giant.getAtk(),
					giant.getRatk(),
					giant.getDef(),
					giant.getResistance());
		}

		private void restore(final SpeakerNPC giant) {
			giant.setAtk(atk);
			giant.setRatk(ratk);
			giant.setDef(def);
			giant.setResistance(resistance);
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

	private static final class SpawnStats {
		private final int nearGiant;
		private final int fallback;

		private SpawnStats(final int nearGiant, final int fallback) {
			this.nearGiant = nearGiant;
			this.fallback = fallback;
		}
	}

	private static final class PlayerSnapshot {
		private final int x;
		private final int y;
		private final long recordedAt;

		private PlayerSnapshot(final int x, final int y, final long recordedAt) {
			this.x = x;
			this.y = y;
			this.recordedAt = recordedAt;
		}

		private boolean hasMovedTo(final PlayerSnapshot other) {
			return other != null && (x != other.x || y != other.y) && other.recordedAt > recordedAt;
		}
	}
}
