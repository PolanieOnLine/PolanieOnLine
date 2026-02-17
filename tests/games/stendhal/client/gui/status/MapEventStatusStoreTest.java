/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.junit.Test;

public class MapEventStatusStoreTest {

	@Test
	public void testVisibleStatusProjectsRemainingSeconds() throws Exception {
		final MapEventStatusStore store = MapEventStatusStore.get();
		store.updateStatus("event-projection", "Test", true, Integer.valueOf(3), Integer.valueOf(10),
				Integer.valueOf(12), Integer.valueOf(6), Integer.valueOf(50),
				Integer.valueOf(1), Integer.valueOf(4), "Obrona w toku",
				Collections.<String>emptyList(), Collections.singletonList("0_test_zone"));

		Thread.sleep(1200L);

		final ActiveMapEventStatus visible = store.getVisibleStatusForZone("0_test_zone");
		assertNotNull("Expected active status for matching zone", visible);
		assertEquals("Remaining time should decrement based on UI time", 2, visible.getRemainingSeconds());
		assertEquals("Should preserve event total creature counter", 12, visible.getEventTotalSpawnedCreatures());
		assertEquals("Should preserve defeated creature counter", 6, visible.getEventDefeatedCreatures());
		assertEquals("Should preserve defeat percent", 50, visible.getEventDefeatPercent());
	}
}
