/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions;

import games.stendhal.common.constants.Actions;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.AchievementLogEvent;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPEvent;

/**
 * @author KarajuSs
 */
public class AchievementLogAction implements ActionListener {
	public static void register() {
		CommandCenter.register(Actions.ACHIEVEMENTLOG, new AchievementLogAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		final RPEvent event = new AchievementLogEvent(player);
		player.addEvent(event);
		player.notifyWorldAboutChanges();
	}
}