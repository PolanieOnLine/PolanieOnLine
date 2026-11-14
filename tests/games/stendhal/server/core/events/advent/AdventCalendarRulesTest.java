package games.stendhal.server.core.events.advent;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.time.LocalDate;

import org.junit.Test;

public class AdventCalendarRulesTest {

	@Test
	public void allowsTodayAndExactlyThreePreviousDates() {
		final LocalDate today = LocalDate.of(2026, 12, 10);
		for (int day = 7; day <= 10; day++) {
			assertEquals("available", AdventCalendarRules.state(today, day, 0));
		}
		assertEquals("expired", AdventCalendarRules.state(today, 6, 0));
		assertEquals("future", AdventCalendarRules.state(today, 11, 0));
	}

	@Test
	public void christmasEveGiftExpiresAfterDecember27() {
		assertEquals("available", AdventCalendarRules.state(LocalDate.of(2026, 12, 27), 24, 0));
		assertEquals("expired", AdventCalendarRules.state(LocalDate.of(2026, 12, 28), 24, 0));
	}

	@Test
	public void claimsAreRecognisedWithoutOpeningFutureOrInvalidDays() {
		assertEquals("claimed", AdventCalendarRules.state(LocalDate.of(2026, 12, 10), 3, 1 << 2));
		assertEquals("invalid", AdventCalendarRules.state(LocalDate.of(2026, 12, 10), 0, 0));
		assertEquals("invalid", AdventCalendarRules.state(LocalDate.of(2026, 12, 10), 25, 0));
		assertEquals("future", AdventCalendarRules.state(LocalDate.of(2026, 11, 30), 1, 0));
		assertEquals("future", AdventCalendarRules.state(LocalDate.of(2027, 1, 1), 1, 0));
	}

	@Test
	public void midnightUsesWarsawInsteadOfMachineTimeZone() {
		assertEquals(LocalDate.of(2026, 12, 1),
				AdventCalendarRules.dateAt(Instant.parse("2026-11-30T23:00:00Z")));
		assertEquals(LocalDate.of(2026, 12, 10),
				AdventCalendarRules.dateAt(Instant.parse("2026-12-09T23:30:00Z")));
	}
}
