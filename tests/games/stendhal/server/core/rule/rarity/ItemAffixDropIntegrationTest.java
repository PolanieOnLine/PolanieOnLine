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
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixDropIntegrationTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void legendarySwordDropGetsThreePersistentAffixes() {
		final Item item = sword();
		new ItemRarityService(new Random(19L)).initialize(item,
				drop(ItemRarity.LEGENDARY));

		assertSame(ItemRarity.LEGENDARY, item.getRarity());
		assertEquals(3, ItemAffixState.getValues(item).size());
		assertTrue(ItemAffixState.has(item,
				ParryService.PARRY_CHANCE_ATTRIBUTE));
		assertTrue(ItemAffixState.has(item,
				WeaponAffixService.LIFESTEAL_ATTRIBUTE));
		assertTrue(ItemAffixState.has(item,
				WeaponAffixService.ACCURACY_ATTRIBUTE));

		assertTrue(item.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE) >= 0.05);
		assertTrue(item.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE) <= 0.15);
		assertTrue(item.getDouble(WeaponAffixService.LIFESTEAL_ATTRIBUTE) >= 0.03);
		assertTrue(item.getDouble(WeaponAffixService.LIFESTEAL_ATTRIBUTE) <= 0.10);
		assertTrue(item.getDouble(WeaponAffixService.ACCURACY_ATTRIBUTE) >= 5.0);
		assertTrue(item.getDouble(WeaponAffixService.ACCURACY_ATTRIBUTE) <= 15.0);

		assertTrue(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
				ItemTooltip.PARRY_CHANCE));
		assertTrue(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
				ItemTooltip.LIFESTEAL));
		assertTrue(item.getMap(ItemTooltip.ATTRIBUTE).containsKey(
				ItemTooltip.ACCURACY_BONUS));
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
	public void nonDropContextDoesNotGenerateAffixes() {
		final Item item = sword();
		new ItemRarityService(new Random(7L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.LEGENDARY)
						.randomizeModifiers(false).build());

		assertFalse(ItemAffixState.hasAny(item));
	}

	@Test
	public void intrinsicLifestealIsScaledButNotRecordedAsRandom() {
		final Item item = sword();
		item.put(WeaponAffixService.LIFESTEAL_ATTRIBUTE, 0.30);

		new ItemRarityService(new Random(5L)).initialize(item,
				drop(ItemRarity.LEGENDARY));

		// Intrinsic properties still participate in the existing rarity stat
		// scaling. The affix layer must not overwrite or claim that value.
		assertEquals(1.275, item.getRarityModifier(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE).doubleValue(), 0.0000001);
		assertEquals(0.3825, item.getDouble(
				WeaponAffixService.LIFESTEAL_ATTRIBUTE), 0.0000001);
		assertFalse(ItemAffixState.has(item,
				WeaponAffixService.LIFESTEAL_ATTRIBUTE));
		assertEquals(2, ItemAffixState.getValues(item).size());
	}

	private ItemCreationContext drop(final ItemRarity rarity) {
		return ItemCreationContext.builder(ItemCreationContext.Source.DROP)
				.withForcedRarity(rarity)
				.randomizeModifiers(false).build();
	}

	private Item sword() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "30");
		attributes.put("rate", "5");
		final Item item = new Item("affix drop sword", "sword", "test",
				attributes);
		item.setEquipableSlots(Arrays.asList("rhand", "lhand", "bag"));
		item.configureRarity(Boolean.TRUE, ItemRarityProfile.DEFAULT_ID, 1000);
		return item;
	}
}
