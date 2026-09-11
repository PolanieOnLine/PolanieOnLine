/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.client;

/**
 * Smooths the camera in native map pixel coordinates.
 *
 * The smoothing uses elapsed time instead of a fixed amount per frame. This
 * keeps camera movement consistent at different frame rates. Integer pixel
 * coordinates remain available for legacy screen logic while rendering can
 * use the full fractional position.
 */
final class CameraSmoother {
	private static final double RESPONSE_PER_SECOND = 18.0;
	private static final double MAX_DELTA_MILLIS = 100.0;
	private static final double SNAP_DISTANCE = 0.15;

	private double x;
	private double y;
	private double targetX;
	private double targetY;

	synchronized void reset(final double x, final double y) {
		this.x = x;
		this.y = y;
		targetX = x;
		targetY = y;
	}

	synchronized void setTarget(final double x, final double y) {
		targetX = x;
		targetY = y;
	}

	synchronized void update(final double deltaMillis) {
		if (isSettled()) {
			return;
		}

		final double boundedDelta = Math.max(0.0, Math.min(MAX_DELTA_MILLIS, deltaMillis));
		if (boundedDelta == 0.0) {
			return;
		}

		final double alpha = 1.0 - Math.exp(-RESPONSE_PER_SECOND * boundedDelta / 1000.0);
		x += (targetX - x) * alpha;
		y += (targetY - y) * alpha;

		if (Math.abs(targetX - x) <= SNAP_DISTANCE) {
			x = targetX;
		}
		if (Math.abs(targetY - y) <= SNAP_DISTANCE) {
			y = targetY;
		}
	}

	synchronized void snapToTarget() {
		x = targetX;
		y = targetY;
	}

	synchronized boolean isSettled() {
		return (x == targetX) && (y == targetY);
	}

	synchronized double getX() {
		return x;
	}

	synchronized double getY() {
		return y;
	}

	synchronized int getPixelX() {
		return (int) Math.round(x);
	}

	synchronized int getPixelY() {
		return (int) Math.round(y);
	}
}
