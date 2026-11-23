package games.stendhal.client.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.GameObjects;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.gui.wt.core.WtWindowManager;
import games.stendhal.client.util.UserInterfaceTestHelper;
import games.stendhal.common.Direction;
import marauroa.common.game.RPObject;

public class UserMovementPredictionTest {
	private String savedSpeed;
	private String savedJitter;

	@BeforeClass
	public static void initializeClient() {
		UserInterfaceTestHelper.initUserInterface();
		GameObjects.createInstance(new StaticGameLayers());
	}

	@Before
	public void useKnownStartupPrediction() {
		final WtWindowManager manager = WtWindowManager.getInstance();
		savedSpeed = manager.getProperty("predictor.speed", "0.3");
		savedJitter = manager.getProperty("predictor.jitter", "0.0");
		manager.setProperty("predictor.speed", "0.6");
		manager.setProperty("predictor.jitter", "0.0");
		User.setNull();
	}

	@After
	public void restorePredictionSettings() {
		final WtWindowManager manager = WtWindowManager.getInstance();
		manager.setProperty("predictor.speed", savedSpeed);
		manager.setProperty("predictor.jitter", savedJitter);
		User.setNull();
	}

	@Test
	public void delayedFacingPacketDoesNotCancelANewerPredictedStart() {
		for (final Direction direction : new Direction[] {
				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			final RPObject base = stoppedObject(direction.getdx() == 0 ? Direction.LEFT : Direction.UP);
			final User user = new User();
			user.initialize(base);
			user.predictMovement(direction, false);
			user.update(60);
			final double beforeX = user.getRenderX(), beforeY = user.getRenderY();
			// FaceAction on an already stopped player publishes only dir: speed
			// remains zero on the server until the newer move command is applied.
			final RPObject facing = new RPObject();
			facing.put("dir", direction.oppositeDirection().get());
			user.processPositioning(base, facing);
			assertFalse("A delayed facing update must not stop the new start", user.stopped());
			assertEquals(direction, user.getDirection());
			user.update(60);
			assertTrue("The startup must keep moving forwards",
					(user.getRenderX() - beforeX) * direction.getdx()
					+ (user.getRenderY() - beforeY) * direction.getdy() > 0);
			final RPObject movement = new RPObject();
			movement.put("dir", direction.get());
			movement.put("speed", 1.1);
			movement.put("x", 5 + direction.getdx());
			movement.put("y", 5 + direction.getdy());
			user.processPositioning(base, movement);
			assertFalse(user.stopped());
		}
	}

	@Test
	public void sameZoneMetadataDoesNotMakeAnOldFacingPacketAStationaryConfirmation() {
		final RPObject base = stoppedObject(Direction.UP);
		base.put("zoneid", "movement_test");
		final User user = new User();
		user.initialize(base);
		user.predictMovement(Direction.RIGHT, false);
		user.update(60);
		final RPObject facing = new RPObject();
		facing.put("dir", Direction.LEFT.get());
		facing.put("zoneid", "movement_test");
		user.processPositioning(base, facing);
		assertFalse(user.stopped());
		assertEquals(Direction.RIGHT, user.getDirection());
		facing.put("zoneid", "new_zone");
		user.processPositioning(base, facing);
		assertTrue("A changed zone must remain authoritative", user.stopped());
		assertEquals(Direction.LEFT, user.getDirection());
	}

	@Test
	public void speedAcknowledgementWaitsForARealStepButAnAbortedStartCancelsTiming() throws Exception {
		final RPObject base = stoppedObject(Direction.RIGHT);
		final User user = new User();
		user.initialize(base);
		user.predictMovement(Direction.RIGHT, false);
		user.update(60);
		final RPObject speed = new RPObject();
		speed.put("speed", 0.5);
		user.processPositioning(base, speed);
		base.put("speed", 0.5);
		assertTrue("Speed alone is not the first whole-tile movement", predictor(user).isActive());
		final RPObject stop = new RPObject();
		stop.put("speed", 0.0);
		user.processPositioning(base, stop);
		assertFalse("An aborted start must not time an unrelated later move", predictor(user).isActive());
	}

	@Test
	public void anExplicitStopWithCoordinatesDoesNotLearnAnAbortedStartupLatency() throws Exception {
		final RPObject base = stoppedObject(Direction.RIGHT);
		final User user = new User();
		user.initialize(base);
		user.predictMovement(Direction.RIGHT, false);
		user.update(60);
		final double prediction = predictor(user).prediction;
		final RPObject stop = new RPObject();
		stop.put("speed", 0.0);
		stop.put("x", 5);
		stop.put("y", 5);
		user.processPositioning(base, stop);
		assertFalse(predictor(user).isActive());
		assertEquals("A stationary confirmation is not a successful first step",
				prediction, predictor(user).prediction, 0.000001);
	}

	@Test
	public void anInitiallyMissingSpeedAlsoMeansTheOldServerStateWasStopped() {
		final RPObject base = stoppedObject(Direction.UP);
		base.remove("speed");
		final User user = new User();
		user.initialize(base);
		user.predictMovement(Direction.RIGHT, false);
		user.update(60);
		final RPObject facing = new RPObject();
		facing.put("dir", Direction.LEFT.get());
		user.processPositioning(base, facing);
		assertFalse(user.stopped());
		assertEquals(Direction.RIGHT, user.getDirection());
	}

	@Test
	public void aNewFacingRequestStillAcceptsTheStationaryServerDirection() {
		final RPObject base = stoppedObject(Direction.RIGHT);
		final User user = new User();
		user.initialize(base);
		user.predictMovement(Direction.RIGHT, false);
		user.update(60);
		user.predictMovement(Direction.UP, true);
		final RPObject facing = new RPObject();
		facing.put("dir", Direction.UP.get());
		user.processPositioning(base, facing);
		assertTrue("The latest facing request must still stop movement", user.stopped());
		assertEquals(Direction.UP, user.getDirection());
	}

	@Test
	public void anExplicitStopDuringStartupStillReconcilesToTheServer() {
		final RPObject base = stoppedObject(Direction.RIGHT);
		final User user = new User();
		user.initialize(base);
		user.predictMovement(Direction.RIGHT, false);
		user.update(60);
		final RPObject stop = new RPObject();
		stop.put("speed", 0.0);
		user.processPositioning(base, stop);
		assertTrue(user.stopped());
		user.update(120);
		assertEquals(5, user.getX(), 0.000001);
		assertEquals(5, user.getRenderX(), 0.000001);
	}

	private RPObject stoppedObject(final Direction direction) {
		final RPObject object = new RPObject();
		object.put("name", "movement-prediction-test");
		object.put("x", 5);
		object.put("y", 5);
		object.put("dir", direction.get());
		object.put("speed", 0.0);
		return object;
	}

	private SpeedPredictor predictor(final User user) throws Exception {
		final Field field = User.class.getDeclaredField("speedPredictor");
		field.setAccessible(true);
		return (SpeedPredictor) field.get(user);
	}
}
