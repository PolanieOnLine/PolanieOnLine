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

import games.stendhal.server.core.rule.rarity.ItemAffixState;
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
	public void testInventorySnapshotStoresRarityUpgradeStatsAndModifiers() throws Exception {
		final DBTransaction transaction = TransactionPool.get().beginWork();
		try {
			final Player player = PlayerTestHelper.createPlayerWithOutFit("inventory_snapshot_player");
			final Item item = new Item("test sword", "sword", "test", null);
			item.put(StendhalItemDAO.ATTR_ITEM_LOGID, 12345);
			item.put(Item.RARITY_ID, "epic");
			item.put(Item.UPGRADE_LEVEL_ATTRIBUTE, 2);
			item.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, 5);
			item.put("atk", 12);
			item.put("ratk", 4);
			item.put("def", 3);
			item.put("damage_min", 8);
			item.put("damage_max", 16);
			item.put("accuracy_bonus", 0.15);
			item.put("rate", 4);
			item.put(Item.VALUE, 500);
			item.put("lifesteal", 0.10);
			item.put(Item.RARITY_MODIFIERS, "atk", "1.20");
			item.put(ItemAffixState.ATTRIBUTE, "critical_chance", "0.05");
			player.getSlot("bag").add(item);

			final StendhalCharacterItemDAO dao = new StendhalCharacterItemDAO();
			dao.replaceInventorySnapshot(transaction, player,
					new Timestamp(System.currentTimeMillis()));

			final ResultSet result = transaction.query(
					"SELECT slot_name, itemid, item_name, item_class, item_subclass,"
					+ " quantity, rarity_id, upgrade_level, max_upgrade_level,"
					+ " atk, ratk, def, damage_min, damage_max, accuracy_bonus,"
					+ " attack_rate, item_value, lifesteal"
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
				assertEquals(14, result.getInt("atk"));
				assertEquals(6, result.getInt("ratk"));
				assertEquals(5, result.getInt("def"));
				assertEquals(10, result.getInt("damage_min"));
				assertEquals(18, result.getInt("damage_max"));
				assertEquals(0.15, result.getDouble("accuracy_bonus"), 0.0001);
				assertEquals(4, result.getInt("attack_rate"));
				assertEquals(500, result.getInt("item_value"));
				assertEquals(0.10, result.getDouble("lifesteal"), 0.0001);
				assertFalse(result.next());
			} finally {
				result.close();
			}

			final ResultSet rarityModifier = transaction.query(
					"SELECT modifier_value FROM character_item_modifiers"
					+ " WHERE charname='inventory_snapshot_player'"
					+ " AND modifier_source='rarity' AND modifier_name='atk'", null);
			try {
				assertTrue(rarityModifier.next());
				assertEquals("1.20", rarityModifier.getString("modifier_value"));
			} finally {
				rarityModifier.close();
			}

			final ResultSet affix = transaction.query(
					"SELECT modifier_value FROM character_item_modifiers"
					+ " WHERE charname='inventory_snapshot_player'"
					+ " AND modifier_source='affix' AND modifier_name='critical_chance'", null);
			try {
				assertTrue(affix.next());
				assertEquals("0.05", affix.getString("modifier_value"));
			} finally {
				affix.close();
			}

			player.getSlot("bag").clear();
			dao.replaceInventorySnapshot(transaction, player,
					new Timestamp(System.currentTimeMillis()));
			assertEquals(0, transaction.querySingleCellInt(
					"SELECT count(*) FROM character_items"
					+ " WHERE charname='inventory_snapshot_player'", null));
			assertEquals(0, transaction.querySingleCellInt(
					"SELECT count(*) FROM character_item_modifiers"
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
