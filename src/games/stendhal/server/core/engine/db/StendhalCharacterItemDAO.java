/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import games.stendhal.server.core.rule.rarity.ItemAffixState;
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
	private static final String MODIFIER_TABLE_NAME = "character_item_modifiers";
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
					+ "atk INTEGER, "
					+ "ratk INTEGER, "
					+ "def INTEGER, "
					+ "damage_min INTEGER, "
					+ "damage_max INTEGER, "
					+ "accuracy_bonus FLOAT, "
					+ "attack_rate INTEGER, "
					+ "item_value INTEGER, "
					+ "lifesteal FLOAT, "
					+ "timedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
					+ "PRIMARY KEY(id));", null);
			addColumnIfMissing(transaction, "atk", "INTEGER");
			addColumnIfMissing(transaction, "ratk", "INTEGER");
			addColumnIfMissing(transaction, "def", "INTEGER");
			addColumnIfMissing(transaction, "damage_min", "INTEGER");
			addColumnIfMissing(transaction, "damage_max", "INTEGER");
			addColumnIfMissing(transaction, "accuracy_bonus", "FLOAT");
			addColumnIfMissing(transaction, "attack_rate", "INTEGER");
			addColumnIfMissing(transaction, "item_value", "INTEGER");
			addColumnIfMissing(transaction, "lifesteal", "FLOAT");

			transaction.execute("CREATE INDEX IF NOT EXISTS i_character_items_charname "
					+ "ON " + TABLE_NAME + "(charname)", null);

			transaction.execute("CREATE TABLE IF NOT EXISTS " + MODIFIER_TABLE_NAME + " ("
					+ "id INTEGER AUTO_INCREMENT NOT NULL, "
					+ "charname VARCHAR(32) NOT NULL, "
					+ "slot_path VARCHAR(512) NOT NULL, "
					+ "itemid INTEGER, "
					+ "modifier_source VARCHAR(16) NOT NULL, "
					+ "modifier_name VARCHAR(64) NOT NULL, "
					+ "modifier_value VARCHAR(128) NOT NULL, "
					+ "timedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
					+ "PRIMARY KEY(id));", null);
			transaction.execute("CREATE INDEX IF NOT EXISTS i_character_item_modifiers_charname "
					+ "ON " + MODIFIER_TABLE_NAME + "(charname)", null);
			transaction.execute("CREATE INDEX IF NOT EXISTS i_character_item_modifiers_item "
					+ "ON " + MODIFIER_TABLE_NAME + "(charname, slot_path)", null);
			schemaReady = true;
		}
	}

	private void addColumnIfMissing(final DBTransaction transaction,
			final String columnName, final String columnType) throws SQLException {
		if (!transaction.doesColumnExist(TABLE_NAME, columnName)) {
			transaction.execute("ALTER TABLE " + TABLE_NAME + " ADD COLUMN ("
					+ columnName + " " + columnType + ")", null);
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
		transaction.execute("DELETE FROM " + MODIFIER_TABLE_NAME
				+ " WHERE charname='[charname]'", deleteParams);
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
		final int itemId = item.has(StendhalItemDAO.ATTR_ITEM_LOGID)
				? item.getInt(StendhalItemDAO.ATTR_ITEM_LOGID) : 0;
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("charname", charname);
		params.put("slot_path", slotPath);
		params.put("slot_name", slotName);
		params.put("itemid", Integer.valueOf(itemId));
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
		params.put("atk", Integer.valueOf(item.has("atk") ? item.getAttack() : 0));
		params.put("ratk", Integer.valueOf(item.has("ratk") ? item.getRangedAttack() : 0));
		params.put("def", Integer.valueOf(item.has("def") ? item.getDefense() : 0));
		params.put("damage_min", Integer.valueOf(isWeapon(item) ? item.getDamageMin() : 0));
		params.put("damage_max", Integer.valueOf(isWeapon(item) ? item.getDamageMax() : 0));
		params.put("accuracy_bonus", Double.valueOf(item.has("accuracy_bonus") ? item.getDouble("accuracy_bonus") : 0.0));
		params.put("attack_rate", Integer.valueOf(isWeapon(item) ? item.getAttackRate() : 0));
		params.put("item_value", Integer.valueOf(item.has(Item.VALUE) ? item.getInt(Item.VALUE) : 0));
		params.put("lifesteal", Double.valueOf(item.has("lifesteal") ? item.getDouble("lifesteal") : 0.0));
		params.put("timedate", timestamp);

		transaction.execute("INSERT INTO " + TABLE_NAME
				+ " (charname, slot_path, slot_name, itemid, item_name, item_class,"
				+ " item_subclass, quantity, rarity_id, upgrade_level, max_upgrade_level,"
				+ " atk, ratk, def, damage_min, damage_max, accuracy_bonus, attack_rate,"
				+ " item_value, lifesteal, timedate)"
				+ " VALUES ('[charname]', '[slot_path]', '[slot_name]', [itemid],"
				+ " '[item_name]', '[item_class]', '[item_subclass]', [quantity],"
				+ " '[rarity_id]', [upgrade_level], [max_upgrade_level], [atk], [ratk], [def],"
				+ " [damage_min], [damage_max], '[accuracy_bonus]', [attack_rate],"
				+ " [item_value], '[lifesteal]', '[timedate]')",
				params);

		saveModifiers(transaction, charname, slotPath, itemId, "rarity",
				item.hasMap(Item.RARITY_MODIFIERS)
						? item.getMap(Item.RARITY_MODIFIERS) : null,
				timestamp);
		saveModifiers(transaction, charname, slotPath, itemId, "affix",
				ItemAffixState.getValues(item), timestamp);
	}

	private boolean isWeapon(final Item item) {
		return item.has("atk") || item.has("ratk")
				|| item.has("damage_min") || item.has("damage_max");
	}


	private void saveModifiers(final DBTransaction transaction, final String charname,
			final String slotPath, final int itemId, final String source,
			final Map<String, String> modifiers, final Timestamp timestamp)
			throws SQLException {
		if (modifiers == null || modifiers.isEmpty()) {
			return;
		}
		for (final Entry<String, String> entry : modifiers.entrySet()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("charname", charname);
			params.put("slot_path", slotPath);
			params.put("itemid", Integer.valueOf(itemId));
			params.put("modifier_source", source);
			params.put("modifier_name", entry.getKey());
			params.put("modifier_value", entry.getValue());
			params.put("timedate", timestamp);
			transaction.execute("INSERT INTO " + MODIFIER_TABLE_NAME
					+ " (charname, slot_path, itemid, modifier_source, modifier_name,"
					+ " modifier_value, timedate)"
					+ " VALUES ('[charname]', '[slot_path]', [itemid], '[modifier_source]',"
					+ " '[modifier_name]', '[modifier_value]', '[timedate]')", params);
		}
	}
}
