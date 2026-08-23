/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.Spot;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.Area;

/** Holds the shared world objects used by Challenge Arena sessions. */
public final class ChallengeArenaInfo {
	private final Area arena;
	private final StendhalRPZone zone;
	private final Spot entrance;
	private ChallengeArenaEngine engine;

	public ChallengeArenaInfo(final Area arena, final StendhalRPZone zone,
			final Spot entrance) {
		if (arena == null || zone == null || entrance == null) {
			throw new IllegalArgumentException("Challenge Arena world data must not be null");
		}
		this.arena = arena;
		this.zone = zone;
		this.entrance = entrance;
	}

	public Area getArena() {
		return arena;
	}

	public StendhalRPZone getZone() {
		return zone;
	}

	public Spot getEntrance() {
		return entrance;
	}

	public boolean isInArena(final Player player) {
		return player != null && arena.contains(player);
	}

	public boolean hasOnlyPlayer(final Player player) {
		return isInArena(player) && arena.getPlayers().size() == 1;
	}

	public synchronized boolean startSession(final Player player,
			final ChallengeArenaTier tier, final EventRaiser raiser) {
		if (player == null || tier == null || !hasOnlyPlayer(player)
				|| !ChallengeArenaManager.reserve(player.getName())) {
			return false;
		}
		final ChallengeArenaState state = ChallengeArenaState.start(tier);
		player.setQuest(ChallengeArenaState.QUEST_SLOT, state.serialize());
		engine = new ChallengeArenaEngine(player, this, raiser);
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
