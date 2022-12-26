package games.stendhal.server.entity.npc.condition;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Checks if a player has improved a minimum number of item(s).
 *
 * @author KarajuSs
 */
@Dev(category=Category.ITEMS_UPGRADES, label="Item?")
public class PlayerImprovesNumberOfItemCondition implements ChatCondition {
	private List<String> items = null;
	private int number = -1;

	/**
	 * Create a new PlayerImprovesNumberOfItemCondition
	 *
	 * @param number
	 * 			required number of each item
	 * @param item
	 * 			list of required items
	 */
	public PlayerImprovesNumberOfItemCondition(int number, String... item){
		this.number = number;
		items = new LinkedList<String>();
		if (item != null) {
			for (String string : item) {
				items.add(string);
			}
		}
	}

	/**
	 * Create a new PlayerImprovesNumberOfItemCondition
	 *
	 * @param item
	 * 			list of required items
	 */
	public PlayerImprovesNumberOfItemCondition(String... item){
		items = new LinkedList<String>();
		if (item != null) {
			for (String string : item) {
				items.add(string);
			}
		}
	}

	/**
	 * Create a new PlayerImprovesNumberOfItemCondition
	 *
	 * @param number
	 * 			count of item improved by player
	 */
	public PlayerImprovesNumberOfItemCondition(int number){
		this.number = number;
	}

	@Override
	public boolean fire(Player player, Sentence sentence, Entity npc) {
		if (items == null) {
			final int actual = player.getQuantityOfImprovedItems(player.getName());
			if (actual < number) {
				return false;
			}
			return true;
		}

		if (number == -1) {
			for (String item : items) {
				final Item actual = SingletonRepository.getEntityManager().getItem(item);
				if (player.getNumberOfImprovedForItem(item) != actual.getMaxImproves()) {
					return false;
				}
			}
			return true;
		}

		for (String item : items) {
			if (player.getNumberOfImprovedForItem(item) < number) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (items == null) {
			return 43991 * number;
		}
		return 43991 * items.hashCode() + number;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PlayerImprovesNumberOfItemCondition)) {
			return false;
		}
		PlayerImprovesNumberOfItemCondition other = (PlayerImprovesNumberOfItemCondition) obj;
		if (items == null) {
			return number == other.number;
		}
		return (number == other.number)
			&& items.equals(other.items);
	}

	@Override
	public String toString() {
		if (items == null) {
			return "PlayerImprovesNumberOfItemCondition <"+number+">";
		}
		return "PlayerImprovesNumberOfItemCondition <"+number+" of "+items.toString()+">";
	}
}
