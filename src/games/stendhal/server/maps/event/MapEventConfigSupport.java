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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

	public static MapEventConfig.PhaseConfig phase(final String phaseId, final String title,
			final String description, final int startWave, final String transitionAnnouncement) {
		return new MapEventConfig.PhaseConfig(phaseId, title, description, startWave,
				transitionAnnouncement, 1.0d, Collections.<String, Double>emptyMap(), null);
	}

	public static MapEventConfig.PhaseConfig phase(final String phaseId, final String title,
			final String description, final int startWave, final String transitionAnnouncement,
			final double spawnMultiplier, final Map<String, Double> zoneSpawnMultipliers,
			final String defenseStatus) {
		return new MapEventConfig.PhaseConfig(phaseId, title, description, startWave,
				transitionAnnouncement, spawnMultiplier, zoneSpawnMultipliers, defenseStatus);
	}

	public static List<MapEventConfig.PhaseConfig> phases(final MapEventConfig.PhaseConfig... phases) {
		return Arrays.asList(phases);
	}

	public static MapEventConfig.ModifierConfig modifier(final String modifierId, final String title,
			final String description, final int activationWave, final String activationAnnouncement,
			final double spawnMultiplier, final double rewardMultiplierBonus,
			final BaseMapEvent.EventSpawn... extraSpawns) {
		return new MapEventConfig.ModifierConfig(modifierId, title, description, activationWave,
				activationAnnouncement, spawnMultiplier, Collections.<String, Double>emptyMap(),
				Arrays.asList(extraSpawns), rewardMultiplierBonus);
	}

	public static MapEventConfig.ModifierConfig modifier(final String modifierId, final String title,
			final String description, final int activationWave, final String activationAnnouncement,
			final double spawnMultiplier, final Map<String, Double> zoneSpawnMultipliers,
			final double rewardMultiplierBonus, final BaseMapEvent.EventSpawn... extraSpawns) {
		return new MapEventConfig.ModifierConfig(modifierId, title, description, activationWave,
				activationAnnouncement, spawnMultiplier, zoneSpawnMultipliers,
				Arrays.asList(extraSpawns), rewardMultiplierBonus);
	}

	public static List<MapEventConfig.ModifierConfig> modifiers(final MapEventConfig.ModifierConfig... modifiers) {
		return Arrays.asList(modifiers);
	}

	public static MapEventConfig.SecondaryObjectiveConfig killTargetObjective(final String objectiveId,
			final String title, final String description, final int startWave, final int endWave,
			final int targetCount, final double rewardMultiplierBonus, final String rewardDescription,
			final String activationAnnouncement, final String completionAnnouncement,
			final String failureAnnouncement, final String... trackedCreatures) {
		return killTargetObjective(objectiveId, title, description, startWave, endWave, targetCount,
				MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
				rewardMultiplierBonus, rewardDescription, activationAnnouncement, completionAnnouncement,
				failureAnnouncement, Collections.<BaseMapEvent.EventSpawn>emptyList(),
				Collections.<BaseMapEvent.EventSpawn>emptyList(), trackedCreatures);
	}

	public static MapEventConfig.SecondaryObjectiveConfig killTargetObjective(final String objectiveId,
			final String title, final String description, final int startWave, final int endWave,
			final int targetCount, final MapEventConfig.SecondaryObjectiveConfig.RewardType rewardType,
			final String rewardItemName, final double rewardMultiplierBonus, final String rewardDescription,
			final String activationAnnouncement, final String completionAnnouncement,
			final String failureAnnouncement, final List<BaseMapEvent.EventSpawn> completionExtraSpawns,
			final List<BaseMapEvent.EventSpawn> failureExtraSpawns, final String... trackedCreatures) {
		return new MapEventConfig.SecondaryObjectiveConfig(objectiveId, title, description,
				MapEventConfig.SecondaryObjectiveConfig.ObjectiveType.KILL_TARGET,
				startWave, endWave, targetCount, 0, Arrays.asList(trackedCreatures), Arrays.asList(trackedCreatures),
				Collections.<String>emptyList(), rewardType, rewardItemName, rewardDescription,
				completionExtraSpawns, failureExtraSpawns, activationAnnouncement, completionAnnouncement,
				failureAnnouncement, rewardMultiplierBonus);
	}

	public static MapEventConfig.SecondaryObjectiveConfig captureObjective(final String objectiveId,
			final String title, final String description, final int startWave, final int endWave,
			final int targetPercent, final double rewardMultiplierBonus, final String rewardDescription,
			final String activationAnnouncement, final String completionAnnouncement,
			final String failureAnnouncement, final String... capturePointIds) {
		return captureObjective(objectiveId, title, description, startWave, endWave, targetPercent,
				MapEventConfig.SecondaryObjectiveConfig.RewardType.BONUS_GOLD_CHEST, "złota skrzynia",
				rewardMultiplierBonus, rewardDescription, activationAnnouncement, completionAnnouncement,
				failureAnnouncement, Collections.<BaseMapEvent.EventSpawn>emptyList(),
				Collections.<BaseMapEvent.EventSpawn>emptyList(), capturePointIds);
	}

	public static MapEventConfig.SecondaryObjectiveConfig captureObjective(final String objectiveId,
			final String title, final String description, final int startWave, final int endWave,
			final int targetPercent, final MapEventConfig.SecondaryObjectiveConfig.RewardType rewardType,
			final String rewardItemName, final double rewardMultiplierBonus, final String rewardDescription,
			final String activationAnnouncement, final String completionAnnouncement,
			final String failureAnnouncement, final List<BaseMapEvent.EventSpawn> completionExtraSpawns,
			final List<BaseMapEvent.EventSpawn> failureExtraSpawns, final String... capturePointIds) {
		return new MapEventConfig.SecondaryObjectiveConfig(objectiveId, title, description,
				MapEventConfig.SecondaryObjectiveConfig.ObjectiveType.CAPTURE_PROGRESS,
				startWave, endWave, 0, targetPercent, Collections.<String>emptyList(), Arrays.asList(capturePointIds),
				Arrays.asList(capturePointIds), rewardType, rewardItemName, rewardDescription,
				completionExtraSpawns, failureExtraSpawns, activationAnnouncement, completionAnnouncement,
				failureAnnouncement, rewardMultiplierBonus);
	}

	public static List<MapEventConfig.SecondaryObjectiveConfig> secondaryObjectives(
			final MapEventConfig.SecondaryObjectiveConfig... secondaryObjectives) {
		return Arrays.asList(secondaryObjectives);
	}

	public static Map<String, Double> zoneMultipliers(final Object... values) {
		final Map<String, Double> multipliers = new LinkedHashMap<>();
		for (int i = 0; i + 1 < values.length; i += 2) {
			multipliers.put(String.valueOf(values[i]), ((Number) values[i + 1]).doubleValue());
		}
		return multipliers;
	}

	public static MapEventConfig.RewardSettings rewardSettings(final String rewardType,
			final String chestEventName, final int minDefeatPercent, final double baseDifficultyMultiplier) {
		return new MapEventConfig.RewardSettings(rewardType, chestEventName,
				minDefeatPercent, baseDifficultyMultiplier);
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
