package games.stendhal.server.maps.quests.hunting;

/** Persisted names must remain stable: they are also stored on reward chests. */
public enum HuntingDifficulty {
	EASY("łatwe", 5, -10, -2, 20, 30),
	MEDIUM("średnie", 15, -1, 2, 20, 30),
	HARD("trudne", 30, 3, 8, 40, 50);

	private final String label;
	private final int percent;
	private final int minimumOffset;
	private final int maximumOffset;
	private final int minimumKillCount;
	private final int maximumKillCount;

	HuntingDifficulty(String label, int percent, int minimumOffset, int maximumOffset,
			int minimumKillCount, int maximumKillCount) {
		this.label = label;
		this.percent = percent;
		this.minimumOffset = minimumOffset;
		this.maximumOffset = maximumOffset;
		this.minimumKillCount = minimumKillCount;
		this.maximumKillCount = maximumKillCount;
	}

	public String getLabel() { return label; }
	public int getPercent() { return percent; }
	public int getMinimumKillCount() { return minimumKillCount; }
	public int getMaximumKillCount() { return maximumKillCount; }
	/** Broad and non-overlapping percentage bands at higher levels give each
	 * difficulty a distinct roster instead of only a few nearby levels. */
	public int getMinimumCreatureLevel(int playerLevel) {
		if (playerLevel <= 20) {
			return Math.max(0, playerLevel + minimumOffset);
		}
		if (this == EASY) {
			return Math.max(1, percent(playerLevel, 60));
		}
		if (this == MEDIUM) {
			return percent(playerLevel, 85) + 1;
		}
		return percent(playerLevel, 105) + 1;
	}

	public int getMaximumCreatureLevel(int playerLevel) {
		if (playerLevel <= 20) {
			return playerLevel + maximumOffset;
		}
		if (this == EASY) {
			return percent(playerLevel, 85);
		}
		if (this == MEDIUM) {
			return percent(playerLevel, 105);
		}
		return percent(playerLevel, 125);
	}

	private static int percent(int level, int value) {
		return (int) Math.min(Integer.MAX_VALUE, (long) level * value / 100);
	}

	public boolean accepts(int playerLevel, int creatureLevel) {
		// Beginning hunters may have only one easy monster available.
		if (this == EASY && playerLevel < 3) {
			return creatureLevel >= 0 && creatureLevel <= Math.max(1, playerLevel);
		}
		return creatureLevel >= getMinimumCreatureLevel(playerLevel)
				&& creatureLevel <= getMaximumCreatureLevel(playerLevel);
	}

	public static HuntingDifficulty fromQuest(String quest) {
		if (quest != null) {
			String[] fields = quest.split(";", -1);
			if (fields.length > 3) {
				try { return valueOf(fields[3]); }
				catch (IllegalArgumentException ignored) { /* Legacy hunt. */ }
			}
		}
		return MEDIUM;
	}
}
