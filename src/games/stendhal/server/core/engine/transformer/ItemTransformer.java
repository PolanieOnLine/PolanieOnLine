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

import java.util.Map.Entry;

import org.apache.log4j.Logger;

import games.stendhal.server.core.rule.damage.WeaponDamageRangeService;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
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

			final String name = UpdateConverter.updateItemName(rpobject.get("name"));
			final Item item = UpdateConverter.updateItem(name,
					ItemCreationContext.restore());

			if (item == null) {
				// no such item in the game anymore
				return null;
			}

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
			final boolean restoreAllAttributes = rpobject.has("persistent")
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
			} else if (savedRarity || legacyRarityItem) {
				restoreRarityInstanceAttributes(item, rpobject, savedRarity);
			}

			// Damage ranges are instance state even when rarity is disabled.
			restoreDamageRangeInstanceAttributes(item, rpobject);
			// Random affixes are also instance state and must survive RESTORE
			// independently of current XML definitions and rarity modifiers.
			ItemAffixState.restore(item, rpobject);

			if (item instanceof StackableItem) {
				int quantity = 1;
				if (rpobject.has("quantity")) {
					quantity = rpobject.getInt("quantity");
				} else {
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
					"description", "bound", "undroppableondeath",
					"uses", "improve", "max_improves", "persistent", "logid", "state"};
			for (final String attribute : individualAttributes) {
				if (rpobject.has(attribute)) {
					item.put(attribute, rpobject.get(attribute));
				}
			}
			UpdateConverter.updateItemAttributes(item);

			if (item instanceof MarkedScroll) {
				((MarkedScroll) item).applyDestInfo();
			}

			UpdateConverter.clampUpgradeLevel(item);

			// Existing weapons without a saved range are migrated from their final
			// restored ATK. The generated values are normal persistent attributes,
			// so the conversion happens only once for each item instance.
			WeaponDamageRangeService.migrateRestored(item, definitionAttack,
					definitionDamageMin, definitionDamageMax);

			if (!savedRarity && legacyRarityItem) {
				ItemRarityService.getInstance().markLegacyCommon(item);
			}

			for (RPSlot slot : rpobject.slots()) {
				RPSlot itemSlot = item.getSlot(slot.getName());
				for (RPObject obj : slot) {
					itemSlot.add(transform(obj));
				}
			}

			// Rebuild the volatile client presentation after all persisted and
			// converted values have reached their final state.
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

	private void restoreDamageRangeInstanceAttributes(final Item item,
			final RPObject saved) {
		final String[] attributes = { "damage_min", "damage_max" };
		for (final String attribute : attributes) {
			if (saved.has(attribute)) {
				item.put(attribute, saved.get(attribute));
			} else if (item.has(attribute)) {
				item.remove(attribute);
			}
		}
	}

	/**
	 * Restores only fields owned by rarity. This keeps the item's unrelated XML
	 * definition fields updateable, unlike the pre-existing persistent flag.
	 */
	private void restoreRarityInstanceAttributes(final Item item,
			final RPObject saved, final boolean savedRarity) {
		for (final String statistic
				: ItemRarityService.getInstance().getSupportedStatistics()) {
			if (saved.has(statistic)) {
				item.put(statistic, saved.get(statistic));
			} else if (item.has(statistic)) {
				item.remove(statistic);
			}
		}
		if (saved.has(Item.VALUE)) {
			item.put(Item.VALUE, saved.get(Item.VALUE));
		}
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
