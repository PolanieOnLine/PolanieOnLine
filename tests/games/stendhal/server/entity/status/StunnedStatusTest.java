/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class StunnedStatusTest {
	@Test
	public void standardDurationsExposeCurrentBalance() {
		final StunnedStatus status = new StunnedStatus();

		assertEquals(StatusType.STUNNED, status.getStatusType());
		assertEquals(4, StunnedStatus.PLAYER_DURATION_SECONDS);
		assertEquals(3, StunnedStatus.CREATURE_DURATION_SECONDS);
		assertEquals("stunned", status.getName());
	}

	@Test
	public void explicitDurationOverridesTargetBasedDefault() {
		assertEquals(5, new StunnedStatus(5).getDurationSeconds(null));
	}

	@Test(expected = IllegalArgumentException.class)
	public void zeroDurationIsRejected() {
		new StunnedStatus(0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void negativeDurationIsRejected() {
		new StunnedStatus(-1);
	}
}
