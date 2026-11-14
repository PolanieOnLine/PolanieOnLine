package games.stendhal.server.core.engine.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.server.entity.player.Player;
import marauroa.server.db.DBTransaction;

/** Account claim records are committed in the character's inventory transaction. */
public final class AdventCalendarDAO {

	public static final String QUEST_SLOT = "advent_calendar_claims";
	public static final int ALL_DAYS = (1 << 24) - 1;

	public Account accountFor(final DBTransaction transaction, final String character) throws SQLException {
		final Map<String, Object> params = new HashMap<>();
		params.put("character", character);
		try (ResultSet result = transaction.query("SELECT a.id, a.username FROM account a"
				+ " INNER JOIN characters c ON c.player_id=a.id WHERE c.charname='[character]'", params)) {
			if (!result.next()) {
				throw new SQLException("No account for advent player");
			}
			return new Account(result.getInt("id"), result.getString("username"));
		}
	}

	public int claimedMask(final DBTransaction transaction, final int accountId, final int year) throws SQLException {
		final Map<String, Object> params = parameters(accountId, year);
		int mask = 0;
		try (ResultSet result = transaction.query("SELECT reward_day FROM advent_calendar_claims"
				+ " WHERE account_id=[account_id] AND reward_year=[reward_year]", params)) {
			while (result.next()) {
				final int day = result.getInt("reward_day");
				if (day >= 1 && day <= 24) {
					mask |= 1 << (day - 1);
				}
			}
		}
		return mask;
	}

	public void persistClaims(final DBTransaction transaction, final String username, final Player player)
			throws SQLException {
		if (!player.hasQuest(QUEST_SLOT)) {
			return;
		}
		final String[] fields = player.getQuest(QUEST_SLOT).split(";", -1);
		if (fields.length != 2) {
			throw new SQLException("Invalid advent claim state");
		}
		final int year;
		final int mask;
		try {
			year = Integer.parseInt(fields[0]);
			mask = Integer.parseInt(fields[1]);
		} catch (final NumberFormatException exception) {
			throw new SQLException("Invalid advent claim state", exception);
		}
		if (year < 2026 || year > 9999 || mask < 0 || (mask & ~ALL_DAYS) != 0) {
			throw new SQLException("Invalid advent claim state");
		}
		final Map<String, Object> accountParams = new HashMap<>();
		accountParams.put("username", username);
		final int accountId;
		try (ResultSet account = transaction.query(
				"SELECT id FROM account WHERE username='[username]' FOR UPDATE", accountParams)) {
			if (!account.next()) {
				throw new SQLException("Missing advent account");
			}
			accountId = account.getInt("id");
		}
		final int missing = mask & ~claimedMask(transaction, accountId, year);
		final Map<String, Object> params = parameters(accountId, year);
		params.put("character", player.getName());
		for (int day = 1; day <= 24; day++) {
			if ((missing & (1 << (day - 1))) != 0) {
				params.put("reward_day", day);
				transaction.execute("INSERT INTO advent_calendar_claims"
						+ " (account_id, reward_year, reward_day, charname)"
						+ " VALUES ([account_id], [reward_year], [reward_day], '[character]')", params);
			}
		}
	}

	private Map<String, Object> parameters(final int accountId, final int year) {
		final Map<String, Object> params = new HashMap<>();
		params.put("account_id", accountId);
		params.put("reward_year", year);
		return params;
	}

	public static final class Account {
		public final int id;
		public final String username;

		public Account(final int id, final String username) {
			this.id = id;
			this.username = username;
		}
	}
}
