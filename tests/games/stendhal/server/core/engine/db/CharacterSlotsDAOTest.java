package games.stendhal.server.core.engine.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.db.CharacterSlotsDAO.LimitReachedException;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DatabaseFactory;

public class CharacterSlotsDAOTest {
	@BeforeClass
	public static void initializeDatabase() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
	}

	private Map<String, Object> createAccount(final DBTransaction transaction) throws SQLException {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", "slots_" + UUID.randomUUID().toString().substring(0, 12));
		transaction.execute("INSERT INTO account (username, password, status, timedate)"
				+ " VALUES ('[username]', '00', 'active', CURRENT_TIMESTAMP)", params);
		params.put("player_id", transaction.querySingleCellInt(
				"SELECT id FROM account WHERE username='[username]'", params));
		return params;
	}

	private void addSlots(final DBTransaction transaction, final Map<String, Object> params,
			final int count, final String status) throws SQLException {
		params.put("status", status);
		for (int i = 0; i < count; i++) {
			params.put("charname", "slot_" + UUID.randomUUID().toString().substring(0, 12));
			transaction.execute("INSERT INTO characters (player_id, charname, object_id, status, timedate)"
					+ " VALUES ([player_id], '[charname]', 0, '[status]', CURRENT_TIMESTAMP)", params);
		}
	}

	@Test
	public void allowsEighthCharacterAndBlocksNinthAtPersistenceBoundary() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			final Map<String, Object> params = createAccount(transaction);
			addSlots(transaction, params, 7, "active");
			CharacterSlotsDAO.requireFreeSlot(transaction, (String) params.get("username"));
			addSlots(transaction, params, 1, "active");
			try {
				new StendhalCharacterDAO().addCharacter(transaction, (String) params.get("username"),
						"blocked_character", new RPObject(), new Timestamp(System.currentTimeMillis()));
				fail("Ninth character must be rejected before any character data is written");
			} catch (LimitReachedException expected) {
				assertEquals(8, transaction.querySingleCellInt(
						"SELECT count(*) FROM characters WHERE player_id=[player_id]", params));
			}
		} finally {
			pool.rollback(transaction);
		}
	}

	@Test
	public void inactiveCharactersStillOccupySlots() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			final Map<String, Object> params = createAccount(transaction);
			addSlots(transaction, params, 7, "active");
			addSlots(transaction, params, 1, "inactive");
			try {
				CharacterSlotsDAO.requireFreeSlot(transaction, (String) params.get("username"));
				fail("Inactive character must occupy its account slot");
			} catch (LimitReachedException expected) {
				// Expected.
			}
		} finally {
			pool.rollback(transaction);
		}
	}

	@Test
	public void accountsAlreadyAboveLimitCannotCreateMoreCharacters() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			final Map<String, Object> params = createAccount(transaction);
			addSlots(transaction, params, 9, "active");
			try {
				CharacterSlotsDAO.requireFreeSlot(transaction, (String) params.get("username"));
				fail("Over-capacity account must be rejected without deleting existing characters");
			} catch (LimitReachedException expected) {
				assertEquals(9, transaction.querySingleCellInt(
						"SELECT count(*) FROM characters WHERE player_id=[player_id]", params));
			}
		} finally {
			pool.rollback(transaction);
		}
	}

	@Test(timeout = 15000)
	public void concurrentRequestsCannotBothClaimLastSlot() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction setup = pool.beginWork();
		final Map<String, Object> params;
		try {
			params = createAccount(setup);
			addSlots(setup, params, 7, "active");
			pool.commit(setup);
		} catch (Exception e) {
			pool.rollback(setup);
			throw e;
		}
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final DBTransaction first = pool.beginWork();
		boolean committed = false;
		try {
			CharacterSlotsDAO.requireFreeSlot(first, (String) params.get("username"));
			final CountDownLatch started = new CountDownLatch(1);
			final Future<Boolean> second = executor.submit(() -> {
				final DBTransaction transaction = pool.beginWork();
				try {
					started.countDown();
					CharacterSlotsDAO.requireFreeSlot(transaction, (String) params.get("username"));
					return false;
				} catch (LimitReachedException expected) {
					return true;
				} finally {
					pool.rollback(transaction);
				}
			});
			if (!started.await(5, TimeUnit.SECONDS)) fail("Second request did not start");
			try {
				second.get(200, TimeUnit.MILLISECONDS);
				fail("Second request must wait for the first account lock");
			} catch (TimeoutException expected) {
				// The first request still owns the account lock.
			}
			addSlots(first, params, 1, "active");
			pool.commit(first);
			committed = true;
			assertEquals(Boolean.TRUE, second.get(5, TimeUnit.SECONDS));
		} finally {
			if (!committed) pool.rollback(first);
			executor.shutdownNow();
			executor.awaitTermination(5, TimeUnit.SECONDS);
			final DBTransaction cleanup = pool.beginWork();
			try {
				cleanup.execute("DELETE FROM characters WHERE player_id=[player_id]", params);
				cleanup.execute("DELETE FROM account WHERE id=[player_id]", params);
				pool.commit(cleanup);
			} catch (Exception e) {
				pool.rollback(cleanup);
				throw e;
			}
		}
	}
}
