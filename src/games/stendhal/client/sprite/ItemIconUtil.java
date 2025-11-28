/***************************************************************************
 *                      (C) Copyright 2024 - PolanieOnLine                 *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.sprite;

import java.awt.Color;

import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/**
 * Shared icon lookup used by item log and improvement UI to resolve sprites
 * from item class/subclass identifiers.
 */
public final class ItemIconUtil {
	private ItemIconUtil() {
		// utility
	}

	/**
	 * Resolve a sprite for the given item class and subclass. Falls back to a
	 * placeholder bag icon if the identifiers are missing or the sprite cannot be
	 * loaded.
	 *
	 * @param clazz    item class
	 * @param subclass item subclass
	 * @return sprite representing the item or a placeholder
	 */
	public static Sprite getItemSprite(final String clazz, final String subclass) {
		if (clazz == null || clazz.isEmpty() || subclass == null || subclass.isEmpty()) {
			return getPlaceholderSprite();
		}

		try {
			final SpriteStore store = SpriteStore.get();
			final String path = "/data/sprites/items/" + clazz + "/" + subclass + ".png";
			Sprite sprite = store.getSprite(path);
			if (sprite.getWidth() > sprite.getHeight()) {
				sprite = store.getAnimatedSprite(sprite, 100);
			}
			return sprite;
		} catch (final Exception e) {
			return getPlaceholderSprite();
		}
	}

	/**
	 * @return the standard placeholder sprite used when no specific item sprite is
	 *         available.
	 */
	public static Sprite getPlaceholderSprite() {
		return SpriteStore.get().getColoredSprite("/data/gui/bag.png", Color.LIGHT_GRAY);
	}
}
