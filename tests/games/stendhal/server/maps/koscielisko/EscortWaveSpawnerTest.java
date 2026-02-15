package games.stendhal.server.maps.koscielisko;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.log4j.Logger;
import org.junit.Test;

public class EscortWaveSpawnerTest {
	@Test
	public void shouldInitializeWaveBudgetPerStageWithHardCap() {
		final EscortWaveSpawner spawner = new EscortWaveSpawner(Logger.getLogger("test"), "evt", 20, 10, 4);
		spawner.initializeWaveBudgetIfNeeded(10, 100);
		assertThat(spawner.getCurrentWaveBudget(), is(10));
		spawner.initializeWaveBudgetIfNeeded(60, 100);
		assertThat(spawner.getCurrentWaveBudget(), is(18));
		spawner.initializeWaveBudgetIfNeeded(95, 100);
		assertThat(spawner.getCurrentWaveBudget(), is(20));
	}
}
