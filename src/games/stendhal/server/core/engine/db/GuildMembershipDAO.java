/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
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
import java.util.HashMap;
import java.util.Map;

import marauroa.server.db.DBTransaction;

/**
 * Database access for player guild membership and guild display metadata.
 */
public class GuildMembershipDAO {

	/**
	 * Lightweight guild membership projection for runtime cache.
	 */
	public static final class GuildMembershipData {
		private final int guildId;
		private final String guildName;
		private final String guildTag;

		public GuildMembershipData(final int guildId, final String guildName, final String guildTag) {
			this.guildId = guildId;
			this.guildName = guildName;
			this.guildTag = guildTag;
		}

		public int getGuildId() {
			return guildId;
		}

		public String getGuildName() {
			return guildName;
		}

		public String getGuildTag() {
			return guildTag;
		}
	}

	/**
	 * Loads guild membership for a character using DB as single source of truth.
	 *
	 * @param transaction DB transaction
	 * @param charname character name
	 * @return membership data or null when player is not in a guild
	 * @throws SQLException in case of database error
	 */
	public GuildMembershipData loadByCharacterName(final DBTransaction transaction,
			final String charname) throws SQLException {
		final String query = "SELECT gm.guild_id, g.name, g.tag "
				+ "FROM guild_members gm "
				+ "JOIN guilds g ON g.id = gm.guild_id "
				+ "JOIN characters c ON c.player_id = gm.player_id "
				+ "WHERE c.charname='[charname]' "
				+ "ORDER BY gm.joined_at DESC LIMIT 1";

		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("charname", charname);

		ResultSet resultSet = transaction.query(query, params);
		try {
			if (!resultSet.next()) {
				return null;
			}
			return new GuildMembershipData(
					resultSet.getInt("guild_id"),
					resultSet.getString("name"),
					resultSet.getString("tag"));
		} finally {
			resultSet.close();
		}
	}
}
