/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import javax.swing.JPanel;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.util.UserInterfaceTestHelper;

public class ReserveSetWindowTest {
	@BeforeClass
	public static void init() {
		UserInterfaceTestHelper.initUserInterface();
	}

	@Test
	public void setSlotsAreRequiredBeforeSavedVisibilityCanRevealWindow() {
		TestOwner owner = new TestOwner();
		ReserveSetWindow window = new ReserveSetWindow(owner, new JPanel());

		window.setVisible(true);
		assertFalse(window.isVisible());

		owner.available = true;
		window.setVisible(true);
		assertTrue(window.isVisible());

		owner.available = false;
		window.setVisible(true);
		assertFalse(window.isVisible());
	}

	private static final class TestOwner implements ReserveSetWindow.Owner {
		private boolean available;

		@Override
		public boolean isReserveWindowAvailable() {
			return available;
		}

		@Override
		public void onReserveWindowVisibilityChange(boolean visible) {
			// Component events are not relevant for the visibility gate test.
		}
	}
}
