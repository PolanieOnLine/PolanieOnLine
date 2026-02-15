package games.stendhal.server.maps.koscielisko;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EscortActivityMonitorTest {
	@Test
	public void shouldDetectSnapshotMovement() {
		final EscortActivityMonitor.PlayerSnapshot first = new EscortActivityMonitor.PlayerSnapshot(1, 1, 1000L);
		final EscortActivityMonitor.PlayerSnapshot second = new EscortActivityMonitor.PlayerSnapshot(2, 1, 2000L);
		assertThat(first.hasMovedTo(second), is(true));
	}
}
