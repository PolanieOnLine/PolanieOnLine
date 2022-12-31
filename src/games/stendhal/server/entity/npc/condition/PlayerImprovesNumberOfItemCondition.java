package games.stendhal.server.entity.npc.condition;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Checks if a player has improved a minimum number of item(s).
 *
 * @author KarajuSs
 */
@Dev(category=Category.ITEMS_UPGRADES, label="Item?")
public class PlayerImprovesNumberOfItemCondition implements ChatCondition {
	private Map<String, Integer> entities = null;
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
		entities = new HashMap<String, Integer>();
		List<String> names = Arrays.asList(item);
		for (String name : names) {
			entities.put(name, number);
		}
	}

	/**
	 * Create a new PlayerImprovesNumberOfItemCondition
	 *
	 * @param item
	 * 			list of required items
	 */
	public PlayerImprovesNumberOfItemCondition(String... item){
		entities = new HashMap<String, Integer>();
		List<String> names = Arrays.asList(item);
		for (String name : names) {
			final int getMaxNumberOfImproves = SingletonRepository.getEntityManager().getItem(name).getMaxImproves();
			entities.put(name, getMaxNumberOfImproves);
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
		if (entities == null) {
			final int getNumberOfImproves = player.getNumberOfImprovedForItem(player.getName());
			if (getNumberOfImproves < number) {
				return false;
			}
			return true;
		}

		for (Entry<String, Integer> entry : entities.entrySet()) {
			final String item = entry.getKey();
			final int getNumberOfImproves = player.getNumberOfImprovedForItem(item);

			if (entry.getValue().intValue() > getNumberOfImproves) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		if (entities == null) {
			return 43991 * number;
		}
		return 43991 * entities.hashCode() + number;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PlayerImprovesNumberOfItemCondition)) {
			return false;
		}
		PlayerImprovesNumberOfItemCondition other = (PlayerImprovesNumberOfItemCondition) obj;
		if (entities == null) {
			return number == other.number;
		}
		return (number == other.number)
			&& entities.equals(other.entities);
	}

	@Override
	public String toString() {
		if (entities == null) {
			return "PlayerImprovesNumberOfItemCondition <"+number+">";
		}
		return "PlayerImprovesNumberOfItemCondition <"+number+" of "+entities.toString()+">";
	}
}
