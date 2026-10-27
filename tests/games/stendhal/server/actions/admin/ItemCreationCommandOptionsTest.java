/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.rarity.ItemAffixState;
import games.stendhal.server.core.rule.rarity.ItemAffixRegistry;
import games.stendhal.server.core.rule.rarity.ItemAffixDefinition;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.core.rule.rarity.ItemRarityService;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import utilities.RPClass.ItemTestHelper;

public class ItemCreationCommandOptionsTest {
	@Test(expected = IllegalArgumentException.class)
	public void normalDropsCannotRequestMaximizedAffixes() {
		ItemCreationContext.builder(ItemCreationContext.Source.DROP).maximizeAffixes(true).build();
	}

	@Test(expected = IllegalArgumentException.class)
	public void restoreCannotRequestMaximizedAffixes() {
		ItemCreationContext.builder(ItemCreationContext.Source.RESTORE).maximizeAffixes(true).build();
	}

	@Test
	public void maxModeIsExplicitAndDoesNotChangeOrdinaryContexts() {
		final RPAction action = new RPAction();
		action.put("affixes", "max");
		final ItemCreationContext maximum = ItemCreationCommandOptions.fromAction(action);
		assertTrue(maximum.isMaximizeAffixes());
		assertTrue(maximum.isGenerateAffixes());
		assertFalse(maximum.equals(ItemCreationContext.admin()));
		action.put("affixes", "random");
		assertFalse(ItemCreationCommandOptions.fromAction(action).isMaximizeAffixes());
		assertFalse(ItemCreationContext.drop().isMaximizeAffixes());
	}

	@Test
	public void maxAffixesUseLegalMaximumsAndSurviveRestore() {
		final Map<String, Double> maximums = new HashMap<String, Double>();
		maximums.put("lifesteal", 0.10);
		maximums.put("accuracy_bonus", 8.0);
		maximums.put("critical_chance", 6.0);
		maximums.put("critical_damage_bonus", 0.15);
		maximums.put("bleed_on_hit", 0.08);
		maximums.put("execute_damage", 0.15);
		maximums.put("armor_penetration", 0.15);
		final Set<String> selected = new HashSet<String>();
		for (long seed = 0; seed < 32; seed++) {
			final RPAction action = new RPAction();
			action.put("rarity", "legendary");
			action.put("affixes", "MAX");
			action.put("seed", Long.toString(seed));
			action.put("stat-multiplier", "1.35");
			final Item item = randomlyRolledItem(action, 0.5);
			assertEquals(27, item.getInt("atk"));
			assertEquals(Long.valueOf(seed), ItemAffixState.getSeed(item));
			assertEquals(4, ItemAffixState.getValues(item).size());
			for (String id : ItemAffixState.getValues(item).keySet()) {
				final ItemAffixDefinition definition = ItemAffixRegistry.getInstance().get(id);
				if (definition != null) {
					assertTrue("Unexpected axe affix: " + id, maximums.containsKey(id));
					assertEquals(id, maximums.get(id), item.getDouble(definition.getAttribute()), 0.000001);
					selected.add(id);
				}
			}
			final Item restored = new Item("topór", "axe", "test", null);
			ItemAffixState.restore(restored, new RPObject(item));
			assertEquals(ItemAffixState.getValues(item), ItemAffixState.getValues(restored));
		}
		assertEquals("Affix types must remain random", maximums.keySet(), selected);
	}

	@Test
	public void summonWithoutRarityGeneratesAffixesForTheRolledTier() {
		final RPAction action = new RPAction();
		action.put("type", "summon");
		action.put("creature", "topór");
		final double[] rolls = {0.0, 0.75, 0.95, 0.99};
		final ItemRarity[] rarities = {ItemRarity.COMMON, ItemRarity.RARE,
				ItemRarity.EPIC, ItemRarity.LEGENDARY};
		final int[] affixCounts = {0, 1, 2, 4};
		for (int index = 0; index < rolls.length; index++) {
			final Item item = randomlyRolledItem(action, rolls[index]);
			assertEquals(rarities[index], item.getRarity());
			assertEquals("Affixes for randomly rolled " + rarities[index],
					affixCounts[index], ItemAffixState.getValues(item).size());
		}
	}

	@Test
	public void summonWithoutRarityStillHonoursExplicitlyDisabledAffixes() {
		final RPAction action = new RPAction();
		action.put("affixes", "none");
		for (double roll : new double[] {0.75, 0.95}) {
			final Item item = randomlyRolledItem(action, roll);
			assertTrue(item.getRarity() == ItemRarity.RARE || item.getRarity() == ItemRarity.EPIC);
			assertTrue(ItemAffixState.getValues(item).isEmpty());
			assertFalse(ItemAffixState.hasSeed(item));
		}
	}

	private Item randomlyRolledItem(final RPAction action, final double roll) {
		ItemTestHelper.generateRPClasses();
		final Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("atk", "20");
		attributes.put("rate", "10");
		final Item item = new Item("topór", "axe", "test", attributes);
		item.setEquipableSlots(Arrays.asList("rhand", "bag"));
		final Random random = new Random(7L) {
			private static final long serialVersionUID = 1L;
			@Override public double nextDouble() { return roll; }
		};
		new ItemRarityService(random).initialize(item, ItemCreationCommandOptions.fromAction(action));
		return item;
	}

	@Test
	public void testFixedModifiersDisableRandomizationButKeepEpicAffixes() {
		final RPAction action = new RPAction();
		action.put("rarity", "EPIC");
		action.put("attack-multiplier", "1.15");
		action.put("value-multiplier", "1.50");

		final ItemCreationContext context = ItemCreationCommandOptions.fromAction(action);
		assertEquals(ItemCreationContext.Source.ADMIN, context.getSource());
		assertEquals(ItemRarity.EPIC, context.getRarity());
		assertFalse(context.isRandomizeModifiers());
		assertTrue(context.isGenerateAffixes());
		assertEquals(Double.valueOf(1.15), context.getModifiers().getAttackMultiplier());
		assertEquals(Double.valueOf(1.50), context.getModifiers().getValueMultiplier());
	}

	@Test
	public void testForcedRareEpicAndLegendaryAutomaticallyEnableAffixes() {
		assertForcedRarityGeneratesAffixes("rare", ItemRarity.RARE);
		assertForcedRarityGeneratesAffixes("epic", ItemRarity.EPIC);
		assertForcedRarityGeneratesAffixes("legendary", ItemRarity.LEGENDARY);
	}

	@Test
	public void testForcedCommonDoesNotEnableAffixes() {
		final RPAction action = new RPAction();
		action.put("rarity", "common");

		assertFalse(ItemCreationCommandOptions.fromAction(action).isGenerateAffixes());
	}

	@Test
	public void testRandomAffixesAndSeedAreForwarded() {
		final RPAction action = new RPAction();
		action.put("rarity", "epic");
		action.put("affixes", "random");
		action.put("seed", "123456789");

		final ItemCreationContext context = ItemCreationCommandOptions.fromAction(action);

		assertTrue(context.isGenerateAffixes());
		assertEquals(Long.valueOf(123456789L), context.getAffixSeed());
	}

	@Test
	public void testAffixesNoneExplicitlyDisablesRareOrEpicAffixes() {
		final RPAction action = new RPAction();
		action.put("rarity", "epic");
		action.put("affixes", "none");

		assertFalse(ItemCreationCommandOptions.fromAction(action).isGenerateAffixes());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testLegendaryCannotDisableAffixes() {
		final RPAction action = new RPAction();
		action.put("rarity", "legendary");
		action.put("affixes", "none");
		ItemCreationCommandOptions.fromAction(action);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testBuilderCannotDisableForcedAdminLegendaryAffixes() {
		ItemCreationContext.builder(ItemCreationContext.Source.ADMIN)
				.withForcedRarity(ItemRarity.LEGENDARY)
				.generateAffixes(false)
				.build();
	}

	@Test
	public void testBuilderCanExplicitlyDisableForcedAdminEpicAffixes() {
		final ItemCreationContext context = ItemCreationContext
				.builder(ItemCreationContext.Source.ADMIN)
				.withForcedRarity(ItemRarity.EPIC)
				.generateAffixes(false)
				.build();
		assertFalse(context.isGenerateAffixes());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSeedCannotBeCombinedWithDisabledAffixes() {
		final RPAction action = new RPAction();
		action.put("rarity", "epic");
		action.put("affixes", "none");
		action.put("seed", "42");
		ItemCreationCommandOptions.fromAction(action);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testInvalidSeedIsRejected() {
		final RPAction action = new RPAction();
		action.put("rarity", "epic");
		action.put("affixes", "random");
		action.put("seed", "not-a-number");
		ItemCreationCommandOptions.fromAction(action);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testUnknownRarityIsRejected() {
		final RPAction action = new RPAction();
		action.put("rarity", "mythic");
		ItemCreationCommandOptions.fromAction(action);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNonFiniteMultiplierIsRejected() {
		final RPAction action = new RPAction();
		action.put("rarity", "legendary");
		action.put("attack-multiplier", "NaN");
		ItemCreationCommandOptions.fromAction(action);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFixedModifiersRequireRarity() {
		final RPAction action = new RPAction();
		action.put("attack-multiplier", "1.20");
		ItemCreationCommandOptions.fromAction(action);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testFixedAndRandomModifiersConflict() {
		final RPAction action = new RPAction();
		action.put("rarity", "rare");
		action.put("attack-multiplier", "1.10");
		action.put("randomize-modifiers", "true");
		ItemCreationCommandOptions.fromAction(action);
	}

	private void assertForcedRarityGeneratesAffixes(final String rarityId,
			final ItemRarity expected) {
		final RPAction action = new RPAction();
		action.put("rarity", rarityId);
		final ItemCreationContext context = ItemCreationCommandOptions.fromAction(action);
		assertEquals(expected, context.getRarity());
		assertTrue(context.isGenerateAffixes());
	}
}
