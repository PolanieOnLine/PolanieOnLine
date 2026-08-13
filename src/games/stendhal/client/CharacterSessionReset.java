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
package games.stendhal.client;

import java.awt.Frame;

import javax.swing.JFrame;

import games.stendhal.client.entity.CharacterSessionEntityReset;
import games.stendhal.client.gui.CharacterSessionGuiReset;
import games.stendhal.client.gui.j2DClient;

/**
 * Clears state owned by the active character after the server accepted a
 * character-session leave. Account authentication and the network connection
 * are deliberately left untouched.
 */
public final class CharacterSessionReset {
	private CharacterSessionReset() {
		// utility class
	}

	/**
	 * Reset the local world before another character is selected.
	 *
	 * @param client active authenticated client
	 */
	public static void reset(final StendhalClient client) {
		j2DClient ui = j2DClient.get();
		if (ui != null) {
			Frame frame = ui.getMainFrame();
			if (frame instanceof JFrame) {
				CharacterSessionGuiReset.reset((JFrame) frame);
			}
		}

		client.getGameObjects().clear();
		client.getStaticGameLayers().clear();
		UserContext.get().resetCharacterSession();
		CharacterSessionEntityReset.reset();
		World.get().getPlayerList().getNamesList().clear();
	}
}
