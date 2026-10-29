/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.List;
import java.util.function.LongSupplier;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

/** Runs one paid Challenge Arena session. */
public final class ChallengeArenaEngine implements TurnListener {
	private static final Logger logger = Logger.getLogger(ChallengeArenaEngine.class);
	private static final long WAVE_DELAY_MILLIS = 2500L;
	private static final long LOOT_PERIOD_MILLIS = 60000L;
	private static final long BLOCKED_WAVE_TIMEOUT_MILLIS = 60000L;

	private final Player player;
	private final ChallengeArenaInfo arenaInfo;
	private final ChallengeArenaCreatureSpawner spawner;
	private final List<ChallengeArenaModifier> modifiers;
	private final LongSupplier clock;

	private boolean running = true;
	private boolean finishing;
	private long nextWaveAt;
	private long lootUntil;
	private long blockedWaveSince = -1L;

	ChallengeArenaEngine(final Player player, final ChallengeArenaInfo arenaInfo) {
		this(player, arenaInfo, System::currentTimeMillis);
	}

	ChallengeArenaEngine(final Player player, final ChallengeArenaInfo arenaInfo,
			final LongSupplier clock) {
		this.clock = clock;
		this.player = player;
		this.arenaInfo = arenaInfo;
		this.spawner = new ChallengeArenaCreatureSpawner();

		final ChallengeArenaState state = getState();
		final int modifierCount = state == null ? 0
				: state.getTier().getModifierCount();
		this.modifiers = ChallengeArenaModifier.randomModifiers(modifierCount);
		announceStart(state);
	}

	@Override
	public void onTurnReached(final int currentTurn) {
		if (!running) {
			return;
		}
		if (finishing) {
			finishAndReturnPlayer();
			return;
		}

		final ChallengeArenaState state = getState();
		if (lootUntil != 0) {
			if (clock.getAsLong() >= lootUntil || player.isDisconnected()
					|| player.getHP() <= 0 || !arenaInfo.isInArena(player)
					|| !ChallengeArenaManager.isReservedBy(player.getName())) {
				finishAndReturnPlayer();
			} else {
				SingletonRepository.getTurnNotifier().notifyInTurns(0, this);
			}
			return;
		}
		if (!isSessionValid(state)) {
			failRun(state);
			return;
		}

		final boolean allCreaturesDead = spawner.areAllCreaturesDead();
		if (state.getSpawnedCreatures() >= state.getTier().getCreatureCount()
				&& allCreaturesDead) {
			completeRun(state);
			return;
		}

		final ChallengeArenaTier tier = state.getTier();
		final int waveNumber = tier.getNextWaveNumber(state.getSpawnedCreatures());
		final int remainingInWave = tier.getRemainingWaveSize(state.getSpawnedCreatures());
		final boolean partialWave = remainingInWave > 0
				&& remainingInWave < tier.getWaveSizeForWave(waveNumber);
		if (partialWave && allCreaturesDead) {
			if (blockedWaveSince < 0) {
				blockedWaveSince = clock.getAsLong();
			} else if (clock.getAsLong() - blockedWaveSince >= BLOCKED_WAVE_TIMEOUT_MILLIS) {
				logger.warn("Challenge Arena partial wave remained blocked for " + player.getName());
				failRun(state);
				return;
			}
		} else {
			// Combat may take arbitrarily long; only idle placement failures time out.
			blockedWaveSince = -1L;
		}

		if ((allCreaturesDead || partialWave)
				&& clock.getAsLong() >= nextWaveAt) {
			spawnNextWave(state);
		}

		if (running) {
			SingletonRepository.getTurnNotifier().notifyInTurns(0, this);
		}
	}

	boolean forfeit(final Player requester) {
		if (!running || requester == null || requester != player
				|| !ChallengeArenaManager.isReservedBy(player.getName())) {
			return false;
		}
		if (lootUntil != 0) {
			finishAndReturnPlayer();
			return true;
		}
		player.sendPrivateText("Poddajesz Arenę Wyzwań. Wpisowe przepada.");
		failRun(getState());
		return true;
	}

	private ChallengeArenaState getState() {
		return ChallengeArenaState.parse(
				player.getQuest(ChallengeArenaState.QUEST_SLOT));
	}

	private boolean isSessionValid(final ChallengeArenaState state) {
		return state != null
				&& state.getLifecycle() == ChallengeArenaState.Lifecycle.ACTIVE
				&& !player.isDisconnected()
				&& player.getHP() > 0
				&& arenaInfo.isInArena(player)
				&& ChallengeArenaManager.isReservedBy(player.getName());
	}

	private void spawnNextWave(final ChallengeArenaState state) {
		final ChallengeArenaTier tier = state.getTier();
		final int alreadySpawned = state.getSpawnedCreatures();
		final int remaining = tier.getCreatureCount() - alreadySpawned;
		final int waveNumber = tier.getNextWaveNumber(alreadySpawned);
		if (remaining <= 0 || waveNumber > tier.getWaveCount()) {
			return;
		}
		final int waveSize = tier.getRemainingWaveSize(alreadySpawned);
		final boolean firstBatch = waveSize == tier.getWaveSizeForWave(waveNumber);
		if (firstBatch) {
			spawner.beginWave(tier.getTargetCreatureLevel(player.getLevel(), alreadySpawned + 1));
		}
		int spawnedNow = 0;

		for (int i = 0; i < waveSize; i++) {
			final int creatureNumber = alreadySpawned + spawnedNow + 1;
			final int targetLevel = tier.getTargetCreatureLevel(
					player.getLevel(), creatureNumber);
			final boolean finalChampion = tier.isFinalChampion(creatureNumber);
			final Creature creature = spawner.spawn(player, arenaInfo, targetLevel,
					tier.shouldForceElite(creatureNumber), finalChampion, tier,
					modifiers);
			if (creature == null) {
				break;
			}
			if (finalChampion) {
				player.sendPrivateText("Na arenę wkracza Czempion Areny.");
			}
			spawnedNow++;
		}

		if (spawnedNow == 0) {
			if (!firstBatch) {
				// Keep the successful part of this wave and retry the missing slots.
				nextWaveAt = clock.getAsLong() + WAVE_DELAY_MILLIS;
				return;
			}
			logger.warn("Challenge Arena could not spawn a wave for "
					+ player.getName());
			failRun(state);
			return;
		}

		final ChallengeArenaState updated = state.withSpawnedCreatures(
				alreadySpawned + spawnedNow);
		player.setQuest(ChallengeArenaState.QUEST_SLOT, updated.serialize());
		// Progress starts a fresh idle allowance even if the new enemy dies
		// before the next engine turn can observe it alive.
		blockedWaveSince = -1L;
		nextWaveAt = clock.getAsLong() + WAVE_DELAY_MILLIS;

		if (firstBatch) {
			// Announce the logical wave once, including any pending placements.
			player.sendPrivateText("Arena Wyzwań. Fala " + waveNumber + " z "
					+ tier.getWaveCount() + ". Przeciwników w tej fali "
					+ tier.getWaveSizeForWave(waveNumber) + ".");
		}
	}

	private void completeRun(final ChallengeArenaState state) {
		final long duration = Math.max(0L,
				clock.getAsLong() - state.getStartedAt());
		player.setQuest(ChallengeArenaState.QUEST_SLOT,
				state.withLifecycle(ChallengeArenaState.Lifecycle.VICTORY).serialize());
		ChallengeArenaRewardService.rewardVictory(player, state.getTier(), duration);
		player.setQuest(ChallengeArenaState.QUEST_SLOT,
				state.withLifecycle(ChallengeArenaState.Lifecycle.DONE).serialize());
		lootUntil = clock.getAsLong() + LOOT_PERIOD_MILLIS;
		ChallengeArenaObservation.returnViewers(arenaInfo);
		player.sendPrivateText("Zwycięstwo! Masz 60 sekund na zebranie łupów, zanim wrócisz do kupca. Możesz wcześniej opuścić arenę południową bramą bez utraty nagrody.");
		SingletonRepository.getTurnNotifier().notifyInTurns(0, this);
	}

	private void failRun(final ChallengeArenaState state) {
		if (!running) {
			return;
		}
		if (state != null) {
			player.setQuest(ChallengeArenaState.QUEST_SLOT,
					state.withLifecycle(ChallengeArenaState.Lifecycle.FAILED).serialize());
		}
		if (!player.isDisconnected() && player.getHP() > 0) {
			player.sendPrivateText("Arena Wyzwań została przerwana. Wpisowe przepada.");
		}
		finishAndReturnPlayer();
	}

	private void finishAndReturnPlayer() {
		finishing = true;
		spawner.removeAllCreatures();
		if (!player.isDisconnected() && arenaInfo.isInZone(player)
				&& !arenaInfo.teleportToLobby(player)) {
			// Keep the map alive and retry rather than stranding its occupant.
			SingletonRepository.getTurnNotifier().notifyInTurns(1, this);
			return;
		}
		stop();
		ChallengeArenaManager.release(player.getName(), arenaInfo);
		// Legacy/test sessions do not have a registered private instance.
		if (ChallengeArenaManager.getArenaInfo(player.getName()) == null) {
			ChallengeArenaManager.release(player.getName());
		}
	}

	void stop() {
		running = false;
		ChallengeArenaObservation.returnViewers(arenaInfo);
		SingletonRepository.getTurnNotifier().dontNotify(this);
		spawner.removeAllCreatures();
		arenaInfo.clearEngine(this);
	}

	private void announceStart(final ChallengeArenaState state) {
		if (state == null) {
			return;
		}
		player.sendPrivateText("Arena Wyzwań rozpoczęta. Do pokonania "
				+ state.getTier().getCreatureCount() + " przeciwników w "
				+ state.getTier().getWaveCount() + " falach.");
		player.sendPrivateText("To twoja prywatna arena. Aby się poddać, użyj południowej bramy. Podczas walki wpisowe przepada.");
		if (!modifiers.isEmpty()) {
			final StringBuilder text = new StringBuilder("Modyfikatory areny ");
			for (int i = 0; i < modifiers.size(); i++) {
				if (i > 0) {
					text.append(", ");
				}
				text.append(modifiers.get(i).getDisplayName());
			}
			player.sendPrivateText(text.toString());
		}
	}
}
