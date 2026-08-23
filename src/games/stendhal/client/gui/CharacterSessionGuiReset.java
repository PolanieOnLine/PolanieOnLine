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

import java.awt.Component;
import java.awt.Container;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;

/**
 * Clears desktop UI bindings that belong to the active character. The main
 * frame is intentionally preserved because it owns the authenticated client
 * session and the next character selector.
 */
public final class CharacterSessionGuiReset {
	private CharacterSessionGuiReset() {
		// utility class
	}

	/**
	 * Clear character-owned bindings on the game loop and schedule visual
	 * dialog cleanup on the Swing event dispatch thread.
	 *
	 * @param frame main client frame
	 */
	public static void reset(final JFrame frame) {
		clearBindings(frame.getContentPane());

		/*
		 * Snapshot the currently owned windows before a fresh character list can
		 * create the new selector. The deferred cleanup must never hide UI that
		 * belongs to the post-leave selection phase.
		 */
		final Window[] oldOwnedWindows = frame.getOwnedWindows();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				hideLayeredWindows(frame.getContentPane());
				for (Window window : oldOwnedWindows) {
					if (window != null) {
						window.setVisible(false);
					}
				}
			}
		});
	}

	private static void clearBindings(final Component component) {
		if (component instanceof SlotWindow) {
			((SlotWindow) component).clearSlot();
		}
		if (component instanceof ItemPanel) {
			ItemPanel panel = (ItemPanel) component;
			panel.setEntity(null);
			panel.setParent(null);
		}
		if (component instanceof Character) {
			Character character = (Character) component;
			character.setTitle("Character");
			character.resetSession();
		}

		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				clearBindings(child);
			}
		}
	}

	private static void hideLayeredWindows(final Component component) {
		if (component instanceof InternalManagedWindow
				&& component.getParent() instanceof JLayeredPane) {
			component.setVisible(false);
			return;
		}

		if (component instanceof Container) {
			for (Component child : ((Container) component).getComponents()) {
				hideLayeredWindows(child);
			}
		}
	}
}
