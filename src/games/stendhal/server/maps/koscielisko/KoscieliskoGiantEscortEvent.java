/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.maps.koscielisko;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.IntPredicate;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.ConfiguredMapEvent;
import games.stendhal.server.maps.event.MapEventConfig;
import games.stendhal.server.maps.event.MapEventConfigLoader;

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
	private static final int MIN_ACTIVITY_TICKS_FOR_REWARD = 6;
	private static final int MAX_LOW_ACTIVITY_TICKS = 12;
	private static final int GIANT_TARGET_RANGE = 18;
	private static final int PLAYER_FALLBACK_RANGE = 14;
	private static final int LEASH_RANGE = 24;
	private static final int MAX_GIANT_PATH_LENGTH = 96;
	private static final int GIANT_CHASE_GRACE_TICKS = 4;
	private static final int WAVE_STATUS_MIN_INTERVAL_SECONDS = 50;
	private static final int CRITICAL_STATUS_MIN_INTERVAL_SECONDS = 15;
	private static volatile long lastEventFinishedAtMillis;

	private final MapEventConfig.EscortSettings escortSettings;
	private final int giantDefBonus;
	private final int giantResistance;
	private final int eventCooldownMinutes;
	private final int eventActiveCreatureHardCap;
	private final int waveBudgetBase;
	private final int waveBudgetPerStage;

	private final EscortAnnouncementService announcementService;
	private final GiantHealthController healthController;
	private final EscortAggroController aggroController;
	private final EscortWaveSpawner waveSpawner;
	private final EscortActivityMonitor activityMonitor;
	private final EscortRewardService rewardService;

	private final TurnListener giantHealthMonitor = currentTurn -> monitorGiantHealth();
	private final TurnListener escortAggroTurn = currentTurn -> applyEscortAggroLogic();
	private final TurnListener activityTracker = currentTurn -> samplePlayerActivity();

	private volatile SpeakerNPC giantNpc;
	private volatile GiantSnapshot snapshot;
	private volatile boolean failedByGiantHealth;
	private volatile boolean failedByLowActivity;
	private volatile boolean blockedByCooldown;
	private volatile boolean escortSuccess;
	private volatile long eventStartedAtMillis;
	private volatile int giantEventHp;
	private volatile double finalHealthRatio;
	private final List<Integer> announcedWaveOffsets = new ArrayList<>();
	private final List<Integer> announcedWaveMilestones = new ArrayList<>();

	public KoscieliskoGiantEscortEvent() {
		this(MapEventConfigLoader.load(MapEventConfigLoader.KOSCIELISKO_GIANT_ESCORT));
	}

	private KoscieliskoGiantEscortEvent(final MapEventConfig config) {
		super(LOGGER, config);
		escortSettings = config.getEscortSettings();
		giantDefBonus = resolveConfiguredInt(escortSettings != null ? escortSettings.getGiantDefBonus() : null, 220, value -> value >= 0);
		giantResistance = resolveConfiguredInt(escortSettings != null ? escortSettings.getResistance() : null, 40, value -> value >= 0);
		eventCooldownMinutes = resolveConfiguredInt(escortSettings != null ? escortSettings.getCooldownMinutes() : null, 45, value -> value >= 0);
		eventActiveCreatureHardCap = resolveConfiguredInt(escortSettings != null ? escortSettings.getHardCap() : null, 34, value -> value > 0);
		waveBudgetBase = resolveConfiguredInt(escortSettings != null ? escortSettings.getWaveBudgetBase() : null, 10, value -> value >= 0);
		waveBudgetPerStage = resolveConfiguredInt(escortSettings != null ? escortSettings.getWaveBudgetPerStage() : null, 4, value -> value >= 0);
		announcementService = new EscortAnnouncementService(LOGGER, getEventName(),
				TimeUnit.SECONDS.toMillis(WAVE_STATUS_MIN_INTERVAL_SECONDS),
				TimeUnit.SECONDS.toMillis(CRITICAL_STATUS_MIN_INTERVAL_SECONDS));
		healthController = new GiantHealthController(GIANT_FAIL_HP_THRESHOLD, GIANT_CRITICAL_HP_THRESHOLD, GIANT_HEAL_CAP_PER_SECOND);
		aggroController = new EscortAggroController(LOGGER, getEventName(), GIANT_TARGET_RANGE, PLAYER_FALLBACK_RANGE,
				LEASH_RANGE, MAX_GIANT_PATH_LENGTH, GIANT_CHASE_GRACE_TICKS);
		waveSpawner = new EscortWaveSpawner(LOGGER, getEventName(), eventActiveCreatureHardCap, waveBudgetBase, waveBudgetPerStage);
		activityMonitor = new EscortActivityMonitor(MIN_PLAYERS_TO_START, MIN_ACTIVITY_TICKS_FOR_REWARD, MAX_LOW_ACTIVITY_TICKS);
		rewardService = new EscortRewardService();
	}

	@Override
	protected void onStart() {
		failedByGiantHealth = false;
		failedByLowActivity = false;
		blockedByCooldown = false;
		escortSuccess = false;
		finalHealthRatio = 0.0d;
		snapshot = null;
		eventStartedAtMillis = 0L;
		giantEventHp = resolveGiantEventHp();
		announcementService.reset();
		aggroController.reset();
		waveSpawner.reset();
		activityMonitor.reset();
		announcedWaveOffsets.clear();
		announcedWaveMilestones.clear();

		if (shouldValidateStartGates() && !validateStartGates()) {
			endEvent();
			return;
		}

		giantNpc = SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		super.onStart();
		if (giantNpc == null) {
			failedByGiantHealth = true;
			endEvent();
			return;
		}

		snapshot = GiantSnapshot.capture(giantNpc);
		giantNpc.setBaseHP(giantEventHp);
		giantNpc.setHP(giantEventHp);
		giantNpc.setResistance(giantResistance);
		giantNpc.setDef(snapshot.def + giantDefBonus);
		eventStartedAtMillis = System.currentTimeMillis();
		healthController.reset(giantEventHp, eventStartedAtMillis);
		announcementService.announceProgressStatus("START", 0, healthController.hpPercent(giantNpc));
		scheduleHealthMonitor();
		scheduleAggroController();
		scheduleActivityTracker();
	}

	@Override
	protected void onStop() {
		SingletonRepository.getTurnNotifier().dontNotify(giantHealthMonitor);
		SingletonRepository.getTurnNotifier().dontNotify(escortAggroTurn);
		SingletonRepository.getTurnNotifier().dontNotify(activityTracker);
		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		try {
			escortSuccess = !failedByGiantHealth && !failedByLowActivity && currentGiant != null && currentGiant.getHP() > GIANT_FAIL_HP_THRESHOLD;
			if (currentGiant != null) {
				finalHealthRatio = Math.max(0.0d, Math.min(1.0d, (double) currentGiant.getHP() / giantEventHp));
			}
			if (escortSuccess) {
				rewardService.payout(finalHealthRatio, activityMonitor.rewardEligiblePlayers(), playersInObserverZones());
			}
		} finally {
			if (snapshot != null && currentGiant != null) {
				snapshot.restore(currentGiant);
			}
			snapshot = null;
			giantNpc = null;
			lastEventFinishedAtMillis = System.currentTimeMillis();
			super.onStop();
		}
	}

	@Override
	protected void onWaveSpawn(final EventSpawn spawn) {
		final Integer waveOffset = findWaveOffset(spawn);
		if (waveOffset == null) {
			return;
		}
		waveSpawner.initializeWaveBudgetIfNeeded(waveOffset, (int) getEventDuration().getSeconds());
		if (announcedWaveOffsets.contains(waveOffset)) {
			return;
		}
		announcedWaveOffsets.add(waveOffset);
		final int durationSeconds = (int) getEventDuration().getSeconds();
		final int progress = durationSeconds <= 0 ? 0 : (int) Math.round((waveOffset * 100.0d) / durationSeconds);
		final Integer milestone = announcementService.findWaveMilestone(Math.min(progress, 100));
		if (milestone == null || announcedWaveMilestones.contains(milestone)) {
			return;
		}
		announcedWaveMilestones.add(milestone);
		if (announcementService.canAnnounceWaveMilestone(milestone, System.currentTimeMillis())) {
			announcementService.announceProgressStatus("FALA_" + milestone, milestone,
					giantNpc == null ? 0 : healthController.hpPercent(giantNpc));
		}
	}

	@Override
	protected void spawnCreatures(final String creatureName, final int count) {
		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		giantNpc = currentGiant;
		int activeCreatures = countActiveEventCreatures();
		for (final String zoneName : getZones()) {
			final double zoneSpawnMultiplier = getConfig().getZoneSpawnMultiplier(zoneName);
			final Integer zoneSpawnCap = getConfig().getZoneSpawnCap(zoneName);
			final EscortWaveSpawner.SpawnOutcome outcome = waveSpawner.spawnCreatures(
					Collections.singletonList(zoneName),
					creatureName,
					count,
					currentGiant,
					zoneSpawnMultiplier,
					zoneSpawnCap,
					activeCreatures,
					this::registerEventCreature);
			activeCreatures += outcome.getTotal();
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

	private void monitorGiantHealth() {
		if (!isEventActive()) {
			return;
		}
		giantNpc = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		if (healthController.shouldFail(giantNpc)) {
			failedByGiantHealth = true;
			endEvent();
			return;
		}
		healthController.limitHealingPerTick(giantNpc);
		healthController.announceMilestones(giantNpc, getEventDuration(), System.currentTimeMillis(), announcementService);
		scheduleHealthMonitor();
	}

	private void applyEscortAggroLogic() {
		if (!isEventActive()) {
			return;
		}
		giantNpc = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		aggroController.apply(getEventCreaturesSnapshot(), giantNpc, getConfig().isGiantOnlyAggro());
		scheduleAggroController();
	}

	private void samplePlayerActivity() {
		if (!isEventActive()) {
			return;
		}
		if (giantNpc == null || giantNpc.getZone() == null) {
			scheduleActivityTracker();
			return;
		}
		if (!activityMonitor.sample(playersInObserverZones(), giantNpc.getZone().getName(), System.currentTimeMillis())) {
			failedByLowActivity = true;
			endEvent();
			return;
		}
		scheduleActivityTracker();
	}

	private void scheduleHealthMonitor() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(HEALTH_CHECK_INTERVAL_SECONDS, giantHealthMonitor);
	}

	private void scheduleAggroController() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(AGGRO_TICK_INTERVAL_SECONDS, escortAggroTurn);
	}

	private void scheduleActivityTracker() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(ACTIVITY_SAMPLE_INTERVAL_SECONDS, activityTracker);
	}

	private int resolveGiantEventHp() {
		final int configuredHp = resolveConfiguredInt(escortSettings != null ? escortSettings.getGiantHp() : null, 32000, value -> value > 0);
		if (configuredHp > MAX_ENTITY_HP_SHORT) {
			LOGGER.warn(getEventName() + " received out-of-range HP; clamping to " + MAX_ENTITY_HP_SHORT + ".");
			return MAX_ENTITY_HP_SHORT;
		}
		return configuredHp;
	}

	private int resolveConfiguredInt(final Integer configuredValue, final int fallbackValue, final IntPredicate validator) {
		if (configuredValue == null) {
			return fallbackValue;
		}
		return validator.test(configuredValue) ? configuredValue : fallbackValue;
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
			SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
					"Za mało ludzi na szlaku. Do eskorty potrzeba co najmniej " + MIN_PLAYERS_TO_START + " obrońców.");
			failedByLowActivity = true;
			return false;
		}
		return true;
	}

	private boolean isCooldownReady() {
		if (lastEventFinishedAtMillis <= 0L) {
			return true;
		}
		final long readyAt = lastEventFinishedAtMillis + TimeUnit.MINUTES.toMillis(eventCooldownMinutes);
		if (System.currentTimeMillis() >= readyAt) {
			return true;
		}
		final long remainingMinutes = Math.max(1L, TimeUnit.MILLISECONDS.toMinutes(readyAt - System.currentTimeMillis()));
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG,
				"Szlak po ostatniej walce jeszcze niegotów. Następna eskorta za około " + remainingMinutes + " min.");
		return false;
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

	private int countActiveEventCreatures() {
		int active = 0;
		for (final Creature creature : getEventCreaturesSnapshot()) {
			if (creature != null && creature.getZone() != null && creature.getHP() > 0) {
				active++;
			}
		}
		return active;
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
			return new GiantSnapshot(giant.getHP(), giant.getBaseHP(), giant.getAtk(), giant.getRatk(), giant.getDef(), giant.getResistance());
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


	public static final class BroadcastRateLimiter {
		private final long minIntervalMillis;
		private final java.util.Map<String, Long> lastBroadcastAtMillis = new java.util.HashMap<>();

		public BroadcastRateLimiter(final long minIntervalMillis) {
			this.minIntervalMillis = Math.max(0L, minIntervalMillis);
		}

		public synchronized boolean tryAcquire(final String key, final long nowMillis) {
			final Long previous = lastBroadcastAtMillis.get(key);
			if (previous != null && (nowMillis - previous) < minIntervalMillis) {
				return false;
			}
			lastBroadcastAtMillis.put(key, nowMillis);
			return true;
		}

		public synchronized void clear() {
			lastBroadcastAtMillis.clear();
		}
	}

}