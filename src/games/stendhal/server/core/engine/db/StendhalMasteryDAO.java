/***************************************************************************
 *                    (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import marauroa.server.db.DBTransaction;

/**
 * DAO for character mastery progression.
 */
public class StendhalMasteryDAO {

	private static final Logger logger = Logger.getLogger(StendhalMasteryDAO.class);

	public static final class CharacterMastery {
		private final int masteryLevel;
		private final long masteryXP;

		public CharacterMastery(final int masteryLevel, final long masteryXP) {
			this.masteryLevel = masteryLevel;
			this.masteryXP = masteryXP;
		}

		public int getMasteryLevel() {
			return masteryLevel;
		}

		public long getMasteryXP() {
			return masteryXP;
		}
	}

	public static final class CharacterMasteryEntry {
		private final String charname;
		private final int masteryLevel;
		private final long masteryXP;

		public CharacterMasteryEntry(final String charname, final int masteryLevel, final long masteryXP) {
			this.charname = charname;
			this.masteryLevel = masteryLevel;
			this.masteryXP = masteryXP;
		}

		public String getCharname() {
			return charname;
		}

		public int getMasteryLevel() {
			return masteryLevel;
		}

		public long getMasteryXP() {
			return masteryXP;
		}
	}

	/**
	 * Loads mastery data for a character.
	 *
	 * @param transaction db transaction
	 * @param charname character name
	 * @return mastery data or {@code null} when not found
	 * @throws SQLException on database error
	 */
	public CharacterMastery loadByCharname(final DBTransaction transaction, final String charname) throws SQLException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("charname", charname);
		ResultSet resultSet = transaction.query(
				"SELECT mastery_level, mastery_xp FROM character_mastery WHERE charname='[charname]'",
				params);
		try {
			if (!resultSet.next()) {
				return null;
			}
			return new CharacterMastery(resultSet.getInt("mastery_level"), resultSet.getLong("mastery_xp"));
		} finally {
			resultSet.close();
		}
	}

	/**
	 * Inserts or updates mastery data for a character.
	 *
	 * @param transaction db transaction
	 * @param charname character name
	 * @param masteryLevel mastery level to store
	 * @param masteryXP mastery xp to store
	 * @throws SQLException on database error
	 */
	public void upsert(final DBTransaction transaction, final String charname,
			final int masteryLevel, final long masteryXP) throws SQLException {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("charname", charname);
		params.put("mastery_level", Integer.toString(masteryLevel));
		params.put("mastery_xp", Long.toString(masteryXP));

		int count = transaction.execute(
				"UPDATE character_mastery"
				+ " SET mastery_level='[mastery_level]', mastery_xp='[mastery_xp]', updated_at=CURRENT_TIMESTAMP"
				+ " WHERE charname='[charname]'",
				params);
		if (count == 0) {
			transaction.execute(
					"INSERT INTO character_mastery(charname, mastery_level, mastery_xp, updated_at)"
					+ " VALUES('[charname]', '[mastery_level]', '[mastery_xp]', CURRENT_TIMESTAMP)",
					params);
		}
	}

	/**
	 * Returns top characters by mastery level and XP.
	 *
	 * @param transaction db transaction
	 * @param maxResults max number of rows
	 * @return list of top entries
	 * @throws SQLException on database error
	 */
	public List<CharacterMasteryEntry> topByLevel(final DBTransaction transaction, final int maxResults) throws SQLException {
		List<CharacterMasteryEntry> result = new ArrayList<CharacterMasteryEntry>();
		String sql = "SELECT charname, mastery_level, mastery_xp FROM character_mastery"
				+ " ORDER BY mastery_level DESC, mastery_xp DESC";
		if (maxResults > 0) {
			sql += " LIMIT " + maxResults;
		}
		ResultSet resultSet = transaction.query(sql, null);
		try {
			while (resultSet.next()) {
				result.add(new CharacterMasteryEntry(
						resultSet.getString("charname"),
						resultSet.getInt("mastery_level"),
						resultSet.getLong("mastery_xp")));
			}
		} catch (SQLException e) {
			logger.warn("Error reading mastery leaderboard", e);
			throw e;
		} finally {
			resultSet.close();
		}
		return result;
	}
}
