/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SeasonalEventTypeTest {
	@Test
	public void parsesCanonicalNamesAndAliases() {
		assertEquals(SeasonalEventType.CHRISTMAS, SeasonalEventType.parse("christmas"));
		assertEquals(SeasonalEventType.CHRISTMAS, SeasonalEventType.parse(" XMAS "));
		assertEquals(SeasonalEventType.MINE_TOWN, SeasonalEventType.parse("minetown"));
		assertEquals(SeasonalEventType.MINE_TOWN, SeasonalEventType.parse("mine-town"));
		assertEquals(SeasonalEventType.MINE_TOWN, SeasonalEventType.parse("REVIVAL"));
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
		assertEquals("stendhal.easter", SeasonalEventType.EASTER.getProperty());
	}
}
