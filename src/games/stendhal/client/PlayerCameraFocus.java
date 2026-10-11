/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

/** Camera focus driven only by painted player positions and explicit view requests. */
final class PlayerCameraFocus {
	private double playerX;
	private double playerY;
	private boolean playerKnown;
	private double requestedX;
	private double requestedY;
	private boolean remote;

	void capturePlayer(final double x, final double y) {
		if (playerKnown && remote && (Math.abs(playerX - x) > 0.001 || Math.abs(playerY - y) > 0.001)) {
			remote = false;
		}
		playerX = x;
		playerY = y;
		playerKnown = true;
	}

	void requestView(final double x, final double y) {
		requestedX = x;
		requestedY = y;
		remote = true;
	}

	void clearRemoteView() { remote = false; }
	boolean followsPlayer() { return !remote; }
	double getX() { return remote ? requestedX : playerX; }
	double getY() { return remote ? requestedY : playerY; }
}
