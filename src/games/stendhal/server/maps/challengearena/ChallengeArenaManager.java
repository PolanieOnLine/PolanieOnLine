/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

/**
 * Keeps the shared Ados arena reserved while a Challenge Arena run is active.
 *
 * <p>The first implementation intentionally reuses the physical Deathmatch
 * ring, but Challenge Arena remains a separate game mode and quest state.</p>
 */
public final class ChallengeArenaManager {
	private static String activePlayer;

	private ChallengeArenaManager() {
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
	}
}
