/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.work.*;
import java.time.Instant;
import java.util.HashMap;

public final class EventReminderWorker extends Worker {
	static final String CHANNEL="calendar-events";
	public EventReminderWorker(@NonNull Context context,@NonNull WorkerParameters parameters) { super(context,parameters); }
	@NonNull @Override public Result doWork() {
		Context context=getApplicationContext();
		int id=getInputData().getInt("id",-1); long target=getInputData().getLong("target",-1);
		Reminders.Selection selection=Reminders.find(context,id);
		if(selection==null || selection.target!=target) { return Result.success(); }
		if(System.currentTimeMillis()<target) { return Result.retry(); }
		try {
			CalendarEntry fresh=CalendarSyncWorker.lookup(selection.event,new HashMap<>());
			if(fresh!=null) { if(!Reminders.update(context,selection,fresh)) { return Result.success(); } selection=Reminders.find(context,id); }
			else { // Do not notify about a removed or moved event whose latest date cannot be confirmed.
				Reminders.remove(context,id); return Result.success();
			}
		} catch(Exception offline) { /* A valid locally saved date remains usable when temporarily offline. */ }
		if(isStopped() || selection==null || !selection.event.end.isAfter(Instant.now())) { Reminders.delivered(context,id,target); return Result.success(); }
		if(!canNotify(context)) { Reminders.delivered(context,id,target); return Result.success(); }
		ensureChannel(context);
		Intent intent=new Intent(context,CalendarActivity.class).putExtra("event_id",id).putExtra("event",selection.event.json.toString());
		PendingIntent pending=PendingIntent.getActivity(context,id,intent,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
		android.app.Notification notification=new NotificationCompat.Builder(context,CHANNEL)
			.setSmallIcon(R.drawable.ic_event_notification).setContentTitle(selection.event.title)
			.setContentText(selection.event.dates()).setStyle(new NotificationCompat.BigTextStyle().bigText(selection.event.dates()))
			.setContentIntent(pending).setAutoCancel(true).build();
		// Re-check after network I/O: removal or rescheduling must invalidate an older worker.
		synchronized(Reminders.class) {
			Reminders.Selection current=Reminders.find(context,id);
			if(current==null || current.target!=target || isStopped()) { return Result.success(); }
			try { NotificationManagerCompat.from(context).notify(id,notification); }
			catch(SecurityException denied) { }
			Reminders.delivered(context,id,target);
		}
		return Result.success();
	}
	static void ensureChannel(Context context) {
		if(Build.VERSION.SDK_INT>=26) {
			NotificationManager manager=context.getSystemService(NotificationManager.class);
			manager.createNotificationChannel(new NotificationChannel(CHANNEL,"Wydarzenia PolanieOnLine",NotificationManager.IMPORTANCE_DEFAULT));
		}
	}
	static boolean canNotify(Context context) {
		if(Build.VERSION.SDK_INT>=33 && ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) { return false; }
		if(!NotificationManagerCompat.from(context).areNotificationsEnabled()) { return false; }
		if(Build.VERSION.SDK_INT>=26) {
			NotificationChannel channel=context.getSystemService(NotificationManager.class).getNotificationChannel(CHANNEL);
			if(channel!=null && channel.getImportance()==NotificationManager.IMPORTANCE_NONE) { return false; }
		}
		return true;
	}
}
