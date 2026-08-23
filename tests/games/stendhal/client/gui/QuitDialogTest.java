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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.util.UserInterfaceTestHelper;

public class QuitDialogTest {
	@BeforeClass
	public static void init() {
		UserInterfaceTestHelper.initUserInterface();
	}

	@Test
	public void containsVerticalSessionActionsInNaturalOrder() {
		QuitDialog dialog = new QuitDialog();
		List<String> labels = new ArrayList<String>();
		collectButtonLabels(dialog.getQuitDialog(), labels);

		assertEquals(Arrays.asList("Ustawienia", "Zmień postać", "Wyjdź z gry"), labels);
	}

	@Test
	public void canBeHiddenBySecondEscapeAction() {
		QuitDialog dialog = new QuitDialog();
		dialog.getQuitDialog().setVisible(true);
		assertTrue(dialog.isVisible());

		dialog.hide();
		assertFalse(dialog.isVisible());
	}

	private static void collectButtonLabels(Component component, List<String> labels) {
		if (component instanceof JButton) {
			String text = ((JButton) component).getText();
			if (text != null && !text.isEmpty()) {
				labels.add(text);
			}
		}
		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				collectButtonLabels(child, labels);
			}
		}
	}
}
