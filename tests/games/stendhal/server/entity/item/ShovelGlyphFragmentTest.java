package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class ShovelGlyphFragmentTest {

	@Test
	public void fragmentStatesUseIndependentConditionalRolls() {
		assertEquals("zniszczony", Shovel.determineFragment(59.99, 99.0, 99.0));
		assertEquals("spękany", Shovel.determineFragment(60.0, 29.99, 99.0));
		assertEquals("nadkruszony", Shovel.determineFragment(60.0, 30.0, 14.99));
		assertNull(Shovel.determineFragment(60.0, 30.0, 15.0));
	}
}
