/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;

/** Holds the dedicated world data used by Challenge Arena sessions. */
public final class ChallengeArenaInfo {
	private final Area arena;
	private final StendhalRPZone zone;
	private final String lobbyZone;
	private final int lobbyX;
	private final int lobbyY;
	private final int startX;
	private final int startY;
	private ChallengeArenaEngine engine;

	public ChallengeArenaInfo(final Area arena, final StendhalRPZone zone,
			final String lobbyZone, final int lobbyX, final int lobbyY,
			final int startX, final int startY) {
		if (arena == null || zone == null || lobbyZone == null
				|| lobbyZone.trim().isEmpty()) {
			throw new IllegalArgumentException("Challenge Arena world data must not be null");
		}
		this.arena = arena;
		this.zone = zone;
		this.lobbyZone = lobbyZone;
		this.lobbyX = lobbyX;
		this.lobbyY = lobbyY;
		this.startX = startX;
		this.startY = startY;
	}

	public Area getArena() {
		return arena;
	}

	public StendhalRPZone getZone() {
		return zone;
	}

	public boolean isInArena(final Player player) {
		return player != null && arena.contains(player);
	}

	public boolean isEmpty() {
		return arena.getPlayers().isEmpty();
	}

	public boolean hasOnlyPlayer(final Player player) {
		return isInArena(player) && arena.getPlayers().size() == 1;
	}

	public boolean teleportIntoArena(final Player player) {
		return player != null && player.teleport(zone, startX, startY,
				Direction.DOWN, null);
	}

	public boolean teleportToLobby(final Player player) {
		return player != null && player.teleport(lobbyZone, lobbyX, lobbyY,
				Direction.DOWN, null);
	}

	public synchronized boolean startSession(final Player player,
			final ChallengeArenaTier tier) {
		if (player == null || tier == null || !hasOnlyPlayer(player)
				|| !ChallengeArenaManager.isReservedBy(player.getName())) {
			return false;
		}
		final ChallengeArenaState state = ChallengeArenaState.start(tier);
		player.setQuest(ChallengeArenaState.QUEST_SLOT, state.serialize());
		engine = new ChallengeArenaEngine(player, this);
		SingletonRepository.getTurnNotifier().notifyInTurns(0, engine);
		return true;
	}

	public synchronized ChallengeArenaEngine getEngine() {
		return engine;
	}

	void clearEngine(final ChallengeArenaEngine finishedEngine) {
		if (engine == finishedEngine) {
			engine = null;
		}
	}
}
