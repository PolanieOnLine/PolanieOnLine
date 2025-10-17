/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.actions;

import static games.stendhal.common.constants.Actions.TARGET_PATH;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.useable.GoldenCauldronEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.EntityHelper;
import marauroa.common.game.RPAction;

/**
 * Handles custom actions for the golden cauldron.
 */
public class GoldenCauldronAction implements ActionListener {
	private static final String ACTION = "goldencauldron";
	private static final String COMMAND = "command";
	private static final String MIX = "mix";

	/**
	 * Register the action.
	 */
	public static void register() {
		CommandCenter.register(ACTION, new GoldenCauldronAction());
	}

	@Override
	public void onAction(final Player player, final RPAction action) {
		if (!action.has(TARGET_PATH)) {
			return;
		}

		final Entity entity =
			EntityHelper.getEntityFromPath(player, action.getList(TARGET_PATH));
		if (!(entity instanceof GoldenCauldronEntity)) {
			return;
		}

		final GoldenCauldronEntity cauldron = (GoldenCauldronEntity) entity;
		final String command = action.get(COMMAND);
		if (MIX.equals(command)) {
			cauldron.mix(player);
		}
	}
}
