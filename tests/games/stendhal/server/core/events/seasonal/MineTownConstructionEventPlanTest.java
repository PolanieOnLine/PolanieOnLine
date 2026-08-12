/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Test;

public class MineTownConstructionEventPlanTest {
	@After
	public void clearProperties() {
		System.clearProperty(MineTownConstructionEventPlan.PROPERTY);
		System.clearProperty(MineTownEventPlan.PROPERTY);
	}

	@Test
	public void preparesEnabledVariantWithoutChangingGlobalProperty() throws Exception {
		System.clearProperty(MineTownConstructionEventPlan.PROPERTY);

		final MineTownConstructionEventPlan plan = MineTownConstructionEventPlan.prepare(true);

		assertNotNull(plan);
		assertNull(System.getProperty(MineTownConstructionEventPlan.PROPERTY));
	}

	@Test
	public void preparesDisabledVariantWithoutChangingGlobalProperty() throws Exception {
		System.setProperty(MineTownConstructionEventPlan.PROPERTY, "true");

		final MineTownConstructionEventPlan plan = MineTownConstructionEventPlan.prepare(false);

		assertNotNull(plan);
		assertFalse(plan.isEnabled());
		assertNotNull(System.getProperty(MineTownConstructionEventPlan.PROPERTY));
	}

	@Test
	public void preparesAroundActiveMineTownWithoutChangingEitherProperty() throws Exception {
		System.setProperty(MineTownEventPlan.PROPERTY, "true");
		System.clearProperty(MineTownConstructionEventPlan.PROPERTY);

		assertNotNull(MineTownConstructionEventPlan.prepare(true));
		assertNotNull(MineTownConstructionEventPlan.prepare(false));
		assertNotNull(System.getProperty(MineTownEventPlan.PROPERTY));
		assertNull(System.getProperty(MineTownConstructionEventPlan.PROPERTY));
	}
}
