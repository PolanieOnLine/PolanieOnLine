package games.stendhal.server.actions.attack;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.server.actions.move.MoveAction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import marauroa.common.game.RPAction;
import utilities.PlayerTestHelper;

public class KeyboardStopActionTest {
	@BeforeClass public static void initialize() {
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Test public void completingTheLastTilePreservesFractionalProgressAndUsesNormalMovement() {
		final Player player = moving("tail_fraction", 0.5);
		player.applyMovement();
		assertEquals(20, player.getX());
		new StopAction().onAction(player, stop(player, 21, 20));
		assertEquals("A stop request must not teleport the player", 20, player.getX());
		assertFalse("A keyboard step must not disable key releases on focus loss", player.has("pathset"));
		player.applyMovement();
		assertEquals(21, player.getX());
		assertTrue(player.stopped());
		player.applyMovement();
		assertEquals(21, player.getX());
	}

	@Test public void aFinishingStepCannotCrossAWall() {
		final Player player = moving(new StendhalRPZone("tail_wall", 60, 60) {
			@Override public boolean simpleCollides(final Entity entity, final double x, final double y,
					final double width, final double height) {
				return x == 21 || super.simpleCollides(entity, x, y, width, height);
			}
		}, 1.1);
		new StopAction().onAction(player, stop(player, 21, 20));
		for (int turn = 0; turn < 4; turn++) player.applyMovement();
		assertEquals(20, player.getX());
		assertTrue(player.stopped());
	}

	@Test public void theServerNeverWalksBackToAnOlderClientEndpoint() {
		final Player player = moving("tail_old_endpoint", 1.1);
		player.setPosition(22, 20);
		new StopAction().onAction(player, stop(player, 21, 20));
		for (int turn = 0; turn < 4; turn++) player.applyMovement();
		assertEquals(22, player.getX());
		assertTrue(player.stopped());
	}

	@Test public void invalidTargetsCannotStartADifferentRoute() {
		for (final String invalid : new String[] {"zone", "direction", "diagonal", "distance", "number"}) {
			final Player player = moving("tail_invalid_" + invalid, 1.1);
			final RPAction action = stop(player, 21, 20);
			if ("zone".equals(invalid)) action.put("finish_zone", "another_zone");
			if ("direction".equals(invalid)) action.put("finish_dir", Direction.LEFT.get());
			if ("diagonal".equals(invalid)) action.put("finish_y", 21);
			if ("distance".equals(invalid)) action.put("finish_x", Integer.MAX_VALUE);
			if ("number".equals(invalid)) action.put("finish_x", "not a coordinate");
			new StopAction().onAction(player, action);
			assertFalse(invalid, player.hasPath());
			assertTrue(invalid, player.stopped());
		}
	}

	@Test public void anotherDirectionCancelsTheFinishingPath() {
		final Player player = moving("tail_new_key", 1.1);
		new StopAction().onAction(player, stop(player, 21, 20));
		final RPAction next = new RPAction();
		next.put("dir", Direction.UP.get());
		new MoveAction().onAction(player, next);
		assertFalse(player.hasPath());
		assertFalse(player.has("pathset"));
		player.applyMovement();
		assertEquals(20, player.getX());
		assertEquals(19, player.getY());
	}

	@Test public void ordinaryStopsAndAutoWalkKeepTheirExistingBehavior() {
		final Player normal = moving("tail_legacy", 1.1);
		new StopAction().onAction(normal, new RPAction());
		assertTrue(normal.stopped());
		assertFalse(normal.hasPath());
		final Player automatic = moving("tail_auto", 1.1);
		automatic.put("autowalk", "");
		new StopAction().onAction(automatic, stop(automatic, 21, 20));
		assertFalse(automatic.stopped());
		assertFalse(automatic.hasPath());
	}

	private Player moving(final String zone, final double speed) {
		return moving(new StendhalRPZone(zone, 60, 60), speed);
	}

	private Player moving(final StendhalRPZone zone, final double speed) {
		final Player player = PlayerTestHelper.createPlayer("tail-test");
		zone.add(player);
		player.setPosition(20, 20);
		player.setBaseSpeed(speed);
		final RPAction move = new RPAction();
		move.put("dir", Direction.RIGHT.get());
		MockStendhalRPRuleProcessor.get().setTurn(100);
		new MoveAction().onAction(player, move);
		MockStendhalRPRuleProcessor.get().setTurn(101);
		return player;
	}

	private RPAction stop(final Player player, final int x, final int y) {
		final RPAction action = new RPAction();
		action.put("finish_x", x); action.put("finish_y", y);
		action.put("finish_dir", Direction.RIGHT.get());
		action.put("finish_zone", player.getZone().getName());
		return action;
	}
}
