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
package games.stendhal.server.actions.query;

import static games.stendhal.common.constants.Actions.MAP_EVENT_STATUS_SNAPSHOT;

import games.stendhal.server.actions.ActionListener;
import games.stendhal.server.actions.CommandCenter;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.event.MapEventStatusPublisher;
import marauroa.common.game.RPAction;

/**
 * Sends an immediate map event status snapshot to the requester.
 */
public class MapEventStatusSnapshotAction implements ActionListener {
	public static void register() {
		CommandCenter.register(MAP_EVENT_STATUS_SNAPSHOT, new MapEventStatusSnapshotAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		MapEventStatusPublisher.sendImmediateSnapshot(player);
		player.notifyWorldAboutChanges();
	}
}
