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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.pathfinder.Path;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;

public final class KoscieliskoGiantEscortEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(KoscieliskoGiantEscortEvent.class);
	private static final String GIANT_NPC_NAME = "Wielkolud";
	private static final int MAX_ENTITY_HP_SHORT = 32767;
	private static final int GIANT_EVENT_HP = 32000;
	private static final int GIANT_EVENT_DEF_BONUS = 220;
	private static final int GIANT_EVENT_RESISTANCE = 40;
	private static final int GIANT_FAIL_HP_THRESHOLD = 100;
	private static final int GIANT_CRITICAL_HP_THRESHOLD = 10000;
	private static final int GIANT_HEAL_CAP_PER_SECOND = 0;
	private static final int HEALTH_CHECK_INTERVAL_SECONDS = 1;
	private static final int AGGRO_TICK_INTERVAL_SECONDS = 1;
	private static final int ACTIVITY_SAMPLE_INTERVAL_SECONDS = 10;
	private static final int EVENT_COOLDOWN_MINUTES = 45;
	private static final int MIN_PLAYERS_TO_START = 3;
	private static final int MIN_ACTIVITY_TICKS_FOR_REWARD = 6;
	private static final int MAX_LOW_ACTIVITY_TICKS = 12;
	private static final int GIANT_TARGET_RANGE = 18;
	private static final int PLAYER_FALLBACK_RANGE = 14;
	private static final int LEASH_RANGE = 24;
	private static final int MAX_GIANT_PATH_LENGTH = 96;
	private static final int ESCORT_RING_MIN_RADIUS = 4;
	private static final int ESCORT_RING_MAX_RADIUS = 10;
	private static final int ESCORT_SPAWN_ATTEMPTS = 30;
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
	private final Map<Integer, SpawnAnchor> creatureAnchors = new HashMap<>();
	private final Set<Integer> firstTargetLogged = new HashSet<>();
	private final Map<String, PlayerSnapshot> playerSnapshots = new HashMap<>();
	private final Map<String, Integer> playerActivityTicks = new HashMap<>();
	private final Set<String> rewardEligiblePlayers = new HashSet<>();
	private final Set<Integer> announcedWaveOffsets = new HashSet<>();
	private final RandomSafeSpotSpawnStrategy fallbackSpawnStrategy = new RandomSafeSpotSpawnStrategy(LOGGER);

	public KoscieliskoGiantEscortEvent() {
		super(LOGGER, MapEventConfigLoader.load(MapEventConfigLoader.KOSCIELISKO_GIANT_ESCORT));
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
		snapshot = null;
		eventStartedAtMillis = 0L;
		giantEventHp = GIANT_EVENT_HP;
		giantHpBeforeTick = giantEventHp;
		lowActivityTicks = 0;
		creatureAnchors.clear();
		firstTargetLogged.clear();
		playerSnapshots.clear();
		playerActivityTicks.clear();
		rewardEligiblePlayers.clear();
		announcedWaveOffsets.clear();

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
		giantEventHp = clampEntityHpToShort(GIANT_EVENT_HP, "GIANT_EVENT_HP");
		LOGGER.info(getEventName() + " escort start: giant resistance before buff=" + snapshot.resistance
				+ ", event resistance=" + GIANT_EVENT_RESISTANCE + ".");
		giantNpc.setBaseHP(giantEventHp);
		giantNpc.setHP(giantEventHp);
		giantNpc.setResistance(GIANT_EVENT_RESISTANCE);
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
			firstTargetLogged.clear();
			playerSnapshots.clear();
			playerActivityTicks.clear();
			rewardEligiblePlayers.clear();
			announcedWaveOffsets.clear();
			lastEventFinishedAtMillis = System.currentTimeMillis();

			super.onStop();
		}
	}

	@Override
	protected String getStopAnnouncementMessage() {
		if (escortSuccess) {
			return "Eskorta zakończona sukcesem! Wielkolud przetrwał napór wrogów (HP: "
					+ Math.round(finalHealthRatio * 100.0d) + "%).";
		}
		if (blockedByCooldown) {
			return "Eskorta Wielkoluda nie wystartowała - wydarzenie jest jeszcze na cooldownie.";
		}
		if (failedByLowActivity) {
			return "Eskorta zakończona porażką - zbyt niska aktywność graczy.";
		}
		return "Eskorta zakończona porażką - Wielkolud został ciężko ranny.";
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		final Integer waveOffset = findWaveOffset(spawn);
		if (waveOffset == null || !announcedWaveOffsets.add(waveOffset)) {
			return;
		}
		final int durationSeconds = (int) getEventDuration().getSeconds();
		final int progress = durationSeconds <= 0 ? 0 : (int) Math.round((waveOffset * 100.0d) / durationSeconds);
		announceProgressStatus("FALA", Math.min(progress, 100));
	}

	@Override
	protected void spawnCreatures(final String creatureName, final int count) {
		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		giantNpc = currentGiant;

		int nearGiantSpawns = 0;
		int fallbackSpawns = 0;

		for (final String zoneName : getZones()) {
			final int requestedCount = count;
			final double spawnMultiplier = getConfig().getZoneSpawnMultiplier(zoneName);
			final int multipliedCount = (int) Math.round(requestedCount * spawnMultiplier);
			final Integer zoneSpawnCap = getConfig().getZoneSpawnCap(zoneName);
			final int finalSpawnCount = zoneSpawnCap == null ? multipliedCount : Math.min(multipliedCount, zoneSpawnCap);

			if (finalSpawnCount <= 0) {
				continue;
			}

			if (currentGiant != null && currentGiant.getZone() != null
					&& currentGiant.getZone().getName().equals(zoneName)) {
				final SpawnStats stats = spawnAroundGiantInZone(currentGiant, creatureName, finalSpawnCount);
				nearGiantSpawns += stats.nearGiant;
				fallbackSpawns += stats.fallback;
				continue;
			}

			LOGGER.warn(getEventName() + " cannot resolve giant NPC in zone " + zoneName
					+ "; falling back to RandomSafeSpotSpawnStrategy for " + creatureName + ".");
			fallbackSpawns += spawnWithFallback(zoneName, creatureName, finalSpawnCount);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(getEventName() + " spawn summary for '" + creatureName + "': near giant=" + nearGiantSpawns
					+ ", fallback=" + fallbackSpawns + ".");
		}
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

			if (getConfig().isGiantOnlyAggro()) {
				resetCreatureTarget(creature);
				continue;
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

			final PlayerSnapshot previous = playerSnapshots.get(player.getName());
			final PlayerSnapshot current = new PlayerSnapshot(player.getX(), player.getY(), System.currentTimeMillis());
			playerSnapshots.put(player.getName(), current);

			if (previous != null && previous.hasMovedTo(current)) {
				movedPlayers++;
				final int ticks = playerActivityTicks.getOrDefault(player.getName(), 0) + 1;
				playerActivityTicks.put(player.getName(), ticks);
				if (ticks >= MIN_ACTIVITY_TICKS_FOR_REWARD) {
					rewardEligiblePlayers.add(player.getName());
				}
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
			criticalHealthAnnounced = true;
			final int hpPercent = Math.max(0, Math.round((currentGiant.getHP() * 100.0f) / giantEventHp));
			SingletonRepository.getRuleProcessor().tellAllPlayers(
					NotificationType.PRIVMSG,
					"[Eskorta Wielkoluda] Krytyczne HP! Wielkolud ma tylko " + hpPercent + "% życia.");
		}
	}

	private void applyEscortSurvivabilityBuff(final SpeakerNPC giant, final GiantSnapshot baseline) {
		if (giant == null || baseline == null) {
			return;
		}

		giant.setDef(baseline.def + GIANT_EVENT_DEF_BONUS);
	}

	private void announceProgressStatus(final String stage, final int progressPercent) {
		final SpeakerNPC currentGiant = giantNpc;
		final int hpPercent = currentGiant == null ? 0 : Math.max(0, Math.round((currentGiant.getHP() * 100.0f) / giantEventHp));
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"[Eskorta Wielkoluda] Etap: " + stage + " | Postęp: " + progressPercent + "% | HP Wielkoluda: " + hpPercent + "%.");
	}

	private boolean isCooldownReady() {
		if (lastEventFinishedAtMillis <= 0L) {
			return true;
		}
		final long cooldownMillis = TimeUnit.MINUTES.toMillis(EVENT_COOLDOWN_MINUTES);
		final long readyAt = lastEventFinishedAtMillis + cooldownMillis;
		if (System.currentTimeMillis() >= readyAt) {
			return true;
		}
		final long remainingMinutes = Math.max(1L, TimeUnit.MILLISECONDS.toMinutes(readyAt - System.currentTimeMillis()));
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"Eskorta Wielkoluda jest na cooldownie. Kolejny start za około " + remainingMinutes + " min.");
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
					"Eskorta Wielkoluda nie wystartowała: potrzeba co najmniej " + MIN_PLAYERS_TO_START
							+ " aktywnych graczy w regionie.");
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

	private void rewardParticipants() {
		final RewardTier tier = RewardTier.fromHealthRatio(finalHealthRatio);
		if (tier == RewardTier.NONE) {
			return;
		}

		int rewardedPlayers = 0;
		for (final Player player : playersInObserverZones()) {
			if (player == null || !rewardEligiblePlayers.contains(player.getName())) {
				continue;
			}
			player.addXP(tier.xpReward);
			player.addKarma(tier.karmaReward);
			player.sendPrivateText("[Eskorta Wielkoluda] Nagroda za aktywną obronę: +" + tier.xpReward
					+ " XP, +" + tier.karmaReward + " karmy. (Stan HP: " + Math.round(finalHealthRatio * 100.0d) + "%)");
			rewardedPlayers++;
		}

		if (rewardedPlayers == 0) {
			SingletonRepository.getRuleProcessor().tellAllPlayers(
					NotificationType.PRIVMSG,
					"Eskorta zakończona, ale brak aktywnych uczestników spełniających próg nagrody.");
		}
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

	private enum RewardTier {
		HIGH(0.8d, 220000, 20.0d),
		MEDIUM(0.5d, 140000, 12.0d),
		LOW(0.2d, 80000, 7.0d),
		NONE(0.0d, 0, 0.0d);

		private final double threshold;
		private final int xpReward;
		private final double karmaReward;

		RewardTier(final double threshold, final int xpReward, final double karmaReward) {
			this.threshold = threshold;
			this.xpReward = xpReward;
			this.karmaReward = karmaReward;
		}

		private static RewardTier fromHealthRatio(final double healthRatio) {
			if (healthRatio >= HIGH.threshold) {
				return HIGH;
			}
			if (healthRatio >= MEDIUM.threshold) {
				return MEDIUM;
			}
			if (healthRatio >= LOW.threshold) {
				return LOW;
			}
			return NONE;
		}
	}
}
