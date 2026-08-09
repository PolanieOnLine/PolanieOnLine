/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import games.stendhal.server.core.rule.damage.WeaponAffixCombatService;
import games.stendhal.server.entity.item.Item;

/** Registry of signature affixes reserved for legendary item drops. */
public final class LegendaryItemAffixRegistry {
	private static final Set<String> DEEP_WOUNDS_WEAPON_CLASSES =
			Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(
					"sword", "dagger", "axe", "whip")));

	private static final LegendaryItemAffixRegistry INSTANCE =
			new LegendaryItemAffixRegistry(Arrays.<ItemAffixDefinition>asList(
					new DeepWoundsAffixDefinition()));

	private final List<ItemAffixDefinition> definitions;
	private final Map<String, ItemAffixDefinition> byId;

	LegendaryItemAffixRegistry(final List<ItemAffixDefinition> definitions) {
		if (definitions == null) {
			throw new IllegalArgumentException(
					"Legendary affix definitions must not be null");
		}
		final List<ItemAffixDefinition> copy =
				new ArrayList<ItemAffixDefinition>();
		final Map<String, ItemAffixDefinition> ids =
				new LinkedHashMap<String, ItemAffixDefinition>();
		for (final ItemAffixDefinition definition : definitions) {
			if (definition == null || definition.getId() == null
					|| definition.getId().trim().length() == 0) {
				throw new IllegalArgumentException(
						"Legendary affix id must not be empty");
			}
			if (ids.containsKey(definition.getId())) {
				throw new IllegalArgumentException(
						"Duplicate legendary affix id: " + definition.getId());
			}
			copy.add(definition);
			ids.put(definition.getId(), definition);
		}
		definitions = Collections.unmodifiableList(copy);
		this.definitions = definitions;
		this.byId = Collections.unmodifiableMap(ids);
	}

	public static LegendaryItemAffixRegistry getInstance() {
		return INSTANCE;
	}

	public ItemAffixDefinition get(final String id) {
		return byId.get(id);
	}

	public List<ItemAffixDefinition> getDefinitions() {
		return definitions;
	}

	public List<ItemAffixDefinition> getEligible(final Item item) {
		final List<ItemAffixDefinition> result =
				new ArrayList<ItemAffixDefinition>();
		for (final ItemAffixDefinition definition : definitions) {
			if (!ItemAffixState.has(item, definition.getId())
					&& definition.isEligible(item)) {
				result.add(definition);
			}
		}
		return result;
	}

	private static final class DeepWoundsAffixDefinition
			implements ItemAffixDefinition {
		@Override
		public String getId() {
			return WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE;
		}

		@Override
		public String getAttribute() {
			return WeaponAffixCombatService.LEGENDARY_DEEP_WOUNDS_ATTRIBUTE;
		}

		@Override
		public boolean isEligible(final Item item) {
			return item != null
					&& DEEP_WOUNDS_WEAPON_CLASSES.contains(item.getItemClass())
					&& !item.has(getAttribute());
		}

		@Override
		public boolean apply(final Item item, final Random random) {
			if (!isEligible(item)) {
				return false;
			}
			// Signature affixes use a stable marker rather than a rolled percentage.
			item.put(getAttribute(), 1.0);
			return true;
		}
	}
}
