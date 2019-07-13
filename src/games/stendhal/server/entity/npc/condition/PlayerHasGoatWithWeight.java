package games.stendhal.server.entity.npc.condition;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.core.config.annotations.Dev;
import games.stendhal.server.core.config.annotations.Dev.Category;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.creature.Goat;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

/**
 * Checks if a player has goat width the required weight
 *
 * @author zekkeq
 */
@Dev(category=Category.OTHER, label="Pet?")
public class PlayerHasGoatWithWeight implements ChatCondition {
	private final int weight;

	/**
	 * Create PlayerHasGoatWithWeight
	 *
	 * @param number required weight of your goat
	 */
	public PlayerHasGoatWithWeight(final int weight) {
		this.weight = weight;
	}
	
	/**
	 * @return true if weight of goat is greater than condition
	 */
	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		final Goat goat = player.getGoat();
		return (goat.getWeight() > weight);
	}

	@Override
	public String toString() {
		return "wojownik posiada kozę z <"+weight+">";
	}

	@Override
	public int hashCode() {
		return 43591 * weight;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof PlayerHasGoatWithWeight)) {
			return false;
		}
		return weight == ((PlayerHasGoatWithWeight) obj).weight;
	}

}
