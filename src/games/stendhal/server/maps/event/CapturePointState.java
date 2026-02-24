/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

/**
 * Runtime state of a single map-event capture point.
 */
public final class CapturePointState {
 	private static final double SOLO_BASE_PROGRESS_PER_TICK = 0.9d;
 	private static final double EXTRA_PLAYER_BONUS_PER_TICK = 0.55d;
 	private static final int ANTI_ZERG_PLAYER_CAP = 6;

 	private final String pointId;
 	private final String zone;
 	private final int x;
 	private final int y;
 	private final int radiusTiles;
 	private double progress;
 	private int activeWave;
 	private boolean completed;

 	public CapturePointState(final String pointId, final String zone, final int x, final int y,
 			final int radiusTiles) {
 		this.pointId = pointId;
 		this.zone = zone;
 		this.x = x;
 		this.y = y;
 		this.radiusTiles = Math.max(1, radiusTiles);
 	}

 	public String getPointId() {
 		return pointId;
 	}

 	public String getZone() {
 		return zone;
 	}

 	public int getX() {
 		return x;
 	}

 	public int getY() {
 		return y;
 	}

 	public int getRadiusTiles() {
 		return radiusTiles;
 	}

 	public int getProgressPercent() {
 		return (int) Math.max(0, Math.min(100, Math.round(progress)));
 	}

 	public int getActiveWave() {
 		return activeWave;
 	}

 	public boolean isCompleted() {
 		return completed;
 	}

 	public void reset() {
 		progress = 0d;
 		activeWave = 0;
 		completed = false;
 	}

 	public boolean tick(final int playersInRadius, final int currentWave) {
 		if (completed || playersInRadius <= 0) {
 			return false;
 		}
 		activeWave = Math.max(0, currentWave);
 		final int effectivePlayers = Math.max(1, Math.min(ANTI_ZERG_PLAYER_CAP, playersInRadius));
 		final double gain = SOLO_BASE_PROGRESS_PER_TICK
 				+ Math.max(0, effectivePlayers - 1) * EXTRA_PLAYER_BONUS_PER_TICK;
 		final double previous = progress;
 		progress = Math.min(100d, progress + gain);
 		if (progress >= 100d) {
 			completed = true;
 		}
 		return Double.compare(previous, progress) != 0;
 	}
}
