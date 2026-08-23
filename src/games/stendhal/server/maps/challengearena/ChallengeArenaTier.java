/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

/**
 * Fixed entry tiers for the Challenge Arena.
 *
 * <p>The stake is intentionally destroyed when a run starts. Higher tiers buy
 * more enemies, denser waves, stronger creature selection, forced elite
 * encounters and more arena modifiers. They do not guarantee a particular
 * item rarity.</p>
 */
public enum ChallengeArenaTier {
	TRIAL(100000, 10, 3, 2, 10, 0, 0, 0),
	SKIRMISH(250000, 15, 4, 4, 14, 0, 0, 0),
	HUNTER(500000, 20, 6, 6, 18, 1, 0, 1),
	VETERAN(1000000, 26, 6, 8, 22, 2, 1, 2),
	CHAMPION(2500000, 34, 7, 12, 28, 3, 1, 3),
	LEGEND(5000000, 45, 9, 16, 36, 5, 2, 4);

	private final int stake;
	private final int creatureCount;
	private final int waveCount;
	private final int minimumLevelOffset;
	private final int maximumLevelOffset;
	private final int forcedEliteCount;
	private final int modifierCount;
	private final int rewardRarityRolls;

	ChallengeArenaTier(final int stake, final int creatureCount,
			final int waveCount, final int minimumLevelOffset,
			final int maximumLevelOffset, final int forcedEliteCount,
			final int modifierCount, final int rewardRarityRolls) {
		this.stake = stake;
		this.creatureCount = creatureCount;
		this.waveCount = waveCount;
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

	public int getWaveCount() {
		return waveCount;
	}

	/** Maximum number of creatures that can appear in one wave. */
	public int getWaveSize() {
		return (creatureCount + waveCount - 1) / waveCount;
	}

	/**
	 * Returns the exact size of one numbered wave. Remainders are assigned to
	 * the first waves, so 10 enemies in 3 waves become 4, 3 and 3.
	 */
	public int getWaveSizeForWave(final int waveNumber) {
		if (waveNumber < 1 || waveNumber > waveCount) {
			return 0;
		}
		final int base = creatureCount / waveCount;
		final int extra = creatureCount % waveCount;
		return base + (waveNumber <= extra ? 1 : 0);
	}

	/** Returns the next wave number for a saved spawned-creature count. */
	public int getNextWaveNumber(final int alreadySpawned) {
		final int safeSpawned = Math.max(0, Math.min(creatureCount, alreadySpawned));
		int total = 0;
		for (int wave = 1; wave <= waveCount; wave++) {
			total += getWaveSizeForWave(wave);
			if (safeSpawned < total) {
				return wave;
			}
		}
		return waveCount + 1;
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

	/** Places forced elite encounters roughly evenly through the run. */
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
