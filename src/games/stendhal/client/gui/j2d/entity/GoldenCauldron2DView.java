/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import games.stendhal.client.IGameScreen;
import games.stendhal.client.entity.GoldenCauldron;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

/**
 * 2D view for the golden cauldron entity.
 */
public class GoldenCauldron2DView extends Entity2DView<GoldenCauldron> {
	private static final String SPRITE_SHEET = "data/maps/tileset/item/pot/cauldron.png";
	private static final int TILE = IGameScreen.SIZE_UNIT_PIXELS;

	private Sprite idleSprite;
	private Sprite activeSprite;

	@Override
	public void initialize(final GoldenCauldron entity) {
		super.initialize(entity);
		representationChanged = true;
	}

	@Override
	protected void buildRepresentation(final GoldenCauldron entity) {
		ensureFrames();

		if (entity.isActive()) {
			setSprite(activeSprite);
		} else {
			setSprite(idleSprite);
		}
	}

	@Override
	void entityChanged(final Object property) {
		super.entityChanged(property);

		if (property == GoldenCauldron.PROP_STATE || property == IEntity.PROP_CLASS) {
			representationChanged = true;
		}
	}

	private void ensureFrames() {
		if ((idleSprite != null) && (activeSprite != null)) {
			return;
		}

		final SpriteStore store = SpriteStore.get();
		final Sprite sheet = store.getSprite(SPRITE_SHEET);

		if (sheet == null) {
			idleSprite = store.getFailsafe();
			activeSprite = idleSprite;
			return;
		}

		idleSprite = composeFrame(store, sheet, 0);
		activeSprite = composeFrame(store, sheet, 1);
	}

	private Sprite composeFrame(final SpriteStore store, final Sprite sheet, final int column) {
		final BufferedImage image = new BufferedImage(TILE, TILE * 2, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();

		try {
			final int x = column * TILE;
			final Sprite top = store.getTile(sheet, x, 0, TILE, TILE);
			final Sprite bottom = store.getTile(sheet, x, TILE, TILE, TILE);

			if (bottom != null) {
				bottom.draw(g, 0, TILE);
			}
			if (top != null) {
				top.draw(g, 0, 0);
			}
		} finally {
			g.dispose();
		}

		return new ImageSprite(image);
	}
}
