/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.item.upgrade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.entity.item.Container;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Weapon;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import marauroa.common.game.RPObject.ID;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class ItemUpgradeServiceTest {
	@BeforeClass
	public static void setUpClasses() {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void requirementsPreserveLegacyEconomyForLevelsOneAndEight() {
		final ItemUpgradeService service = service(0);
		assertEquals(materials("polano", 5, "szafir", 1),
				service.getMaterialRequirements(1));
		assertEquals(materials("polano", 15, "ametyst", 10, "rubin", 8,
				"obsydian", 5, "diament", 2),
				service.getMaterialRequirements(8));
		assertEquals(8, service.getMaximumConfiguredLevel());
	}

	@Test
	public void previewRejectsItemsWithoutLimitAndAtMaximumLevel() {
		final Player player = player("limits");
		final Item withoutLimit = weapon("legacy", 10, 0, 0);
		player.equipToInventoryOnly(withoutLimit);
		assertSame(ItemUpgradeResult.Status.NOT_UPGRADEABLE,
				service(0).createPreview(player, withoutLimit).getBlockingStatus());

		final Item maximum = weapon("maximum", 10, 2, 2);
		player.equipToInventoryOnly(maximum);
		assertSame(ItemUpgradeResult.Status.MAX_LEVEL,
				service(0).createPreview(player, maximum).getBlockingStatus());
	}

	@Test
	public void utilityItemWithLegacyUpgradeLimitIsNotACandidate() {
		final Player player = player("utility_item");
		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, "3");
		final Item potion = new Item("test potion", "drink", "potion",
				attributes);
		potion.setEquipableSlots(Collections.singletonList("bag"));
		player.equipToInventoryOnly(potion);

		final ItemUpgradeService service = service(0);
		assertFalse(potion.hasUpgradeableCombatStats());
		assertFalse(potion.isUpgradeable());
		assertFalse(service.findUpgradeCandidates(player).contains(potion));
		assertSame(ItemUpgradeResult.Status.NOT_UPGRADEABLE,
				service.createPreview(player, potion).getBlockingStatus());
	}

	@Test
	public void previewUsesSameWeaponArmourAndRangedStatFunctionsAsItem() {
		final Player player = player("statistics");
		final Weapon weapon = weapon("range weapon", 12, 0, 3);
		weapon.put("ratk", 17);
		weapon.put("def", 4);
		weapon.put("damage_min", 9);
		weapon.put("damage_max", 15);
		player.equipToInventoryOnly(weapon);

		final ItemUpgradePreview preview = service(0).createPreview(player, weapon);
		assertEquals(Integer.valueOf(12), preview.getCurrentStats().getValues().get("atk"));
		assertEquals(Integer.valueOf(13), preview.getUpgradedStats().getValues().get("atk"));
		assertEquals(Integer.valueOf(18), preview.getUpgradedStats().getValues().get("ratk"));
		assertEquals(Integer.valueOf(10), preview.getUpgradedStats().getValues().get("damage_min"));
		assertEquals(Integer.valueOf(16), preview.getUpgradedStats().getValues().get("damage_max"));
		assertEquals(Integer.valueOf(5), preview.getUpgradedStats().getValues().get("def"));
		assertEquals(1 * (17 + 4) * 3000,
				preview.getRequirements().getFee());
	}

	@Test
	public void armourPreviewShowsOnlyItsRelevantDefenseChange() {
		final Player player = player("armour_stats");
		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put("def", "24");
		attributes.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, "3");
		final Item armour = new Item("test armour", "armor", "test", attributes);
		armour.setEquipableSlots(Arrays.asList("bag", "armor"));
		player.equipToInventoryOnly(armour);

		final ItemUpgradePreview preview = service(0).createPreview(player, armour);
		assertEquals(1, preview.getCurrentStats().getValues().size());
		assertEquals(Integer.valueOf(24),
				preview.getCurrentStats().getValues().get("def"));
		assertEquals(Integer.valueOf(25),
				preview.getUpgradedStats().getValues().get("def"));
	}

	@Test
	public void baseSuccessChanceFollowsLegacyLevelsAndFloor() {
		final ItemUpgradeService service = service(0);
		final Weapon item = weapon("chance", 10, 0, 8);
		assertEquals(1.0, service.calculateBaseSuccessProbability(item), 0.0);
		item.setUpgradeLevel(4);
		assertEquals(0.6, service.calculateBaseSuccessProbability(item), 0.0);
		item.setUpgradeLevel(7);
		assertEquals(0.3, service.calculateBaseSuccessProbability(item), 0.0001);
	}

	@Test
	public void costPreservesMithrilAndQuestExceptions() {
		final Player player = player("mithril");
		final Weapon mithril = weapon("miecz z mithrilu", 20, 0, 1);
		assertEquals(5000000, service(0).calculateUpgradeFee(player, mithril));

		final Weapon dagger = weapon("sztylecik z mithrilu", 10, 0, 2);
		assertEquals(174000, service(0).calculateUpgradeFee(player, dagger));
		player.setQuest("ciupaga_trzy_wasy", "done");
		assertEquals(121800, service(0).calculateUpgradeFee(player, dagger));
	}

	@Test
	public void missingMoneyAndMaterialsAreDistinguished() {
		final Player player = player("requirements");
		final Weapon item = weapon("requirements", 10, 0, 3);
		player.equipToInventoryOnly(item);
		final ItemUpgradeService service = service(0);
		assertSame(ItemUpgradeResult.Status.NOT_ENOUGH_MONEY,
				service.createPreview(player, item).getBlockingStatus());

		PlayerTestHelper.equipWithMoney(player,
				service.calculateUpgradeFee(player, item));
		final ItemUpgradePreview materialsMissing = service.createPreview(player, item);
		assertSame(ItemUpgradeResult.Status.MISSING_RESOURCES,
				materialsMissing.getBlockingStatus());
		assertFalse(materialsMissing.isUpgradeAllowed());
	}

	@Test
	public void successfulUpgradeKeepsRarityAffixesSeedAndSignature() {
		for (final ItemRarity rarity : ItemRarity.values()) {
			final Player player = player("identity_" + rarity.getId());
			final Weapon item = weapon("identity item", 20, 0, 3);
			item.setRarity(rarity);
			item.setRarityModifier("atk", 1.23);
			ItemAffixState.setSeed(item, 12345L);
			item.put(ItemAffixState.ATTRIBUTE, "regular_affix", "7");
			item.put(ItemAffixState.ATTRIBUTE, "legendary_signature", "11");
			player.equipToInventoryOnly(item);
			final ItemUpgradeService service = service(0);
			provideRequirements(player, item, service);
			final Map<String, String> affixes = ItemAffixState.getValues(item);
			final Map<String, Double> modifiers = item.getRarityModifiers();

			final ItemUpgradeResult result = service.performUpgrade(player, item,
					service.createPreview(player, item).getRequestToken());
			assertSame(ItemUpgradeResult.Status.SUCCESS, result.getStatus());
			assertEquals(1, item.getUpgradeLevel());
			assertSame(rarity, item.getRarity());
			assertEquals(Long.valueOf(12345L), ItemAffixState.getSeed(item));
			assertEquals(affixes, ItemAffixState.getValues(item));
			assertEquals(modifiers, item.getRarityModifiers());
		}
	}

	@Test
	public void failureConsumesRequirementsRefundsFortyPercentAndDoesNotDowngrade() {
		final Player player = player("failure");
		final Weapon item = weapon("failure item", 10, 1, 3);
		player.equipToInventoryOnly(item);
		final ItemUpgradeService service = service(99);
		provideRequirements(player, item, service);
		final int fee = service.calculateUpgradeFee(player, item);

		final ItemUpgradePreview preview = service.createPreview(player, item);
		final ItemUpgradeResult result = service.performUpgrade(player, item,
				preview.getRequestToken());
		assertSame(ItemUpgradeResult.Status.FAILURE, result.getStatus());
		assertEquals(1, item.getUpgradeLevel());
		assertEquals((int) (fee * 0.4),
				games.stendhal.server.entity.item.money.MoneyUtils
						.getTotalMoneyInCopper(player));
		for (final String material : service.getMaterialRequirements(2).keySet()) {
			assertTrue(player.getAllEquipped(material).isEmpty());
		}
	}

	@Test
	public void karmaPreviewDoesNotConsumeAndExecutionUsesQuotedModifier() {
		final Player player = player("karma");
		player.addKarma(0.1);
		final Weapon item = weapon("karma item", 10, 1, 3);
		player.equipToInventoryOnly(item);
		final ItemUpgradeService service = service(0);
		provideRequirements(player, item, service);
		final double before = player.getKarma();

		final ItemUpgradePreview preview = service.createPreview(player, item);
		assertEquals(before, player.getKarma(), 0.0);
		assertTrue(preview.getKarmaModifier() > 0.0);
		assertEquals(service.calculateBaseSuccessProbability(item)
				+ preview.getKarmaModifier(), preview.getSuccessProbability(), 0.0001);

		service.performUpgrade(player, item, preview.getRequestToken());
		assertEquals(before - preview.getKarmaModifier(), player.getKarma(), 0.0001);
	}

	@Test
	public void oneUseTokenTargetsExactInstanceWhenNamesMatch() {
		final Player player = player("duplicates");
		final Weapon first = weapon("ten sam miecz", 10, 0, 3);
		final Weapon second = weapon("ten sam miecz", 30, 0, 3);
		player.equipToInventoryOnly(first);
		player.equipToInventoryOnly(second);
		final ItemUpgradeService service = service(0);
		provideRequirements(player, second, service);

		final ItemUpgradePreview preview = service.createPreview(player, second);
		assertSame(ItemUpgradeResult.Status.INVALID_ITEM,
				service.performUpgrade(player, first, preview.getRequestToken()).getStatus());
		assertEquals(0, first.getUpgradeLevel());
		assertEquals(0, second.getUpgradeLevel());

		final ItemUpgradePreview secondPreview = service.createPreview(player, second);
		assertSame(ItemUpgradeResult.Status.SUCCESS,
				service.performUpgrade(player, second,
						secondPreview.getRequestToken()).getStatus());
		assertEquals(0, first.getUpgradeLevel());
		assertEquals(1, second.getUpgradeLevel());
		assertSame(ItemUpgradeResult.Status.STALE_PREVIEW,
				service.performUpgrade(player, second,
						secondPreview.getRequestToken()).getStatus());
	}

	@Test
	public void movingItemAfterPreviewInvalidatesTheBoundAttempt() {
		final Player player = player("moved_after_preview");
		final Weapon item = weapon("moving target", 15, 0, 3);
		player.equipToInventoryOnly(item);
		final ItemUpgradeService service = service(0);
		provideRequirements(player, item, service);
		final ItemUpgradePreview preview = service.createPreview(player, item);

		item.getContainerSlot().remove(item.getID());
		player.equip("lhand", item);

		assertSame(ItemUpgradeResult.Status.STALE_PREVIEW,
				service.performUpgrade(player, item,
						preview.getRequestToken()).getStatus());
		assertEquals(0, item.getUpgradeLevel());
	}

	@Test
	public void movingAnOuterContainerAlsoInvalidatesTheBoundAttempt() {
		final Player player = player("moved_container_after_preview");
		final Container bag = new Container("upgrade bag", "container", "test",
				Collections.<String, String>emptyMap());
		bag.setID(new ID(901, "item_upgrade_container_move"));
		bag.setEquipableSlots(Arrays.asList("bag", "lhand"));
		final Weapon item = weapon("nested moving target", 15, 0, 3);
		player.equipToInventoryOnly(bag);
		bag.getSlot("content").add(item);
		final ItemUpgradeService service = service(0);
		provideRequirements(player, item, service);
		final ItemUpgradePreview preview = service.createPreview(player, item);

		bag.getContainerSlot().remove(bag.getID());
		player.equip("lhand", bag);

		assertSame(ItemUpgradeResult.Status.STALE_PREVIEW,
				service.performUpgrade(player, item,
						preview.getRequestToken()).getStatus());
		assertEquals(0, item.getUpgradeLevel());
	}

	@Test
	public void executionRechecksMaterialsWithoutChargingMoney() {
		final Player player = player("execution_recheck");
		final Weapon item = weapon("execution recheck", 15, 0, 3);
		player.equipToInventoryOnly(item);
		final ItemUpgradeService service = service(0);
		provideRequirements(player, item, service);
		final ItemUpgradePreview preview = service.createPreview(player, item);
		final int moneyBefore = games.stendhal.server.entity.item.money.MoneyUtils
				.getTotalMoneyInCopper(player);
		player.drop("polano", 1);

		assertSame(ItemUpgradeResult.Status.MISSING_RESOURCES,
				service.performUpgrade(player, item,
						preview.getRequestToken()).getStatus());
		assertEquals(moneyBefore,
				games.stendhal.server.entity.item.money.MoneyUtils
						.getTotalMoneyInCopper(player));
		assertEquals(0, item.getUpgradeLevel());
	}

	private static Player player(final String name) {
		return PlayerTestHelper.createPlayer(name);
	}

	private static ItemUpgradeService service(final int randomValue) {
		return new ItemUpgradeService(new FixedRandom(randomValue));
	}

	private static Weapon weapon(final String name, final int attack,
			final int level, final int maximum) {
		final Map<String, String> attributes = new LinkedHashMap<String, String>();
		attributes.put("atk", Integer.toString(attack));
		attributes.put("rate", "5");
		attributes.put("damage_min", Integer.toString(attack - 2));
		attributes.put("damage_max", Integer.toString(attack + 2));
		if (maximum > 0) {
			attributes.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE,
					Integer.toString(maximum));
		}
		final Weapon item = new Weapon(name, "sword", "test", attributes);
		item.setEquipableSlots(Arrays.asList("bag", "lhand", "rhand"));
		item.setUpgradeLevel(level);
		return item;
	}

	private static void provideRequirements(final Player player,
			final Item item, final ItemUpgradeService service) {
		PlayerTestHelper.equipWithMoney(player,
				service.calculateUpgradeFee(player, item));
		for (final Map.Entry<String, Integer> material
				: service.getMaterialRequirements(item.getUpgradeLevel() + 1).entrySet()) {
			PlayerTestHelper.equipWithStackableItem(player, material.getKey(),
					material.getValue());
		}
	}

	private static Map<String, Integer> materials(final Object... values) {
		final Map<String, Integer> result = new LinkedHashMap<String, Integer>();
		for (int index = 0; index < values.length; index += 2) {
			result.put((String) values[index], (Integer) values[index + 1]);
		}
		return result;
	}

	private static final class FixedRandom extends Random {
		private static final long serialVersionUID = 1L;
		private final int value;

		private FixedRandom(final int value) {
			this.value = value;
		}

		@Override
		public int nextInt(final int bound) {
			return Math.min(value, bound - 1);
		}
	}
}
