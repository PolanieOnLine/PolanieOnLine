/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.spawner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Point;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.maps.MockStendlRPWorld;

public class CreatureRespawnPlacementTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		MockStendlRPWorld.get();
	}

	@AfterClass
	public static void tearDownAfterClass() {
		MockStendlRPWorld.reset();
	}

	@Test
	public void findsNearestReachableTileWhenSpawnIsOccupied() {
		final StendhalRPZone zone = new StendhalRPZone("test", 12, 12);
		final Entity creature = new Entity() { };
		final OccupiedTileEntity occupant = new OccupiedTileEntity();
		occupant.setPosition(5, 5);
		zone.add(occupant);

		assertTrue(zone.collides(creature, 5, 5));

		final Point location = CreatureRespawnPlacement.findReachableFreeLocation(
				zone, creature, 5, 5);

		assertNotNull(location);
		assertEquals(1, Math.abs(location.x - 5) + Math.abs(location.y - 5));
		assertFalse(zone.collides(creature, location.x, location.y));
	}

	@Test
	public void searchKeepsExistingDisplacementLimit() {
		assertEquals(36, CreatureRespawnPlacement.MAX_DISPLACEMENT);
	}

	private static final class OccupiedTileEntity extends Entity {
		@Override
		public boolean isObstacle(final Entity entity) {
			return true;
		}
	}
}
