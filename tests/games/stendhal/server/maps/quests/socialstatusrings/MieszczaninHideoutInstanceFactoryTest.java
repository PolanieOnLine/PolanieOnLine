/***************************************************************************
 *                 (C) Copyright 2018-2026 - PolanieOnLine                 *
 ***************************************************************************/
package games.stendhal.server.maps.quests.socialstatusrings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import games.stendhal.common.tiled.LayerDefinition;
import games.stendhal.common.tiled.StendhalMapStructure;

public class MieszczaninHideoutInstanceFactoryTest {

	@Test
	public void fixedHideoutMapKeepsSafeEntranceAndSolidBoundary() {
		final StendhalMapStructure map = MieszczaninHideoutInstanceFactory.createMapStructure();
		final LayerDefinition floor = map.getLayer("0_floor");
		final LayerDefinition terrain = map.getLayer("1_terrain");
		final LayerDefinition collision = map.getLayer("collision");

		assertEquals(1, collision.getTileAt(0, 0));
		assertEquals(1, collision.getTileAt(MieszczaninHideoutInstanceFactory.WIDTH - 1,
				MieszczaninHideoutInstanceFactory.HEIGHT - 1));
		assertEquals(0, collision.getTileAt(MieszczaninHideoutInstanceFactory.START_X,
				MieszczaninHideoutInstanceFactory.START_Y));
		assertEquals(59, floor.getTileAt(MieszczaninHideoutInstanceFactory.EXIT_X,
				MieszczaninHideoutInstanceFactory.EXIT_Y));

		// Wall faces must point into the room on every side.
		assertEquals(16, terrain.getTileAt(1, 0));
		assertEquals(19, terrain.getTileAt(1,
				MieszczaninHideoutInstanceFactory.HEIGHT - 1));
		assertEquals(15, terrain.getTileAt(0, 1));
		assertEquals(20, terrain.getTileAt(MieszczaninHideoutInstanceFactory.WIDTH - 1, 1));
	}
}
