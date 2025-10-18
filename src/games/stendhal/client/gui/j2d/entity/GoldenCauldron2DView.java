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

import games.stendhal.client.IGameScreen;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.GoldenCauldron;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.gui.GoldenCauldronWindow;
import games.stendhal.client.gui.InternalWindow;
import games.stendhal.client.gui.InternalWindow.CloseListener;
import games.stendhal.client.gui.j2DClient;
import games.stendhal.client.gui.j2d.entity.helpers.HorizontalAlignment;
import games.stendhal.client.gui.j2d.entity.helpers.VerticalAlignment;
import games.stendhal.client.sprite.AnimatedSprite;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.common.constants.Actions;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.SwingUtilities;
import marauroa.common.game.RPAction;

/**
 * 2D view for the golden cauldron entity.
 */
public class GoldenCauldron2DView extends Entity2DView<GoldenCauldron> {
	private static final String SPRITE_SHEET = "data/maps/tileset/item/pot/cauldron.png";
	private static final int TILE = IGameScreen.SIZE_UNIT_PIXELS;
	private static final int FRAME_WIDTH = TILE * 2;
	private static final int FRAME_HEIGHT = TILE * 2;
	private static final int SHEET_COLUMNS = 8;
	private static final int ACTIVE_FRAME_DELAY = 250;
	private static final int[] IDLE_FRAME_TILES = {0, 1, 2, 3};
	private static final int[] ACTIVE_TOP_LEFT_SEQUENCE = {32, 34, 36, 38, 36, 34};
	private static final int[] ACTIVE_TOP_RIGHT_SEQUENCE = {33, 35, 37, 39, 37, 35};
	private static final int[] ACTIVE_BOTTOM_LEFT_SEQUENCE = {8, 10, 12, 14, 12, 10};
	private static final int[] ACTIVE_BOTTOM_RIGHT_SEQUENCE = {9, 11, 13, 15, 13, 11};
	private static final int[] ACTIVE_STIR_SEQUENCE = {5, 6};
	private static final String SLOT_CONTENT = "content";

	private Sprite idleSprite;
	private AnimatedSprite activeAnimation;
	private GoldenCauldronWindow window;
	private Inspector inspector;
	private boolean openChanged;
	private boolean statusChanged;
	private boolean brewerChanged;
	private boolean readyAtChanged;
	private boolean requestOpen;

	@Override
	public void initialize(final GoldenCauldron entity) {
		setSpriteAlignment(HorizontalAlignment.LEFT, VerticalAlignment.BOTTOM);
		super.initialize(entity);
		representationChanged = true;
		openChanged = false;
		statusChanged = false;
		brewerChanged = false;
		readyAtChanged = false;
		requestOpen = false;
	}

	@Override
	protected void buildRepresentation(final GoldenCauldron entity) {
		ensureFrames();

		if (entity.isActive()) {
			if (activeAnimation != null) {
				activeAnimation.reset(0);
				setSprite(activeAnimation);
			} else {
				setSprite(idleSprite);
			}
		} else {
			setSprite(idleSprite);
		}
	}

	@Override
	protected void buildActions(final List<String> list) {
		final GoldenCauldron cauldron = entity;
		if ((cauldron != null) && cauldron.isOpen()) {
			list.add(ActionType.INSPECT.getRepresentation());
			list.add(ActionType.CLOSE.getRepresentation());
		} else {
			list.add(ActionType.OPEN.getRepresentation());
		}
	}

	@Override
	void entityChanged(final Object property) {
		super.entityChanged(property);

		if (property == GoldenCauldron.PROP_STATE || property == IEntity.PROP_CLASS) {
			representationChanged = true;
		}
		if (property == GoldenCauldron.PROP_OPEN) {
			openChanged = true;
		}
		if (property == GoldenCauldron.PROP_STATUS) {
			statusChanged = true;
		}
		if (property == GoldenCauldron.PROP_BREWER) {
			brewerChanged = true;
		}
		if (property == GoldenCauldron.PROP_READY_AT) {
			readyAtChanged = true;
		}
	}

	@Override
	protected void update() {
		super.update();

		if (openChanged) {
			openChanged = false;
			if (entity.isOpen()) {
				maybeShowWindow();
			} else {
				destroyWindow();
			}
			requestOpen = false;
		}

		if (statusChanged) {
			statusChanged = false;
			updateStatus();
		}

		if (brewerChanged) {
			brewerChanged = false;
			updateMixAvailability();
		}

		if (readyAtChanged) {
			readyAtChanged = false;
			updateStatus();
			updateMixAvailability();
		}
	}

	@Override
	public void onAction(final ActionType at) {
		if (isReleased()) {
			return;
		}

		switch (at) {
			case OPEN:
			requestOpen = true;
			at.send(at.fillTargetInfo(entity));
			break;
			case CLOSE:
			requestOpen = false;
			at.send(at.fillTargetInfo(entity));
			break;
			case INSPECT:
			requestOpen = true;
			maybeShowWindow();
			break;
			default:
			super.onAction(at);
			break;
		}
	}

	@Override
	public void onAction() {
		if (entity.isOpen()) {
			onAction(ActionType.INSPECT);
		} else {
			onAction(ActionType.OPEN);
		}
	}

	@Override
	public void release() {
		destroyWindow();
		super.release();
	}

	@Override
	public void setInspector(final Inspector inspector) {
		this.inspector = inspector;
		if (window != null) {
			window.setInspector(inspector);
		}
	}

	private void maybeShowWindow() {
		if (!entity.isOpen()) {
			return;
		}

		if (window == null) {
			if (!requestOpen && !entity.isControlledByUser()) {
				return;
			}
			final GoldenCauldronWindow newWindow =
			new GoldenCauldronWindow(entity.getName());
			newWindow.setSlot(entity, SLOT_CONTENT);
			newWindow.setInspector(inspector);
			newWindow.updateStatus(entity.getStatusText(), entity.getReadyAt());
			newWindow.setMixEnabled(entity.isControlledByUser() && !entity.isActive());
			newWindow.setMixAction(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent event) {
					if (entity.isControlledByUser()) {
						sendMixAction();
					}
				}
			});
			newWindow.addCloseListener(new CloseListener() {
				@Override
				public void windowClosed(final InternalWindow closed) {
					window = null;
					if (entity.isOpen() && entity.isControlledByUser()) {
						final RPAction closeAction =
						ActionType.CLOSE.fillTargetInfo(entity);
						ActionType.CLOSE.send(closeAction);
					}
				}
			});
			j2DClient.get().addWindow(newWindow);
			newWindow.setVisible(true);
			window = newWindow;
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.raise();
				}
			});
		}

		requestOpen = false;
		updateStatus();
		updateMixAvailability();
	}

	private void destroyWindow() {
		final GoldenCauldronWindow current = window;
		if (current != null) {
			window = null;
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					current.close();
				}
			});
		}
	}

	private void updateStatus() {
		if (window != null) {
			window.updateStatus(entity.getStatusText(), entity.getReadyAt());
		}
	}

	private void updateMixAvailability() {
		if (window != null) {
			final boolean canMix = entity.isControlledByUser() && !entity.isActive();
			window.setMixEnabled(canMix);
		}
	}

	private void sendMixAction() {
		final RPAction action = new RPAction();
		action.put(Actions.TYPE, "goldencauldron");
		action.put("command", "mix");
		action.put(Actions.TARGET_PATH, entity.getPath());
		StendhalClient.get().send(action);
	}

	private void ensureFrames() {
		if ((idleSprite != null) && (activeAnimation != null)) {
			return;
		}

		final SpriteStore store = SpriteStore.get();
		final Sprite sheet = store.getSprite(SPRITE_SHEET);

		if (sheet == null) {
			idleSprite = store.getFailsafe();
			activeAnimation = null;
			return;
		}

		idleSprite = composeFrame(store, sheet, IDLE_FRAME_TILES, -1);

		final int topFrameCount = ACTIVE_TOP_LEFT_SEQUENCE.length;
		final int bottomFrameCount = ACTIVE_BOTTOM_LEFT_SEQUENCE.length;
		final Sprite[] frames = new Sprite[topFrameCount];

		for (int i = 0; i < topFrameCount; i++) {
			final int[] indices = {
				ACTIVE_TOP_LEFT_SEQUENCE[i],
				ACTIVE_TOP_RIGHT_SEQUENCE[i],
				ACTIVE_BOTTOM_LEFT_SEQUENCE[i % bottomFrameCount],
				ACTIVE_BOTTOM_RIGHT_SEQUENCE[i % bottomFrameCount]
			};
			final int stirIndex = ACTIVE_STIR_SEQUENCE[i % ACTIVE_STIR_SEQUENCE.length];
			frames[i] = composeFrame(store, sheet, indices, stirIndex);
		}

		activeAnimation = new AnimatedSprite(frames, ACTIVE_FRAME_DELAY, true);
	}

	private Sprite composeFrame(final SpriteStore store, final Sprite sheet, final int[] indices,
	final int stirIndex) {
		final BufferedImage image =
		new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();

		try {
			drawTile(store, sheet, g, indices[0], 0, 0);
			drawTile(store, sheet, g, indices[1], TILE, 0);
			drawTile(store, sheet, g, indices[2], 0, TILE);
			drawTile(store, sheet, g, indices[3], TILE, TILE);
			if (stirIndex >= 0) {
				final int stirX = (stirIndex == 5) ? TILE : 0;
				drawTile(store, sheet, g, stirIndex, stirX, 0);
			}
		} finally {
			g.dispose();
		}

		return new ImageSprite(image);
	}

	private void drawTile(final SpriteStore store, final Sprite sheet, final Graphics2D g,
	final int index, final int drawX, final int drawY) {
		final int column = index % SHEET_COLUMNS;
		final int row = index / SHEET_COLUMNS;
		final Sprite tile = store.getTile(sheet, column * TILE, row * TILE, TILE, TILE);

		if (tile != null) {
			tile.draw(g, drawX, drawY);
		}
	}
}
