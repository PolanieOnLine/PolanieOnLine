/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;

public class MineTownEventPlanTest {
	@After
	public void clearProperty() {
		System.clearProperty(MineTownEventPlan.PROPERTY);
	}

	@Test
	public void preparesEnabledVariantWithoutChangingGlobalProperty() throws Exception {
		System.clearProperty(MineTownEventPlan.PROPERTY);

		final MineTownEventPlan plan = MineTownEventPlan.prepare(true);

		assertNotNull(plan);
		assertNull(System.getProperty(MineTownEventPlan.PROPERTY));
	}

	@Test
	public void preparesDisabledVariantWithoutChangingGlobalProperty() throws Exception {
		System.setProperty(MineTownEventPlan.PROPERTY, "true");

		final MineTownEventPlan plan = MineTownEventPlan.prepare(false);

		assertNotNull(plan);
		assertFalse(plan.isEnabled());
		assertNotNull(System.getProperty(MineTownEventPlan.PROPERTY));
	}
}
