/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

/**
 * Fixed entry tiers for the Challenge Arena.
 *
 * <p>The stake is intentionally destroyed when a run starts. Higher tiers buy
 * a longer run, stronger creature selection, forced elite encounters and more
 * arena modifiers. They do not guarantee a particular item rarity.</p>
 */
public enum ChallengeArenaTier {
	TRIAL(100000, 10, 1, -2, 2, 0, 0, 0),
	SKIRMISH(250000, 12, 1, 0, 4, 0, 0, 0),
	HUNTER(500000, 15, 2, 2, 6, 1, 0, 1),
	VETERAN(1000000, 18, 2, 4, 8, 2, 1, 2),
	CHAMPION(2500000, 22, 3, 6, 12, 3, 1, 3),
	LEGEND(5000000, 28, 3, 8, 16, 5, 2, 4);

	private final int stake;
	private final int creatureCount;
	private final int waveSize;
	private final int minimumLevelOffset;
	private final int maximumLevelOffset;
	private final int forcedEliteCount;
	private final int modifierCount;
	private final int rewardRarityRolls;

	ChallengeArenaTier(final int stake, final int creatureCount,
			final int waveSize, final int minimumLevelOffset,
			final int maximumLevelOffset, final int forcedEliteCount,
			final int modifierCount, final int rewardRarityRolls) {
		this.stake = stake;
		this.creatureCount = creatureCount;
		this.waveSize = waveSize;
		this.minimumLevelOffset = minimumLevelOffset;
		this.maximumLevelOffset = maximumLevelOffset;
		this.forcedEliteCount = forcedEliteCount;
		this.modifierCount = modifierCount;
		this.rewardRarityRolls = rewardRarityRolls;
	}

	public int getStake() {
		return stake;
	}

	public int getCreatureCount() {
		return creatureCount;
	}

	public int getWaveSize() {
		return waveSize;
	}

	public int getMinimumLevelOffset() {
		return minimumLevelOffset;
	}

	public int getMaximumLevelOffset() {
		return maximumLevelOffset;
	}

	public int getForcedEliteCount() {
		return forcedEliteCount;
	}

	public int getModifierCount() {
		return modifierCount;
	}

	/**
	 * Number of normal drop-rarity rolls used for the equipment reward.
	 * Zero means this tier does not award an equipment chest.
	 */
	public int getRewardRarityRolls() {
		return rewardRarityRolls;
	}

	public boolean awardsEquipmentChest() {
		return rewardRarityRolls > 0;
	}

	/** Higher two tiers finish with an additional champion encounter. */
	public boolean isFinalChampion(final int creatureNumber) {
		return (this == CHAMPION || this == LEGEND)
				&& creatureNumber == creatureCount;
	}

	public double getChampionHpMultiplier() {
		return this == LEGEND ? 1.50 : this == CHAMPION ? 1.25 : 1.0;
	}

	public double getChampionAttackMultiplier() {
		return this == LEGEND ? 1.20 : this == CHAMPION ? 1.10 : 1.0;
	}

	public double getChampionDefenseMultiplier() {
		return this == LEGEND ? 1.15 : this == CHAMPION ? 1.10 : 1.0;
	}

	/**
	 * Scales the target creature level over the run so later enemies are more
	 * dangerous than the opening ones.
	 */
	public int getTargetCreatureLevel(final int playerLevel,
			final int creatureNumber) {
		final int safeNumber = Math.max(1,
				Math.min(creatureCount, creatureNumber));
		final double progress = creatureCount <= 1 ? 1.0
				: (safeNumber - 1) / (double) (creatureCount - 1);
		final int offset = (int) Math.round(minimumLevelOffset
				+ progress * (maximumLevelOffset - minimumLevelOffset));
		return Math.max(1, playerLevel + offset);
	}

	/**
	 * Places forced elite encounters roughly evenly through the run.
	 */
	public boolean shouldForceElite(final int creatureNumber) {
		if (forcedEliteCount <= 0 || creatureNumber <= 0
				|| creatureNumber > creatureCount) {
			return false;
		}
		for (int elite = 1; elite <= forcedEliteCount; elite++) {
			final int position = (int) Math.round(
					elite * creatureCount / (double) (forcedEliteCount + 1));
			if (creatureNumber == Math.max(1, position)) {
				return true;
			}
		}
		return false;
	}

	public static ChallengeArenaTier forStake(final int stake) {
		for (final ChallengeArenaTier tier : values()) {
			if (tier.stake == stake) {
				return tier;
			}
		return null;
	}
}
