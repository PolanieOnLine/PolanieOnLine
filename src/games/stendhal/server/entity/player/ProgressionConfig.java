package games.stendhal.server.entity.player;

import java.io.IOException;

import marauroa.common.Configuration;

/**
 * Centralna konfiguracja progresji postaci.
 */
public final class ProgressionConfig {
	private static final String CFG_MASTERY_PREFIX = "mastery.progression.";

	private ProgressionConfig() {
		// utility class
	}

	/** Minimalna liczba resetów potrzebna do aktywacji ścieżki mastery. */
	public static final int MASTERY_MIN_RESETS = 5;

	/** Maksymalny poziom mastery. Po osiągnięciu limitu dalszy mastery XP nie jest naliczany. */
	public static final int MASTERY_MAX_LEVEL;

	/** Koniec przedziału "early" (włącznie). */
	public static final int MASTERY_EARLY_END_LEVEL;

	/** Koniec przedziału "mid" (włącznie). */
	public static final int MASTERY_MID_END_LEVEL;

	/** Bazowy koszt XP na poziom, przed modyfikatorami faz i wzrostem. */
	public static final long MASTERY_BASE_XP_PER_LEVEL;

	/** Liniowy wzrost kosztu XP zależny od poziomu. */
	public static final long MASTERY_LINEAR_XP_GROWTH;

	/** Kwadratowy wzrost kosztu XP zależny od poziomu (spowalnia endgame). */
	public static final double MASTERY_QUADRATIC_XP_GROWTH;

	/** Mnożnik kosztu XP na starcie (early). */
	public static final double MASTERY_EARLY_START_MULTIPLIER;

	/** Mnożnik kosztu XP na końcu early. */
	public static final double MASTERY_EARLY_END_MULTIPLIER;

	/** Dodatkowy ramping kosztu XP w mid (stabilny środek, lekki wzrost). */
	public static final double MASTERY_MID_RAMP;

	/** Bazowy mnożnik kosztu XP w late. */
	public static final double MASTERY_LATE_BASE_MULTIPLIER;

	/** Dodatkowy mnożnik narastający w late (krzywa kwadratowa). */
	public static final double MASTERY_LATE_CURVE_MULTIPLIER;

	/** Twardy limit całkowitego mastery XP, żeby uniknąć overflow. */
	public static final long MASTERY_MAX_TOTAL_XP;

	/** Cumulative XP needed for each mastery level index. */
	private static final long[] masteryCumulativeXP;

	static {
		int maxLevel = 2000;
		int earlyEnd = 250;
		int midEnd = 1400;
		long baseXp = 12_000L;
		long linearGrowth = 450L;
		double quadraticGrowth = 0.025d;
		double earlyStartMultiplier = 0.55d;
		double earlyEndMultiplier = 1.0d;
		double midRamp = 0.08d;
		double lateBaseMultiplier = 1.15d;
		double lateCurveMultiplier = 0.95d;
		long maxTotalXp = Long.MAX_VALUE;

		try {
			final Configuration cfg = Configuration.getConfiguration();
			maxLevel = Math.max(1, cfg.getInt(CFG_MASTERY_PREFIX + "max_level", maxLevel));
			earlyEnd = cfg.getInt(CFG_MASTERY_PREFIX + "early_end_level", earlyEnd);
			midEnd = cfg.getInt(CFG_MASTERY_PREFIX + "mid_end_level", midEnd);
			baseXp = Long.parseLong(cfg.get(CFG_MASTERY_PREFIX + "base_xp_per_level", String.valueOf(baseXp)));
			linearGrowth = Long.parseLong(cfg.get(CFG_MASTERY_PREFIX + "linear_xp_growth", String.valueOf(linearGrowth)));
			quadraticGrowth = Double.parseDouble(cfg.get(CFG_MASTERY_PREFIX + "quadratic_xp_growth", String.valueOf(quadraticGrowth)));
			earlyStartMultiplier = Double.parseDouble(cfg.get(CFG_MASTERY_PREFIX + "early_start_multiplier", String.valueOf(earlyStartMultiplier)));
			earlyEndMultiplier = Double.parseDouble(cfg.get(CFG_MASTERY_PREFIX + "early_end_multiplier", String.valueOf(earlyEndMultiplier)));
			midRamp = Double.parseDouble(cfg.get(CFG_MASTERY_PREFIX + "mid_ramp", String.valueOf(midRamp)));
			lateBaseMultiplier = Double.parseDouble(cfg.get(CFG_MASTERY_PREFIX + "late_base_multiplier", String.valueOf(lateBaseMultiplier)));
			lateCurveMultiplier = Double.parseDouble(cfg.get(CFG_MASTERY_PREFIX + "late_curve_multiplier", String.valueOf(lateCurveMultiplier)));
			maxTotalXp = Long.parseLong(cfg.get(CFG_MASTERY_PREFIX + "max_total_xp", String.valueOf(maxTotalXp)));
		} catch (IOException | NumberFormatException ignored) {
			// fallback to defaults
		}

		MASTERY_MAX_LEVEL = maxLevel;
		MASTERY_EARLY_END_LEVEL = clamp(earlyEnd, 1, MASTERY_MAX_LEVEL);
		MASTERY_MID_END_LEVEL = clamp(midEnd, MASTERY_EARLY_END_LEVEL + 1, MASTERY_MAX_LEVEL);
		MASTERY_BASE_XP_PER_LEVEL = Math.max(1L, baseXp);
		MASTERY_LINEAR_XP_GROWTH = Math.max(0L, linearGrowth);
		MASTERY_QUADRATIC_XP_GROWTH = Math.max(0.0d, quadraticGrowth);
		MASTERY_EARLY_START_MULTIPLIER = Math.max(0.01d, earlyStartMultiplier);
		MASTERY_EARLY_END_MULTIPLIER = Math.max(MASTERY_EARLY_START_MULTIPLIER, earlyEndMultiplier);
		MASTERY_MID_RAMP = Math.max(0.0d, midRamp);
		MASTERY_LATE_BASE_MULTIPLIER = Math.max(1.0d, lateBaseMultiplier);
		MASTERY_LATE_CURVE_MULTIPLIER = Math.max(0.0d, lateCurveMultiplier);
		MASTERY_MAX_TOTAL_XP = Math.max(1L, maxTotalXp);

		masteryCumulativeXP = buildMasteryXPTable();
	}

	public static long getMasteryXPForLevel(final int level) {
		if (level <= 0) {
			return 0L;
		}
		if (level >= MASTERY_MAX_LEVEL) {
			return masteryCumulativeXP[MASTERY_MAX_LEVEL];
		}
		return masteryCumulativeXP[level];
	}

	public static int getMasteryLevelForXP(final long totalMasteryXP) {
		if (totalMasteryXP <= 0L) {
			return 0;
		}

		int low = 0;
		int high = MASTERY_MAX_LEVEL;
		while (low < high) {
			final int mid = (low + high + 1) >>> 1;
			if (masteryCumulativeXP[mid] <= totalMasteryXP) {
				low = mid;
			} else {
				high = mid - 1;
			}
		}

		return low;
	}

	public static long getMasteryMaxXP() {
		return masteryCumulativeXP[MASTERY_MAX_LEVEL];
	}

	private static long[] buildMasteryXPTable() {
		final long[] table = new long[MASTERY_MAX_LEVEL + 1];
		long cumulative = 0L;
		for (int level = 1; level <= MASTERY_MAX_LEVEL; level++) {
			final long levelCost = getMasteryXPDeltaForLevel(level);
			cumulative = safeAdd(cumulative, levelCost, MASTERY_MAX_TOTAL_XP);
			table[level] = cumulative;
		}
		return table;
	}

	private static long getMasteryXPDeltaForLevel(final int level) {
		final double baseGrowth = MASTERY_BASE_XP_PER_LEVEL
			+ (MASTERY_LINEAR_XP_GROWTH * (double) level)
			+ (MASTERY_QUADRATIC_XP_GROWTH * level * (double) level);
		final double multiplier = getPhaseMultiplier(level);
		final double scaledCost = baseGrowth * multiplier;
		if (scaledCost <= 1.0d) {
			return 1L;
		}
		if (scaledCost >= Long.MAX_VALUE) {
			return Long.MAX_VALUE;
		}
		return Math.max(1L, Math.round(scaledCost));
	}

	private static double getPhaseMultiplier(final int level) {
		if (level <= MASTERY_EARLY_END_LEVEL) {
			final double progress = normalize(level, 1, MASTERY_EARLY_END_LEVEL);
			return lerp(MASTERY_EARLY_START_MULTIPLIER, MASTERY_EARLY_END_MULTIPLIER, progress);
		}

		if (level <= MASTERY_MID_END_LEVEL) {
			final double progress = normalize(level, MASTERY_EARLY_END_LEVEL + 1, MASTERY_MID_END_LEVEL);
			return MASTERY_EARLY_END_MULTIPLIER + (MASTERY_MID_RAMP * progress);
		}

		final double progress = normalize(level, MASTERY_MID_END_LEVEL + 1, MASTERY_MAX_LEVEL);
		return MASTERY_LATE_BASE_MULTIPLIER + (MASTERY_LATE_CURVE_MULTIPLIER * progress * progress);
	}

	private static double normalize(final int value, final int start, final int end) {
		if (end <= start) {
			return 1.0d;
		}
		final double normalized = (value - start) / (double) (end - start);
		if (normalized <= 0.0d) {
			return 0.0d;
		}
		if (normalized >= 1.0d) {
			return 1.0d;
		}
		return normalized;
	}

	private static double lerp(final double from, final double to, final double t) {
		return from + ((to - from) * t);
	}

	private static int clamp(final int value, final int min, final int max) {
		return Math.max(min, Math.min(max, value));
	}

	private static long safeAdd(final long left, final long right, final long cap) {
		if (left >= cap) {
			return cap;
		}
		if (right <= 0L) {
			return left;
		}
		if (Long.MAX_VALUE - left < right) {
			return cap;
		}
		final long sum = left + right;
		if (sum < 0L || sum > cap) {
			return cap;
		}
		return sum;
	}
}
