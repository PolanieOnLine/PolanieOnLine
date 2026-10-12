/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.*;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import org.junit.BeforeClass;
import org.junit.Test;
import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Glyph;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemAffixBalanceServiceTest {
	@BeforeClass
	public static void initialize() { ItemTestHelper.generateRPClasses(); }

	@Test
	public void weaponLimitsUpdateGameplayAndSavedMapWithoutRerolling() {
		final Item item = new Item("old sword", "sword", "test", null);
		final String[] attributes = {"accuracy_bonus", "critical_chance", "critical_damage_bonus",
				"bleed_on_hit", "poison_on_hit", "execute_damage", "distance_damage", "armor_penetration"};
		final double[] maximum = {8, 6, 0.15, 0.08, 0.08, 0.15, 0.12, 0.15};
		for (final String attribute : attributes) record(item, attribute, 99);
		item.put("max_improves", 4);
		item.put("improve", 3);
		ItemAffixState.setSeed(item, 123456);
		item.put(ItemAffixState.ATTRIBUTE, "future_affix", "unknown");
		for (int repeat = 0; repeat < 3; repeat++) {
			ItemAffixBalanceService.enforce(item);
			for (int i = 0; i < attributes.length; i++) {
				assertEquals(attributes[i], maximum[i], item.getDouble(attributes[i]), 0.000001);
				assertEquals(item.get(attributes[i]), ItemAffixState.getValues(item).get(attributes[i]));
			}
			assertEquals(Long.valueOf(123456), ItemAffixState.getSeed(item));
			assertEquals(3, item.getUpgradeLevel());
			assertEquals("unknown", ItemAffixState.getValues(item).get("future_affix"));
			assertEquals(9, ItemAffixState.getValues(item).size());
		}
	}

	@Test
	public void equipmentAndJewelleryHaveTheirOwnLimits() {
		final Item item = new Item("old ring", "ring", "test", null);
		record(item, "accuracy_bonus", 25);
		record(item, "critical_chance", 7);
		record(item, "critical_damage_bonus", 0.15);
		final String[] resistances = {"resist_poisoned", "resist_bleeding", "resist_shocked",
				"resist_confused", "resist_heavy", "resist_stunned"};
		for (final String resistance : resistances) record(item, resistance, 0.25);
		ItemAffixBalanceService.enforce(item);
		assertEquals(5, item.getDouble("accuracy_bonus"), 0);
		assertEquals(4, item.getDouble("critical_chance"), 0);
		assertEquals(0.07, item.getDouble("critical_damage_bonus"), 0);
		for (final String resistance : resistances) assertEquals(0.12, item.getDouble(resistance), 0);
		final Item shield = new Item("shield", "shield", "test", null);
		record(shield, "spiked_plating", 0.04);
		ItemAffixBalanceService.enforce(shield);
		assertEquals(0.03, shield.getDouble("spiked_plating"), 0);
	}

	@Test
	public void weakerRollsAndDrawbacksStayExactlyAsSaved() {
		final Item item = new Item("sword", "sword", "test", null);
		record(item, "accuracy_bonus", 2.1234);
		record(item, "critical_chance", -3);
		record(item, "critical_damage_bonus", 0.025);
		final Map<String, String> saved = ItemAffixState.getValues(item);
		ItemAffixBalanceService.enforce(item);
		assertEquals(saved, ItemAffixState.getValues(item));
		assertEquals(2.1234, item.getDouble("accuracy_bonus"), 0);
		assertEquals(-3, item.getDouble("critical_chance"), 0);
		assertEquals(0.025, item.getDouble("critical_damage_bonus"), 0);
	}

	@Test
	public void lifestealParryAndLegendarySignaturesAreExempt() {
		final Item item = new Item("sword", "sword", "test", null);
		record(item, "lifesteal", 0.41);
		record(item, "parry_chance", 0.15);
		record(item, "legendary_duel_master", 1);
		final Map<String, String> saved = ItemAffixState.getValues(item);
		ItemAffixBalanceService.enforce(item);
		assertEquals(saved, ItemAffixState.getValues(item));
		assertEquals(0.41, item.getDouble("lifesteal"), 0);
		assertEquals(0.15, item.getDouble("parry_chance"), 0);
		assertEquals(1, item.getDouble("legendary_duel_master"), 0);
	}

	@Test
	public void glyphsRemainDefinitionOwnedEvenWithLegacyEquipmentClass() {
		final Glyph glyph = new Glyph("glyph", "ring", "test", null);
		glyph.put("accuracy_bonus", 25.0);
		glyph.put("critical_chance", 15.0);
		ItemAffixBalanceService.enforce(glyph);
		assertEquals(25, glyph.getDouble("accuracy_bonus"), 0);
		assertEquals(15, glyph.getDouble("critical_chance"), 0);
	}

	@Test
	public void flatAttackReductionDoesNotSubtractUpgradesOrRepeat() {
		final Item item = new Item("ring", "ring", "test", Collections.singletonMap("atk", "23"));
		item.put("max_improves", 4);
		item.put("improve", 3);
		item.put("flat_attack_bonus", 3);
		item.put(ItemAffixState.ATTRIBUTE, "flat_attack_bonus", "3");
		for (int repeat = 0; repeat < 3; repeat++) {
			ItemAffixBalanceService.enforce(item);
			assertEquals(22, item.getInt("atk"));
			assertEquals(25, item.getAttack());
			assertEquals(3, item.getUpgradeLevel());
			assertEquals("2", ItemAffixState.getValues(item).get("flat_attack_bonus"));
		}
	}

	@Test
	public void intrinsicRarityScaledBonusesCannotBypassRandomAffixLimits() {
		final Item item = new Item("ring", "ring", "test", Collections.singletonMap("accuracy_bonus", "25"));
		item.setEquipableSlots(java.util.Arrays.asList("finger", "bag"));
		new ItemRarityService(new Random(1)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
						.withRarity(ItemRarity.EPIC).randomizeModifiers(false).build());
		assertEquals(5, item.getDouble("accuracy_bonus"), 0);
		assertFalse(ItemAffixState.has(item, "accuracy_bonus"));
		assertEquals(Double.valueOf(1.15), item.getRarityModifier("accuracy_bonus"));
	}

	@Test
	public void questSnapshotAndReforgingCannotBringBackOldValues() {
		final Item source = new Item("source", "sword", "test", Collections.singletonMap("atk", "20"));
		source.setRarity(ItemRarity.EPIC);
		record(source, "accuracy_bonus", 15);
		final Item returned = new Item("returned", "sword", "test", null);
		ItemRaritySnapshot.restore(returned, ItemRaritySnapshot.encode(source));
		assertEquals(8, returned.getDouble("accuracy_bonus"), 0);
		final Item reforged = new Item("reforged", "sword", "test", Collections.singletonMap("atk", "30"));
		ItemRarityTransferSnapshot.apply(reforged, ItemRarityTransferSnapshot.encode(source));
		assertEquals(8, reforged.getDouble("accuracy_bonus"), 0);
		assertEquals(reforged.get("accuracy_bonus"), ItemAffixState.getValues(reforged).get("accuracy_bonus"));
	}

	@Test
	public void transferredFlatBonusesAreAddedToNewBaseBeforeApplyingLimit() {
		final Item source = new Item("old ring", "ring", "test", Collections.singletonMap("atk", "23"));
		source.setRarity(ItemRarity.COMMON);
		source.put("flat_attack_bonus", 3);
		source.put(ItemAffixState.ATTRIBUTE, "flat_attack_bonus", "3");
		final Item target = new Item("new ring", "ring", "test", Collections.singletonMap("atk", "30"));
		ItemRarityTransferSnapshot.apply(target, ItemRarityTransferSnapshot.encode(source));
		assertEquals(32, target.getInt("atk"));
		assertEquals(2, target.getInt("flat_attack_bonus"));
		assertEquals("2", ItemAffixState.getValues(target).get("flat_attack_bonus"));
	}

	private void record(final Item item, final String attribute, final double value) {
		item.put(attribute, value);
		item.put(ItemAffixState.ATTRIBUTE, attribute, item.get(attribute));
	}
}
