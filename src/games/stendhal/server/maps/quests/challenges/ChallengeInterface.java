package games.stendhal.server.maps.quests.challenges;

import games.stendhal.server.entity.player.Player;

public interface ChallengeInterface {
	/**
	 * Gets QUEST_SLOT index.
	 *
	 * @param player
	 * 		The player.
	 * @param index
	 * 		Index of QUEST_SLOT.
	 * @return value from QUEST_SLOT indexing.
	 */
	String getQuestSlot(final Player player, int index);
	/**
	 * Get entities to record.
	 *
	 * @return array
	 */
	String[] getEntitiesToRecord();
}
