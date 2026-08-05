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

import java.time.Instant;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.creature.CircumstancesOfDeath;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.ScreenAnnouncementBroadcaster;

public class ConfiguredMapEvent extends BaseMapEvent {
	private static final int CAPTURE_WAVE_SPAWN_RING_PADDING_TILES = 6;

	private final Logger logger;
	private final MapEventSpawnStrategy spawnStrategy;
	private final KillThresholdTrigger killThresholdTrigger;
	private final CaptureProgressTrigger captureProgressTrigger;
	private final MapEventConfig.ScalingConfig scalingConfig;
	private final Map<BaseMapEvent.EventSpawn, Integer> spawnWaveIndexes;
	private final List<Integer> waveBaseTotals;
	private final Map<Integer, WaveScaleState> waveScaleStates = new HashMap<>();
	private final Map<Creature, Integer> creatureWaveIndexes = new HashMap<>();
	private final List<Double> completedWaveClearTimesSec = new ArrayList<>();
	private final List<CapturePointState> capturePoints = new ArrayList<>();
	private final Map<String, Integer> captureSecondsByPlayer = new HashMap<>();
	private final Map<String, Integer> captureSpawnKillsByPlayer = new HashMap<>();
	private final Map<Creature, Boolean> captureSpawnCreatures = new IdentityHashMap<>();
	private final MapEventContributionTracker contributionTracker = new MapEventContributionTracker();
	private final MapEventRewardPolicy rewardPolicy = MapEventRewardPolicy.defaultEscortPolicy();
	private final RandomEventRewardService randomEventRewardService = new RandomEventRewardService();
	private final List<MapEventConfig.PhaseConfig> phaseConfigs;
	private final List<MapEventConfig.ModifierConfig> modifierConfigs;
	private final List<MapEventConfig.SecondaryObjectiveConfig> secondaryObjectiveConfigs;
	private final List<MapEventConfig.ModifierConfig> selectedModifiers = new ArrayList<>();
	private final List<MapEventConfig.ModifierConfig> activeModifiers = new ArrayList<>();
	private final Map<String, ObjectiveState> objectivesById = new LinkedHashMap<>();
	private final Set<String> activatedModifierIds = new HashSet<>();
	private volatile MapEventConfig.PhaseConfig activePhase;
	private volatile String phaseDescription;
	private volatile boolean scriptForceStartRequested;
	private volatile int activeSpawningWaveIndex = -1;

	public ConfiguredMapEvent(final Logger logger, final MapEventConfig config) {
		this(logger, config, new RandomSafeSpotSpawnStrategy(logger));
	}

	public ConfiguredMapEvent(final Logger logger, final MapEventConfig config,
			final MapEventSpawnStrategy spawnStrategy) {
		super(logger, config);
		this.logger = Objects.requireNonNull(logger, "logger");
		this.spawnStrategy = Objects.requireNonNull(spawnStrategy, "spawnStrategy");
		scalingConfig = getConfig().getScaling();
		captureProgressTrigger = new CaptureProgressTrigger(getConfig().getCaptureProgressWaves());
		phaseConfigs = new ArrayList<>(getConfig().getPhases());
		modifierConfigs = new ArrayList<>(getConfig().getModifiers());
		secondaryObjectiveConfigs = new ArrayList<>(getConfig().getSecondaryObjectives());
		spawnWaveIndexes = createSpawnWaveIndexes(getConfig().getWaves());
		waveBaseTotals = createWaveBaseTotals(getConfig().getWaves());
		if (getConfig().getTriggerThreshold() > 0) {
			killThresholdTrigger = new KillThresholdTrigger(
					getObserverZones(),
					circs -> !isEventActive() && getCreatureFilter().contains(circs.getVictim().getName()),
					getConfig().getTriggerThreshold(),
					this::startEventFromKills);
		} else {
			killThresholdTrigger = null;
			logger.info(getEventName() + " event configured without kill-trigger (triggerThreshold="
					+ getConfig().getTriggerThreshold() + ").");
		}
	}

	public final void registerObserverZone(final StendhalRPZone zone) {
		if (killThresholdTrigger == null) {
			return;
		}
		killThresholdTrigger.registerZoneObserver(Objects.requireNonNull(zone, "zone"));
	}

	public final void scheduleGuaranteedStart(final LocalTime time, final int intervalDays) {
		scheduleEveryDaysAt(time, intervalDays);
	}

	public final boolean forceStartEvent() {
		return startFromScript(true);
	}

	public final String getEventDisplayName() {
		return getEventName();
	}

	public final boolean startFromScript(final boolean force) {
		scriptForceStartRequested = force;
		try {
			if (!startEvent()) {
				logger.warn(getEventName() + " event already active; refusing "
						+ (force ? "forced" : "safe") + " script start.");
				return false;
			}
			return true;
		} finally {
			scriptForceStartRequested = false;
		}
	}

	protected final boolean isScriptForceStartRequested() {
		return scriptForceStartRequested;
	}

	protected String getStartAnnouncementMessage() {
		final String configuredMessage = getConfig().getStartAnnouncement();
		if (configuredMessage != null) {
			return configuredMessage;
		}
		return "Rozpoczyna się wydarzenie: " + getEventName() + ".";
	}

	protected String getStopAnnouncementMessage() {
		final String configuredMessage = getConfig().getStopAnnouncement();
		if (configuredMessage != null) {
			return configuredMessage;
		}
		return "Wydarzenie " + getEventName() + " dobiegło końca.";
	}

	@Override
	protected void onStart() {
		contributionTracker.clear();
		if (killThresholdTrigger != null) {
			killThresholdTrigger.resetCounter("event started");
		}
		if (captureProgressTrigger.isEnabled()) {
			captureProgressTrigger.reset("event started");
		}
		waveScaleStates.clear();
		completedWaveClearTimesSec.clear();
		initializeCapturePoints();
		initializePhasesAndModifiers();
		initializeSecondaryObjectives();
		captureSecondsByPlayer.clear();
		captureSpawnKillsByPlayer.clear();
		captureSpawnCreatures.clear();
		synchronized (creatureWaveIndexes) {
			creatureWaveIndexes.clear();
		}
		logger.info(getEventName() + " event started.");
		ScreenAnnouncementBroadcaster.broadcastToAllPlayers(
				getEventName(),
				getStartAnnouncementMessage(),
				ScreenAnnouncementBroadcaster.CATEGORY_EVENT);
		activatePhaseForWave(0, true);
		activateModifiersForWave(0);
		activateObjectivesForWave(0);
	}

	@Override
	protected void onStop() {
		final int defeatPercent = getEventDefeatPercent();
		if (killThresholdTrigger != null) {
			killThresholdTrigger.resetCounter("event ended");
		}
		if (captureProgressTrigger.isEnabled()) {
			captureProgressTrigger.reset("event ended");
		}
		logger.info(getEventName() + " event ended.");
		failExpiredObjectives(Integer.MAX_VALUE);
		rewardParticipants(defeatPercent);
		waveScaleStates.clear();
		completedWaveClearTimesSec.clear();
		activePhase = null;
		phaseDescription = null;
		selectedModifiers.clear();
		activeModifiers.clear();
		activatedModifierIds.clear();
		objectivesById.clear();
		capturePoints.clear();
		captureSecondsByPlayer.clear();
		captureSpawnKillsByPlayer.clear();
		captureSpawnCreatures.clear();
		contributionTracker.clear();
		synchronized (creatureWaveIndexes) {
			creatureWaveIndexes.clear();
		}
		removeEventCreatures();
		stopAnnouncements();
		ScreenAnnouncementBroadcaster.broadcastToPlayersInZones(
				getEventName(),
				getStopAnnouncementMessage(),
				ScreenAnnouncementBroadcaster.CATEGORY_EVENT,
				getZones());
	}

	@Override
	protected void spawnCreaturesForWave(final EventSpawn spawn) {
		final int waveIndex = spawnWaveIndexes.containsKey(spawn) ? spawnWaveIndexes.get(spawn) : -1;
		if (waveIndex < 0 || scalingConfig == null) {
			super.spawnCreaturesForWave(spawn);
			return;
		}

		final WaveScaleState waveState = waveScaleStates.computeIfAbsent(waveIndex, this::createWaveScaleState);
		final int baseTotal = Math.max(1, waveState.baseWaveTotal);
		final int nextBaseSoFar = waveState.baseAssigned + spawn.getCount();
		final int targetUntilCurrentSpawn = (int) Math.round((nextBaseSoFar / (double) baseTotal) * waveState.scaledWaveTarget);
		final int scaledSpawnCount = Math.max(0, targetUntilCurrentSpawn - waveState.scaledAssigned);
		waveState.baseAssigned = nextBaseSoFar;
		waveState.scaledAssigned += scaledSpawnCount;
		waveState.pendingCreatures += scaledSpawnCount;

		logger.debug(getEventName() + " wave " + (waveIndex + 1)
				+ " spawn scaling for creature " + spawn.getCreatureName()
				+ ": base=" + spawn.getCount()
				+ ", scaled=" + scaledSpawnCount
				+ ", totalBase=" + waveState.baseWaveTotal
				+ ", totalScaled=" + waveState.scaledWaveTarget + ".");

		activeSpawningWaveIndex = waveIndex;
		try {
			spawnCreatures(spawn.getCreatureName(), scaledSpawnCount);
		} finally {
			activeSpawningWaveIndex = -1;
		}
	}

	@Override
	protected void spawnCreatures(final String creatureName, final int count) {
		final int adjustedRequestedCount = applyActiveSpawnMultiplier(count);
		for (final String zoneName : getZones()) {
			final int requestedCount = adjustedRequestedCount;
			final double spawnMultiplier = resolveEffectiveZoneSpawnMultiplier(zoneName);
			final int multipliedCount = (int) Math.round(requestedCount * spawnMultiplier);
			final Integer zoneSpawnCap = getConfig().getZoneSpawnCap(zoneName);
			final int finalSpawnCount = zoneSpawnCap == null ? multipliedCount : Math.min(multipliedCount, zoneSpawnCap);

			logger.debug(getEventName() + " spawn request for zone " + zoneName + ": requested=" + requestedCount
					+ ", multiplier=" + spawnMultiplier + ", multiplied=" + multipliedCount
					+ (zoneSpawnCap == null ? "" : ", cap=" + zoneSpawnCap)
					+ ", final=" + finalSpawnCount + ".");

			if (finalSpawnCount <= 0) {
				logger.debug(getEventName() + " spawn skipped in zone " + zoneName
						+ "; final spawn count is 0 after multiplier/cap.");
				continue;
			}

			final int spawningWaveIndex = activeSpawningWaveIndex;
			spawnStrategy.spawnCreatures(
					getEventName(),
					Collections.singletonList(zoneName),
					creatureName,
					finalSpawnCount,
					creature -> {
						registerEventCreature(creature);
						if (spawningWaveIndex >= 0) {
							synchronized (creatureWaveIndexes) {
								creatureWaveIndexes.put(creature, spawningWaveIndex);
							}
						}
					});
		}
	}

	@Override
	protected void onWaveStarted(final int currentWaveNumber, final EventWave wave) {
		activatePhaseForWave(currentWaveNumber, false);
		activateModifiersForWave(currentWaveNumber);
		activateObjectivesForWave(currentWaveNumber);
		failExpiredObjectives(currentWaveNumber);
	}

	@Override
	protected void onEventCreatureDeath(final CircumstancesOfDeath circs) {
		if (circs != null && circs.getVictim() instanceof Creature) {
			final Creature victim = (Creature) circs.getVictim();
			if (captureSpawnCreatures.remove(victim) != null && circs.getKiller() instanceof Player) {
				final String killerName = ((Player) circs.getKiller()).getName();
				captureSpawnKillsByPlayer.merge(killerName, 1, Integer::sum);
			}
			progressKillObjectives(victim);
		}
		if (circs != null && circs.getKiller() instanceof Player) {
			contributionTracker.recordKillCount(((Player) circs.getKiller()).getName(), 1);
		}

		if (scalingConfig == null || circs == null || circs.getVictim() == null) {
			return;
		}
		final Integer waveIndex;
		synchronized (creatureWaveIndexes) {
			waveIndex = creatureWaveIndexes.remove(circs.getVictim());
		}
		if (waveIndex == null) {
			return;
		}
		final WaveScaleState waveState = waveScaleStates.get(waveIndex);
		if (waveState == null || waveState.pendingCreatures <= 0) {
			return;
		}
		waveState.pendingCreatures--;
		if (waveState.pendingCreatures == 0) {
			final double clearTimeSec = Math.max(1d, waveState.startedAt.until(currentInstant(), java.time.temporal.ChronoUnit.MILLIS) / 1000d);
			completedWaveClearTimesSec.add(clearTimeSec);
			logger.debug(getEventName() + " wave " + (waveIndex + 1) + " cleared in " + clearTimeSec + "s.");
		}
	}

	protected Instant currentInstant() {
		return Instant.now();
	}

	protected int countPlayersInZones() {
		if (scalingConfig != null) {
			return countActivePlayersInZones(
					scalingConfig.getOnlineZoneMinPlayerLevel(),
					scalingConfig.getOnlineZoneMaxPlayerLevel());
		}
		return countActivePlayersInZones(0, Integer.MAX_VALUE);
	}

	protected int countActivePlayersInZones(final int minLevelInclusive, final int maxLevelInclusive) {
		final int minLevel = Math.max(0, minLevelInclusive);
		final int maxLevel = Math.max(minLevel, maxLevelInclusive);
		int players = 0;
		for (final String zoneName : getZones()) {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				continue;
			}
			for (final Player player : zone.getPlayers()) {
				if (player == null || player.isGhost() || player.isDisconnected()) {
					continue;
				}
				final int level = player.getLevel();
				if (level < minLevel || level > maxLevel) {
					continue;
				}
				players++;
			}
		}
		return players;
	}

	@Override
	protected void onStatusTick() {
		if (!capturePoints.isEmpty()) {
			for (CapturePointState capturePoint : capturePoints) {
				final List<String> activePlayerNamesNearPoint = getActivePlayerNamesAroundPoint(capturePoint,
						scalingConfig != null ? scalingConfig.getOnlineZoneMinPlayerLevel() : 0,
						scalingConfig != null ? scalingConfig.getOnlineZoneMaxPlayerLevel() : Integer.MAX_VALUE);
				final int playersNearPoint = activePlayerNamesNearPoint.size();
				if (!capturePoint.isCompleted() && playersNearPoint > 0) {
					for (String playerName : activePlayerNamesNearPoint) {
						captureSecondsByPlayer.merge(playerName, 1, Integer::sum);
						contributionTracker.recordTimeInZone(playerName, 1);
						if ((captureSecondsByPlayer.get(playerName).intValue() % 10) == 0) {
							contributionTracker.recordObjectiveAction(playerName, 1);
						}
					}
				}
				capturePoint.tick(playersNearPoint, getCurrentWave());
				captureProgressTrigger.evaluate(capturePoint, this::spawnCaptureProgressWave);
			}
		}
		updateCaptureObjectives();
		failExpiredObjectives(getCurrentWave());
	}

	@Override
	protected List<String> getActivityTop() {
		return MapEventRewardSettlementService.buildActivityTop(contributionTracker);
	}

	@Override
	protected String resolveDefenseStatus() {
		if (activePhase != null && activePhase.getDefenseStatus() != null
				&& !activePhase.getDefenseStatus().trim().isEmpty()) {
			return activePhase.getDefenseStatus();
		}
		return super.resolveDefenseStatus();
	}

	@Override
	protected String getPhaseName() {
		return activePhase == null ? null : activePhase.getTitle();
	}

	@Override
	protected String getPhaseDescription() {
		return phaseDescription;
	}

	@Override
	protected String getModifierName() {
		if (activeModifiers.isEmpty()) {
			return null;
		}
		return activeModifiers.stream().map(MapEventConfig.ModifierConfig::getTitle)
				.collect(Collectors.joining(", "));
	}

	@Override
	protected String getModifierDescription() {
		if (activeModifiers.isEmpty()) {
			return null;
		}
		return activeModifiers.stream()
				.map(MapEventConfig.ModifierConfig::getDescription)
				.filter(value -> value != null && !value.trim().isEmpty())
				.collect(Collectors.joining(" | "));
	}

	@Override
	protected String getSecondaryObjectivesStatusPayload() {
		if (objectivesById.isEmpty()) {
			return null;
		}
		final StringBuilder payload = new StringBuilder();
		payload.append('[');
		boolean appendedAny = false;
		for (ObjectiveState objectiveState : objectivesById.values()) {
			if (!objectiveState.isVisible()) {
				continue;
			}
			if (appendedAny) {
				payload.append(',');
			}
			payload.append(objectiveState.toStatusPayload());
			appendedAny = true;
		}
		payload.append(']');
		return appendedAny ? payload.toString() : null;
	}

	@Override
	protected Integer getRewardBonusPercent() {
		return Integer.valueOf((int) Math.round(resolveRewardMultiplierBonus() * 100.0d));
	}

	private void initializePhasesAndModifiers() {
		activePhase = null;
		phaseDescription = null;
		selectedModifiers.clear();
		activeModifiers.clear();
		activatedModifierIds.clear();
		if (!phaseConfigs.isEmpty()) {
			phaseConfigs.sort((left, right) -> Integer.compare(left.getStartWave(), right.getStartWave()));
		}
		if (modifierConfigs.isEmpty() || getConfig().getMaxActiveModifiers() <= 0) {
			return;
		}
		final List<MapEventConfig.ModifierConfig> pool = new ArrayList<>(modifierConfigs);
		Collections.shuffle(pool, ThreadLocalRandom.current());
		final int maxCount = Math.min(pool.size(), getConfig().getMaxActiveModifiers());
		final int minCount = Math.min(maxCount, Math.max(0, getConfig().getMinActiveModifiers()));
		final int selectedCount = maxCount <= minCount ? minCount
				: ThreadLocalRandom.current().nextInt(minCount, maxCount + 1);
		for (int i = 0; i < selectedCount; i++) {
			selectedModifiers.add(pool.get(i));
		}
		selectedModifiers.sort((left, right) -> Integer.compare(left.getActivationWave(), right.getActivationWave()));
	}

	private void initializeSecondaryObjectives() {
		objectivesById.clear();
		for (MapEventConfig.SecondaryObjectiveConfig objectiveConfig : secondaryObjectiveConfigs) {
			objectivesById.put(objectiveConfig.getObjectiveId(), new ObjectiveState(objectiveConfig));
		}
	}

	private void activatePhaseForWave(final int waveNumber, final boolean forceAnnouncement) {
		MapEventConfig.PhaseConfig nextPhase = null;
		for (MapEventConfig.PhaseConfig candidate : phaseConfigs) {
			if (candidate.getStartWave() <= waveNumber) {
				nextPhase = candidate;
			}
		}
		if (nextPhase == null || nextPhase == activePhase) {
			return;
		}
		activePhase = nextPhase;
		phaseDescription = nextPhase.getDescription();
		if (forceAnnouncement || nextPhase.getStartWave() > 0) {
			announceEventChange(nextPhase.getTitle(),
					nextPhase.getTransitionAnnouncement() != null ? nextPhase.getTransitionAnnouncement()
							: nextPhase.getDescription());
		}
	}

	private void activateModifiersForWave(final int waveNumber) {
		for (MapEventConfig.ModifierConfig modifier : selectedModifiers) {
			if (modifier.getActivationWave() != waveNumber || !activatedModifierIds.add(modifier.getModifierId())) {
				continue;
			}
			activeModifiers.add(modifier);
			if (modifier.getActivationAnnouncement() != null && !modifier.getActivationAnnouncement().trim().isEmpty()) {
				announceEventChange(modifier.getTitle(), modifier.getActivationAnnouncement());
			}
			for (EventSpawn extraSpawn : modifier.getExtraSpawns()) {
				spawnCreatures(extraSpawn.getCreatureName(), extraSpawn.getCount());
			}
		}
	}

	private void activateObjectivesForWave(final int waveNumber) {
		for (ObjectiveState objectiveState : objectivesById.values()) {
			if (objectiveState.shouldActivate(waveNumber)) {
				objectiveState.activate();
			}
		}
	}

	private void failExpiredObjectives(final int currentWave) {
		for (ObjectiveState objectiveState : objectivesById.values()) {
			if (!objectiveState.shouldFail(currentWave)) {
				continue;
			}
			objectiveState.fail();
			spawnObjectiveOutcomeCreatures(objectiveState.getConfig().getFailureExtraSpawns());
			if (objectiveState.getConfig().getFailureAnnouncement() != null) {
				announceEventChange(objectiveState.getConfig().getTitle(),
						objectiveState.getConfig().getFailureAnnouncement());
			}
		}
	}

	private void progressKillObjectives(final Creature victim) {
		if (victim == null) {
			return;
		}
		for (ObjectiveState objectiveState : objectivesById.values()) {
			if (!objectiveState.isActiveKillObjectiveFor(victim.getName())) {
				continue;
			}
			objectiveState.incrementProgress(1);
			if (objectiveState.completeIfReady()) {
				handleCompletedObjective(objectiveState);
			}
		}
	}

	private void updateCaptureObjectives() {
		for (ObjectiveState objectiveState : objectivesById.values()) {
			if (!objectiveState.isActiveCaptureObjective()) {
				continue;
			}
			objectiveState.setProgress(resolveCaptureObjectiveProgress(objectiveState.getConfig()));
			if (objectiveState.completeIfReady()) {
				handleCompletedObjective(objectiveState);
			}
		}
	}

	private void handleCompletedObjective(final ObjectiveState objectiveState) {
		if (objectiveState == null) {
			return;
		}
		spawnObjectiveOutcomeCreatures(objectiveState.getConfig().getCompletionExtraSpawns());
		if (objectiveState.getConfig().getCompletionAnnouncement() != null) {
			announceEventChange(objectiveState.getConfig().getTitle(),
					objectiveState.getConfig().getCompletionAnnouncement());
		}
	}

	private void spawnObjectiveOutcomeCreatures(final List<BaseMapEvent.EventSpawn> extraSpawns) {
		if (extraSpawns == null || extraSpawns.isEmpty()) {
			return;
		}
		for (EventSpawn extraSpawn : extraSpawns) {
			if (extraSpawn == null) {
				continue;
			}
			spawnCreatures(extraSpawn.getCreatureName(), extraSpawn.getCount());
		}
	}

	private int resolveCaptureObjectiveProgress(final MapEventConfig.SecondaryObjectiveConfig objectiveConfig) {
		int bestProgress = 0;
		for (CapturePointState capturePoint : capturePoints) {
			if (!objectiveConfig.getCapturePointIds().contains(capturePoint.getPointId())) {
				continue;
			}
			bestProgress = Math.max(bestProgress, capturePoint.getProgressPercent());
		}
		return bestProgress;
	}

	private int applyActiveSpawnMultiplier(final int count) {
		double multiplier = 1.0d;
		if (activePhase != null) {
			multiplier *= activePhase.getSpawnMultiplier();
		}
		for (MapEventConfig.ModifierConfig modifier : activeModifiers) {
			multiplier *= modifier.getSpawnMultiplier();
		}
		return Math.max(0, (int) Math.round(Math.max(0, count) * multiplier));
	}

	private double resolveEffectiveZoneSpawnMultiplier(final String zoneName) {
		double multiplier = getConfig().getZoneSpawnMultiplier(zoneName);
		if (activePhase != null) {
			multiplier *= activePhase.getZoneSpawnMultiplier(zoneName);
		}
		for (MapEventConfig.ModifierConfig modifier : activeModifiers) {
			multiplier *= modifier.getZoneSpawnMultiplier(zoneName);
		}
		return multiplier;
	}

	private void rewardParticipants(final int defeatPercent) {
		final MapEventConfig.RewardSettings rewardSettings = getConfig().getRewardSettings();
		if (rewardSettings == null || defeatPercent < rewardSettings.getMinDefeatPercent()) {
			return;
		}
		final RandomEventRewardService.RandomEventType rewardType;
		try {
			rewardType = RandomEventRewardService.RandomEventType.valueOf(rewardSettings.getRewardType());
		} catch (IllegalArgumentException e) {
			logger.warn(getEventName() + " reward settlement skipped; unknown reward type "
					+ rewardSettings.getRewardType() + ".", e);
			return;
		}
		final double rewardDifficulty = rewardSettings.getBaseDifficultyMultiplier()
				* (0.85d + (Math.max(0, Math.min(100, defeatPercent)) / 100.0d * 0.25d))
				* (1.0d + resolveRewardMultiplierBonus());
		final MapEventRewardSettlementService.SettlementResult settlementResult = new MapEventRewardSettlementService(
				getEventId(),
				contributionTracker,
				rewardPolicy,
				context -> {
					final double eventProgress = Math.max(0.0d, Math.min(1.0d, defeatPercent / 100.0d));
					final double playerScore = Math.max(0.0d,
							Math.min(1.0d, context.getDecision().getTotalScore() / 35.0d));
					final double participationScore = (eventProgress * 0.6d) + (playerScore * 0.4d);
					final RandomEventRewardService.Reward reward = randomEventRewardService.grantRandomEventRewards(
							context.getPlayer(),
							rewardType,
							participationScore,
							rewardDifficulty * context.getDecision().getMultiplier());
					context.getPlayer().sendPrivateText(NotificationType.POSITIVE,
							"Za udział w wydarzeniu otrzymujesz +" + reward.getXp()
									+ " PD oraz +" + Math.round(reward.getKarma() * 100.0d) / 100.0d + " karmy.");
				},
				rewardSettings.getChestEventName() == null ? getEventName() : rewardSettings.getChestEventName())
				.settleRewardsDetailed(MapEventRewardSettlementService.SettlementOptions.defaultOptions());
		final ObjectiveState completedObjective = resolveCompletedObjective();
		final int awardedActivityChests = settlementResult.getAwardedActivityChests();
		final int awardedObjectiveChests = awardCompletedObjectiveChests(completedObjective,
				settlementResult.getQualifiedRewardContexts());
		final String summary = "Podsumowanie wydarzenia " + getEventName() + ": wybito " + defeatPercent
				+ "% sil, bonus modyfikatorĂłw +" + getRewardBonusPercent() + "%, skrzynie aktywnoĹ›ci: "
				+ awardedActivityChests
				+ (completedObjective == null ? ", cel poboczny: niewykonany"
						: ", cel poboczny: " + completedObjective.getConfig().getTitle()
								+ ", dodatkowe zĹ‚ote skrzynie: " + awardedObjectiveChests);
		announceEventChange(getEventName(), summary);
	}

	private int awardCompletedObjectiveChests(final ObjectiveState completedObjective,
			final List<MapEventRewardSettlementService.RewardContext> qualifiedRewardContexts) {
		if (completedObjective == null || qualifiedRewardContexts == null || qualifiedRewardContexts.isEmpty()) {
			return 0;
		}
		final MapEventConfig.SecondaryObjectiveConfig objectiveConfig = completedObjective.getConfig();
		if (objectiveConfig.getRewardType() != MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST) {
			return 0;
		}
		return EventActivityChestRewardService.awardObjectiveCompletionChests(
				getEventName(),
				objectiveConfig.getTitle(),
				objectiveConfig.getRewardItemName(),
				qualifiedRewardContexts);
	}

	private ObjectiveState resolveCompletedObjective() {
		for (ObjectiveState objectiveState : objectivesById.values()) {
			if (objectiveState.isCompleted()) {
				return objectiveState;
			}
		}
		return null;
	}

	private double resolveRewardMultiplierBonus() {
		double bonus = 0.0d;
		for (MapEventConfig.ModifierConfig modifier : activeModifiers) {
			bonus += modifier.getRewardMultiplierBonus();
		}
		for (ObjectiveState objectiveState : objectivesById.values()) {
			if (objectiveState.isCompleted()
					&& objectiveState.getConfig().getRewardType()
							== MapEventConfig.SecondaryObjectiveConfig.RewardType.REWARD_MULTIPLIER) {
				bonus += objectiveState.getConfig().getRewardMultiplierBonus();
			}
		}
		return bonus;
	}

	private void announceEventChange(final String title, final String message) {
		if (message == null || message.trim().isEmpty()) {
			return;
		}
		ScreenAnnouncementBroadcaster.broadcastToPlayersInZones(title, message,
				ScreenAnnouncementBroadcaster.CATEGORY_EVENT, getZones());
	}

	@Override
	protected String getCapturePointsStatusPayload() {
		if (capturePoints.isEmpty()) {
			return null;
		}
		final StringBuilder payload = new StringBuilder();
		payload.append('[');
		for (int i = 0; i < capturePoints.size(); i++) {
			final CapturePointState point = capturePoints.get(i);
			if (i > 0) {
				payload.append(',');
			}
			payload.append('{')
					.append("\"pointId\":\"").append(escapeJson(point.getPointId())).append("\",")
					.append("\"zone\":\"").append(escapeJson(point.getZone())).append("\",")
					.append("\"x\":").append(point.getX()).append(',')
					.append("\"y\":").append(point.getY()).append(',')
					.append("\"radiusTiles\":").append(point.getRadiusTiles()).append(',')
					.append("\"progressPercent\":").append(point.getProgressPercent()).append(',')
					.append("\"activeWave\":").append(point.getActiveWave()).append(',')
					.append("\"completed\":").append(point.isCompleted()).append(',')
					.append("\"owner\":\"players\",")
					.append("\"contested\":false,")
					.append("\"remainingBossWaves\":")
					.append(Math.max(0, getTotalWaves() - point.getActiveWave()))
					.append('}');
		}
		payload.append(']');
		return payload.toString();
	}

	protected List<CapturePointState> createCapturePoints() {
		final List<CapturePointState> points = new ArrayList<>();
		for (MapEventConfig.CapturePointConfig capturePointConfig : getConfig().getCapturePoints()) {
			points.add(new CapturePointState(capturePointConfig.getPointId(), capturePointConfig.getZone(),
					capturePointConfig.getX(), capturePointConfig.getY(), capturePointConfig.getRadiusTiles()));
		}
		return points;
	}

	private void initializeCapturePoints() {
		capturePoints.clear();
		for (CapturePointState point : createCapturePoints()) {
			if (point == null) {
				continue;
			}
			point.reset();
			capturePoints.add(point);
		}
	}

	private void spawnCaptureProgressWave(final CapturePointState capturePoint,
			final MapEventConfig.CaptureProgressWaveConfig wave) {
		if (!isEventActive()) {
			return;
		}
		logger.info(getEventName() + " capture progress trigger fired: point=" + capturePoint.getPointId()
				+ ", threshold=" + wave.getThresholdPercent() + "%.");
		for (EventSpawn spawn : wave.getSpawns()) {
			spawnCreaturesAroundCapturePoint(capturePoint, spawn.getCreatureName(), spawn.getCount());
		}
	}

	private void spawnCreaturesAroundCapturePoint(final CapturePointState capturePoint, final String creatureName,
			final int count) {
		final String zoneName = capturePoint.getZone();
		final int requestedCount = applyActiveSpawnMultiplier(count);
		final double spawnMultiplier = resolveEffectiveZoneSpawnMultiplier(zoneName);
		final int multipliedCount = (int) Math.round(requestedCount * spawnMultiplier);
		final Integer zoneSpawnCap = getConfig().getZoneSpawnCap(zoneName);
		final int finalSpawnCount = zoneSpawnCap == null ? multipliedCount : Math.min(multipliedCount, zoneSpawnCap);

		if (finalSpawnCount <= 0) {
			logger.debug(getEventName() + " capture spawn skipped in zone " + zoneName
					+ "; final spawn count is 0 after multiplier/cap.");
			return;
		}

		spawnStrategy.spawnCreatures(
				getEventName(),
				Collections.singletonList(zoneName),
				creatureName,
				finalSpawnCount,
				zoneName,
				capturePoint.getX(),
				capturePoint.getY(),
				Math.max(0, capturePoint.getRadiusTiles() + 1),
				Math.max(0, capturePoint.getRadiusTiles() + CAPTURE_WAVE_SPAWN_RING_PADDING_TILES),
				creature -> {
					registerEventCreature(creature);
					captureSpawnCreatures.put(creature, Boolean.TRUE);
				});
	}

	protected List<String> getActivePlayerNamesAroundPoint(final CapturePointState point,
			final int minLevelInclusive, final int maxLevelInclusive) {
		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(point.getZone());
		if (zone == null) {
			return Collections.emptyList();
		}
		final int minLevel = Math.max(0, minLevelInclusive);
		final int maxLevel = Math.max(minLevel, maxLevelInclusive);
		final int radiusSquared = point.getRadiusTiles() * point.getRadiusTiles();
		final List<String> players = new ArrayList<>();
		for (Player player : zone.getPlayers()) {
			if (player == null || player.isGhost() || player.isDisconnected()) {
				continue;
			}
			final int level = player.getLevel();
			if (level < minLevel || level > maxLevel) {
				continue;
			}
			final int dx = player.getX() - point.getX();
			final int dy = player.getY() - point.getY();
			if ((dx * dx) + (dy * dy) > radiusSquared) {
				continue;
			}
			if (player.getName() != null && !player.getName().trim().isEmpty()) {
				players.add(player.getName());
			}
		}
		return players;
	}

	private static String escapeJson(final String value) {
		if (value == null) {
			return "";
		}
		return value.replace("\\", "\\\\").replace("\"", "\\\"");
	}

	private WaveScaleState createWaveScaleState(final int waveIndex) {
		final int baseWaveTotal = waveBaseTotals.get(waveIndex);
		final int onlinePlayers = countPlayersInZones();
		final double onlineScale = calculateOnlineScale(onlinePlayers);
		final double clearRateScale = calculateClearRateScale(waveIndex);
		final double combinedScale = onlineScale * clearRateScale;
		final int scaledWaveTarget = clampSpawnPerWave((int) Math.round(baseWaveTotal * combinedScale));
		logger.info(getEventName() + " wave " + (waveIndex + 1)
				+ " scaling: onlinePlayers=" + onlinePlayers
				+ ", onlineScale=" + onlineScale
				+ ", clearRateScale=" + clearRateScale
				+ ", combinedScale=" + combinedScale
				+ ", baseSpawn=" + baseWaveTotal
				+ ", finalSpawn=" + scaledWaveTarget + ".");
		return new WaveScaleState(baseWaveTotal, scaledWaveTarget, currentInstant());
	}

	private double calculateOnlineScale(final int onlinePlayers) {
		if (scalingConfig == null || !scalingConfig.isScaleByOnlineInZones()) {
			return 1.0d;
		}
		final int minPlayers = scalingConfig.getMinPlayers();
		if (minPlayers <= 0) {
			return 1.0d;
		}
		final int cappedPlayers = scalingConfig.getMaxPlayers() > 0
				? Math.min(onlinePlayers, scalingConfig.getMaxPlayers())
				: onlinePlayers;
		return Math.max(0.2d, cappedPlayers / (double) minPlayers);
	}

	private double calculateClearRateScale(final int waveIndex) {
		if (scalingConfig == null || completedWaveClearTimesSec.isEmpty() || waveIndex <= 0) {
			return 1.0d;
		}
		final int completedWaves = Math.min(completedWaveClearTimesSec.size(), waveIndex);
		double avgExpectedSec = 0d;
		for (int i = 0; i < completedWaves; i++) {
			avgExpectedSec += getConfig().getWaves().get(i).getIntervalSeconds();
		}
		avgExpectedSec /= completedWaves;
		double avgClearSec = 0d;
		for (int i = completedWaveClearTimesSec.size() - completedWaves; i < completedWaveClearTimesSec.size(); i++) {
			avgClearSec += completedWaveClearTimesSec.get(i);
		}
		avgClearSec /= completedWaves;
		if (avgExpectedSec <= 0d || avgClearSec <= 0d) {
			return 1.0d;
		}
		final double rawRatio = avgExpectedSec / avgClearSec;
		return 1.0d + ((rawRatio - 1.0d) * scalingConfig.getKillRateMultiplier());
	}

	private int clampSpawnPerWave(final int desiredSpawnCount) {
		if (scalingConfig == null) {
			return desiredSpawnCount;
		}
		final int minSpawn = scalingConfig.getMinSpawnPerWave();
		final int maxSpawn = scalingConfig.getMaxSpawnPerWave();
		return Math.max(minSpawn, Math.min(desiredSpawnCount, maxSpawn));
	}

	private static Map<BaseMapEvent.EventSpawn, Integer> createSpawnWaveIndexes(final List<BaseMapEvent.EventWave> waves) {
		final Map<BaseMapEvent.EventSpawn, Integer> indexes = new IdentityHashMap<>();
		for (int waveIndex = 0; waveIndex < waves.size(); waveIndex++) {
			for (BaseMapEvent.EventSpawn spawn : waves.get(waveIndex).getSpawns()) {
				indexes.put(spawn, waveIndex);
			}
		}
		return indexes;
	}

	private static List<Integer> createWaveBaseTotals(final List<BaseMapEvent.EventWave> waves) {
		final List<Integer> totals = new ArrayList<>();
		for (BaseMapEvent.EventWave wave : waves) {
			int total = 0;
			for (BaseMapEvent.EventSpawn spawn : wave.getSpawns()) {
				total += spawn.getCount();
			}
			totals.add(total);
		}
		return totals;
	}

	private void startEventFromKills() {
		if (!startEvent()) {
			logger.warn(getEventName() + " event already active; skipping duplicate start.");
		}
	}

	private static final class ObjectiveState {
		private enum State {
			PENDING,
			ACTIVE,
			COMPLETED,
			FAILED
		}

		private final MapEventConfig.SecondaryObjectiveConfig config;
		private State state = State.PENDING;
		private int progress;

		private ObjectiveState(final MapEventConfig.SecondaryObjectiveConfig config) {
			this.config = config;
		}

		private MapEventConfig.SecondaryObjectiveConfig getConfig() {
			return config;
		}

		private boolean shouldActivate(final int currentWave) {
			return state == State.PENDING && currentWave >= config.getStartWave();
		}

		private void activate() {
			state = State.ACTIVE;
			progress = 0;
		}

		private boolean shouldFail(final int currentWave) {
			return state == State.ACTIVE && currentWave > config.getEndWave();
		}

		private void fail() {
			state = State.FAILED;
		}

		private boolean isActiveKillObjectiveFor(final String creatureName) {
			return state == State.ACTIVE
					&& config.getType() == MapEventConfig.SecondaryObjectiveConfig.ObjectiveType.KILL_TARGET
					&& config.getTrackedCreatures().contains(creatureName);
		}

		private boolean isActiveCaptureObjective() {
			return state == State.ACTIVE
					&& config.getType() == MapEventConfig.SecondaryObjectiveConfig.ObjectiveType.CAPTURE_PROGRESS;
		}

		private void incrementProgress(final int amount) {
			progress += Math.max(0, amount);
		}

		private void setProgress(final int progress) {
			this.progress = Math.max(0, progress);
		}

		private boolean completeIfReady() {
			if (state != State.ACTIVE) {
				return false;
			}
			final int target = resolveTarget();
			if (target <= 0 || progress < target) {
				return false;
			}
			state = State.COMPLETED;
			progress = target;
			return true;
		}

		private int resolveTarget() {
			if (config.getType() == MapEventConfig.SecondaryObjectiveConfig.ObjectiveType.CAPTURE_PROGRESS) {
				return config.getTargetPercent();
			}
			return config.getTargetCount();
		}

		private boolean isVisible() {
			return state == State.PENDING || state == State.ACTIVE || state == State.COMPLETED;
		}

		private boolean isCompleted() {
			return state == State.COMPLETED;
		}

		private String toStatusPayload() {
			return "{"
					+ "\"objectiveId\":\"" + escapeJson(config.getObjectiveId()) + "\","
					+ "\"title\":\"" + escapeJson(config.getTitle()) + "\","
					+ "\"details\":\"" + escapeJson(config.getDescription()) + "\","
					+ "\"rewardType\":\"" + escapeJson(config.getRewardType().name().toLowerCase()) + "\","
					+ "\"rewardItemName\":\"" + escapeJson(config.getRewardItemName()) + "\","
					+ "\"rewardDescription\":\"" + escapeJson(config.getRewardDescription()) + "\","
					+ "\"state\":\"" + state.name().toLowerCase() + "\","
					+ "\"startWave\":" + Math.max(0, config.getStartWave()) + ","
					+ "\"endWave\":" + Math.max(config.getStartWave(), config.getEndWave()) + ","
					+ "\"trackedTargetLabels\":" + toJsonArray(config.getTrackedTargetLabels()) + ","
					+ "\"progress\":" + Math.max(0, progress) + ","
					+ "\"target\":" + Math.max(0, resolveTarget())
					+ "}";
		}
	}

	private static String toJsonArray(final List<String> values) {
		final StringBuilder payload = new StringBuilder();
		payload.append('[');
		for (int i = 0; i < values.size(); i++) {
			if (i > 0) {
				payload.append(',');
			}
			payload.append("\"").append(escapeJson(values.get(i))).append("\"");
		}
		payload.append(']');
		return payload.toString();
	}

	private static final class WaveScaleState {
		private final int baseWaveTotal;
		private final int scaledWaveTarget;
		private final Instant startedAt;
		private int baseAssigned;
		private int scaledAssigned;
		private int pendingCreatures;

		private WaveScaleState(final int baseWaveTotal, final int scaledWaveTarget, final Instant startedAt) {
			this.baseWaveTotal = baseWaveTotal;
			this.scaledWaveTarget = scaledWaveTarget;
			this.startedAt = startedAt;
		}
	}
}
