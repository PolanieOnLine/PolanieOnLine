/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.engine.transformer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.core.events.seasonal.MineTownLanternSpawns;
import games.stendhal.server.core.rule.damage.WeaponDamageRangeService;
import games.stendhal.server.core.rule.glyph.GlyphProgressionService;
import games.stendhal.server.core.rule.item.ItemDefinitionRefreshService;
import games.stendhal.server.core.rule.item.ItemValueService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemAffixBalanceService;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.core.rule.rarity.LifestealMigrationService;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.ItemTooltipService;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.item.scroll.MarkedScroll;
import games.stendhal.server.entity.player.UpdateConverter;
import marauroa.common.game.RPClass;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class ItemTransformer {
	private static Logger logger = Logger.getLogger(ItemTransformer.class);

	/**
	 * Transform an <code>RPObject</code> to an item
	 *
	 * @param rpobject	the object to be transformed
	 * @return	Item corresponding to the <code>RPObject</code>
	 */
	public Item transform(RPObject rpobject) {
		// We simply ignore corpses...
		if (rpobject.get("type").equals("item")) {

			final String previousName = UpdateConverter.updateItemName(rpobject.get("name"));
			// Only lanterns marked by the festival spawner become stackable.
			final boolean festivalLantern = "latarenka".equals(previousName)
					&& rpobject.has("itemdata")
					&& rpobject.get("itemdata").matches("minetown_lantern_[0-9]{2}");
			final String name = festivalLantern ? MineTownLanternSpawns.LANTERN_NAME : previousName;
			final Item item = UpdateConverter.updateItem(name,
					ItemCreationContext.restore());

			if (item == null) {
				// no such item in the game anymore
				return null;
			}

			/*
			 * Glyphs and explicitly rarity-disabled rings use definition-owned stats.
			 * Refresh them from XML on restore so balance changes reach existing players
			 * at login instead of preserving an old rarity-scaled copy forever.
			 */
			final boolean definitionAuthoritative = item instanceof Glyph
					|| ("ring".equals(item.getItemClass())
							&& Boolean.FALSE.equals(item.getRarityEnabledOverride()));
			// Trophy DEF is advanced exclusively by Deathmatch victories.
			// Legacy rarity rolls and affixes must not be reapplied on login.
			final boolean deathmatchTrophy = "zdobyczny hełm".equals(name);

			// Build the current unmodified range before replaying this instance's
			// saved multipliers. RESTORE itself never rolls rarity or affixes.
			WeaponDamageRangeService.initialize(item, ItemCreationContext.defaultCreation());
			final Item definition = new Item(item);

			// Keep the current definition template before saved instance data can
			// replace it. It is used only when migrating an old exceptional weapon
			// which predates persisted damage ranges.
			final int definitionAttack = getStoredAttack(item);
			final Integer definitionDamageMin = getOptionalInt(item, "damage_min");
			final Integer definitionDamageMax = getOptionalInt(item, "damage_max");
			final Integer definitionMaxUpgradeLevel = getOptionalInt(item,
					Item.MAX_UPGRADE_LEVEL_ATTRIBUTE);

			item.setID(rpobject.getID());

			// update infostring to itemdata
			if (rpobject.has("infostring")) {
				rpobject.put("itemdata", rpobject.get("infostring"));
				rpobject.remove("infostring");
			}

			boolean autobind = item.has("autobind");
			final boolean savedRarity = rpobject.has(Item.RARITY_ID);
			final boolean legacyRarityItem = !savedRarity
					&& ItemRarityService.getInstance().isEligible(item);
			final boolean restoreAllAttributes = !definitionAuthoritative && !deathmatchTrophy
					&& rpobject.has("persistent")
					&& (rpobject.getInt("persistent") == 1);
			if (restoreAllAttributes) {
				// keep [new] menu
				final String menuvalue = item.get("menu");
				// keep [new] rpclass
				final RPClass rpclass = item.getRPClass();
				item.fill(rpobject);
				item.setRPClass(rpclass);
				// max_improves is a volatile XML rule, so it is not present in old
				// saved persistent instances. Keep the current definition after fill.
				if (!rpobject.has(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE)
						&& definitionMaxUpgradeLevel != null) {
					item.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE,
							definitionMaxUpgradeLevel.intValue());
				}

				// If we've updated the item name we don't want persistent reverting it
				item.put("name", name);
				// Also autobinding must work for persistent items
				if (autobind) {
					item.put("autobind", "");
				}

				// prevent persistent from removing "menu" attribute
				if (!item.has("menu") && menuvalue != null) {
					item.put("menu", menuvalue);
				}
			}

			if (!definitionAuthoritative && !deathmatchTrophy && savedRarity) {
				restoreRarityMetadata(item, rpobject, savedRarity);
			}
			final Set<String> refreshedBaseFields = ItemDefinitionRefreshService.refresh(
					item, definition, rpobject);
			// Random affixes are also instance state and must survive RESTORE
			// independently of current XML definitions and rarity modifiers. Items
			// with definition-owned stats intentionally discard legacy affix state.
			if (!definitionAuthoritative && !deathmatchTrophy) {
				ItemAffixState.restore(item, rpobject, refreshedBaseFields);
				ItemDefinitionRefreshService.replayFlatBonuses(item, rpobject);
			}

			if (item instanceof StackableItem) {
				int quantity = 1;
				if (rpobject.has("quantity")) {
					quantity = rpobject.getInt("quantity");
				} else if (!festivalLantern) {
					logger.warn("Adding quantity=1 to "
							+ rpobject
							+ ". Most likely cause is that this item was not stackable in the past");
				}
				((StackableItem) item).setQuantity(quantity);

				if (quantity <= 0) {
					logger.warn("Ignoring item "
							+ name
							+ " because this item has an invalid quantity: "
							+ quantity);
					return null;
				}
			}

			// make sure saved individual information is restored
			final String[] individualAttributes = { "itemdata",
					"bound", "undroppableondeath", "owner",
					"uses", "improve", "persistent", "logid", "state"};
			for (final String attribute : individualAttributes) {
				if (rpobject.has(attribute)) {
					item.put(attribute, rpobject.get(attribute));
				}
			}
			UpdateConverter.updateItemAttributes(item);

			if (item instanceof MarkedScroll) {
				((MarkedScroll) item).applyDestInfo();
			}

			// Fill any missing/invalid range after refreshing the current base and
			// replaying saved modifiers. Already rebuilt valid ranges stay intact.
			WeaponDamageRangeService.migrateRestored(item, definitionAttack,
					definitionDamageMin, definitionDamageMax);

			if (!savedRarity && legacyRarityItem && !deathmatchTrophy) {
				ItemRarityService.getInstance().markLegacyCommon(item);
			}

			for (RPSlot slot : rpobject.slots()) {
				final List<RPObject> contents = new ArrayList<RPObject>();
				for (RPObject obj : slot) {
					contents.add(obj);
				}
				if (!item.hasSlot(slot.getName())) {
					item.addSlot(new RPSlot(slot.getName()));
				}
				final RPSlot itemSlot = item.getSlot(slot.getName());
				// fill() already copies persistent container contents. Replace that
				// copy with the transformed children instead of duplicating them.
				itemSlot.clear();
				final RPSlot definitionSlot = definition.getSlot(slot.getName());
				if (definitionSlot != null) {
					final int capacity = definitionSlot.getCapacity();
					itemSlot.setCapacity(capacity < 0 ? capacity : Math.max(capacity, contents.size()));
				}
				for (final RPObject obj : contents) {
					final Item child = transform(obj);
					if (child != null) itemSlot.add(child);
				}
			}

			// Rebuild the volatile client presentation after all persisted and
			// converted values have reached their final state.
			if (!definitionAuthoritative && !deathmatchTrophy) {
				LifestealMigrationService.migrate(item, rpobject);
			}
			ItemAffixBalanceService.enforce(item);
			ItemValueService.refresh(item);
			GlyphProgressionService.restore(item, rpobject);
			ItemTooltipService.update(item);
			return item;
		} else {
			logger.warn("Non-item object found: " + rpobject);
			return null;
		}
	}

	private int getStoredAttack(final Item item) {
		final int melee = item.has("atk") ? Math.max(0, item.getInt("atk")) : 0;
		final int ranged = item.has("ratk") ? Math.max(0, item.getInt("ratk")) : 0;
		return Math.max(melee, ranged);
	}

	private Integer getOptionalInt(final Item item, final String attribute) {
		return item.has(attribute) ? Integer.valueOf(item.getInt(attribute)) : null;
	}

	/** Restores the saved roll, never the obsolete materialized base stats. */
	private void restoreRarityMetadata(final Item item,
			final RPObject saved, final boolean savedRarity) {
		if (!savedRarity) {
			return;
		}

		item.put(Item.RARITY_ID, saved.get(Item.RARITY_ID));
		if (saved.has(Item.RARITY_PROFILE)) {
			item.put(Item.RARITY_PROFILE, saved.get(Item.RARITY_PROFILE));
		}
		if (saved.hasMap(Item.RARITY_MODIFIERS)) {
			for (final Entry<String, String> entry
					: saved.getMap(Item.RARITY_MODIFIERS).entrySet()) {
				item.put(Item.RARITY_MODIFIERS, entry.getKey(), entry.getValue());
			}
		}
	}
}
