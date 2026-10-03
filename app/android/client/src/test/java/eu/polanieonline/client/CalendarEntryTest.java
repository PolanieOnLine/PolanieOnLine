package eu.polanieonline.client;
import static org.junit.Assert.*;
import java.time.*;
import org.json.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class) @Config(sdk=29, manifest=Config.NONE)
public class CalendarEntryTest {
	static JSONObject json(int id,String start,String end,boolean allDay) throws Exception {
		return new JSONObject().put("id",id).put("title","Wydarzenie").put("description","Opis")
			.put("starts_at",start).put("ends_at",end).put("all_day",allDay).put("state","published");
	}
	@Test public void allDayUsesInclusiveLastDateAndWarsawMorning() throws Exception {
		CalendarEntry e=new CalendarEntry(json(1,"2026-10-30T00:00:00+01:00","2026-11-09T00:00:00+01:00",true));
		assertEquals("30 października 2026 do 8 listopada 2026",e.dates());
		assertEquals(Instant.parse("2026-10-30T08:00:00Z").toEpochMilli(),e.reminderAt(30));
	}
	@Test public void decemberRangeCrossesYearWithoutExtraDay() throws Exception {
		CalendarEntry e=new CalendarEntry(json(1,"2026-12-21T00:00:00+01:00","2027-01-11T00:00:00+01:00",true));
		assertEquals("21 grudnia 2026 do 10 stycznia 2027",e.dates());
	}
	@Test public void timedReminderUsesOffsetAndRejectsPastOrCancelled() throws Exception {
		JSONObject value=json(2,"2026-10-31T21:00:00+01:00","2026-10-31T22:00:00+01:00",false);
		CalendarEntry e=new CalendarEntry(value);
		assertEquals("31 października 2026, 21:00 do 22:00",e.dates());
		assertEquals(Instant.parse("2026-10-31T19:30:00Z").toEpochMilli(),e.reminderAt(30));
		assertFalse(e.canRemind(e.reminderAt(0),0));
		value.put("state","cancelled"); assertFalse(new CalendarEntry(value).canRemind(0,0));
	}
	@Test public void morningUsesDaylightSavingTime() throws Exception {
		CalendarEntry e=new CalendarEntry(json(1,"2026-10-10T00:00:00+02:00","2026-10-11T00:00:00+02:00",true));
		assertEquals(Instant.parse("2026-10-10T07:00:00Z").toEpochMilli(),e.reminderAt(0));
	}
	@Test public void eventsAreDeduplicatedSortedAndMalformedIgnored() throws Exception {
		JSONObject one=json(1,"2026-10-30T00:00:00+01:00","2026-11-09T00:00:00+01:00",true);
		JSONObject two=json(2,"2026-10-10T00:00:00+02:00","2026-10-11T00:00:00+02:00",true);
		JSONObject data=new JSONObject().put("events",new JSONArray().put(one).put(new JSONObject()).put(two))
			.put("upcoming",new JSONArray().put(one));
		assertEquals(2,SiteData.events(data,true).size()); assertEquals(2,SiteData.events(data,true).get(0).id);
	}
	@Test(expected=JSONException.class) public void rejectsNegativeId() throws Exception {
		new CalendarEntry(json(-1,"2026-10-10T00:00:00+02:00","2026-10-11T00:00:00+02:00",true));
	}
	@Test(expected=JSONException.class) public void rejectsReversedRange() throws Exception {
		new CalendarEntry(json(1,"2026-10-11T00:00:00+02:00","2026-10-10T00:00:00+02:00",true));
	}
	@Test(expected=JSONException.class) public void rejectsTamperedReminderOffset() throws Exception {
		new Reminders.Selection(new JSONObject().put("event",json(1,"2026-10-10T00:00:00+02:00","2026-10-11T00:00:00+02:00",true))
			.put("minutes",123).put("target",1));
	}
	@Test(expected=JSONException.class) public void rejectsTamperedTarget() throws Exception {
		new Reminders.Selection(new JSONObject().put("event",json(1,"2026-10-10T00:00:00+02:00","2026-10-11T00:00:00+02:00",true))
			.put("minutes",0).put("target",1));
	}
}
