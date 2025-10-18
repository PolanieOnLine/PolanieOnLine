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
	private static final int FRAME_TOP_ROW = 1;
	private static final int FRAME_BOTTOM_ROW = 2;
	private static final int FRAME_COLUMN_SPAN = 2;
	private static final int IDLE_FRAME = 0;
	private static final int ACTIVE_FRAME = 1;
	private static final String SLOT_CONTENT = "content";

	private Sprite idleSprite;
	private Sprite activeSprite;
	private GoldenCauldronWindow window;
	private Inspector inspector;
	private boolean openChanged;
	private boolean statusChanged;
	private boolean brewerChanged;
	private boolean requestOpen;

	@Override
	public void initialize(final GoldenCauldron entity) {
		super.initialize(entity);
		representationChanged = true;
		openChanged = false;
		statusChanged = false;
		brewerChanged = false;
		requestOpen = false;
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
			newWindow.setStatusText(entity.getStatusText());
			newWindow.setMixEnabled(entity.isControlledByUser());
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
			window.setStatusText(entity.getStatusText());
		}
	}

	private void updateMixAvailability() {
		if (window != null) {
			window.setMixEnabled(entity.isControlledByUser());
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

		idleSprite = composeFrame(store, sheet, IDLE_FRAME);
		activeSprite = composeFrame(store, sheet, ACTIVE_FRAME);
	}

	private Sprite composeFrame(final SpriteStore store, final Sprite sheet, final int frame) {
		final BufferedImage image =
			new BufferedImage(FRAME_WIDTH, FRAME_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D g = image.createGraphics();

		try {
			final int column = frame * FRAME_COLUMN_SPAN;
			drawRow(store, sheet, g, column, FRAME_TOP_ROW, 0);
			drawRow(store, sheet, g, column, FRAME_BOTTOM_ROW, TILE);
		} finally {
			g.dispose();
		}

		return new ImageSprite(image);
	}

	private void drawRow(final SpriteStore store, final Sprite sheet, final Graphics2D g,
			final int column, final int row, final int y) {
		final int x = column * TILE;
		final Sprite left = store.getTile(sheet, x, row * TILE, TILE, TILE);
		final Sprite right =
			store.getTile(sheet, (column + 1) * TILE, row * TILE, TILE, TILE);

		if (left != null) {
			left.draw(g, 0, y);
		}
		if (right != null) {
			right.draw(g, TILE, y);
		}
	}
}
