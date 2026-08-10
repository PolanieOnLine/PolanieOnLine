/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.rarity;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ItemRollPrecisionTest {
	@Test
	public void roundsFiniteRollsHalfUpToTwoDecimalPlaces() {
		assertEquals(1.27, ItemRollPrecision.round(1.2695127077879125), 0.0);
		assertEquals(0.13, ItemRollPrecision.round(0.12512031523088826), 0.0);
		assertEquals(1.21, ItemRollPrecision.round(1.2053918188589878), 0.0);
		assertEquals(-1.24, ItemRollPrecision.round(-1.235), 0.0);
	}

	@Test
	public void positiveMultiplierKeepsMinimumRepresentablePrecision() {
		assertEquals(0.01, ItemRollPrecision.roundPositive(0.004), 0.0);
		assertEquals(1.27, ItemRollPrecision.roundPositive(1.2695), 0.0);
	}
}
