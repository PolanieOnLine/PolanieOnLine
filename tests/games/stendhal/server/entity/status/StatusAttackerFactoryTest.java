/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class StatusAttackerFactoryTest {
	@Test
	public void createsStunnedStatusAttackerFromCreatureProfile() {
		final StatusAttacker attacker = StatusAttackerFactory.get("StunnedStatus,12");

		assertNotNull(attacker);
		assertEquals(12.0, attacker.getProbability(), 0.0);
		assertEquals("stunned", attacker.getStatusName());
	}
}
