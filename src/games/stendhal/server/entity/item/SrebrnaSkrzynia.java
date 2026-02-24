package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.ChestOpenTelemetry;
import games.stendhal.server.entity.ChestRewardProfile;
import games.stendhal.server.entity.player.Player;

/**
 * Srebrna skrzynia.
 */
public class SrebrnaSkrzynia extends StackableBox {

	static final ChestRewardProfile PROFILE = new ChestRewardProfile("silver", new ChestRewardProfile.RewardEntry[] {
			ChestRewardProfile.reward("money", 18, 300, 900, false, 1),
			ChestRewardProfile.reward("wielki eliksir", 16, 2, 4, false, 11500),
			ChestRewardProfile.reward("gigantyczny eliksir", 10, 1, 3, false, 30000),
			ChestRewardProfile.reward("skóra czerwonego smoka", 8, 2, 5, false, 2000),
			ChestRewardProfile.reward("skóra czarnego smoka", 8, 2, 5, false, 2500),
			ChestRewardProfile.reward("korale", 5, 1, 1, true, 1234),
			ChestRewardProfile.reward("ciupaga", 4, 1, 1, true, 5716),
			ChestRewardProfile.reward("zbroja cieni", 6, 1, 1, true, 16500),
			ChestRewardProfile.reward("spodnie cieni", 6, 1, 1, true, 5103),
			ChestRewardProfile.reward("buty cieni", 6, 1, 1, true, 5103),
			ChestRewardProfile.reward("złote spodnie", 5, 1, 1, true, 3584),
			ChestRewardProfile.reward("sztylet mroku", 4, 1, 1, true, 12800),
			ChestRewardProfile.reward("kamienna tarcza", 2, 1, 1, true, 36000),
			ChestRewardProfile.reward("miecz lodowy", 2, 1, 1, true, 62000),
	});

	public SrebrnaSkrzynia(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public SrebrnaSkrzynia(final SrebrnaSkrzynia item) {
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
