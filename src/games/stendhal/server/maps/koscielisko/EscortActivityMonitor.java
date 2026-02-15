package games.stendhal.server.maps.koscielisko;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.server.entity.player.Player;

final class EscortActivityMonitor {
	private final int minPlayersToStart;
	private final int minActivityTicksForReward;
	private final int maxLowActivityTicks;
	private final Map<String, PlayerSnapshot> playerSnapshots = new HashMap<>();
	private final Map<String, Integer> playerActivityTicks = new HashMap<>();
	private final Set<String> rewardEligiblePlayers = new HashSet<>();
	private int lowActivityTicks;

	EscortActivityMonitor(final int minPlayersToStart, final int minActivityTicksForReward, final int maxLowActivityTicks) {
		this.minPlayersToStart = minPlayersToStart;
		this.minActivityTicksForReward = minActivityTicksForReward;
		this.maxLowActivityTicks = maxLowActivityTicks;
	}

	void reset() {
		playerSnapshots.clear();
		playerActivityTicks.clear();
		rewardEligiblePlayers.clear();
		lowActivityTicks = 0;
	}

	boolean sample(final List<Player> players, final String giantZoneName, final long nowMillis) {
		int movedPlayers = 0;
		for (final Player player : players) {
			if (player == null || player.getZone() == null || !player.getZone().getName().equals(giantZoneName)) {
				continue;
			}
			final PlayerSnapshot previous = playerSnapshots.get(player.getName());
			final PlayerSnapshot current = new PlayerSnapshot(player.getX(), player.getY(), nowMillis);
			playerSnapshots.put(player.getName(), current);
			if (previous != null && previous.hasMovedTo(current)) {
				movedPlayers++;
				final int ticks = playerActivityTicks.getOrDefault(player.getName(), 0) + 1;
				playerActivityTicks.put(player.getName(), ticks);
				if (ticks >= minActivityTicksForReward) {
					rewardEligiblePlayers.add(player.getName());
				}
			}
		}

		if (movedPlayers < minPlayersToStart) {
			lowActivityTicks++;
			return lowActivityTicks < maxLowActivityTicks;
		}
		lowActivityTicks = 0;
		return true;
	}

	Set<String> rewardEligiblePlayers() {
		return rewardEligiblePlayers;
	}

	static final class PlayerSnapshot {
		private final int x;
		private final int y;
		private final long recordedAt;

		PlayerSnapshot(final int x, final int y, final long recordedAt) {
			this.x = x;
			this.y = y;
			this.recordedAt = recordedAt;
		}

		boolean hasMovedTo(final PlayerSnapshot other) {
			return other != null && (x != other.x || y != other.y) && other.recordedAt > recordedAt;
		}
	}
}
