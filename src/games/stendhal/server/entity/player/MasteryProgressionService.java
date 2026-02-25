package games.stendhal.server.entity.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import games.stendhal.server.entity.RPEntity;

/**
 * Domain service odpowiedzialny za całą logikę mastery progression.
 */
public class MasteryProgressionService {

	private static final int[] MASTERY_MILESTONES = { 100, 500, 1000, 2000 };

	public MasteryProgressionResult addMasteryExp(final RPEntity character, final long expAmount) {
		syncMasteryPayload(character);
		if (expAmount <= 0L) {
			return MasteryProgressionResult.noop();
		}

		final long currentMasteryExp = character.has("mastery_exp")
			? character.getLong("mastery_exp")
			: (character.has("mastery_xp") ? character.getLong("mastery_xp") : 0L);
		final long currentMasteryTotalExp = character.has("mastery_total_exp") ? character.getLong("mastery_total_exp") : currentMasteryExp;
		final int currentMasteryLevel = character.has("mastery_level") ? character.getInt("mastery_level") : 0;

		if (currentMasteryLevel >= ProgressionConfig.MASTERY_MAX_LEVEL) {
			syncMasteryPayload(character);
			return MasteryProgressionResult.capped(currentMasteryLevel);
		}

		long targetMasteryExp = safeAdd(currentMasteryExp, expAmount);
		final long updatedMasteryTotalExp = safeAdd(currentMasteryTotalExp, expAmount);
		final long maxMasteryXP = ProgressionConfig.getMasteryMaxXP();
		if (targetMasteryExp > maxMasteryXP) {
			targetMasteryExp = maxMasteryXP;
		}

		final long appliedMasteryExpGain = Math.max(0L, targetMasteryExp - currentMasteryExp);
		int updatedMasteryLevel = currentMasteryLevel;
		final List<Integer> levelUps = new ArrayList<Integer>();
		while (updatedMasteryLevel < ProgressionConfig.MASTERY_MAX_LEVEL
			&& targetMasteryExp >= ProgressionConfig.getMasteryXPForLevel(updatedMasteryLevel + 1)) {
			updatedMasteryLevel++;
			levelUps.add(updatedMasteryLevel);
		}

		final List<Integer> reachedMilestones = new ArrayList<Integer>();
		for (final int milestone : MASTERY_MILESTONES) {
			if (currentMasteryLevel < milestone && updatedMasteryLevel >= milestone) {
				reachedMilestones.add(milestone);
			}
		}

		character.put("mastery_exp", targetMasteryExp);
		character.put("mastery_xp", targetMasteryExp);
		character.put("mastery_level", updatedMasteryLevel);
		character.put("mastery_total_exp", updatedMasteryTotalExp);
		if (!character.has("mastery_unlocked_at")) {
			character.put("mastery_unlocked_at", System.currentTimeMillis());
		}
		syncMasteryPayload(character);

		return new MasteryProgressionResult(
			appliedMasteryExpGain,
			targetMasteryExp,
			updatedMasteryLevel,
			Collections.unmodifiableList(levelUps),
			Collections.unmodifiableList(reachedMilestones),
			updatedMasteryLevel >= ProgressionConfig.MASTERY_MAX_LEVEL,
			false);
	}

	private static void syncMasteryPayload(final RPEntity character) {
		final int masteryLevel = character.has("mastery_level") ? character.getInt("mastery_level") : 0;
		final long masteryExp = character.has("mastery_exp")
			? character.getLong("mastery_exp")
			: (character.has("mastery_xp") ? character.getLong("mastery_xp") : 0L);
		final int requiredResets = ProgressionConfig.MASTERY_MIN_RESETS;
		final int requiredLevel = games.stendhal.common.Level.maxLevel();
		final int maxMasteryLevel = ProgressionConfig.MASTERY_MAX_LEVEL;

		final long nextLevelXp = masteryLevel >= maxMasteryLevel ? masteryExp : ProgressionConfig.getMasteryXPForLevel(masteryLevel + 1);
		final long expToNext = Math.max(0L, nextLevelXp - masteryExp);
		character.put("mastery_exp_to_next", expToNext);
		character.put("mastery_required_resets", requiredResets);
		character.put("mastery_required_level", requiredLevel);
		character.put("mastery_max_level", maxMasteryLevel);
		character.put("mastery_unlocked", (masteryLevel > 0 || character.has("mastery_unlocked_at")) ? "1" : "0");
	}

	private static long safeAdd(final long left, final long right) {
		if (right <= 0L) {
			return left;
		}
		if (Long.MAX_VALUE - left < right) {
			return Long.MAX_VALUE;
		}
		return left + right;
	}

	public static final class MasteryProgressionResult {
		private static final MasteryProgressionResult NOOP = new MasteryProgressionResult(0L, 0L, 0,
			Collections.<Integer>emptyList(), Collections.<Integer>emptyList(), false, false);

		private final long appliedMasteryExpGain;
		private final long updatedMasteryExp;
		private final int updatedMasteryLevel;
		private final List<Integer> levelUps;
		private final List<Integer> reachedMilestones;
		private final boolean cappedAfterUpdate;
		private final boolean alreadyCapped;

		private MasteryProgressionResult(final long appliedMasteryExpGain, final long updatedMasteryExp,
				final int updatedMasteryLevel, final List<Integer> levelUps, final List<Integer> reachedMilestones,
				final boolean cappedAfterUpdate, final boolean alreadyCapped) {
			this.appliedMasteryExpGain = appliedMasteryExpGain;
			this.updatedMasteryExp = updatedMasteryExp;
			this.updatedMasteryLevel = updatedMasteryLevel;
			this.levelUps = levelUps;
			this.reachedMilestones = reachedMilestones;
			this.cappedAfterUpdate = cappedAfterUpdate;
			this.alreadyCapped = alreadyCapped;
		}

		private static MasteryProgressionResult noop() {
			return NOOP;
		}

		private static MasteryProgressionResult capped(final int cappedLevel) {
			return new MasteryProgressionResult(0L, 0L, cappedLevel,
				Collections.<Integer>emptyList(), Collections.<Integer>emptyList(), true, true);
		}

		public long getAppliedMasteryExpGain() {
			return appliedMasteryExpGain;
		}

		public long getUpdatedMasteryExp() {
			return updatedMasteryExp;
		}

		public int getUpdatedMasteryLevel() {
			return updatedMasteryLevel;
		}

		public List<Integer> getLevelUps() {
			return levelUps;
		}

		public List<Integer> getReachedMilestones() {
			return reachedMilestones;
		}

		public boolean isCappedAfterUpdate() {
			return cappedAfterUpdate;
		}

		public boolean isAlreadyCapped() {
			return alreadyCapped;
		}

		public boolean hasChanges() {
			return appliedMasteryExpGain > 0L || !levelUps.isEmpty() || !reachedMilestones.isEmpty();
		}
	}
}
