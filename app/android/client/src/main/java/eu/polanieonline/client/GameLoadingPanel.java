/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

/** Native loading surface. Never submits or retries game commands. */
final class GameLoadingPanel {
	static final long SLOW_AFTER_MS=45000;
	private final Activity activity;
	private final FrameLayout host;
	private final Handler handler=new Handler(Looper.getMainLooper());
	private Object owner;
	private Runnable cancellation;
	private long started;
	private final Runnable tick=this::refresh;
	GameLoadingPanel(Activity activity,FrameLayout host) { this.activity=activity; this.host=host; }
	boolean owns(Object candidate) { return owner!=null && owner==candidate; }
	boolean visible() { return owner!=null; }
	void begin(Object candidate,Runnable cancel) {
		if(owns(candidate)) { return; }
		owner=candidate; cancellation=cancel; started=SystemClock.elapsedRealtime(); refresh();
	}
	void finish(Object candidate) {
		if(!owns(candidate)) { return; }
		dispose();
	}
	void cancel() {
		if(owner==null) { return; }
		Runnable action=cancellation; dispose(); if(action!=null) { action.run(); }
	}
	void dispose() {
		handler.removeCallbacks(tick); owner=null; cancellation=null; host.removeAllViews(); host.setVisibility(View.GONE);
	}
	void refresh() {
		handler.removeCallbacks(tick); if(owner==null) { return; }
		boolean slow=SystemClock.elapsedRealtime()-started>=SLOW_AFTER_MS;
		host.removeAllViews(); host.setVisibility(View.VISIBLE);
		host.setBackgroundColor(0xf20c151d); host.setClickable(true);
		LinearLayout card=NativeUi.card(activity);
		card.addView(NativeUi.text(activity,slow ? "Ładowanie trwa dłużej niż zwykle" : "Łączenie z grą",20,NativeUi.TEXT));
		card.addView(NativeUi.text(activity,slow ? "Możesz nadal czekać lub wrócić do menu i spróbować ponownie."
			: "Trwa ładowanie klienta. Poczekaj chwilę.",14,NativeUi.MUTED));
		if(!slow) {
			ProgressBar progress=new ProgressBar(activity); progress.setIndeterminate(true);
			LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(NativeUi.dp(activity,40),NativeUi.dp(activity,40));
			pp.gravity=Gravity.CENTER_HORIZONTAL; pp.topMargin=NativeUi.dp(activity,12); card.addView(progress,pp);
		}
		card.addView(NativeUi.woodButton(activity,slow ? "Wróć do menu" : "Anuluj",this::cancel));
		ScrollView scroll=new ScrollView(activity); scroll.addView(card);
		int width=Math.max(180,Math.min(380,activity.getResources().getConfiguration().screenWidthDp-48));
		host.addView(scroll,new FrameLayout.LayoutParams(NativeUi.dp(activity,width),-2,Gravity.CENTER));
		if(!slow) { handler.postDelayed(tick,Math.max(1,SLOW_AFTER_MS-(SystemClock.elapsedRealtime()-started))); }
	}
}
