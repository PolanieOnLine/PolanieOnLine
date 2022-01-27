package games.stendhal.server.core.rp.achievement.condition;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

public class ItemImprovedNumberOfCondition implements ChatCondition {
	private final int count;

	public ItemImprovedNumberOfCondition(final int count) {
		this.count = count;
	}

	@Override
	public boolean fire(final Player player, final Sentence sentence, final Entity entity) {
		final int actual = player.getQuantityOfImprovedItems(player.getName());
		if (actual < count) {
			return false;
		}

		return true;
	}

	@Override
	public String toString() {
		return "ItemImprovedNumberOfCondition <" + count + ">";
	}

	@Override
	public int hashCode() {
		return 47 * count;
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof ItemImprovedNumberOfCondition)) {
			return false;
		}
		return count == ((ItemImprovedNumberOfCondition) obj).count;
	}
}
