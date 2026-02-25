/***************************************************************************
 *                    (C) Copyright 2003-2020 - Stendhal                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.apache.log4j.Logger;

import games.stendhal.server.entity.player.Player;
import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.db.CharacterDAO;
import marauroa.server.game.db.DAORegister;

/**
 * Stendhal specific extensions to the normal CharacterDAO which will update
 * the redundant tables for the web application.
 */
public class StendhalCharacterDAO extends CharacterDAO {
	private static Logger logger = Logger.getLogger(StendhalCharacterDAO.class);

	@Override
	public void addCharacter(final DBTransaction transaction, final String username,
			final String character, final RPObject player, Timestamp timestamp) throws SQLException, IOException {

		super.addCharacter(transaction, username, character, player, timestamp);

		if (!(player instanceof Player)) {
			logger.error("player no instance of Player but: " + player, new Throwable());
			return;
		}

		final Player instance = (Player) player;

		// Keep core character creation resilient even if auxiliary website/buddy sync fails.
		try {
			DAORegister.get().get(StendhalHallOfFameDAO.class)
				.setHallOfFamePoints(transaction, instance.getName(), "T", instance.getTradescore());
			DAORegister.get().get(StendhalWebsiteDAO.class).insertIntoCharStats(transaction, instance, timestamp);
			DAORegister.get().get(StendhalBuddyDAO.class).saveRelations(transaction, character, instance);
		} catch (final SQLException sqle) {
			logger.error("auxiliary character creation sync failed for " + character
				+ "; core character record already created", sqle);
		}
	}

	@Override
	public void storeCharacter(final DBTransaction transaction, final String username,
			final String character, final RPObject player, Timestamp timestamp) throws SQLException, IOException {

		super.storeCharacter(transaction, username, character, player, timestamp);

		if (!(player instanceof Player)) {
			logger.error("player no instance of Player but: " + player, new Throwable());
			return;
		}

		final Player instance = (Player) player;

		// Keep primary character persistence intact when redundant tables fail.
		try {
			final int count = DAORegister.get().get(StendhalWebsiteDAO.class).updateCharStats(transaction, instance, timestamp);
			if (count == 0) {
				DAORegister.get().get(StendhalWebsiteDAO.class).insertIntoCharStats(transaction, instance, timestamp);
			}
			DAORegister.get().get(StendhalBuddyDAO.class).saveRelations(transaction, character, instance);
		} catch (final SQLException sqle) {
			logger.error("auxiliary character sync failed for " + character
				+ "; keeping primary character record", sqle);
		}
	}

}
