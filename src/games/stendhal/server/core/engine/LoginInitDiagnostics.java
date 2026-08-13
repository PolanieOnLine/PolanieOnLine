/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.engine;

import org.apache.log4j.Logger;

/** Records a compact stage breakdown for unusually slow character login. */
final class LoginInitDiagnostics {
	private static final long SLOW_LOGIN_NANOS = 50L * 1000L * 1000L;

	private final String playerName;
	private final long startedNanos = System.nanoTime();
	private long placementNanos;
	private long transferNanos;
	private long onlineNanos;
	private long listenersNanos;
	private long adminNanos;
	private long welcomeNanos;
	private long outfitNanos;

	LoginInitDiagnostics(final String playerName) {
		this.playerName = playerName;
	}

	void placement(final long nanos) {
		placementNanos = nanos;
	}

	void transfer(final long nanos) {
		transferNanos = nanos;
	}

	void online(final long nanos) {
		onlineNanos = nanos;
	}

	void listeners(final long nanos) {
		listenersNanos = nanos;
	}

	void admin(final long nanos) {
		adminNanos = nanos;
	}

	void welcome(final long nanos) {
		welcomeNanos = nanos;
	}

	void outfit(final long nanos) {
		outfitNanos = nanos;
	}

	void logIfSlow(final Logger logger) {
		final long elapsedNanos = System.nanoTime() - startedNanos;
		if (elapsedNanos < SLOW_LOGIN_NANOS) {
			return;
		}
		logger.warn("Slow player onInit [player=" + playerName
				+ ", elapsedMs=" + elapsedNanos / 1000000L
				+ ", placementUs=" + placementNanos / 1000L
				+ ", transferUs=" + transferNanos / 1000L
				+ ", onlineUs=" + onlineNanos / 1000L
				+ ", listenersUs=" + listenersNanos / 1000L
				+ ", adminUs=" + adminNanos / 1000L
				+ ", welcomeUs=" + welcomeNanos / 1000L
				+ ", outfitUs=" + outfitNanos / 1000L + "]");
	}
}
