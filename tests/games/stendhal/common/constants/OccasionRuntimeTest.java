/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.common.constants;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OccasionRuntimeTest {
	private static final String CHRISTMAS = "stendhal.christmas";
	private static final String EASTER = "stendhal.easter";
	private static final String MINETOWN = "stendhal.minetown";
	private static final String MINETOWN_CONSTRUCTION = "stendhal.minetownconstruction";

	private final Map<String, String> previous = new HashMap<String, String>();

	@Before
	public void rememberProperties() {
		remember(CHRISTMAS);
		remember(EASTER);
		remember(MINETOWN);
		remember(MINETOWN_CONSTRUCTION);
	}

	@After
	public void restoreProperties() {
		restore(CHRISTMAS);
		restore(EASTER);
		restore(MINETOWN);
		restore(MINETOWN_CONSTRUCTION);
		Occasion.refresh();
	}

	@Test
	public void refreshTracksRuntimeChristmasPropertyChanges() {
		assertRefreshTracks(CHRISTMAS, new OccasionValue() {
			@Override
			public boolean get() {
				return Occasion.CHRISTMAS.booleanValue();
			}
		});
	}

	@Test
	public void refreshTracksRuntimeEasterPropertyChanges() {
		assertRefreshTracks(EASTER, new OccasionValue() {
			@Override
			public boolean get() {
				return Occasion.EASTER.booleanValue();
			}
		});
	}

	@Test
	public void refreshTracksRuntimeMineTownPropertyChanges() {
		assertRefreshTracks(MINETOWN, new OccasionValue() {
			@Override
			public boolean get() {
				return Occasion.MINETOWN.booleanValue();
			}
		});
	}

	@Test
	public void refreshTracksRuntimeMineTownConstructionPropertyChanges() {
		assertRefreshTracks(MINETOWN_CONSTRUCTION, new OccasionValue() {
			@Override
			public boolean get() {
				return Occasion.MINETOWN_CONSTRUCTION.booleanValue();
			}
		});
	}

	private void assertRefreshTracks(final String property, final OccasionValue value) {
		System.clearProperty(property);
		Occasion.refresh();
		assertFalse(value.get());

		System.setProperty(property, "true");
		Occasion.refresh();
		assertTrue(value.get());

		System.clearProperty(property);
		Occasion.refresh();
		assertFalse(value.get());
	}

	private void remember(final String property) {
		previous.put(property, System.getProperty(property));
	}

	private void restore(final String property) {
		final String value = previous.get(property);
		if (value == null) {
			System.clearProperty(property);
		} else {
			System.setProperty(property, value);
		}
	}

	private interface OccasionValue {
		boolean get();
	}
}
