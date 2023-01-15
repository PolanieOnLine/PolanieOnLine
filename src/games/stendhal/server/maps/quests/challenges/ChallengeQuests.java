package games.stendhal.server.maps.quests.challenges;

import java.util.Arrays;
import java.util.List;

import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.AbstractQuest;

public class ChallengeQuests extends AbstractQuest implements ChallengeInterface {
	// Trigger ConversationPhrases
	protected final List<String> triggers = Arrays.asList("wyzwanie", "zadanie", "misja", "przysługa", "task", "quest", "favor", "favour");

	@Override
	public List<String> getHistory(Player player) {
		// do nothing
		return null;
	}
	@Override
	public String getSlotName() {
		// do nothing
		return null;
	}
	@Override
	public void addToWorld() {
		// do nothing
	}
	@Override
	public String getName() {
		// do nothing
		return null;
	}

	@Override
	public String getQuestSlot(Player player, int index) {
		// do nothing
		return null;
	}

	@Override
	public String[] getEntitiesToRecord() {
		// do nothing
		return null;
	}

	/**
	 * Gets actual purpose value to do by player.
	 *
	 * @param player
	 * 		The player.
	 * @return value
	 */
	public int getPurposeValue(final Player player) {
		return Integer.parseInt(getQuestSlot(player, 1));
	}

	/**
	 * Gets previous entity value.
	 *
	 * @param player
	 * 		The player.
	 * @return value
	 */
	public int getPreviousValue(final Player player) {
		return Integer.parseInt(getQuestSlot(player, 2));
	}

	/**
	 * Get kill value difference.
	 *
	 * @param player
	 * 		The player.
	 * @return value difference.
	 */
	public int getDiffKills(final Player player) {
		return getActualKills(player, getEntitiesToRecord()) - getPreviousValue(player);
	}

	/**
	 * Get value how many monsters were killed by player.
	 *
	 * @param player
	 * 		The player.
	 * @return value
	 */
	public int getKills(final Player player) {
		return getPurposeValue(player) - (getPurposeValue(player) - getDiffKills(player));
	}

	/**
	 * Get the current value of the monsters killed by the player.
	 *
	 * @param player
	 * 		The player.
	 * @param creatures
	 * 		List of creatures to kill.
	 * @return kills
	 */
	public int getActualKills(final Player player, String... creatures) {
		int kills = 0;
		for (final String creature: creatures) {
			kills += player.getSoloKill(creature) + player.getSharedKill(creature);
		}

		return kills;
	}

	/**
	 * Get loot value difference.
	 *
	 * @param player
	 * 		The player.
	 * @return value difference.
	 */
	public int getDiffLoot(final Player player) {
		return getActualLooted(player, getEntitiesToRecord()) - getPreviousValue(player);
	}

	/**
	 * Get value how many items were looted by player.
	 *
	 * @param player
	 * 		The player.
	 * @return value
	 */
	public int getLoot(final Player player) {
		return getPurposeValue(player) - (getPurposeValue(player) - getDiffLoot(player));
	}

	/**
	 * Get the current value of the items looted by the player.
	 *
	 * @param player
	 * 		The player.
	 * @param items
	 * 		List of items to loot.
	 * @return looted
	 */
	public int getActualLooted(final Player player, String... items) {
		int looted = 0;
		for (final String item: items) {
			looted += player.getNumberOfLootsForItem(item);
		}

		return looted;
	}

	public boolean getGreaterKills(final Player player) {
		return getKills(player) >= getPurposeValue(player);
	}
	public boolean getLessKills(final Player player) {
		return getKills(player) < getPurposeValue(player);
	}

	public boolean getGreaterLoots(final Player player) {
		return getLoot(player) >= getPurposeValue(player);
	}
	public boolean getLessLoots(final Player player) {
		return getLoot(player) < getPurposeValue(player);
	}
}
