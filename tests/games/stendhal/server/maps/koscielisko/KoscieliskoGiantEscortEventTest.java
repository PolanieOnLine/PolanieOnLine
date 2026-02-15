/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.koscielisko;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class KoscieliskoGiantEscortEventTest {
	@Test
	public void shouldLimitFrequentWaveBroadcastsPerMinute() {
		final KoscieliskoGiantEscortEvent.BroadcastRateLimiter limiter =
				new KoscieliskoGiantEscortEvent.BroadcastRateLimiter(TimeUnit.SECONDS.toMillis(50));
		int allowed = 0;
		for (int second = 0; second < 60; second++) {
			if (limiter.tryAcquire("FALA_50", TimeUnit.SECONDS.toMillis(second))) {
				allowed++;
			}
		}

		assertThat("Limiter should not exceed the wave broadcast budget per minute", allowed, is(2));
	}
}
