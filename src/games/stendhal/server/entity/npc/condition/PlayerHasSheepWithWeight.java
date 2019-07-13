package games.stendhal.server.entity.npc.condition;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Sheep;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Checks if a player has sheep width the required weight
 *
 * @author zekkeq
 */
@Dev(category=Category.OTHER, label="Pet?")
public class PlayerHasSheepWithWeight implements ChatCondition {
	private final int weight;

	/**
	 * Create PlayerHasSheepWithWeight
	 *
	 * @param number required weight of your sheep
	 */
	public PlayerHasSheepWithWeight(final int weight) {
		this.weight = weight;
	}
	
	/**
	 * @return true if weight of sheep is greater than condition
	 */
	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		final Sheep sheep = player.getSheep();
		return (sheep.getWeight() > weight);
	}

	@Override
	public String toString() {
		return "wojownik posiada owcę z <"+weight+">";
	}

	@Override
	public int hashCode() {
		return 43591 * weight;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof PlayerHasSheepWithWeight)) {
			return false;
		}
		return weight == ((PlayerHasSheepWithWeight) obj).weight;
	}

}
