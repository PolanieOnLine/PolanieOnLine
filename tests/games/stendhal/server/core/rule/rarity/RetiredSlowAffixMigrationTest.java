package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.transformer.ItemTransformer;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class RetiredSlowAffixMigrationTest {
	private static final String RESISTANCE = "resist_heavy";
	private static final String BINDING = "legendary_binding_strike";

	@BeforeClass public static void setup() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	private Item item(final String name, final ItemRarity rarity) {
		return SingletonRepository.getEntityManager().getItem(name,
				ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT).withRarity(rarity)
						.randomizeModifiers(false).generateAffixes(false).build());
	}

	private void affix(final Item item, final String id, final String value) {
		item.put(id, value);
		item.put(ItemAffixState.ATTRIBUTE, id, value);
	}

	@Test public void retiredAffixesAreNotInEitherRollPool() {
		for (final ItemAffixDefinition definition : ItemAffixRegistry.getInstance().getDefinitions())
			assertNotEquals(RESISTANCE, definition.getId());
		for (final ItemAffixDefinition definition : LegendaryItemAffixRegistry.getInstance().getDefinitions())
			assertNotEquals(BINDING, definition.getId());
	}

	@Test public void regularAndLegendarySlotsAreReplacedOnceWithoutChangingOtherRollsOrOwnership() {
		for (final boolean legendary : new boolean[] {false, true}) {
			final Item item = item(legendary ? "zatruty bicz" : "pas skórzany",
					legendary ? ItemRarity.LEGENDARY : ItemRarity.RARE);
			final String retired = legendary ? BINDING : RESISTANCE;
			affix(item, retired, legendary ? "1.0" : "0.12");
			affix(item, "accuracy_bonus", "7.0");
			item.setBoundTo("owner");
			item.setUpgradeLevel(2);
			ItemAffixState.setSeed(item, 121L);
			final ItemRarity rarity = item.getRarity();
			final int upgrade = item.getUpgradeLevel();
			final Map<String, Double> modifiers = new LinkedHashMap<>(item.getRarityModifiers());
			ItemAffixBalanceService.enforce(item);
			assertFalse(item.has(retired));
			assertFalse(ItemAffixState.has(item, retired));
			assertEquals(2, ItemAffixState.getValues(item).size());
			assertEquals("7.0", ItemAffixState.getValues(item).get("accuracy_bonus"));
			assertEquals("owner", item.getBoundTo());
			assertEquals(rarity, item.getRarity());
			assertEquals(upgrade, item.getUpgradeLevel());
			assertEquals(modifiers, item.getRarityModifiers());
			for (final String id : ItemAffixState.getValues(item).keySet()) if (!"accuracy_bonus".equals(id)) {
				assertNotNull(legendary ? LegendaryItemAffixRegistry.getInstance().get(id)
						: ItemAffixRegistry.getInstance().get(id));
			}
			final Map<String, String> after = ItemAffixState.getValues(item);
			ItemAffixBalanceService.enforce(item);
			assertEquals(after, ItemAffixState.getValues(item));
		}
	}

	@Test public void restoreMigratesOldMetadataAndLegacyAttributesAndKeepsTheReplacementOnLaterLogins() {
		for (final boolean metadata : new boolean[] {true, false}) {
			Item item = item("pas skórzany", ItemRarity.RARE);
			item.put("id", 44);
			item.put(RESISTANCE, 0.11);
			if (metadata) item.put(ItemAffixState.ATTRIBUTE, RESISTANCE, "0.11");
			item = new ItemTransformer().transform(new RPObject(item));
			assertFalse(item.has(RESISTANCE));
			assertEquals(1, ItemAffixState.getValues(item).size());
			final Map<String, String> after = ItemAffixState.getValues(item);
			for (int login = 0; login < 3; login++) {
				item = new ItemTransformer().transform(new RPObject(item));
				assertEquals(after, ItemAffixState.getValues(item));
			}
		}
	}

	@Test public void paidOfferKeepsItsPriceAndChoiceWhenItsOldSlotIsRetired() {
		for (final boolean accept : new boolean[] {true, false}) {
			final Player player = PlayerTestHelper.createPlayer("retired_paid_" + accept);
			Item item = item("pas skórzany", ItemRarity.RARE);
			affix(item, RESISTANCE, "0.12");
			assertTrue(player.equipToInventoryOnly(item));
			final StackableItem stones = (StackableItem) SingletonRepository.getEntityManager().getItem(ItemAffixRerollService.STONE);
			stones.setQuantity(3);
			assertTrue(player.equipToInventoryOnly(stones));
			final ItemAffixRerollService service = new ItemAffixRerollService(new Random(3));
			assertEquals("OFFER", service.roll(player, item, RESISTANCE, service.select(player, item, RESISTANCE)));
			final Map<String, String> before = ItemAffixRerollService.state(item);
			ItemAffixBalanceService.enforce(item);
			final Map<String, String> after = ItemAffixRerollService.state(item);
			assertNotEquals(RESISTANCE, after.get("old_id"));
			assertEquals(before.get("token"), after.get("token"));
			assertEquals(before.get("new_id"), after.get("new_id"));
			assertEquals(before.get("new_value"), after.get("new_value"));
			assertEquals(before.get("paid." + ItemAffixRerollService.STONE), after.get("paid." + ItemAffixRerollService.STONE));
			assertTrue(service.validPending(player, item));
			for (int login = 0; login < 3; login++) {
				final Item restored = new ItemTransformer().transform(new RPObject(item));
				item.getContainerSlot().remove(item.getID());
				assertTrue(player.equipToInventoryOnly(restored));
				item = restored;
				assertEquals(after, ItemAffixRerollService.state(item));
				assertTrue(service.validPending(player, item));
			}
			assertEquals(accept ? "ACCEPTED" : "KEPT", service.finish(player, item, after.get("token"), accept));
			assertFalse(item.has(RESISTANCE));
			assertEquals(2, service.stones(player));
		}
	}

	@Test public void replacementAlsoReachesEquipmentInsideSavedContainers() {
		final Item equipment = item("pas skórzany", ItemRarity.RARE);
		equipment.put("id", 51);
		affix(equipment, RESISTANCE, "0.10");
		final Item box = item("skrzynka", ItemRarity.COMMON);
		box.put("id", 52);
		box.addSlot(new RPSlot("content"));
		box.getSlot("content").add(equipment);
		final Item restored = new ItemTransformer().transform(new RPObject(box));
		final Item nested = (Item) restored.getSlot("content").iterator().next();
		assertFalse(nested.has(RESISTANCE));
		assertEquals(1, ItemAffixState.getValues(nested).size());
	}

	@Test public void permanentPrzemirLockFollowsTheReplacement() {
		final Item item = item("pas skórzany", ItemRarity.RARE);
		affix(item, RESISTANCE, "0.12");
		affix(item, "resist_poisoned", "0.10");
		item.put(ItemAffixRerollService.ATTRIBUTE, "locked", RESISTANCE);
		ItemAffixBalanceService.enforce(item);
		final String locked = ItemAffixRerollService.state(item).get("locked");
		assertNotEquals(RESISTANCE, locked);
		assertTrue(ItemAffixState.has(item, locked));
		assertEquals(java.util.Collections.singletonList(locked), new ItemAffixRerollService(new Random(1)).affixes(item));
		assertFalse(ItemAffixRerollService.cost(item, locked).containsKey(ItemAffixRerollService.STONE));
	}

	@Test public void snapshotsAndUpgradesCannotReintroduceRetiredSlots() {
		final Item source = item("zatruty bicz", ItemRarity.LEGENDARY);
		affix(source, BINDING, "1.0");
		source.put(ItemAffixRerollService.ATTRIBUTE, "locked", BINDING);
		final Item restored = item("zatruty bicz", ItemRarity.COMMON);
		ItemRaritySnapshot.restore(restored, ItemRaritySnapshot.encode(source));
		assertFalse(ItemAffixState.has(restored, BINDING));
		assertEquals(1, ItemAffixState.getValues(restored).size());
		assertTrue(ItemAffixState.has(restored, ItemAffixRerollService.state(restored).get("locked")));
		final Item transferred = item("zatruty bicz", ItemRarity.COMMON);
		ItemRarityTransferSnapshot.apply(transferred, ItemRarityTransferSnapshot.encode(source));
		assertFalse(ItemAffixState.has(transferred, BINDING));
		assertEquals(1, ItemAffixState.getValues(transferred).size());
		assertTrue(ItemAffixState.has(transferred, ItemAffixRerollService.state(transferred).get("locked")));
	}

	@Test public void retiredPaidCandidateIsReplacedWithoutTakingAnotherStone() {
		final Player player = PlayerTestHelper.createPlayer("retired_paid_candidate");
		final Item item = item("pas skórzany", ItemRarity.RARE);
		affix(item, "resist_poisoned", "0.10");
		assertTrue(player.equipToInventoryOnly(item));
		final StackableItem stones = (StackableItem) SingletonRepository.getEntityManager().getItem(ItemAffixRerollService.STONE);
		stones.setQuantity(2); player.equipToInventoryOnly(stones);
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(5));
		assertEquals("OFFER", service.roll(player, item, "resist_poisoned", service.select(player, item, "resist_poisoned")));
		final Map<String, String> offer = ItemAffixRerollService.state(item);
		offer.keySet().removeIf(key -> key.startsWith("patch."));
		offer.put("new_id", RESISTANCE); offer.put("new_value", "0.12");
		offer.put("patch.resist_poisoned", ""); offer.put("patch." + RESISTANCE, "0.12");
		ItemAffixRerollService.replaceState(item, offer);
		ItemAffixBalanceService.enforce(item);
		assertNotEquals(RESISTANCE, ItemAffixRerollService.state(item).get("new_id"));
		assertTrue(service.validPending(player, item));
		assertEquals("ACCEPTED", service.finish(player, item, offer.get("token"), true));
		assertFalse(item.has(RESISTANCE)); assertEquals(1, service.stones(player));
	}

	@Test public void bothRetiredSlotsKeepTheirSeparateCategories() {
		final Item item = item("zatruty bicz", ItemRarity.LEGENDARY);
		affix(item, RESISTANCE, "0.10"); affix(item, BINDING, "1.0");
		affix(item, "accuracy_bonus", "7.0");
		ItemAffixBalanceService.enforce(item);
		assertFalse(item.has(RESISTANCE)); assertFalse(item.has(BINDING));
		int ordinary = 0, legendary = 0;
		for (final String id : ItemAffixState.getValues(item).keySet()) {
			if (ItemAffixRegistry.getInstance().get(id) != null) ordinary++;
			if (LegendaryItemAffixRegistry.getInstance().get(id) != null) legendary++;
		}
		assertEquals(2, ordinary); assertEquals(1, legendary);
	}

	@Test public void migratingDoesNotValidateAnOfferForAnItemChangedAfterPayment() {
		final Player player = PlayerTestHelper.createPlayer("retired_invalid_offer");
		final Item item = item("pas skórzany", ItemRarity.RARE);
		affix(item, RESISTANCE, "0.10"); player.equipToInventoryOnly(item);
		final StackableItem stone = (StackableItem) SingletonRepository.getEntityManager().getItem(ItemAffixRerollService.STONE);
		player.equipToInventoryOnly(stone);
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(3));
		assertEquals("OFFER", service.roll(player, item, RESISTANCE, service.select(player, item, RESISTANCE)));
		final String token = ItemAffixRerollService.state(item).get("token");
		item.put("infostring", "changed after paying");
		ItemAffixBalanceService.enforce(item);
		assertFalse(service.validPending(player, item));
		assertEquals("CHANGED_ITEM", service.finish(player, item, token, true));
		assertEquals("KEPT", service.finish(player, item, token, false));
		assertFalse(item.has(RESISTANCE));
	}

	@Test public void paidFlatAttackOnJewelleryWithoutBaseAttackIsEffectiveImmediatelyAfterAcceptance() {
		final Player player = PlayerTestHelper.createPlayer("retired_paid_attack");
		final Item item = item("pierścień niewidzialności", ItemRarity.RARE);
		if (item.has("atk")) item.remove("atk");
		affix(item, RESISTANCE, "0.10"); player.equipToInventoryOnly(item);
		final StackableItem stones = (StackableItem) SingletonRepository.getEntityManager().getItem(ItemAffixRerollService.STONE);
		stones.setQuantity(2); player.equipToInventoryOnly(stones);
		final ItemAffixRerollService service = new ItemAffixRerollService(new Random(8));
		assertEquals("OFFER", service.roll(player, item, RESISTANCE, service.select(player, item, RESISTANCE)));
		final Map<String, String> offer = ItemAffixRerollService.state(item);
		offer.keySet().removeIf(key -> key.startsWith("patch."));
		offer.put("new_id", "flat_attack_bonus"); offer.put("new_value", "2");
		offer.put("patch." + RESISTANCE, ""); offer.put("patch.flat_attack_bonus", "2"); offer.put("patch.atk", "2");
		ItemAffixRerollService.replaceState(item, offer);
		ItemAffixBalanceService.enforce(item);
		assertTrue(service.validPending(player, item));
		assertEquals("2", ItemAffixRerollService.state(item).get("patch.atk"));
		assertEquals("ACCEPTED", service.finish(player, item, offer.get("token"), true));
		assertEquals(2, item.getInt("atk"));
		assertEquals("2", ItemAffixState.getValues(item).get("flat_attack_bonus"));
		assertEquals(1, service.stones(player));
	}
}
