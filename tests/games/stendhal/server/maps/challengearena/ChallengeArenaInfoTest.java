package games.stendhal.server.maps.challengearena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.geom.Rectangle2D;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.util.Area;
import utilities.PlayerTestHelper;

public class ChallengeArenaInfoTest {
	@BeforeClass
	public static void setUpBeforeClass() {
		MockStendlRPWorld.get();
		PlayerTestHelper.generatePlayerRPClasses();
	}

	@Test
	public void lobbyReturnIsDirectlyInFrontOfArenaMaster() {
		assertEquals(ChallengeArenaZone.NPC_X, ChallengeArenaZone.LOBBY_RETURN_X);
		assertEquals(ChallengeArenaZone.NPC_Y + 1, ChallengeArenaZone.LOBBY_RETURN_Y);
	}

	@Test
	public void teleportToLobbyPlacesWinnerFacingArenaMaster() {
		final String zoneName = "int_tarnow_challenge_arena_return_test";
		final StendhalRPZone zone = new StendhalRPZone(zoneName, 64, 64);
		MockStendlRPWorld.get().addRPZone(zone);

		final ChallengeArenaInfo info = new ChallengeArenaInfo(
				new Area(zone, new Rectangle2D.Double(1, 1, 62, 39)),
				zone, zoneName,
				ChallengeArenaZone.LOBBY_RETURN_X,
				ChallengeArenaZone.LOBBY_RETURN_Y,
				32, 19);

		final Player player = PlayerTestHelper.createPlayer("ArenaWinner");
		player.setPosition(32, 19);
		zone.add(player);

		assertTrue(info.teleportToLobby(player));
		assertSame(zone, player.getZone());
		assertEquals(ChallengeArenaZone.LOBBY_RETURN_X, player.getX());
		assertEquals(ChallengeArenaZone.LOBBY_RETURN_Y, player.getY());
		assertEquals(Direction.UP, player.getDirection());
	}
}
