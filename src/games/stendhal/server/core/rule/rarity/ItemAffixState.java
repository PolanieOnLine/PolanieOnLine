/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import games.stendhal.server.entity.item.Item;
import marauroa.common.game.RPObject;

/** Persistent instance state for randomly rolled item affixes. */
public final class ItemAffixState {
	/** Hidden persistent map: stable affix id -> exact materialized value. */
	public static final String ATTRIBUTE = "item_affixes";

	private ItemAffixState() {
		// utility class
	}

	public static boolean hasAny(final Item item) {
		return item != null && item.hasMap(ATTRIBUTE)
				&& !item.getMap(ATTRIBUTE).isEmpty();
	}

	public static boolean has(final Item item, final String affixId) {
		return item != null && affixId != null && item.hasMap(ATTRIBUTE)
				&& item.getMap(ATTRIBUTE).containsKey(affixId);
	}

	/**
	 * Records the exact value already materialized by an affix definition.
	 */
	public static void record(final Item item,
			final ItemAffixDefinition definition) {
		if (item == null || definition == null
				|| !item.has(definition.getAttribute())) {
			return;
		}
		item.put(ATTRIBUTE, definition.getId(),
				item.get(definition.getAttribute()));
	}

	/** Returns an immutable snapshot of the persisted affix map. */
	public static Map<String, String> getValues(final Item item) {
		if (item == null || !item.hasMap(ATTRIBUTE)) {
			return Collections.emptyMap();
		}
		return Collections.unmodifiableMap(
				new LinkedHashMap<String, String>(item.getMap(ATTRIBUTE)));
	}

	/**
	 * Restores saved affix metadata and rematerializes known gameplay
	 * attributes. Unknown ids remain preserved in the map for forward/backward
	 * compatibility but are not applied to gameplay.
	 *
	 * Older saves created before the affix map existed may contain only the
	 * materialized attribute. Preserve those values as intrinsic/legacy state
	 * without retroactively marking them as randomly rolled affixes.
	 */
	public static void restore(final Item item, final RPObject saved) {
		if (item == null || saved == null) {
			return;
		}

		if (!saved.hasMap(ATTRIBUTE)) {
			for (final ItemAffixDefinition definition
					: ItemAffixRegistry.getInstance().getDefinitions()) {
				if (saved.has(definition.getAttribute())) {
					item.put(definition.getAttribute(),
							saved.get(definition.getAttribute()));
				}
			}
			return;
		}

		for (final Entry<String, String> entry
				: saved.getMap(ATTRIBUTE).entrySet()) {
			item.put(ATTRIBUTE, entry.getKey(), entry.getValue());
			final ItemAffixDefinition definition =
					ItemAffixRegistry.getInstance().get(entry.getKey());
			if (definition != null) {
				item.put(definition.getAttribute(), entry.getValue());
			}
		}
	}
}
