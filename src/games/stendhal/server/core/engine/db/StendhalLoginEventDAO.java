package games.stendhal.server.core.engine.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import games.stendhal.server.util.TimeUtil;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.db.AccountDAO;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.LoginEventDAO;

/** Displays login dates in Warsaw time without relying on the JDBC time zone. */
public class StendhalLoginEventDAO extends LoginEventDAO {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
			.withZone(TimeUtil.SERVER_TIME_ZONE);

	@Override
	public List<String> getLoginEvents(final DBTransaction transaction, final String username, final int events)
			throws SQLException {
		final int playerId = DAORegister.get().get(AccountDAO.class).getDatabasePlayerId(transaction, username);
		if (playerId == -1) {
			throw new SQLException("Unable to find player(" + username + ")");
		}
		final boolean epoch = usesDatabaseEpoch(transaction);
		final Map<String, Object> params = new HashMap<>();
		params.put("player_id", playerId);
		params.put("events", events);
		final String query = "SELECT address, result, " + timestampColumn(epoch)
				+ " FROM loginEvent WHERE player_id=[player_id] ORDER BY timedate DESC LIMIT [events]";
		final List<String> logins = new ArrayList<>();
		try (ResultSet result = transaction.query(query, params)) {
			while (result.next()) {
				logins.add(new LoginEvent(result.getString("address"), formatDate(result, epoch),
						result.getBoolean("result")).toString());
			}
		}
		return logins;
	}

	@Override
	public LoginEvent getLastSuccessfulLoginEvent(final DBTransaction transaction, final int playerId,
			final String service) throws SQLException {
		final boolean epoch = usesDatabaseEpoch(transaction);
		final Map<String, Object> params = new HashMap<>();
		params.put("player_id", playerId);
		params.put("service", service);
		final String query = "SELECT id, player_id, service, address, result, " + timestampColumn(epoch)
				+ " FROM loginEvent WHERE player_id=[player_id] AND result=1"
				+ (service == null ? "" : " AND service='[service]'")
				+ " ORDER BY timedate DESC LIMIT 2";
		try (ResultSet result = transaction.query(query, params)) {
			// The first successful event belongs to the current login.
			if (!result.next() || !result.next()) {
				return null;
			}
			return new LoginEvent(result.getLong("id"), result.getLong("player_id"), result.getString("service"),
					result.getString("address"), formatDate(result, epoch), result.getBoolean("result"));
		}
	}

	private boolean usesDatabaseEpoch(final DBTransaction transaction) throws SQLException {
		try (PreparedStatement statement = transaction.prepareStatement("SELECT 1", null)) {
			final String database = statement.getConnection().getMetaData().getDatabaseProductName()
					.toLowerCase(Locale.ROOT);
			return database.contains("mysql") || database.contains("mariadb");
		}
	}

	private String timestampColumn(final boolean epoch) {
		// MySQL returns the actual instant even if serverTimezone in the JDBC URL
		// disagrees with the SQL session time zone. H2 has no remote session zone.
		return epoch ? "UNIX_TIMESTAMP(timedate) AS login_time" : "timedate";
	}

	private String formatDate(final ResultSet result, final boolean epoch) throws SQLException {
		final Instant instant = epoch ? Instant.ofEpochSecond(result.getLong("login_time"))
				: result.getTimestamp("timedate").toInstant();
		return DATE_FORMAT.format(instant);
	}
}
