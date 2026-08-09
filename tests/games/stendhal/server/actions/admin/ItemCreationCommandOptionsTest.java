/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import games.stendhal.common.constants.ItemRarity;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import marauroa.common.game.RPAction;

public class ItemCreationCommandOptionsTest {
	@Test
	public void testFixedModifiersDisableRandomization() {
		final RPAction action = new RPAction();
		action.put("rarity", "EPIC");
		action.put("attack-multiplier", "1.15");
		action.put("value-multiplier", "1.50");

		final ItemCreationContext context = ItemCreationCommandOptions.fromAction(action);
		assertEquals(ItemCreationContext.Source.ADMIN, context.getSource());
		assertEquals(ItemRarity.EPIC, context.getRarity());
		assertFalse(context.isRandomizeModifiers());
		assertFalse(context.isGenerateAffixes());
		assertEquals(Double.valueOf(1.15), context.getModifiers().getAttackMultiplier());
		assertEquals(Double.valueOf(1.50), context.getModifiers().getValueMultiplier());
	}

	@Test
	public void testLegendaryAutomaticallyEnablesAffixes() {
		final RPAction action = new RPAction();
		action.put("rarity", "legendary");

		final ItemCreationContext context = ItemCreationCommandOptions.fromAction(action);

		assertEquals(ItemRarity.LEGENDARY, context.getRarity());
		assertTrue(context.isGenerateAffixes());
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
	public void testAffixesNoneKeepsNonLegendaryAdminDeterministic() {
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
}
