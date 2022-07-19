package games.stendhal.server.core.engine;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import games.stendhal.server.maps.MockStendlRPWorld;

@RunWith(Parameterized.class)
public class StendhalRPZoneTest {

	private String zoneName;
	private String expectedZoneDescription;

	public StendhalRPZoneTest(String zoneName, String expectedZoneDescription) {
		this.zoneName = zoneName;
		this.expectedZoneDescription = expectedZoneDescription;
	}

	@Parameters(name = "{index}: describe({0}) = {1}")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
                {"int_fado_tavern", "w budynku na Fado"},
                {"0_semos_city", "Semos city"},
                {"0_semos_mountain_n_w4", "Semos mountain północ zachód"},
                {"-3_semos_dungeon", "głęboko pod ziemią na poziomie Semos dungeon"},
                {"-1_ados_caves", "pod ziemią na poziomie Ados caves"},
                {"6_kikareukin_islands", "wysoko nad powierzchnią ziemi na poziomie Kikareukin islands"},
                {"hell", "w Piekle"},
                {"malleus_plain", "na równinie Malleus"}
		});
	}

	@Test
	public void testDescribe() throws Exception {
		StendhalRPZone zone = new StendhalRPZone(zoneName);
		MockStendlRPWorld.get().addRPZone(zone);

		assertEquals(expectedZoneDescription, StendhalRPZone.describe(zoneName));

		MockStendlRPWorld.get().removeRPZone(zone.getID());
	}
}
