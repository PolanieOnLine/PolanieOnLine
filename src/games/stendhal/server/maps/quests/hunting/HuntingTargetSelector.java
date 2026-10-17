package games.stendhal.server.maps.quests.hunting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

/** Chooses achievable public-spawn targets without assigning trivial mobs at high levels. */
public final class HuntingTargetSelector {
	private static final int MIN_ADAPTIVE_PLAYER_LEVEL = 50;
	private static final int MAX_LEVELS_ABOVE_PLAYER = 8;
	private static final int MIN_FALLBACK_SPREAD = 3;

	private HuntingTargetSelector() { }

	public static <T> List<T> select(final int playerLevel, final HuntingDifficulty difficulty,
			final List<T> available, final ToIntFunction<T> levels) {
		final List<T> exact = new ArrayList<>();
		for (final T target : available) {
			if (difficulty.accepts(playerLevel, levels.applyAsInt(target))) {
				exact.add(target);
			}
		}
		if (!exact.isEmpty()) {
			return exact;
		}

		// When the player has outgrown the current public respawn roster, the
		// original narrow level windows cannot ever produce an offer.
		// Do not loosen beginner hunts or substitute a rat for a veteran.
		if (playerLevel < MIN_ADAPTIVE_PLAYER_LEVEL) {
			return Collections.emptyList();
		}
		final List<T> reachable = new ArrayList<>();
		final int minimumLevel = Math.max(20, (playerLevel + 1) / 2);
		for (final T target : available) {
			final int level = levels.applyAsInt(target);
			if (level >= minimumLevel && level <= playerLevel + MAX_LEVELS_ABOVE_PLAYER) {
				reachable.add(target);
			}
		}
		if (reachable.isEmpty()) {
			return Collections.emptyList();
		}

		// Preserve more possible opponents when there is no exact match:
		// choose the closest targets to this difficulty's desired level band.
		final int preferredLevel = (int) (((long) difficulty.getMinimumCreatureLevel(playerLevel)
				+ difficulty.getMaximumCreatureLevel(playerLevel)) / 2);
		int bestDistance = Integer.MAX_VALUE;
		for (final T target : reachable) {
			bestDistance = Math.min(bestDistance,
					Math.abs(levels.applyAsInt(target) - preferredLevel));
		}
		// Three levels was too restrictive for veterans: spread grows with level.
		final int spread = Math.max(MIN_FALLBACK_SPREAD, playerLevel / 20);
		final List<T> chosen = new ArrayList<>();
		for (final T target : reachable) {
			if (Math.abs(levels.applyAsInt(target) - preferredLevel)
					<= bestDistance + spread) {
				chosen.add(target);
			}
		}
		return chosen;
	}
}
