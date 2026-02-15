package games.stendhal.server.maps.koscielisko;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class EscortAnnouncementServiceTest {
	@Test
	public void shouldLimitFrequentWaveBroadcastsPerMinute() {
		final EscortAnnouncementService.BroadcastRateLimiter limiter =
				new EscortAnnouncementService.BroadcastRateLimiter(TimeUnit.SECONDS.toMillis(50));
		int allowed = 0;
		for (int second = 0; second < 60; second++) {
			if (limiter.tryAcquire("FALA_50", TimeUnit.SECONDS.toMillis(second))) {
				allowed++;
			}
		}

		assertThat(allowed, is(2));
	}
}
