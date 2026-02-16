package games.stendhal.server.entity;

import java.util.Arrays;

import games.stendhal.common.Rand;

/**
 * Balancing profile for chest rewards.
 */
public class ChestRewardProfile {

	public static final class RewardEntry {
		private final String itemName;
		private final int weight;
		private final int minQuantity;
		private final int maxQuantity;
		private final boolean bound;
		private final double unitValue;

		public RewardEntry(final String itemName, final int weight, final int minQuantity,
				final int maxQuantity, final boolean bound, final double unitValue) {
			this.itemName = itemName;
			this.weight = weight;
			this.minQuantity = minQuantity;
			this.maxQuantity = maxQuantity;
			this.bound = bound;
			this.unitValue = unitValue;
		}
	}

	public static final class RolledReward {
		private final RewardEntry entry;
		private final int quantity;

		private RolledReward(final RewardEntry entry, final int quantity) {
			this.entry = entry;
			this.quantity = quantity;
		}

		public String getItemName() {
			return entry.itemName;
		}

		public int getQuantity() {
			return quantity;
		}

		public boolean isBound() {
			return entry.bound;
		}

		public double expectedValue() {
			return entry.unitValue * quantity;
		}
	}

	private final String chestId;
	private final RewardEntry[] entries;
	private final int totalWeight;

	public ChestRewardProfile(final String chestId, final RewardEntry[] entries) {
		this.chestId = chestId;
		this.entries = Arrays.copyOf(entries, entries.length);

		int weightSum = 0;
		for (final RewardEntry entry : entries) {
			weightSum += entry.weight;
		}
		this.totalWeight = weightSum;
	}

	public RolledReward rollReward() {
		int pick = Rand.rand(totalWeight);
		for (final RewardEntry entry : entries) {
			pick -= entry.weight;
			if (pick < 0) {
				final int quantity = rollQuantity(entry);
				return new RolledReward(entry, quantity);
			}
		}

		final RewardEntry fallback = entries[entries.length - 1];
		return new RolledReward(fallback, rollQuantity(fallback));
	}

	public double expectedValue() {
		double expected = 0;
		for (final RewardEntry entry : entries) {
			expected += entry.weight * entry.unitValue
					* ((entry.minQuantity + entry.maxQuantity) / 2.0);
		}
		return expected / totalWeight;
	}

	public String getChestId() {
		return chestId;
	}

	private int rollQuantity(final RewardEntry entry) {
		if (entry.maxQuantity <= entry.minQuantity) {
			return entry.minQuantity;
		}
		return entry.minQuantity + Rand.rand(entry.maxQuantity - entry.minQuantity + 1);
	}

	public static RewardEntry reward(final String itemName, final int weight,
			final int minQuantity, final int maxQuantity, final boolean bound,
			final double unitValue) {
		return new RewardEntry(itemName, weight, minQuantity, maxQuantity, bound, unitValue);
	}
}
