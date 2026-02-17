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
package games.stendhal.server.entity.player;

import games.stendhal.server.core.engine.db.GuildMembershipDAO.GuildMembershipData;
import games.stendhal.server.core.engine.dbcommand.ReadGuildMembershipForPlayerCommand;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnListenerDecorator;
import games.stendhal.server.core.events.TurnNotifier;
import marauroa.server.db.command.DBCommand;
import marauroa.server.db.command.DBCommandQueue;
import marauroa.server.db.command.ResultHandle;

/**
 * Loads guild membership data from DB when player logs in.
 */
public class ReadGuildMembershipOnLogin implements LoginListener, TurnListener {
	private final ResultHandle handle = new ResultHandle();

	@Override
	public void onLoggedIn(final Player player) {
		DBCommand command = new ReadGuildMembershipForPlayerCommand(player);
		DBCommandQueue.get().enqueueAndAwaitResult(command, handle);
		TurnNotifier.get().notifyInTurns(1, new TurnListenerDecorator(this));
	}

	@Override
	public void onTurnReached(final int currentTurn) {
		ReadGuildMembershipForPlayerCommand command = DBCommandQueue.get().getOneResult(
				ReadGuildMembershipForPlayerCommand.class, handle);

		if (command == null) {
			TurnNotifier.get().notifyInTurns(0, new TurnListenerDecorator(this));
			return;
		}

		Player player = command.getPlayer();
		GuildMembershipData guildMembership = command.getGuildMembership();
		if (guildMembership == null) {
			player.clearGuildMembership();
			return;
		}

		player.setGuildMembership(guildMembership.getGuildId(),
				guildMembership.getGuildName(), guildMembership.getGuildTag());
	}
}
