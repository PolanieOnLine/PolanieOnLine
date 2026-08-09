/***************************************************************************
 *                   (C) Copyright 2003-2023 - Marauroa                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.entity;

import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

public class Item extends Entity {
	/** Attribute used by the server to expose an item's rarity. */
	public static final String RARITY_ID_ATTRIBUTE = "rarity_id";

	/**
	 * The content slot, or <code>null</code> if the item has none or it's not
	 * accessible.
	 */
	private RPSlot content;

	/** Quantity property. */
	public static final Property PROP_QUANTITY = new Property();
	/** The item quantity. */
	private int quantity;

	/**
	 * Create an item.
	 */
	public Item() {
		quantity = 0;
	}

	/**
	 * Initialize this entity for an object.
	 *
	 * @param object
	 *            The object.
	 *
	 * @see #release()
	 */
	@Override
	public void initialize(final RPObject object) {
		super.initialize(object);

		if (object.hasSlot("content")) {
			content = object.getSlot("content");
		} else {
			content = null;
		}
	}

	/**
	 * Get the content slot.
	 *
	 * @return Content slot or <code>null</code> if the item has none or it's
	 * not accessible.
	 */
	public RPSlot getContent() {
		return content;
	}

	/**
	 * Get the item quantity.
	 *
	 * @return The number of items.
	 */
	public int getQuantity() {
		return quantity;
	}

	/**
	 * Get the rarity assigned to this item.
	 *
	 * @return rarity, or {@code null} for legacy, excluded, or malformed items
	 */
	public ItemRarity getRarity() {
		return getRarity(rpObject);
	}

	/**
	 * Read rarity information directly from an item object. This is also used
	 * by item-information objects displayed in shop lists.
	 *
	 * @param object item object
	 * @return rarity, or {@code null} if no supported rarity is present
	 */
	public static ItemRarity getRarity(final RPObject object) {
		if ((object == null) || !object.has(RARITY_ID_ATTRIBUTE)) {
			return null;
		}

		final String rarityId = object.get(RARITY_ID_ATTRIBUTE);
		final ItemRarity rarity = ItemRarity.fromId(rarityId);
		if ((rarity == null) || !rarity.getId().equals(rarityId)) {
			return null;
		}
		return rarity;
	}

	/**
	 * The object added/changed attribute(s).
	 *
	 * @param object
	 *            The base object.
	 * @param changes
	 *            The changes.
	 */
	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);

		if (changes.has("state")) {
			fireChange(PROP_STATE);
		}
		if (changes.has("quantity")) {
			quantity = changes.getInt("quantity");
			fireChange(PROP_QUANTITY);
		}
	}

	public int getState() {
		if (rpObject.has("state")) {
			return rpObject.getInt("state");
		}
		return 0;
	}
}
