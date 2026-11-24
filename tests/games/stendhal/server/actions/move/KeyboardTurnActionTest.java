package games.stendhal.server.actions.move;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class KeyboardTurnActionTest {
	@BeforeClass public static void initialize() {
		MockStendlRPWorld.get(); MockStendhalRPRuleProcessor.get();
	}

	@Test public void theTurnUsesTheCornerWithoutLosingItsFractionalMovementBudget() {
		final Player player = moving(new StendhalRPZone("corner_budget", 60, 60));
		new MoveAction().onAction(player, turn(player, Direction.UP));
		assertTrue(player.hasQueuedKeyboardTurn());
		assertFalse("Keyboard turns must still release keys when focus is lost", player.has("pathset"));
		assertEquals(20, player.getX());
		player.applyMovement();
		assertEquals(21, player.getX());
		assertEquals(20, player.getY());
		assertEquals(Direction.UP, player.getDirection());
		assertFalse(player.hasQueuedKeyboardTurn());
		assertFalse(player.hasPath());
		assertFalse(player.has("pathset"));
		player.applyMovement();
		assertEquals(18, player.getY());
		assertEquals(3, player.getStepsTaken());
	}

	@Test public void releasingTheQueuedKeyReturnsToTheRemainingHeldDirection() {
		final Player player = moving(new StendhalRPZone("corner_release", 60, 60));
		new MoveAction().onAction(player, turn(player, Direction.UP));
		final RPAction release = new RPAction();
		release.put("dir", -Direction.UP.get());
		new MoveAction().onAction(player, release);
		player.applyMovement();
		assertEquals(Direction.RIGHT, player.getDirection());
		assertEquals(21, player.getX());
		assertFalse(player.has("pathset"));
		assertFalse(player.hasQueuedKeyboardTurn());
	}

	@Test public void aBlockedCornerStopsInsteadOfTurningThroughTheWall() {
		final Player player = moving(new StendhalRPZone("corner_wall", 60, 60) {
			@Override public boolean simpleCollides(final Entity entity, final double x, final double y,
					final double width, final double height) {
				return x == 21 || super.simpleCollides(entity, x, y, width, height);
			}
		});
		new MoveAction().onAction(player, turn(player, Direction.UP));
		player.applyMovement();
		assertEquals(20, player.getX());
		assertEquals(20, player.getY());
		assertTrue(player.stopped());
		assertFalse(player.hasQueuedKeyboardTurn());
		assertFalse(player.has("pathset"));
	}

	@Test public void anotherInputCancelsTheCornerWithoutLeavingMousePathFlags() {
		final Player player = moving(new StendhalRPZone("corner_cancel", 60, 60));
		new MoveAction().onAction(player, turn(player, Direction.UP));
		final RPAction move = new RPAction(); move.put("dir", Direction.RIGHT.get());
		new MoveAction().onAction(player, move);
		assertFalse(player.hasQueuedKeyboardTurn());
		assertFalse(player.hasPath());
		assertFalse(player.has("pathset"));
	}

	@Test public void keyboardTakeoverClearsThePreviousMousePathFlag() {
		final Player player = moving(new StendhalRPZone("corner_mouse_takeover", 60, 60));
		player.setPath(new games.stendhal.server.core.pathfinder.FixedPath(java.util.Arrays.asList(
				new games.stendhal.server.core.pathfinder.Node(20, 20),
				new games.stendhal.server.core.pathfinder.Node(22, 20)), false));
		assertTrue(player.has("pathset"));
		final RPAction move = new RPAction(); move.put("dir", Direction.UP.get());
		new MoveAction().onAction(player, move);
		assertFalse(player.hasPath());
		assertFalse(player.has("pathset"));
	}

	private Player moving(final StendhalRPZone zone) {
		final Player player = PlayerTestHelper.createPlayer("corner-player");
		zone.add(player); player.setPosition(20, 20); player.setBaseSpeed(1.5);
		final RPAction move = new RPAction(); move.put("dir", Direction.RIGHT.get());
		new MoveAction().onAction(player, move);
		return player;
	}

	private RPAction turn(final Player player, final Direction next) {
		final RPAction action = new RPAction(); action.put("dir", next.get());
		action.put("turn_x", 21); action.put("turn_y", 20);
		action.put("turn_from", Direction.RIGHT.get());
		action.put("turn_zone", player.getZone().getName());
		return action;
	}
}
