/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2024 - PolanieOnLine                    *
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
import java.util.List;

import javax.swing.SwingUtilities;

import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.GoldenCauldron;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.User;
import games.stendhal.client.gui.GoldenCauldronWindow;
import games.stendhal.client.gui.InternalWindow;
import games.stendhal.client.gui.InternalWindow.CloseListener;
import games.stendhal.client.gui.SlotWindow;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;

class GoldenCauldron2DView extends UseableEntity2DView<GoldenCauldron> {
	private static final String SPRITE_REF = "data/maps/tileset/item/pot/cauldron.png";
	private static final int TILE_SIZE = 32;
	private static final int FRAME_SIZE = TILE_SIZE * 2;
	private static final Sprite[] STATE_SPRITES = new Sprite[2];

	private GoldenCauldronWindow window;
	private Inspector inspector;
	private boolean openChanged;

	GoldenCauldron2DView() {
		openChanged = false;
	}

	@Override
	protected void buildRepresentation(final GoldenCauldron cauldron) {
		final SpriteStore store = SpriteStore.get();
		final Sprite sheet = store.getSprite(SPRITE_REF);
		if (sheet == null) {
			setSprite(store.getFailsafe());
			return;
		}
		if (STATE_SPRITES[0] == null) {
			STATE_SPRITES[0] = composeState(sheet, 0, 0, SPRITE_REF + "#idle");
			STATE_SPRITES[1] = composeState(sheet, 0, 2, SPRITE_REF + "#active");
		}

		int state = 0;
		if (cauldron != null) {
			state = cauldron.getState();
		}
		if (state < 0 || state >= STATE_SPRITES.length) {
			state = 0;
		}
		setSprite(STATE_SPRITES[state]);
	}

	private static Sprite composeState(final Sprite sheet, final int columnOffset, final int rowOffset,
				final String ref) {
		final BufferedImage frame = new BufferedImage(FRAME_SIZE, FRAME_SIZE, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g2 = frame.createGraphics();
		try {
			drawTile(sheet, columnOffset, rowOffset, g2, 0, 0);
			drawTile(sheet, columnOffset + 1, rowOffset, g2, TILE_SIZE, 0);
			drawTile(sheet, columnOffset, rowOffset + 1, g2, 0, TILE_SIZE);
			drawTile(sheet, columnOffset + 1, rowOffset + 1, g2, TILE_SIZE, TILE_SIZE);
		} finally {
			g2.dispose();
		}

		return new ImageSprite(frame, ref);
	}

	private static void drawTile(final Sprite sheet, final int tileX, final int tileY, final Graphics2D g2,
				final int destX, final int destY) {
		final int sourceX = tileX * TILE_SIZE;
		final int sourceY = tileY * TILE_SIZE;
		final Sprite tile = sheet.createRegion(sourceX, sourceY, TILE_SIZE, TILE_SIZE,
					SPRITE_REF + '[' + tileX + ',' + tileY + ']');
		tile.draw(g2, destX, destY);
	}

	@Override
	protected void buildActions(final List<String> list) {
		GoldenCauldron cauldron = entity;
		if (cauldron != null && cauldron.isOpen()) {
			list.add(ActionType.INSPECT.getRepresentation());
			list.add(ActionType.CLOSE.getRepresentation());
		} else {
			list.add(ActionType.OPEN.getRepresentation());
		}
		list.add(ActionType.LOOK.getRepresentation());
	}

	@Override
	public void onAction() {
		GoldenCauldron cauldron = entity;
		if (cauldron != null && cauldron.isOpen()) {
			onAction(ActionType.INSPECT);
		} else {
			onAction(ActionType.OPEN);
		}
	}

	@Override
	public void onAction(ActionType at) {
		if (at == null) {
			at = ActionType.OPEN;
		}
		if (isReleased()) {
			return;
		}

		switch (at) {
		case INSPECT:
			showWindow();
			return;
		case OPEN:
			sendUseAction();
			return;
		case CLOSE:
			if (window != null) {
				window.close();
			}
			sendUseAction();
			return;
		default:
			super.onAction(at);
			return;
		}
	}

	@Override
	protected void update() {
		super.update();

		if (openChanged) {
			openChanged = false;
			final GoldenCauldron cauldron = entity;
			if (cauldron != null && cauldron.isOpen()) {
				if (cauldron.isBrewer(User.getCharacterName())) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							showWindow();
						}
					});
				}
			} else if (window != null) {
				final GoldenCauldronWindow current = window;
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						current.closeFromServer();
					}
				});
			}
		}
	}

	@Override
	void entityChanged(final Object property) {
		super.entityChanged(property);

		if (property == GoldenCauldron.PROP_OPEN) {
			openChanged = true;
		} else if (property == GoldenCauldron.PROP_BREWER && window != null) {
			window.updateBrewer();
		}
	}

	@Override
	public void setInspector(final Inspector inspector) {
		this.inspector = inspector;
		super.setInspector(inspector);
		if (window != null) {
			window.setInspector(inspector);
		}
	}

	@Override
	public void release() {
		final GoldenCauldronWindow current = window;
		if (current != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					current.closeFromServer();
				}
			});
			window = null;
		}
		super.release();
	}

	private void showWindow() {
		final GoldenCauldron cauldron = entity;
		if (cauldron == null || !cauldron.isOpen()) {
			return;
		}
		if (!cauldron.isBrewer(User.getCharacterName())) {
			return;
		}
		if (inspector == null) {
			return;
		}

		final GoldenCauldronWindow current = window;
			final SlotWindow created = inspector.inspectMe(cauldron, cauldron.getMixSlot(), current, 4, 2);
		if (created instanceof GoldenCauldronWindow) {
			window = (GoldenCauldronWindow) created;
			window.setCauldron(cauldron);
			if (created != current) {
				window.addCloseListener(new CloseListener() {
					@Override
					public void windowClosed(final InternalWindow closed) {
						if (closed == window) {
							window = null;
						}
					}
				});
			}
		}
	}

	private void sendUseAction() {
		ActionType.USE.send(ActionType.USE.fillTargetInfo(entity));
	}
}
