/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.common.constants;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OccasionRuntimeTest {
	private static final String CHRISTMAS = "stendhal.christmas";
	private String previous;

	@Before
	public void rememberProperty() {
		previous = System.getProperty(CHRISTMAS);
	}

	@After
	public void restoreProperty() {
		if (previous == null) {
			System.clearProperty(CHRISTMAS);
		} else {
			System.setProperty(CHRISTMAS, previous);
		}
		Occasion.refresh();
	}

	@Test
	public void refreshTracksRuntimeChristmasPropertyChanges() {
		System.clearProperty(CHRISTMAS);
		Occasion.refresh();
		assertFalse(Occasion.CHRISTMAS.booleanValue());

		System.setProperty(CHRISTMAS, "true");
		Occasion.refresh();
		assertTrue(Occasion.CHRISTMAS.booleanValue());

		System.clearProperty(CHRISTMAS);
		Occasion.refresh();
		assertFalse(Occasion.CHRISTMAS.booleanValue());
	}
}
