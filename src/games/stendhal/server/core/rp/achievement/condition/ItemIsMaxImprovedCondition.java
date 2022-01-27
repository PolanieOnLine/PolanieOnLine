package games.stendhal.server.core.rp.achievement.condition;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

public class ItemIsMaxImprovedCondition implements ChatCondition {
	private final List<String> items;

	public ItemIsMaxImprovedCondition(String item) {
		items = new LinkedList<String>();
		items.add(item);
	}

	public ItemIsMaxImprovedCondition(String... item) {
		items = new LinkedList<String>();
		if (item != null) {
			for (String string : item) {
				items.add(string);
			}
		}
	}

	@Override
	public boolean fire(Player player, Sentence sentence, Entity npc) {
		boolean did = false;
		for(final String entity : this.items) {
			Item item = player.getFirstEquipped(entity);
			if(item == null) {
				return false;
			}

			if(item.isMaxImproved()) {
				did = true;
			} else {
				did = false;
			}
		}
		return did;
	}

	@Override
	public int hashCode() {
		return 43991 * items.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ItemIsMaxImprovedCondition)) {
			return false;
		}
		ItemIsMaxImprovedCondition other = (ItemIsMaxImprovedCondition) obj;
		return items.equals(other.items);
	}

	@Override
	public String toString() {
		return "max improved items are <"+items.toString()+">";
	}
}
