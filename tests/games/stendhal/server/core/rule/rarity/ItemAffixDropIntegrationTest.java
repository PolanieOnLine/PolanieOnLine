/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.rule.damage.CriticalHitService;
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.core.rule.damage.WeaponArmorInteractionService;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixDropIntegrationTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void legendarySwordDropGetsThreeRegularAndOneSignatureAffix() {
		final Item item = sword();
		new ItemRarityService(new Random(19L)).initialize(item,
				drop(ItemRarity.LEGENDARY));

		assertSame(ItemRarity.LEGENDARY, item.getRarity());
		final Map<String, String> affixes = ItemAffixState.getValues(item);
		assertEquals(4, affixes.size());

		int regularAffixes = 0;
		int legendaryAffixes = 0;
		for (final String id : affixes.keySet()) {
			ItemAffixDefinition definition = ItemAffixRegistry.getInstance().get(id);
			if (definition != null) {
				regularAffixes++;
			} else {
				definition = LegendaryItemAffixRegistry.getInstance().get(id);
				assertTrue(definition != null);
				legendaryAffixes++;
			}
			assertTrue(item.has(definition.getAttribute()));
			assertTrue(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
					definition.getAttribute()));
		}
		assertEquals(3, regularAffixes);
		assertEquals(1, legendaryAffixes);

		if (ItemAffixState.has(item, ParryService.PARRY_CHANCE_ATTRIBUTE)) {
			assertTrue(item.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE) >= 0.05);
			assertTrue(item.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE) <= 0.15);
		}
		if (ItemAffixState.has(item, WeaponAffixService.LIFESTEAL_ATTRIBUTE)) {
			assertTrue(item.getDouble(WeaponAffixService.LIFESTEAL_ATTRIBUTE) >= 0.03);
			assertTrue(item.getDouble(WeaponAffixService.LIFESTEAL_ATTRIBUTE) <= 0.10);
		}
		if (ItemAffixState.has(item, WeaponAffixService.ACCURACY_ATTRIBUTE)) {
			assertTrue(item.getDouble(WeaponAffixService.ACCURACY_ATTRIBUTE) >= 5.0);
			assertTrue(item.getDouble(WeaponAffixService.ACCURACY_ATTRIBUTE) <= 15.0);
		}
		if (ItemAffixState.has(item, CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE)) {
			assertTrue(item.getDouble(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE) >= 3.0);
			assertTrue(item.getDouble(CriticalHitService.CRITICAL_CHANCE_ATTRIBUTE) <= 10.0);
		}
		if (ItemAffixState.has(item,
				WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE)) {
			assertTrue(item.getDouble(
					WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE) >= 0.10);
			assertTrue(item.getDouble(
					WeaponArmorInteractionService.ARMOR_PENETRATION_ATTRIBUTE) <= 0.25);
		}
	}

	@Test
	public void adminLegendaryAutomaticallyGetsAffixes() {
		final Item item = sword();
		new ItemRarityService(new Random(31L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.LEGENDARY)
						.randomizeModifiers(false).withAffixSeed(123L).build());

		assertSame(ItemRarity.LEGENDARY, item.getRarity());
		assertEquals(4, ItemAffixState.getValues(item).size());
	}

	@Test
	public void rareSwordDropGetsExactlyOneAffix() {
		final Item item = sword();
		new ItemRarityService(new Random(7L)).initialize(item,
				drop(ItemRarity.RARE));

		assertEquals(1, ItemAffixState.getValues(item).size());
	}

	@Test
	public void commonDropGetsNoAffixes() {
		final Item item = sword();
		new ItemRarityService(new Random(7L)).initialize(item,
				drop(ItemRarity.COMMON));

		assertFalse(ItemAffixState.hasAny(item));
	}

	@Test
	public void forcedAdminRareGetsExactlyOneAffixByDefault() {
		final Item item = sword();
		new ItemRarityService(new Random(7L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.RARE)
						.randomizeModifiers(false).build());

		assertEquals(1, ItemAffixState.getValues(item).size());
	}

	@Test
	public void forcedAdminEpicGetsExactlyTwoAffixesByDefault() {
		final Item item = sword();
		new ItemRarityService(new Random(9L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.EPIC)
						.randomizeModifiers(false).build());

		assertEquals(2, ItemAffixState.getValues(item).size());
	}

	@Test
	public void adminEpicCanExplicitlyDisableAffixes() {
		final Item item = sword();
		new ItemRarityService(new Random(9L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.EPIC)
						.generateAffixes(false)
						.randomizeModifiers(false).build());

		assertFalse(ItemAffixState.hasAny(item));
	}

	@Test
	public void intrinsicLifestealConsumesOneLegendaryRegularAffixSlot() {
		final Item item = sword();
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, 0.30);

		new ItemRarityService(new Random(5L)).initialize(item,
				drop(ItemRarity.LEGENDARY));

		// The XML value still participates in rarity scaling, but is now recorded
		// as a guaranteed regular affix instead of adding a free fifth affix.
		assertEquals(1.28, item.getRarityModifier(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE).doubleValue(), 0.0);
		assertEquals(0.38, item.getDouble(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE), 0.0);
		assertTrue(ItemAffixState.has(item,
				WeaponAffixService.LIFESTEAL_ATTRIBUTE));
		assertEquals(4, ItemAffixState.getValues(item).size());
	}

	@Test
	public void intrinsicLifestealFillsTheOnlyRareAffixSlot() {
		final Item item = sword();
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, 0.10);

		new ItemRarityService(new Random(5L)).initialize(item,
				drop(ItemRarity.RARE));

		assertEquals(1, ItemAffixState.getValues(item).size());
		assertTrue(ItemAffixState.has(item,
				WeaponAffixService.LIFESTEAL_ATTRIBUTE));
		assertEquals(0.11, item.getDouble(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE), 0.0);
	}

	@Test
	public void intrinsicLifestealLeavesOneRandomEpicAffixSlot() {
		final Item item = sword();
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, 0.10);

		new ItemRarityService(new Random(5L)).initialize(item,
				drop(ItemRarity.EPIC));

		assertEquals(2, ItemAffixState.getValues(item).size());
		assertTrue(ItemAffixState.has(item,
				WeaponAffixService.LIFESTEAL_ATTRIBUTE));
	}

	@Test
	public void commonRemovesPositiveIntrinsicLifesteal() {
		final Item item = sword();
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, 0.10);

		new ItemRarityService(new Random(5L)).initialize(item,
				drop(ItemRarity.COMMON));

		assertFalse(item.has(WeaponAffixService.LIFESTEAL_ATTRIBUTE));
		assertFalse(ItemAffixState.hasAny(item));
	}

	@Test
	public void negativeLifestealRemainsAnIntrinsicDrawback() {
		final Item item = sword();
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, -0.10);

		new ItemRarityService(new Random(5L)).initialize(item,
				drop(ItemRarity.COMMON));

		assertEquals(-0.10, item.getDouble(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE), 0.0);
		assertFalse(ItemAffixState.hasAny(item));
	}

	@Test
	public void commonAccessoryKeepsItsIntrinsicLifesteal() {
		final Item item = equipment("ring", "finger");
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, 0.01);

		new ItemRarityService(new Random(5L)).initialize(item,
				drop(ItemRarity.COMMON));

		assertEquals(0.01, item.getDouble(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE), 0.0);
		assertFalse(ItemAffixState.hasAny(item));
	}

	private ItemCreationContext drop(final ItemRarity rarity) {
		return ItemCreationContext.builder(ItemCreationContext.Source.DROP)
				.withForcedRarity(rarity)
				.randomizeModifiers(false).build();
	}

	private Item sword() {
		return equipment("sword", "rhand");
	}

	private Item equipment(final String itemClass, final String slot) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "5");
		final Item item = new Item("affix drop item", itemClass, "test",
				attributes);
		item.setEquipableSlots(Arrays.asList(slot, "bag"));
		item.configureRarity(Boolean.TRUE, ItemRarityProfile.DEFAULT_ID, 1000);
		return item;
	}
}
