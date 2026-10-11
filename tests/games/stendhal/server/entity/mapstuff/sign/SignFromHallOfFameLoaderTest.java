package games.stendhal.server.entity.mapstuff.sign;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

public class SignFromHallOfFameLoaderTest {
	@Test
	public void numberedRankingPreservesOrderAndNames() {
		assertEquals("1. PolanieOnLine\n2. Drugi Gracz\n3. Żółw",
				SignFromHallOfFameLoader.formatPlayers(
						Arrays.asList("PolanieOnLine", "Drugi Gracz", "Żółw"), true, true));
	}

	@Test
	public void onePlayerHasFirstPlaceAndNoTrailingLineBreak() {
		assertEquals("1. PolanieOnLine", SignFromHallOfFameLoader.formatPlayers(
				Collections.singletonList("PolanieOnLine"), true, true));
	}

	@Test
	public void emptyRankingDoesNotCreateAnEmptyPlace() {
		assertEquals("", SignFromHallOfFameLoader.formatPlayers(
				Collections.<String>emptyList(), true, true));
	}

	@Test
	public void tenthPlaceUsesItsFullNumber() {
		assertEquals("1. A\n2. B\n3. C\n4. D\n5. E\n6. F\n7. G\n8. H\n9. I\n10. J",
				SignFromHallOfFameLoader.formatPlayers(
						Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H", "I", "J"), true, true));
	}

	@Test
	public void otherLineSeparatedSignsKeepTheirOriginalFormat() {
		assertEquals("A\nB", SignFromHallOfFameLoader.formatPlayers(
				Arrays.asList("A", "B"), true, false));
	}

	@Test
	public void otherInlineSignsKeepTheirOriginalFormat() {
		assertEquals("[A, B]", SignFromHallOfFameLoader.formatPlayers(
				Arrays.asList("A", "B"), false, false));
	}
}
