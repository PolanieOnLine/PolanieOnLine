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

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.Log4J;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;
import marauroa.server.game.db.DatabaseFactory;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class StendhalCharacterItemDAOTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		Log4J.init();
		new DatabaseFactory().initializeDatabase();
		PlayerTestHelper.generatePlayerRPClasses();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void testInventorySnapshotStoresRarityAndUpgradeData() throws Exception {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			final Player player = PlayerTestHelper.createPlayerWithOutFit("inventory_snapshot_player");
			final Item item = new Item("test sword", "sword", "test", null);
			item.put(StendhalItemDAO.ATTR_ITEM_LOGID, 12345);
			item.put(Item.RARITY_ID, "epic");
			item.put(Item.UPGRADE_LEVEL_ATTRIBUTE, 2);
			item.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, 5);
			player.getSlot("bag").add(item);

			final StendhalCharacterItemDAO dao = new StendhalCharacterItemDAO();
			dao.replaceInventorySnapshot(transaction, player,
					new Timestamp(System.currentTimeMillis()));

			final ResultSet result = transaction.query(
					"SELECT slot_name, itemid, item_name, item_class, item_subclass,"
					+ " quantity, rarity_id, upgrade_level, max_upgrade_level"
					+ " FROM character_items WHERE charname='inventory_snapshot_player'", null);
			try {
				assertTrue(result.next());
				assertEquals("bag", result.getString("slot_name"));
				assertEquals(12345, result.getInt("itemid"));
				assertEquals("test sword", result.getString("item_name"));
				assertEquals("sword", result.getString("item_class"));
				assertEquals("test", result.getString("item_subclass"));
				assertEquals(1, result.getInt("quantity"));
				assertEquals("epic", result.getString("rarity_id"));
				assertEquals(2, result.getInt("upgrade_level"));
				assertEquals(5, result.getInt("max_upgrade_level"));
				assertFalse(result.next());
			} finally {
				result.close();
			}

			player.getSlot("bag").clear();
			dao.replaceInventorySnapshot(transaction, player,
					new Timestamp(System.currentTimeMillis()));
			assertEquals(0, transaction.querySingleCellInt(
					"SELECT count(*) FROM character_items"
					+ " WHERE charname='inventory_snapshot_player'", null));

			TransactionPool.get().rollback(transaction);
		} catch (final Exception e) {
			TransactionPool.get().rollback(transaction);
			throw e;
		}
	}

	@Test
	public void testLegacyItemDefaultsToCommonAndNoUpgrade() throws Exception {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			final Player player = PlayerTestHelper.createPlayerWithOutFit("inventory_legacy_player");
			player.getSlot("bag").add(new Item("legacy sword", "sword", "test", null));

			new StendhalCharacterItemDAO().replaceInventorySnapshot(transaction, player,
					new Timestamp(System.currentTimeMillis()));

			final ResultSet result = transaction.query(
					"SELECT rarity_id, upgrade_level, max_upgrade_level"
					+ " FROM character_items WHERE charname='inventory_legacy_player'", null);
			try {
				assertTrue(result.next());
				assertEquals("common", result.getString("rarity_id"));
				assertEquals(0, result.getInt("upgrade_level"));
				assertEquals(0, result.getInt("max_upgrade_level"));
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
