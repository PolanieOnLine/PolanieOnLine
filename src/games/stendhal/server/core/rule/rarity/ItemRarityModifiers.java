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
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Immutable concrete or family-wide multipliers used when creating an item.
 * Concrete item attribute names take precedence over family multipliers.
 */
public final class ItemRarityModifiers {
	public static final String ALL_STATS = "stats";
	public static final String ATTACK = "attack";
	public static final String DEFENSE = "defense";
	public static final String SPEED = "speed";
	public static final String RANGE = "range";
	public static final String VALUE = "value";

	private static final ItemRarityModifiers EMPTY =
			new ItemRarityModifiers(Collections.<String, Double>emptyMap());

	private final Map<String, Double> multipliers;

	private ItemRarityModifiers(final Map<String, Double> multipliers) {
		this.multipliers = Collections.unmodifiableMap(
				new LinkedHashMap<String, Double>(multipliers));
	}

	public static ItemRarityModifiers empty() {
		return EMPTY;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Returns a multiplier, or {@code null} when none was specified.
	 */
	public Double getMultiplier(final String statistic) {
		return multipliers.get(statistic);
	}

	public double getMultiplier(final String statistic, final double fallback) {
		final Double result = getMultiplier(statistic);
		return result == null ? fallback : result.doubleValue();
	}

	public Double getStatMultiplier() {
		return getMultiplier(ALL_STATS);
	}

	public Double getAttackMultiplier() {
		return getMultiplier(ATTACK);
	}

	public Double getDefenseMultiplier() {
		return getMultiplier(DEFENSE);
	}

	public Double getSpeedMultiplier() {
		return getMultiplier(SPEED);
	}

	public Double getRangeMultiplier() {
		return getMultiplier(RANGE);
	}

	public Double getValueMultiplier() {
		return getMultiplier(VALUE);
	}

	public boolean isEmpty() {
		return multipliers.isEmpty();
	}

	public Map<String, Double> asMap() {
		return multipliers;
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ItemRarityModifiers)) {
			return false;
		}
		final ItemRarityModifiers other = (ItemRarityModifiers) obj;
		return multipliers.equals(other.multipliers);
	}

	@Override
	public int hashCode() {
		return multipliers.hashCode();
	}

	@Override
	public String toString() {
		return "ItemRarityModifiers" + multipliers;
	}

	/** Builder for deterministic quest/admin modifiers. */
	public static final class Builder {
		private final Map<String, Double> multipliers =
				new LinkedHashMap<String, Double>();

		public Builder statMultiplier(final double multiplier) {
			return multiplier(ALL_STATS, multiplier);
		}

		public Builder attackMultiplier(final double multiplier) {
			return multiplier(ATTACK, multiplier);
		}

		public Builder defenseMultiplier(final double multiplier) {
			return multiplier(DEFENSE, multiplier);
		}

		public Builder speedMultiplier(final double multiplier) {
			return multiplier(SPEED, multiplier);
		}

		public Builder rangeMultiplier(final double multiplier) {
			return multiplier(RANGE, multiplier);
		}

		public Builder valueMultiplier(final double multiplier) {
			return multiplier(VALUE, multiplier);
		}

		/**
		 * Adds a multiplier for an exact RP attribute or a supported family key.
		 */
		public Builder multiplier(final String statistic, final double multiplier) {
			if (statistic == null || statistic.trim().length() == 0) {
				throw new IllegalArgumentException("Statistic name must not be empty");
			}
			if (Double.isNaN(multiplier) || Double.isInfinite(multiplier)
					|| multiplier <= 0.0) {
				throw new IllegalArgumentException("Multiplier must be finite and positive");
			}
			multipliers.put(statistic.trim(), Double.valueOf(multiplier));
			return this;
		}

		public ItemRarityModifiers build() {
			if (multipliers.isEmpty()) {
				return EMPTY;
			}
			return new ItemRarityModifiers(multipliers);
		}
	}
}
