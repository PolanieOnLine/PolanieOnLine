package games.stendhal.server.maps.quests.hunting;

/** Persisted names must remain stable: they are also stored on reward chests. */
public enum HuntingDifficulty {
	EASY("łatwe", 5, -10, -2),
	MEDIUM("średnie", 15, -1, 2),
	HARD("trudne", 30, 3, 8);

	private final String label;
	private final int percent;
	private final int minimumOffset;
	private final int maximumOffset;

	HuntingDifficulty(String label, int percent, int minimumOffset, int maximumOffset) {
		this.label = label;
		this.percent = percent;
		this.minimumOffset = minimumOffset;
		this.maximumOffset = maximumOffset;
	}

	public String getLabel() { return label; }
	public int getPercent() { return percent; }
	public boolean accepts(int playerLevel, int creatureLevel) {
		// Beginning hunters may have no creatures two levels below them.
		if (this == EASY && playerLevel < 3) {
			return creatureLevel <= Math.max(1, playerLevel);
		}
		return creatureLevel >= Math.max(0, playerLevel + minimumOffset)
				&& creatureLevel <= playerLevel + maximumOffset;
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
