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
package games.stendhal.server.core.engine.dbcommand;

import java.io.IOException;
import java.sql.SQLException;

import games.stendhal.server.core.engine.db.GuildMembershipDAO;
import games.stendhal.server.core.engine.db.GuildMembershipDAO.GuildMembershipData;
import games.stendhal.server.entity.player.Player;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.command.AbstractDBCommand;
import marauroa.server.game.db.DAORegister;

/**
 * Reads guild membership data for player login runtime cache.
 */
public class ReadGuildMembershipForPlayerCommand extends AbstractDBCommand {
	private final Player player;
	private GuildMembershipData guildMembership;

	public ReadGuildMembershipForPlayerCommand(final Player player) {
		this.player = player;
	}

	@Override
	public void execute(final DBTransaction transaction) throws SQLException, IOException {
		guildMembership = DAORegister.get().get(GuildMembershipDAO.class)
				.loadByCharacterName(transaction, player.getName());
	}

	public Player getPlayer() {
		return player;
	}

	public GuildMembershipData getGuildMembership() {
		return guildMembership;
	}

	@Override
	public String toString() {
		return "ReadGuildMembershipForPlayerCommand [player=" + player + "]";
	}
}
