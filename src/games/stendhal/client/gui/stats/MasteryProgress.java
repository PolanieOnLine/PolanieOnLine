package games.stendhal.client.gui.stats;

import games.stendhal.common.Level;

/**
 * Snapshot of mastery progression state used by UI.
 */
public final class MasteryProgress {
	public static final int DEFAULT_REQUIRED_RESETS = 5;
	public static final int DEFAULT_MAX_MASTERY_LEVEL = 2000;

	private final boolean unlocked;
	private final int masteryLevel;
	private final long currentExp;
	private final long expToNext;
	private final int requiredResets;
	private final int requiredLevel;
	private final int maxMasteryLevel;
	private final int currentResets;
	private final int currentLevel;

	public MasteryProgress(boolean unlocked, int masteryLevel, long currentExp, long expToNext,
			int requiredResets, int requiredLevel, int maxMasteryLevel, int currentResets, int currentLevel) {
		this.unlocked = unlocked;
		this.masteryLevel = masteryLevel;
		this.currentExp = currentExp;
		this.expToNext = expToNext;
		this.requiredResets = requiredResets;
		this.requiredLevel = requiredLevel;
		this.maxMasteryLevel = maxMasteryLevel;
		this.currentResets = currentResets;
		this.currentLevel = currentLevel;
	}

	public static MasteryProgress lockedDefault() {
		return new MasteryProgress(false, 0, 0, 0, DEFAULT_REQUIRED_RESETS, Level.maxLevel(), DEFAULT_MAX_MASTERY_LEVEL, 0, 0);
	}

	public boolean isUnlocked() { return unlocked; }
	public int getMasteryLevel() { return masteryLevel; }
	public long getCurrentExp() { return currentExp; }
	public long getExpToNext() { return expToNext; }
	public int getRequiredResets() { return requiredResets; }
	public int getRequiredLevel() { return requiredLevel; }
	public int getMaxMasteryLevel() { return maxMasteryLevel; }
	public int getCurrentResets() { return currentResets; }
	public int getCurrentLevel() { return currentLevel; }

	public boolean isCapped() {
		return masteryLevel >= maxMasteryLevel || expToNext <= 0;
	}
}
