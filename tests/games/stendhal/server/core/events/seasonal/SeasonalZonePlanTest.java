/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class SeasonalZonePlanTest {
	@Test
	public void preparesChristmasEnabledVariant() throws Exception {
		final SeasonalZonePlan plan = SeasonalZonePlan.prepare(ChristmasEventPlan.PROPERTY, true);
		assertNotNull(plan);
		assertEquals(ChristmasEventPlan.PROPERTY, plan.getProperty());
	}

	@Test
	public void preparesChristmasDisabledVariant() throws Exception {
		assertNotNull(SeasonalZonePlan.prepare(ChristmasEventPlan.PROPERTY, false));
	}

	@Test
	public void preparesMineTownEnabledVariant() throws Exception {
		final SeasonalZonePlan plan = SeasonalZonePlan.prepare(MineTownEventPlan.PROPERTY, true);
		assertNotNull(plan);
		assertEquals(MineTownEventPlan.PROPERTY, plan.getProperty());
	}

	@Test
	public void preparesMineTownDisabledVariant() throws Exception {
		assertNotNull(SeasonalZonePlan.prepare(MineTownEventPlan.PROPERTY, false));
	}

	@Test
	public void preparesEasterEnabledVariant() throws Exception {
		final SeasonalZonePlan plan = SeasonalZonePlan.prepare(EasterEventPlan.PROPERTY, true);
		assertNotNull(plan);
		assertEquals(EasterEventPlan.PROPERTY, plan.getProperty());
	}

	@Test
	public void preparesEasterDisabledVariant() throws Exception {
		assertNotNull(SeasonalZonePlan.prepare(EasterEventPlan.PROPERTY, false));
	}
}
