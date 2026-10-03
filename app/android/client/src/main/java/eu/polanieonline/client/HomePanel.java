/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.content.Intent;
import android.content.res.Configuration;
import android.view.*;
import android.widget.*;
import java.time.Instant;
import org.json.*;

/** Native home screen; the game page is never used to show public website content. */
final class HomePanel {
	private final MainActivity activity;
	private final android.widget.FrameLayout host;
	private JSONObject news, calendar;
	private boolean disposed, loading;
	private long lastLoad;
	HomePanel(MainActivity activity) {
		this.activity=activity; host=activity.findViewById(R.id.home_panel);
		news=SiteData.cached(activity,"home"); calendar=SiteData.cached(activity,"calendar-home"); render();
	}
	void show(boolean visible) {
		host.setVisibility(visible ? View.VISIBLE : View.GONE);
		if (visible) { render(); refresh(); Updates.automatic(activity); }
	}
	void dispose() { disposed=true; }
	void render() {
		if (disposed) { return; }
		host.removeAllViews();
		FrameLayout stage=new FrameLayout(activity);
		int gap=NativeUi.dp(activity,16);
		stage.setPadding(gap,gap,gap,gap);
		boolean wide=activity.getResources().getConfiguration().orientation==Configuration.ORIENTATION_LANDSCAPE;
		int screen=activity.getResources().getConfiguration().screenWidthDp;
		int panelWidth=NativeUi.dp(activity,Math.max(240,Math.min(280,screen*.29f)));

		LinearLayout menu=NativeUi.column(activity);
		menu.setBackground(new NativeUi.Ornament(activity,false,true));
		menu.setPadding(NativeUi.dp(activity,18),NativeUi.dp(activity,14),NativeUi.dp(activity,18),NativeUi.dp(activity,22));
		menu.addView(NativeUi.link(activity,"Wersja "+BuildConfig.VERSION_NAME+" | Aktualizacje",()->Updates.open(activity)));
		Button play=NativeUi.woodButton(activity,"Graj",activity::loadLogin); play.setTextSize(18); play.getLayoutParams().height=NativeUi.dp(activity,54);
		play.setBackgroundResource(R.drawable.btn_wood); play.setBackgroundTintList(null); menu.addView(play);
		menu.addView(NativeUi.row(activity,NativeUi.woodButton(activity,"Zapisane konta",()->SavedAccounts.show(activity)),
			NativeUi.woodButton(activity,"Kalendarz",()->activity.startActivity(new Intent(activity,CalendarActivity.class)))));
		menu.addView(NativeUi.row(activity,NativeUi.woodButton(activity,"Ustawienia",activity::showSettings),
			NativeUi.woodButton(activity,"Zgłoś problem",()->Diagnostics.show(activity))));
		menu.addView(NativeUi.row(activity,NativeUi.woodButton(activity,"Moje konto",()->NativeUi.openSite(activity,"/profile")),
			NativeUi.woodButton(activity,"Pomoc",()->NativeUi.openSite(activity,"/faq"))));

		LinearLayout feed=new LinearLayout(activity);
		LinearLayout newsCard=compactCard();
		newsCard.addView(NativeUi.text(activity,"AKTUALNOŚCI",10,NativeUi.TEAL));
		JSONArray list=news.optJSONArray("news"); JSONObject article=list==null ? null : list.optJSONObject(0);
		String newsTitle=article==null ? "Sprawdź wiadomości na stronie" : article.optString("title");
		TextView articleTitle=NativeUi.text(activity,newsTitle,14,NativeUi.TEXT);
		articleTitle.setMaxLines(2); articleTitle.setEllipsize(android.text.TextUtils.TruncateAt.END); newsCard.addView(articleTitle);
		String path=article==null ? "/archiwum" : article.optString("nice_url","/archiwum");
		if(!path.startsWith("/news/") || path.startsWith("//")) { path="/archiwum"; }
		final String articlePath=path;
		newsCard.addView(NativeUi.link(activity,"Czytaj",()->NativeUi.openSite(activity,articlePath)));
		feed.addView(newsCard);

		LinearLayout eventCard=compactCard();
		LinearLayout.LayoutParams ep=(LinearLayout.LayoutParams)eventCard.getLayoutParams(); ep.leftMargin=NativeUi.dp(activity,12);
		eventCard.addView(NativeUi.text(activity,"WYDARZENIA",10,NativeUi.TEAL));
		CalendarEntry nearest=null;
		for(CalendarEntry event:SiteData.events(calendar,true)) {
			if("published".equals(event.state) && event.end.isAfter(Instant.now())) { nearest=event; break; }
		}
		TextView eventTitle=NativeUi.text(activity,nearest==null ? "Zobacz kalendarz" : nearest.title,14,NativeUi.TEXT);
		eventTitle.setMaxLines(2); eventTitle.setEllipsize(android.text.TextUtils.TruncateAt.END); eventCard.addView(eventTitle);
		if(nearest!=null) { TextView date=NativeUi.text(activity,nearest.dates(),11,NativeUi.MUTED); date.setMaxLines(2); eventCard.addView(date); }
		eventCard.addView(NativeUi.link(activity,"Zobacz",()->activity.startActivity(new Intent(activity,CalendarActivity.class))));
		feed.addView(eventCard);
		ScrollView menuScroll=new ScrollView(activity); menuScroll.setFillViewport(false);
		ImageView brand=NativeUi.brand(activity);
		int availableBrandWidth=wide ? Math.max(NativeUi.dp(activity,200),NativeUi.dp(activity,screen)-2*gap-panelWidth-NativeUi.dp(activity,20)) : NativeUi.dp(activity,screen)-2*gap;
		int brandWidth=Math.min(NativeUi.dp(activity,250),availableBrandWidth);
		FrameLayout.LayoutParams bp=new FrameLayout.LayoutParams(brandWidth,NativeUi.dp(activity,60),Gravity.TOP|(wide ? Gravity.START : Gravity.CENTER_HORIZONTAL));
		if(wide) { bp.leftMargin=(availableBrandWidth-brandWidth)/2; }
		bp.topMargin=NativeUi.dp(activity,wide ? 16 : 28); stage.addView(brand,bp);
		if(wide) {
			menuScroll.addView(menu);
			FrameLayout.LayoutParams mp=new FrameLayout.LayoutParams(panelWidth,-2,Gravity.END|Gravity.CENTER_VERTICAL);
			stage.addView(menuScroll,mp);
			int feedWidth=Math.max(NativeUi.dp(activity,200),NativeUi.dp(activity,screen)-2*gap-panelWidth-NativeUi.dp(activity,20));
			FrameLayout.LayoutParams fp=new FrameLayout.LayoutParams(feedWidth,-2,Gravity.START|Gravity.BOTTOM);
			stage.addView(feed,fp);
		} else {
			LinearLayout bottom=NativeUi.column(activity); bottom.addView(menu);
			LinearLayout.LayoutParams fp=new LinearLayout.LayoutParams(-1,-2); fp.topMargin=NativeUi.dp(activity,12); feed.setLayoutParams(fp); bottom.addView(feed);
			menuScroll.addView(bottom); stage.addView(menuScroll,new FrameLayout.LayoutParams(-1,-2,Gravity.BOTTOM));
		}
		host.addView(stage,new FrameLayout.LayoutParams(-1,-1));
	}
	private LinearLayout compactCard() {
		LinearLayout card=NativeUi.column(activity); card.setBackground(new NativeUi.Ornament(activity,true));
		card.setPadding(NativeUi.dp(activity,16),NativeUi.dp(activity,12),NativeUi.dp(activity,16),NativeUi.dp(activity,16));
		card.setLayoutParams(new LinearLayout.LayoutParams(0,-1,1)); return card;
	}
	private void refresh() {
		if(loading || System.currentTimeMillis()-lastLoad<60000) { return; }
		loading=true; lastLoad=System.currentTimeMillis();
		final android.content.Context app=activity.getApplicationContext();
		SiteData.IO.execute(()->{
			boolean failed=false;
			JSONObject nextNews=news,nextCalendar=calendar;
			try { nextNews=SiteData.fetch("/api/v1/site/home"); SiteData.cache(app,"home",nextNews); } catch(Exception e) { failed=true; }
			try { nextCalendar=SiteData.fetch("/api/v1/site/calendar"); SiteData.cache(app,"calendar-home",nextCalendar); } catch(Exception e) { failed=true; }
			final JSONObject n=nextNews,c=nextCalendar; final boolean offline=failed;
			activity.runOnUiThread(()->{
				if(disposed || activity.isDestroyed()) { return; }
				loading=false; news=n; calendar=c; render();
				if(offline && host.getVisibility()==View.VISIBLE) { Toast.makeText(activity,"Nie udało się odświeżyć danych strony.",Toast.LENGTH_SHORT).show(); }
			});
		});
	}
}
