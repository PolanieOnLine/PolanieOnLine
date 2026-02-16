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
package games.stendhal.server.maps.event;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks player contribution dimensions for map event reward evaluation.
 */
public class MapEventContributionTracker {
	private final Map<String, ContributionSnapshot> contributionByPlayer = new LinkedHashMap<>();

	public synchronized void clear() {
		contributionByPlayer.clear();
	}

	public synchronized void recordDamage(final String playerName, final int damage) {
		if (damage <= 0) {
			return;
		}
		ensurePlayer(playerName).damage += damage;
	}

	public synchronized void recordKillAssist(final String playerName, final int amount) {
		if (amount <= 0) {
			return;
		}
		ensurePlayer(playerName).killAssists += amount;
	}

	public synchronized void recordObjectiveAction(final String playerName, final int amount) {
		if (amount <= 0) {
			return;
		}
		ensurePlayer(playerName).objectiveActions += amount;
	}

	public synchronized void recordTimeInZone(final String playerName, final int seconds) {
		if (seconds <= 0) {
			return;
		}
		ensurePlayer(playerName).timeInZoneSeconds += seconds;
	}

	public synchronized Map<String, ContributionSnapshot> snapshotAll() {
		final Map<String, ContributionSnapshot> copy = new LinkedHashMap<>();
		for (Map.Entry<String, ContributionSnapshot> entry : contributionByPlayer.entrySet()) {
			copy.put(entry.getKey(), entry.getValue().immutableCopy());
		}
		return Collections.unmodifiableMap(copy);
	}

	private ContributionSnapshot ensurePlayer(final String playerName) {
		final String key = Objects.requireNonNull(playerName, "playerName").trim();
		if (key.isEmpty()) {
			throw new IllegalArgumentException("playerName must not be blank");
		}
		ContributionSnapshot existing = contributionByPlayer.get(key);
		if (existing == null) {
			existing = new ContributionSnapshot();
			contributionByPlayer.put(key, existing);
		}
		return existing;
	}

	public static final class ContributionSnapshot {
		private int damage;
		private int killAssists;
		private int objectiveActions;
		private int timeInZoneSeconds;

		public int getDamage() {
			return damage;
		}

		public int getKillAssists() {
			return killAssists;
		}

		public int getObjectiveActions() {
			return objectiveActions;
		}

		public int getTimeInZoneSeconds() {
			return timeInZoneSeconds;
		}

		private ContributionSnapshot immutableCopy() {
			final ContributionSnapshot copy = new ContributionSnapshot();
			copy.damage = damage;
			copy.killAssists = killAssists;
			copy.objectiveActions = objectiveActions;
			copy.timeInZoneSeconds = timeInZoneSeconds;
			return copy;
		}
	}
}
