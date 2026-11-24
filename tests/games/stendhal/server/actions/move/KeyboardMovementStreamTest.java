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

	@Test public void anImmediateStartCompletesItsFirstTileBeforeANewerDirection() {
		final Player player = player("immediate_start");
		player.setBaseSpeed(1.0);
		final RPAction start = new RPAction(); start.put("dir", Direction.RIGHT.get()); start.put("start_step", "");
		new MoveAction().onAction(player, start);
		move(player, Direction.UP);
		player.applyMovement();
		assertEquals("the client has already begun the eastward step", 21, player.getX());
		assertEquals(20, player.getY());
		player.applyMovement();
		assertEquals(21, player.getX()); assertEquals(19, player.getY());
	}

	@Test public void releasingAnImmediateStartFinishesExactlyOneTile() {
		final Player player = player("immediate_release");
		player.setBaseSpeed(1.5);
		final RPAction start = new RPAction(); start.put("dir", Direction.RIGHT.get()); start.put("start_step", "");
		new MoveAction().onAction(player, start);
		final RPAction release = new RPAction(); release.put("dir", -Direction.RIGHT.get());
		new MoveAction().onAction(player, release);
		new StopAction().onAction(player, new RPAction());
		for (int i = 0; i < 4; i++) player.applyMovement();
		assertEquals(21, player.getX()); assertEquals(20, player.getY()); assertTrue(player.stopped());
	}

	@Test public void aZeroSpeedStartStillAcknowledgesTheStationaryTile() {
		final Player player = player("zero_speed_start");
		player.setBaseSpeed(0); player.resetAddedAndDeleted();
		final RPAction start = new RPAction(); start.put("dir", Direction.RIGHT.get()); start.put("start_step", "");
		new MoveAction().onAction(player, start);
		final marauroa.common.game.RPObject changes = new marauroa.common.game.RPObject();
		player.getDifferences(changes, new marauroa.common.game.RPObject());
		assertTrue("otherwise the client waits for a first step that cannot happen", changes.has("speed"));
		assertEquals(0, changes.getDouble("speed"), 0.000001);
		assertEquals(20, changes.getInt("x"));
	}

	@Test public void heldImmediateStartsContinueAcrossZoneBordersButReleasedTapsStop() {
		for (final boolean released : new boolean[] {false, true}) {
			final StendhalRPZone west = new StendhalRPZone("immediate_border_w_" + released, 4, 4);
			final StendhalRPZone east = new StendhalRPZone("immediate_border_e_" + released, 4, 4);
			west.setPosition(released ? 778 : 777, 5000000, 5000000);
			east.setPosition(released ? 778 : 777, 5000004, 5000000);
			MockStendlRPWorld.get().addRPZone(west); MockStendlRPWorld.get().addRPZone(east);
			final Player player = PlayerTestHelper.createPlayer("border-player");
			west.add(player); player.setPosition(3, 1); player.stop(); player.setBaseSpeed(1);
			player.put(games.stendhal.common.constants.Actions.MOVE_CONTINUOUS, "");
			final RPAction start = new RPAction(); start.put("dir", Direction.RIGHT.get()); start.put("start_step", "");
			new MoveAction().onAction(player, start);
			if (released) {
				final RPAction release = new RPAction(); release.put("dir", -Direction.RIGHT.get());
				new MoveAction().onAction(player, release);
				new StopAction().onAction(player, new RPAction());
			}
			player.applyMovement();
			assertSame(east, player.getZone());
			assertEquals(0, player.getX());
			assertEquals(released, player.stopped());
			player.applyMovement();
			assertEquals(released ? 0 : 1, player.getX());
		}
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
