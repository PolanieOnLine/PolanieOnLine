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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Fires configured callbacks once per capture point whenever a progress threshold
 * is reached.
 */
public class CaptureProgressTrigger {
	private static final Logger LOGGER = Logger.getLogger(CaptureProgressTrigger.class);

	private final List<MapEventConfig.CaptureProgressWaveConfig> waves;
	private final Map<String, Set<Integer>> firedThresholdsByPoint = new HashMap<>();

	public CaptureProgressTrigger(final Collection<MapEventConfig.CaptureProgressWaveConfig> waves) {
		final List<MapEventConfig.CaptureProgressWaveConfig> orderedWaves = new ArrayList<>(
				Objects.requireNonNull(waves, "waves"));
		Collections.sort(orderedWaves, Comparator.comparingInt(MapEventConfig.CaptureProgressWaveConfig::getThresholdPercent));
		this.waves = Collections.unmodifiableList(orderedWaves);
	}

	public boolean isEnabled() {
		return !waves.isEmpty();
	}

	public void reset(final String reason) {
		firedThresholdsByPoint.clear();
		LOGGER.debug("Capture progress trigger reset (" + reason + ").");
	}

	public void evaluate(final CapturePointState capturePoint, final Listener listener) {
		Objects.requireNonNull(capturePoint, "capturePoint");
		Objects.requireNonNull(listener, "listener");
		if (waves.isEmpty()) {
			return;
		}
		final String pointId = capturePoint.getPointId();
		Set<Integer> firedThresholds = firedThresholdsByPoint.get(pointId);
		if (firedThresholds == null) {
			firedThresholds = new HashSet<>();
			firedThresholdsByPoint.put(pointId, firedThresholds);
		}
		final int progressPercent = capturePoint.getProgressPercent();
		for (MapEventConfig.CaptureProgressWaveConfig wave : waves) {
			final int threshold = wave.getThresholdPercent();
			if (progressPercent < threshold || firedThresholds.contains(Integer.valueOf(threshold))) {
				continue;
			}
			firedThresholds.add(Integer.valueOf(threshold));
			listener.onThresholdReached(capturePoint, wave);
		}
	}

	public interface Listener {
		void onThresholdReached(CapturePointState capturePoint, MapEventConfig.CaptureProgressWaveConfig wave);
	}
}
