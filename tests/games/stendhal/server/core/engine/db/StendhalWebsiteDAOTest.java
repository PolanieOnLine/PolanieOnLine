/***************************************************************************
 *                    (C) Copyright 2003-2026 - Stendhal                   *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.Timestamp;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.DatabaseFactory;
import marauroa.server.game.db.RPObjectDAO;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class StendhalWebsiteDAOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
		PlayerTestHelper.generatePlayerRPClasses();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void testRuneStatsAreStoredAndCleared() throws Exception {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			transaction.execute("DELETE FROM character_stats WHERE name='rune_stats_player'", null);

			final Player player = PlayerTestHelper.createPlayerWithOutFit("rune_stats_player");
			player.getSlot("offensive_rune").add(new Item("test offensive rune", "rune", "test", null));
			player.getSlot("utility_rune").add(new Item("test utility rune", "rune", "test", null));

			final StendhalWebsiteDAO dao = new StendhalWebsiteDAO();
			dao.insertIntoCharStats(transaction, player, new Timestamp(System.currentTimeMillis()));

			ResultSet result = transaction.query(
					"SELECT offensive_rune, defensive_rune, utility_rune"
					+ " FROM character_stats WHERE name='rune_stats_player'", null);
			try {
				assertTrue(result.next());
				assertEquals("test offensive rune", result.getString("offensive_rune"));
				assertEquals("", result.getString("defensive_rune"));
				assertEquals("test utility rune", result.getString("utility_rune"));
			} finally {
				result.close();
			}

			player.getSlot("offensive_rune").clear();
			dao.updateCharStats(transaction, player, new Timestamp(System.currentTimeMillis()));

			result = transaction.query(
					"SELECT offensive_rune FROM character_stats WHERE name='rune_stats_player'", null);
			try {
				assertTrue(result.next());
				assertEquals("", result.getString("offensive_rune"));
			} finally {
				result.close();
			}

			TransactionPool.get().rollback(transaction);
		} catch (final Exception e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}

	@Test
	public void testBackfillRuneStatsLoadsPersistedRPObject() throws Exception {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			final String name = "legacy_backfill_rune_stats";
			transaction.execute("DELETE FROM character_stats WHERE name='" + name + "'", null);
			transaction.execute("DELETE FROM characters WHERE charname='" + name + "'", null);

			final RPObject player = new RPObject();
			player.setRPClass("player");
			player.put("name", name);
			player.addSlot(new RPSlot("offensive_rune"));
			player.getSlot("offensive_rune").add(
					new Item("persisted offensive rune", "rune", "test", null));

			final int objectId = DAORegister.get().get(RPObjectDAO.class)
					.storeRPObject(transaction, player);
			transaction.execute(
					"INSERT INTO characters (player_id, charname, object_id, status)"
					+ " VALUES (1, '" + name + "', " + objectId + ", 'active')", null);
			transaction.execute(
					"INSERT INTO character_stats (name, offensive_rune, defensive_rune,"
					+ " resistance_rune, utility_rune, healing_rune, control_rune, special_rune)"
					+ " VALUES ('" + name + "', NULL, NULL, NULL, NULL, NULL, NULL, NULL)", null);

			final StendhalWebsiteDAO dao = new StendhalWebsiteDAO();
			assertEquals(1, dao.backfillRuneStats(transaction));

			final ResultSet result = transaction.query(
					"SELECT offensive_rune, defensive_rune FROM character_stats"
					+ " WHERE name='" + name + "'", null);
			try {
				assertTrue(result.next());
				assertEquals("persisted offensive rune", result.getString("offensive_rune"));
				assertEquals("", result.getString("defensive_rune"));
			} finally {
				result.close();
			}

			TransactionPool.get().rollback(transaction);
		} catch (final Exception e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}

	@Test
	public void testRuneStatsCanBeUpdatedFromRawRPObject() throws Exception {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			transaction.execute("DELETE FROM character_stats WHERE name='legacy_rune_stats'", null);
			transaction.execute("INSERT INTO character_stats (name) VALUES ('legacy_rune_stats')", null);

			final RPObject player = new RPObject();
			player.put("name", "legacy_rune_stats");
			player.addSlot(new RPSlot("offensive_rune"));
			final RPObject rune = new RPObject();
			rune.put("name", "legacy offensive rune");
			player.getSlot("offensive_rune").add(rune);

			final StendhalWebsiteDAO dao = new StendhalWebsiteDAO();
			dao.updateRuneStats(transaction, player);

			final ResultSet result = transaction.query(
					"SELECT offensive_rune, defensive_rune"
					+ " FROM character_stats WHERE name='legacy_rune_stats'", null);
			try {
				assertTrue(result.next());
				assertEquals("legacy offensive rune", result.getString("offensive_rune"));
				assertEquals("", result.getString("defensive_rune"));
			} finally {
				result.close();
			}

			TransactionPool.get().rollback(transaction);
		} catch (final Exception e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}
}
