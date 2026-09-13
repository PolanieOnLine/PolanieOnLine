package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import utilities.PlayerTestHelper;
import utilities.RPClass.ItemTestHelper;

public class RingOfTeleportationTest {
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		MockStendlRPWorld.get();
		ItemTestHelper.generateRPClasses();
		PlayerTestHelper.generatePlayerRPClasses();
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

	@Test
	public void completedReturnClearsSavedPositionAndDimsRing() {
		final RingOfTeleportation ring = new RingOfTeleportation();
		final Player player = PlayerTestHelper.createPlayer("return-ring-test");
		ring.activeRing();
		ring.setItemData("0_semos_plains_n 10 20");

		assertFalse(ring.isUsed());
		assertEquals(1, ring.getInt("state"));

		ring.completeSuccessfulReturn(player);

		assertTrue(ring.isUsed());
		assertEquals(0, ring.getInt("state"));
		assertNull(ring.getItemData());
		assertTrue(ring.has("frequency"));
	}
}
