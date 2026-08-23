/***************************************************************************
 *                   (C) Copyright 2003-2013 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import games.stendhal.client.IGameScreen;
import games.stendhal.client.ZoneInfo;
import games.stendhal.client.entity.StatefulEntity;
import games.stendhal.client.sprite.EmptySprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import marauroa.common.game.RPObject;

class VariableSpriteEntity2DView<T extends StatefulEntity> extends Entity2DView<T> {

	private static final String TILESET_ATTRIBUTE = "tileset";
	private static final String TILE_INDEX_ATTRIBUTE = "tile_index";
	private static final String TILESET_COLUMNS_ATTRIBUTE = "tileset_columns";

	@Override
	protected void buildRepresentation(T entity) {
		final SpriteStore store = SpriteStore.get();
		final ZoneInfo info = ZoneInfo.get();
		final RPObject object = entity.getRPObject();

		if (object.has(TILESET_ATTRIBUTE)
				&& object.has(TILE_INDEX_ATTRIBUTE)
				&& object.has(TILESET_COLUMNS_ATTRIBUTE)) {
			buildTilesetRepresentation(store, info, object);
			return;
		}

		Sprite sprite;
		if (entity.getName() == null) {
			setSprite(new EmptySprite(1, 1, null));
			return;
		} else {
			sprite = store.getModifiedSprite(translate(getClassResourcePath() + "/" + entity.getName()),
					info.getZoneColor(), info.getColorMethod());
		}

		/*
		 * Entities are [currently] always 1x1. Extra columns are animation.
		 * Extra rows are ignored.
		 */
		final int imageWidth = sprite.getWidth();
		final int width = Math.max((int) entity.getWidth(), 1);
		final int height = Math.max((int) entity.getHeight(), 1);
		int frames = imageWidth / IGameScreen.SIZE_UNIT_PIXELS / width;

		// Just use the normal sprite if there are no animation frames
		int state = entity.getState();
		if (frames > 1) {
			sprite = store.getAnimatedSprite(sprite,
					0, state * IGameScreen.SIZE_UNIT_PIXELS * height,
					imageWidth / IGameScreen.SIZE_UNIT_PIXELS / width,
					IGameScreen.SIZE_UNIT_PIXELS * width,
					IGameScreen.SIZE_UNIT_PIXELS * height,
					100);
		} else {
			sprite = store.getTile(sprite,
					0, state * IGameScreen.SIZE_UNIT_PIXELS * height,
					IGameScreen.SIZE_UNIT_PIXELS * width,
					IGameScreen.SIZE_UNIT_PIXELS * height);
		}

		setSprite(sprite);
	}

	private void buildTilesetRepresentation(final SpriteStore store, final ZoneInfo info,
			final RPObject object) {
		final String tileset = object.get(TILESET_ATTRIBUTE);
		final int tileIndex = object.getInt(TILE_INDEX_ATTRIBUTE);
		final int columns = object.getInt(TILESET_COLUMNS_ATTRIBUTE);
		if (tileIndex < 0 || columns < 1) {
			setSprite(new EmptySprite(1, 1, null));
			return;
		}

		final Sprite sheet = store.getModifiedSprite(
				"data/maps/tileset/" + tileset + ".png",
				info.getZoneColor(), info.getColorMethod());
		final int tileSize = IGameScreen.SIZE_UNIT_PIXELS;
		final int sourceX = tileIndex % columns * tileSize;
		final int sourceY = tileIndex / columns * tileSize;

		if (sourceX + tileSize > sheet.getWidth() || sourceY + tileSize > sheet.getHeight()) {
			setSprite(new EmptySprite(1, 1, null));
			return;
		}

		setSprite(store.getTile(sheet, sourceX, sourceY, tileSize, tileSize));
	}
}
