/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.content.*;
import androidx.work.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import org.json.*;

/** Opt-in, per-device reminders. Preferences contain only public event information. */
final class Reminders {
	private Reminders() { }
	private static SharedPreferences prefs(Context c) { return c.getSharedPreferences("calendar_reminders",0); }
	static List<Selection> list(Context c) {
		List<Selection> result=new ArrayList<>();
		for(String key:prefs(c).getAll().keySet()) {
			try { result.add(new Selection(new JSONObject(prefs(c).getString(key,"{}")))); }
			catch(JSONException|RuntimeException ignored) { }
		}
		result.sort(Comparator.comparingLong(s->s.target)); return result;
	}
	static Selection find(Context c,int id) { for(Selection s:list(c)) { if(s.event.id==id) { return s; } } return null; }
	static synchronized void save(Context c,CalendarEntry e,int minutes) {
		long target=e.reminderAt(minutes);
		if(!e.canRemind(System.currentTimeMillis(),minutes)) { throw new IllegalArgumentException("Event is not in the future"); }
		try {
			JSONObject value=new JSONObject().put("event",e.json).put("minutes",minutes).put("target",target);
			prefs(c).edit().putString(Integer.toString(e.id),value.toString()).commit();
			schedule(c,e.id,target); ensureSync(c);
		} catch(JSONException exception) { throw new IllegalArgumentException("Invalid reminder",exception); }
	}
	private static void schedule(Context c,int id,long target) {
		OneTimeWorkRequest work=new OneTimeWorkRequest.Builder(EventReminderWorker.class)
			.setInputData(new Data.Builder().putInt("id",id).putLong("target",target).build())
			.setInitialDelay(Math.max(0,target-System.currentTimeMillis()),TimeUnit.MILLISECONDS).build();
		WorkManager.getInstance(c).enqueueUniqueWork("calendar-reminder-"+id,ExistingWorkPolicy.REPLACE,work);
	}
	static synchronized void remove(Context c,int id) {
		prefs(c).edit().remove(Integer.toString(id)).commit();
		WorkManager.getInstance(c).cancelUniqueWork("calendar-reminder-"+id); ensureSync(c);
	}
	static synchronized void delivered(Context c,int id,long target) {
		Selection current=find(c,id);
		if(current!=null && current.target==target) { prefs(c).edit().remove(Integer.toString(id)).commit(); ensureSync(c); }
	}
	static synchronized boolean update(Context c,Selection previous,CalendarEntry refreshed) {
		Selection current=find(c,previous.event.id);
		if(current==null || current.target!=previous.target) { return false; }
		if(!"published".equals(refreshed.state) || !refreshed.end.isAfter(Instant.now())) { remove(c,refreshed.id); return false; }
		long target=refreshed.reminderAt(current.minutes);
		if(target!=current.target) {
			if(target<=System.currentTimeMillis()) { remove(c,refreshed.id); return false; }
			save(c,refreshed,current.minutes); return false;
		}
		try { prefs(c).edit().putString(Integer.toString(refreshed.id),new JSONObject().put("event",refreshed.json)
			.put("minutes",current.minutes).put("target",current.target).toString()).commit(); }
		catch(JSONException ignored) { }
		return true;
	}
	private static void ensureSync(Context c) {
		if(list(c).isEmpty()) { WorkManager.getInstance(c).cancelUniqueWork("calendar-reminder-sync"); return; }
		PeriodicWorkRequest sync=new PeriodicWorkRequest.Builder(CalendarSyncWorker.class,6,TimeUnit.HOURS)
			.setInitialDelay(6,TimeUnit.HOURS)
			.setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();
		WorkManager.getInstance(c).enqueueUniquePeriodicWork("calendar-reminder-sync",ExistingPeriodicWorkPolicy.KEEP,sync);
	}
	static final class Selection {
		final CalendarEntry event; final int minutes; final long target;
		Selection(JSONObject value) throws JSONException {
			event=new CalendarEntry(value.getJSONObject("event")); minutes=value.getInt("minutes"); target=value.getLong("target");
			if(minutes!=0 && minutes!=30 && minutes!=60) { throw new JSONException("Invalid offset"); }
			if(target!=event.reminderAt(minutes)) { throw new JSONException("Invalid target"); }
		}
	}
}
