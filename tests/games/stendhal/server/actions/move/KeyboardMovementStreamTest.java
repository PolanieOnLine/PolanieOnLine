package games.stendhal.server.actions.move;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.server.actions.attack.StopAction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class KeyboardMovementStreamTest {
	@BeforeClass public static void initializeWorld() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test public void aTapAtFractionalSpeedStillWalksOneRealCollisionCheckedTile() {
		final Player player = player("short_tap");
		player.setBaseSpeed(0.5);
		MockStendhalRPRuleProcessor.get().setTurn(500);
		move(player, Direction.RIGHT);
		player.applyMovement();
		MockStendhalRPRuleProcessor.get().setTurn(501);
		new StopAction().onAction(player, new RPAction());
		for (int i = 0; i < 4; i++) player.applyMovement();
		assertEquals(21, player.getX());
		assertTrue(player.stopped());
		assertFalse(player.has("pathset"));
	}

	@Test public void aNewMoveIsNotCancelledByAnOlderDeferredStop() {
		final Player player = player("newer_key");
		MockStendhalRPRuleProcessor.get().setTurn(600);
		TurnNotifier.get().logic(599);
		move(player, Direction.RIGHT);
		new StopAction().onAction(player, new RPAction());
		move(player, Direction.UP);
		MockStendhalRPRuleProcessor.get().setTurn(601);
		TurnNotifier.get().logic(601);
		player.applyMovement();
		assertEquals(19, player.getY());
		assertFalse(player.stopped());
	}

	@Test public void aNewKeyboardMoveClearsTheMousePathMarker() {
		final Player player = player("mouse_to_key");
		player.setPath(new games.stendhal.server.core.pathfinder.FixedPath(java.util.Arrays.asList(
				new games.stendhal.server.core.pathfinder.Node(20, 20),
				new games.stendhal.server.core.pathfinder.Node(25, 20)), false));
		move(player, Direction.UP);
		assertFalse(player.hasPath());
		assertFalse("focus loss must still release the keys", player.has("pathset"));
	}

	@Test public void tappingIntoAWallDoesNotCompleteATileThroughIt() {
		final Player player = PlayerTestHelper.createPlayer("wall-tap");
		new StendhalRPZone("wall_tap", 60, 60) {
			@Override public boolean simpleCollides(final Entity entity, final double x, final double y,
					final double width, final double height) {
				return x == 21 || super.simpleCollides(entity, x, y, width, height);
			}
		}.add(player);
		player.setPosition(20, 20); player.stop(); player.setBaseSpeed(0.5);
		move(player, Direction.RIGHT);
		new StopAction().onAction(player, new RPAction());
		for (int i = 0; i < 4; i++) player.applyMovement();
		assertEquals(20, player.getX());
		assertTrue(player.stopped());
	}

	@Test public void facingCancelsATapInsteadOfResumingItsOldPath() {
		final Player player = player("face_after_tap");
		move(player, Direction.RIGHT);
		new StopAction().onAction(player, new RPAction());
		final RPAction face = new RPAction(); face.put("dir", Direction.UP.get());
		new FaceAction().onAction(player, face);
		for (int i = 0; i < 4; i++) player.applyMovement();
		assertEquals(20, player.getX());
		assertEquals(20, player.getY());
		assertEquals(Direction.UP, player.getDirection());
		assertFalse(player.hasPath());
	}

	@Test public void aFastMouseCornerReportsTheTilesActuallyWalked() {
		final Player player = player("mouse_corner");
		player.setBaseSpeed(2);
		player.setPath(new games.stendhal.server.core.pathfinder.FixedPath(java.util.Arrays.asList(
				new games.stendhal.server.core.pathfinder.Node(20, 20),
				new games.stendhal.server.core.pathfinder.Node(21, 20),
				new games.stendhal.server.core.pathfinder.Node(21, 21),
				new games.stendhal.server.core.pathfinder.Node(22, 21)), false));
		player.applyMovement();
		assertEquals(21, player.getX()); assertEquals(21, player.getY());
		assertEquals(Direction.RIGHT, player.getDirection());
		assertTrue(player.has("movement_steps"));
		assertEquals("20,20;21,20;21,21", player.get("movement_steps"));
	}

	@Test public void nearbyTeleportsPublishAnExplicitNewRelocation() {
		final Player player = player("nearby_teleport");
		assertTrue(player.teleport(player.getZone(), 22, 20, null, null));
		assertTrue(player.has("movement_reset"));
		final String first = player.get("movement_reset");
		assertEquals("", player.get("movement_steps"));
		assertTrue(player.teleport(player.getZone(), 24, 20, null, null));
		assertNotEquals(first, player.get("movement_reset"));
	}

	private Player player(final String zone) {
		final Player player = PlayerTestHelper.createPlayer("keyboard-player");
		new StendhalRPZone(zone, 60, 60).add(player);
		player.setPosition(20, 20); player.stop(); player.setBaseSpeed(1.0);
		return player;
	}

	private void move(final Player player, final Direction direction) {
		final RPAction action = new RPAction(); action.put("dir", direction.get());
		new MoveAction().onAction(player, action);
	}
}
