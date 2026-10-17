/***************************************************************************
 *                    (C) Copyright 2026 - PolanieOnLine                  *
 ***************************************************************************/
package games.stendhal.server.maps.deathmatch;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.common.Level;
import games.stendhal.server.core.rp.achievement.factory.DeathmatchAchievementFactory;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.player.RebornSystem;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * One-time best-effort reconstruction for trophy helmets whose saved DEF
 * dropped to 1 during the version 1.43 item refresh.
 *
 * There is no complete defense history: bails, replacement helmets and levels
 * at each win are unknown. Recorded victories determine the restored defense
 * for every player; a rebirth only changes the historical level cap to that
 * of level 597 (124 DEF), irrespective of the CURRENT level. The estimate
 * is min(recorded wins, historical level cap) and cannot reconstruct every
 * exact pre-1.43 value.
 */
public final class DeathmatchHelmetRecovery {
	private static final Logger logger = Logger.getLogger(DeathmatchHelmetRecovery.class);
	public static final String MIGRATION_MARKER = "deathmatch_helmet_recovery_v1";
	private static final String HELMET_NAME = "zdobyczny hełm";

	private DeathmatchHelmetRecovery() {
		// utility class
	}

	/** Applied only once for each existing character, even if no helmet is found. */
	public static void migrateOnLogin(final Player player) {
		if (player == null || player.has(MIGRATION_MARKER)) {
			return;
		}

		// Mark all players, including those without a helmet, so a later new
		// trophy is not mistaken for a broken pre-migration one.
		player.put(MIGRATION_MARKER, 1);

		final List<Item> helmets = new ArrayList<Item>();
		for (final RPSlot slot : player.slots()) {
			collectHelmets(slot, helmets);
		}
		if (helmets.size() != 1) {
			if (helmets.size() > 1) {
				logger.warn("Cannot automatically recover multiple Deathmatch helmets for "
						+ player.getName() + ": " + helmets.size());
			}
			return;
		}

		final Item helmet = helmets.get(0);
		if (!player.getName().equals(helmet.getBoundTo())
				|| !helmet.has("def") || helmet.getInt("def") != 1) {
			return;
		}

		final int wins = getRecordedWins(player);
		if (wins <= 1) {
			return;
		}

		// Reborn characters reached level 597 before their reset, so their
		// current level cannot lower the historic cap; wins still determine
		// the amount of defense earned.
		final int reborns = RebornSystem.getRebornCount(player);
		final int historicalLevel = reborns > 0
				? Level.maxLevel()
				: Math.max(0, Math.min(player.getLevel(), Level.maxLevel()));
		final int historicalCap = 5 + historicalLevel / 5;
		final int estimatedDefense = Math.min(wins, historicalCap);
		if (estimatedDefense <= 1) {
			return;
		}

		helmet.put("def", estimatedDefense);
		helmet.setPersistent(true);
		player.updateItemAtkDef();
		logger.info("Recovered Deathmatch helmet DEF for " + player.getName()
				+ " from 1 to " + estimatedDefense + " (wins=" + wins
				+ ", reborns=" + reborns + ", historicalCap=" + historicalCap
				+ "; win-capped estimate, previous bails are not recorded)");
	}

	private static int getRecordedWins(final Player player) {
		if (!player.hasQuest(DeathmatchAchievementFactory.WINS_SLOT)) {
			return 0;
		}
		try {
			return Math.max(0, Integer.parseInt(
					player.getQuest(DeathmatchAchievementFactory.WINS_SLOT, 0)));
		} catch (final NumberFormatException ex) {
			return 0;
		}
	}

	/** Search banks and nested inventories, not only currently carried items. */
	private static void collectHelmets(final RPSlot slot, final List<Item> helmets) {
		for (final RPObject object : slot) {
			if (object instanceof Item) {
				final Item item = (Item) object;
				if (HELMET_NAME.equals(item.getName())) {
					helmets.add(item);
				}
			}
			for (final RPSlot child : object.slots()) {
				collectHelmets(child, helmets);
			}
		}
	}
}
