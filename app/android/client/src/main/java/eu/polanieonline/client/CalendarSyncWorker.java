/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import java.time.*;
import java.util.*;
import org.json.*;

/** Updates selected reminders only, without fetching private game data. */
public final class CalendarSyncWorker extends Worker {
	public CalendarSyncWorker(@NonNull Context context,@NonNull WorkerParameters parameters) { super(context,parameters); }
	@NonNull @Override public Result doWork() {
		Map<String,JSONObject> months=new HashMap<>();
		boolean failed=false;
		for(Reminders.Selection selection:Reminders.list(getApplicationContext())) {
			if(isStopped()) { return Result.success(); }
			try {
				CalendarEntry updated=lookup(selection.event,months);
				if(updated!=null) { Reminders.update(getApplicationContext(),selection,updated); }
				// Missing is not proof of cancellation: an administrator may move the event to another month.
				// Leave the selection intact until a valid updated entry or explicit cancellation is returned.
			} catch(Exception e) { failed=true; }
		}
		return failed ? Result.retry() : Result.success();
	}
	static CalendarEntry lookup(CalendarEntry event,Map<String,JSONObject> months) throws Exception {
		LinkedHashSet<String> search=new LinkedHashSet<>();
		search.add(YearMonth.from(event.start.atZone(CalendarEntry.WARSAW)).toString());
		YearMonth now=YearMonth.now(CalendarEntry.WARSAW);
		// Calendar has no single-event endpoint. A moved event may disappear from its old month
		// and from the five nearest events, so verify the next year before treating it as unavailable.
		for(int offset=0;offset<=12;offset++) { search.add(now.plusMonths(offset).toString()); }
		for(String month:search) {
			JSONObject data=months.get(month);
			if(data==null) {
				data=SiteData.fetch("/api/v1/site/calendar?month="+month);
				if(data.optJSONArray("events")==null) { throw new org.json.JSONException("Missing events"); }
				months.put(month,data);
			}
			for(CalendarEntry entry:SiteData.events(data,true)) { if(entry.id==event.id) { return entry; } }
		}
		return null;
	}
}
