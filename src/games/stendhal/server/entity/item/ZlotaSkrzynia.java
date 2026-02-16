package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.ChestOpenTelemetry;
import games.stendhal.server.entity.ChestRewardProfile;
import games.stendhal.server.entity.player.Player;

/**
 * Zlota skrzynia.
 */
public class ZlotaSkrzynia extends StackableBox {

	static final ChestRewardProfile PROFILE = new ChestRewardProfile("gold", new ChestRewardProfile.RewardEntry[] {
			ChestRewardProfile.reward("money", 10, 600, 1200, false, 1),
			ChestRewardProfile.reward("gigantyczny eliksir", 13, 2, 4, false, 33000),
			ChestRewardProfile.reward("wielki eliksir", 12, 2, 4, false, 10500),
			ChestRewardProfile.reward("skóra złotego smoka", 14, 5, 9, false, 2300),
			ChestRewardProfile.reward("skóra arktycznego smoka", 12, 5, 9, false, 2600),
			ChestRewardProfile.reward("złota zbroja", 10, 1, 1, true, 19000),
			ChestRewardProfile.reward("zbroja cieni", 8, 1, 1, true, 21000),
			ChestRewardProfile.reward("tarcza płytowa", 8, 1, 1, true, 13500),
			ChestRewardProfile.reward("kamienna tarcza", 2, 1, 1, true, 30000),
			ChestRewardProfile.reward("miecz lodowy", 2, 1, 1, true, 60000),
			ChestRewardProfile.reward("czarne spodnie", 3, 1, 1, true, 16500),
			ChestRewardProfile.reward("czarne buty", 3, 1, 1, true, 16500),
	});

	public ZlotaSkrzynia(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public ZlotaSkrzynia(final ZlotaSkrzynia item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		removeOne();

		final ChestRewardProfile.RolledReward reward = PROFILE.rollReward();
		final Item item = SingletonRepository.getEntityManager().getItem(reward.getItemName());
		if (item instanceof StackableItem) {
			((StackableItem) item).setQuantity(reward.getQuantity());
		}
		if (reward.isBound()) {
			item.setBoundTo(player.getName());
		}

		player.equipOrPutOnGround(item);
		player.incObtainedForItem(item.getName(), item.getQuantity());
		player.notifyWorldAboutChanges();
		player.sendPrivateText("Gratulacje! Ze skrzynki otrzymałeś #'"
				+ Grammar.quantityplnoun(item.getQuantity(), item.getName()) + "'!");

		ChestOpenTelemetry.record(PROFILE.getChestId(), reward.expectedValue());
		return true;
	}
}
