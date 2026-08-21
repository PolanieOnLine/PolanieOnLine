/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.server.entity.player.Player;

/** Persistent internal progress for the private Mieszczanin hideout. */
final class MieszczaninHideoutProgress {
	static final String SLOT = "pierscien_mieszczanina_hideout";
	static final String CLEARED = "cleared";
	static final String MESSENGER_FREED = "messenger_freed";
	static final String TOOLS_RECOVERED = "tools_recovered";

	private MieszczaninHideoutProgress() {
		// utility class
	}

	static boolean isCleared(final Player player) {
		return isIn(player, CLEARED, MESSENGER_FREED, TOOLS_RECOVERED);
	}

	static boolean isMessengerFreed(final Player player) {
		return isIn(player, MESSENGER_FREED, TOOLS_RECOVERED);
	}

	static boolean areToolsRecovered(final Player player) {
		return isIn(player, TOOLS_RECOVERED);
	}

	static void markCleared(final Player player) {
		if (!isCleared(player)) {
			player.setQuest(SLOT, CLEARED);
		}
	}

	static void markMessengerFreed(final Player player) {
		if (!isMessengerFreed(player)) {
			player.setQuest(SLOT, MESSENGER_FREED);
		}
	}

	static void markToolsRecovered(final Player player) {
		player.setQuest(SLOT, TOOLS_RECOVERED);
	}

	static void clear(final Player player) {
		if (player.hasQuest(SLOT)) {
			player.removeQuest(SLOT);
		}
	}

	private static boolean isIn(final Player player, final String... states) {
		return player.hasQuest(SLOT) && player.isQuestInState(SLOT, states);
	}
}
