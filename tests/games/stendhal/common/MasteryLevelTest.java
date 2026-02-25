package games.stendhal.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MasteryLevelTest {

	@Test
	public final void testMonotonicThresholds() {
		long previous = MasteryLevel.getXP(0);
		for (int level = 1; level <= MasteryLevel.MAX_MASTERY_LEVEL; level++) {
			long current = MasteryLevel.getXP(level);
			assertTrue("XP must be strictly increasing at level " + level,
					current > previous);
			previous = current;
		}
	}

	@Test
	public final void testLevelXpBoundaries() {
		for (int level = 0; level < MasteryLevel.MAX_MASTERY_LEVEL; level++) {
			long currentXP = MasteryLevel.getXP(level);
			long nextXP = MasteryLevel.getXP(level + 1);

			assertEquals(level, MasteryLevel.getLevel(currentXP));
			assertEquals(level, MasteryLevel.getLevel(nextXP - 1));
			assertEquals(level + 1, MasteryLevel.getLevel(nextXP));
		}
	}

	@Test
	public final void testCapOn2000() {
		assertEquals(2000, MasteryLevel.maxLevel());
		assertEquals(2000, MasteryLevel.getLevel(MasteryLevel.getXP(2000)));
		assertEquals(2000, MasteryLevel.getLevel(Long.MAX_VALUE));
		assertEquals(-1L, MasteryLevel.getXP(-1));
		assertEquals(-1L, MasteryLevel.getXP(2001));
	}
}
