package games.stendhal.server.core.events.advent;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/** Calendar dates are always interpreted in Polish civil time. */
public final class AdventCalendarRules {

	public static final ZoneId TIME_ZONE = ZoneId.of("Europe/Warsaw");

	private AdventCalendarRules() {
	}

	public static LocalDate dateAt(final Instant instant) {
		return instant.atZone(TIME_ZONE).toLocalDate();
	}

	public static String state(final LocalDate today, final int day, final int claimedMask) {
		return state(today, day, claimedMask, false);
	}

	static String state(final LocalDate today, final int day, final int claimedMask, final boolean debug) {
		if (day < 1 || day > 24) {
			return "invalid";
		}
		if ((claimedMask & (1 << (day - 1))) != 0) {
			return "claimed";
		}
		if (debug) {
			return "available";
		}
		if (today.getMonthValue() != 12 || today.getDayOfMonth() < day) {
			return "future";
		}
		return today.getDayOfMonth() - day <= 3 ? "available" : "expired";
	}
}
