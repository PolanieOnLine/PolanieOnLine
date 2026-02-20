/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
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

import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;

/**
 * Shared helpers for map event configuration providers.
 */
public final class MapEventConfigSupport {
	private MapEventConfigSupport() {
		// utility class
	}

	public static BaseMapEvent.EventSpawn spawn(final String creatureName, final int count) {
		return new BaseMapEvent.EventSpawn(creatureName, count);
	}

	public static BaseMapEvent.EventWave wave(final int intervalSeconds, final BaseMapEvent.EventSpawn... spawns) {
		return new BaseMapEvent.EventWave(intervalSeconds, Arrays.asList(spawns));
	}

	public static List<BaseMapEvent.EventWave> waves(final BaseMapEvent.EventWave... waves) {
		return Arrays.asList(waves);
	}

	public static MapEventConfig.CapturePointConfig capturePoint(final String pointId, final String zone,
			final int x, final int y, final int radiusTiles) {
		return new MapEventConfig.CapturePointConfig(pointId, zone, x, y, radiusTiles);
	}

	public static MapEventConfig.CaptureProgressWaveConfig captureProgressWave(final int thresholdPercent,
			final BaseMapEvent.EventSpawn... spawns) {
		return new MapEventConfig.CaptureProgressWaveConfig(thresholdPercent, Arrays.asList(spawns));
	}

	public static List<MapEventConfig.CaptureProgressWaveConfig> captureProgressWaves(
			final MapEventConfig.CaptureProgressWaveConfig... waves) {
		return Arrays.asList(waves);
	}

	public static String validatedDefaultStartTime(final String configuredStartTime, final String context) {
		if (configuredStartTime == null || configuredStartTime.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing default start time for " + context + ".");
		}
		final String normalized = configuredStartTime.trim();
		try {
			LocalTime.parse(normalized);
		} catch (DateTimeParseException e) {
			throw new IllegalArgumentException("Invalid default start time '" + configuredStartTime
					+ "' for " + context + ". Expected ISO local time (e.g. 20:00).", e);
		}
		return normalized;
	}

	public static int validatedDefaultIntervalDays(final int configuredIntervalDays, final String context) {
		if (configuredIntervalDays <= 0) {
			throw new IllegalArgumentException("Invalid default interval days " + configuredIntervalDays + " for "
					+ context + ". Expected value > 0.");
		}
		return configuredIntervalDays;
	}
}
