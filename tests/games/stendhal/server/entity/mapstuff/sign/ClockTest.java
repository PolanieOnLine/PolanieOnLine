package games.stendhal.server.entity.mapstuff.sign;

import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.entity.Entity;
import marauroa.common.game.RPClass;

public class ClockTest {
	@BeforeClass
	public static void setUpClass() {
		if (!RPClass.hasRPClass("entity")) {
			Entity.generateRPClass();
		}
		if (!RPClass.hasRPClass("sign")) {
			Sign.generateRPClass();
		}
	}

	@Test
	public void showsWarsawTimeWhenServerRunsInUtc() {
		final TimeZone previous = TimeZone.getDefault();
		try {
			TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
			final Instant before = Instant.now();
			final String description = new Clock().describe();
			final Set<String> expectedHours = new HashSet<>();
			for (int seconds = -5; seconds <= 12; seconds++) {
				final ZonedDateTime time = before.plusSeconds(seconds).atZone(ZoneId.of("Europe/Warsaw"));
				final int hour = (time.getHour() + (time.getMinute() > 30 ? 1 : 0)) % 12;
				expectedHours.add(Grammar.clockNumberString(hour == 0 ? 12 : hour) + '.');
			}
			assertTrue(description, expectedHours.stream().anyMatch(description::endsWith));
		} finally {
			TimeZone.setDefault(previous);
		}
	}
}
