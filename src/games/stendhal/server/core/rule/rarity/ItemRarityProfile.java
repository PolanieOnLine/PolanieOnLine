/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import games.stendhal.common.constants.ItemRarity;

/**
 * A named rarity distribution and its modifier ranges.
 */
public final class ItemRarityProfile {
	public static final String DEFAULT_ID = "default";
	private static final double EXPECTED_TOTAL_WEIGHT = 100.0;
	private static final double WEIGHT_EPSILON = 0.000001;

	private final String id;
	private final Map<ItemRarity, Tier> tiers;

	private ItemRarityProfile(final Builder builder) {
		this.id = builder.id;
		this.tiers = Collections.unmodifiableMap(
				new EnumMap<ItemRarity, Tier>(builder.tiers));
		validate();
	}

	/**
	 * The first-version defaults. All tunable rarity numbers live here.
	 */
	public static ItemRarityProfile defaultProfile() {
		return builder(DEFAULT_ID)
				.tier(ItemRarity.COMMON, 70.0, 1.00, 1.00, 1.00)
				.tier(ItemRarity.RARE, 22.0, 1.05, 1.10, 1.20)
				.tier(ItemRarity.EPIC, 6.0, 1.10, 1.20, 1.50)
				.tier(ItemRarity.LEGENDARY, 2.0, 1.20, 1.35, 2.00)
				.build();
	}

	public static Builder builder(final String id) {
		return new Builder(id);
	}

	public String getId() {
		return id;
	}

	public Tier getTier(final ItemRarity rarity) {
		return tiers.get(rarity);
	}

	public Map<ItemRarity, Tier> getTiers() {
		return tiers;
	}

	public double getTotalWeight() {
		double total = 0.0;
		for (final Tier tier : tiers.values()) {
			total += tier.getWeight();
		}
		return total;
	}

	/**
	 * Selects a rarity from a random value in the half-open range [0, 1).
	 */
	public ItemRarity roll(final double randomValue) {
		if (Double.isNaN(randomValue) || randomValue < 0.0 || randomValue >= 1.0) {
			throw new IllegalArgumentException("Random rarity value must be in [0, 1)");
		}

		final double point = randomValue * EXPECTED_TOTAL_WEIGHT;
		double cumulative = 0.0;
		for (final ItemRarity rarity : ItemRarity.values()) {
			cumulative += tiers.get(rarity).getWeight();
			if (point < cumulative) {
				return rarity;
			}
		}

		// Only reachable because of floating point accumulation.
		return ItemRarity.LEGENDARY;
	}

	private void validate() {
		for (final ItemRarity rarity : ItemRarity.values()) {
			if (!tiers.containsKey(rarity)) {
				throw new IllegalArgumentException("Missing rarity tier: " + rarity);
			}
		}
		if (Math.abs(getTotalWeight() - EXPECTED_TOTAL_WEIGHT) > WEIGHT_EPSILON) {
			throw new IllegalArgumentException("Rarity weights must add up to 100");
		}
	}

	/** Configuration for one rarity tier. */
	public static final class Tier {
		private final double weight;
		private final double minimumStatMultiplier;
		private final double maximumStatMultiplier;
		private final double valueMultiplier;

		private Tier(final double weight, final double minimumStatMultiplier,
				final double maximumStatMultiplier, final double valueMultiplier) {
			if (weight < 0.0 || Double.isNaN(weight) || Double.isInfinite(weight)) {
				throw new IllegalArgumentException("Rarity weight must be finite and non-negative");
			}
			if (minimumStatMultiplier <= 0.0
					|| maximumStatMultiplier < minimumStatMultiplier
					|| valueMultiplier <= 0.0
					|| Double.isNaN(minimumStatMultiplier)
					|| Double.isInfinite(minimumStatMultiplier)
					|| Double.isNaN(maximumStatMultiplier)
					|| Double.isInfinite(maximumStatMultiplier)
					|| Double.isNaN(valueMultiplier)
					|| Double.isInfinite(valueMultiplier)) {
				throw new IllegalArgumentException("Invalid rarity multiplier range");
			}
			this.weight = weight;
			this.minimumStatMultiplier = minimumStatMultiplier;
			this.maximumStatMultiplier = maximumStatMultiplier;
			this.valueMultiplier = valueMultiplier;
		}

		public double getWeight() {
			return weight;
		}

		public double getMinimumStatMultiplier() {
			return minimumStatMultiplier;
		}

		public double getMaximumStatMultiplier() {
			return maximumStatMultiplier;
		}

		public double getValueMultiplier() {
			return valueMultiplier;
		}

		public double midpointStatMultiplier() {
			return (minimumStatMultiplier + maximumStatMultiplier) / 2.0;
		}
	}

	public static final class Builder {
		private final String id;
		private final EnumMap<ItemRarity, Tier> tiers =
				new EnumMap<ItemRarity, Tier>(ItemRarity.class);

		private Builder(final String id) {
			if (id == null || id.trim().length() == 0) {
				throw new IllegalArgumentException("Profile id must not be empty");
			}
			this.id = id.trim();
		}

		public Builder tier(final ItemRarity rarity, final double weight,
				final double minimumStatMultiplier,
				final double maximumStatMultiplier,
				final double valueMultiplier) {
			if (rarity == null) {
				throw new IllegalArgumentException("Rarity must not be null");
			}
			tiers.put(rarity, new Tier(weight, minimumStatMultiplier,
					maximumStatMultiplier, valueMultiplier));
			return this;
		}

		public ItemRarityProfile build() {
			return new ItemRarityProfile(this);
		}
	}
}
