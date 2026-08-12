/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;

public class EasterEventPlanTest {
	@After
	public void clearProperty() {
		System.clearProperty(EasterEventPlan.PROPERTY);
	}

	@Test
	public void preparesEnabledVariantWithoutChangingGlobalProperty() throws Exception {
		System.clearProperty(EasterEventPlan.PROPERTY);

		final EasterEventPlan plan = EasterEventPlan.prepare(true);

		assertNotNull(plan);
		assertNull(System.getProperty(EasterEventPlan.PROPERTY));
	}

	@Test
	public void preparesDisabledVariantWithoutChangingGlobalProperty() throws Exception {
		System.setProperty(EasterEventPlan.PROPERTY, "true");

		final EasterEventPlan plan = EasterEventPlan.prepare(false);

		assertNotNull(plan);
		assertFalse(plan.isEnabled());
		assertNotNull(System.getProperty(EasterEventPlan.PROPERTY));
	}
}
