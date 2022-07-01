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
package games.stendhal.client.actions;

import games.stendhal.client.ClientSingletonRepository;
import games.stendhal.common.constants.Actions;
import marauroa.common.game.RPAction;

/**
 * @author KarajuSs
 */
public class AchievementLogAction implements SlashAction {
	@Override
	public boolean execute(final String[] params, final String remainder) {
		final RPAction action = new RPAction();
		action.put("type", Actions.ACHIEVEMENTLOG);
		ClientSingletonRepository.getClientFramework().send(action);

		return true;
	}

	@Override
	public int getMaximumParameters() {
		return 0;
	}

	@Override
	public int getMinimumParameters() {
		return 0;
	}
}
