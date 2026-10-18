package games.stendhal.server.entity.npc.behaviour.impl;

import static org.junit.Assert.*;
import java.util.Map;
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
import games.stendhal.server.entity.item.money.MoneyUtils;
import games.stendhal.server.entity.npc.EventRaiser;
import games.stendhal.server.entity.npc.SpeakerNPC;
import games.stendhal.server.entity.npc.shop.ShopType;
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
	@Test public void priceOfferUsesPolishCasesWithoutQuotes() {
		final String payment = CurrencyReform.useReformedCurrency() ? "1 miedziaka" : "1 monetę";
		final String[] names = { "polano", "kolczuga", "miecz", "spodnie kolcze" };
		final String[] forms = { "polano", "kolczugę", "miecz", "parę spodni kolczych" };
		for (int i = 0; i < names.length; i++) {
			final String name = names[i];
			final ItemParserResult res = new ItemParserResult(true, name, 1, Collections.<String>emptySet());
			assertEquals("Dam ci " + payment + " za " + forms[i] + ". Sprzedajesz?",
					buyer().getPriceOffer(res, 1));
		}
	}
	@Test public void priceOffersDeclineCoinsAndItemQuantitiesTogether() {
		final int[] amounts = { 2, 5, 12, 22 };
		final String[] legacy = { "2 monety", "5 monet", "12 monet", "22 monety" };
		final String[] reformed = { "2 miedziaki", "5 miedziaków", "12 miedziaków", "22 miedziaki" };
		final String[] items = { "2 polana", "5 polan", "12 polan", "22 polana" };
		for (int i = 0; i < amounts.length; i++) {
			final ItemParserResult res = new ItemParserResult(true, "polano", amounts[i], Collections.<String>emptySet());
			assertEquals("Dam ci " + (CurrencyReform.useReformedCurrency() ? reformed[i] : legacy[i])
					+ " za " + items[i] + ". Sprzedajesz?",
					buyer().getPriceOffer(res, amounts[i]));
		}
	}
	@Test public void amethystOfferMatchesTheNaturalStackDescription() {
		final ItemParserResult res = new ItemParserResult(true, "ametyst", 8, Collections.<String>emptySet());
		final String payment = CurrencyReform.useReformedCurrency() ? "40 talarów" : "4000 monet";
		assertEquals("Dam ci " + payment + " za 8 sztuk ametystu. Sprzedajesz?", buyer().getPriceOffer(res, 4000));
	}
	@Test public void catalogueAutomaticallyIncludesDefinitionsButNeverMoneyOrBoundChest() {
		assertTrue(buyer().hasItem("miecz"));
		assertTrue(buyer().hasItem("zbroja z mithrilu"));
		assertFalse(buyer().hasItem("money"));
		assertFalse(buyer().hasItem("skrzynia Areny Wyzwań"));
	}
	@Test public void elixirsUseReducedArenaPricesWithoutChangingTheirItemValue() {
		final String[] names = { "mały eliksir", "eliksir", "duży eliksir", "wielki eliksir",
				"eliksir miłości", "gigantyczny eliksir", "smoczy eliksir", "duży smoczy eliksir",
				"eliksir mroku" };
		final int[] unitPrices = { 19, 49, 99, 293, 3000, 586, 12000, 16000, 75 };
		final ValueBuyerBehaviour arenaBuyer = buyer();
		for (int i = 0; i < names.length; i++) {
			final String name = names[i];
			final Player player = PlayerTestHelper.createPlayer("ArenaElixirSeller");
			final StackableItem potion = (StackableItem) manager().getItem(name, ItemCreationContext.restore());
			potion.setQuantity(5);
			player.getSlot("bag").add(potion);
			final int originalValue = potion.getValue();
			final int unitPrice = unitPrices[i];
			final ItemParserResult request = new ItemParserResult(true, name, 3, Collections.<String>emptySet());
			assertEquals(name + " catalogue", unitPrice, arenaBuyer.getUnitPrice(name));
			assertEquals(name + " quote", unitPrice * 3, arenaBuyer.getCharge(request, player));
			assertEquals(name + " repeated quote", unitPrice * 3, arenaBuyer.getCharge(request, player));
			assertEquals(name + " item value", originalValue, potion.getValue());
			assertEquals(name + " ordinary merchant", originalValue * 3,
					new BuyerBehaviour(Collections.singletonMap(name, originalValue)).getCharge(request, player));
		}
	}
	@Test public void ordinaryDrinksAndAntidotesKeepTheirFullValue() {
		for (final String name : new String[] { "sok z chmielu", "zupa", "mleko", "antidotum" }) {
			final Player player = PlayerTestHelper.createPlayer("ArenaDrinkSeller");
			final Item drink = manager().getItem(name, ItemCreationContext.restore());
			player.getSlot("bag").add(drink);
			final ItemParserResult request = new ItemParserResult(true, name, 1, Collections.<String>emptySet());
			assertEquals(name, drink.getValue(), buyer().getCharge(request, player));
			assertEquals(name, drink.getValue(), buyer().getUnitPrice(name));
		}
	}
	@Test public void elixirSalePaysDiscountedPriceAndConsumesOnlyRequestedQuantity() {
		final Player player = PlayerTestHelper.createPlayer("ArenaElixirPayment");
		final StendhalRPZone zone = new StendhalRPZone("arena-elixir-payment", 20, 20);
		zone.add(player);
		final StackableItem potion = (StackableItem) manager().getItem("mały eliksir", ItemCreationContext.restore());
		potion.setQuantity(5);
		player.getSlot("bag").add(potion);
		PlayerTestHelper.generateNPCRPClasses();
		final SpeakerNPC npc = new SpeakerNPC("Elixir Test Buyer");
		zone.add(npc);
		final ItemParserResult request = new ItemParserResult(true, "mały eliksir", 3, Collections.<String>emptySet());
		assertTrue(buyer().transactAgreedDeal(request, new EventRaiser(npc), player));
		assertEquals(57, MoneyUtils.getTotalMoneyInCopper(player));
		assertEquals(2, potion.getQuantity());
		assertEquals(79, potion.getValue());
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
	@Test public void oldRareLootUsesTheNewPriceWithoutApplyingTheMultiplierTwice() {
		final Player player = PlayerTestHelper.createPlayer("old-value-test");
		final Item rare = item(ItemRarity.RARE);
		rare.setRarityModifier("value", 1.20);
		rare.setValue((int) Math.round(rare.getDefinitionValue() * 1.20));
		final int attack = rare.getInt("atk");
		rare.put("improve", 2);
		player.getSlot("bag").add(rare);
		final int expected = (int) Math.round(rare.getDefinitionValue() * 1.25);
		assertEquals(expected, buyer().getCharge(request(1), player));
		assertEquals(expected, buyer().getCharge(request(1), player));
		assertEquals(Double.valueOf(1.25), rare.getRarityModifier("value"));
		assertEquals(attack, rare.getInt("atk"));
		assertEquals(2, rare.getUpgradeLevel());
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
	@Test public void blackShopItemsRequireBalduinOnlyForQuestGatedBuyer() {
		final Map<String, Integer> blackItems = SingletonRepository.getShopsList()
				.get("buyblack", ShopType.ITEM_BUY);
		assertNotNull("Balduin's black-item catalogue must be loaded", blackItems);
		final ValueBuyerBehaviour arenaBuyer = new ValueBuyerBehaviour(
				manager().getDefaultItems(), blackItems.keySet(), "ultimate_collector");
		int tradable = 0;
		for (final String name : blackItems.keySet()) {
			if (!arenaBuyer.hasItem(name)) { continue; }
			tradable++;
			final Player player = PlayerTestHelper.createPlayer("black-item-seller");
			final Item item = manager().getItem(name, ItemCreationContext.restore());
			player.getSlot("bag").add(item);
			final ItemParserResult request = new ItemParserResult(true, name, 1,
					Collections.<String>emptySet());

			assertEquals(name, 0, arenaBuyer.getCharge(request, player));
			assertTrue(name, arenaBuyer.getUnavailableOffer(request).contains("Balduina"));
			// This gate is specific to the arena merchant; the ordinary buyer is unchanged.
			assertEquals(name, item.getValue(), buyer().getCharge(request, player));

			player.setQuest("ultimate_collector", "done");
			assertEquals(name, item.getValue(), arenaBuyer.getCharge(request, player));
			player.setQuest("ultimate_collector", "start");
			assertEquals(name, 0, arenaBuyer.getCharge(request, player));
		}
		assertTrue("Expected black items eligible for the arena merchant", tradable > 0);
	}

	@Test public void completionDoesNotChangeOtherLootOrAllowBoundBlackItems() {
		final ValueBuyerBehaviour arenaBuyer = new ValueBuyerBehaviour(
				manager().getDefaultItems(),
				SingletonRepository.getShopsList().get("buyblack", ShopType.ITEM_BUY).keySet(),
				"ultimate_collector");
		final Player player = PlayerTestHelper.createPlayer("arena-black-bound");
		final Item regular = item(ItemRarity.RARE);
		player.getSlot("bag").add(regular);
		assertEquals(regular.getValue(), arenaBuyer.getCharge(request(1), player));
		final Item black = manager().getItem("czarna tarcza", ItemCreationContext.restore());
		black.setBoundTo(player.getName());
		player.getSlot("bag").add(black);
		player.setQuest("ultimate_collector", "done");
		final ItemParserResult blackRequest = new ItemParserResult(true, "czarna tarcza", 1,
				Collections.<String>emptySet());
		assertEquals(0, arenaBuyer.getCharge(blackRequest, player));
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
		assertEquals(sword.getValue(), MoneyUtils.getTotalMoneyInCopper(player));
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
