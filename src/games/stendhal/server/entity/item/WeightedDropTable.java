/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import java.util.List;

import games.stendhal.common.Rand;

/**
 * Helper for drawing items from a weighted drop table.
 */
public final class WeightedDropTable {

	/**
	 * Single entry in the drop table.
	 */
	public static final class Entry {

		private final String itemName;
		private final double chancePercent;
		private final int minQuantity;
		private final int maxQuantity;

		/**
		 * Creates a drop table entry with a fixed quantity of 1.
		 *
		 * @param itemName
		 *            name of the item that can be won
		 * @param chancePercent
		 *            probability of winning the item expressed as percent
		 */
		public Entry(final String itemName, final double chancePercent) {
			this(itemName, chancePercent, 1, 1);
		}

		/**
		 * Creates a drop table entry with a fixed quantity.
		 *
		 * @param itemName
		 *            name of the item that can be won
		 * @param chancePercent
		 *            probability of winning the item expressed as percent
		 * @param quantity
		 *            exact quantity given when the item is selected
		 */
		public Entry(final String itemName, final double chancePercent, final int quantity) {
			this(itemName, chancePercent, quantity, quantity);
		}

		/**
		 * Creates a drop table entry with a quantity range.
		 *
		 * @param itemName
		 *            name of the item that can be won
		 * @param chancePercent
		 *            probability of winning the item expressed as percent
		 * @param minQuantity
		 *            minimum quantity that can be awarded
		 * @param maxQuantity
		 *            maximum quantity that can be awarded
		 */
		public Entry(final String itemName, final double chancePercent, final int minQuantity, final int maxQuantity) {
			if (itemName == null) {
				throw new IllegalArgumentException("itemName must not be null");
			}
			if (chancePercent <= 0) {
				throw new IllegalArgumentException("chancePercent must be positive");
			}
			if (minQuantity <= 0 || maxQuantity <= 0) {
				throw new IllegalArgumentException("quantities must be positive");
			}
			if (minQuantity > maxQuantity) {
				throw new IllegalArgumentException("minQuantity must not exceed maxQuantity");
			}
			this.itemName = itemName;
			this.chancePercent = chancePercent;
			this.minQuantity = minQuantity;
			this.maxQuantity = maxQuantity;
		}

		/**
		 * @return name of the item bound to this entry
		 */
		public String getItemName() {
			return itemName;
		}

		/**
		 * @return probability of the item being selected
		 */
		public double getChance() {
			return chancePercent;
		}

		/**
		 * Rolls the quantity for this entry.
		 *
		 * @return quantity that should be granted
		 */
		public int rollQuantity() {
			if (minQuantity == maxQuantity) {
				return minQuantity;
			}
			return Rand.randUniform(minQuantity, maxQuantity);
		}
	}

	/**
	 * Result of rolling the drop table.
	 */
	public static final class Result {

		private final String itemName;
		private final int quantity;

		/**
		 * Creates a new result.
		 *
		 * @param itemName
		 *            name of the item won
		 * @param quantity
		 *            quantity of the item won
		 */
		public Result(final String itemName, final int quantity) {
			if (itemName == null) {
				throw new IllegalArgumentException("itemName must not be null");
			}
			if (quantity <= 0) {
				throw new IllegalArgumentException("quantity must be positive");
			}
			this.itemName = itemName;
			this.quantity = quantity;
		}

		/**
		 * @return name of the item that was rolled
		 */
		public String getItemName() {
			return itemName;
		}

		/**
		 * @return quantity of the item that was rolled
		 */
		public int getQuantity() {
			return quantity;
		}
	}

	private WeightedDropTable() {
		// Utility class
	}

	/**
	 * Draws a random entry based on weighted probabilities.
	 *
	 * @param entries
	 *            list of entries to draw from
	 * @return randomly selected drop result
	 */
	public static Result roll(final List<Entry> entries) {
		if (entries == null || entries.isEmpty()) {
			throw new IllegalArgumentException("entries must not be null or empty");
		}

		double totalChance = 0.0;
		for (final Entry entry : entries) {
			totalChance += entry.getChance();
		}

		if (totalChance <= 0) {
			throw new IllegalArgumentException("total chance must be positive");
		}

		final double roll = Rand.rand() * totalChance;
		double cumulative = 0.0;
		for (final Entry entry : entries) {
			cumulative += entry.getChance();
			if (roll < cumulative) {
			return new Result(entry.getItemName(), entry.rollQuantity());
			}
		}

		final Entry fallback = entries.get(entries.size() - 1);
		return new Result(fallback.getItemName(), fallback.rollQuantity());
	}
}
