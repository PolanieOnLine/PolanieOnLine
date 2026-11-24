package games.stendhal.client.entity;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.GameObjects;
import games.stendhal.client.MockStendhalClient;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.StendhalClient;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.util.UserInterfaceTestHelper;
import games.stendhal.common.Direction;
import games.stendhal.server.actions.attack.StopAction;
import games.stendhal.server.actions.move.MoveAction;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.maps.MockStendlRPWorld;
import games.stendhal.server.maps.MockStendhalRPRuleProcessor;
import marauroa.common.game.RPAction;
import marauroa.common.game.RPObject;
import utilities.PlayerTestHelper;

/** Exercises keyboard packets, real server actions and client frames together. */
public class KeyboardTapMovementTest {
	private String savedSpeed, savedJitter;

	@BeforeClass public static void initialize() {
		UserInterfaceTestHelper.initUserInterface();
		GameObjects.createInstance(new StaticGameLayers());
		MockStendlRPWorld.get();
		MockStendhalRPRuleProcessor.get();
	}

	@Before public void configurePrediction() {
		final WtWindowManager manager = WtWindowManager.getInstance();
		savedSpeed = manager.getProperty("predictor.speed", "0.3");
		savedJitter = manager.getProperty("predictor.jitter", "0.0");
		manager.setProperty("predictor.speed", "0.6");
		manager.setProperty("predictor.jitter", "0.0");
		User.setNull();
	}

	@After public void cleanUp() {
		WtWindowManager.getInstance().setProperty("predictor.speed", savedSpeed);
		WtWindowManager.getInstance().setProperty("predictor.jitter", savedJitter);
		User.setNull();
		StendhalClient.resetClient();
	}

	@Test public void aReleasedKeyCannotExtrapolatePastItsFinalTileWhileTheReplyIsInFlight() {
		for (final Direction direction : new Direction[] {Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN}) {
			for (final double speed : new double[] {0.5, 1.0, 1.1, 1.5, 2.0, 4.5}) {
				final Session session = new Session(direction, speed);
				session.press();
				session.user.update(60);
				while (session.server.getX() == 20 && session.server.getY() == 20) session.server.applyMovement();
				session.deliver();
				session.user.update(100);
				final double released = session.progress();
				final double endpoint = Math.ceil(released - 0.000001);
				session.client.removeDirection(direction, false);
				for (int frame = 0; frame < 40; frame++) {
					session.user.update(16);
					assertTrue(direction + " speed " + speed + ": released movement passed its final tile",
							session.progress() <= endpoint + 0.000001);
				}
			}
		}
	}

	@Test public void finishingAKeyboardStepDoesNotReverseWhenTheStopReachesTheServer() {
		for (final Direction direction : new Direction[] {Direction.RIGHT, Direction.LEFT, Direction.UP, Direction.DOWN}) {
			for (final double speed : new double[] {1.0, 1.1, 1.5, 2.0}) {
				final Session session = new Session(direction, speed);
				session.press();
				session.user.update(60);
				session.server.applyMovement();
				session.deliver();
				session.user.update(180);
				session.client.removeDirection(direction, false);
				for (final RPAction action : session.drain()) {
					if ("move".equals(action.get("type"))) new MoveAction().onAction(session.server, action);
					if ("stop".equals(action.get("type"))) new StopAction().onAction(session.server, action);
				}
				double previous = session.progress();
				for (int turn = 0; turn < 4; turn++) {
					session.server.applyMovement();
					session.deliver();
					for (int frame = 0; frame < 20; frame++) {
						session.user.update(15);
						assertTrue(direction + " speed " + speed + ": a short keyboard trip recoiled",
								session.progress() >= previous - 0.000001);
						previous = session.progress();
					}
				}
				assertTrue(session.server.stopped());
				assertEquals(session.server.getX(), session.user.getX(), 0.000001);
				assertEquals(session.server.getY(), session.user.getY(), 0.000001);
			}
		}
	}

	@Test public void keyUpBoundsPredictionBeforeTheRepeatFilterSendsTheNetworkRelease() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.user.update(60);
		session.client.prepareDirectionRelease(Direction.RIGHT, false);
		for (int frame = 0; frame < 60; frame++) session.user.update(16);
		assertTrue(session.progress() <= 1.000001);
		assertTrue("The repeat filter must not send an early stop", session.sent.isEmpty());
		session.client.removeDirection(Direction.RIGHT, false);
		final List<RPAction> actions = session.drain();
		assertEquals(21, actions.get(1).getInt("finish_x"));
	}

	@Test public void releasingOneOfTwoHeldKeysDoesNotStopTheRemainingDirection() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.client.addDirection(Direction.UP, false);
		session.drain();
		session.client.prepareDirectionRelease(Direction.UP, false);
		session.client.removeDirection(Direction.UP, false);
		final List<RPAction> actions = session.drain();
		assertEquals("move", actions.get(1).get("type"));
		assertEquals(Direction.RIGHT.get(), actions.get(1).getInt("dir"));
		session.user.update(900);
		assertTrue(session.progress() > 1);
	}

	@Test public void aNewKeyPressCancelsThePreviousStepLimit() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.user.update(60);
		session.client.removeDirection(Direction.RIGHT, false);
		session.user.update(600);
		final double before = session.progress();
		session.user.predictMovement(Direction.RIGHT, false);
		session.user.update(600);
		assertTrue(session.progress() > before);
	}

	@Test public void startingMouseMovementCancelsTheKeyboardLimit() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.client.removeDirection(Direction.RIGHT, false);
		session.user.cancelKeyboardStopPrediction();
		session.user.update(900);
		assertTrue(session.progress() > 1);
	}

	@Test public void aNewDirectionAfterKeyUpStartsFromItsConfirmedCorner() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.user.update(60);
		session.client.removeDirection(Direction.RIGHT, false);
		session.user.update(600);
		final double beforeY = session.user.getY();
		session.drain();
		session.client.addDirection(Direction.UP, false);
		session.user.predictMovement(Direction.UP, false);
		for (final RPAction action : session.drain()) new MoveAction().onAction(session.server, action);
		session.server.applyMovement(); session.deliver();
		session.user.update(60);
		assertEquals(Direction.UP, session.user.getDirection());
		assertTrue(session.user.getY() < beforeY);
	}

	@Test public void releasingBeforeTheFirstReplyStillIgnoresAnOlderFacingPacket() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.user.update(60);
		session.client.prepareDirectionRelease(Direction.RIGHT, false);
		final RPObject facing = new RPObject();
		facing.put("dir", Direction.UP.get());
		facing.put("zoneid", session.base.get("zoneid"));
		session.user.processPositioning(session.base, facing);
		final double before = session.progress();
		session.user.update(60);
		assertEquals(Direction.RIGHT, session.user.getDirection());
		assertTrue(session.progress() >= before);
	}

	@Test public void aTapWithDisabledStartupPredictionStillCompletesOneTile() {
		WtWindowManager.getInstance().setProperty("predictor.speed", "0.3");
		final Session session = new Session(Direction.RIGHT, 0.5);
		session.press();
		session.user.update(45);
		session.client.removeDirection(Direction.RIGHT, false);
		for (final RPAction action : session.drain()) {
			if ("move".equals(action.get("type"))) new MoveAction().onAction(session.server, action);
			if ("stop".equals(action.get("type"))) new StopAction().onAction(session.server, action);
		}
		double previous = session.progress();
		for (int turn = 0; turn < 4; turn++) {
			session.server.applyMovement();
			session.deliver();
			for (int frame = 0; frame < 20; frame++) {
				session.user.update(15);
				assertTrue(session.progress() >= previous - 0.000001);
				previous = session.progress();
			}
		}
		assertEquals(21, session.server.getX());
		assertEquals(21, session.user.getX(), 0.000001);
	}

	@Test public void aDeniedStartStillReceivesAnAuthoritativeStopWhenServerSpeedWasAlreadyZero() {
		final Session session = new Session(Direction.RIGHT, 0.0);
		session.press();
		session.user.update(60);
		session.client.removeDirection(Direction.RIGHT, false);
		for (final RPAction action : session.drain()) {
			if ("move".equals(action.get("type"))) new MoveAction().onAction(session.server, action);
			if ("stop".equals(action.get("type"))) new StopAction().onAction(session.server, action);
		}
		session.deliver();
		session.user.update(300);
		assertTrue(session.user.stopped());
		assertEquals(20, session.user.getX(), 0.000001);
	}

	@Test public void anOlderStopReplyCannotOverrideANewerDirection() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.server.applyMovement();
		session.deliver();
		session.user.update(180);
		session.client.removeDirection(Direction.RIGHT, false);
		final List<RPAction> release = session.drain();
		session.client.addDirection(Direction.UP, false);
		session.user.predictMovement(Direction.UP, false);
		final Direction predicted = session.user.getDirection();
		for (final RPAction action : release) {
			if ("move".equals(action.get("type"))) new MoveAction().onAction(session.server, action);
			if ("stop".equals(action.get("type"))) new StopAction().onAction(session.server, action);
		}
		session.deliver();
		assertEquals("A late stop must not replace the newer movement plan", predicted, session.user.getDirection());
		final double before = session.progress();
		session.user.update(60);
		assertTrue("An older stop must not reverse the pending step", session.progress() >= before - 0.000001);
	}

	@Test public void unsequencedServerStopsRemainAuthoritative() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press();
		session.user.update(60);
		session.server.applyMovement(); session.deliver();
		new StopAction().onAction(session.server, new RPAction());
		session.deliver();
		assertTrue("Legacy stop and non-keyboard commands must not be ignored", session.user.stopped());
	}

	@Test public void anEarlierTurnReplyCannotReplaceTheLatestRequestedTurn() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press(); session.server.applyMovement(); session.deliver(); session.user.update(300);
		session.client.addDirection(Direction.UP, false); session.user.predictMovement(Direction.UP, false);
		final List<RPAction> up = session.drain();
		session.client.addDirection(Direction.LEFT, false); session.user.predictMovement(Direction.LEFT, false);
		final List<RPAction> left = session.drain();
		for (final RPAction action : up) new MoveAction().onAction(session.server, action);
		session.server.applyMovement(); session.deliver();
		assertNotEquals("An older UP reply must not replace a newer LEFT plan", Direction.UP, session.user.getDirection());
		for (final RPAction action : left) new MoveAction().onAction(session.server, action);
		for (int turn = 0; turn < 3; turn++) {
			session.server.applyMovement(); session.deliver(); session.user.update(300);
		}
		assertEquals(Direction.LEFT, session.user.getDirection());
	}

	@Test public void aFilteredRepeatKeepsTheAlreadySentMovementIdentity() {
		final Session session = new Session(Direction.RIGHT, 1.1);
		session.press(); session.user.update(60);
		session.server.applyMovement(); session.deliver();
		session.client.prepareDirectionRelease(Direction.RIGHT, false);
		session.user.resumeKeyboardPrediction(Direction.RIGHT);
		assertTrue(session.sent.isEmpty());
		session.server.applyMovement(); session.deliver();
		assertTrue("A repeat must not wait for an input ID that was never sent", session.user.getX() >= session.server.getX() - 1.1);
	}

	@Test public void turningDuringALongKeyboardRunNeverCorrectsBackAlongTheOldDirection() {
		for (final Direction next : new Direction[] {Direction.UP, Direction.DOWN, Direction.LEFT}) {
			for (final double speed : new double[] {1.0, 1.1, 1.5, 2.0}) {
				final Session session = new Session(Direction.RIGHT, speed);
				session.press();
				for (int turn = 0; turn < 5; turn++) {
					session.server.applyMovement(); session.deliver(); session.user.update(300);
				}
				final double beforeX = session.user.getRenderX();
				session.client.addDirection(next, false);
				session.user.predictMovement(next, false);
				for (final RPAction action : session.drain()) new MoveAction().onAction(session.server, action);
				double previousX = beforeX;
				for (int turn = 0; turn < 3; turn++) {
					session.server.applyMovement(); session.deliver();
					for (int frame = 0; frame < 20; frame++) {
						session.user.update(15);
						final double current = session.user.getRenderX();
						if (next == Direction.LEFT && session.user.getDirection() == Direction.LEFT) {
							assertTrue("A confirmed reversal must keep going backwards", current <= previousX + 0.000001);
						} else {
							assertTrue("Turning " + next + " at speed " + speed + " pulled the player back along the old axis",
									current >= previousX - 0.000001);
						}
						assertTrue("A turn must not teleport the sprite in one frame", Math.abs(current - previousX) < 0.5);
						previousX = current;
					}
				}
				if (next != Direction.LEFT) assertTrue(session.user.getRenderX() >= beforeX - 0.000001);
			}
		}
	}

	private static final class Session {
		final Direction direction;
		final games.stendhal.server.entity.player.Player server;
		final RPObject base = new RPObject();
		final User user;
		final List<RPAction> sent = new ArrayList<>();
		final MockStendhalClient client;

		Session(final Direction direction, final double speed) {
			this.direction = direction;
			User.setNull();
			final String zoneName = "tap_" + direction + "_" + Double.toString(speed).replace('.', '_');
			server = PlayerTestHelper.createPlayer("tap-player");
			new StendhalRPZone(zoneName, 60, 60).add(server);
			server.setPosition(20, 20);
			server.stop();
			server.setDirection(direction);
			server.setBaseSpeed(speed);
			server.resetAddedAndDeleted();
			base.put("name", "tap-player");
			base.put("x", 20); base.put("y", 20);
			base.put("dir", direction.get()); base.put("speed", 0.0); base.put("zoneid", zoneName);
			user = new User();
			user.initialize(base);
			client = new MockStendhalClient() {
				@Override public RPObject getPlayer() { return base; }
				@Override public void send(final RPAction action) { sent.add(action); }
			};
		}

		void press() {
			MockStendhalRPRuleProcessor.get().setTurn(100);
			client.addDirection(direction, false);
			user.predictMovement(direction, false);
			new MoveAction().onAction(server, drain().get(0));
			MockStendhalRPRuleProcessor.get().setTurn(101);
		}

		List<RPAction> drain() {
			final List<RPAction> result = new ArrayList<>(sent);
			sent.clear();
			return result;
		}

		void deliver() {
			final RPObject changes = new RPObject();
			server.getDifferences(changes, new RPObject());
			user.processPositioning(base, changes);
			for (final String key : new String[] {"x", "y", "dir", "speed", "zoneid", "movement_request"}) {
				if (changes.has(key)) base.put(key, changes.get(key));
			}
			server.resetAddedAndDeleted();
		}

		double progress() {
			return (user.getRenderX() - 20) * direction.getdx() + (user.getRenderY() - 20) * direction.getdy();
		}
	}
}
