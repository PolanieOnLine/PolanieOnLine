/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static games.stendhal.common.constants.Actions.TEXT;

import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.ScreenAnnouncementBroadcaster;
import marauroa.common.game.RPAction;

/**
 * Dedicated command for center-top administrative screen announcements.
 */
public class AnnounceAction extends AdministrationAction {

	public static void register() {
		CommandCenter.register("announce", new AnnounceAction(), 3);
	}

	@Override
	protected void perform(final Player player, final RPAction action) {
		if (!action.has(TEXT)) {
			return;
		}

		String sender = player.getName();
		if (action.has("sender") && (player.getName().equals("postman"))) {
			sender = action.get("sender");
		}

		new GameEvent(sender, "announce", action.get(TEXT)).raise();
		ScreenAnnouncementBroadcaster.broadcastToAllPlayers(
				"Ogłoszenie administracji",
				action.get(TEXT),
				ScreenAnnouncementBroadcaster.CATEGORY_ADMIN);
	}
}
