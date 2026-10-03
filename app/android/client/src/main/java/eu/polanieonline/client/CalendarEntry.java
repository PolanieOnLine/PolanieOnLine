/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import java.time.*;
import java.time.format.DateTimeFormatter;
import org.json.*;

final class CalendarEntry {
	static final ZoneId WARSAW = ZoneId.of("Europe/Warsaw");
	final int id;
	final String title, description, state, location;
	final boolean allDay;
	final Instant start, end;
	final JSONObject json;
	CalendarEntry(JSONObject value) throws JSONException {
		json = new JSONObject(value.toString()); id = value.getInt("id");
		if (id<=0) { throw new JSONException("Invalid event id"); }
		title = value.getString("title"); description = value.optString("description", "");
		location = value.isNull("location") ? "" : value.optString("location", "");
		state = value.getString("state"); allDay = value.getBoolean("all_day");
		start = OffsetDateTime.parse(value.getString("starts_at")).toInstant();
		end = OffsetDateTime.parse(value.getString("ends_at")).toInstant();
		if (!end.isAfter(start) || title.length()>160) { throw new JSONException("Invalid event"); }
	}
	String dates() {
		String first = date(start.atZone(WARSAW));
		String last = date(end.minusSeconds(1).atZone(WARSAW));
		if (allDay) { return first.equals(last) ? first + ", cały dzień" : first + " do " + last; }
		DateTimeFormatter time = DateTimeFormatter.ofPattern("HH:mm");
		return first + ", " + time.format(start.atZone(WARSAW)) + " do "
				+ (first.equals(last) ? "" : date(end.atZone(WARSAW)) + ", ") + time.format(end.atZone(WARSAW));
	}
	private static String date(ZonedDateTime value) {
		// Android 5 desugaring and newer Android locales do not always inflect Polish month names equally.
		String[] months={"stycznia","lutego","marca","kwietnia","maja","czerwca","lipca","sierpnia","września","października","listopada","grudnia"};
		return value.getDayOfMonth()+" "+months[value.getMonthValue()-1]+" "+value.getYear();
	}
	long reminderAt(int minutes) {
		return allDay ? start.atZone(WARSAW).toLocalDate().atTime(9,0).atZone(WARSAW).toInstant().toEpochMilli()
				: start.minusSeconds(minutes*60L).toEpochMilli();
	}
	boolean canRemind(long now, int minutes) { return "published".equals(state) && reminderAt(minutes)>now; }
}
