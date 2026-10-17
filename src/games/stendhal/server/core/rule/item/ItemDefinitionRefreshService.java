/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.rarity.EquipmentAffixService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.core.rule.rarity.ItemRollPrecision;
import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.core.rule.rarity.WeaponAffixService;
import games.stendhal.server.entity.item.GateKey;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.HouseKey;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.RingOfTeleportation;
import marauroa.common.game.RPObject;

/** Rebuilds definition-owned fields without changing saved instance progress. */
public final class ItemDefinitionRefreshService {
	private static final Set<String> INSTANCE_FIELDS = new HashSet<String>(Arrays.asList(
			"id", "zoneid", "x", "y", "quantity", "itemdata", "infostring",
			"bound", "owner", "uses", "improve", "persistent", "logid", "state",
			"description", Item.DEFINITION_DESCRIPTION,
			Item.RARITY_ID, Item.RARITY_PROFILE));
	private static final Set<String> DEFINITION_FIELDS = new HashSet<String>(Arrays.asList(
			"regen", "frequency", "max_quantity", "min_use", "min_level",
			"antipoison", "immunization", "life_support", "durability",
			"statusattack", "itemset", "use_sound", "menu", "max_improves",
			"slot_name", "slot_size", "autobind"));

	private ItemDefinitionRefreshService() { }

	/** Returns base fields which must not be restored as undocumented old affixes. */
	public static Set<String> refresh(final Item item, final Item definition,
			final RPObject saved) {
		final Set<String> baseFields = new HashSet<String>();
		final Set<String> statistics = ItemRarityService.getInstance().getSupportedStatistics();
		for (final String attribute : DEFINITION_FIELDS) {
			if (!definition.has(attribute) && item.has(attribute)) {
				item.remove(attribute);
			}
		}
		for (final String attribute : statistics) {
			if (definition.has(attribute) || item.getRarityModifier(attribute) != null) {
				baseFields.add(attribute);
			}
			if (!definition.has(attribute) && item.has(attribute)) {
				item.remove(attribute);
			}
		}
		for (final String attribute : definition) {
			if (INSTANCE_FIELDS.contains(attribute)
					|| ("subclass".equals(attribute) && (item instanceof HouseKey || item instanceof GateKey
							|| item instanceof RingOfTeleportation))
					|| ("amount".equals(attribute) && "ring".equals(item.getItemClass()))) {
				continue;
			}
			// The Deathmatch trophy earns its defense from arena victories, not XML.
			// Restoring the definition would reset every earned bonus to def=1.
			if ("def".equals(attribute) && "zdobyczny hełm".equals(item.getName())
					&& saved.has("def")) {
				item.put("def", saved.get("def"));
				baseFields.add("def");
				continue;
			}
			item.put(attribute, definition.get(attribute));
			if (statistics.contains(attribute)) {
				baseFields.add(attribute);
				replayModifier(item, attribute, definition.get(attribute));
			}
		}
		// A newly generated Common weapon suppresses intrinsic life steal. Do not
		// reintroduce it on login; old Common weapons that owned it still refresh.
		if (item.getRarity() == ItemRarity.COMMON && WeaponAffixService.isWeapon(item) && !saved.has("lifesteal")
				&& item.has("lifesteal")) {
			item.remove("lifesteal");
		}
		refreshDescription(item, definition, saved);
		preserveUpgrade(item, saved);
		return baseFields;
	}

	private static void replayModifier(final Item item, final String attribute,
			final String baseValue) {
		Double savedMultiplier = item.getRarityModifier(attribute);
		if (savedMultiplier == null && ("damage_min".equals(attribute) || "damage_max".equals(attribute))) {
			savedMultiplier = item.getRarityModifier("atk");
			if (savedMultiplier == null) {
				savedMultiplier = item.getRarityModifier("ratk");
			}
			if (savedMultiplier != null && Double.isFinite(savedMultiplier.doubleValue())
					&& savedMultiplier.doubleValue() > 0.0) {
				item.setRarityModifier(attribute, savedMultiplier.doubleValue());
			}
		}
		final double multiplier = savedMultiplier != null
				&& Double.isFinite(savedMultiplier.doubleValue())
				&& savedMultiplier.doubleValue() > 0.0 ? savedMultiplier.doubleValue() : 1.0;
		final double base = Double.parseDouble(baseValue);
		if (base <= 0.0 || item instanceof Glyph || Double.compare(multiplier, 1.0) == 0) {
			return;
		}
		final double adjusted = "rate".equals(attribute) ? base / multiplier : base * multiplier;
		if (isIntegral(attribute)) {
			item.put(attribute, Math.max(1, (int) Math.min(Short.MAX_VALUE,
					Math.round(adjusted + 0.000000001))));
		} else {
			item.put(attribute, ItemRollPrecision.round(Math.min(Float.MAX_VALUE, adjusted)));
		}
	}

	/** Flat ATK/legacy DEF affixes are part of the raw stat, not only metadata. */
	public static void replayFlatBonuses(final Item item, final RPObject saved) {
		replayFlatBonus(item, saved, EquipmentAffixService.FLAT_ATTACK_BONUS_ATTRIBUTE, "atk");
		replayFlatBonus(item, saved, EquipmentAffixService.FLAT_DEFENSE_BONUS_ATTRIBUTE, "def");
		replayFlatBonus(item, saved, LegendaryEquipmentAffixService.RELIC_POWER_ATTRIBUTE, "atk");
		replayFlatBonus(item, saved, LegendaryEquipmentAffixService.BASTION_BONUS_ATTRIBUTE, "def");
		preserveUpgrade(item, saved);
	}

	private static void replayFlatBonus(final Item item, final RPObject saved,
			final String affix, final String statistic) {
		String value = saved.has(affix) ? saved.get(affix) : null;
		if (saved.hasMap(ItemAffixState.ATTRIBUTE)
				&& saved.getMap(ItemAffixState.ATTRIBUTE).containsKey(affix)) {
			value = saved.getMap(ItemAffixState.ATTRIBUTE).get(affix);
		}
		if (value == null) return;
		try {
			final int bonus = Integer.parseInt(value);
			if (bonus <= 0) return;
			item.put(affix, bonus);
			final long base = item.has(statistic) ? item.getInt(statistic) : 0;
			item.put(statistic, (int) Math.max(Short.MIN_VALUE,
					Math.min(Short.MAX_VALUE, base + bonus)));
		} catch (final NumberFormatException e) {
			// Keep damaged optional metadata without breaking player login.
		}
	}

	private static boolean isIntegral(final String attribute) {
		return Arrays.asList("atk", "ratk", "damage_min", "damage_max", "rate", "def",
				"range", "skill_atk", "rate_increase", "health").contains(attribute);
	}

	private static void refreshDescription(final Item item, final Item definition,
			final RPObject saved) {
		final String base = definition.getDescription() == null ? "" : definition.getDescription();
		// Replace the former shadow lore even on legacy stacks carrying itemdata.
		// The material itself, its quantity and individual metadata stay intact.
		final boolean renamedBlackIron = "resource".equals(item.getItemClass())
				&& Arrays.asList("ruda czarnego żelaza", "sztabka czarnego żelaza").contains(item.getName())
				&& !item.getName().equals(saved.get("name"));
		final boolean recordedCustomText = saved.has(Item.DEFINITION_DESCRIPTION)
				&& saved.has("description")
				&& !saved.get("description").equals(saved.get(Item.DEFINITION_DESCRIPTION));
		// Older saves lack the marker. Keep personal documents and quest itemdata.
		final boolean legacyPersonalText = !saved.has(Item.DEFINITION_DESCRIPTION)
				&& (saved.has("itemdata") || Arrays.asList("key", "scroll", "book", "document")
						.contains(item.getItemClass()));
		if (!renamedBlackIron && !(item instanceof Glyph) && saved.has("description")
				&& (recordedCustomText || legacyPersonalText)) {
			item.setDescription(saved.get("description"));
		} else {
			item.setDescription(base);
		}
		item.put(Item.DEFINITION_DESCRIPTION, base);
	}

	private static void preserveUpgrade(final Item item, final RPObject saved) {
		if (!saved.has(Item.UPGRADE_LEVEL_ATTRIBUTE)) {
			return;
		}
		final int upgrade = Math.max(0, saved.getInt(Item.UPGRADE_LEVEL_ATTRIBUTE));
		item.put(Item.UPGRADE_LEVEL_ATTRIBUTE, upgrade);
		// A changed/removed XML limit must not erase already paid-for upgrades.
		if (upgrade > 0 && item.hasUpgradeableCombatStats()
				&& upgrade > item.getMaxUpgradeLevel()) {
			item.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, upgrade);
		}
	}
}
