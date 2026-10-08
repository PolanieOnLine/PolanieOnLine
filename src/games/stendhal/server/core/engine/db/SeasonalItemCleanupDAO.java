/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.engine.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;

import marauroa.server.db.DBTransaction;

/** Durable invalidation of items held by characters who are offline at event end. */
public final class SeasonalItemCleanupDAO {
	public long load(final DBTransaction transaction) throws SQLException {
		try (ResultSet result = transaction.query(
				"SELECT generation FROM seasonal_item_cleanup WHERE event_key='minetown'", null)) {
			return result.next() ? result.getLong(1) : 0;
		}
	}

	public long advance(final DBTransaction transaction) throws SQLException {
		if (transaction.execute("UPDATE seasonal_item_cleanup SET generation=generation+1"
				+ " WHERE event_key='minetown'", null) == 0) {
			transaction.execute("INSERT INTO seasonal_item_cleanup (event_key, generation)"
					+ " VALUES ('minetown', 1)", Collections.emptyMap());
		}
		return load(transaction);
	}
}
