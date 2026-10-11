/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;
import marauroa.server.game.rp.InstanceScope;

/** Shared lobby configuration and independent, private player runs. */
public final class ChallengeArenaManager {
	private static final Logger logger = Logger.getLogger(ChallengeArenaManager.class);
	private static final Set<String> activePlayers = new HashSet<>();
	private static final Map<String, ChallengeArenaInfo> sessions = new HashMap<>();
	private static ChallengeArenaInfo arenaInfo;
	private static ChallengeArenaRankingSign rankingSign;

	private ChallengeArenaManager() {
	}

	public static synchronized void configureArena(final ChallengeArenaInfo info) {
		arenaInfo = info;
	}

	public static synchronized ChallengeArenaInfo getArenaInfo() {
		return arenaInfo;
	}

	public static synchronized ChallengeArenaInfo getArenaInfo(final String playerName) {
		return sessions.get(playerName);
	}

	/** A snapshot; never expose the mutable session registry to the UI. */
	static synchronized List<String> getObservablePlayers() {
		final List<String> names = new ArrayList<>();
		for (final Map.Entry<String, ChallengeArenaInfo> entry : sessions.entrySet()) {
			if (isObservable(entry.getKey(), entry.getValue())) {
				names.add(entry.getKey());
			}
		}
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER);
		return names;
	}

	static synchronized boolean isObservable(final String name, final ChallengeArenaInfo info) {
		if (info == null || sessions.get(name) != info || info.getEngine() == null) {
			return false;
		}
		for (final Player player : info.getZone().getPlayers()) {
			if (name.equals(player.getName())) {
				final ChallengeArenaState state = ChallengeArenaState.parse(
						player.getQuest(ChallengeArenaState.QUEST_SLOT));
				return !player.isDisconnected() && !player.isGhost() && player.getHP() > 0
						&& info.isInArena(player) && state != null
						&& state.getLifecycle() == ChallengeArenaState.Lifecycle.ACTIVE;
			}
		}
		return false;
	}

	/** Acquire a fresh paid run; never reuse an already active player's session. */
	static synchronized ChallengeArenaInfo createSession(final Player player) {
		if (arenaInfo == null || player == null || isReservedBy(player.getName())) {
			return null;
		}
		final String name = player.getName();
		final ChallengeArenaInstanceFactory factory = new ChallengeArenaInstanceFactory(player, arenaInfo);
		try {
			SingletonRepository.getRPWorld().getInstanceZoneManager().acquire(
					arenaInfo.getZone().getName(), "challenge_arena",
					InstanceScope.player(name), name, factory);
			final ChallengeArenaInfo session = factory.getArenaInfo();
			if (session == null) {
				throw new IllegalStateException("Existing unmanaged Challenge Arena session: " + name);
			}
			sessions.put(name, session);
			activePlayers.add(name);
			return session;
		} catch (final Exception e) {
			logger.error("Could not create private Challenge Arena for " + name, e);
			return null;
		}
	}

	public static synchronized void configureRankingSign(
			final ChallengeArenaRankingSign sign) {
		rankingSign = sign;
	}

	public static synchronized void refreshRankingSign() {
		if (rankingSign != null) {
			rankingSign.updatePlayers();
		}
	}

	public static synchronized boolean reserve(final String playerName) {
		if (playerName == null || playerName.trim().isEmpty()) {
			return false;
		}
		activePlayers.add(playerName);
		return true;
	}

	public static synchronized void release(final String playerName) {
		release(playerName, sessions.get(playerName));
	}

	/** Entry rollback also retries a temporarily blocked lobby without losing money. */
	static synchronized void returnAndRelease(final Player player, final ChallengeArenaInfo expected) {
		if (sessions.get(player.getName()) != expected) {
			return;
		}
		if (!player.isDisconnected() && expected.isInZone(player)
				&& !expected.teleportToLobby(player)) {
			SingletonRepository.getTurnNotifier().notifyInTurns(1,
					turn -> returnAndRelease(player, expected));
			return;
		}
		release(player.getName(), expected);
	}

	/** Identity guard prevents a delayed exit callback from cancelling a newer run. */
	static synchronized void release(final String playerName, final ChallengeArenaInfo expected) {
		if (sessions.get(playerName) != expected) {
			return;
		}
		if (expected != null) {
			// Never detach a map while its owner is still on it.
			for (final Player player : expected.getZone().getPlayers()) {
				if (playerName.equals(player.getName())) {
					return;
				}
			}
			if (!ChallengeArenaObservation.returnViewers(expected)) {
				SingletonRepository.getTurnNotifier().notifyInTurns(1,
						turn -> release(playerName, expected));
				return;
			}
			try {
				SingletonRepository.getRPWorld().getInstanceZoneManager().release(
						expected.getZone().getID(), playerName);
			} catch (final Exception e) {
				logger.error("Could not release private Challenge Arena for " + playerName, e);
				return;
			}
		}
		forgetSession(playerName, expected);
	}

	static synchronized void forgetSession(final String name, final ChallengeArenaInfo expected) {
		if (sessions.get(name) == expected) {
			sessions.remove(name);
			activePlayers.remove(name);
		}
	}

	public static synchronized boolean isReserved() {
		return !activePlayers.isEmpty();
	}

	public static synchronized boolean isReservedBy(final String playerName) {
		return activePlayers.contains(playerName);
	}

	static synchronized void clearForTests() {
		ChallengeArenaObservation.clearForTests();
		activePlayers.clear();
		sessions.clear();
		arenaInfo = null;
		rankingSign = null;
	}
}
