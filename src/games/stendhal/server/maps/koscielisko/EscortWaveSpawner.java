package games.stendhal.server.maps.koscielisko;

import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Rand;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rp.StendhalRPAction;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.maps.event.RandomSafeSpotSpawnStrategy;

final class EscortWaveSpawner {
	private static final int ESCORT_RING_MIN_RADIUS = 4;
	private static final int ESCORT_RING_MAX_RADIUS = 10;
	private static final int ESCORT_SPAWN_ATTEMPTS = 30;
	private static final int LOW_PRESSURE_ACTIVE_CREATURE_THRESHOLD = 7;

	private final Logger logger;
	private final String eventName;
	private final int hardCap;
	private final int waveBudgetBase;
	private final int waveBudgetPerStage;
	private final RandomSafeSpotSpawnStrategy fallbackSpawnStrategy;
	private int currentWaveOffsetSeconds = -1;
	private int currentWaveBudget;

	EscortWaveSpawner(final Logger logger,
			final String eventName,
			final int hardCap,
			final int waveBudgetBase,
			final int waveBudgetPerStage) {
		this.logger = logger;
		this.eventName = eventName;
		this.hardCap = hardCap;
		this.waveBudgetBase = waveBudgetBase;
		this.waveBudgetPerStage = waveBudgetPerStage;
		this.fallbackSpawnStrategy = new RandomSafeSpotSpawnStrategy(logger);
	}

	void reset() {
		currentWaveOffsetSeconds = -1;
		currentWaveBudget = 0;
	}

	void initializeWaveBudgetIfNeeded(final int waveOffset, final int eventDurationSeconds) {
		if (waveOffset == currentWaveOffsetSeconds) {
			return;
		}
		currentWaveOffsetSeconds = waveOffset;
		final int stage = resolveDifficultyStage(waveOffset, eventDurationSeconds);
		currentWaveBudget = Math.min(hardCap, waveBudgetBase + ((stage - 1) * waveBudgetPerStage));
	}

	int getCurrentWaveBudget() {
		return currentWaveBudget;
	}

	SpawnOutcome spawnCreatures(final List<String> zones,
			final String creatureName,
			final int count,
			final SpeakerNPC currentGiant,
			final double zoneSpawnMultiplier,
			final Integer zoneSpawnCap,
			final int activeCreatures,
			final CreatureRegistrar registrar) {
		int nearGiantSpawns = 0;
		int fallbackSpawns = 0;
		int active = activeCreatures;

		for (final String zoneName : zones) {
			final int multipliedCount = (int) Math.round(count * zoneSpawnMultiplier);
			final int finalSpawnCount = zoneSpawnCap == null ? multipliedCount : Math.min(multipliedCount, zoneSpawnCap);
			if (finalSpawnCount <= 0) {
				continue;
			}
			final int hardCapSlots = Math.max(0, hardCap - active);
			int budgetSlots = currentWaveBudget;
			if (active < LOW_PRESSURE_ACTIVE_CREATURE_THRESHOLD && hardCapSlots > 0 && budgetSlots <= 0) {
				budgetSlots = 1;
			}
			final int allowed = Math.min(finalSpawnCount, Math.min(hardCapSlots, budgetSlots));
			if (allowed <= 0) {
				continue;
			}

			if (currentGiant != null && currentGiant.getZone() != null && currentGiant.getZone().getName().equals(zoneName)) {
				final SpawnStats stats = spawnAroundGiantInZone(currentGiant, creatureName, allowed, registrar);
				nearGiantSpawns += stats.nearGiant;
				fallbackSpawns += stats.fallback;
				final int spawnedNow = stats.nearGiant + stats.fallback;
				consumeWaveBudget(spawnedNow);
				active += spawnedNow;
			} else {
				final int fallbackSpawned = spawnWithFallback(zoneName, creatureName, allowed, registrar);
				fallbackSpawns += fallbackSpawned;
				consumeWaveBudget(fallbackSpawned);
				active += fallbackSpawned;
			}
		}
		return new SpawnOutcome(nearGiantSpawns, fallbackSpawns);
	}

	private void consumeWaveBudget(final int spawned) {
		if (spawned > 0) {
			currentWaveBudget = Math.max(0, currentWaveBudget - spawned);
		}
	}

	private int resolveDifficultyStage(final int waveOffsetSeconds, final int durationSeconds) {
		final int safeDuration = Math.max(1, durationSeconds);
		final double progress = Math.max(0.0d, Math.min(1.0d, waveOffsetSeconds / (double) safeDuration));
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

	private SpawnStats spawnAroundGiantInZone(final SpeakerNPC currentGiant,
			final String creatureName,
			final int count,
			final CreatureRegistrar registrar) {
		int successfulSpawns = 0;
		int fallbackSpawns = 0;
		for (int i = 0; i < count; i++) {
			final Creature template = SingletonRepository.getEntityManager().getCreature(creatureName);
			if (template == null) {
				break;
			}
			final Creature creature = new Creature(template.getNewInstance());
			if (placeCreatureNearGiant(currentGiant, creature)) {
				registrar.register(creature);
				successfulSpawns++;
			} else {
				fallbackSpawns += spawnWithFallback(currentGiant.getZone().getName(), creatureName, 1, registrar);
			}
		}
		return new SpawnStats(successfulSpawns, fallbackSpawns);
	}

	private int spawnWithFallback(final String zoneName,
			final String creatureName,
			final int count,
			final CreatureRegistrar registrar) {
		final int[] spawned = { 0 };
		fallbackSpawnStrategy.spawnCreatures(eventName, Collections.singletonList(zoneName), creatureName, count, creature -> {
			registrar.register(creature);
			spawned[0]++;
		});
		return spawned[0];
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

	interface CreatureRegistrar {
		void register(Creature creature);
	}

	static final class SpawnOutcome {
		private final int nearGiant;
		private final int fallback;

		SpawnOutcome(final int nearGiant, final int fallback) {
			this.nearGiant = nearGiant;
			this.fallback = fallback;
		}

		int getTotal() {
			return nearGiant + fallback;
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
}
