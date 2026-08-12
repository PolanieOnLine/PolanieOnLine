package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BleedingStatusTest {
	@Test
	public void ticksAlwaysAddUpToExactWoundDamage() {
		final BleedingStatus wound = new BleedingStatus(21, 4, 3, null);

		assertEquals(6, wound.consumeNextTick());
		assertEquals(5, wound.consumeNextTick());
		assertEquals(5, wound.consumeNextTick());
		assertEquals(5, wound.consumeNextTick());
		assertTrue(wound.isConsumed());
		assertEquals(0, wound.getRemainingDamage());
		assertEquals(0, wound.consumeNextTick());
	}

	@Test
	public void handlerCapsBleedingAtThreeWounds() {
		final StatusList statuses = new StatusList(null);
		final BleedingStatus first = wound(10);
		final BleedingStatus second = wound(20);
		final BleedingStatus third = wound(30);
		final BleedingStatus fourth = wound(40);

		assertTrue(BleedingStatusHandler.addOrReplaceWound(first, statuses));
		assertTrue(BleedingStatusHandler.addOrReplaceWound(second, statuses));
		assertTrue(BleedingStatusHandler.addOrReplaceWound(third, statuses));
		assertTrue(BleedingStatusHandler.addOrReplaceWound(fourth, statuses));
		assertEquals(BleedingStatusHandler.MAX_STACKS,
				statuses.countStatusByType(StatusType.BLEEDING));
		assertFalse(statuses.getStatuses().contains(first));
		assertTrue(statuses.getStatuses().contains(fourth));
	}

	@Test
	public void weakerWoundDoesNotReplaceExistingStackAtCap() {
		final StatusList statuses = new StatusList(null);
		final BleedingStatus first = wound(20);
		final BleedingStatus second = wound(25);
		final BleedingStatus third = wound(30);
		final BleedingStatus weaker = wound(10);

		BleedingStatusHandler.addOrReplaceWound(first, statuses);
		BleedingStatusHandler.addOrReplaceWound(second, statuses);
		BleedingStatusHandler.addOrReplaceWound(third, statuses);

		assertFalse(BleedingStatusHandler.addOrReplaceWound(weaker, statuses));
		assertEquals(3, statuses.getStatuses().size());
		assertTrue(statuses.getStatuses().contains(first));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void compatibilityConstructorConvertsOldSignedValues() {
		final BleedingStatus wound = new BleedingStatus(-20, 3, -5);

		assertEquals(20, wound.getTotalDamage());
		assertEquals(4, wound.getTicksRemaining());
		assertEquals(3, wound.getTickIntervalTurns());
		assertNull(wound.getSource());
	}

	private BleedingStatus wound(final int damage) {
		return new BleedingStatus(damage, 4, 3, null);
	}
}
