package games.stendhal.server.entity.npc.behaviour.impl;

import static org.junit.Assert.*;
import java.util.Collections;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.constants.CurrencyReform;
import games.stendhal.common.grammar.ItemParserResult;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.transformer.ItemTransformer;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;

public class ValueBuyerBehaviourTest {
	@BeforeClass public static void setup() { MockStendlRPWorld.get(); }
	private EntityManager manager() { return SingletonRepository.getEntityManager(); }
	private ValueBuyerBehaviour buyer() { return new ValueBuyerBehaviour(manager().getDefaultItems()); }
	private ItemParserResult request(final int amount) {
		return new ItemParserResult(true, "miecz", amount, Collections.<String>emptySet());
	}
	private Item item(final ItemRarity rarity) {
		return manager().getItem("miecz", ItemCreationContext.builder(ItemCreationContext.Source.DROP)
				.withFactoryRarity(rarity).randomizeModifiers(false).generateAffixes(false).build());
	}
	@Test public void priceOfferAvoidsGuessingTheGenderOfItemNames() {
		final String payment = CurrencyReform.useReformedCurrency() ? "1 miedziaka" : "1 monetę";
		for (String name : new String[] { "polano", "kolczuga", "miecz", "spodnie kolcze" }) {
			final ItemParserResult res = new ItemParserResult(true, name, 1, Collections.<String>emptySet());
			assertEquals("Dam ci " + payment + " za „" + name + "”. Sprzedajesz?",
					buyer().getPriceOffer(res, 1));
		}
	}
	@Test public void priceOffersDeclineCoinsAndKeepStackSizeSeparateFromNames() {
		final int[] amounts = { 2, 5, 12, 22 };
		final String[] legacy = { "2 monety", "5 monet", "12 monet", "22 monety" };
		final String[] reformed = { "2 miedziaki", "5 miedziaków", "12 miedziaków", "22 miedziaki" };
		for (int i = 0; i < amounts.length; i++) {
			final ItemParserResult res = new ItemParserResult(true, "polano", amounts[i], Collections.<String>emptySet());
			assertEquals("Dam ci " + (CurrencyReform.useReformedCurrency() ? reformed[i] : legacy[i])
					+ " za „polano” (" + amounts[i] + " szt.). Sprzedajesz?",
					buyer().getPriceOffer(res, amounts[i]));
		}
	}
	@Test public void catalogueAutomaticallyIncludesDefinitionsButNeverMoneyOrBoundChest() {
		assertTrue(buyer().hasItem("miecz"));
		assertTrue(buyer().hasItem("zbroja z mithrilu"));
		assertFalse(buyer().hasItem("money"));
		assertFalse(buyer().hasItem("skrzynia Areny Wyzwań"));
	}
	@Test public void sumsActualValuesInDropOrderWithoutApplyingRarityTwice() {
		final Player player = PlayerTestHelper.createPlayer("value-test");
		final Item common = item(ItemRarity.COMMON);
		final Item legendary = item(ItemRarity.LEGENDARY);
		player.getSlot("bag").add(common);
		player.getSlot("bag").add(legendary);
		assertEquals(common.getValue() + legendary.getValue(), buyer().getCharge(request(2), player));
		assertEquals(0, buyer().getCharge(request(3), player));
	}
	@Test public void staleSavedValueIsRepricedOnRestoreWithoutChangingRarityOrCombatStats() {
		final Item original = item(ItemRarity.LEGENDARY);
		final int attack = original.getInt("atk");
		final int expected = original.getDefinitionValue() * 2;
		original.setValue(9999999);
		original.setID(new RPObject.ID(201, "value-restore-test"));
		final Item restored = new ItemTransformer().transform(new RPObject(original));
		assertEquals(expected, restored.getValue());
		assertEquals(attack, restored.getInt("atk"));
		assertEquals(ItemRarity.LEGENDARY, restored.getRarity());
	}
	@Test public void refusesBoundItemsAndInvalidAmounts() {
		final Player player = PlayerTestHelper.createPlayer("value-test");
		final Item sword = item(ItemRarity.RARE);
		sword.setBoundTo(player.getName());
		player.getSlot("bag").add(sword);
		assertEquals(0, buyer().getCharge(request(1), player));
		assertEquals(0, buyer().getCharge(request(-1), player));
		assertEquals(0, buyer().getCharge(request(1001), player));
	}
	@Test public void transactionConsumesLootAndPaysItsValue() {
		final Player player = PlayerTestHelper.createPlayer("value-test");
		final StendhalRPZone zone = new StendhalRPZone("value-buy-test", 20, 20);
		zone.add(player);
		final Item sword = item(ItemRarity.RARE);
		player.getSlot("bag").add(sword);
		PlayerTestHelper.generateNPCRPClasses();
		final SpeakerNPC npc = new SpeakerNPC("Test Buyer");
		zone.add(npc);
		assertTrue(buyer().transactAgreedDeal(request(1), new EventRaiser(npc), player));
		assertFalse(player.isEquipped("miecz"));
		int money = 0;
		for (final Item stack : player.getAllEquipped("money")) { money += stack.getQuantity(); }
		assertEquals(sword.getValue(), money);
	}
	@Test public void refusesPaymentThatWouldOverflowMoneyStack() {
		final Player player = PlayerTestHelper.createPlayer("value-test");
		player.getSlot("bag").add(item(ItemRarity.COMMON));
		final StackableItem money = (StackableItem) manager().getItem("money");
		money.setQuantity(Integer.MAX_VALUE);
		player.getSlot("pouch").add(money);
		assertEquals(0, buyer().getCharge(request(1), player));
		assertTrue(player.isEquipped("miecz"));
	}
}
