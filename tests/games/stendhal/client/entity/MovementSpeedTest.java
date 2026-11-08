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
		final Field field = ActiveEntity.class.getDeclaredField("dx");
		field.setAccessible(true);
		return field.getDouble(entity);
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
