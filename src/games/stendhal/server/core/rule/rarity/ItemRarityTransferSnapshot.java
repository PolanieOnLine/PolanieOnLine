/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemTooltipService;

/**
 * Compact, versioned rarity state used when one item definition is transformed
 * into another. Unlike {@link ItemRaritySnapshot}, it reapplies the saved roll
 * to the target definition's base statistics instead of copying final values.
 */
public final class ItemRarityTransferSnapshot {
	private static final int VERSION = 1;
	private static final int MAX_ENTRIES = 64;
	private static final int MAX_ENCODED_LENGTH = 32768;

	private ItemRarityTransferSnapshot() {
		// utility class
	}

	/** Captures tier, exact roll, compatible affixes and upgrade level. */
	public static String encode(final Item item) {
		if (item == null) {
			return "";
		}
		try {
			final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
			final DataOutputStream output = new DataOutputStream(bytes);
			output.writeByte(VERSION);
			output.writeUTF(item.getItemClass());
			output.writeUTF(item.getRarityOrCommon().getId());
			output.writeUTF(item.has(Item.RARITY_PROFILE)
					? item.get(Item.RARITY_PROFILE)
					: normalizedProfile(item.getRarityProfile()));
			writeDoubleMap(output, item.getRarityModifiers());
			final Long seed = ItemAffixState.getSeed(item);
			output.writeBoolean(seed != null);
			if (seed != null) {
				output.writeLong(seed.longValue());
			}
			writeStringMap(output, ItemAffixState.getValues(item));
			output.writeInt(item.getUpgradeLevel());
			output.close();
			return Base64.getUrlEncoder().withoutPadding()
					.encodeToString(bytes.toByteArray());
		} catch (final IOException e) {
			throw new IllegalStateException("Unable to encode item rarity transfer", e);
		}
	}

	/**
	 * Applies a captured roll to a raw target item. The target keeps its own base
	 * statistics and receives only affixes which remain eligible for it.
	 */
	public static void apply(final Item target, final String encoded) {
		if (target == null) {
			throw new IllegalArgumentException("Target item must not be null");
		}
		if (encoded == null || encoded.length() == 0) {
			return;
		}
		final TransferData data = decode(encoded);
		final ItemRarityService service = ItemRarityService.getInstance();
		final ItemRarityModifiers modifiers = completeModifiers(target, data, service);
		final ItemCreationContext context = ItemCreationContext
				.builder(ItemCreationContext.Source.QUEST)
				.withRarity(data.rarity)
				.withProfile(data.profile)
				.withModifiers(modifiers)
				.randomizeModifiers(false)
				.generateAffixes(false)
				.build();
		service.initialize(target, context);
		restoreCompatibleAffixes(target, data);
		if (target.hasUpgradeLimit()) {
			target.setUpgradeLevel(Math.min(data.upgradeLevel,
					target.getMaxUpgradeLevel()));
		}
		ItemTooltipService.update(target);
	}

	private static ItemRarityModifiers completeModifiers(final Item target,
			final TransferData data, final ItemRarityService service) {
		final ItemRarityModifiers.Builder builder = ItemRarityModifiers.builder();
		for (final Map.Entry<String, Double> entry : data.modifiers.entrySet()) {
			builder.multiplier(entry.getKey(), entry.getValue().doubleValue());
		}
		final ItemRarityProfile.Tier tier = service.getProfile(data.profile)
				.getTier(data.rarity);
		for (final String statistic : service.getSupportedStatistics()) {
			if (target.has(statistic) && !data.modifiers.containsKey(statistic)) {
				builder.multiplier(statistic, tier.midpointStatMultiplier());
			}
		}
		if (!data.modifiers.containsKey(ItemRarityModifiers.VALUE)) {
			builder.valueMultiplier(tier.getValueMultiplier());
		}
		return builder.build();
	}

	private static void restoreCompatibleAffixes(final Item target,
			final TransferData data) {
		if (data.affixSeed != null) {
			ItemAffixState.setSeed(target, data.affixSeed.longValue());
		}
		for (final Map.Entry<String, String> entry : data.affixes.entrySet()) {
			final ItemAffixDefinition definition = findDefinition(entry.getKey());
			if (definition == null) {
				target.put(ItemAffixState.ATTRIBUTE, entry.getKey(), entry.getValue());
			} else if (data.itemClass.equals(target.getItemClass())
					|| definition.isEligible(target)) {
				target.put(ItemAffixState.ATTRIBUTE, entry.getKey(), entry.getValue());
				target.put(definition.getAttribute(), entry.getValue());
			}
		}
	}

	private static ItemAffixDefinition findDefinition(final String id) {
		final ItemAffixDefinition regular = ItemAffixRegistry.getInstance().get(id);
		return regular != null ? regular
				: LegendaryItemAffixRegistry.getInstance().get(id);
	}

	private static TransferData decode(final String encoded) {
		if (encoded.length() > MAX_ENCODED_LENGTH) {
			throw new IllegalArgumentException("Item rarity transfer is too large");
		}
		try {
			final DataInputStream input = new DataInputStream(new ByteArrayInputStream(
					Base64.getUrlDecoder().decode(encoded)));
			if (input.readUnsignedByte() != VERSION) {
				throw new IllegalArgumentException(
						"Unsupported item rarity transfer version");
			}
			final String itemClass = input.readUTF();
			if (itemClass.length() == 0 || itemClass.length() > 128) {
				throw new IllegalArgumentException("Invalid item class in transfer");
			}
			final ItemRarity rarity = ItemRarity.fromId(input.readUTF());
			if (rarity == null) {
				throw new IllegalArgumentException("Unknown item rarity in transfer");
			}
			final String profile = input.readUTF();
			if (profile.length() == 0 || profile.length() > 128) {
				throw new IllegalArgumentException("Invalid rarity profile in transfer");
			}
			final Map<String, Double> modifiers = readDoubleMap(input);
			final Long seed = input.readBoolean()
					? Long.valueOf(input.readLong()) : null;
			final Map<String, String> affixes = readStringMap(input);
			final int upgradeLevel = input.readInt();
			if (upgradeLevel < 0 || input.available() != 0) {
				throw new IllegalArgumentException("Invalid item rarity transfer data");
			}
			return new TransferData(itemClass, rarity, profile, modifiers, seed, affixes,
					upgradeLevel);
		} catch (final IllegalArgumentException e) {
			throw e;
		} catch (final EOFException e) {
			throw new IllegalArgumentException("Truncated item rarity transfer", e);
		} catch (final IOException e) {
			throw new IllegalArgumentException("Invalid item rarity transfer", e);
		}
	}

	private static void writeDoubleMap(final DataOutputStream output,
			final Map<String, Double> values) throws IOException {
		final List<Map.Entry<String, Double>> entries =
				new ArrayList<Map.Entry<String, Double>>(values.entrySet());
		Collections.sort(entries,
				Comparator.comparing(Map.Entry<String, Double>::getKey));
		output.writeInt(entries.size());
		for (final Map.Entry<String, Double> entry : entries) {
			output.writeUTF(entry.getKey());
			output.writeDouble(entry.getValue().doubleValue());
		}
	}

	private static Map<String, Double> readDoubleMap(final DataInputStream input)
			throws IOException {
		final int count = readCount(input);
		final Map<String, Double> values = new LinkedHashMap<String, Double>();
		for (int index = 0; index < count; index++) {
			final String key = input.readUTF();
			final double value = input.readDouble();
			if ((!ItemRarityModifiers.VALUE.equals(key)
					&& !ItemRarityService.isSupportedStatistic(key))
					|| !Double.isFinite(value) || value <= 0.0
					|| values.put(key, Double.valueOf(value)) != null) {
				throw new IllegalArgumentException("Invalid rarity modifier in transfer");
			}
		}
		return values;
	}

	private static void writeStringMap(final DataOutputStream output,
			final Map<String, String> values) throws IOException {
		final List<Map.Entry<String, String>> entries =
				new ArrayList<Map.Entry<String, String>>(values.entrySet());
		Collections.sort(entries,
				Comparator.comparing(Map.Entry<String, String>::getKey));
		output.writeInt(entries.size());
		for (final Map.Entry<String, String> entry : entries) {
			output.writeUTF(entry.getKey());
			output.writeUTF(entry.getValue());
		}
	}

	private static Map<String, String> readStringMap(final DataInputStream input)
			throws IOException {
		final int count = readCount(input);
		final Map<String, String> values = new LinkedHashMap<String, String>();
		for (int index = 0; index < count; index++) {
			final String key = input.readUTF();
			final String value = input.readUTF();
			if (key.length() == 0 || values.put(key, value) != null) {
				throw new IllegalArgumentException("Invalid affix in rarity transfer");
			}
		}
		return values;
	}

	private static int readCount(final DataInputStream input) throws IOException {
		final int count = input.readInt();
		if (count < 0 || count > MAX_ENTRIES) {
			throw new IllegalArgumentException("Invalid item rarity transfer count");
		}
		return count;
	}

	private static String normalizedProfile(final String profile) {
		return profile == null || profile.trim().length() == 0
				? ItemRarityProfile.DEFAULT_ID : profile.trim();
	}

	private static final class TransferData {
		private final String itemClass;
		private final ItemRarity rarity;
		private final String profile;
		private final Map<String, Double> modifiers;
		private final Long affixSeed;
		private final Map<String, String> affixes;
		private final int upgradeLevel;

		private TransferData(final String itemClass, final ItemRarity rarity,
				final String profile,
				final Map<String, Double> modifiers, final Long affixSeed,
				final Map<String, String> affixes, final int upgradeLevel) {
			this.itemClass = itemClass;
			this.rarity = rarity;
			this.profile = profile;
			this.modifiers = modifiers;
			this.affixSeed = affixSeed;
			this.affixes = affixes;
			this.upgradeLevel = upgradeLevel;
		}
	}
}
