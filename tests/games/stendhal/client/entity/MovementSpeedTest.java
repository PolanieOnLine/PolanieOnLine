package games.stendhal.client.entity;

import static org.junit.Assert.*;

import java.lang.reflect.Field;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.Direction;
import games.stendhal.client.util.UserInterfaceTestHelper;
import marauroa.common.game.RPObject;

public class MovementSpeedTest {
	@BeforeClass
	public static void initializeUI() {
		UserInterfaceTestHelper.initUserInterface();
	}

	@Test
	public void speedOnlyChangesPreserveInterpolatedPositionWhileAcceleratingOrSlowing() throws Exception {
		for (final double speed : new double[] {0.25, 0.5, 1.2, 2.0}) {
			final ActiveEntity entity = new ActiveEntity() { };
			final RPObject object = movingObject();
			entity.initialize(object);
			entity.x = 6.2;
			final RPObject changes = new RPObject();
			changes.put("speed", speed);
			entity.processPositioning(object, changes);
			assertEquals(6.2, entity.getX(), 0.00001);
			assertEquals(speed, velocity(entity), 0.00001);
		}
	}

	@Test
	public void unrelatedUpdatesDoNotReapplyOldCoordinatesOrChangeVelocity() throws Exception {
		final ActiveEntity entity = new ActiveEntity() { };
		final RPObject object = movingObject();
		entity.initialize(object);
		entity.x = 6.2;
		final RPObject changes = new RPObject();
		changes.put("hp", 90);
		entity.processPositioning(object, changes);
		assertEquals(6.2, entity.getX(), 0.00001);
		assertEquals(1.0, velocity(entity), 0.00001);
	}

	private double velocity(final ActiveEntity entity) throws Exception {
		return velocity(entity, "dx");
	}

	private double velocity(final ActiveEntity entity, final String axis) throws Exception {
		final Field field = ActiveEntity.class.getDeclaredField(axis);
		field.setAccessible(true);
		return field.getDouble(entity);
	}

	@Test
	public void largeRelocationsUseTheServerPositionAndStopsSettleWithoutSnapping() throws Exception {
		final ActiveEntity entity = new ActiveEntity() { };
		final RPObject object = movingObject();
		object.put("speed", 4.5);
		entity.initialize(object);
		entity.x = 6.2;
		final RPObject relocation = new RPObject();
		relocation.put("x", 40);
		entity.processPositioning(object, relocation);
		assertEquals(40, entity.getX(), 0.00001);
		assertEquals(4.5, velocity(entity), 0.00001);
		object.put("x", 40);
		entity.x = 40.7;
		final RPObject stop = new RPObject();
		stop.put("speed", 0.0);
		entity.processPositioning(object, stop);
		assertEquals(40.7, entity.getX(), 0.00001);
		assertTrue(entity.stopped());
		entity.update(120);
		assertEquals(40, entity.getX(), 0.00001);
	}

	@Test
	public void stoppingAfterOneTileDoesNotJumpInEitherAxis() {
		for (final Direction direction : new Direction[] {
				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			final ActiveEntity entity = new ActiveEntity() { };
			final RPObject object = movingObject();
			object.put("dir", direction.get());
			entity.initialize(object);
			entity.x = 5 + 1.8 * direction.getdx();
			entity.y = 5 + 1.8 * direction.getdy();
			final double beforeX = entity.x, beforeY = entity.y;
			final RPObject stop = new RPObject();
			stop.put("x", 5 + direction.getdx());
			stop.put("y", 5 + direction.getdy());
			stop.put("speed", 0.0);
			entity.processPositioning(object, stop);
			assertEquals(beforeX, entity.getX(), 0.00001);
			assertEquals(beforeY, entity.getY(), 0.00001);
			assertTrue(entity.stopped());
			entity.update(30);
			assertEquals(beforeX - .2 * direction.getdx(), entity.getX(), 0.00001);
			assertEquals(beforeY - .2 * direction.getdy(), entity.getY(), 0.00001);
			entity.update(90);
			assertEquals(stop.getInt("x"), entity.getX(), 0.00001);
			assertEquals(stop.getInt("y"), entity.getY(), 0.00001);
			entity.update(300);
			assertEquals(stop.getInt("x"), entity.getX(), 0.00001);
		}
	}

	@Test
	public void aStoppedTeleportIsNotInterpolatedAcrossTheMap() {
		final ActiveEntity entity = new ActiveEntity() { };
		final RPObject object = movingObject();
		entity.initialize(object);
		entity.x = 6.8;
		final RPObject teleport = new RPObject();
		teleport.put("x", 40);
		teleport.put("y", 25);
		teleport.put("speed", 0.0);
		entity.processPositioning(object, teleport);
		assertEquals(40, entity.getX(), 0.00001);
		assertEquals(25, entity.getY(), 0.00001);
		entity.update(120);
		assertEquals(40, entity.getX(), 0.00001);
	}

	@Test
	public void startingAnotherStepCancelsThePreviousStopCorrection() {
		final ActiveEntity entity = new ActiveEntity() { };
		final RPObject object = movingObject();
		entity.initialize(object);
		entity.x = 6.8;
		final RPObject stop = new RPObject();
		stop.put("x", 6);
		stop.put("speed", 0.0);
		entity.processPositioning(object, stop);
		entity.update(30);
		assertEquals(6.6, entity.getX(), 0.00001);
		entity.setSpeed(0, 1);
		entity.setSpeed(0, 0);
		entity.update(120);
		assertEquals("New movement must not pull the player back to the previous tile", 6.6, entity.getX(), 0.00001);
	}

	@Test
	public void genuineTimingErrorsAreStillCorrectedInBothDirections() throws Exception {
		for (final Direction direction : new Direction[] {
				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			for (final double progress : new double[] {-0.4, 1.4}) {
				final ActiveEntity entity = new ActiveEntity() { };
				final RPObject object = movingObject();
				object.put("dir", direction.get());
				object.put("speed", 1.5);
				entity.initialize(object);
				entity.x = 5 + progress * direction.getdx();
				entity.y = 5 + progress * direction.getdy();
				final RPObject changes = new RPObject();
				changes.put("x", 5);
				changes.put("y", 5);
				final double predictedX = entity.getX(), predictedY = entity.getY();
				entity.processPositioning(object, changes);
				assertEquals(predictedX, entity.getX(), 0.00001);
				assertEquals(predictedY, entity.getY(), 0.00001);
				final double forwardSpeed = velocity(entity, "dx") * direction.getdx()
						+ velocity(entity, "dy") * direction.getdy();
				assertTrue(forwardSpeed > 0);
				if (progress < 0) assertTrue("Catch up when behind", forwardSpeed > 1.5);
				else assertTrue("Slow down when ahead of fractional tile", forwardSpeed < 1.5);
			}
		}
	}

	@Test
	public void fractionalTileProgressDoesNotMakeWalkingPulseAtEachServerTurn() throws Exception {
		for (final Direction direction : new Direction[] {
				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			for (final double speed : new double[] {0.5, 1.1, 1.5, 2.0, 2.5, 4.5}) {
				final ActiveEntity entity = new ActiveEntity() { };
				final RPObject object = movingObject();
				object.put("x", 100);
				object.put("y", 100);
				object.put("dir", direction.get());
				object.put("speed", speed);
				entity.initialize(object);
				final String axis = direction.getdx() == 0 ? "y" : "x";
				for (int turn = 1; turn <= 100; turn++) {
					entity.x += velocity(entity, "dx");
					entity.y += velocity(entity, "dy");
					final int steps = (int) Math.floor(turn * speed + 1e-9);
					final int serverPosition = 100 + steps * (direction.getdx() + direction.getdy());
					if (object.getInt(axis) == serverPosition) continue;
					final RPObject changes = new RPObject();
					changes.put(axis, serverPosition);
					final double predictedX = entity.getX(), predictedY = entity.getY();
					entity.processPositioning(object, changes);
					final String context = direction + " speed " + speed + " turn " + turn;
					assertEquals(context, predictedX, entity.getX(), 0.00001);
					assertEquals(context, predictedY, entity.getY(), 0.00001);
					assertEquals(context, speed, velocity(entity, "dx") * direction.getdx()
							+ velocity(entity, "dy") * direction.getdy(), 0.00001);
					object.put(axis, serverPosition);
				}
			}
		}
	}

	@Test
	public void steadyFastMovementStaysSmoothInEveryDirection() throws Exception {
		for (final Direction direction : new Direction[] {
				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			for (final double speed : new double[] {1.0, 1.2, 2.0, 2.5, 4.5, 8.0}) {
				final ActiveEntity entity = new ActiveEntity() { };
				final RPObject object = movingObject();
				object.put("x", 100);
				object.put("y", 100);
				object.put("dir", direction.get());
				object.put("speed", speed);
				entity.initialize(object);
				for (int frame = 1; frame <= 300; frame++) {
					entity.x += velocity(entity, "dx") / 10;
					entity.y += velocity(entity, "dy") / 10;
					if (frame % 10 != 0) continue;
					final int steps = (int) Math.floor(frame / 10 * speed + 1e-9);
					final RPObject changes = new RPObject();
					final String axis = direction.getdx() == 0 ? "y" : "x";
					changes.put(axis, 100 + steps * (direction.getdx() + direction.getdy()));
					final double predictedX = entity.getX(), predictedY = entity.getY();
					entity.processPositioning(object, changes);
					final String context = direction + " speed " + speed + " frame " + frame;
					assertEquals(context, predictedX, entity.getX(), 0.00001);
					assertEquals(context, predictedY, entity.getY(), 0.00001);
					final double forwardSpeed = velocity(entity, "dx") * direction.getdx()
							+ velocity(entity, "dy") * direction.getdy();
					assertTrue(context + " slows down excessively", forwardSpeed >= speed * 0.65);
					assertTrue(context + " accelerates excessively", forwardSpeed <= speed * 1.35);
					object.put(axis, changes.getInt(axis));
				}
			}
		}
	}

	private RPObject movingObject() {
		final RPObject object = new RPObject();
		object.put("x", 5);
		object.put("y", 5);
		object.put("dir", Direction.RIGHT.get());
		object.put("speed", 1.0);
		return object;
	}
}
