/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import utilities.RPClass.ItemTestHelper;

public class ItemRarityServiceTest {
	@BeforeClass
	public static void generateRPClasses() {
		ItemTestHelper.generateRPClasses();
	}

	@Test
	public void defaultWeightsAddUpAndRollOnlySupportedRarities() {
		final ItemRarityProfile profile = ItemRarityProfile.defaultProfile();
		assertEquals(100.0, profile.getTotalWeight(), 0.0);
		assertSame(ItemRarity.COMMON, profile.roll(0.0));
		assertSame(ItemRarity.COMMON, profile.roll(0.699999));
		assertSame(ItemRarity.RARE, profile.roll(0.70));
		assertSame(ItemRarity.EPIC, profile.roll(0.92));
		assertSame(ItemRarity.LEGENDARY, profile.roll(0.98));
		for (int index = 0; index < 1000; index++) {
			assertTrue(Arrays.asList(ItemRarity.values()).contains(
					profile.roll(index / 1000.0)));
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void rejectsProfilesWhoseWeightsDoNotAddUp() {
		ItemRarityProfile.builder("invalid")
				.tier(ItemRarity.COMMON, 60, 1, 1, 1)
				.tier(ItemRarity.RARE, 20, 1, 1, 1)
				.tier(ItemRarity.EPIC, 5, 1, 1, 1)
				.tier(ItemRarity.LEGENDARY, 2, 1, 1, 1)
				.build();
	}

	@Test
	public void fixedRarityAndModifiersSkipAllRandomness() {
		final CountingRandom random = new CountingRandom(0.999);
		final ItemRarityService service = new ItemRarityService(random);
		final Item item = combatItem();
		final ItemRarityModifiers modifiers = ItemRarityModifiers.builder()
				.attackMultiplier(1.30)
				.defenseMultiplier(1.15)
				.valueMultiplier(2.00)
				.build();

		service.initialize(item, ItemCreationContext
				.builder(ItemCreationContext.Source.ADMIN)
				.withRarity(ItemRarity.LEGENDARY)
				.withModifiers(modifiers)
				.build());

		assertEquals(0, random.calls);
		assertSame(ItemRarity.LEGENDARY, item.getRarity());
		assertEquals(130, item.getInt("atk"));
		assertEquals(58, item.getInt("def"));
		assertEquals(10, item.getInt("rate"));
		assertFalse(item.has("range"));
		assertEquals(2000, item.getValue());
		assertEquals(Double.valueOf(1.30), item.getRarityModifier("atk"));
		assertEquals(Double.valueOf(1.15), item.getRarityModifier("def"));
		assertEquals(Double.valueOf(2.00), item.getRarityModifier("value"));
		assertFalse(item.isPersistent());
	}

	@Test
	public void extremeFixedModifiersSaturateAtRpAttributeLimits() {
		final Item item = combatItem();
		item.put("lifesteal", 0.2);
		final ItemRarityModifiers modifiers = ItemRarityModifiers.builder()
				.statMultiplier(1.0e308)
				.valueMultiplier(1.0e308)
				.build();

		new ItemRarityService(new Random(1L)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.LEGENDARY)
						.withModifiers(modifiers)
						.randomizeModifiers(false)
						.build());

		assertEquals(Short.MAX_VALUE, item.getInt("atk"));
		assertEquals(Short.MAX_VALUE, item.getInt("def"));
		assertEquals(1, item.getInt("rate"));
		assertEquals((double) Float.MAX_VALUE, item.getDouble("lifesteal"), 0.0);
		assertEquals(Integer.MAX_VALUE, item.getValue());
	}

	@Test
	public void forcedRaritySkipsOnlyTheRarityRollWhenModifiersAreRandom() {
		final CountingRandom random = new CountingRandom(0.5);
		final Item item = combatItem();
		new ItemRarityService(random).initialize(item, ItemCreationContext
				.builder(ItemCreationContext.Source.DEFAULT)
				.withRarity(ItemRarity.RARE).build());

		// atk, def and rate each get one concrete roll; there is no fourth roll.
		assertEquals(3, random.calls);
		assertSame(ItemRarity.RARE, item.getRarity());
	}

	@Test
	public void commonLeavesBaseStatsAndValueUnchanged() {
		final Item item = combatItem();
		new ItemRarityService(new Random(1)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withRarity(ItemRarity.COMMON).build());

		assertEquals(100, item.getInt("atk"));
		assertEquals(50, item.getInt("def"));
		assertEquals(10, item.getInt("rate"));
		assertEquals(1000, item.getValue());
	}

	@Test
	public void randomModifiersStayInsideTierRangeAndValueIsScaled() {
		final ItemRarity[] rarities = {
			ItemRarity.RARE, ItemRarity.EPIC, ItemRarity.LEGENDARY
		};
		final double[] minimums = {1.05, 1.10, 1.20};
		final double[] maximums = {1.10, 1.20, 1.35};
		final int[] expectedValues = {1200, 1500, 2000};

		for (int tierIndex = 0; tierIndex < rarities.length; tierIndex++) {
			final Item item = combatItem();
			new ItemRarityService(new Random(17 + tierIndex)).initialize(item,
					ItemCreationContext.builder(ItemCreationContext.Source.DEFAULT)
							.withRarity(rarities[tierIndex]).build());

			assertSame(rarities[tierIndex], item.getRarity());
			for (final Map.Entry<String, Double> entry
					: item.getRarityModifiers().entrySet()) {
				if (!ItemRarityModifiers.VALUE.equals(entry.getKey())) {
					assertTrue(entry.getValue().doubleValue() >= minimums[tierIndex]);
					assertTrue(entry.getValue().doubleValue() <= maximums[tierIndex]);
				}
			}
			assertEquals(expectedValues[tierIndex], item.getValue());
		}
	}

	@Test
	public void initializationAndRestoreAreIdempotent() {
		final CountingRandom random = new CountingRandom(0.5);
		final ItemRarityService service = new ItemRarityService(random);
		final Item item = combatItem();
		final ItemCreationContext fixed = ItemCreationContext
				.builder(ItemCreationContext.Source.ADMIN)
				.withRarity(ItemRarity.LEGENDARY)
				.withModifiers(ItemRarityModifiers.builder()
						.statMultiplier(1.20).valueMultiplier(2.0).build())
				.build();

		service.initialize(item, fixed);
		final int attack = item.getInt("atk");
		service.initialize(item, fixed);
		service.initialize(item, ItemCreationContext.restore());

		assertEquals(attack, item.getInt("atk"));
		assertEquals(120, attack);
		assertEquals(0, random.calls);
	}

	@Test
	public void defaultEligibilityUsesStatsAndEquipmentSlot() {
		final CountingRandom random = new CountingRandom(0.5);
		final ItemRarityService service = new ItemRarityService(random);
		final Item bagOnly = combatItem();
		bagOnly.setEquipableSlots(Arrays.asList("bag", "content"));
		service.initialize(bagOnly, ItemCreationContext.defaultCreation());
		assertNull(bagOnly.getRarity());
		assertEquals(0, random.calls);

		final Item explicitlyDisabled = combatItem();
		explicitlyDisabled.configureRarity(Boolean.FALSE, "default", 1000);
		service.initialize(explicitlyDisabled, ItemCreationContext.defaultCreation());
		assertNull(explicitlyDisabled.getRarity());

		final Item explicitlyEnabled = combatItem();
		explicitlyEnabled.setEquipableSlots(Arrays.asList("bag"));
		explicitlyEnabled.configureRarity(Boolean.TRUE, "default", 1000);
		service.initialize(explicitlyEnabled, ItemCreationContext
				.builder(ItemCreationContext.Source.ADMIN)
				.withRarity(ItemRarity.RARE).build());
		assertSame(ItemRarity.RARE, explicitlyEnabled.getRarity());
	}

	@Test
	public void stackableItemIsNeverEligible() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "100");
		final StackableItem stack = new StackableItem("arrows", "missile", "test",
				attributes);
		stack.setEquipableSlots(Arrays.asList("rhand"));
		stack.configureRarity(Boolean.TRUE, "default", 50);
		new ItemRarityService(new Random(1)).initialize(stack,
				ItemCreationContext.defaultCreation());
		assertNull(stack.getRarity());
	}

	@Test
	public void explicitEnableAllowsValueOnlyNonStackableItem() {
		final Item item = new Item("story relic", "relic", "test",
				new HashMap<String, String>());
		item.setEquipableSlots(Arrays.asList("bag"));
		item.configureRarity(Boolean.TRUE, "default", 100);

		new ItemRarityService(new Random(1)).initialize(item,
				ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
						.withForcedRarity(ItemRarity.RARE).build());

		assertSame(ItemRarity.RARE, item.getRarity());
		assertEquals(120, item.getValue());
		assertEquals(1, item.getRarityModifiers().size());
		assertEquals(Double.valueOf(1.2), item.getRarityModifier("value"));
	}

	@Test
	public void deterministicQuestUsesCommonByDefaultAndTierMidpointWhenForced() {
		final Item first = combatItem();
		final Item second = combatItem();
		new ItemRarityService(new Random(1)).initialize(first,
				ItemCreationContext.quest());
		new ItemRarityService(new Random(999)).initialize(second,
				ItemCreationContext.quest());
		assertSame(ItemRarity.COMMON, first.getRarity());
		assertEquals(first.getInt("atk"), second.getInt("atk"));

		final Item epicA = combatItem();
		final Item epicB = combatItem();
		final ItemCreationContext epicQuest = ItemCreationContext
				.builder(ItemCreationContext.Source.QUEST)
				.withRarity(ItemRarity.EPIC).build();
		new ItemRarityService(new Random(2)).initialize(epicA, epicQuest);
		new ItemRarityService(new Random(888)).initialize(epicB, epicQuest);
		assertEquals(epicA.getRarityModifiers(), epicB.getRarityModifiers());
		assertEquals(epicA.getInt("atk"), epicB.getInt("atk"));
		assertEquals(115, epicA.getInt("atk"));
	}

	@Test
	public void questModifierRandomnessRequiresExplicitOptIn() {
		final CountingRandom deterministicRandom = new CountingRandom(0.9);
		final Item deterministic = combatItem();
		new ItemRarityService(deterministicRandom).initialize(deterministic,
				ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
						.withRarity(ItemRarity.EPIC)
						.randomizeModifiers(false).build());
		assertEquals(0, deterministicRandom.calls);
		assertEquals(Double.valueOf(1.15),
				deterministic.getRarityModifier("atk"));

		final CountingRandom randomizedRandom = new CountingRandom(0.9);
		final Item randomized = combatItem();
		new ItemRarityService(randomizedRandom).initialize(randomized,
				ItemCreationContext.builder(ItemCreationContext.Source.QUEST)
						.withRarity(ItemRarity.EPIC)
						.randomizeModifiers(true).build());
		assertEquals(3, randomizedRandom.calls);
		assertEquals(1.19, randomized.getRarityModifier("atk").doubleValue(),
				0.0000001);
	}

	@Test
	public void restoreNeverConsumesRandomnessOrChangesStats() {
		final CountingRandom random = new CountingRandom(0.99);
		final Item item = combatItem();
		new ItemRarityService(random).initialize(item, ItemCreationContext.restore());
		assertEquals(0, random.calls);
		assertNull(item.getRarity());
		assertEquals(100, item.getInt("atk"));
	}

	@Test
	public void profileChangesCannotChangeAnInitializedInstance() {
		final ItemRarityService service = new ItemRarityService(new Random(1));
		final Item item = combatItem();
		service.initialize(item, ItemCreationContext
				.builder(ItemCreationContext.Source.DEFAULT)
				.withRarity(ItemRarity.RARE)
				.withModifiers(ItemRarityModifiers.builder()
						.statMultiplier(1.10).valueMultiplier(1.20).build())
				.build());
		final int attack = item.getInt("atk");
		final int value = item.getValue();

		service.registerProfile(ItemRarityProfile.builder("default")
				.tier(ItemRarity.COMMON, 70, 2.0, 2.0, 3.0)
				.tier(ItemRarity.RARE, 22, 2.0, 2.0, 3.0)
				.tier(ItemRarity.EPIC, 6, 2.0, 2.0, 3.0)
				.tier(ItemRarity.LEGENDARY, 2, 2.0, 2.0, 3.0)
				.build());
		service.initialize(item, ItemCreationContext.defaultCreation());

		assertEquals(attack, item.getInt("atk"));
		assertEquals(value, item.getValue());
	}

	@Test
	public void contextResolvesDeclaredPriority() {
		final ItemCreationContext context = ItemCreationContext
				.builder(ItemCreationContext.Source.QUEST)
				.withFactoryRarity(ItemRarity.RARE)
				.withQuestRarity(ItemRarity.EPIC)
				.withForcedRarity(ItemRarity.LEGENDARY)
				.build();
		assertSame(ItemRarity.LEGENDARY, context.getResolvedRarity());
		assertSame(ItemRarity.EPIC, ItemCreationContext
				.builder(ItemCreationContext.Source.QUEST)
				.withFactoryRarity(ItemRarity.RARE)
				.withQuestRarity(ItemRarity.EPIC)
				.build().getResolvedRarity());
		assertSame(ItemRarity.RARE, ItemCreationContext
				.builder(ItemCreationContext.Source.DEFAULT)
				.withFactoryRarity(ItemRarity.RARE)
				.build().getResolvedRarity());
	}

	@Test
	public void legacyMigrationPreservesExistingStatsAndValue() {
		final Item item = combatItem();
		item.put("atk", 137);
		item.setValue(777);
		new ItemRarityService(new Random(1)).markLegacyCommon(item);

		assertSame(ItemRarity.COMMON, item.getRarity());
		assertEquals(137, item.getInt("atk"));
		assertEquals(777, item.getValue());
		assertEquals(Double.valueOf(1.0), item.getRarityModifier("atk"));
		assertFalse(item.isPersistent());
	}

	private Item combatItem() {
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "100");
		attributes.put("def", "50");
		attributes.put("rate", "10");
		final Item item = new Item("test sword", "sword", "test", attributes);
		item.setEquipableSlots(Arrays.asList("rhand", "bag"));
		item.configureRarity(null, "default", 1000);
		return item;
	}

	private static final class CountingRandom extends Random {
		private static final long serialVersionUID = 1L;
		private final double value;
		private int calls;

		private CountingRandom(final double value) {
			this.value = value;
		}

		@Override
		public double nextDouble() {
			calls++;
			return value;
		}
	}
}
