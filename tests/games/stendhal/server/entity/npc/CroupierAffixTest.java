package games.stendhal.server.entity.npc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.defaultruleset.DefaultItem;
import games.stendhal.server.core.rule.rarity.ItemAffixGenerator;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemRarityProfile;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.Pair;
import utilities.PlayerTestHelper;

@RunWith(Parameterized.class)
public class CroupierAffixTest {
	private final ItemRarity rarity;

	public CroupierAffixTest(final ItemRarity rarity) {
		this.rarity = rarity;
	}

	@Parameters(name = "{0}")
	public static Iterable<Object[]> rarities() {
		return Arrays.asList(new Object[][] {
			{ItemRarity.COMMON}, {ItemRarity.RARE},
			{ItemRarity.EPIC}, {ItemRarity.LEGENDARY}
		});
	}

	@BeforeClass
	public static void initializeWorld() {
		MockStendlRPWorld.get();
	}

	@Test
	public void dicePrizesHaveAffixesForTheirRolledRarity() {
		assertPrizeAffixes(false);
	}

	@Test
	public void billiardPrizesHaveAffixesForTheirRolledRarity() {
		assertPrizeAffixes(true);
	}

	private void assertPrizeAffixes(final boolean billiard) {
		final String name = billiard ? "AffixBilliardCroupier" : "AffixDiceCroupier";
		final CroupierNPC npc = new CroupierNPC(name) { };
		final Player player = PlayerTestHelper.createPlayer("Gambler");
		final DefaultItem definition = SingletonRepository.getEntityManager()
				.getDefaultItems().stream()
				.filter(item -> "hełm zabójcy".equals(item.getItemName()))
				.findFirst().get();
		final String originalProfile = definition.getRarityProfile();
		final String profileId = "croupier-test-" + rarity;
		final ItemRarityProfile.Builder profile = ItemRarityProfile.builder(profileId);
		for (final ItemRarity tier : ItemRarity.values()) {
			profile.tier(tier, tier == rarity ? 100 : 0, 1, 1, 1);
		}
		ItemRarityService.getInstance().registerProfile(profile.build());
		definition.setRarityProfile(profileId);
		try {
			npc.setPrizes(Collections.singletonMap(18,
					new Pair<String, String>("hełm zabójcy", "Oto twoja nagroda.")));
			if (billiard) {
				npc.processWinBill(player, 18);
			} else {
				npc.processWin(player, 18);
			}
			final Item prize = player.getFirstEquipped("hełm zabójcy");
			assertNotNull(prize);
			assertEquals(rarity, prize.getRarity());
			assertEquals(ItemAffixGenerator.getSlotCount(rarity)
					+ ItemAffixGenerator.getLegendarySlotCount(rarity),
					ItemAffixState.getValues(prize).size());
		} finally {
			definition.setRarityProfile(originalProfile);
			SingletonRepository.getNPCList().remove(name);
		}
	}
}
