package games.stendhal.server.core.engine.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalPlayerDatabase;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.db.DAORegister;
import marauroa.server.game.db.LoginEventDAO;

public class StendhalLoginEventDAOTest {
	private TimeZone previous;
	private Connection connection;
	private DBTransaction transaction;
	private LoginEventDAO dao;
	private Map<Class<?>, Object> previousDaos;

	@Before
	public void setUp() throws Exception {
		previous = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		connection = DriverManager.getConnection("jdbc:h2:mem:login_clock", "sa", "");
		connection.createStatement().execute("CREATE TABLE account(id INT, username VARCHAR(64))");
		connection.createStatement().execute("INSERT INTO account VALUES(1, 'clock_test')");
		connection.createStatement().execute("CREATE TABLE loginEvent(id INT, player_id INT, service VARCHAR(64),"
				+ " address VARCHAR(64), timedate TIMESTAMP, result INT)");
		transaction = new TestTransaction(connection);
		previousDaos = new HashMap<>(registeredDaos());
		final Method register = StendhalPlayerDatabase.class.getDeclaredMethod("registerStendhalDAOs");
		register.setAccessible(true);
		register.invoke(new StendhalPlayerDatabase());
		dao = DAORegister.get().get(LoginEventDAO.class);
	}

	@After
	public void tearDown() throws Exception {
		if (previousDaos != null) {
			registeredDaos().clear();
			registeredDaos().putAll(previousDaos);
		}
		if (connection != null) {
			connection.close();
		}
		TimeZone.setDefault(previous);
	}

	@Test
	public void winterLoginIsDisplayedInWarsawTime() throws Exception {
		insert(1, "2026-11-08 19:52:00", 1, "game");
		assertEquals("logowanie zakończyło się sukcesem dnia 2026-11-08 20:52 czasu serwera z 127.0.0.1",
				dao.getLoginEvents(transaction, "clock_test", 1).get(0));
	}

	@Test
	public void summerLoginUsesSummerTimeAndKeepsFailureResult() throws Exception {
		insert(1, "2026-07-08 19:52:00", 0, "game");
		assertEquals("logowanie zakończyło się NIEPOWODZENIEM dnia 2026-07-08 21:52 czasu serwera z 127.0.0.1",
				dao.getLoginEvents(transaction, "clock_test", 1).get(0));
	}

	@Test
	public void previousSuccessfulLoginSkipsCurrentAndFailedAttempts() throws Exception {
		insert(1, "2026-11-08 19:52:00", 1, "game");
		insert(2, "2026-11-08 19:53:00", 0, "game");
		insert(3, "2026-11-08 19:54:00", 1, "game");
		insert(4, "2026-11-08 19:55:00", 1, "web");
		assertEquals("2026-11-08 20:52", dao.getLastSuccessfulLoginEvent(transaction, 1, "game").date);
		assertEquals("2026-11-08 20:54", dao.getLastSuccessfulLoginEvent(transaction, 1, null).date);
	}

	@Test
	public void firstLoginHasNoPreviousSuccessfulLogin() throws Exception {
		insert(1, "2026-11-08 19:52:00", 1, "game");
		assertNull(dao.getLastSuccessfulLoginEvent(transaction, 1, null));
	}

	@Test
	public void mysqlDatesBypassDriverTimezoneConversion() throws Exception {
		insert(1, "2026-07-08 19:52:00", 1, "game");
		insert(2, "2026-11-08 19:52:00", 1, "game");
		transaction = new TestTransaction(connection, true);
		TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
		assertEquals("logowanie zakończyło się sukcesem dnia 2026-11-08 20:52 czasu serwera z 127.0.0.1",
				dao.getLoginEvents(transaction, "clock_test", 1).get(0));
		assertEquals("2026-07-08 21:52", dao.getLastSuccessfulLoginEvent(transaction, 1, null).date);
	}

	@SuppressWarnings("unchecked")
	private Map<Class<?>, Object> registeredDaos() throws Exception {
		final Field field = DAORegister.class.getDeclaredField("register");
		field.setAccessible(true);
		return (Map<Class<?>, Object>) field.get(DAORegister.get());
	}

	private void insert(final int id, final String time, final int result, final String service) throws SQLException {
		try (PreparedStatement statement = connection.prepareStatement(
				"INSERT INTO loginEvent VALUES(?, 1, ?, '127.0.0.1', ?, ?)")) {
			statement.setInt(1, id);
			statement.setString(2, service);
			statement.setString(3, time);
			statement.setInt(4, result);
			statement.executeUpdate();
		}
	}

	private static final class TestTransaction extends DBTransaction {
		private final Connection connection;
		private final boolean mysql;

		TestTransaction(final Connection connection) {
			this(connection, false);
		}

		TestTransaction(final Connection connection, final boolean mysql) {
			super(null);
			this.connection = connection;
			this.mysql = mysql;
		}

		@Override
		public ResultSet query(final String sql, final Map<String, Object> params) throws SQLException {
			final String query = mysql ? sql.replace("UNIX_TIMESTAMP(timedate)",
					"DATEDIFF('SECOND', TIMESTAMP '1970-01-01 00:00:00', timedate)") : sql;
			final ResultSet result = connection.createStatement().executeQuery(subst(query, params));
			if (!mysql) {
				return result;
			}
			return (ResultSet) Proxy.newProxyInstance(getClass().getClassLoader(), new Class<?>[] {ResultSet.class},
					(proxy, method, args) -> {
						if (method.getName().equals("getTimestamp")) {
							throw new AssertionError("MySQL login dates must not depend on JDBC timestamp conversion");
						}
						return method.invoke(result, args);
					});
		}

		@Override
		public PreparedStatement prepareStatement(final String sql, final Map<String, Object> params) throws SQLException {
			final PreparedStatement statement = connection.prepareStatement(subst(sql, params));
			if (!mysql) {
				return statement;
			}
			final DatabaseMetaData metadata = (DatabaseMetaData) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[] {DatabaseMetaData.class}, (proxy, method, args) -> {
						if (method.getName().equals("getDatabaseProductName")) {
							return "MySQL";
						}
						return method.invoke(connection.getMetaData(), args);
					});
			final Connection mysqlConnection = (Connection) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[] {Connection.class}, (proxy, method, args) -> {
						if (method.getName().equals("getMetaData")) {
							return metadata;
						}
						return method.invoke(connection, args);
					});
			return (PreparedStatement) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[] {PreparedStatement.class}, (proxy, method, args) -> {
						if (method.getName().equals("getConnection")) {
							return mysqlConnection;
						}
						return method.invoke(statement, args);
					});
		}
	}
}
