package games.stendhal.server.maps.koscielisko;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EscortAggroControllerTest {
	@Test
	public void shouldReusePathCacheWhenPositionsAreUnchangedWithinCooldown() {
		final EscortAggroController.PathSearchCacheEntry entry =
				new EscortAggroController.PathSearchCacheEntry(10, 5, 6, 8, 9, true);

		assertThat(entry.canReuse(12, 5, 6, 8, 9, 3), is(true));
		assertThat(entry.canReuse(13, 5, 6, 8, 9, 3), is(false));
		assertThat(entry.canReuse(11, 6, 6, 8, 9, 3), is(false));
	}
}
