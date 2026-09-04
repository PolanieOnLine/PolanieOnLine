/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

/** Persistent state stored in the player quest slot for Challenge Arena runs. */
public final class ChallengeArenaState {
	public static final String QUEST_SLOT = "challenge_arena";

	public enum Lifecycle {
		ACTIVE,
		VICTORY,
		FAILED,
		DONE
	}

	private final Lifecycle lifecycle;
	private final ChallengeArenaTier tier;
	private final int spawnedCreatures;
	private final long startedAt;

	private ChallengeArenaState(final Lifecycle lifecycle,
			final ChallengeArenaTier tier, final int spawnedCreatures,
			final long startedAt) {
		this.lifecycle = lifecycle;
		this.tier = tier;
		this.spawnedCreatures = Math.max(0, spawnedCreatures);
		this.startedAt = Math.max(0L, startedAt);
	}

	public static ChallengeArenaState start(final ChallengeArenaTier tier) {
		if (tier == null) {
			throw new IllegalArgumentException("Challenge Arena tier must not be null");
		}
		return new ChallengeArenaState(Lifecycle.ACTIVE, tier, 0,
				System.currentTimeMillis());
	}

	public Lifecycle getLifecycle() {
		return lifecycle;
	}

	public ChallengeArenaTier getTier() {
		return tier;
	}

	public int getSpawnedCreatures() {
		return spawnedCreatures;
	}

	public long getStartedAt() {
		return startedAt;
	}

	public ChallengeArenaState withSpawnedCreatures(final int count) {
		return new ChallengeArenaState(lifecycle, tier, count, startedAt);
	}

	public ChallengeArenaState withLifecycle(final Lifecycle newLifecycle) {
		return new ChallengeArenaState(newLifecycle, tier, spawnedCreatures,
				startedAt);
	}

	public String serialize() {
		return lifecycle.name().toLowerCase() + ";" + tier.name() + ";"
				+ spawnedCreatures + ";" + startedAt;
	}

	public static ChallengeArenaState parse(final String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		final String[] parts = value.split(";");
		if (parts.length < 2) {
			return null;
		}
		try {
			final Lifecycle lifecycle = Lifecycle.valueOf(parts[0].trim().toUpperCase());
			final ChallengeArenaTier tier = ChallengeArenaTier.valueOf(parts[1].trim().toUpperCase());
			final int spawned = parts.length > 2
					? Integer.parseInt(parts[2].trim()) : 0;
			final long started = parts.length > 3
					? Long.parseLong(parts[3].trim()) : 0L;
			return new ChallengeArenaState(lifecycle, tier, spawned, started);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}
}
