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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;

/**
 * Compact snapshot used by the few quest scripts which temporarily remove an
 * item instead of keeping its RPObject. Normal database persistence does not
 * use this class.
 */
public final class ItemRaritySnapshot {
	private static final int LEGACY_VERSION = 1;
	private static final int VERSION = 2;
	private static final int MAX_ENTRIES = 64;
	private static final int MAX_STATISTICS = 64;
	private static final int MAX_ENCODED_LENGTH = 32768;

	private ItemRaritySnapshot() {
		// utility class
	}

	/**
	 * Captures rarity metadata, exact modifiers and the already-computed final
	 * values. Returning an empty string keeps old, non-rarity quest state
	 * compatible.
	 */
	public static String encode(final Item item) {
		if (item == null || item.getRarity() == null) {
			return "";
		}

		try {
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final DataOutputStream output = new DataOutputStream(bytes);
			output.writeByte(VERSION);
			output.writeUTF(item.getRarity().getId());
			output.writeUTF(item.has(Item.RARITY_PROFILE)
					? item.get(Item.RARITY_PROFILE) : ItemRarityProfile.DEFAULT_ID);
			output.writeInt(item.getValue());

			final List<Map.Entry<String, Double>> entries =
					new ArrayList<Map.Entry<String, Double>>(
							item.getRarityModifiers().entrySet());
			Collections.sort(entries,
					Comparator.comparing(Map.Entry<String, Double>::getKey));
			output.writeInt(entries.size());
			for (final Map.Entry<String, Double> entry : entries) {
				final String attribute = entry.getKey();
				output.writeUTF(attribute);
				output.writeDouble(entry.getValue().doubleValue());
			}

			final List<String> statistics = new ArrayList<String>();
			for (final String attribute
					: ItemRarityService.getInstance().getSupportedStatistics()) {
				if (item.has(attribute)) {
					statistics.add(attribute);
				}
			}
			Collections.sort(statistics);
			output.writeInt(statistics.size());
			for (final String attribute : statistics) {
				output.writeUTF(attribute);
				if (ItemRarityService.isIntegralStatistic(attribute)) {
					output.writeInt(item.getInt(attribute));
				} else {
					output.writeDouble(item.getDouble(attribute));
				}
			}
			output.close();
			return Base64.getUrlEncoder().withoutPadding()
					.encodeToString(bytes.toByteArray());
		} catch (final IOException e) {
			// Byte array streams should never throw an I/O error.
			throw new IllegalStateException("Unable to encode item rarity", e);
		}
	}

	/**
	 * Restores a snapshot onto a raw item definition without rolling or
	 * applying any multiplier again.
	 *
	 * @throws IllegalArgumentException if the snapshot is malformed
	 */
	public static void restore(final Item item, final String encoded) {
		if (item == null) {
			throw new IllegalArgumentException("Item must not be null");
		}
		if (encoded == null || encoded.length() == 0) {
			return;
		}
		if (encoded.length() > MAX_ENCODED_LENGTH) {
			throw new IllegalArgumentException("Item rarity snapshot is too large");
		}

		try {
			final byte[] bytes = Base64.getUrlDecoder().decode(encoded);
			final DataInputStream input = new DataInputStream(
					new ByteArrayInputStream(bytes));
			final int version = input.readUnsignedByte();
			if (version != LEGACY_VERSION && version != VERSION) {
				throw new IllegalArgumentException("Unsupported item rarity snapshot version");
			}
			final ItemRarity rarity = ItemRarity.fromId(input.readUTF());
			if (rarity == null) {
				throw new IllegalArgumentException("Unknown rarity in item snapshot");
			}
			final String profile = input.readUTF();
			if (profile.length() == 0 || profile.length() > 128) {
				throw new IllegalArgumentException("Invalid rarity profile in item snapshot");
			}
			final int value = input.readInt();
			if (value < 0) {
				throw new IllegalArgumentException("Invalid item value in rarity snapshot");
			}
			final int count = input.readInt();
			if (count < 0 || count > MAX_ENTRIES) {
				throw new IllegalArgumentException("Invalid rarity modifier count");
			}

			final List<Entry> entries = new ArrayList<Entry>(count);
			for (int index = 0; index < count; index++) {
				final String attribute = input.readUTF();
				final double multiplier = input.readDouble();
				if ((!ItemRarityModifiers.VALUE.equals(attribute)
						&& !ItemRarityService.isSupportedStatistic(attribute))
						|| !Double.isFinite(multiplier) || multiplier <= 0.0) {
					throw new IllegalArgumentException("Invalid rarity modifier");
				}
				if (version == LEGACY_VERSION) {
					final boolean statistic = input.readBoolean();
					if (statistic && !ItemRarityService.isSupportedStatistic(attribute)) {
						throw new IllegalArgumentException("Invalid rarity statistic marker");
					}
					if (statistic && ItemRarityService.isIntegralStatistic(attribute)) {
						entries.add(Entry.integral(attribute, multiplier, input.readInt()));
					} else if (statistic) {
						final double statisticValue = input.readDouble();
						if (!Double.isFinite(statisticValue)) {
							throw new IllegalArgumentException("Invalid rarity statistic value");
						}
						entries.add(Entry.floating(attribute, multiplier, statisticValue));
					} else {
						entries.add(Entry.modifier(attribute, multiplier));
					}
				} else {
					entries.add(Entry.modifier(attribute, multiplier));
				}
			}

			final List<StatisticValue> statistics = new ArrayList<StatisticValue>();
			if (version == VERSION) {
				final int statisticCount = input.readInt();
				if (statisticCount < 0 || statisticCount > MAX_STATISTICS) {
					throw new IllegalArgumentException("Invalid rarity statistic count");
				}
				final Set<String> seenStatistics = new HashSet<String>();
				for (int index = 0; index < statisticCount; index++) {
					final String attribute = input.readUTF();
					if (!ItemRarityService.isSupportedStatistic(attribute)
							|| !seenStatistics.add(attribute)) {
						throw new IllegalArgumentException("Invalid rarity statistic");
					}
					if (ItemRarityService.isIntegralStatistic(attribute)) {
						statistics.add(StatisticValue.integral(attribute, input.readInt()));
					} else {
						final double statisticValue = input.readDouble();
						if (!Double.isFinite(statisticValue)) {
							throw new IllegalArgumentException("Invalid rarity statistic value");
						}
						statistics.add(StatisticValue.floating(attribute, statisticValue));
					}
				}
			}
			if (input.available() != 0) {
				throw new IllegalArgumentException("Trailing item rarity snapshot data");
			}

			if (version == VERSION) {
				for (final String attribute
						: ItemRarityService.getInstance().getSupportedStatistics()) {
					if (item.has(attribute)) {
						item.remove(attribute);
					}
				}
				for (final StatisticValue statistic : statistics) {
					statistic.apply(item);
				}
			}
			if (item.hasMap(Item.RARITY_MODIFIERS)) {
				item.removeMap(Item.RARITY_MODIFIERS);
			}
			for (final Entry entry : entries) {
				if (version == LEGACY_VERSION && entry.statistic) {
					if (entry.integral) {
						item.put(entry.attribute, entry.integralValue);
					} else {
						item.put(entry.attribute, entry.floatingValue);
					}
				}
				item.setRarityModifier(entry.attribute, entry.multiplier);
			}
			item.setValue(value);
			item.setRarity(rarity);
			item.put(Item.RARITY_PROFILE, profile);
		} catch (final IllegalArgumentException e) {
			throw e;
		} catch (final EOFException e) {
			throw new IllegalArgumentException("Truncated item rarity snapshot", e);
		} catch (final IOException e) {
			throw new IllegalArgumentException("Invalid item rarity snapshot", e);
		}
	}

	private static final class StatisticValue {
		private final String attribute;
		private final boolean integral;
		private final int integralValue;
		private final double floatingValue;

		private StatisticValue(final String attribute, final boolean integral,
				final int integralValue, final double floatingValue) {
			this.attribute = attribute;
			this.integral = integral;
			this.integralValue = integralValue;
			this.floatingValue = floatingValue;
		}

		private static StatisticValue integral(final String attribute,
				final int value) {
			return new StatisticValue(attribute, true, value, 0.0);
		}

		private static StatisticValue floating(final String attribute,
				final double value) {
			return new StatisticValue(attribute, false, 0, value);
		}

		private void apply(final Item item) {
			if (integral) {
				item.put(attribute, integralValue);
			} else {
				item.put(attribute, floatingValue);
			}
		}
	}

	private static final class Entry {
		private final String attribute;
		private final double multiplier;
		private final boolean statistic;
		private final boolean integral;
		private final int integralValue;
		private final double floatingValue;

		private Entry(final String attribute, final double multiplier,
				final boolean statistic, final boolean integral,
				final int integralValue, final double floatingValue) {
			this.attribute = attribute;
			this.multiplier = multiplier;
			this.statistic = statistic;
			this.integral = integral;
			this.integralValue = integralValue;
			this.floatingValue = floatingValue;
		}

		private static Entry modifier(final String attribute,
				final double multiplier) {
			return new Entry(attribute, multiplier, false, false, 0, 0.0);
		}

		private static Entry integral(final String attribute,
				final double multiplier, final int value) {
			return new Entry(attribute, multiplier, true, true, value, 0.0);
		}

		private static Entry floating(final String attribute,
				final double multiplier, final double value) {
			return new Entry(attribute, multiplier, true, false, 0, value);
		}
	}
}
