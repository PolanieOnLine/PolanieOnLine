package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.ChestOpenTelemetry;
import games.stendhal.server.entity.ChestRewardProfile;
import games.stendhal.server.entity.player.Player;

/**
 * Brazowa skrzynia.
 */
public class BrazowaSkrzynia extends Box {

	static final ChestRewardProfile PROFILE = new ChestRewardProfile("bronze", new ChestRewardProfile.RewardEntry[] {
			ChestRewardProfile.reward("money", 30, 80, 180, false, 1),
			ChestRewardProfile.reward("duży eliksir", 20, 1, 2, false, 4000),
			ChestRewardProfile.reward("wielki eliksir", 12, 1, 2, false, 12000),
			ChestRewardProfile.reward("gigantyczny eliksir", 5, 1, 1, false, 48000),
			ChestRewardProfile.reward("korale", 6, 1, 1, true, 1234),
			ChestRewardProfile.reward("ciupaga", 5, 1, 1, true, 5716),
			ChestRewardProfile.reward("spodnie kamienne", 5, 1, 1, true, 1512),
			ChestRewardProfile.reward("kamienna zbroja", 3, 1, 1, true, 40824),
			ChestRewardProfile.reward("buty kamienne", 4, 1, 1, true, 7000),
			ChestRewardProfile.reward("sztylet mroku", 3, 1, 1, true, 12800),
			ChestRewardProfile.reward("kosa", 2, 1, 1, true, 68590),
			ChestRewardProfile.reward("złota kolczuga", 5, 1, 1, true, 5103),
	});

	public BrazowaSkrzynia(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public BrazowaSkrzynia(final BrazowaSkrzynia item) {
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
