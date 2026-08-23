package games.stendhal.server.entity.item.scroll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class WhiteBalloonScrollTest {
	@Test
	public void normalAndAlternativeZakopaneCloudsBelongToTimedArea() {
		assertTrue(WhiteBalloonScroll.isZakopaneCloudZone("6_zakopane_clouds"));
		assertTrue(WhiteBalloonScroll.isZakopaneCloudZone("alt_6_zakopane_clouds"));
	}

	@Test
	public void otherAlternativeZonesAreNotZakopaneClouds() {
		assertFalse(WhiteBalloonScroll.isZakopaneCloudZone("alt_6_kikareukin_islands"));
	}
}
