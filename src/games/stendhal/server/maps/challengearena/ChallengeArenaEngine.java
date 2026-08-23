/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.player.Player;

/** Runs one paid Challenge Arena session. */
public final class ChallengeArenaEngine implements TurnListener {
	private static final Logger logger = Logger.getLogger(ChallengeArenaEngine.class);
	private static final long WAVE_DELAY_MILLIS = 2500L;

	private final Player player;
	private final ChallengeArenaInfo arenaInfo;
	private final ChallengeArenaCreatureSpawner spawner;
	private final List<ChallengeArenaModifier> modifiers;

	private boolean running = true;
	private long nextWaveAt;

	ChallengeArenaEngine(final Player player, final ChallengeArenaInfo arenaInfo) {
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

		final ChallengeArenaState state = getState();
		if (!isSessionValid(state)) {
			failRun(state);
			return;
		}

		if (state.getSpawnedCreatures() >= state.getTier().getCreatureCount()
				&& spawner.areAllCreaturesDead()) {
			completeRun(state);
			return;
		}

		if (spawner.areAllCreaturesDead()
				&& System.currentTimeMillis() >= nextWaveAt) {
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
		final int waveSize = Math.min(tier.getWaveSize(), remaining);
		int spawnedNow = 0;

		for (int i = 0; i < waveSize; i++) {
			final int creatureNumber = alreadySpawned + spawnedNow + 1;
			final int targetLevel = tier.getTargetCreatureLevel(
					player.getLevel(), creatureNumber);
			final Creature creature = spawner.spawn(player, arenaInfo, targetLevel,
					tier.shouldForceElite(creatureNumber), modifiers);
			if (creature == null) {
				break;
			}
			spawnedNow++;
		}

		if (spawnedNow == 0) {
			logger.warn("Challenge Arena could not spawn a wave for "
					+ player.getName());
			failRun(state);
			return;
		}

		final ChallengeArenaState updated = state.withSpawnedCreatures(
				alreadySpawned + spawnedNow);
		player.setQuest(ChallengeArenaState.QUEST_SLOT, updated.serialize());
		nextWaveAt = System.currentTimeMillis() + WAVE_DELAY_MILLIS;

		final int waveNumber = 1 + alreadySpawned / Math.max(1, tier.getWaveSize());
		player.sendPrivateText("Arena Wyzwań. Fala " + waveNumber
				+ ". Przeciwników w tej fali " + spawnedNow + ".");
	}

	private void completeRun(final ChallengeArenaState state) {
		final long duration = Math.max(0L,
				System.currentTimeMillis() - state.getStartedAt());
		player.setQuest(ChallengeArenaState.QUEST_SLOT,
				state.withLifecycle(ChallengeArenaState.Lifecycle.VICTORY).serialize());
		ChallengeArenaRewardService.rewardVictory(player, state.getTier(), duration);
		player.setQuest(ChallengeArenaState.QUEST_SLOT,
				state.withLifecycle(ChallengeArenaState.Lifecycle.DONE).serialize());
		finishAndReturnPlayer();
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
		running = false;
		spawner.removeAllCreatures();
		ChallengeArenaManager.release(player.getName());
		arenaInfo.clearEngine(this);
		if (!player.isDisconnected() && player.getHP() > 0
				&& arenaInfo.isInArena(player)) {
			arenaInfo.returnPlayer(player);
		}
	}

	private void announceStart(final ChallengeArenaState state) {
		if (state == null) {
			return;
		}
		player.sendPrivateText("Arena Wyzwań rozpoczęta. Do pokonania "
				+ state.getTier().getCreatureCount() + " przeciwników.");
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
