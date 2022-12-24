package games.stendhal.server.core.rp.achievement.condition;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.common.parser.Sentence;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.npc.ChatCondition;
import games.stendhal.server.entity.player.Player;

public class ItemIsImprovedCondition implements ChatCondition {
	private final List<String> items;
	private int value = 1;

	public ItemIsImprovedCondition(String item) {
		items = new LinkedList<String>();
		items.add(item);
	}

	public ItemIsImprovedCondition(String... item) {
		items = new LinkedList<String>();
		if (item != null) {
			for (String string : item) {
				items.add(string);
			}
		}
	}

	public ItemIsImprovedCondition(int value, String item) {
		this.value = value;
		items = new LinkedList<String>();
		items.add(item);
	}

	public ItemIsImprovedCondition(int value, String... item) {
		this.value = value;
		items = new LinkedList<String>();
		if (item != null) {
			for (String string : item) {
				items.add(string);
			}
		}
	}

	@Override
	public boolean fire(Player player, Sentence sentence, Entity npc) {
		for(final String item : items) {
			if (player.getQuantityOfImprovedItems(item) < this.value) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int hashCode() {
		return 43991 * items.hashCode() + value;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ItemIsImprovedCondition)) {
			return false;
		}
		ItemIsImprovedCondition other = (ItemIsImprovedCondition) obj;
		return (value == other.value)
			&& items.equals(other.items);
	}

	@Override
	public String toString() {
		return "ItemIsImprovedCondition <"+value+" up_level of "+items.toString()+">";
	}
}
