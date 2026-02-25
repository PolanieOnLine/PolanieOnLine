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

import org.apache.log4j.Logger;

/** Utility class for mastery levels based on long experience thresholds. */
public final class MasteryLevel {

	public static final int DEFAULT_MAX_MASTERY_LEVEL = 2000;

	/**
	 * @deprecated use {@link #maxLevel()} so runtime config is respected.
	 */
	@Deprecated
	public static final int MAX_MASTERY_LEVEL = DEFAULT_MAX_MASTERY_LEVEL;

	private static final Logger logger = Logger.getLogger(MasteryLevel.class);
	private static final MasteryLevelConfig.LoadedConfig loadedConfig;
	private static final MasteryLevelConfig config;

	private static final long[] xp;

	static {
		loadedConfig = MasteryLevelConfig.load();
		config = loadedConfig.getConfig();

		xp = new long[config.getMaxLevel() + 1];
		xp[0] = 0L;

		for (int level = 1; level <= config.getMaxLevel(); level++) {
			long increment = stageIncrement(level);

			if ((level % config.getMilestoneInterval()) == 0) {
				increment += milestoneOffset(level);
			}

			xp[level] = xp[level - 1] + increment;
			if ((config.getHardCapXP() > 0L) && (xp[level] > config.getHardCapXP())) {
				xp[level] = config.getHardCapXP();
			}
		}
	}

	private MasteryLevel() {
		// Utility class.
	}

	private static long stageIncrement(final int level) {
		if (level <= config.getEarlyMaxLevel()) {
			return config.getEarlyBaseIncrement() + (level * config.getEarlyLevelIncrement());
		}
		if (level <= config.getMidMaxLevel()) {
			return config.getMidBaseIncrement() + ((level - config.getEarlyMaxLevel()) * config.getMidLevelIncrement());
		}
		return config.getLateBaseIncrement() + ((level - config.getMidMaxLevel()) * config.getLateLevelIncrement());
	}

	private static long milestoneOffset(final int level) {
		return level * config.getMilestoneMultiplier();
	}

	/**
	 * Validate mastery pacing during server startup.
	 *
	 * @return {@code true} when custom config is valid, {@code false} when defaults were used
	 */
	public static boolean validateConfigurationOnStartup() {
		if (!loadedConfig.isValid()) {
			logger.error("Mastery pacing configuration invalid. Running with safe defaults. Source: "
					+ loadedConfig.getSourcePath());
			return false;
		}
		return true;
	}

	/**
	 * gets the highest mastery level
	 *
	 * @return highest mastery level
	 */
	public static int maxLevel() {
		return config.getMaxLevel();
	}

	/**
	 * Calculates mastery level according to experience.
	 *
	 * @param experience experience points
	 * @return mastery level
	 */
	public static int getLevel(final long experience) {
		int first = 0;
		int last = config.getMaxLevel();
		if (experience <= xp[first]) {
			return first;
		}
		if (experience >= xp[last]) {
			return last;
		}
		while (last - first > 1) {
			final int current = first + ((last - first) / 2);
			if (experience < xp[current]) {
				last = current;
			} else {
				first = current;
			}
		}
		return first;
	}

	/**
	 * Calculates the experience needed for a mastery level.
	 *
	 * @param level mastery level
	 * @return experience threshold, or -1 if out of range
	 */
	public static long getXP(final int level) {
		if ((level >= 0) && (level < xp.length)) {
			return xp[level];
		}
		return -1L;
	}
}
