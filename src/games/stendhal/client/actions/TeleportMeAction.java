/***************************************************************************
 *                   (C) Copyright 2003-2023 - Stendhal                    *
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

import static games.stendhal.common.constants.Actions.TELEPORTME;

import games.stendhal.client.ClientSingletonRepository;
import marauroa.common.game.RPAction;

/**
 * Teleport a player.
 */
class TeleportMeAction implements SlashAction {
	/**
	 * Execute a chat command.
	 *
	 * @param params
	 *            The formal parameters.
	 * @param remainder
	 *            Line content after parameters.
	 *
	 * @return <code>true</code> if was handled.
	 */
	@Override
	public boolean execute(final String[] params, final String remainder) {
		final RPAction teleport = new RPAction();

		teleport.put("type", TELEPORTME);
		teleport.put("zone", params[0]);
		teleport.put("x", params[1]);
		teleport.put("y", params[2]);

		ClientSingletonRepository.getClientFramework().send(teleport);

		return true;
	}

	/**
	 * Get the maximum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMaximumParameters() {
		return 3;
	}

	/**
	 * Get the minimum number of formal parameters.
	 *
	 * @return The parameter count.
	 */
	@Override
	public int getMinimumParameters() {
		return 3;
	}
}
