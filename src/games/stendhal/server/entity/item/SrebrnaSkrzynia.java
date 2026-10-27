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
 * Srebrna skrzynia.
 */
public class SrebrnaSkrzynia extends StackableBox {

	static final ChestRewardProfile PROFILE = new ChestRewardProfile("silver", new ChestRewardProfile.RewardEntry[] {
			ChestRewardProfile.reward("money", 10, 600, 1500, false, 1),
			ChestRewardProfile.reward("wielki eliksir", 14, 3, 5, false, 11500),
			ChestRewardProfile.reward("gigantyczny eliksir", 10, 2, 4, false, 30000),
			ChestRewardProfile.reward("skóra czerwonego smoka", 6, 3, 6, false, 2000),
			ChestRewardProfile.reward("skóra czarnego smoka", 5, 3, 6, false, 2500),
			ChestRewardProfile.reward("hełm chaosu", 8, 1, 1, true, 6000),
			ChestRewardProfile.reward("ciupaga", 5, 1, 1, true, 5716),
			ChestRewardProfile.reward("zbroja cieni", 8, 1, 1, true, 16500),
			ChestRewardProfile.reward("spodnie chaosu", 8, 1, 1, true, 8000),
			ChestRewardProfile.reward("buty chaosu", 8, 1, 1, true, 4000),
			ChestRewardProfile.reward("sztylet mroku", 6, 1, 1, true, 12800),
			ChestRewardProfile.reward("kamienna tarcza", 5, 1, 1, true, 36000),
			ChestRewardProfile.reward("miecz lodowy", 5, 1, 1, true, 62000),
			ChestRewardProfile.reward("zbroja chaosu", 2, 1, 1, true, 19500),
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
		final Item item = SingletonRepository.getEntityManager().getItem(reward.getItemName(),
				ItemCreationContext.builder(ItemCreationContext.Source.DROP)
						.withProfile(ItemRarityProfile.SILVER_CHEST_ID).build());
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
