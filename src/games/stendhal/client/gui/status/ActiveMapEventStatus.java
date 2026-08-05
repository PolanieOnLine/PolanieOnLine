/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.status;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable UI model of active map event status.
 */
public final class ActiveMapEventStatus {
	private final String eventId;
	private final String eventName;
	private final int remainingSeconds;
	private final int totalSeconds;
	private final int eventTotalSpawnedCreatures;
	private final int eventDefeatedCreatures;
	private final int eventDefeatPercent;
	private final int currentWave;
	private final int totalWaves;
	private final String defenseStatus;
	private final String phaseName;
	private final String phaseDescription;
	private final String modifierName;
	private final String modifierDescription;
	private final List<SecondaryObjectiveStatus> secondaryObjectives;
	private final int rewardBonusPercent;
	private final List<String> activityTop;
	private final List<String> zones;
	private final List<CapturePointStatus> capturePoints;

	public ActiveMapEventStatus(final String eventId, final String eventName,
			final int remainingSeconds, final int totalSeconds,
			final int eventTotalSpawnedCreatures, final int eventDefeatedCreatures,
			final int eventDefeatPercent, final int currentWave, final int totalWaves,
			final String defenseStatus, final String phaseName, final String phaseDescription,
			final String modifierName, final String modifierDescription,
			final List<SecondaryObjectiveStatus> secondaryObjectives, final int rewardBonusPercent,
			final List<String> activityTop, final List<String> zones,
			final List<CapturePointStatus> capturePoints) {
		this.eventId = eventId;
		this.eventName = eventName;
		this.remainingSeconds = remainingSeconds;
		this.totalSeconds = totalSeconds;
		this.eventTotalSpawnedCreatures = Math.max(0, eventTotalSpawnedCreatures);
		this.eventDefeatedCreatures = Math.max(0, eventDefeatedCreatures);
		this.eventDefeatPercent = Math.max(0, Math.min(100, eventDefeatPercent));
		this.currentWave = Math.max(0, currentWave);
		this.totalWaves = Math.max(0, totalWaves);
		this.defenseStatus = (defenseStatus == null) ? "" : defenseStatus;
		this.phaseName = (phaseName == null) ? "" : phaseName;
		this.phaseDescription = (phaseDescription == null) ? "" : phaseDescription;
		this.modifierName = (modifierName == null) ? "" : modifierName;
		this.modifierDescription = (modifierDescription == null) ? "" : modifierDescription;
		this.secondaryObjectives = Collections.unmodifiableList(new ArrayList<SecondaryObjectiveStatus>(secondaryObjectives));
		this.rewardBonusPercent = Math.max(0, rewardBonusPercent);
		this.activityTop = Collections.unmodifiableList(new ArrayList<String>(activityTop));
		this.zones = Collections.unmodifiableList(new ArrayList<String>(zones));
		this.capturePoints = Collections.unmodifiableList(new ArrayList<CapturePointStatus>(capturePoints));
	}

	public String getEventId() {
		return eventId;
	}

	public String getEventName() {
		return eventName;
	}

	public int getRemainingSeconds() {
		return remainingSeconds;
	}

	public int getTotalSeconds() {
		return totalSeconds;
	}

	public int getEventTotalSpawnedCreatures() {
		return eventTotalSpawnedCreatures;
	}

	public int getEventDefeatedCreatures() {
		return eventDefeatedCreatures;
	}

	public int getEventDefeatPercent() {
		return eventDefeatPercent;
	}


	public int getCurrentWave() {
		return currentWave;
	}

	public int getTotalWaves() {
		return totalWaves;
	}

	public String getDefenseStatus() {
		return defenseStatus;
	}

	public String getPhaseName() {
		return phaseName;
	}

	public String getPhaseDescription() {
		return phaseDescription;
	}

	public String getModifierName() {
		return modifierName;
	}

	public String getModifierDescription() {
		return modifierDescription;
	}

	public List<SecondaryObjectiveStatus> getSecondaryObjectives() {
		return secondaryObjectives;
	}

	public int getRewardBonusPercent() {
		return rewardBonusPercent;
	}

	public List<String> getActivityTop() {
		return activityTop;
	}

	public List<String> getZones() {
		return zones;
	}

	public List<CapturePointStatus> getCapturePoints() {
		return capturePoints;
	}

	public CapturePointStatus findNearestCapturePoint(final String playerZone, final double playerX,
			final double playerY) {
		if (capturePoints.isEmpty()) {
			return null;
		}
		CapturePointStatus nearestInZone = null;
		double nearestInZoneDistance = Double.MAX_VALUE;
		CapturePointStatus nearestAny = null;
		double nearestAnyDistance = Double.MAX_VALUE;
		for (CapturePointStatus capturePoint : capturePoints) {
			final double distanceSquared = squaredDistance(playerX, playerY, capturePoint.x, capturePoint.y);
			if (distanceSquared < nearestAnyDistance) {
				nearestAny = capturePoint;
				nearestAnyDistance = distanceSquared;
			}
			if (!isSameZone(playerZone, capturePoint.zone)) {
				continue;
			}
			if (distanceSquared < nearestInZoneDistance) {
				nearestInZone = capturePoint;
				nearestInZoneDistance = distanceSquared;
			}
		}
		return (nearestInZone != null) ? nearestInZone : nearestAny;
	}

	public static boolean isInsideCapturePoint(final String playerZone, final double playerX,
			final double playerY, final CapturePointStatus point) {
		if (point == null || !isSameZone(playerZone, point.zone)) {
			return false;
		}
		final int radius = Math.max(0, point.radiusTiles);
		final double radiusSquared = radius * radius;
		return squaredDistance(playerX, playerY, point.x, point.y) <= radiusSquared;
	}

	private static boolean isSameZone(final String left, final String right) {
		if (left == null || right == null) {
			return false;
		}
		return left.equalsIgnoreCase(right);
	}

	private static double squaredDistance(final double fromX, final double fromY, final double toX,
			final double toY) {
		final double dx = fromX - toX;
		final double dy = fromY - toY;
		return (dx * dx) + (dy * dy);
	}

	public int getProgressPercent() {
		if (totalSeconds <= 0) {
			return 0;
		}
		final int elapsed = Math.max(0, totalSeconds - remainingSeconds);
		return Math.min(100, (int) Math.round(((double) elapsed / (double) totalSeconds) * 100.0d));
	}

	public static final class CapturePointStatus {
		private final String pointId;
		private final String zone;
		private final int x;
		private final int y;
		private final int radiusTiles;
		private final int progressPercent;
		private final String ownerFaction;
		private final boolean contested;
		private final int remainingBossWaves;

		public CapturePointStatus(final String pointId, final String zone, final int x, final int y,
				final int radiusTiles, final int progressPercent, final String ownerFaction,
				final boolean contested, final int remainingBossWaves) {
			this.pointId = pointId;
			this.zone = zone;
			this.x = x;
			this.y = y;
			this.radiusTiles = radiusTiles;
			this.progressPercent = progressPercent;
			this.ownerFaction = ownerFaction;
			this.contested = contested;
			this.remainingBossWaves = remainingBossWaves;
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
			return progressPercent;
		}

		public String getOwnerFaction() {
			return ownerFaction;
		}

		public boolean isContested() {
			return contested;
		}

		public int getRemainingBossWaves() {
			return remainingBossWaves;
		}
	}

	public static final class SecondaryObjectiveStatus {
		private final String objectiveId;
		private final String title;
		private final String details;
		private final List<String> trackedTargetLabels;
		private final String rewardType;
		private final String rewardItemName;
		private final String rewardDescription;
		private final String state;
		private final int startWave;
		private final int endWave;
		private final int progress;
		private final int target;

		public SecondaryObjectiveStatus(final String objectiveId, final String title,
				final String details, final List<String> trackedTargetLabels, final String rewardType,
				final String rewardItemName, final String rewardDescription,
				final String state, final int startWave, final int endWave, final int progress, final int target) {
			this.objectiveId = objectiveId;
			this.title = title;
			this.details = details;
			this.trackedTargetLabels = Collections.unmodifiableList(new ArrayList<String>(trackedTargetLabels));
			this.rewardType = rewardType;
			this.rewardItemName = rewardItemName;
			this.rewardDescription = rewardDescription;
			this.state = state;
			this.startWave = startWave;
			this.endWave = endWave;
			this.progress = progress;
			this.target = target;
		}

		public String getObjectiveId() {
			return objectiveId;
		}

		public String getTitle() {
			return title;
		}

		public String getDetails() {
			return details;
		}

		public List<String> getTrackedTargetLabels() {
			return trackedTargetLabels;
		}

		public String getRewardType() {
			return rewardType;
		}

		public String getRewardItemName() {
			return rewardItemName;
		}

		public String getRewardDescription() {
			return rewardDescription;
		}

		public String getState() {
			return state;
		}

		public int getStartWave() {
			return startWave;
		}

		public int getEndWave() {
			return endWave;
		}

		public int getProgress() {
			return progress;
		}

		public int getTarget() {
			return target;
		}
	}
}
