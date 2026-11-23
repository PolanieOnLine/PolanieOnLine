/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.LinkedHashMap;
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
		player.getSlot("bag").setCapacity(30);
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

	private Map<String, Integer> price(final Object... resources) {
		final Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (int index = 0; index < resources.length; index += 2) result.put((String) resources[index], (Integer) resources[index + 1]);
		return result;
	}

	private Map<String, StackableItem> fund(final Player player, final Map<String, Integer> price, final int surplus) {
		final Map<String, StackableItem> result = new LinkedHashMap<String, StackableItem>();
		for (final Map.Entry<String, Integer> entry : price.entrySet()) result.put(entry.getKey(), resource(player, entry.getKey(), entry.getValue() + surplus));
		return result;
	}

	private void assertUntouched(final Map<String, StackableItem> stacks, final Map<String, Integer> price) {
		for (final Map.Entry<String, StackableItem> entry : stacks.entrySet()) assertEquals(entry.getKey(), (int) price.get(entry.getKey()), entry.getValue().getQuantity());
	}

	private Map<String, Integer> ordinaryPrice(final ItemRarity rarity) {
		switch (rarity) {
		case RARE:
			return price("sztabka srebra", 64, "sztabka złota", 32, "kieł tygrysi", 24, "pazury tygrysie", 24, "skóra tygrysa", 16, "money", 80000);
		case EPIC:
			return price("sztabka złota", 64, "sztabka platyny", 32, "pióro anioła", 24, "pióro archanioła", 16, "kieł smoka", 16, "money", 200000);
		case LEGENDARY:
			return price("sztabka mithrilu", 48, "sztabka czarnego żelaza", 32, "pióro serafina", 24, "pióro cherubina", 16, "pióro lilith", 16, "kieł złotej kostuchy", 1, "money", 400000);
		default:
			return price("sztabka żelaza", 80, "kieł wilczy", 32, "pazury wilcze", 32, "skóra zwierzęca", 24, "money", 20000);
		}
	}

	@Test public void repeatPricesRequireTheFullRaritySpecificIngotsAndMonsterMaterials() {
		for (final ItemRarity rarity : ItemRarity.values()) {
			final Player player = PlayerTestHelper.createPlayer("price_balance");
			final Item item = item(player, rarity, "accuracy_bonus");
			item.put(ItemAffixRerollService.ATTRIBUTE, "locked", "accuracy_bonus");
			assertEquals(rarity.toString(), ordinaryPrice(rarity), ItemAffixRerollService.cost(item, "accuracy_bonus"));
		}
	}

	@Test public void keepingFirstRollUnlocksMaterialPaymentForEveryItemRarity() {
		for (final ItemRarity rarity : ItemRarity.values()) {
			final Player player = PlayerTestHelper.createPlayer("payment_reroller");
			final Item item = item(player, rarity, "accuracy_bonus");
			final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
			final StackableItem stones = resource(player, ItemAffixRerollService.STONE, 4);
			keep(service, player, item, "accuracy_bonus");
			final Map<String, StackableItem> materials = fund(player, ordinaryPrice(rarity), 1);
			keep(service, player, item, "accuracy_bonus");
			assertEquals(rarity.toString(), 3, stones.getQuantity());
			for (final StackableItem stack : materials.values()) assertEquals(rarity.toString(), 1, stack.getQuantity());
		}
	}

	@Test public void legendaryAffixRepeatsChargeTripleMaterialsButOnlyOneRareFang() {
		final Player player = PlayerTestHelper.createPlayer("legendary_payment");
		final Item item = item(player, ItemRarity.LEGENDARY, "legendary_deep_wounds");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 3);
		keep(service, player, item, "legendary_deep_wounds");
		final Map<String, Integer> price = price("sztabka mithrilu", 144, "sztabka czarnego żelaza", 96, "pióro serafina", 72, "pióro cherubina", 48, "pióro lilith", 48, "kieł złotej kostuchy", 1, "money", 1200000);
		assertEquals(price, ItemAffixRerollService.cost(item, "legendary_deep_wounds"));
		final Map<String, StackableItem> materials = fund(player, price, 1);
		keep(service, player, item, "legendary_deep_wounds");
		assertEquals(0, service.stones(player));
		for (final StackableItem stack : materials.values()) assertEquals(1, stack.getQuantity());
	}

	@Test public void missingLastResourceDoesNotPartiallyConsumeAnything() {
		final Player player = PlayerTestHelper.createPlayer("missing_payment");
		final Item item = item(player, ItemRarity.RARE, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		keep(service, player, item, "accuracy_bonus");
		final Map<String, Integer> price = new LinkedHashMap<String, Integer>(ItemAffixRerollService.cost(item, "accuracy_bonus"));
		price.put("money", price.get("money") - 1);
		final Map<String, StackableItem> materials = fund(player, price, 0);
		final String token = service.select(player, item, "accuracy_bonus");
		assertEquals("MISSING_MATERIALS", service.roll(player, item, "accuracy_bonus", token));
		assertUntouched(materials, price);
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
		final Map<String, Integer> otherMaterials = new LinkedHashMap<String, Integer>(ItemAffixRerollService.cost(item, "accuracy_bonus"));
		final int ironCost = otherMaterials.remove("sztabka żelaza");
		final StackableItem nestedIron = resource(player, "sztabka żelaza", ironCost - 4);
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
		fund(player, otherMaterials, 0);
		assertEquals(ironCost, service.count(player, "sztabka żelaza"));
		assertTrue(service.canPay(player, item, "accuracy_bonus"));
		assertEquals("READY", service.priceStatus(player, item, "accuracy_bonus"));
		keep(service, player, item, "accuracy_bonus");
		assertEquals(0, service.count(player, "sztabka żelaza"));
		for (final String name : otherMaterials.keySet()) assertEquals(name, 0, service.count(player, name));
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
		final Map<String, Integer> price = ItemAffixRerollService.cost(item, "accuracy_bonus");
		final Map<String, StackableItem> materials = fund(player, price, 0);
		final String token = service.select(player, item, "accuracy_bonus");
		assertEquals("INVALID_REQUEST", service.roll(player, item, "accuracy_bonus", "old-token"));
		item.put("atk", 50);
		assertEquals("INVALID_REQUEST", service.roll(player, item, "accuracy_bonus", token));
		assertUntouched(materials, price);
		assertFalse(service.hasPending(item));
	}

	@Test public void paidRepeatedOfferResumesAfterRestartWithoutAnotherPayment() {
		final Player player = PlayerTestHelper.createPlayer("resume_material_payment");
		final Item item = item(player, ItemRarity.COMMON, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		keep(service, player, item, "accuracy_bonus");
		fund(player, ItemAffixRerollService.cost(item, "accuracy_bonus"), 0);
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
		fund(player, ItemAffixRerollService.cost(upgraded, "accuracy_bonus"), 0);
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
		final Map<String, Integer> price = ItemAffixRerollService.cost(item, "accuracy_bonus");
		final Map<String, StackableItem> materials = fund(player, price, 0);
		assertEquals("NO_REPLACEMENT", service.roll(player, item, "accuracy_bonus", service.select(player, item, "accuracy_bonus")));
		assertUntouched(materials, price);
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
		final Map<String, Integer> price = ItemAffixRerollService.cost(item, "accuracy_bonus");
		fund(player, price, 0);
		assertEquals("OFFER", service.roll(player, item, "accuracy_bonus", service.select(player, item, "accuracy_bonus")));
		for (final Map.Entry<String, Integer> entry : price.entrySet()) {
			assertEquals(entry.getValue().toString(), ItemAffixRerollService.state(item).get("paid." + entry.getKey()));
		}
		assertFalse(ItemAffixRerollService.state(item).containsKey("paid." + ItemAffixRerollService.STONE));
		assertEquals(ordinaryPrice(ItemRarity.COMMON), ItemAffixRerollService.quotedCost(item, "accuracy_bonus"));
	}

	@Test public void savedOfferKeepsItsPreviouslyPaidPriceAfterBalanceChanges() {
		final Player player = PlayerTestHelper.createPlayer("previous_balance_payment");
		final Item item = item(player, ItemRarity.COMMON, "accuracy_bonus");
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(42));
		resource(player, ItemAffixRerollService.STONE, 1);
		assertEquals("OFFER", service.roll(player, item, "accuracy_bonus", service.select(player, item, "accuracy_bonus")));
		final Map<String, String> saved = ItemAffixRerollService.state(item);
		saved.remove("paid." + ItemAffixRerollService.STONE);
		saved.put("paid.sztabka żelaza", "10");
		saved.put("paid.kieł wilczy", "10");
		saved.put("paid.money", "5000");
		ItemAffixRerollService.replaceState(item, saved);
		final Item restored = new Item(item);
		item.getContainerSlot().remove(item.getID());
		player.getSlot("bag").add(restored);
		final ItemAffixRerollService restarted = new ItemAffixRerollService(new Random(17));
		assertEquals("OFFER", restarted.roll(player, restored, "accuracy_bonus", ""));
		assertEquals(price("sztabka żelaza", 10, "kieł wilczy", 10, "money", 5000), ItemAffixRerollService.quotedCost(restored, "accuracy_bonus"));
		assertEquals(ordinaryPrice(ItemRarity.COMMON), ItemAffixRerollService.cost(restored, "accuracy_bonus"));
		assertEquals(0, restarted.stones(player));
		assertEquals(0, restarted.count(player, "money"));
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
