/***************************************************************************
 *                    (C) Copyright 2003-2012 - Stendhal                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.sign;

import java.util.Calendar;

import games.stendhal.common.Rand;
import games.stendhal.common.constants.Actions;
import games.stendhal.common.grammar.Grammar;

/**
 * A map object that when looked at shows the server time.
 */
public class Clock extends Sign {
	/** Maximum seconds the clock can be wrong to either direction */
	private static final int MAX_IMPRECISION = 5;

	/**
	 * The amount of seconds this clock is wrong.
	 * [ -MAX_IMPRECISION, MAX_IMPRECISION - 1 ]
	 */
	private final int imprecisionSeconds;

	/**
	 * Create a new clock.
	 */
	public Clock() {
		put(Actions.ACTION, Actions.LOOK);
		put("class", "transparent");
		imprecisionSeconds = Rand.rand(2 * MAX_IMPRECISION) - MAX_IMPRECISION;
	}

	@Override
	public String describe() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, imprecisionSeconds);

		// Add 6 seconds so that the rounding is what humans expect
		cal.add(Calendar.SECOND, 6);

		int min = cal.get(Calendar.MINUTE);
		int hour = cal.get(Calendar.HOUR);
		if (min > 30) {
			// For getting the hour right for the "x to y" versions
			hour = (hour + 1) % 12;
		}
		if (hour == 0) {
			hour = 12;
		}

		StringBuilder msg = new StringBuilder("Jest godzina ");
		msg.append(describeMinute(min));
		msg.append(Grammar.numberString(hour));
		msg.append('.');

		return msg.toString();
	}

	/**
	 * Textual description of the minute part.
	 *
	 * @param m minute
	 * @return description of the minute. Empty string if it's even.
	 */
	private String describeMinute(int m) {
		switch (m) {
		case 1: return "jeden po ";
		case 2: return "dwa po ";
		case 3: return "trzy po ";
		case 4: return "cztery po ";
		case 5: return "pięć po ";
		case 6: return "sześć po ";
		case 7: return "siedem po ";
		case 8: return "osiem po ";
		case 9: return "dziewięć po ";
		case 10: return "dziesięć po ";
		case 11: return "jedenaście po ";
		case 12: return "dwanaście po ";
		case 13: return "trzynaście po ";
		case 14: return "czternaście po ";
		case 15: return "kwadrans po ";
		case 16: return "szestanście po ";
		case 17: return "siedemnaście po ";
		case 18: return "osiemnaście po ";
		case 19: return "dziewiętnaście po ";
		case 20: return "dwadzieścia po ";
		case 21: return "dwadzieścia jeden po ";
		case 22: return "dwadzieścia dwa po ";
		case 23: return "dwadzieścia trzy po ";
		case 24: return "dwadzieścia cztery po ";
		case 25: return "dwadzieścia pięć po ";
		case 26: return "dwadzieścia sześć po ";
		case 27: return "dwadzieścia siedem po ";
		case 28: return "dwadzieścia osiem po ";
		case 29: return "dwadzieścia dziewięć po ";
		case 30: return "trzydzieści po ";
		case 31: return "za dwadzieścia dziewięć ";
		case 32: return "za dwadzieścia osiem ";
		case 33: return "za dwadzieścia siedem ";
		case 34: return "za dwadzieścia sześć ";
		case 35: return "za dwadzieścia pięć ";
		case 36: return "za dwadzieścia cztery ";
		case 37: return "za dwadzieścia trzy ";
		case 38: return "za dwadzieścia dwa ";
		case 39: return "za dwadzieścia jeden ";
		case 40: return "za dwadzieścia ";
		case 41: return "za dziewiętnaście ";
		case 42: return "za osiemnaście ";
		case 43: return "za siedemnaście ";
		case 44: return "za szesnaście ";
		case 45: return "za kwadrans ";
		case 46: return "za czternaście ";
		case 47: return "za trzynaście ";
		case 48: return "za dwanaście ";
		case 49: return "za jedenaście ";
		case 50: return "za dziesięć ";
		case 51: return "za dziewięć ";
		case 52: return "za osiem ";
		case 53: return "za siedem ";
		case 54: return "za sześć ";
		case 55: return "za pięć ";
		case 56: return "za cztery ";
		case 57: return "za trzy ";
		case 58: return "za dwie ";
		case 59: return "za jeden ";
		default: return "";
		}
	}
}
