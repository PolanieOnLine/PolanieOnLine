/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
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

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.junit.Test;

import games.stendhal.client.entity.IEntity;
import games.stendhal.client.entity.User;
import games.stendhal.client.entity.factory.EntityFactory;
import games.stendhal.client.gui.j2d.entity.EntityView;
import games.stendhal.client.gui.j2d.entity.EntityViewFactory;
import games.stendhal.client.gui.styled.cursor.CursorRepository;
import games.stendhal.client.gui.styled.cursor.StendhalCursor;
import games.stendhal.common.constants.ItemRarity;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemPanelTest {
	private static final CursorRepository cursors = new CursorRepository();

	@Test
	public void rarityOutlineUsesFourDirectionSinglePixelThickness() {
		final BufferedImage source = new BufferedImage(7, 7,
				BufferedImage.TYPE_INT_ARGB);
		source.setRGB(3, 3, 0xffffffff);

		final BufferedImage outline = ItemPanel.createRarityOutline(source,
				ItemRarity.LEGENDARY);
		final int rarityColor = Color.decode(
				ItemRarity.LEGENDARY.getColorHex()).getRGB();

		// Source pixel is not covered by the rarity marker.
		assertEquals(0, outline.getRGB(4, 4));
		// Only the four orthogonal neighbours use the existing rarity color.
		assertEquals(rarityColor, outline.getRGB(4, 3));
		assertEquals(rarityColor, outline.getRGB(3, 4));
		assertEquals(rarityColor, outline.getRGB(5, 4));
		assertEquals(rarityColor, outline.getRGB(4, 5));
		// Diagonal pixels stay empty, keeping the outline visually thin.
		assertEquals(0, outline.getRGB(3, 3));
		assertEquals(0, outline.getRGB(5, 3));
		assertEquals(0, outline.getRGB(3, 5));
		assertEquals(0, outline.getRGB(5, 5));
		// There is no second outline layer for higher rarities.
		assertEquals(0, outline.getRGB(2, 4));
		assertEquals(0, outline.getRGB(4, 2));
	}

	@Test
	public void missingRarityDoesNotCreateItemOutline() {
		final BufferedImage source = new BufferedImage(3, 3,
				BufferedImage.TYPE_INT_ARGB);
		source.setRGB(1, 1, 0xffffffff);
		final BufferedImage outline = ItemPanel.createRarityOutline(source, null);
		assertEquals(0, outline.getRGB(1, 1));
		assertEquals(0, outline.getRGB(2, 2));
	}

	/**
	 * Test getting the cursor.
	 */
	@Test
	public void testCursors() {
		ItemPanel panel = new ItemPanel("blah", null);
		// For comparing with the default cursor
		JPanel dummy = new JPanel();

		assertEquals("Default cursor", dummy.getCursor(), panel.getCursor());

		// Check adding an item to the slot
		RPObject obj = ItemTestHelper.createItem("wedding ring");
		IEntity item = EntityFactory.createEntity(obj);
		/*
		 * Set a dummy owner for the panel to simulate something not owned by
		 * the User
		 */
		panel.setParent(item);
		panel.setEntity(item);
		/*
		 * Comparing the string representations because the cursors come from
		 * different repositories, and would compare unequal otherwise
		 */
		assertEquals("Pick up cursor",
				cursors.get(StendhalCursor.ITEM_PICK_UP_FROM_SLOT).toString(),
				panel.getCursor().toString());

		// Repeat the checks with an user owned slot
		User user = new User();
		panel.setParent(user);
		/*
		 * Comparing empty slots first because normally the parent of the slot
		 * does not change from User to a non-user or vice versa, so ItemPanel
		 * does not handle the situation.
		 */
		panel.setEntity(null);
		assertEquals("Default cursor", dummy.getCursor(), panel.getCursor());
		panel.setEntity(item);
		// Get the cursor from the view
		EntityView<?> view = EntityViewFactory.create(item);
		assertEquals("Cursor from the entity view",
				cursors.get(view.getCursor()).toString(),
				panel.getCursor().toString());
	}
}
