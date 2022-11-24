/***************************************************************************
 *                   (C) Copyright 2011-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp;

import java.util.Calendar;

/**
 * day light phase
 *
 * @author hendrik
 */
public enum DaylightPhase {
	/** during the night */
	NIGHT (0x47408c, "noc"),
	/** early morning before sunrise at */
	DAWN (0x774590, "świt"),
	/** the sun is rising */
	SUNRISE (0xc0a080, "wschód słońca"),
	/** during the day */
	DAY ("dzień"),
	/** the sun is setting */
	SUNSET (0xc0a080, "zachód słońca"),
	/** early night */
	DUSK (0x774590, "wieczór");

	private Integer color;
	private String greetingName;

	// this should only be set for testing purposes
	private static DaylightPhase testing_phase;

	private DaylightPhase(int color, String greetingName) {
		this.color = Integer.valueOf(color);
		this.greetingName = greetingName;
	}

	private DaylightPhase(String greetingName) {
		this.color = null;
		this.greetingName = greetingName;
	}

	/**
	 * gets the current daylight phase
	 *
	 * @return DaylightPhase
	 */
	public static DaylightPhase current() {
		if (testing_phase != null) {
			return testing_phase;
		}

		Calendar cal = Calendar.getInstance();

		int hour = cal.get(Calendar.HOUR_OF_DAY);
		int month = cal.get(Calendar.MONTH);

		int timeDayNight;
		int timeDawnDusk;
		// winter
		if (month == 11 || month == 0 || month == 1) {
			timeDayNight = 7;
			timeDawnDusk = 6;
		// summer
		} else if (month == 5 || month == 6 || month == 7) {
			timeDayNight = 4;
			timeDawnDusk = 3;
		// spring/autumn
		} else {
			// late autumn month and march
			if (month == 10 || month == 2) {
				timeDayNight = 6;
				timeDawnDusk = 5;
			} else {
				timeDayNight = 5;
				timeDawnDusk = 4;
			}
		}

		// anything but precise, but who cares
		int diffToMidnight = Math.min(hour, 24 - hour);
		if (diffToMidnight > timeDayNight) {
			return DAY;
		} else if (diffToMidnight == timeDayNight) {
			if (hour < 12) {
				return SUNRISE;
			} else {
				return SUNSET;
			}
		} else if (diffToMidnight == timeDawnDusk) {
			if (hour < 12) {
				return DAWN;
			} else {
				return DUSK;
			}
		} else {
			return NIGHT;
		}
	}

	/**
	 * gets the color of this daylight phase
	 *
	 * @return color
	 */
	public Integer getColor() {
		return color;
	}

	/**
	 * Gets the greeting name
	 *
	 * @return greeting name
	 */
	public String getGreetingName() {
		return greetingName;
	}

	/**
	 * WARNING: this should only be used for testing purposes.
	 *
	 * @param phase
	 * 		<code>DaylightPhase</code> to set for testing.
	 */
	public static void setTestingPhase(final DaylightPhase phase) {
		testing_phase = phase;
	}

	/**
	 * Disables testing phase.
	 */
	public static void unsetTestingPhase() {
		testing_phase = null;
	}
}
