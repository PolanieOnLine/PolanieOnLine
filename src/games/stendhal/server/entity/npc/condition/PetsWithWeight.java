package games.stendhal.server.entity.npc.condition;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Checks if a player has a pet
 *
 * @author zekkeq
 */
@Dev(category=Category.OTHER, label="Pet?")
public class PetsWithWeight implements ChatCondition {
	private final List<String> nameList;
	private final int weight;

	/**
	 * Checks if a player has harvested a minimum number of an item
	 *
	 * @param number required number of each item
	 * @param items list of items required
	 */
	public PetsWithWeight(int number, String... names) {
		nameList = new ArrayList<String>();
		if (names != null) {
			for (String item : names) {
				nameList.add(item);
			}
		}
		weight = number;
	}

	@Override
	public boolean fire(Player player, Sentence sentence, Entity npc) {
		for(String item : nameList) {
			if(item=="sheep") {
				if(weight > player.getSheep().getWeight()) {
					return false;
				}
			} else if(item=="goat") {
				if(weight > player.getGoat().getWeight()) {
					return false;
				}
			} else {
				if(weight > player.getPet().getWeight()) {
					return false;
				}
			}
		}
		return true;
	}


	@Override
	public int hashCode() {
		return 43853 * nameList.hashCode() + weight;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof PetsWithWeight)) {
			return false;
		}
		PetsWithWeight other = (PetsWithWeight) obj;
		return (weight == other.weight)
			&& nameList.equals(other.nameList);
	}

	@Override
	public String toString() {
		return "<"+weight+" weight of "+nameList+">";
	}

}
