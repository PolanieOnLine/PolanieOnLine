package games.stendhal.server.core.engine.db;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;

import org.junit.Test;

public class StendhalAccountDAOTest {
	@Test
	public void acceptsFreshSteamSeed() {
		final long now = 1000000L;
		assertTrue(StendhalAccountDAO.isFresh(new Timestamp(now - 60000L), now));
	}

	@Test
	public void rejectsExpiredOrInvalidSteamSeed() {
		final long now = 1000000L;
		assertFalse(StendhalAccountDAO.isFresh(null, now));
		assertFalse(StendhalAccountDAO.isFresh(new Timestamp(now - 180001L), now));
		assertFalse(StendhalAccountDAO.isFresh(new Timestamp(now + 30001L), now));
	}
}
