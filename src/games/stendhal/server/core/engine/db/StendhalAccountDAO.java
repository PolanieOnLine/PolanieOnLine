package games.stendhal.server.core.engine.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import marauroa.common.net.message.MessageS2CLoginNACK;
import marauroa.server.db.DBTransaction;
import marauroa.server.game.container.SecuredLoginInfo;
import marauroa.server.game.db.AccountDAO;

/** Enforces a short lifetime for website-issued, single-use game login seeds. */
public class StendhalAccountDAO extends AccountDAO {
	private static final long STEAM_SEED_LIFETIME_MILLIS = 180000L;

	@Override
	public boolean verify(final DBTransaction transaction, final SecuredLoginInfo info) throws SQLException {
		if (info.seed != null && info.seed.startsWith("S_")) {
			final Map<String, Object> params = new HashMap<String, Object>();
			params.put("seed", info.seed);
			final ResultSet result = transaction.query(
					"SELECT timedate FROM loginseed WHERE seed='[seed]' AND used=0 FOR UPDATE", params);
			try {
				if (!result.next() || !isFresh(result.getTimestamp("timedate"), System.currentTimeMillis())) {
					info.reason = MessageS2CLoginNACK.Reasons.SEED_WRONG;
					return false;
				}
			} finally {
				result.close();
			}
		}

		return super.verify(transaction, info);
	}

	static boolean isFresh(final Timestamp issuedAt, final long now) {
		return issuedAt != null && issuedAt.getTime() <= now + 30000L
				&& now - issuedAt.getTime() <= STEAM_SEED_LIFETIME_MILLIS;
	}
}
