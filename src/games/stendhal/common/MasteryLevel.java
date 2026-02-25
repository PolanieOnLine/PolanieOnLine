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

/** Utility class for mastery levels based on long experience thresholds. */
public final class MasteryLevel {

	public static final int MAX_MASTERY_LEVEL = 2000;

	private static final long[] xp;

	static {
		xp = new long[MAX_MASTERY_LEVEL + 1];
		xp[0] = 0L;

		for (int level = 1; level <= MAX_MASTERY_LEVEL; level++) {
			long increment = stageIncrement(level);

			if ((level % 100) == 0) {
				increment += milestoneOffset(level);
			}

			xp[level] = xp[level - 1] + increment;
		}
	}

	private MasteryLevel() {
		// Utility class.
	}

	private static long stageIncrement(final int level) {
		if (level <= 100) {
			// Early game: faster progression.
			return 100L + (level * 15L);
		}
		if (level <= 800) {
			// Mid game: stable progression.
			return 1700L + ((level - 100L) * 35L);
		}
		// Late game: visibly slower progression.
		return 26200L + ((level - 800L) * 65L);
	}

	private static long milestoneOffset(final int level) {
		return level * 250L;
	}

	/**
	 * gets the highest mastery level
	 *
	 * @return highest mastery level
	 */
	public static int maxLevel() {
		return MAX_MASTERY_LEVEL;
	}

	/**
	 * Calculates mastery level according to experience.
	 *
	 * @param experience experience points
	 * @return mastery level
	 */
	public static int getLevel(final long experience) {
		int first = 0;
		int last = MAX_MASTERY_LEVEL;
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
