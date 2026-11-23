package games.stendhal.client.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.client.GameObjects;
import games.stendhal.client.StaticGameLayers;
import games.stendhal.client.util.UserInterfaceTestHelper;
import games.stendhal.common.Direction;
import marauroa.common.game.RPObject;

public class MovementReconciliationTest {
	@BeforeClass
	public static void initializeClient() {
		UserInterfaceTestHelper.initUserInterface();
		GameObjects.createInstance(new StaticGameLayers());
	}

	@Test
	public void turningKeepsTheSpriteContinuousButCorrectsTheCollisionPositionImmediately() {
		for (final Direction direction : new Direction[] {
				Direction.LEFT, Direction.RIGHT, Direction.UP, Direction.DOWN}) {
			final RPObject base = object();
			final ActiveEntity entity = new ActiveEntity() { };
			entity.initialize(base);
			entity.x = 5 + (direction.getdx() == 0 ? 0.8 : 0);
			entity.y = 5 + (direction.getdy() == 0 ? 0.8 : 0);
			final double beforeX = entity.getRenderX(), beforeY = entity.getRenderY();
			entity.processPositioning(base, turn(direction));
			assertEquals(5, entity.getX(), 0.000001);
			assertEquals(5, entity.getY(), 0.000001);
			assertEquals(beforeX, entity.getRenderX(), 0.000001);
			assertEquals(beforeY, entity.getRenderY(), 0.000001);
			// Visual smoothing must not change the area used for collisions.
			assertEquals(5, entity.getArea().getX(), 0.000001);
			assertEquals(5, entity.getArea().getY(), 0.000001);
			entity.setSpeed(0, 0);
			entity.update(30);
			assertEquals(5 + (beforeX - 5) * 0.75, entity.getRenderX(), 0.000001);
			assertEquals(5 + (beforeY - 5) * 0.75, entity.getRenderY(), 0.000001);
			entity.update(90);
			assertEquals(entity.getX(), entity.getRenderX(), 0.000001);
			assertEquals(entity.getY(), entity.getRenderY(), 0.000001);
		}
	}

	@Test
	public void repeatedUpdatesDoNotRestartTheSameCorrection() {
		final RPObject base = object();
		final ActiveEntity entity = new ActiveEntity() { };
		entity.initialize(base);
		entity.x = 5.8;
		entity.processPositioning(base, turn(Direction.DOWN));
		entity.setSpeed(0, 0);
		entity.update(30);
		assertEquals(5.6, entity.getRenderX(), 0.000001);
		entity.processPositioning(base, turn(Direction.DOWN));
		entity.setSpeed(0, 0);
		entity.update(90);
		assertEquals(5, entity.getRenderX(), 0.000001);
	}

	@Test
	public void positionListenersNeverSeeTheIntermediateSnappedCoordinate() {
		final RPObject base = object();
		final ActiveEntity entity = new ActiveEntity() { };
		entity.initialize(base);
		entity.x = 5.8;
		final List<Double> captured = new ArrayList<>();
		entity.addChangeListener((changed, property) -> {
			if (property == IEntity.PROP_POSITION) captured.add(changed.getRenderX());
		});
		entity.processPositioning(base, turn(Direction.DOWN));
		assertEquals("Publish only the completed correction", 1, captured.size());
		assertEquals(5.8, captured.get(0), 0.000001);
	}

	@Test
	public void aSpeedCallbackCannotCaptureAHalfAppliedPositionCorrection() {
		final RPObject base = object();
		final ActiveEntity entity = new ActiveEntity() { };
		entity.initialize(base);
		entity.x = 5.8;
		final List<Double> captured = new ArrayList<>();
		entity.addChangeListener((changed, property) -> {
			if (property == ActiveEntity.PROP_SPEED) captured.add(changed.getRenderX());
		});
		final RPObject turn = turn(Direction.DOWN);
		turn.put("speed", 1.1);
		entity.processPositioning(base, turn);
		assertEquals(1, captured.size());
		assertEquals(5.8, captured.get(0), 0.000001);
	}

	@Test
	public void teleportsAndReinitializationDiscardTheOldVisualOffset() {
		for (final boolean reinitialize : new boolean[] {false, true}) {
			final RPObject base = object();
			final ActiveEntity entity = new ActiveEntity() { };
			entity.initialize(base);
			entity.x = 5.8;
			entity.processPositioning(base, turn(Direction.DOWN));
			assertEquals(5.8, entity.getRenderX(), 0.000001);
			if (reinitialize) {
				entity.initialize(base);
			} else {
				final RPObject teleport = turn(Direction.DOWN);
				teleport.put("x", 40);
				teleport.put("y", 50);
				entity.processPositioning(base, teleport);
			}
			assertEquals(entity.getX(), entity.getRenderX(), 0.000001);
			assertEquals(entity.getY(), entity.getRenderY(), 0.000001);
		}
	}

	@Test
	public void zoneMetadataDoesNotCancelACorrectionButChangingZonesDoes() {
		final RPObject base = object();
		base.put("zoneid", "old_zone");
		final ActiveEntity entity = new ActiveEntity() { };
		entity.initialize(base);
		entity.x = 5.8;
		final RPObject turn = turn(Direction.DOWN);
		turn.put("zoneid", "old_zone");
		entity.processPositioning(base, turn);
		assertEquals(5.8, entity.getRenderX(), 0.000001);
		final RPObject zoneChange = new RPObject();
		zoneChange.put("zoneid", "new_zone");
		entity.processPositioning(base, zoneChange);
		assertEquals(entity.getX(), entity.getRenderX(), 0.000001);
		assertEquals(entity.getY(), entity.getRenderY(), 0.000001);
	}

	@Test
	public void speedAndUnrelatedUpdatesLeaveAnExistingCorrectionAlone() {
		final RPObject base = object();
		final ActiveEntity entity = new ActiveEntity() { };
		entity.initialize(base);
		entity.x = 5.8;
		entity.processPositioning(base, turn(Direction.DOWN));
		final RPObject speed = new RPObject();
		speed.put("speed", 1.1);
		entity.processPositioning(base, speed);
		assertEquals(5.8, entity.getRenderX(), 0.000001);
		final RPObject health = new RPObject();
		health.put("hp", 99);
		entity.processPositioning(base, health);
		assertEquals(5.8, entity.getRenderX(), 0.000001);
		entity.setSpeed(0, 0);
		entity.update(120);
		assertEquals(5, entity.getRenderX(), 0.000001);
	}

	@Test
	public void visualCorrectionsSettleAtDifferentFrameRatesAndFractionalSpeeds() {
		for (final int fps : new int[] {30, 60, 144}) {
			for (final double speed : new double[] {1.1, 1.5, 2.0}) {
				final RPObject base = object();
				final ActiveEntity entity = new ActiveEntity() { };
				entity.initialize(base);
				entity.x = 5.8;
				final RPObject turn = turn(Direction.DOWN);
				turn.put("speed", speed);
				entity.processPositioning(base, turn);
				int elapsed = 0;
				double previousX = entity.getRenderX() * 32;
				while (elapsed < 180) {
					final int delta = Math.min(180 - elapsed, Math.round(1000f / fps));
					entity.update(delta);
					assertTrue("No single-frame correction larger than the elapsed fraction",
							Math.abs(previousX - entity.getRenderX() * 32) <= 25.6 * delta / 120 + 0.000001);
					previousX = entity.getRenderX() * 32;
					elapsed += delta;
				}
				assertEquals(entity.getX(), entity.getRenderX(), 0.000001);
				assertEquals(entity.getY(), entity.getRenderY(), 0.000001);
			}
		}
	}

	private RPObject object() {
		final RPObject object = new RPObject();
		object.put("x", 5);
		object.put("y", 5);
		object.put("dir", Direction.RIGHT.get());
		object.put("speed", 1.5);
		return object;
	}

	private RPObject turn(final Direction direction) {
		final RPObject changes = new RPObject();
		changes.put("x", 5);
		changes.put("y", 5);
		changes.put("dir", direction.get());
		return changes;
	}
}
