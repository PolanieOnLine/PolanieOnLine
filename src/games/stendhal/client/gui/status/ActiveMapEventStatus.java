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
	private final List<String> zones;

	public ActiveMapEventStatus(final String eventId, final String eventName,
			final int remainingSeconds, final int totalSeconds, final List<String> zones) {
		this.eventId = eventId;
		this.eventName = eventName;
		this.remainingSeconds = remainingSeconds;
		this.totalSeconds = totalSeconds;
		this.zones = Collections.unmodifiableList(new ArrayList<String>(zones));
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

	public List<String> getZones() {
		return zones;
	}

	public int getProgressPercent() {
		if (totalSeconds <= 0) {
			return 0;
		}
		final int elapsed = Math.max(0, totalSeconds - remainingSeconds);
		return Math.min(100, (int) Math.round(((double) elapsed / (double) totalSeconds) * 100.0d));
	}
}
