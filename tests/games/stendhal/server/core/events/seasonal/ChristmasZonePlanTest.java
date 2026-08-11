/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class ChristmasZonePlanTest {
	@Test
	public void preparesChristmasEnabledVariant() throws Exception {
		assertNotNull(ChristmasZonePlan.prepare(true));
	}

	@Test
	public void preparesChristmasDisabledVariant() throws Exception {
		assertNotNull(ChristmasZonePlan.prepare(false));
	}
}
