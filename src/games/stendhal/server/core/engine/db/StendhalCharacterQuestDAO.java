/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.server.entity.player.Player;
import marauroa.server.db.DBTransaction;

/**
 * Maintains a relational snapshot of character quest state for website queries.
 */
public class StendhalCharacterQuestDAO {
	private static final String TABLE_NAME = "character_quests";
	private static volatile boolean schemaReady;

	private void ensureSchema(final DBTransaction transaction) throws SQLException {
		if (schemaReady) {
			return;
		}
		synchronized (StendhalCharacterQuestDAO.class) {
			if (schemaReady) {
				return;
			}
			transaction.execute("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ("
					+ "id INTEGER AUTO_INCREMENT NOT NULL, "
					+ "charname VARCHAR(32) NOT NULL, "
					+ "quest_name VARCHAR(128) NOT NULL, "
					+ "quest_state TEXT, "
					+ "completed INTEGER NOT NULL, "
					+ "timedate TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
					+ "PRIMARY KEY(id));", null);
			transaction.execute("CREATE INDEX IF NOT EXISTS i_character_quests_charname "
					+ "ON " + TABLE_NAME + "(charname)", null);
			transaction.execute("CREATE INDEX IF NOT EXISTS i_character_quests_quest_name "
					+ "ON " + TABLE_NAME + "(quest_name)", null);
			schemaReady = true;
		}
	}

	/**
	 * Replaces the quest snapshot for one character.
	 *
	 * @param transaction database transaction
	 * @param player player being stored
	 * @param timestamp character store timestamp
	 * @throws SQLException in case of a database error
	 */
	public void replaceQuestSnapshot(final DBTransaction transaction,
			final Player player, final Timestamp timestamp) throws SQLException {
		ensureSchema(transaction);

		final Map<String, Object> deleteParams = new HashMap<String, Object>();
		deleteParams.put("charname", player.getName());
		transaction.execute("DELETE FROM " + TABLE_NAME
				+ " WHERE charname='[charname]'", deleteParams);

		if (!player.hasSlot("!quests")) {
			return;
		}
		for (final String quest : player.getQuests()) {
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("charname", player.getName());
			params.put("quest_name", quest);
			params.put("quest_state", player.getQuest(quest));
			params.put("completed", Integer.valueOf(player.isQuestCompleted(quest) ? 1 : 0));
			params.put("timedate", timestamp);
			transaction.execute("INSERT INTO " + TABLE_NAME
					+ " (charname, quest_name, quest_state, completed, timedate)"
					+ " VALUES ('[charname]', '[quest_name]', '[quest_state]',"
					+ " [completed], '[timedate]')", params);
		}
	}
}
