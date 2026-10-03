/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.Manifest;
import android.app.AlertDialog;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.json.*;

/** Public calendar and reminders are native Android screens, separate from the game WebView. */
public final class CalendarActivity extends AppCompatActivity {
	private YearMonth month;
	private LinearLayout list;
	private TextView status;
	private long generation;
	private CalendarEntry pending;
	private int pendingMinutes;
	private boolean first=true;
	@Override protected void onCreate(Bundle state) {
		super.onCreate(state);
		month=state==null ? YearMonth.now(CalendarEntry.WARSAW) : YearMonth.parse(state.getString("month"));
		if(state!=null) { first=state.getBoolean("first",false); try { String json=state.getString("pending"); if(json!=null) { pending=new CalendarEntry(new JSONObject(json)); pendingMinutes=state.getInt("minutes"); } } catch(Exception ignored) { } }
		LinearLayout root=createContent();
		NativeUi.screen(this,root,true); load();
		if(state==null && getIntent().hasExtra("event")) {
			try { CalendarEntry event=new CalendarEntry(new JSONObject(getIntent().getStringExtra("event")));
				getIntent().removeExtra("event_id"); getIntent().removeExtra("event"); root.post(()->details(event));
			} catch(Exception ignored) { }
		}
	}
	LinearLayout createContent() {
		LinearLayout root=NativeUi.column(this),header=NativeUi.card(this);
		boolean wide=getResources().getConfiguration().orientation==android.content.res.Configuration.ORIENTATION_LANDSCAPE
			&& getResources().getConfiguration().screenWidthDp>=600;
		header.setBackground(new NativeUi.Ornament(this,false,true));
		LinearLayout heading=new LinearLayout(this); heading.setGravity(android.view.Gravity.CENTER_VERTICAL);
		ImageView logo=NativeUi.brand(this); heading.addView(logo,new LinearLayout.LayoutParams(NativeUi.dp(this,136),NativeUi.dp(this,28)));
		android.view.View space=new android.view.View(this); heading.addView(space,new LinearLayout.LayoutParams(0,1,1));
		Button back=NativeUi.woodButton(this,"Wróć",this::finish); back.setLayoutParams(new LinearLayout.LayoutParams(NativeUi.dp(this,72),NativeUi.dp(this,48)));
		heading.addView(back); header.addView(heading);
		TextView title=NativeUi.text(this,"KALENDARZ WYDARZEŃ",20,NativeUi.TEXT); title.setTypeface(null,android.graphics.Typeface.BOLD);
		header.addView(title);
		LinearLayout navigation=new LinearLayout(this);
		Button previous=NativeUi.woodButton(this,"Poprzedni",()->change(-1)),next=NativeUi.woodButton(this,"Następny",()->change(1));
		previous.setLayoutParams(new LinearLayout.LayoutParams(NativeUi.dp(this,84),NativeUi.dp(this,48))); next.setLayoutParams(new LinearLayout.LayoutParams(NativeUi.dp(this,84),NativeUi.dp(this,48)));
		status=NativeUi.text(this,"",16,NativeUi.TEAL); status.setGravity(android.view.Gravity.CENTER);
		status.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1)); navigation.setGravity(android.view.Gravity.CENTER_VERTICAL);
		if(wide) {
			status.setLayoutParams(new LinearLayout.LayoutParams(-1,-2)); header.addView(status);
			header.addView(NativeUi.row(this,previous,next));
		} else { navigation.addView(previous); navigation.addView(status); navigation.addView(next); header.addView(navigation); }
		Button reminders=NativeUi.woodButton(this,"Moje przypomnienia",this::showReminders),website=NativeUi.woodButton(this,"Kalendarz na stronie",()->NativeUi.openSite(this,"/kalendarz"));
		if(wide) { header.addView(reminders); header.addView(website); }
		else { header.addView(NativeUi.row(this,reminders,website)); }
		LinearLayout body=NativeUi.column(this); list=NativeUi.column(this); body.addView(list);
		body.addView(NativeUi.text(this,"Przypomnienia są dobrowolne i zapisują się tylko na tym telefonie. Oszczędzanie baterii może opóźnić powiadomienie.",13,NativeUi.MUTED));
		if(wide) {
			root.setOrientation(LinearLayout.HORIZONTAL);
			body.setLayoutParams(new LinearLayout.LayoutParams(0,-2,1)); root.addView(body);
			LinearLayout.LayoutParams sidebar=new LinearLayout.LayoutParams(NativeUi.dp(this,260),-2); sidebar.leftMargin=NativeUi.dp(this,14); sidebar.topMargin=NativeUi.dp(this,12);
			header.setLayoutParams(sidebar); root.addView(header);
		} else { root.addView(header); root.addView(body); }
		return root;
	}
	@Override protected void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state); state.putString("month",month.toString()); state.putBoolean("first",first);
		if(pending!=null) { state.putString("pending",pending.json.toString()); state.putInt("minutes",pendingMinutes); }
	}
	private void change(int delta) { YearMonth candidate=month.plusMonths(delta); if(candidate.getYear()<2000 || candidate.getYear()>2099) { return; } month=candidate; first=false; load(); }
	private void load() {
		long request=++generation; String selected=month.toString(); boolean upcoming=first;
		status.setText(month.format(DateTimeFormatter.ofPattern("LLLL yyyy",Locale.forLanguageTag("pl-PL"))));
		render(SiteData.cached(this,"calendar-"+selected),upcoming);
		final Context app=getApplicationContext();
		SiteData.IO.execute(()->{
			try {
				JSONObject data=SiteData.fetch("/api/v1/site/calendar?month="+selected); SiteData.cache(app,"calendar-"+selected,data);
				runOnUiThread(()->{ if(!isDestroyed() && generation==request) { render(data,upcoming); } });
			} catch(Exception e) { runOnUiThread(()->{ if(!isDestroyed() && generation==request) { Toast.makeText(this,"Nie udało się odświeżyć kalendarza.",Toast.LENGTH_LONG).show(); } }); }
		});
	}
	private void render(JSONObject data,boolean upcoming) {
		list.removeAllViews(); int count=0;
		if(upcoming) { list.addView(NativeUi.text(this,"Ten miesiąc i najbliższe wydarzenia",14,NativeUi.MUTED)); }
		for(CalendarEntry event:SiteData.events(data,upcoming)) {
			Instant from=month.atDay(1).atStartOfDay(CalendarEntry.WARSAW).toInstant();
			Instant to=month.plusMonths(1).atDay(1).atStartOfDay(CalendarEntry.WARSAW).toInstant();
			if(!upcoming && (!event.start.isBefore(to) || !event.end.isAfter(from))) { continue; }
			if(upcoming && !event.end.isAfter(Instant.now())) { continue; }
			count++;
			LinearLayout card=NativeUi.card(this);
			card.addView(NativeUi.text(this,event.title,19,NativeUi.TEXT));
			card.addView(NativeUi.text(this,event.dates(),14,NativeUi.MUTED));
			if("cancelled".equals(event.state)) { card.addView(NativeUi.text(this,"Odwołane",14,0xfff29d8d)); }
			if(!event.description.isEmpty()) { TextView description=NativeUi.text(this,event.description,14,NativeUi.MUTED); description.setMaxLines(3); description.setEllipsize(android.text.TextUtils.TruncateAt.END); card.addView(description); }
			card.addView(NativeUi.link(this,"Szczegóły i przypomnienie",()->details(event))); list.addView(card);
			if(getIntent().getIntExtra("event_id",-1)==event.id) { getIntent().removeExtra("event_id"); list.post(()->details(event)); }
		}
		if(count==0) { list.addView(NativeUi.text(this,data.has("events") ? "Brak wydarzeń w tym okresie." : "Pobieranie wydarzeń...",16,NativeUi.MUTED)); }
	}
	private void details(CalendarEntry event) {
		String message=event.dates()+"\n\n"+event.description+(event.location.isEmpty() ? "" : "\n\nMiejsce: "+event.location);
		AlertDialog.Builder dialog=new AlertDialog.Builder(this,R.style.NativeDialog).setTitle(event.title).setMessage(message).setNegativeButton("Zamknij",null);
		if(Reminders.find(this,event.id)!=null) { dialog.setNeutralButton("Usuń przypomnienie",(d,w)->{ Reminders.remove(this,event.id); Toast.makeText(this,"Usunięto przypomnienie.",Toast.LENGTH_SHORT).show(); }); }
		if(event.canRemind(System.currentTimeMillis(),0)) { dialog.setPositiveButton("Przypomnij",(d,w)->choose(event)); }
		dialog.show();
	}
	private void choose(CalendarEntry event) {
		List<Integer> offsets=new ArrayList<>(); List<String> labels=new ArrayList<>();
		if(event.allDay) { offsets.add(0); labels.add("Pierwszego dnia o 9:00"); }
		else { for(int minutes:new int[]{30,60,0}) { if(event.canRemind(System.currentTimeMillis(),minutes)) { offsets.add(minutes); labels.add(minutes==0 ? "Na początku wydarzenia" : minutes+" minut wcześniej"); } } }
		new AlertDialog.Builder(this,R.style.NativeDialog).setTitle("Kiedy przypomnieć?").setItems(labels.toArray(new String[0]),(d,w)->enable(event,offsets.get(w))).setNegativeButton("Anuluj",null).show();
	}
	private void enable(CalendarEntry event,int minutes) {
		EventReminderWorker.ensureChannel(this);
		if(Build.VERSION.SDK_INT>=33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED) {
			pending=event; pendingMinutes=minutes; requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},41); return;
		}
		save(event,minutes);
	}
	@Override public void onRequestPermissionsResult(int request,String[] permissions,int[] results) {
		super.onRequestPermissionsResult(request,permissions,results);
		if(request==41 && pending!=null) {
			CalendarEntry event=pending; int minutes=pendingMinutes; pending=null;
			if(results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) { save(event,minutes); }
			else { notificationSettings(); }
		}
	}
	private void save(CalendarEntry event,int minutes) {
		if(!EventReminderWorker.canNotify(this)) { notificationSettings(); return; }
		try { Reminders.save(this,event,minutes); Toast.makeText(this,"Zapisano przypomnienie.",Toast.LENGTH_SHORT).show(); }
		catch(IllegalArgumentException e) { Toast.makeText(this,"Ten termin już minął.",Toast.LENGTH_SHORT).show(); }
	}
	private void notificationSettings() {
		new AlertDialog.Builder(this,R.style.NativeDialog).setMessage("Powiadomienia są wyłączone. Aby korzystać z przypomnień, włącz je w ustawieniach telefonu.")
			.setNegativeButton("Anuluj",null).setPositiveButton("Ustawienia",(d,w)->{
				Intent intent=Build.VERSION.SDK_INT>=26 ? new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName())
					: new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,android.net.Uri.parse("package:"+getPackageName()));
				startActivity(intent);
			}).show();
	}
	private void showReminders() {
		List<Reminders.Selection> selected=Reminders.list(this);
		if(selected.isEmpty()) { new AlertDialog.Builder(this,R.style.NativeDialog).setMessage("Nie masz zapisanych przypomnień. Wybierz wydarzenie i dotknij Przypomnij.").setPositiveButton("OK",null).show(); return; }
		String[] labels=new String[selected.size()];
		for(int i=0;i<labels.length;i++) {
			Reminders.Selection s=selected.get(i); labels[i]=s.event.title+"\n"+Instant.ofEpochMilli(s.target).atZone(CalendarEntry.WARSAW).format(DateTimeFormatter.ofPattern("d.MM.yyyy HH:mm"));
		}
		new AlertDialog.Builder(this,R.style.NativeDialog).setTitle("Moje przypomnienia").setItems(labels,(d,w)->details(selected.get(w).event)).setNegativeButton("Zamknij",null).show();
	}
}
