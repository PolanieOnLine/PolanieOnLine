/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.account;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;

import org.junit.BeforeClass;
import org.junit.Test;

import marauroa.common.Log4J;
import marauroa.common.game.Result;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.DAORegister;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class CharacterCreatorTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
		PlayerTestHelper.generatePlayerRPClasses();
		ItemTestHelper.generateRPClasses();
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			final AccountDAO accounts = DAORegister.get().get(AccountDAO.class);
			if (!accounts.hasPlayer(transaction, "user")) {
				accounts.addPlayer(transaction, "user", new byte[16], "user@example.invalid");
			}
			TransactionPool.get().commit(transaction);
		} catch (Exception e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}

	/**
	 * Tests for create.
	 * @throws SQLException
	 */
	@Test
	public void testCreate() throws SQLException {
		cleanDB();

		final CharacterCreator cc = new CharacterCreator("user", "player", null);
		assertEquals(Result.OK_CREATED, cc.create().getResult());
		assertEquals(Result.FAILED_PLAYER_EXISTS, cc.create().getResult());

		cleanDB();
	}

	@Test
	public void testFullAccountReturnsTooMany() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction setup = pool.beginWork();
		try {
			DAORegister.get().get(AccountDAO.class).addPlayer(
					setup, "fullaccount", new byte[16], "fullaccount@example.invalid");
			final int accountId = DAORegister.get().get(AccountDAO.class)
					.getDatabasePlayerId(setup, "fullaccount");
			for (int i = 0; i < 8; i++) {
				setup.execute("INSERT INTO characters (player_id, charname, object_id, status, timedate)"
						+ " VALUES (" + accountId + ", 'limitfixture" + i + "', 0, 'inactive', CURRENT_TIMESTAMP)", null);
			}
			pool.commit(setup);
		} catch (Exception e) {
			pool.rollback(setup);
			throw e;
		}
		try {
			assertEquals(Result.FAILED_TOO_MANY,
					new CharacterCreator("fullaccount", "limitnewplayer", null).create().getResult());
		} finally {
			final DBTransaction cleanup = pool.beginWork();
			try {
				cleanup.execute("DELETE FROM characters WHERE player_id IN"
						+ " (SELECT id FROM account WHERE username='fullaccount')", null);
				cleanup.execute("DELETE FROM email WHERE player_id IN"
						+ " (SELECT id FROM account WHERE username='fullaccount')", null);
				cleanup.execute("DELETE FROM account WHERE username='fullaccount'", null);
				pool.commit(cleanup);
			} catch (Exception e) {
				pool.rollback(cleanup);
				throw e;
			}
		}
	}

	private void cleanDB() throws SQLException {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			transaction.execute("DELETE FROM character_stats where name='player';", null);
			transaction.execute("DELETE FROM rpobject WHERE object_id IN (SELECT object_id FROM characters WHERE characters.charname = 'player');", null);
			transaction.execute("DELETE FROM characters WHERE characters.charname = 'player';", null);
			TransactionPool.get().commit(transaction);
		} catch (final SQLException e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}
}
