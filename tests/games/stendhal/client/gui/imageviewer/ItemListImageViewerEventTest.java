/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.imageviewer;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;

public class ItemListImageViewerEventTest {

	@Test
	public void testRarityColorsHighlightedItemName() {
		assertEquals("before <u><font color=\"#ff8c00\">sword</font></u> after",
				ItemListImageViewerEvent.colorHighlightedItemName(
						"before <u>sword</u> after", ItemRarity.LEGENDARY));
	}

	@Test
	public void testMissingRarityLeavesDescriptionUnchanged() {
		assertEquals("before <u>sword</u> after",
				ItemListImageViewerEvent.colorHighlightedItemName(
						"before <u>sword</u> after", null));
	}

	@Test
	public void testDescriptionWithoutHighlightedNameIsSafe() {
		assertEquals("plain description",
				ItemListImageViewerEvent.colorHighlightedItemName(
						"plain description", ItemRarity.RARE));
	}
}
