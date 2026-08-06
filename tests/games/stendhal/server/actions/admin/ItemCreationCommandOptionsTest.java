/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.actions.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

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
		assertEquals(Double.valueOf(1.15), context.getModifiers().getAttackMultiplier());
		assertEquals(Double.valueOf(1.50), context.getModifiers().getValueMultiplier());
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
