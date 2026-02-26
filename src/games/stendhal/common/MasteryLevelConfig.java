/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

/** Configuration loader for mastery progression pacing. */
public final class MasteryLevelConfig {

	public static final String CONFIG_PATH_PROPERTY = "stendhal.mastery.config.path";
	public static final String DEFAULT_CONFIG_PATH = "data/conf/mastery-level.properties";

	private static final Logger logger = Logger.getLogger(MasteryLevelConfig.class);

	private final int maxLevel;
	private final int earlyMaxLevel;
	private final long earlyBaseIncrement;
	private final long earlyLevelIncrement;
	private final int midMaxLevel;
	private final long midBaseIncrement;
	private final long midLevelIncrement;
	private final long lateBaseIncrement;
	private final long lateLevelIncrement;
	private final double globalMultiplier;
	private final int milestoneInterval;
	private final long milestoneMultiplier;
	private final long hardCapXP;

	private MasteryLevelConfig(final int maxLevel, final int earlyMaxLevel,
			final long earlyBaseIncrement, final long earlyLevelIncrement,
			final int midMaxLevel, final long midBaseIncrement,
			final long midLevelIncrement, final long lateBaseIncrement,
			final long lateLevelIncrement, final double globalMultiplier,
			final int milestoneInterval,
			final long milestoneMultiplier, final long hardCapXP) {
		this.maxLevel = maxLevel;
		this.earlyMaxLevel = earlyMaxLevel;
		this.earlyBaseIncrement = earlyBaseIncrement;
		this.earlyLevelIncrement = earlyLevelIncrement;
		this.midMaxLevel = midMaxLevel;
		this.midBaseIncrement = midBaseIncrement;
		this.midLevelIncrement = midLevelIncrement;
		this.lateBaseIncrement = lateBaseIncrement;
		this.lateLevelIncrement = lateLevelIncrement;
		this.globalMultiplier = globalMultiplier;
		this.milestoneInterval = milestoneInterval;
		this.milestoneMultiplier = milestoneMultiplier;
		this.hardCapXP = hardCapXP;
	}

	public static LoadedConfig load() {
		final MasteryLevelConfig defaults = defaults();
		final String configPath = System.getProperty(CONFIG_PATH_PROPERTY, DEFAULT_CONFIG_PATH);

		final Properties properties = new Properties();
		try (InputStream in = new FileInputStream(configPath)) {
			properties.load(in);
		} catch (IOException e) {
			logger.error("Cannot load mastery config from: " + configPath + ". Using safe defaults.", e);
			return new LoadedConfig(defaults, false, configPath);
		}

		try {
			final MasteryLevelConfig configured = fromProperties(properties);
			logger.info("Mastery pacing loaded from " + configPath + " (max level: " + configured.maxLevel + ").");
			return new LoadedConfig(configured, true, configPath);
		} catch (IllegalArgumentException e) {
			logger.error("Invalid mastery config in: " + configPath + ". Using safe defaults. " + e.getMessage());
			return new LoadedConfig(defaults, false, configPath);
		}
	}

	private static MasteryLevelConfig fromProperties(final Properties properties) {
		final int maxLevel = parseInt(properties, "mastery.maxLevel", 1, Integer.MAX_VALUE);
		final int earlyMaxLevel = parseInt(properties, "mastery.early.maxLevel", 1, Integer.MAX_VALUE);
		final long earlyBaseIncrement = parseLong(properties, "mastery.early.baseIncrement", 1L, Long.MAX_VALUE);
		final long earlyLevelIncrement = parseLong(properties, "mastery.early.levelIncrement", 0L, Long.MAX_VALUE);
		final int midMaxLevel = parseInt(properties, "mastery.mid.maxLevel", 1, Integer.MAX_VALUE);
		final long midBaseIncrement = parseLong(properties, "mastery.mid.baseIncrement", 1L, Long.MAX_VALUE);
		final long midLevelIncrement = parseLong(properties, "mastery.mid.levelIncrement", 0L, Long.MAX_VALUE);
		final long lateBaseIncrement = parseLong(properties, "mastery.late.baseIncrement", 1L, Long.MAX_VALUE);
		final long lateLevelIncrement = parseLong(properties, "mastery.late.levelIncrement", 0L, Long.MAX_VALUE);
		final double globalMultiplier = parseDouble(properties, "mastery.globalMultiplier", 0.01d, 1000d);
		final int milestoneInterval = parseInt(properties, "mastery.milestone.interval", 1, Integer.MAX_VALUE);
		final long milestoneMultiplier = parseLong(properties, "mastery.milestone.multiplier", 0L, Long.MAX_VALUE);
		final long hardCapXP = parseOptionalLong(properties, "mastery.hardCapXP", -1L, Long.MAX_VALUE, -1L);

		if (earlyMaxLevel >= maxLevel) {
			throw new IllegalArgumentException("mastery.early.maxLevel must be below mastery.maxLevel");
		}
		if ((midMaxLevel <= earlyMaxLevel) || (midMaxLevel >= maxLevel)) {
			throw new IllegalArgumentException("mastery.mid.maxLevel must be between mastery.early.maxLevel and mastery.maxLevel");
		}
		validateMonotonicMinimumIncrement(maxLevel, earlyMaxLevel, earlyBaseIncrement,
				earlyLevelIncrement, midMaxLevel, midBaseIncrement,
				midLevelIncrement, lateBaseIncrement, lateLevelIncrement,
				globalMultiplier);

		return new MasteryLevelConfig(maxLevel, earlyMaxLevel, earlyBaseIncrement,
				earlyLevelIncrement, midMaxLevel, midBaseIncrement,
				midLevelIncrement, lateBaseIncrement, lateLevelIncrement,
				globalMultiplier,
				milestoneInterval, milestoneMultiplier, hardCapXP);
	}

	private static int parseInt(final Properties properties, final String key,
			final int minValue, final int maxValue) {
		final String value = required(properties, key);
		try {
			final int parsed = Integer.parseInt(value.trim());
			if ((parsed < minValue) || (parsed > maxValue)) {
				throw new IllegalArgumentException("Property out of range: " + key);
			}
			return parsed;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid integer in property: " + key);
		}
	}

	private static long parseLong(final Properties properties, final String key,
			final long minValue, final long maxValue) {
		final String value = required(properties, key);
		return parseLongValue(key, value, minValue, maxValue);
	}

	private static long parseOptionalLong(final Properties properties, final String key,
			final long minValue, final long maxValue, final long fallbackValue) {
		final String value = properties.getProperty(key);
		if (value == null || value.trim().isEmpty()) {
			return fallbackValue;
		}
		return parseLongValue(key, value, minValue, maxValue);
	}

	private static double parseDouble(final Properties properties, final String key,
			final double minValue, final double maxValue) {
		final String value = required(properties, key);
		try {
			final double parsed = Double.parseDouble(value.trim());
			if ((parsed < minValue) || (parsed > maxValue)) {
				throw new IllegalArgumentException("Property out of range: " + key);
			}
			return parsed;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid double in property: " + key);
		}
	}

	private static long parseLongValue(final String key, final String value,
			final long minValue, final long maxValue) {
		try {
			final long parsed = Long.parseLong(value.trim());
			if ((parsed < minValue) || (parsed > maxValue)) {
				throw new IllegalArgumentException("Property out of range: " + key);
			}
			return parsed;
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Invalid long in property: " + key);
		}
	}

	private static String required(final Properties properties, final String key) {
		final String value = properties.getProperty(key);
		if ((value == null) || value.trim().isEmpty()) {
			throw new IllegalArgumentException("Missing property: " + key);
		}
		return value;
	}

	private static void validateMonotonicMinimumIncrement(final int maxLevel,
			final int earlyMaxLevel, final long earlyBaseIncrement,
			final long earlyLevelIncrement, final int midMaxLevel,
			final long midBaseIncrement, final long midLevelIncrement,
			final long lateBaseIncrement, final long lateLevelIncrement,
			final double globalMultiplier) {
		long previous = applyGlobalMultiplier(stageIncrementForLevel(1, earlyMaxLevel,
				earlyBaseIncrement, earlyLevelIncrement, midMaxLevel,
				midBaseIncrement, midLevelIncrement, lateBaseIncrement,
				lateLevelIncrement), globalMultiplier);

		for (int level = 2; level <= maxLevel; level++) {
			final long current = applyGlobalMultiplier(stageIncrementForLevel(level, earlyMaxLevel,
					earlyBaseIncrement, earlyLevelIncrement, midMaxLevel,
					midBaseIncrement, midLevelIncrement, lateBaseIncrement,
					lateLevelIncrement), globalMultiplier);
			if (current < previous) {
				throw new IllegalArgumentException(
						"mastery minimal increment must be monotonic (non-decreasing), broken at level " + level);
			}
			previous = current;
		}
	}

	private static long stageIncrementForLevel(final int level, final int earlyMaxLevel,
			final long earlyBaseIncrement, final long earlyLevelIncrement,
			final int midMaxLevel, final long midBaseIncrement,
			final long midLevelIncrement, final long lateBaseIncrement,
			final long lateLevelIncrement) {
		if (level <= earlyMaxLevel) {
			return earlyBaseIncrement + (level * earlyLevelIncrement);
		}
		if (level <= midMaxLevel) {
			return midBaseIncrement + ((level - earlyMaxLevel) * midLevelIncrement);
		}
		return lateBaseIncrement + ((level - midMaxLevel) * lateLevelIncrement);
	}

	private static long applyGlobalMultiplier(final long baseIncrement,
			final double globalMultiplier) {
		return Math.max(1L, Math.round(baseIncrement * globalMultiplier));
	}

	public static MasteryLevelConfig defaults() {
		return new MasteryLevelConfig(2000, 100, 300L, 40L, 800,
				5200L, 75L, 70000L, 130L, 1.0d, 100, 250L, -1L);
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public int getEarlyMaxLevel() {
		return earlyMaxLevel;
	}

	public long getEarlyBaseIncrement() {
		return earlyBaseIncrement;
	}

	public long getEarlyLevelIncrement() {
		return earlyLevelIncrement;
	}

	public int getMidMaxLevel() {
		return midMaxLevel;
	}

	public long getMidBaseIncrement() {
		return midBaseIncrement;
	}

	public long getMidLevelIncrement() {
		return midLevelIncrement;
	}

	public long getLateBaseIncrement() {
		return lateBaseIncrement;
	}

	public long getLateLevelIncrement() {
		return lateLevelIncrement;
	}

	public double getGlobalMultiplier() {
		return globalMultiplier;
	}

	public int getMilestoneInterval() {
		return milestoneInterval;
	}

	public long getMilestoneMultiplier() {
		return milestoneMultiplier;
	}

	public long getHardCapXP() {
		return hardCapXP;
	}

	/** Loaded mastery pacing configuration result. */
	public static final class LoadedConfig {
		private final MasteryLevelConfig config;
		private final boolean valid;
		private final String sourcePath;

		private LoadedConfig(final MasteryLevelConfig config, final boolean valid,
				final String sourcePath) {
			this.config = config;
			this.valid = valid;
			this.sourcePath = sourcePath;
		}

		public MasteryLevelConfig getConfig() {
			return config;
		}

		public boolean isValid() {
			return valid;
		}

		public String getSourcePath() {
			return sourcePath;
		}
	}
}
