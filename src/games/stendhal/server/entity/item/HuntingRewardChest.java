package games.stendhal.server.entity.item;

import java.util.Map;
import games.stendhal.common.Level;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.quests.hunting.HuntingDifficulty;
import games.stendhal.server.maps.quests.hunting.HuntingRewards;

/** Bound hunting reward, with difficulty and level frozen when earned. */
public final class HuntingRewardChest extends StackableBox {
	public static final String NAME = "skrzynia myśliwego";

	public HuntingRewardChest(String name, String clazz, String subclass, Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}
	public HuntingRewardChest(HuntingRewardChest other) { super(other); }

	public static String createRewardData(HuntingDifficulty difficulty, int level) {
		if (difficulty == null || level < 0 || level > Level.maxLevel()) {
			throw new IllegalArgumentException("Invalid hunting chest data");
		}
		return difficulty.name() + ";" + level;
	}

	@Override
	protected boolean useMe(Player player) {
		if (getQuantity() <= 0) { return false; }
		// Even a restored chest must have a valid owner; no anonymous reward rolls.
		if (!isBound() || !player.getName().equals(getBoundTo())) {
			player.sendPrivateText("Ta skrzynia nie jest przypisana do Ciebie.");
			return false;
		}
		HuntingDifficulty difficulty;
		int level;
		try {
			String[] parts = getItemData().split(";", -1);
			if (parts.length != 2) { return invalidData(player); }
			difficulty = HuntingDifficulty.valueOf(parts[0]);
			level = Integer.parseInt(parts[1]);
			createRewardData(difficulty, level);
		} catch (IllegalArgumentException | NullPointerException e) {
			return invalidData(player);
		}
		Item reward = HuntingRewards.createReward(difficulty, level);
		if (reward == null) {
			player.sendPrivateText("Nie udało się przygotować nagrody. Skrzynia pozostaje u Ciebie.");
			return false;
		}
		removeOne();
		addRewardToInventory(player, reward);
		player.incObtainedForItem(reward.getName(), reward.getQuantity());
		player.notifyWorldAboutChanges();
		player.sendPrivateText("Ze Skrzyni Myśliwego otrzymujesz: " + reward.getName() + ".");
		return true;
	}

	private boolean invalidData(Player player) {
		player.sendPrivateText("Ta skrzynia nie ma poprawnie zapisanej nagrody polowania.");
		return false;
	}
}
