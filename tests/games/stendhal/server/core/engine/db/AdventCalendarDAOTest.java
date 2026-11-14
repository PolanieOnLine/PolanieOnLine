package games.stendhal.server.core.engine.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.entity.player.Player;
import marauroa.common.Log4J;
import marauroa.common.game.RPObject;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DatabaseFactory;
import utilities.PlayerTestHelper;

public class AdventCalendarDAOTest {

	@BeforeClass
	public static void initializeDatabase() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
	}

	@Test
	public void frozenInventoryAndAccountClaimShareOneTransaction() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			final Map<String, Object> params = createAccount(transaction);
			final Player player = PlayerTestHelper.createPlayer("gift_" + UUID.randomUUID().toString().substring(0, 12));
			final StendhalCharacterDAO characters = new StendhalCharacterDAO();
			characters.addCharacter(transaction, (String) params.get("username"), player.getName(), player,
					new Timestamp(System.currentTimeMillis()));
			player.setQuest(AdventCalendarDAO.QUEST_SLOT, "2026;512");
			PlayerTestHelper.equipWithStackableItem(player, "money", 5000);
			final RPObject frozen = (RPObject) player.clone();
			assertTrue(frozen instanceof Player);
			characters.storeCharacter(transaction, (String) params.get("username"), player.getName(), frozen,
					new Timestamp(System.currentTimeMillis()));
			final AdventCalendarDAO calendar = new AdventCalendarDAO();
			assertEquals(512, calendar.claimedMask(transaction, (Integer) params.get("account_id"), 2026));
			assertEquals(0, calendar.claimedMask(transaction, (Integer) params.get("account_id"), 2027));
			final RPObject restored = characters.loadCharacter(transaction, (String) params.get("username"), player.getName());
			final Player restoredPlayer = new Player(restored);
			assertEquals(5000, restoredPlayer.getNumberOfEquipped("money"));
			assertEquals("2026;512", restoredPlayer.getQuest(AdventCalendarDAO.QUEST_SLOT));
			calendar.persistClaims(transaction, (String) params.get("username"), restoredPlayer);
			assertEquals(512, calendar.claimedMask(transaction, (Integer) params.get("account_id"), 2026));
		} finally {
			pool.rollback(transaction);
		}
	}

	@Test
	public void committedAccountClaimSurvivesANewTransaction() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		final int accountId;
		boolean committed = false;
		try {
			final Map<String, Object> params = createAccount(transaction);
			accountId = (Integer) params.get("account_id");
			final Player player = PlayerTestHelper.createPlayer("saved_advent");
			player.setQuest(AdventCalendarDAO.QUEST_SLOT, "2026;512");
			new AdventCalendarDAO().persistClaims(transaction, (String) params.get("username"), player);
			pool.commit(transaction);
			committed = true;
		} finally {
			if (!committed) {
				pool.rollback(transaction);
			}
		}
		final DBTransaction restored = pool.beginWork();
		try {
			assertEquals(512, new AdventCalendarDAO().claimedMask(restored, accountId, 2026));
		} finally {
			pool.rollback(restored);
		}
	}

	@Test
	public void rolledBackClaimLeavesNoAccountRecord() throws Exception {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		final int accountId;
		try {
			final Map<String, Object> params = createAccount(transaction);
			accountId = (Integer) params.get("account_id");
			final Player player = PlayerTestHelper.createPlayer("rollback_gift");
			player.setQuest(AdventCalendarDAO.QUEST_SLOT, "2026;8388608");
			new AdventCalendarDAO().persistClaims(transaction, (String) params.get("username"), player);
			assertEquals(8388608, new AdventCalendarDAO().claimedMask(transaction, accountId, 2026));
		} finally {
			pool.rollback(transaction);
		}
		final DBTransaction check = pool.beginWork();
		try {
			assertEquals(0, new AdventCalendarDAO().claimedMask(check, accountId, 2026));
		} finally {
			pool.rollback(check);
		}
	}

	private Map<String, Object> createAccount(final DBTransaction transaction) throws Exception {
		final Map<String, Object> params = new HashMap<>();
		params.put("username", "advent_" + UUID.randomUUID().toString().substring(0, 12));
		transaction.execute("INSERT INTO account (username, password, status, timedate)"
				+ " VALUES ('[username]', '00', 'active', CURRENT_TIMESTAMP)", params);
		params.put("account_id", transaction.querySingleCellInt(
				"SELECT id FROM account WHERE username='[username]'", params));
		return params;
	}
}
