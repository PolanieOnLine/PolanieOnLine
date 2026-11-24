package games.stendhal.client.entity;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.GameObjects;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.util.UserInterfaceTestHelper;
import games.stendhal.common.Direction;
import marauroa.common.game.RPObject;

/** Keyboard and mouse share the same confirmed, bounded movement stream. */
public class ConfirmedUserMovementTest {
	@BeforeClass public static void initializeClient() {
		UserInterfaceTestHelper.initUserInterface();
		GameObjects.createInstance(new StaticGameLayers());
	}

	@After public void clearUser() { User.setNull(); }

	@Test public void aKeyPressCannotMovePastTheServerBeforeAReply() {
		final Session session = new Session();
		session.user.predictMovement(Direction.RIGHT, false);
		session.user.update(900);
		assertEquals(20, session.user.getRenderX(), 0.000001);
	}

	@Test public void movementCannotOvershootAnAcknowledgedTileEvenWithALateStop() {
		for (final Direction direction : new Direction[] {Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			for (final double speed : new double[] {0.5, 1.0, 1.1, 1.5, 2.0, 4.5}) {
				final Session session = new Session();
				session.packet(20 + direction.getdx(), 20 + direction.getdy(), direction, speed);
				double previous = 0;
				for (int frame = 0; frame < 90; frame++) {
					session.user.update(16);
					final double progress = (session.user.getRenderX() - 20) * direction.getdx()
							+ (session.user.getRenderY() - 20) * direction.getdy();
					assertTrue("confirmed motion must not reverse", progress >= previous - 0.000001);
					assertTrue("no extra tile may be invented", progress <= 1.000001);
					previous = progress;
				}
				session.packet(20 + direction.getdx(), 20 + direction.getdy(), direction, 0);
				session.user.update(600);
				assertEquals(20 + direction.getdx(), session.user.getRenderX(), 0.000001);
				assertEquals(20 + direction.getdy(), session.user.getRenderY(), 0.000001);
			}
		}
	}

	@Test public void anEarlyStopStillFinishesItsConfirmedStepWithoutRecoil() {
		final Session session = new Session();
		session.packet(21, 20, Direction.RIGHT, 1.1);
		session.user.update(60);
		final double before = session.user.getRenderX();
		assertTrue(before > 20 && before < 21);
		session.packet(21, 20, Direction.RIGHT, 0);
		for (int frame = 0; frame < 60; frame++) {
			session.user.update(16);
			assertTrue(session.user.getRenderX() >= before);
			assertTrue(session.user.getRenderX() <= 21.000001);
		}
		assertEquals(21, session.user.getRenderX(), 0.000001);
		assertTrue(session.user.stopped());
	}

	@Test public void successiveTurnsFollowConfirmedCornersInsteadOfCuttingOrRewindingThem() {
		final Session session = new Session();
		session.packet(21, 20, Direction.RIGHT, 1.5);
		session.user.update(80);
		session.user.predictMovement(Direction.UP, false);
		session.packet(21, 19, Direction.UP, 1.5);
		session.user.predictMovement(Direction.LEFT, false);
		session.packet(20, 19, Direction.LEFT, 0);
		double previousX = session.user.getRenderX(), previousY = 20;
		boolean reachedCorner = false, reachedSecondCorner = false;
		for (int frame = 0; frame < 100; frame++) {
			session.user.update(10);
			final double x = session.user.getRenderX(), y = session.user.getRenderY();
			assertTrue("frame must not teleport", Math.hypot(x - previousX, y - previousY) < 0.15);
			if (!reachedCorner && Math.abs(y - 20) < 0.000001) {
				assertTrue(x >= previousX);
			} else if (!reachedSecondCorner && Math.abs(x - 21) < 0.000001) {
				reachedCorner = true;
				assertTrue(y <= previousY);
			} else {
				reachedCorner = true;
				reachedSecondCorner = true;
				assertEquals("a rendered frame must remain on the confirmed grid route", 19, y, 0.000001);
				assertTrue(x <= previousX);
			}
			previousX = x; previousY = y;
		}
		assertTrue(reachedCorner && reachedSecondCorner);
		assertEquals(20, session.user.getRenderX(), 0.000001);
		assertEquals(19, session.user.getRenderY(), 0.000001);
	}

	@Test public void aNewZoneDiscardsAnUnfinishedRoute() {
		final Session session = new Session();
		session.packet(21, 20, Direction.RIGHT, 1.1);
		session.user.update(60);
		final RPObject teleport = new RPObject();
		teleport.put("zoneid", "another_zone");
		teleport.put("x", 5); teleport.put("y", 8); teleport.put("speed", 0);
		session.user.processPositioning(session.base, teleport);
		session.user.update(900);
		assertEquals(5, session.user.getRenderX(), 0.000001);
		assertEquals(8, session.user.getRenderY(), 0.000001);
	}

	@Test public void fractionalSpeedsUseTheRealServerStreamWithoutPausesOrBackwardCorrections() {
		games.stendhal.server.maps.MockStendlRPWorld.get();
		games.stendhal.server.maps.MockStendhalRPRuleProcessor.get();
		for (final double speed : new double[] {0.5, 1.0, 1.1, 1.5, 2.0, 4.5}) {
			final Session session = new Session();
			final games.stendhal.server.entity.player.Player player = utilities.PlayerTestHelper.createPlayer("stream-player");
			new games.stendhal.server.core.engine.StendhalRPZone("stream_test", 200, 60).add(player);
			player.setPosition(20, 20); player.stop(); player.setBaseSpeed(speed);
			player.resetAddedAndDeleted();
			final marauroa.common.game.RPAction move = new marauroa.common.game.RPAction();
			move.put("dir", Direction.RIGHT.get());
			new games.stendhal.server.actions.move.MoveAction().onAction(player, move);
			double previous = 20;
			for (int turn = 0; turn < 24; turn++) {
				player.applyMovement();
				final RPObject added = new RPObject(), removed = new RPObject();
				player.getDifferences(added, removed);
				session.user.processPositioning(session.base, added);
				session.base.applyDifferences(added, removed);
				player.resetAddedAndDeleted();
				for (int frame = 0; frame < 20; frame++) {
					session.user.update(15);
					final double current = session.user.getRenderX();
					assertTrue("backward frame at " + speed, current >= previous - 0.000001);
					assertTrue("unconfirmed tile at " + speed, current <= player.getX() + 0.000001);
					if (turn >= 3) assertTrue("pause during a held move at " + speed + " turn=" + turn + " frame=" + frame + " client=" + current + " server=" + player.getX() + " packet=" + added, current > previous + 0.000001);
					previous = current;
				}
			}
			player.stop();
			session.packet(player.getX(), 20, Direction.RIGHT, 0);
			for (int frame = 0; frame < 100; frame++) {
				session.user.update(15);
				assertTrue(session.user.getRenderX() >= previous - 0.000001);
				previous = session.user.getRenderX();
			}
			assertEquals(player.getX(), session.user.getRenderX(), 0.000001);
			assertTrue(session.user.stopped());
		}
	}

	@Test public void speedOnlyAndUnrelatedPacketsCannotInventOrRestartATile() {
		final Session session = new Session();
		session.packet(20, 20, Direction.RIGHT, 0.5);
		session.user.update(300);
		assertEquals(20, session.user.getRenderX(), 0.000001);
		session.packet(21, 20, Direction.RIGHT, 0.5);
		session.user.update(150);
		final double before = session.user.getRenderX();
		final RPObject unrelated = new RPObject(); unrelated.put("hp", 100);
		session.user.processPositioning(session.base, unrelated);
		session.user.update(450);
		assertTrue(before > 20 && before < 21);
		assertEquals(21, session.user.getRenderX(), 0.000001);
	}

	@Test public void aTeleportWithinTheSameZoneDoesNotAnimateThroughWalls() {
		final Session session = new Session();
		session.packet(21, 20, Direction.RIGHT, 1.1);
		session.user.update(60);
		session.packet(50, 40, Direction.LEFT, 0);
		assertEquals(50, session.user.getRenderX(), 0.000001);
		assertEquals(40, session.user.getRenderY(), 0.000001);
		session.user.update(600);
		assertEquals(50, session.user.getRenderX(), 0.000001);
	}

	@Test public void aWholeTapConfirmedTogetherWithItsStopDoesNotAddAnExtraWalkingDelay() {
		final Session session = new Session();
		session.packet(21, 20, Direction.RIGHT, 0);
		session.user.update(60);
		assertTrue(session.user.getRenderX() > 20 && session.user.getRenderX() < 21);
		session.user.update(60);
		assertEquals(21, session.user.getRenderX(), 0.000001);
		assertTrue(session.user.stopped());
	}

	@Test public void keyboardAndMouseUseTheSameMovementFrames() {
		final Session keyboard = new Session(), mouse = new Session();
		mouse.base.put("pathset", "");
		for (int turn = 0; turn < 6; turn++) {
			keyboard.packet(21 + turn, 20, Direction.RIGHT, turn == 5 ? 0 : 1.1);
			mouse.packet(21 + turn, 20, Direction.RIGHT, turn == 5 ? 0 : 1.1);
			for (int frame = 0; frame < 20; frame++) {
				keyboard.user.update(15); mouse.user.update(15);
				assertEquals(keyboard.user.getRenderX(), mouse.user.getRenderX(), 0.000001);
			}
		}
	}

	@Test public void finishingTheConfirmedTailNotifiesTheSpriteToStopWalking() {
		final Session session = new Session();
		session.packet(21, 20, Direction.RIGHT, 0);
		final java.util.concurrent.atomic.AtomicBoolean stoppedAnimation = new java.util.concurrent.atomic.AtomicBoolean();
		session.user.addChangeListener((changed, property) -> {
			if (property == ActiveEntity.PROP_SPEED && session.user.stopped()) stoppedAnimation.set(true);
		});
		session.user.update(120);
		assertTrue("the sprite must receive the final stop after finishing its tail", stoppedAnimation.get());
	}

	@Test public void theServerRouteWinsOverTheDirectionOfTheUpcomingMouseLeg() {
		final Session session = new Session();
		final RPObject diff = new RPObject();
		diff.put("x", 21); diff.put("y", 21); diff.put("dir", Direction.RIGHT.get()); diff.put("speed", 2);
		diff.put("movement_steps", "20,20;21,20;21,21");
		session.user.processPositioning(session.base, diff);
		session.user.update(80);
		assertTrue("first travel east along the real route", session.user.getRenderX() > 20);
		assertEquals("do not walk south through the wrong corner", 20, session.user.getRenderY(), 0.000001);
		session.user.update(600);
		assertEquals(21, session.user.getRenderX(), 0.000001);
		assertEquals(21, session.user.getRenderY(), 0.000001);
	}

	@Test public void anExplicitNearbyTeleportDiscardsTheOldRouteImmediately() {
		final Session session = new Session();
		session.packet(21, 20, Direction.RIGHT, 1.1);
		session.user.update(60);
		final RPObject teleport = new RPObject();
		teleport.put("x", 22); teleport.put("y", 20); teleport.put("speed", 0);
		teleport.put("movement_reset", 1);
		session.user.processPositioning(session.base, teleport);
		assertEquals(22, session.user.getRenderX(), 0.000001);
		session.user.update(900);
		assertEquals(22, session.user.getRenderX(), 0.000001);
	}

	@Test public void keyboardStartsMovingInTheFirstFrameBeforeTheNetworkReply() {
		final Session session = new Session();
		final marauroa.common.game.RPAction action = new marauroa.common.game.RPAction();
		session.user.prepareKeyboardMove(Direction.RIGHT, action);
		session.user.update(16);
		assertTrue("a key press must move immediately", session.user.getRenderX() > 20.02);
		assertEquals(20, session.user.getRenderY(), 0.000001);
		assertTrue(action.has("start_step"));
		session.user.update(3000);
		assertTrue("a lost reply must not extrapolate another tile", session.user.getRenderX() < 20.501);
	}

	@Test public void confirmingAnImmediateStartNeverReplaysOrRewindsItsFirstStep() {
		for (final Direction direction : new Direction[] {Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			final Session session = new Session();
			session.user.prepareKeyboardMove(direction, new marauroa.common.game.RPAction());
			session.user.update(120);
			double previous = (session.user.getRenderX() - 20) * direction.getdx() + (session.user.getRenderY() - 20) * direction.getdy();
			assertTrue(previous > 0);
			session.packet(20 + direction.getdx(), 20 + direction.getdy(), direction, 1.1);
			for (int frame = 0; frame < 60; frame++) {
				session.user.update(15);
				final double current = (session.user.getRenderX() - 20) * direction.getdx() + (session.user.getRenderY() - 20) * direction.getdy();
				assertTrue("first-step acknowledgement must not rewind", current >= previous - 0.000001);
				assertTrue(current <= 1.000001);
				previous = current;
			}
		}
	}

	@Test public void facingOnlyDoesNotStartAPreviewedWalk() {
		final Session session = new Session();
		session.user.predictMovement(Direction.UP, true);
		session.user.update(300);
		assertEquals(20, session.user.getRenderX(), 0.000001);
		assertEquals(20, session.user.getRenderY(), 0.000001);
	}

	@Test public void immediateKeyboardInputStaysSmoothThroughTheRealClientAndServerPipeline() {
		games.stendhal.server.maps.MockStendlRPWorld.get();
		games.stendhal.server.maps.MockStendhalRPRuleProcessor.get();
		for (final double speed : new double[] {0.5, 1.0, 1.1, 1.5, 2.0, 4.5}) {
			final Session session = new Session();
			final String zone = "immediate_stream_" + Double.toString(speed).replace('.', '_');
			session.base.put("zoneid", zone);
			final games.stendhal.server.entity.player.Player player = utilities.PlayerTestHelper.createPlayer("immediate-player");
			new games.stendhal.server.core.engine.StendhalRPZone(zone, 200, 60).add(player);
			player.setPosition(20, 20); player.stop(); player.setBaseSpeed(speed); player.resetAddedAndDeleted();
			final java.util.List<marauroa.common.game.RPAction> sent = new java.util.ArrayList<>();
			final games.stendhal.client.MockStendhalClient client = new games.stendhal.client.MockStendhalClient() {
				@Override public RPObject getPlayer() { return session.base; }
				@Override public void send(final marauroa.common.game.RPAction action) { sent.add(action); }
			};
			try {
				client.addDirection(Direction.RIGHT, false);
				session.user.update(60);
				double previous = session.user.getRenderX();
				assertTrue("the actual keyboard path must react before sending a perception", previous > 20);
				assertTrue(sent.get(0).has("start_step"));
				new games.stendhal.server.actions.move.MoveAction().onAction(player, sent.remove(0));
				for (int turn = 0; turn < 24; turn++) {
					player.applyMovement();
					final RPObject added = new RPObject(), removed = new RPObject();
					player.getDifferences(added, removed);
					session.user.processPositioning(session.base, added);
					session.base.applyDifferences(added, removed);
					player.resetAddedAndDeleted();
					for (int frame = 0; frame < 20; frame++) {
						session.user.update(15);
						final double current = session.user.getRenderX();
						assertTrue("acknowledgement recoiled at " + speed, current >= previous - 0.000001);
						assertTrue(current <= Math.max(20.5, player.getX()) + 0.000001);
						if (turn >= 4) assertTrue("held keyboard input paused at " + speed, current > previous + 0.000001);
						previous = current;
					}
				}
			} finally {
				games.stendhal.client.StendhalClient.resetClient();
			}
		}
	}

	@Test public void aServerRejectedFirstStepSettlesAtTheRealTile() {
		final Session session = new Session();
		session.user.prepareKeyboardMove(Direction.RIGHT, new marauroa.common.game.RPAction());
		session.user.update(60);
		assertTrue(session.user.getRenderX() > 20);
		final RPObject blocked = new RPObject(); blocked.put("speed", 0);
		session.user.processPositioning(session.base, blocked);
		session.user.update(120);
		assertEquals(20, session.user.getRenderX(), 0.000001);
		session.user.update(900);
		assertEquals(20, session.user.getRenderX(), 0.000001);
		assertTrue(session.user.stopped());
	}

	private static class Session {
		final RPObject base = new RPObject();
		final User user;
		Session() {
			User.setNull();
			base.put("name", "confirmed-motion"); base.put("zoneid", "test_zone");
			base.put("x", 20); base.put("y", 20); base.put("dir", Direction.RIGHT.get()); base.put("speed", 0);
			user = new User(); user.initialize(base);
		}
		void packet(final int x, final int y, final Direction direction, final double speed) {
			final RPObject diff = new RPObject();
			if (base.getInt("x") != x) diff.put("x", x);
			if (base.getInt("y") != y) diff.put("y", y);
			if (base.getInt("dir") != direction.get()) diff.put("dir", direction.get());
			if (base.getDouble("speed") != speed) diff.put("speed", speed);
			user.processPositioning(base, diff);
			base.applyDifferences(diff, null);
		}
	}
}
