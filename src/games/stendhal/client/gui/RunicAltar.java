/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JComponent;

import org.apache.log4j.Logger;

import games.stendhal.client.GameObjects;
import games.stendhal.client.entity.ContentChangeListener;
import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityMap;
import games.stendhal.client.gui.layout.SBoxLayout;
import games.stendhal.client.sprite.ImageSprite;
import games.stendhal.client.sprite.Sprite;
import games.stendhal.client.sprite.SpriteStore;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPObject.ID;
import marauroa.common.game.RPSlot;

class RunicAltar implements ContentChangeListener {
	private static final Logger logger = Logger.getLogger(RunicAltar.class);

	private InternalManagedWindow runicAltarDialog;

	private final Map<String, ItemPanel> slotPanels = new HashMap<String, ItemPanel>();
	private User player;

	private static final int PADDING = 1;
	private static final int SLOT_TINT_ALPHA = 56;
	private static final int SLOT_ARC_DIAMETER = 8;
	private static final Color CONTROL_SLOT_COLOR = new Color(55, 155, 185, SLOT_TINT_ALPHA);
	private static final Color UTILITY_SLOT_COLOR = new Color(126, 82, 180, SLOT_TINT_ALPHA);
	private static final Color OFFENSIVE_SLOT_COLOR = new Color(190, 58, 48, SLOT_TINT_ALPHA);
	private static final Color SPECIAL_SLOT_COLOR = new Color(190, 76, 158, SLOT_TINT_ALPHA);
	private static final Color DEFENSIVE_SLOT_COLOR = new Color(60, 105, 180, SLOT_TINT_ALPHA);
	private static final Color HEALING_SLOT_COLOR = new Color(58, 150, 82, SLOT_TINT_ALPHA);
	private static final Color RESISTANCE_SLOT_COLOR = new Color(195, 150, 48, SLOT_TINT_ALPHA);

	/**
	 * Constructor for the RunicAltar class.
	 * It sets the window to have 7 slots in total, distributed in the shape of the Star of David.
	 * The constructor also configures the window to be closeable by the user.
	 */
	public RunicAltar() {
		runicAltarDialog = createLayout();
		runicAltarDialog.setVisible(false);
	}

	public Component getRunicAltar() {
		return runicAltarDialog;
	}

	public void getVisibleRunicAltar() {
		runicAltarDialog.setVisible(true);
	}

	public void setPlayer(final User userEntity) {
		player = userEntity;
		userEntity.addContentChangeListener(this);
		refreshContents();
	}

	private InternalManagedWindow createLayout() {
		JComponent content = SBoxLayout.createContainer(SBoxLayout.VERTICAL, PADDING);
		JComponent row = SBoxLayout.createContainer(SBoxLayout.HORIZONTAL, 20);
		JComponent left = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 15);
		JComponent middle = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 20);
		JComponent right = SBoxLayout.createContainer(SBoxLayout.VERTICAL, 15);

		left.setAlignmentY(Component.CENTER_ALIGNMENT);
		middle.setAlignmentY(Component.CENTER_ALIGNMENT);
		right.setAlignmentY(Component.CENTER_ALIGNMENT);

		row.add(left);
		row.add(middle);
		row.add(right);
		content.add(row);

		Class<? extends IEntity> itemClass = EntityMap.getClass("item", null, null);
		SpriteStore store = SpriteStore.get();

		left.add(createItemPanel(itemClass, store, "control_rune", "data/gui/rune-control.png",
				"Slot glifu kontroli", CONTROL_SLOT_COLOR));
		left.add(createItemPanel(itemClass, store, "utility_rune", "data/gui/rune-utility.png",
				"Slot glifu użytkowego", UTILITY_SLOT_COLOR));

		middle.add(createItemPanel(itemClass, store, "offensive_rune", "data/gui/rune-offensive.png",
				"Slot glifu ofensywnego", OFFENSIVE_SLOT_COLOR));
		middle.add(createItemPanel(itemClass, store, "special_rune", "data/gui/rune-special.png",
				"Slot glifu specjalnego", SPECIAL_SLOT_COLOR));
		middle.add(createItemPanel(itemClass, store, "defensive_rune", "data/gui/rune-defensive.png",
				"Slot glifu obronnego", DEFENSIVE_SLOT_COLOR));

		right.add(createItemPanel(itemClass, store, "healing_rune", "data/gui/rune-healing.png",
				"Slot glifu leczenia", HEALING_SLOT_COLOR));
		right.add(createItemPanel(itemClass, store, "resistance_rune", "data/gui/rune-resistance.png",
				"Slot glifu odporności", RESISTANCE_SLOT_COLOR));

		InternalManagedWindow window = new InternalManagedWindow("runicaltar", "Ołtarz Runiczny");
		window.setContent(content);
		window.setHideOnClose(true);
		window.setMinimizable(true);
		window.setMovable(true);

		return window;
	}

	private ItemPanel createItemPanel(Class<? extends IEntity> itemClass, SpriteStore store,
			String id, String image, final String tooltip, final Color slotColor) {
		final Sprite placeholder = store.getSprite(image);
		final Sprite overlayPlaceholder = prepareOverlayPlaceholder(placeholder);
		ItemPanel panel = new ItemPanel(id, placeholder) {
			private static final long serialVersionUID = 1L;

			@Override
			public void paintComponent(final Graphics graphics) {
				super.paintComponent(graphics);
				if (getEntity() != null) {
					return;
				}

				final Graphics2D tintGraphics = (Graphics2D) graphics.create();
				try {
					tintGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_OFF);
					tintGraphics.setColor(slotColor);
					tintGraphics.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2,
							SLOT_ARC_DIAMETER, SLOT_ARC_DIAMETER);
				} finally {
					tintGraphics.dispose();
				}

				if (overlayPlaceholder != null) {
					overlayPlaceholder.draw(graphics,
							(getWidth() - overlayPlaceholder.getWidth()) / 2,
							(getHeight() - overlayPlaceholder.getHeight()) / 2);
				}
			}

			@Override
			public String getToolTipText(final MouseEvent event) {
				if (getEntity() == null) {
					return tooltip;
				}
				return super.getToolTipText(event);
			}
		};
		slotPanels.put(id, panel);
		panel.setAcceptedTypes(itemClass);
		panel.setToolTipText(tooltip);

		return panel;
	}

	private Sprite prepareOverlayPlaceholder(final Sprite original) {
		if ((original == null) || (TransparencyMode.TRANSPARENCY == Transparency.BITMASK)) {
			return original;
		}

		final BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(),
				BufferedImage.TYPE_INT_ARGB);
		final Graphics graphics = image.createGraphics();
		original.draw(graphics, 0, 0);
		graphics.dispose();

		final RescaleOp rescaleOp = new RescaleOp(
				new float[] {3.0f, 3.0f, 3.0f, 0.5f},
				new float[] {-450f, -450f, -450f, 0f}, null);
		rescaleOp.filter(image, image);
		return new ImageSprite(image);
	}

	/**
	 * Updates the player slot panels.
	 */
	private void refreshContents() {
		for (final Entry<String, ItemPanel> entry : slotPanels.entrySet()) {
			final ItemPanel entitySlot = entry.getValue();

			if (entitySlot != null) {
				// Set the parent entity for all slots, even if they are not
				// visible. They may become visible without zone changes
				entitySlot.setParent(player);

				final RPSlot slot = player.getSlot(entry.getKey());
				if (slot == null) {
					continue;
				}

				final Iterator<RPObject> iter = slot.iterator();

				if (iter.hasNext()) {
					final RPObject object = iter.next();

					IEntity entity = GameObjects.getInstance().get(object);

					entitySlot.setEntity(entity);
				} else {
					entitySlot.setEntity(null);
				}
			}
		}
	}

	@Override
	public void contentAdded(RPSlot added) {
		ItemPanel panel = slotPanels.get(added.getName());
		if (panel == null) {
			// Not a slot we are interested in
			return;
		}

		for (RPObject obj : added) {
			ID id = obj.getID();
			IEntity entity = panel.getEntity();
			if (entity != null && id.equals(entity.getRPObject().getID())) {
				// Changed rather than added.
				return;
			}
			// Actually added, fetch the corresponding entity
			entity = GameObjects.getInstance().get(obj);
			if (entity == null) {
				logger.error("Unable to find entity for: " + obj,
						new Throwable("here"));
				return;
			}

			panel.setEntity(entity);
		}
	}

	@Override
	public void contentRemoved(RPSlot removed) {
		ItemPanel panel = slotPanels.get(removed.getName());
		if (panel == null) {
			// Not a slot we are interested in
			return;
		}
		for (RPObject obj : removed) {
			ID id = obj.getID();
			IEntity entity = panel.getEntity();
			if (entity != null && id.equals(entity.getRPObject().getID())) {
				if (obj.size() == 1) {
					// The object was removed
					panel.setEntity(null);
					continue;
				}
			} else {
				logger.error("Tried removing wrong object from a panel. "
						+ "removing: " + obj + " , but panel contains: "
						+ panel.getEntity(), new Throwable());
			}
		}
	}
}
