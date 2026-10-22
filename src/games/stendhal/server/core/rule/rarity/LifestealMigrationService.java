/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.item.Item;
import marauroa.common.game.RPObject;

/** Converts the old guaranteed-affix representation without rerolling an item. */
public final class LifestealMigrationService {
	private static final Logger LOGGER = Logger.getLogger(LifestealMigrationService.class);
	private static final long COMPENSATION_SALT = 0x4c49464553544541L;

	private LifestealMigrationService() { }

	public static void migrate(final Item item, final RPObject saved) {
		if (item == null || saved == null) return;
		if (item.getDefinitionLifesteal() == 0.0 && !saved.has("lifesteal")
				&& !WeaponAffixService.isWeapon(item)) return;
		copyBackup(item, saved);
		final boolean converted = saved.has(ItemLifestealService.VERSION_ATTRIBUTE)
				&& saved.getInt(ItemLifestealService.VERSION_ATTRIBUTE) >= ItemLifestealService.VERSION;
		final double authored = item.getDefinitionLifesteal();
		final boolean guaranteed = !converted && authored > 0.0
				&& WeaponAffixService.isWeapon(item) && saved.hasMap(ItemAffixState.ATTRIBUTE)
				&& saved.getMap(ItemAffixState.ATTRIBUTE).containsKey("lifesteal");

		if (!converted && (authored != 0.0 || saved.has("lifesteal"))) {
			backup(item, saved, "lifesteal", "lifesteal");
			if (saved.hasMap(Item.RARITY_MODIFIERS)
					&& saved.getMap(Item.RARITY_MODIFIERS).containsKey("lifesteal")) {
				item.put(ItemLifestealService.LEGACY_ATTRIBUTE, "modifier",
						saved.getMap(Item.RARITY_MODIFIERS).get("lifesteal"));
			}
		}
		if (authored != 0.0) {
			item.put("lifesteal", authored);
			if (item.hasMap(Item.RARITY_MODIFIERS)) item.remove(Item.RARITY_MODIFIERS, "lifesteal");
		} else if (saved.has("lifesteal") && !item.has("lifesteal")) {
			// Preserve unclassified legacy grants, without inventing an affix slot.
			item.put("lifesteal", saved.get("lifesteal"));
		}

		if (guaranteed) {
			item.put(ItemLifestealService.LEGACY_ATTRIBUTE, "affix",
					saved.getMap(ItemAffixState.ATTRIBUTE).get("lifesteal"));
			item.remove(ItemAffixState.ATTRIBUTE, "lifesteal");
			final int missing = ItemAffixGenerator.getSlotCount(item.getRarityOrCommon())
					- regularCount(item);
			if (missing > 0) {
				final Long oldSeed = ItemAffixState.getSeed(item);
				final long seed = oldSeed != null ? oldSeed.longValue()
						: ((long) (saved.has("id") ? saved.getInt("id") : 0) << 32) ^ item.getName().hashCode();
				final java.util.List<String> added = new ItemAffixGenerator(new Random(0))
						.refillRegularAffixes(item, 1, seed ^ COMPENSATION_SALT);
				if (added.isEmpty()) {
					LOGGER.warn("Unable to compensate life-steal affix for " + item.getName());
					// Preserve the old representation so a subsequent saved retry
					// still knows that this item is owed a replacement slot.
					item.put(ItemAffixState.ATTRIBUTE, "lifesteal",
							saved.getMap(ItemAffixState.ATTRIBUTE).get("lifesteal"));
					if (saved.has("lifesteal")) item.put("lifesteal", saved.get("lifesteal"));
					if (saved.hasMap(Item.RARITY_MODIFIERS)
							&& saved.getMap(Item.RARITY_MODIFIERS).containsKey("lifesteal")) {
						item.put(Item.RARITY_MODIFIERS, "lifesteal",
								saved.getMap(Item.RARITY_MODIFIERS).get("lifesteal"));
					}
					return;
				}
				item.put(ItemLifestealService.LEGACY_ATTRIBUTE, "replacement", added.get(0));
			}
		}
		item.put(ItemLifestealService.VERSION_ATTRIBUTE, ItemLifestealService.VERSION);
	}

	private static int regularCount(final Item item) {
		int count = 0;
		for (final String id : ItemAffixState.getValues(item).keySet()) {
			if (LegendaryItemAffixRegistry.getInstance().get(id) == null) count++;
		}
		return count;
	}

	private static void backup(final Item item, final RPObject saved,
			final String source, final String target) {
		if (saved.has(source)) item.put(ItemLifestealService.LEGACY_ATTRIBUTE, target, saved.get(source));
		else item.put(ItemLifestealService.LEGACY_ATTRIBUTE, target + "_absent", "true");
	}

	private static void copyBackup(final Item item, final RPObject saved) {
		if (saved.hasMap(ItemLifestealService.LEGACY_ATTRIBUTE)) {
			for (final Map.Entry<String, String> entry : saved.getMap(ItemLifestealService.LEGACY_ATTRIBUTE).entrySet()) {
				item.put(ItemLifestealService.LEGACY_ATTRIBUTE, entry.getKey(), entry.getValue());
			}
		}
	}
}
