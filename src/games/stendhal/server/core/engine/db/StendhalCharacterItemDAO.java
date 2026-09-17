/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.Slots;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.db.DBTransaction;

/**
 * Maintains a relational snapshot of character inventory for website queries.
 */
public class StendhalCharacterItemDAO {
	private static final String TABLE_NAME = "character_items";
	private static volatile boolean schemaReady;

	private void ensureSchema(final DBTransaction transaction) throws SQLException {
		if (schemaReady) {
			return;
		}
		synchronized (StendhalCharacterItemDAO.class) {
			if (schemaReady) {
				return;
			}
			transaction.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
					+ "id INTEGER AUTO_INCREMENT NOT NULL, "
					+ "charname VARCHAR(32) NOT NULL, "
					+ "slot_path VARCHAR(512) NOT NULL, "
					+ "slot_name VARCHAR(64) NOT NULL, "
					+ "itemid INTEGER, "
					+ "item_name VARCHAR(64) NOT NULL, "
					+ "item_class VARCHAR(64), "
					+ "item_subclass VARCHAR(64), "
					+ "quantity INTEGER NOT NULL, "
					+ "rarity_id VARCHAR(16) NOT NULL, "
					+ "upgrade_level INTEGER NOT NULL, "
					+ "max_upgrade_level INTEGER NOT NULL, "
					+ "timedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
					+ "PRIMARY KEY(id))", null);
			transaction.execute("CREATE INDEX IF NOT EXISTS i_character_items_charname "
					+ "ON " + TABLE_NAME + "(charname)", null);
			schemaReady = true;
		}
	}

	/**
	 * Replaces the inventory snapshot for one character.
	 *
	 * @param transaction database transaction
	 * @param player player being stored
	 * @param timestamp character store timestamp
	 * @throws SQLException in case of a database error
	 */
	public void replaceInventorySnapshot(final DBTransaction transaction,
			final Player player, final Timestamp timestamp) throws SQLException {
		ensureSchema(transaction);

		final Map<String, Object> deleteParams = new HashMap<String, Object>();
		deleteParams.put("charname", player.getName());
		transaction.execute("DELETE FROM " + TABLE_NAME
				+ " WHERE charname='[charname]'", deleteParams);

		for (final RPSlot slot : player.slots(Slots.CARRYING)) {
			saveSlot(transaction, player.getName(), slot, slot.getName(), timestamp);
		}
	}

	private void saveSlot(final DBTransaction transaction, final String charname,
			final RPSlot slot, final String parentPath, final Timestamp timestamp)
			throws SQLException {
		int position = 0;
		for (final RPObject object : slot) {
			final String objectPath = parentPath + "[" + position + "]";
			if (object instanceof Item) {
				saveItem(transaction, charname, slot.getName(), objectPath,
						(Item) object, timestamp);
			}
			for (final RPSlot childSlot : object.slots()) {
				saveSlot(transaction, charname, childSlot,
						objectPath + "/" + childSlot.getName(), timestamp);
			}
			position++;
		}
	}

	private void saveItem(final DBTransaction transaction, final String charname,
			final String slotName, final String slotPath, final Item item,
			final Timestamp timestamp) throws SQLException {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("charname", charname);
		params.put("slot_path", slotPath);
		params.put("slot_name", slotName);
		params.put("itemid", item.has(StendhalItemDAO.ATTR_ITEM_LOGID)
				? Integer.valueOf(item.getInt(StendhalItemDAO.ATTR_ITEM_LOGID))
				: Integer.valueOf(0));
		params.put("item_name", item.getName());
		params.put("item_class", item.getItemClass());
		params.put("item_subclass", item.getItemSubclass());
		params.put("quantity", Integer.valueOf(item.has("quantity")
				? item.getInt("quantity") : 1));
		params.put("rarity_id", item.has(Item.RARITY_ID)
				? item.get(Item.RARITY_ID) : "common");
		params.put("upgrade_level", Integer.valueOf(item.has(Item.UPGRADE_LEVEL_ATTRIBUTE)
				? item.getInt(Item.UPGRADE_LEVEL_ATTRIBUTE) : 0));
		params.put("max_upgrade_level", Integer.valueOf(item.has(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE)
				? item.getInt(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE) : 0));
		params.put("timedate", timestamp);

		transaction.execute("INSERT INTO " + TABLE_NAME
				+ " (charname, slot_path, slot_name, itemid, item_name, item_class,"
				+ " item_subclass, quantity, rarity_id, upgrade_level, max_upgrade_level, timedate)"
				+ " VALUES ('[charname]', '[slot_path]', '[slot_name]', [itemid],"
				+ " '[item_name]', '[item_class]', '[item_subclass]', [quantity],"
				+ " '[rarity_id]', [upgrade_level], [max_upgrade_level], '[timedate]')",
				params);
	}
}
