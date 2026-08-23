/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

/** Keeps the dedicated Challenge Arena configured and reserved for one player. */
public final class ChallengeArenaManager {
	private static String activePlayer;
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
		if (activePlayer != null && !activePlayer.equals(playerName)) {
			return false;
		}
		activePlayer = playerName;
		return true;
	}

	public static synchronized void release(final String playerName) {
		if (activePlayer != null && activePlayer.equals(playerName)) {
			activePlayer = null;
		}
	}

	public static synchronized boolean isReserved() {
		return activePlayer != null;
	}

	public static synchronized boolean isReservedBy(final String playerName) {
		return activePlayer != null && activePlayer.equals(playerName);
	}

	static synchronized void clearForTests() {
		activePlayer = null;
		arenaInfo = null;
		rankingSign = null;
	}
}
