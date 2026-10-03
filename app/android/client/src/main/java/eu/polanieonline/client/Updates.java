/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.lang.ref.WeakReference;

/** Checks only in the menu, without keeping an Activity alive during network IO. */
final class Updates {
	private static boolean checking;
	private Updates() { }
	static boolean direct(Context context) {
		return BuildConfig.DIRECT_UPDATES && "eu.polanieonline.client".equals(context.getPackageName())
			&& !"com.android.vending".equals(context.getPackageManager().getInstallerPackageName(context.getPackageName()));
	}
	static boolean playing() {
		MainActivity main=MainActivity.get();
		if(main!=null) { for(ClientView view:main.getClientViewList()) { if(!view.onTitleScreen()) { return true; } } }
		return false;
	}
	static void open(Activity activity) {
		if(playing()) { Toast.makeText(activity,"Wróć do menu gry, aby sprawdzić aktualizacje.",Toast.LENGTH_LONG).show(); return; }
		activity.startActivity(new Intent(activity,UpdateActivity.class));
	}
	static synchronized void automatic(MainActivity activity) {
		if(checking || !direct(activity) || playing()) { return; }
		Context app=activity.getApplicationContext();
		android.content.SharedPreferences prefs=app.getSharedPreferences("android_updater",Context.MODE_PRIVATE);
		long now=System.currentTimeMillis();
		if(now-prefs.getLong("checked",0)<6*60*60*1000L) { return; }
		checking=true; prefs.edit().putLong("checked",now).apply();
		WeakReference<MainActivity> weak=new WeakReference<>(activity);
		SiteData.IO.execute(()->{
			AndroidUpdate result=null;
			try { result=UpdateTransport.check(); } catch(Exception ignored) { /* Automatic checks stay quiet offline. */ }
			final AndroidUpdate update=result;
			new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{
				synchronized(Updates.class) { checking=false; }
				MainActivity owner=weak.get();
				if(owner==null || owner.isFinishing() || owner.isDestroyed() || playing() || !owner.hasWindowFocus()
					|| owner.findViewById(R.id.home_panel).getVisibility()!=android.view.View.VISIBLE
					|| !owner.getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)
					|| update==null || !update.newerThan(BuildConfig.VERSION_CODE,Build.VERSION.SDK_INT)) { return; }
				new AlertDialog.Builder(owner,R.style.NativeDialog).setTitle("Dostępna aktualizacja")
					.setMessage("Wersja "+update.versionName+"\n\n"+update.notes)
					.setPositiveButton("Zobacz",(dialog,which)->open(owner)).setNegativeButton("Później",null).show();
			});
		});
	}
}
