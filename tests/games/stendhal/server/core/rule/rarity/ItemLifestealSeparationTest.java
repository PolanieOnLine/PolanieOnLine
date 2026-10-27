/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemLifestealSeparationTest {
	@BeforeClass public static void classes() { ItemTestHelper.generateRPClasses(); }

	@Test public void authoredLifeStealIsUnscaledAtEveryRarity() {
		for (final ItemRarity rarity : ItemRarity.values()) {
			final Item item = weapon(0.075);
			initialize(item, rarity);
			assertEquals(rarity.toString(), 0.075, item.getDouble("lifesteal"), 0.000001);
			assertNull(item.getRarityModifier("lifesteal"));
		}
	}

	@Test public void authoredLifeStealDoesNotConsumeRandomSlot() {
		final Item item = weapon(0.10);
		initialize(item, ItemRarity.RARE);
		assertFalse(ItemAffixState.has(item, "lifesteal"));
		assertEquals(1, ItemAffixState.getValues(item).size());
	}

	@Test public void exactlyTenOrTwentyEffectivenessCanRollOnAuthoredLifeSteal() {
		final ItemAffixDefinition definition = ItemAffixRegistry.getInstance().get("lifesteal_effectiveness");
		assertNotNull("Effectiveness must be a separate random affix", definition);
		for (int choice = 0; choice < 2; choice++) {
			final int selected = choice;
			final Item item = weapon(0.075);
			assertTrue(definition.isEligible(item));
			assertTrue(definition.apply(item, new Random() {
				private static final long serialVersionUID = 1L;
				@Override public int nextInt(final int bound) {
					assertEquals("Only two effectiveness rolls are allowed", 2, bound);
					return selected;
				}
			}));
			assertEquals(choice == 0 ? 10 : 20, item.getInt("lifesteal_effectiveness"));
			assertEquals(0.075, item.getDouble("lifesteal"), 0.000001);
			assertFalse(WeaponAffixService.applyLifesteal(item, new Random(2)));
			assertFalse(definition.isEligible(item));
		}
	}

	@Test public void grantDoesNotUnlockEffectivenessOnAWeaponWithoutAuthoredLifeSteal() {
		final Item item = weapon(null);
		assertTrue(WeaponAffixService.applyLifesteal(item, new Random(2)));
		assertTrue(item.getDouble("lifesteal") >= 0.03);
		assertTrue(item.getDouble("lifesteal") <= 0.10);
		final ItemAffixDefinition definition = ItemAffixRegistry.getInstance().get("lifesteal_effectiveness");
		assertNotNull(definition);
		assertFalse(definition.isEligible(item));
	}

	@Test public void deliberateNegativeLifeStealCannotRollEitherAffix() {
		final Item item = weapon(-0.10);
		assertFalse(WeaponAffixService.applyLifesteal(item, new Random(2)));
		final ItemAffixDefinition definition = ItemAffixRegistry.getInstance().get("lifesteal_effectiveness");
		assertNotNull(definition);
		assertFalse(definition.isEligible(item));
		initialize(item, ItemRarity.LEGENDARY);
		assertEquals(-0.10, item.getDouble("lifesteal"), 0.000001);
	}

	@Test public void failedCompensationKeepsOriginalAffixSoAStoredRetryIsPossible() {
		final Item item = weapon(0.10);
		item.setRarity(ItemRarity.RARE);
		item.put("lifesteal", 0.11);
		item.put(ItemAffixState.ATTRIBUTE, "lifesteal", "0.11");
		for (final ItemAffixDefinition definition : ItemAffixRegistry.getInstance().getDefinitions()) {
			if (!item.has(definition.getAttribute())) item.put(definition.getAttribute(), "0");
		}
		final marauroa.common.game.RPObject saved = new marauroa.common.game.RPObject(item);

		LifestealMigrationService.migrate(item, saved);

		assertFalse(item.has("lifesteal_version"));
		assertEquals("0.11", ItemAffixState.getValues(item).get("lifesteal"));
		assertEquals(0.11, item.getDouble("lifesteal"), 0.000001);
	}

	static Item weapon(final Double lifeSteal) {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "40");
		attributes.put("rate", "5");
		if (lifeSteal != null) attributes.put("lifesteal", lifeSteal.toString());
		final Item item = new Item("life steal test sword", "sword", "test", attributes);
		item.setEquipableSlots(Arrays.asList("rhand", "bag"));
		return item;
	}

	private static void initialize(final Item item, final ItemRarity rarity) {
		new ItemRarityService(new Random(7)).initialize(item, ItemCreationContext
				.builder(ItemCreationContext.Source.DROP).withRarity(rarity)
				.withAffixSeed(17L).randomizeModifiers(false).build());
	}
}
