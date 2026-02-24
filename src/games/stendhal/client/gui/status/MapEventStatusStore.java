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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.client.StendhalClient;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;

/**
 * Stores map event snapshots and exposes validated data for UI.
 */
public final class MapEventStatusStore {
	private static final int STALE_GRACE_SECONDS = 3;
	private static final MapEventStatusStore INSTANCE = new MapEventStatusStore();

	private final Map<String, CachedMapEventStatus> byEventId = new LinkedHashMap<String, CachedMapEventStatus>();

	private MapEventStatusStore() {
	}

	public static MapEventStatusStore get() {
		return INSTANCE;
	}

	public synchronized void updateStatus(final String eventId, final String eventName, final boolean isActive,
			final Integer remainingSeconds, final Integer totalSeconds,
			final Integer eventTotalSpawnedCreatures, final Integer eventDefeatedCreatures,
			final Integer eventDefeatPercent, final Integer currentWave, final Integer totalWaves,
			final String defenseStatus, final List<String> activityTop, final List<String> zones,
			final List<ActiveMapEventStatus.CapturePointStatus> capturePoints) {
		if ((eventId == null) || eventId.trim().isEmpty()) {
			return;
		}
		final long nowMillis = System.currentTimeMillis();
		if (!isActive) {
			byEventId.put(eventId, CachedMapEventStatus.inactive(eventId, nowMillis));
			return;
		}
		if ((remainingSeconds == null) || (totalSeconds == null) || (zones == null)
				|| (eventTotalSpawnedCreatures == null) || (eventDefeatedCreatures == null)
				|| (eventDefeatPercent == null) || (currentWave == null) || (totalWaves == null)) {
			return;
		}
		final List<ActiveMapEventStatus.CapturePointStatus> validatedCapturePoints =
				validateCapturePoints(capturePoints, zones);
		final ActiveMapEventStatus mapped = new ActiveMapEventStatus(eventId, eventName, remainingSeconds.intValue(),
				totalSeconds.intValue(), eventTotalSpawnedCreatures.intValue(), eventDefeatedCreatures.intValue(),
				eventDefeatPercent.intValue(), currentWave.intValue(), totalWaves.intValue(), defenseStatus,
				activityTop, zones, validatedCapturePoints);
		byEventId.put(eventId, CachedMapEventStatus.active(mapped, nowMillis));
	}

	public synchronized ActiveMapEventStatus getVisibleStatusForZone(final String zoneName) {
		if (zoneName == null || zoneName.isEmpty()) {
			return null;
		}
		final long nowMillis = System.currentTimeMillis();
		for (CachedMapEventStatus cached : byEventId.values()) {
			if (!cached.isActive()) {
				continue;
			}
			final ActiveMapEventStatus status = cached.getStatusAt(nowMillis);
			if (!status.getZones().contains(zoneName)) {
				continue;
			}
			if (!isValid(status, cached, nowMillis)) {
				continue;
			}
			return status;
		}
		return null;
	}

	public synchronized Map<String, ActiveMapEventStatus> getSnapshotByEventId() {
		final Map<String, ActiveMapEventStatus> snapshot = new LinkedHashMap<String, ActiveMapEventStatus>();
		for (Map.Entry<String, CachedMapEventStatus> entry : byEventId.entrySet()) {
			if (entry.getValue().isActive()) {
				snapshot.put(entry.getKey(), entry.getValue().getStatus());
			}
		}
		return Collections.unmodifiableMap(snapshot);
	}

	public void requestSnapshotRefresh() {
		final StendhalClient client = StendhalClient.get();
		if (client == null) {
			return;
		}
		final RPAction action = new RPAction();
		action.put(Actions.TYPE, Actions.MAP_EVENT_STATUS_SNAPSHOT);
		client.send(action);
	}

	private boolean isValid(final ActiveMapEventStatus status, final CachedMapEventStatus cached, final long nowMillis) {
		if ((status.getEventId() == null) || status.getEventId().trim().isEmpty()) {
			return false;
		}
		if ((status.getEventName() == null) || status.getEventName().trim().isEmpty()) {
			return false;
		}
		if (status.getZones().isEmpty()) {
			return false;
		}
		if (status.getTotalSeconds() <= 0) {
			return false;
		}
		if ((status.getRemainingSeconds() < 0) || (status.getRemainingSeconds() > status.getTotalSeconds())) {
			return false;
		}
		for (ActiveMapEventStatus.CapturePointStatus capturePoint : status.getCapturePoints()) {
			if (!isCapturePointValid(capturePoint, status.getZones())) {
				return false;
			}
		}
		final long staleAfterMillis = cached.getReceivedAtMillis() + ((long) (status.getRemainingSeconds() + STALE_GRACE_SECONDS) * 1000L);
		return nowMillis <= staleAfterMillis;
	}

	private List<ActiveMapEventStatus.CapturePointStatus> validateCapturePoints(
			final List<ActiveMapEventStatus.CapturePointStatus> capturePoints,
			final List<String> zones) {
		if (capturePoints == null || capturePoints.isEmpty()) {
			return Collections.emptyList();
		}
		final Set<String> seenPointIds = new LinkedHashSet<String>();
		final List<ActiveMapEventStatus.CapturePointStatus> validated = new ArrayList<ActiveMapEventStatus.CapturePointStatus>();
		for (ActiveMapEventStatus.CapturePointStatus capturePoint : capturePoints) {
			if (capturePoint == null) {
				continue;
			}
			if (!isCapturePointValid(capturePoint, zones)) {
				continue;
			}
			if (!seenPointIds.add(capturePoint.getPointId())) {
				continue;
			}
			validated.add(capturePoint);
		}
		if (validated.isEmpty()) {
			return Collections.emptyList();
		}
		return Collections.unmodifiableList(validated);
	}

	private boolean isCapturePointValid(final ActiveMapEventStatus.CapturePointStatus capturePoint, final List<String> zones) {
		if ((capturePoint.getPointId() == null) || capturePoint.getPointId().trim().isEmpty()) {
			return false;
		}
		if ((capturePoint.getZone() == null) || !zones.contains(capturePoint.getZone())) {
			return false;
		}
		if (capturePoint.getRadiusTiles() <= 0) {
			return false;
		}
		if ((capturePoint.getProgressPercent() < 0) || (capturePoint.getProgressPercent() > 100)) {
			return false;
		}
		if (capturePoint.getRemainingBossWaves() < 0) {
			return false;
		}
		return true;
	}

	private static final class CachedMapEventStatus {
		private final ActiveMapEventStatus status;
		private final long receivedAtMillis;

		private CachedMapEventStatus(final ActiveMapEventStatus status, final long receivedAtMillis) {
			this.status = status;
			this.receivedAtMillis = receivedAtMillis;
		}

		static CachedMapEventStatus active(final ActiveMapEventStatus status, final long receivedAtMillis) {
			return new CachedMapEventStatus(status, receivedAtMillis);
		}

		static CachedMapEventStatus inactive(final String eventId, final long receivedAtMillis) {
			return new CachedMapEventStatus(new ActiveMapEventStatus(eventId, "", 0, 0, 0, 0, 0,
					0, 0, "", Collections.<String>emptyList(), Collections.<String>emptyList(),
					Collections.<ActiveMapEventStatus.CapturePointStatus>emptyList()),
					receivedAtMillis);
		}

		boolean isActive() {
			return status.getTotalSeconds() > 0;
		}

		ActiveMapEventStatus getStatus() {
			return status;
		}

		ActiveMapEventStatus getStatusAt(final long nowMillis) {
			if (!isActive()) {
				return status;
			}
			final long elapsedMillis = Math.max(0L, nowMillis - receivedAtMillis);
			final int elapsedSeconds = (int) (elapsedMillis / 1000L);
			final int projectedRemaining = Math.max(0, status.getRemainingSeconds() - elapsedSeconds);
			if (projectedRemaining == status.getRemainingSeconds()) {
				return status;
			}
			return new ActiveMapEventStatus(status.getEventId(), status.getEventName(), projectedRemaining,
					status.getTotalSeconds(), status.getEventTotalSpawnedCreatures(),
					status.getEventDefeatedCreatures(), status.getEventDefeatPercent(),
					status.getCurrentWave(), status.getTotalWaves(), status.getDefenseStatus(),
					status.getActivityTop(), status.getZones(), status.getCapturePoints());
		}

		long getReceivedAtMillis() {
			return receivedAtMillis;
		}
	}
}
