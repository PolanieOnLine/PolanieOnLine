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
	public void largeRelocationsAndStopsStillUseTheServerPositionAtHighSpeed() throws Exception {
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
		assertEquals(40, entity.getX(), 0.00001);
		assertTrue(entity.stopped());
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
