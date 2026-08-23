/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.damage.ParryService;
import games.stendhal.server.entity.item.Item;
import utilities.RPClass.ItemTestHelper;

public class ItemRarityTransferSnapshotTest {
	@BeforeClass
	public static void setUpClasses() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void appliesExactRollToStrongerTargetBaseAndPreservesAffixAndUpgrade() {
		final Item source = sword("mithril source", 100, 100);
		new ItemRarityService(new Random(1)).initialize(source,
				ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
						.withRarity(ItemRarity.EPIC)
						.withModifiers(ItemRarityModifiers.builder()
								.attackMultiplier(1.13)
								.valueMultiplier(1.80).build())
						.build());
		source.put(ParryService.PARRY_CHANCE_ATTRIBUTE, 0.12);
		source.put(ItemAffixState.ATTRIBUTE,
				ParryService.PARRY_CHANCE_ATTRIBUTE, "0.12");
		ItemAffixState.setSeed(source, 987654321L);
		source.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, 4);
		source.setUpgradeLevel(3);

		final Item target = sword("dark mithril target", 200, 200);
		target.put(Item.MAX_UPGRADE_LEVEL_ATTRIBUTE, 4);
		ItemRarityTransferSnapshot.apply(target,
				ItemRarityTransferSnapshot.encode(source));

		assertSame(ItemRarity.EPIC, target.getRarity());
		assertEquals(226, target.getInt("atk"));
		assertEquals(360, target.getValue());
		assertEquals(Double.valueOf(1.13), target.getRarityModifier("atk"));
		assertEquals(0.12,
				target.getDouble(ParryService.PARRY_CHANCE_ATTRIBUTE), 0.0000001);
		assertEquals("0.12", ItemAffixState.getValues(target).get(
				ParryService.PARRY_CHANCE_ATTRIBUTE));
		assertEquals(Long.valueOf(987654321L), ItemAffixState.getSeed(target));
		assertEquals(3, target.getUpgradeLevel());
	}

	@Test
	public void legacyItemWithoutRarityBecomesDeterministicCommon() {
		final Item source = sword("legacy source", 100, 100);
		final Item target = sword("common target", 200, 200);

		ItemRarityTransferSnapshot.apply(target,
				ItemRarityTransferSnapshot.encode(source));

		assertSame(ItemRarity.COMMON, target.getRarity());
		assertEquals(200, target.getInt("atk"));
		assertEquals(200, target.getValue());
		assertFalse(ItemAffixState.hasAny(target));
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsMalformedTransfer() {
		ItemRarityTransferSnapshot.apply(sword("target", 10, 10), "broken");
	}

	private Item sword(final String name, final int attack, final int value) {
		final Item item = new Item(name, "sword", "test", null);
		item.put("atk", attack);
		item.setEquipableSlots(Arrays.asList("lhand", "bag"));
		item.configureRarity(Boolean.TRUE, ItemRarityProfile.DEFAULT_ID, value);
		return item;
	}
}
