/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import games.stendhal.server.entity.player.Player;

/** Persistent internal progress for repairing the eastern trade route. */
final class MieszczaninRepairProgress {
	static final String SLOT = "pierscien_mieszczanina_repair";
	static final String REPAIRED = "repaired";
	static final String STACH_CONFIRMED = "stach_confirmed";
	static final String COMMUNITY_APPROVED = "community_approved";

	private MieszczaninRepairProgress() {
		// utility class
	}

	static boolean isRepaired(final Player player) {
		return isIn(player, REPAIRED, STACH_CONFIRMED, COMMUNITY_APPROVED);
	}

	static boolean isStachConfirmed(final Player player) {
		return isIn(player, STACH_CONFIRMED, COMMUNITY_APPROVED);
	}

	static boolean isCommunityApproved(final Player player) {
		return isIn(player, COMMUNITY_APPROVED);
	}

	static void markRepaired(final Player player) {
		if (!isRepaired(player)) {
			player.setQuest(SLOT, REPAIRED);
		}
	}

	static void markStachConfirmed(final Player player) {
		if (!isStachConfirmed(player)) {
			player.setQuest(SLOT, STACH_CONFIRMED);
		}
	}

	static void markCommunityApproved(final Player player) {
		player.setQuest(SLOT, COMMUNITY_APPROVED);
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
