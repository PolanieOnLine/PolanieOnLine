/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DatabaseFactory;

public class SeasonalItemCleanupDAOTest {
	@BeforeClass
	public static void initialize() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
	}

	@Test
	public void firstShutdownAndSubsequentShutdownsAdvanceDurably() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			transaction.execute("DELETE FROM seasonal_item_cleanup WHERE event_key='minetown'", null);
			final SeasonalItemCleanupDAO dao = new SeasonalItemCleanupDAO();
			assertEquals(0, dao.load(transaction));
			assertEquals(1, dao.advance(transaction));
			assertEquals(1, new SeasonalItemCleanupDAO().load(transaction));
			assertEquals(2, dao.advance(transaction));
			assertEquals(2, new SeasonalItemCleanupDAO().load(transaction));
		} finally { pool.rollback(transaction); }
	}
}
