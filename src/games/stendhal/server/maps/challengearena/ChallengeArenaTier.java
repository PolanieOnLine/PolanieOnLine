/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

/**
 * Fixed entry tiers for the Challenge Arena.
 *
 * <p>The stake is intentionally destroyed when a run starts. Higher tiers buy
 * a longer run, stronger creature selection, forced elite encounters and more
 * arena modifiers. They do not guarantee a particular item reward.</p>
 */
public enum ChallengeArenaTier {
	TRIAL(100000, 10, -2, 2, 0, 0),
	SKIRMISH(250000, 12, 0, 4, 0, 0),
	HUNTER(500000, 15, 2, 6, 1, 0),
	VETERAN(1000000, 18, 4, 8, 2, 1),
	CHAMPION(2500000, 22, 6, 12, 3, 1),
	LEGEND(5000000, 28, 8, 16, 5, 2);

	private final int stake;
	private final int creatureCount;
	private final int minimumLevelOffset;
	private final int maximumLevelOffset;
	private final int forcedEliteCount;
	private final int modifierCount;

	ChallengeArenaTier(final int stake, final int creatureCount,
			final int minimumLevelOffset, final int maximumLevelOffset,
			final int forcedEliteCount, final int modifierCount) {
		this.stake = stake;
		this.creatureCount = creatureCount;
		this.minimumLevelOffset = minimumLevelOffset;
		this.maximumLevelOffset = maximumLevelOffset;
		this.forcedEliteCount = forcedEliteCount;
		this.modifierCount = modifierCount;
	}

	public int getStake() {
		return stake;
	}

	public int getCreatureCount() {
		return creatureCount;
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
		}
		return null;
	}
}
