package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityProfile;
import games.stendhal.server.entity.ChestOpenTelemetry;
import games.stendhal.server.entity.ChestRewardProfile;
import games.stendhal.server.entity.player.Player;

/**
 * Zlota skrzynia.
 */
public class ZlotaSkrzynia extends StackableBox {

	static final ChestRewardProfile PROFILE = new ChestRewardProfile("gold", new ChestRewardProfile.RewardEntry[] {
			ChestRewardProfile.reward("money", 6, 1500, 3000, false, 1),
			ChestRewardProfile.reward("gigantyczny eliksir", 12, 3, 5, false, 33000),
			ChestRewardProfile.reward("wielki eliksir", 7, 4, 6, false, 10500),
			ChestRewardProfile.reward("skóra złotego smoka", 5, 6, 10, false, 2300),
			ChestRewardProfile.reward("skóra arktycznego smoka", 5, 6, 10, false, 2600),
			ChestRewardProfile.reward("zbroja chaosu", 10, 1, 1, true, 19500),
			ChestRewardProfile.reward("lodowa zbroja", 8, 1, 1, true, 21000),
			ChestRewardProfile.reward("tarcza chaosu", 8, 1, 1, true, 18000),
			ChestRewardProfile.reward("miecz lodowy", 8, 1, 1, true, 60000),
			ChestRewardProfile.reward("czarny miecz", 7, 1, 1, true, 20000),
			ChestRewardProfile.reward("czarne spodnie", 7, 1, 1, true, 16500),
			ChestRewardProfile.reward("czarne buty", 7, 1, 1, true, 16500),
			ChestRewardProfile.reward("czarny hełm", 5, 1, 1, true, 12500),
			ChestRewardProfile.reward("czarna zbroja", 3, 1, 1, true, 60000),
			ChestRewardProfile.reward("czarna tarcza", 2, 1, 1, true, 75000),
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
		final Item item = SingletonRepository.getEntityManager().getItem(reward.getItemName(),
				ItemCreationContext.builder(ItemCreationContext.Source.DROP)
						.withProfile(ItemRarityProfile.GOLD_CHEST_ID).build());
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
