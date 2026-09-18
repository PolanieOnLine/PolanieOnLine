/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.ResultSet;
import java.sql.Timestamp;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DatabaseFactory;
import utilities.PlayerTestHelper;

public class StendhalCharacterQuestDAOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void testQuestSnapshotStoresStateAndCompletion() throws Exception {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			final Player player = PlayerTestHelper.createPlayerWithOutFit("quest_snapshot_player");
			player.setQuest("website_done_quest", "done");
			player.setQuest("website_active_quest", "started;2");

			final StendhalCharacterQuestDAO dao = new StendhalCharacterQuestDAO();
			dao.replaceQuestSnapshot(transaction, player,
					new Timestamp(System.currentTimeMillis()));

			final ResultSet done = transaction.query(
					"SELECT quest_state, completed FROM character_quests"
					+ " WHERE charname='quest_snapshot_player'"
					+ " AND quest_name='website_done_quest'", null);
			try {
				assertTrue(done.next());
				assertEquals("done", done.getString("quest_state"));
				assertEquals(1, done.getInt("completed"));
				assertFalse(done.next());
			} finally {
				done.close();
			}

			final ResultSet active = transaction.query(
					"SELECT quest_state, completed FROM character_quests"
					+ " WHERE charname='quest_snapshot_player'"
					+ " AND quest_name='website_active_quest'", null);
			try {
				assertTrue(active.next());
				assertEquals("started;2", active.getString("quest_state"));
				assertEquals(0, active.getInt("completed"));
				assertFalse(active.next());
			} finally {
				active.close();
			}

			player.removeQuest("website_active_quest");
			dao.replaceQuestSnapshot(transaction, player,
					new Timestamp(System.currentTimeMillis()));
			assertEquals(0, transaction.querySingleCellInt(
					"SELECT count(*) FROM character_quests"
					+ " WHERE charname='quest_snapshot_player'"
					+ " AND quest_name='website_active_quest'", null));

			TransactionPool.get().rollback(transaction);
		} catch (final Exception e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}
}
