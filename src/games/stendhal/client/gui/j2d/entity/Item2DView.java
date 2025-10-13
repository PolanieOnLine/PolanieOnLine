/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import games.stendhal.client.IGameScreen;
import games.stendhal.client.ZoneInfo;
import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.Inspector;
import games.stendhal.client.entity.Item;
import games.stendhal.client.gui.InternalWindow;
import games.stendhal.client.gui.InternalWindow.CloseListener;
import games.stendhal.client.gui.SlotWindow;
import games.stendhal.client.gui.j2d.entity.helpers.DrawingHelper;
import games.stendhal.client.gui.j2d.entity.helpers.HorizontalAlignment;
import games.stendhal.client.gui.j2d.entity.helpers.VerticalAlignment;
import games.stendhal.client.gui.styled.cursor.StendhalCursor;
import games.stendhal.client.sprite.AnimatedSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import games.stendhal.client.sprite.TextSprite;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.MathHelper;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * The 2D view of an item.
 *
 * @param <T> item type
 */
public class Item2DView<T extends Item> extends Entity2DView<T> {
	/**
	 * Log4J.
	 */
	private static final Logger logger = Logger.getLogger(Item2DView.class);
	/** Default size of the container slot. */
	private static final int DEFAULT_SLOT_SIZE = 8;

	/** Window for showing the slot contents, if any. */
	private volatile SlotWindow slotWindow;
	/** Width of the slot window. */
	private int slotWindowWidth;
	/** height of the slot window. */
	private int slotWindowHeight;

	/** The quantity value changed. */
	private volatile boolean quantityChanged;
	/** The image of the quantity. */
	private Sprite quantitySprite;
	/** Whether to show the quantity. */
	private boolean showQuantity;


	@Override
	public void initialize(final T entity) {
		super.initialize(entity);
		quantitySprite = getQuantitySprite(entity);
		quantityChanged = false;
		showQuantity = true;
	}

	//
	// Entity2DView
	//

	/**
	 * Build the visual representation of this entity.
	 */
	@Override
	protected void buildRepresentation(T entity) {
		final SpriteStore store = SpriteStore.get();
		Sprite sprite;
		// Colour items on the ground, but not in bags, corpses etc.
		if (!isContained()) {
			ZoneInfo info = ZoneInfo.get();
			sprite = store.getModifiedSprite(translate(getClassResourcePath()),
					info.getZoneColor(), info.getColorMethod());
		} else {
			sprite = store.getSprite(translate(getClassResourcePath()));
		}

		/*
		 * Items are always 1x1 (they need to fit in entity slots). Extra
		 * columns are animation, extra rows are different states.
		 */
		final int width = sprite.getWidth();
		final int yOffset = 32 * entity.getState();
		store.getTile(sprite, 0, yOffset, IGameScreen.SIZE_UNIT_PIXELS,
				IGameScreen.SIZE_UNIT_PIXELS);

		if (width > IGameScreen.SIZE_UNIT_PIXELS) {
			sprite = new AnimatedSprite(store.getTiles(sprite, 0, yOffset, 
					width / IGameScreen.SIZE_UNIT_PIXELS, 
					IGameScreen.SIZE_UNIT_PIXELS, IGameScreen.SIZE_UNIT_PIXELS),
					100, true);
		} else if (sprite.getHeight() > IGameScreen.SIZE_UNIT_PIXELS) {
			sprite = store.getTile(sprite, 0, yOffset, IGameScreen.SIZE_UNIT_PIXELS,
					IGameScreen.SIZE_UNIT_PIXELS);
		}

		setSprite(sprite);
	}

	/**
	 * Determines on top of which other entities this entity should be drawn.
	 * Entities with a high Z index will be drawn on top of ones with a lower Z
	 * index.
	 *
	 * Also, players can only interact with the topmost entity.
	 *
	 * @return The drawing index.
	 */
	@Override
	public int getZIndex() {
		return 7000;
	}

	/**
	 * Translate a resource name into it's sprite image path.
	 *
	 * @param name
	 *            The resource name.
	 *
	 * @return The full resource name.
	 */
	@Override
	protected String translate(final String name) {
		return "data/sprites/items/" + name + ".png";
	}

	@Override
	protected void draw(final Graphics2D g2d, final int x, final int y,
			final int width, final int height) {
		super.draw(g2d, x, y, width, height);

			drawRarityBadge(g2d, x, y, width, height);

		if (showQuantity && (quantitySprite != null)) {
			drawQuantity(g2d, x, y, width, height);
		}
	}

	private void drawRarityBadge(final Graphics2D g2d, final int x, final int y,
				final int width, final int height) {
		if (entity == null) {
			return;
		}

		ItemRarity rarity = entity.getRarity();
		if (rarity == null) {
			return;
		}

		Color color = rarity.getColor();
		if (color == null) {
			return;
		}

		Graphics2D badgeGraphics = (Graphics2D) g2d.create();
		int baseSize = Math.min(width, height);
		int size = Math.max(10, baseSize / 3);
		int padding = Math.max(2, baseSize / 12);
		int centerX = x + padding + (size / 2);
		int centerY = y + padding + (size / 2);
		int half = size / 2;
		int[] xPoints = new int[] { centerX, centerX + half, centerX, centerX - half };
		int[] yPoints = new int[] { centerY - half, centerY, centerY + half, centerY };

		Color fill = new Color(color.getRed(), color.getGreen(), color.getBlue(), 200);
		badgeGraphics.setColor(fill);
		badgeGraphics.fillPolygon(xPoints, yPoints, 4);
		badgeGraphics.setColor(new Color(0, 0, 0, 160));
		badgeGraphics.drawPolygon(xPoints, yPoints, 4);

		String displayName = rarity.getDisplayName();
		if ((displayName != null) && !displayName.isEmpty()) {
			String initial = displayName.substring(0, 1).toUpperCase();
			float fontSize = Math.max(8f, size / 2.5f);
			badgeGraphics.setFont(badgeGraphics.getFont().deriveFont(Font.BOLD, fontSize));
			FontMetrics metrics = badgeGraphics.getFontMetrics();
			int textX = centerX - (metrics.stringWidth(initial) / 2);
			int textY = centerY + ((metrics.getAscent() - metrics.getDescent()) / 2);
			badgeGraphics.setColor(Color.WHITE);
			badgeGraphics.drawString(initial, textX, textY);
		}

		badgeGraphics.dispose();
	}

	/**
	 * Draw quantity sprite. Exact position depends on containment status.
	 *
	 * @param g2d graphics
	 * @param x x coordinate
	 * @param y y coordinate
	 * @param width width of the drawing area
	 * @param height height of the drawing area
	 */
	private void drawQuantity(final Graphics2D g2d, final int x, final int y,
			final int width, int height) {
		if (isContained()) {
			// Right alignment
			DrawingHelper.drawAlignedSprite(g2d, quantitySprite, HorizontalAlignment.RIGHT, VerticalAlignment.TOP, x, y, width, height);
		} else {
			// Center alignment
			DrawingHelper.drawAlignedSprite(g2d, quantitySprite, HorizontalAlignment.CENTER, VerticalAlignment.TOP, x, y, width, height);
		}
	}

	/**
	 * Set whether this view is contained, and should render in a compressed
	 * (it's defined) area without clipping anything important.
	 *
	 * @param contained
	 *            <code>true</code> if contained.
	 */
	@Override
	public void setContained(final boolean contained) {
		super.setContained(contained);

		quantityChanged = true;
		markChanged();
	}

	/**
	 * Update representation.
	 */
	@Override
	protected void update() {
		super.update();

		T entity  = this.entity;
		if (quantityChanged && (entity != null)) {
			quantityChanged = false;
			quantitySprite = getQuantitySprite(entity);
		}
	}

	//
	// EntityChangeListener
	//

	@Override
	void entityChanged(final Object property) {
		super.entityChanged(property);

		if (property == IEntity.PROP_CLASS || property == IEntity.PROP_STATE) {
			representationChanged = true;
		} else if (property == Item.PROP_QUANTITY) {
			quantityChanged = true;
		}
	}

	//
	// EntityView
	//

	/**
	 * Determine if this entity can be moved (e.g. via dragging).
	 *
	 * @return <code>true</code> if the entity is movable.
	 */
	@Override
	public boolean isMovable() {
		return true;
	}

	/**
	 * Perform the default action.
	 */
	@Override
	public void onAction() {
		onAction(ActionType.USE);
	}

	@Override
	public boolean onHarmlessAction() {
		return false;
	}

	/**
	 * Set the content inspector for this entity.
	 *
	 * @param inspector
	 *            The inspector.
	 */
	@Override
	public void setInspector(final Inspector inspector) {
		if ((getContent() != null) && (inspector != null)) {
			// Autoinspect containers. They have client visible slots only when
			// carried by the user.
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					inspect(inspector);
				}
			});
		}
	}

	/**
	 * Perform an action.
	 *
	 * @param at
	 *            The action.
	 */
	@Override
	public void onAction(final ActionType at) {
		switch (at) {
		case USE:
			/*
			 * Send use action even for released items, if they are in a slot.
			 * Those get released when the slot contents change, and a new view
			 * is created. Users expect to be able to use multiple double clicks
			 * or using a menu created previously for the item.
			 */
			if (!isReleased() || !entity.isOnGround()) {
				at.send(at.fillTargetInfo(entity));
			}
			break;

		default:
			super.onAction(at);
			break;
		}
	}


	/**
	 * Inspect the item. Show the slot contents.
	 *
	 * @param inspector inspector
	 */
	private void inspect(Inspector inspector) {
		RPSlot slot = getContent();
		if (slotWindowWidth == 0) {
			int capacity = getSlotCapacity(slot);
			calculateWindowProportions(capacity);
		}

		boolean addListener = slotWindow == null;
		SlotWindow window = inspector.inspectMe(entity, slot,
				slotWindow, slotWindowWidth, slotWindowHeight);
		slotWindow = window;
		if (window != null) {
			// Don't let the user remove the container windows to keep the UI as
			// clean as possible.
			window.setCloseable(false);
			/*
			 * Register a listener for window closing so that we can
			 * drop the reference to the closed window and let the
			 * garbage collector claim it.
			 */
			if (addListener) {
				window.addCloseListener(new CloseListener() {
					@Override
					public void windowClosed(InternalWindow window) {
						slotWindow = null;
					}
				});
			}
			/*
			 * In case the view got released while the window was created and
			 * added, and before the main thread was aware that there's a window
			 * to be closed, close it now. (onAction is called from the event
			 * dispatch thread).
			 */
			if (isReleased()) {
				window.close();
			}
		}
	}

	/**
	 * Get the actual size of the container slot of a container item.
	 *
	 * @param slot container slot
	 * @return size of the container slot
	 */
	private int getSlotCapacity(RPSlot slot) {
		RPObject obj = entity.getRPObject();
		if (obj.has("slot_size")) {
			return MathHelper.parseIntDefault(obj.get("slot_size"), DEFAULT_SLOT_SIZE);
		}
		// Fall back to default slot size (should not happen)
		logger.warn("Container is missing slot size: " + obj);
		return slot.getCapacity();
	}

	/**
	 * Get the content slot.
	 *
	 * @return Content slot or <code>null</code> if the item has none or it's
	 * not accessible.
	 */
	private RPSlot getContent() {
		return entity.getContent();
	}

	/**
	 * Find out dimensions for a somewhat square slot window.
	 *
	 * @param slots number of slots in the window
	 */
	private void calculateWindowProportions(final int slots) {
		int width = (int) Math.sqrt(slots);

		while (slots % width != 0) {
			width--;
			if (width <= 0) {
				logger.error("Failed to decide dimensions for slot window. slots = " + slots);

				width = 1;
			}
		}
		slotWindowWidth = width;
		slotWindowHeight = slots / width;
	}

	@Override
	public void release() {
		final SlotWindow window = slotWindow;
		if (window != null) {
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					window.close();
				}
			});
		}

		super.release();
	}

	@Override
	public StendhalCursor getCursor() {
		return StendhalCursor.NORMAL;
	}

	//
	// stackable item
	//

	/**
	 * Get the appropriate quantity sprite.
	 *
	 * @param entity
	 * @return A sprite representing the quantity, or <code>null</code> for
	 *         none.
	 */
	private Sprite getQuantitySprite(T entity) {
		int quantity;
		String label;

		quantity = entity.getQuantity();

		if (quantity <= 1) {
			return null;
		} else if (isContained() && (quantity > 9999999)) {
			label = (quantity / 1000000) + "M";
		} else if (isContained() && (quantity > 9999)) {
			label = (quantity / 1000) + "K";
		} else {
			label = Integer.toString(quantity);
		}

		return TextSprite.createTextSprite(label, Color.WHITE);
	}

	/**
	 * Set whether to show the quantity value.
	 *
	 * @param showQuantity
	 *            Whether to show the quantity.
	 */
	public void setShowQuantity(final boolean showQuantity) {
		this.showQuantity = showQuantity;
	}
}
