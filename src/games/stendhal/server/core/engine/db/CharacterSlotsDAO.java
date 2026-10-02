package games.stendhal.server.core.engine.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import games.stendhal.common.constants.CharacterSlots;
import marauroa.server.db.DBTransaction;

/** Checks account capacity in the same transaction as character creation. */
public final class CharacterSlotsDAO {
	private CharacterSlotsDAO() {
	}

	public static void requireFreeSlot(final DBTransaction transaction, final String username)
			throws SQLException {
		final Map<String, Object> params = new HashMap<String, Object>();
		params.put("username", username);
		// Serialise requests for this account, including when it has no characters.
		try (ResultSet account = transaction.query(
				"SELECT id FROM account WHERE username='[username]' FOR UPDATE", params)) {
			if (!account.next()) {
				throw new SQLException("Account does not exist: " + username);
			}
			params.put("player_id", account.getInt("id"));
		}

		// A locking read sees committed rows even if the transaction previously
		// established a repeatable-read snapshot. Inactive characters occupy slots.
		try (ResultSet characters = transaction.query(
				"SELECT charname FROM characters WHERE player_id=[player_id] FOR UPDATE", params)) {
			int count = 0;
			while (characters.next()) {
				if (++count >= CharacterSlots.MAX_CHARACTERS) {
					throw new LimitReachedException();
				}
			}
		}
	}

	public static final class LimitReachedException extends SQLException {
		private static final long serialVersionUID = 1L;

		public LimitReachedException() {
			super("Na jednym koncie może być maksymalnie "
					+ CharacterSlots.MAX_CHARACTERS + " postaci.");
		}
	}
}
