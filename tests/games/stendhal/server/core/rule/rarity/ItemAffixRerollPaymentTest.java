/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;
import marauroa.common.game.RPSlot;

public class ItemAffixRerollPaymentTest {
	@BeforeClass public static void setup() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	private Item item(final Player player, final ItemRarity rarity, final String id) {
		final Item item = new Item("payment test sword", "sword", "test", Collections.singletonMap("atk", "12"));
		item.setEquipableSlots(Collections.singletonList("bag"));
		item.setRarity(rarity);
		final ItemAffixDefinition definition = ItemAffixRerollService.definition(id);
		assertTrue(definition.apply(item, new Random(2)));
		ItemAffixState.record(item, definition);
		player.equipToInventoryOnly(item);
		return item;
	}

	private StackableItem resource(final Player player, final String name, final int quantity) {
		final StackableItem resource = new StackableItem(name, "money".equals(name) ? "money" : "resource",
				ItemAffixRerollService.STONE.equals(name) ? "transformation_stone" : "test", null);
		resource.setQuantity(quantity);
		player.getSlot("bag").add(resource);
		return resource;
	}

	private void keep(final ItemAffixRerollService service, final Player player, final Item item, final String id) {
		assertEquals("OFFER", service.roll(player, item, id, service.select(player, item, id)));
		assertEquals("KEPT", service.finish(player, item, ItemAffixRerollService.state(item).get("token"), false));
	}

	@Test public void keepingFirstRollUnlocksMaterialPaymentForEveryItemRarity() {
		final String[] ingots = {"sztabka żelaza", "sztabka srebra", "sztabka złota", "sztabka mithrilu"};
		final String[] loot = {"kieł wilczy", "kieł tygrysi", "pióro anioła", "pióro serafina"};
		final int[] quantities = {10, 5, 3, 2};
		final int[] money = {5000, 20000, 50000, 100000};
		for (final ItemRarity rarity : ItemRarity.values()) {
			final Player player = PlayerTestHelper.createPlayer("payment_reroller");
			final Item item = item(player, rarity, "accuracy_bonus");
			final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
			final StackableItem stones = resource(player, ItemAffixRerollService.STONE, 4);
			keep(service, player, item, "accuracy_bonus");
			final int index = rarity.ordinal();
			final StackableItem ingot = resource(player, ingots[index], quantities[index] + 1);
			final StackableItem monster = resource(player, loot[index], quantities[index] + 1);
			final StackableItem coins = resource(player, "money", money[index] + 1);
			keep(service, player, item, "accuracy_bonus");
			assertEquals(rarity.toString(), 3, stones.getQuantity());
			assertEquals(rarity.toString(), 1, ingot.getQuantity());
			assertEquals(rarity.toString(), 1, monster.getQuantity());
			assertEquals(rarity.toString(), 1, coins.getQuantity());
		}
	}

	@Test public void legendaryAffixRepeatsChargeTripleMaterialsAndMoneyWithoutStones() {
		final Player player = PlayerTestHelper.createPlayer("legendary_payment");
		final Item item = item(player, ItemRarity.LEGENDARY, "legendary_deep_wounds");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 3);
		keep(service, player, item, "legendary_deep_wounds");
		final StackableItem ingot = resource(player, "sztabka mithrilu", 7);
		final StackableItem feather = resource(player, "pióro serafina", 7);
		final StackableItem coins = resource(player, "money", 300001);
		keep(service, player, item, "legendary_deep_wounds");
		assertEquals(0, service.stones(player));
		assertEquals(1, ingot.getQuantity());
		assertEquals(1, feather.getQuantity());
		assertEquals(1, coins.getQuantity());
	}

	@Test public void missingLastResourceDoesNotPartiallyConsumeAnything() {
		final Player player = PlayerTestHelper.createPlayer("missing_payment");
		final Item item = item(player, ItemRarity.RARE, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		keep(service, player, item, "accuracy_bonus");
		final StackableItem ingot = resource(player, "sztabka srebra", 5);
		final StackableItem tooth = resource(player, "kieł tygrysi", 5);
		final StackableItem coins = resource(player, "money", 19999);
		final String token = service.select(player, item, "accuracy_bonus");
		assertEquals("MISSING_MATERIALS", service.roll(player, item, "accuracy_bonus", token));
		assertEquals(5, ingot.getQuantity());
		assertEquals(5, tooth.getQuantity());
		assertEquals(19999, coins.getQuantity());
		assertFalse(service.hasPending(item));
	}

	@Test public void paymentCountsOnlyCarriedOwnedStackableResourcesIncludingNestedStacks() {
		final Player player = PlayerTestHelper.createPlayer("nested_payment");
		player.getSlot("bag").setCapacity(30);
		final Item item = item(player, ItemRarity.COMMON, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		keep(service, player, item, "accuracy_bonus");
		final Item bag = new Item("payment bag", "container", "test", Collections.emptyMap());
		bag.addSlot(new RPSlot("content"));
		player.getSlot("bag").add(bag);
		final StackableItem nestedIron = resource(player, "sztabka żelaza", 6);
		player.getSlot("bag").remove(nestedIron.getID());
		bag.getSlot("content").add(nestedIron);
		final StackableItem boundIron = resource(player, "sztabka żelaza", 4);
		boundIron.setBoundTo(player.getName());
		final StackableItem foreignIron = resource(player, "sztabka żelaza", 90);
		foreignIron.setBoundTo("someone_else");
		final StackableItem bankIron = resource(player, "sztabka żelaza", 70);
		player.getSlot("bag").remove(bankIron.getID());
		player.getSlot("bank").add(bankIron);
		final Item fakeIron = new Item("sztabka żelaza", "resource", "test", Collections.emptyMap());
		player.getSlot("bag").add(fakeIron);
		final StackableItem wrongClass = new StackableItem("sztabka żelaza", "food", "test", null);
		wrongClass.setQuantity(80);
		player.getSlot("bag").add(wrongClass);
		resource(player, "kieł wilczy", 10);
		resource(player, "money", 5000);
		assertEquals(10, service.count(player, "sztabka żelaza"));
		assertTrue(service.canPay(player, item, "accuracy_bonus"));
		assertEquals("READY", service.priceStatus(player, item, "accuracy_bonus"));
		keep(service, player, item, "accuracy_bonus");
		assertEquals(0, service.count(player, "sztabka żelaza"));
		assertEquals(0, service.count(player, "kieł wilczy"));
		assertEquals(0, service.count(player, "money"));
		assertEquals(90, foreignIron.getQuantity());
		assertEquals(70, bankIron.getQuantity());
		assertEquals(80, wrongClass.getQuantity());
		assertSame(player, fakeIron.getContainer());
		assertSame(player, bag.getContainer());
	}

	@Test public void firstUnlockRejectsWrongStoneClassAndSubclass() {
		final Player player = PlayerTestHelper.createPlayer("fake_stone_payment");
		final Item item = item(player, ItemRarity.COMMON, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		final StackableItem fake = new StackableItem(ItemAffixRerollService.STONE, "resource", "iron", null);
		fake.setQuantity(10);
		player.getSlot("bag").add(fake);
		assertEquals(0, service.count(player, ItemAffixRerollService.STONE));
		assertFalse(service.canPay(player, item, "accuracy_bonus"));
		assertEquals("MISSING_STONE", service.priceStatus(player, item, "accuracy_bonus"));
		final String token = service.select(player, item, "accuracy_bonus");
		assertEquals("MISSING_STONE", service.roll(player, item, "accuracy_bonus", token));
		assertEquals(10, fake.getQuantity());
	}

	@Test public void staleRepeatedRollRequestCannotChargeAnyMaterials() {
		final Player player = PlayerTestHelper.createPlayer("stale_material_payment");
		final Item item = item(player, ItemRarity.EPIC, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		keep(service, player, item, "accuracy_bonus");
		final StackableItem ingot = resource(player, "sztabka złota", 3);
		final StackableItem feather = resource(player, "pióro anioła", 3);
		final StackableItem coins = resource(player, "money", 50000);
		final String token = service.select(player, item, "accuracy_bonus");
		assertEquals("INVALID_REQUEST", service.roll(player, item, "accuracy_bonus", "old-token"));
		item.put("atk", 50);
		assertEquals("INVALID_REQUEST", service.roll(player, item, "accuracy_bonus", token));
		assertEquals(3, ingot.getQuantity());
		assertEquals(3, feather.getQuantity());
		assertEquals(50000, coins.getQuantity());
		assertFalse(service.hasPending(item));
	}

	@Test public void paidRepeatedOfferResumesAfterRestartWithoutAnotherPayment() {
		final Player player = PlayerTestHelper.createPlayer("resume_material_payment");
		final Item item = item(player, ItemRarity.COMMON, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		keep(service, player, item, "accuracy_bonus");
		resource(player, "sztabka żelaza", 10);
		resource(player, "kieł wilczy", 10);
		resource(player, "money", 5000);
		assertEquals("OFFER", service.roll(player, item, "accuracy_bonus", service.select(player, item, "accuracy_bonus")));
		final Map<String, String> paid = ItemAffixRerollService.state(item);
		final Item restored = new Item(item);
		item.getContainerSlot().remove(item.getID());
		player.getSlot("bag").add(restored);
		final ItemAffixRerollService restarted = new ItemAffixRerollService(new Random(17));
		assertEquals("OFFER", restarted.roll(player, restored, "accuracy_bonus", ""));
		assertEquals(paid, ItemAffixRerollService.state(restored));
		assertEquals(0, restarted.count(player, "money"));
		assertEquals("ACCEPTED", restarted.finish(player, restored, paid.get("token"), true));
		final String locked = ItemAffixRerollService.state(restored).get("locked");
		assertEquals("MISSING_MATERIALS", restarted.priceStatus(player, restored, locked));
		assertEquals("MISSING_MATERIALS", restarted.roll(player, restored, locked, restarted.select(player, restored, locked)));
	}

	@Test public void transferredSlotLockUsesCurrentRarityAndCannotBeUnlockedAgain() {
		final Player player = PlayerTestHelper.createPlayer("upgrade_material_payment");
		final Item item = item(player, ItemRarity.RARE, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 4);
		keep(service, player, item, "accuracy_bonus");
		final Item upgraded = new Item("upgraded payment sword", "sword", "test", Collections.singletonMap("atk", "20"));
		ItemRarityTransferSnapshot.apply(upgraded, ItemRarityTransferSnapshot.encode(item));
		upgraded.setRarity(ItemRarity.EPIC);
		item.getContainerSlot().remove(item.getID());
		player.getSlot("bag").add(upgraded);
		assertEquals("accuracy_bonus", ItemAffixRerollService.state(upgraded).get("locked"));
		assertEquals("", service.select(player, upgraded, "critical_chance"));
		assertEquals("MISSING_MATERIALS", service.priceStatus(player, upgraded, "accuracy_bonus"));
		resource(player, "sztabka złota", 3);
		resource(player, "pióro anioła", 3);
		resource(player, "money", 50000);
		keep(service, player, upgraded, "accuracy_bonus");
		assertEquals(3, service.stones(player));
		assertEquals(0, service.count(player, "money"));
	}

	@Test public void noEligibleReplacementChargesNeitherMaterialsNorMoney() {
		final Player player = PlayerTestHelper.createPlayer("no_replacement_payment");
		final Item item = new Item("unsupported equipment", "resource", "test", Collections.emptyMap());
		item.put(ItemAffixState.ATTRIBUTE, "accuracy_bonus", "0.05");
		item.put(ItemAffixRerollService.ATTRIBUTE, "locked", "accuracy_bonus");
		player.getSlot("bag").add(item);
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		final StackableItem iron = resource(player, "sztabka żelaza", 10);
		final StackableItem tooth = resource(player, "kieł wilczy", 10);
		final StackableItem coins = resource(player, "money", 5000);
		assertEquals("NO_REPLACEMENT", service.roll(player, item, "accuracy_bonus", service.select(player, item, "accuracy_bonus")));
		assertEquals(10, iron.getQuantity());
		assertEquals(10, tooth.getQuantity());
		assertEquals(5000, coins.getQuantity());
		assertFalse(service.hasPending(item));
	}

	@Test public void pendingOfferRecordsThePriceActuallyPaidInsteadOfTheNextRollPrice() {
		final Player player = PlayerTestHelper.createPlayer("quoted_material_payment");
		final Item item = item(player, ItemRarity.COMMON, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		assertEquals("OFFER", service.roll(player, item, "accuracy_bonus", service.select(player, item, "accuracy_bonus")));
		assertEquals("1", ItemAffixRerollService.state(item).get("paid." + ItemAffixRerollService.STONE));
		assertEquals(Collections.singletonMap(ItemAffixRerollService.STONE, 1), ItemAffixRerollService.quotedCost(item, "accuracy_bonus"));
		assertEquals("KEPT", service.finish(player, item, ItemAffixRerollService.state(item).get("token"), false));
		assertEquals(Collections.singletonMap("locked", "accuracy_bonus"), ItemAffixRerollService.state(item));
		assertFalse(ItemAffixRerollService.quotedCost(item, "accuracy_bonus").containsKey(ItemAffixRerollService.STONE));
		resource(player, "sztabka żelaza", 10);
		resource(player, "kieł wilczy", 10);
		resource(player, "money", 5000);
		assertEquals("OFFER", service.roll(player, item, "accuracy_bonus", service.select(player, item, "accuracy_bonus")));
		assertEquals("10", ItemAffixRerollService.state(item).get("paid.sztabka żelaza"));
		assertEquals("10", ItemAffixRerollService.state(item).get("paid.kieł wilczy"));
		assertEquals("5000", ItemAffixRerollService.state(item).get("paid.money"));
		assertFalse(ItemAffixRerollService.state(item).containsKey("paid." + ItemAffixRerollService.STONE));
		assertEquals(Integer.valueOf(5000), ItemAffixRerollService.quotedCost(item, "accuracy_bonus").get("money"));
		assertEquals(3, ItemAffixRerollService.quotedCost(item, "accuracy_bonus").size());
	}

	@Test public void legacyPendingOfferQuotesOriginalStonePayment() {
		final Player player = PlayerTestHelper.createPlayer("legacy_quoted_payment");
		final Item item = item(player, ItemRarity.LEGENDARY, "legendary_deep_wounds");
		item.put(ItemAffixRerollService.ATTRIBUTE, "locked", "legendary_deep_wounds");
		item.put(ItemAffixRerollService.ATTRIBUTE, "old_id", "legendary_deep_wounds");
		item.put(ItemAffixRerollService.ATTRIBUTE, "token", "saved-offer-token");
		assertEquals(Collections.singletonMap(ItemAffixRerollService.STONE, 3), ItemAffixRerollService.quotedCost(item, ""));
	}
}
