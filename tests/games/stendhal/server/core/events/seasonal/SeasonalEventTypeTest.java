/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class SeasonalEventTypeTest {
	@Test
	public void parsesCanonicalNamesAndAliases() {
		assertEquals(SeasonalEventType.CHRISTMAS, SeasonalEventType.parse("christmas"));
		assertEquals(SeasonalEventType.CHRISTMAS, SeasonalEventType.parse(" XMAS "));
		assertEquals(SeasonalEventType.MINE_TOWN, SeasonalEventType.parse("minetown"));
		assertEquals(SeasonalEventType.MINE_TOWN, SeasonalEventType.parse("mine-town"));
		assertEquals(SeasonalEventType.MINE_TOWN, SeasonalEventType.parse("REVIVAL"));
		assertEquals(SeasonalEventType.MINE_TOWN_CONSTRUCTION,
				SeasonalEventType.parse("minetownconstruction"));
		assertEquals(SeasonalEventType.MINE_TOWN_CONSTRUCTION,
				SeasonalEventType.parse("mine-town-construction"));
		assertEquals(SeasonalEventType.MINE_TOWN_CONSTRUCTION,
				SeasonalEventType.parse("CONSTRUCTION"));
		assertEquals(SeasonalEventType.EASTER, SeasonalEventType.parse("easter"));
	}

	@Test
	public void rejectsUnknownNames() {
		assertNull(SeasonalEventType.parse(null));
		assertNull(SeasonalEventType.parse(""));
		assertNull(SeasonalEventType.parse("easterbunny"));
	}

	@Test
	public void exposesCanonicalMetadata() {
		assertEquals("christmas", SeasonalEventType.CHRISTMAS.getCommandName());
		assertEquals("stendhal.christmas", SeasonalEventType.CHRISTMAS.getProperty());
		assertEquals("Mine Town Revival Weeks", SeasonalEventType.MINE_TOWN.getDisplayName());
		assertEquals("stendhal.minetownconstruction",
				SeasonalEventType.MINE_TOWN_CONSTRUCTION.getProperty());
		assertEquals("stendhal.easter", SeasonalEventType.EASTER.getProperty());
	}

	@Test
	public void readsEnabledStateFromCanonicalProperty() {
		final String property = SeasonalEventType.EASTER.getProperty();
		final String previous = System.getProperty(property);
		try {
			System.clearProperty(property);
			assertFalse(SeasonalEventType.EASTER.isEnabled());

			System.setProperty(property, "true");
			assertTrue(SeasonalEventType.EASTER.isEnabled());
		} finally {
			if (previous == null) {
				System.clearProperty(property);
			} else {
				System.setProperty(property, previous);
			}
		}
	}
}
