package games.stendhal.server.maps.koscielisko;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GiantHealthControllerTest {
	@Test
	public void shouldFailWhenGiantIsMissing() {
		final GiantHealthController controller = new GiantHealthController(100, 10000, 0);
		controller.reset(1000, 0L);
		assertThat(controller.shouldFail(null), is(true));
	}
}
