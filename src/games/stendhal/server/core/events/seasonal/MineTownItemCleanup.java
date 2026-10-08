/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.ItemLogger;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.db.SeasonalItemCleanupDAO;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import marauroa.server.db.DBTransaction;
import marauroa.server.db.TransactionPool;

/** Event end is remembered across restarts and subsequent event activations. */
public final class MineTownItemCleanup {
	private static final Logger LOGGER = Logger.getLogger(MineTownItemCleanup.class);
	static final String PLAYER_MARKER = "minetown_item_cleanup";
	private static volatile long generation;

	private MineTownItemCleanup() { }

	/** Called after world loading, including a restart with Mine Town disabled. */
	public static void initialize() throws SQLException {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			final long loaded = new SeasonalItemCleanupDAO().load(transaction);
			pool.commit(transaction);
			generation = loaded;
		} catch (final SQLException e) {
			pool.rollback(transaction);
			throw e;
		}
		if (!SeasonalEventService.get().isMineTownEnabled()) {
			finishEvent();
		}
	}

	/** Last fallible step of a successful event shutdown, before deleting anything. */
	public static void finishEvent() throws SQLException {
		final TransactionPool pool = TransactionPool.get();
		final DBTransaction transaction = pool.beginWork();
		try {
			final long next = new SeasonalItemCleanupDAO().advance(transaction);
			pool.commit(transaction);
			generation = next;
		} catch (final SQLException e) {
			pool.rollback(transaction);
			throw e;
		}
		// From here on the event is committed. An isolated cleanup failure must
		// not roll the event back after other players' items were already removed.
		try {
			cleanupWorld();
		} catch (final RuntimeException e) {
			LOGGER.error("Cannot finish Mine Town world cleanup; offline cleanup remains scheduled", e);
		}
	}

	public static int onLogin(final Player player) {
		return cleanupPlayer(player, generation, SeasonalEventService.get().isMineTownEnabled());
	}

	static int cleanupPlayer(final Player player, final long current, final boolean enabled) {
		long previous = 0;
		if (player.hasQuest(PLAYER_MARKER)) {
			try {
				previous = Long.parseLong(player.getQuest(PLAYER_MARKER));
			} catch (final NumberFormatException e) {
				LOGGER.warn("Invalid Mine Town item cleanup marker for " + player.getName());
			}
		}
		final int removed = !enabled || previous != current ? removeContents(player, player) : 0;
		player.setQuest(PLAYER_MARKER, Long.toString(current));
		if (removed > 0) {
			player.unlockTradeItemOffer();
			player.updateItemAtkDef();
		}
		return removed;
	}

	static boolean isEventItem(final RPObject object) {
		if (!object.has("type") || !"item".equals(object.get("type")) || !object.has("name")) {
			return false;
		}
		final String name = object.get("name");
		if ("straszna dynia".equals(name) || MineTownLanternSpawns.LANTERN_NAME.equals(name)) {
			return true;
		}
		// Before the stackable festival item existed it was a marked ordinary lantern.
		return "latarenka".equals(name) && ((object.has("itemdata")
				&& object.get("itemdata").matches("minetown_lantern_[0-9]{2}"))
				|| (object.has("infostring")
						&& object.get("infostring").matches("minetown_lantern_[0-9]{2}")));
	}

	static int removeContents(final RPObject owner, final RPEntity player) {
		int removed = 0;
		for (final RPSlot slot : owner.slots()) {
			final List<RPObject> contents = new ArrayList<RPObject>();
			for (final RPObject object : slot) { contents.add(object); }
			for (final RPObject object : contents) {
				if (isEventItem(object)) {
					new ItemLogger().destroy(player, slot, object, "Mine Town event ended");
					if (object instanceof Item) { ((Item) object).onUnequipped(); }
					slot.remove(object.getID());
					removed++;
				} else {
					removed += removeContents(object, player);
				}
			}
		}
		return removed;
	}

	private static void cleanupWorld() {
		for (final IRPZone rawZone : SingletonRepository.getRPWorld()) {
			final StendhalRPZone zone = (StendhalRPZone) rawZone;
			final List<RPObject> objects = new ArrayList<RPObject>();
			for (final RPObject object : zone) { objects.add(object); }
			for (final RPObject object : objects) {
				try {
					if (isEventItem(object)) {
						if (object instanceof Item) { new ItemLogger().timeout((Item) object); }
						else { new ItemLogger().destroy(null, null, object, "Mine Town event ended"); }
						zone.remove(object);
					} else if (object instanceof Player) {
						if (cleanupPlayer((Player) object, generation, false) > 0) { zone.modify(object); }
					} else if (removeContents(object, object instanceof RPEntity ? (RPEntity) object : null) > 0) {
						zone.modify(object);
					}
				} catch (final RuntimeException e) {
					LOGGER.error("Cannot clean up Mine Town items in " + object.getID(), e);
				}
			}
		}
	}
}
