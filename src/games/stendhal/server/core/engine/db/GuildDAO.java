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
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import marauroa.server.db.DBTransaction;

/**
 * Database access for guild management operations.
 */
public class GuildDAO {

	/**
	 * Guild member projection used by service-level validations.
	 */
	public static final class GuildMemberData {
		private final int guildId;
		private final String role;

		public GuildMemberData(final int guildId, final String role) {
			this.guildId = guildId;
			this.role = role;
		}

		public int getGuildId() {
			return guildId;
		}

		public String getRole() {
			return role;
		}
	}

	public boolean guildNameExists(final DBTransaction transaction, final String guildName) throws SQLException {
		return existsByColumn(transaction, "name", guildName);
	}

	public boolean guildTagExists(final DBTransaction transaction, final String guildTag) throws SQLException {
		return existsByColumn(transaction, "tag", guildTag);
	}

	private boolean existsByColumn(final DBTransaction transaction, final String column, final String value)
			throws SQLException {
		final String query = "SELECT id FROM guilds WHERE LOWER(" + column + ") = LOWER('[value]') LIMIT 1";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("value", value);
		ResultSet resultSet = transaction.query(query, params);
		try {
			return resultSet.next();
		} finally {
			resultSet.close();
		}
	}

	public int createGuild(final DBTransaction transaction, final String name, final String tag,
			final String description, final int leaderPlayerId) throws SQLException {
		final String query = "INSERT INTO guilds(name, tag, description, leader_player_id, created_at, updated_at) "
				+ "VALUES ('[name]', '[tag]', [description], [leader_player_id], CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("name", name);
		params.put("tag", tag);
		params.put("description", description);
		params.put("leader_player_id", Integer.valueOf(leaderPlayerId));
		transaction.execute(query, params);
		return transaction.getLastInsertId("guilds", "id");
	}

	public void addMember(final DBTransaction transaction, final int guildId, final int playerId, final String role)
			throws SQLException {
		final String query = "INSERT INTO guild_members(guild_id, player_id, role, joined_at) "
				+ "VALUES ([guild_id], [player_id], '[role]', CURRENT_TIMESTAMP)";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("player_id", Integer.valueOf(playerId));
		params.put("role", role);
		transaction.execute(query, params);
	}

	public GuildMemberData loadMembership(final DBTransaction transaction, final int playerId)
			throws SQLException {
		final String query = "SELECT guild_id, role FROM guild_members WHERE player_id = [player_id] LIMIT 1";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("player_id", Integer.valueOf(playerId));
		ResultSet resultSet = transaction.query(query, params);
		try {
			if (!resultSet.next()) {
				return null;
			}
			return new GuildMemberData(resultSet.getInt("guild_id"), resultSet.getString("role"));
		} finally {
			resultSet.close();
		}
	}

	public boolean isGuildMember(final DBTransaction transaction, final int guildId, final int playerId)
			throws SQLException {
		final String query = "SELECT player_id FROM guild_members WHERE guild_id = [guild_id] AND player_id = [player_id] LIMIT 1";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("player_id", Integer.valueOf(playerId));
		ResultSet resultSet = transaction.query(query, params);
		try {
			return resultSet.next();
		} finally {
			resultSet.close();
		}
	}

	public void createInvite(final DBTransaction transaction, final int guildId, final int invitedPlayerId,
			final int invitedByPlayerId, final Timestamp expiresAt) throws SQLException {
		final String query = "INSERT INTO guild_invites(guild_id, invited_player_id, invited_by_player_id, expires_at, created_at) "
				+ "VALUES ([guild_id], [invited_player_id], [invited_by_player_id], '[expires_at]', CURRENT_TIMESTAMP)";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("invited_player_id", Integer.valueOf(invitedPlayerId));
		params.put("invited_by_player_id", Integer.valueOf(invitedByPlayerId));
		params.put("expires_at", expiresAt);
		transaction.execute(query, params);
	}

	public boolean hasActiveInvite(final DBTransaction transaction, final int guildId, final int invitedPlayerId)
			throws SQLException {
		final String query = "SELECT id FROM guild_invites "
				+ "WHERE guild_id = [guild_id] AND invited_player_id = [invited_player_id] "
				+ "AND expires_at > CURRENT_TIMESTAMP LIMIT 1";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("invited_player_id", Integer.valueOf(invitedPlayerId));
		ResultSet resultSet = transaction.query(query, params);
		try {
			return resultSet.next();
		} finally {
			resultSet.close();
		}
	}

	public void deleteInvite(final DBTransaction transaction, final int guildId, final int invitedPlayerId)
			throws SQLException {
		final String query = "DELETE FROM guild_invites WHERE guild_id = [guild_id] AND invited_player_id = [invited_player_id]";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("invited_player_id", Integer.valueOf(invitedPlayerId));
		transaction.execute(query, params);
	}

	public void removeMember(final DBTransaction transaction, final int guildId, final int playerId) throws SQLException {
		final String query = "DELETE FROM guild_members WHERE guild_id = [guild_id] AND player_id = [player_id]";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("player_id", Integer.valueOf(playerId));
		transaction.execute(query, params);
	}

	public void updateGuildLeader(final DBTransaction transaction, final int guildId, final int newLeaderPlayerId)
			throws SQLException {
		final String query = "UPDATE guilds SET leader_player_id = [new_leader_player_id], updated_at = CURRENT_TIMESTAMP "
				+ "WHERE id = [guild_id]";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("new_leader_player_id", Integer.valueOf(newLeaderPlayerId));
		params.put("guild_id", Integer.valueOf(guildId));
		transaction.execute(query, params);
	}

	public void updateMemberRole(final DBTransaction transaction, final int guildId, final int playerId,
			final String role) throws SQLException {
		final String query = "UPDATE guild_members SET role='[role]' WHERE guild_id = [guild_id] AND player_id = [player_id]";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("role", role);
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("player_id", Integer.valueOf(playerId));
		transaction.execute(query, params);
	}

	public void updateDescription(final DBTransaction transaction, final int guildId, final String description)
			throws SQLException {
		final String query = "UPDATE guilds SET description = [description], updated_at = CURRENT_TIMESTAMP "
				+ "WHERE id = [guild_id]";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("description", description);
		params.put("guild_id", Integer.valueOf(guildId));
		transaction.execute(query, params);
	}

	public void logEvent(final DBTransaction transaction, final int guildId, final Integer actorPlayerId,
			final String eventType, final String payloadJson) throws SQLException {
		final String query = "INSERT INTO guild_logs(guild_id, actor_player_id, event_type, payload_json, created_at) "
				+ "VALUES ([guild_id], [actor_player_id], '[event_type]', [payload_json], CURRENT_TIMESTAMP)";
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("guild_id", Integer.valueOf(guildId));
		params.put("actor_player_id", actorPlayerId);
		params.put("event_type", eventType);
		params.put("payload_json", payloadJson);
		transaction.execute(query, params);
	}
}
