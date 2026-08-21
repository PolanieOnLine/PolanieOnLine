package games.stendhal.server.entity.item;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.maps.MockStendlRPWorld;

public class RingOfTeleportationTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
	}

	@Test
	public void blocksOzoLabyrinth() {
		assertTrue(RingOfTeleportation.isForbiddenReturnZone(new StendhalRPZone("7_labirynt")));
	}

	@Test
	public void blocksHaizenMazeByReadableName() {
		final StendhalRPZone zone = new StendhalRPZone("instance_daily_player_12345");
		zone.getAttributes().put("readable_name", "Labirynt Haizena");

		assertTrue(RingOfTeleportation.isForbiddenReturnZone(zone));
	}

	@Test
	public void blocksLegacyMazeNames() {
		assertTrue(RingOfTeleportation.isForbiddenReturnZone(new StendhalRPZone("player_daily_maze")));
	}

	@Test
	public void leavesOrdinaryZonesAvailable() {
		assertFalse(RingOfTeleportation.isForbiddenReturnZone(new StendhalRPZone("0_semos_plains_n")));
	}
}
