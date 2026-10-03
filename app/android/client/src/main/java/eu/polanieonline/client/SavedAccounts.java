/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import java.util.List;

/** Passwords remain in encrypted storage and are never displayed by this manager. */
final class SavedAccounts {
	private SavedAccounts() { }
	static void show(MainActivity activity) {
		LinearLayout content=NativeUi.column(activity);
		Dialog dialog=panel(activity,"Zapisane konta",content);
		if(!CredentialsStore.isAvailable(activity)) {
			if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {
				content.addView(NativeUi.text(activity,"Zapamiętywanie kont wymaga Androida 6 lub nowszego.",15,NativeUi.MUTED));
			} else {
				content.addView(NativeUi.text(activity,"Nie można odczytać zaszyfrowanych danych logowania na tym telefonie.",15,NativeUi.MUTED));
			}
			if(android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
				content.addView(NativeUi.woodButton(activity,"Napraw zapisywanie kont",()->confirm(activity,"Napraw zapisywanie kont",
					"Usunąć nieczytelny zapis danych logowania z tego telefonu? Hasła trzeba będzie wpisać ponownie. Konta w grze i pozostałe ustawienia pozostaną bez zmian.","Napraw",()->{
						if(CredentialsStore.resetUnreadableStorage(activity)) { dialog.dismiss(); show(activity); }
						else { Toast.makeText(activity,"Nie udało się naprawić zapisywania kont. Skontaktuj się z support@polanieonline.eu.",Toast.LENGTH_LONG).show(); }
					})));
			}
			dialog.show(); return;
		}
		List<CredentialsStore.Credentials> accounts=CredentialsStore.loadAll(activity);
		if(accounts.isEmpty()) { content.addView(NativeUi.text(activity,"Nie masz zapisanych kont. Możesz zapisać dane podczas logowania do gry.",15,NativeUi.MUTED)); }
		for(CredentialsStore.Credentials account:accounts) {
			content.addView(accountRow(activity,account.getUsername(),()->{
				dialog.dismiss();
				Runnable login=()->activity.getActiveClientView().showLoginDialogWithCredentials(account,false);
				if(activity.getActiveClientView().onTitleScreen()) { login.run(); }
				else { confirm(activity,"Zmiana konta","Zakończyć obecną sesję i zalogować na wybrane konto?","Zaloguj",login); }
			},()->confirm(activity,"Usuń zapisane konto",
				"Usunąć zapisane dane konta "+account.getUsername()+" z tego telefonu? Konto w grze pozostanie bez zmian.","Usuń",()->{
					if(CredentialsStore.remove(activity,account.getUsername())) { dialog.dismiss(); show(activity); }
					else { Toast.makeText(activity,"Nie udało się usunąć zapisanych danych.",Toast.LENGTH_LONG).show(); }
				})));
		}
		dialog.show();
	}
	/** A single wooden panel, rather than an ornament card inside a system alert. */
	static Dialog panel(Activity activity,String title,LinearLayout content) {
		Dialog dialog=new Dialog(activity,R.style.NativeDialog);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		LinearLayout panel=new LinearLayout(activity) {
			@Override protected void onMeasure(int width,int height) {
				int available=activity.getWindow().getDecorView().getHeight();
				if(available<=0) { available=getResources().getDisplayMetrics().heightPixels; }
				androidx.core.view.WindowInsetsCompat insets=androidx.core.view.ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
				if(insets!=null) {
					androidx.core.graphics.Insets safe=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()
						| androidx.core.view.WindowInsetsCompat.Type.displayCutout());
					available-=safe.top+safe.bottom;
				}
				int limit=Math.max(1,available-NativeUi.dp(activity,24));
				if(MeasureSpec.getMode(height)!=MeasureSpec.UNSPECIFIED) { limit=Math.min(limit,MeasureSpec.getSize(height)); }
				super.onMeasure(width,MeasureSpec.makeMeasureSpec(limit,MeasureSpec.AT_MOST));
			}
		};
		panel.setOrientation(LinearLayout.VERTICAL); panel.setBackground(new NativeUi.Ornament(activity,false,true));
		panel.setPadding(NativeUi.dp(activity,20),NativeUi.dp(activity,14),NativeUi.dp(activity,20),NativeUi.dp(activity,24));
		LinearLayout header=new LinearLayout(activity); header.setGravity(Gravity.CENTER_VERTICAL); header.setBaselineAligned(false);
		TextView heading=NativeUi.text(activity,title,18,NativeUi.TEXT);
		header.addView(heading,new LinearLayout.LayoutParams(0,-2,1));
		Button close=NativeUi.link(activity,"Zamknij",dialog::dismiss);
		close.setGravity(Gravity.END|Gravity.CENTER_VERTICAL); close.setPadding(NativeUi.dp(activity,12),0,0,0);
		header.addView(close,new LinearLayout.LayoutParams(-2,-2)); panel.addView(header);
		ScrollView scroll=new ScrollView(activity); scroll.setFillViewport(false); scroll.setClipToPadding(false);
		scroll.addView(content); panel.addView(scroll,new LinearLayout.LayoutParams(-1,-2,1));
		dialog.setContentView(panel); dialog.setCanceledOnTouchOutside(true);
		Window initialWindow=dialog.getWindow();
		if(initialWindow!=null) {
			initialWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); initialWindow.getDecorView().setPadding(0,0,0,0);
		}
		dialog.setOnShowListener(d->{
			Window window=dialog.getWindow(); if(window==null) { return; }
			int available=activity.getWindow().getDecorView().getWidth();
			if(available<=0) { available=activity.getResources().getDisplayMetrics().widthPixels; }
			androidx.core.view.WindowInsetsCompat insets=androidx.core.view.ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
			if(insets!=null) { androidx.core.graphics.Insets safe=insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.systemBars()
				| androidx.core.view.WindowInsetsCompat.Type.displayCutout()); available-=safe.left+safe.right; }
			window.setLayout(Math.max(1,Math.min(NativeUi.dp(activity,460),available-NativeUi.dp(activity,24))),WindowManager.LayoutParams.WRAP_CONTENT);
		});
		return dialog;
	}
	static LinearLayout accountRow(Activity activity,String username,Runnable login,Runnable remove) {
		LinearLayout row=NativeUi.column(activity);
		GradientDrawable background=new GradientDrawable(); background.setColor(0xee0c151d);
		background.setCornerRadius(NativeUi.dp(activity,4)); background.setStroke(NativeUi.dp(activity,1),0x66b59660);
		row.setBackground(background); int pad=NativeUi.dp(activity,12); row.setPadding(pad,pad,pad,pad);
		LinearLayout.LayoutParams margin=new LinearLayout.LayoutParams(-1,-2); margin.topMargin=NativeUi.dp(activity,8); row.setLayoutParams(margin);
		TextView name=NativeUi.text(activity,username,16,NativeUi.TEXT); name.setMaxLines(2); name.setEllipsize(android.text.TextUtils.TruncateAt.END); row.addView(name);
		Button loginButton=NativeUi.woodButton(activity,"Zaloguj",login);
		Button delete=NativeUi.link(activity,"Usuń z telefonu",remove); delete.setGravity(Gravity.CENTER);
		delete.setMaxLines(2); delete.setIncludeFontPadding(false); delete.setEllipsize(android.text.TextUtils.TruncateAt.END);
		delete.setPadding(NativeUi.dp(activity,8),NativeUi.dp(activity,6),NativeUi.dp(activity,8),NativeUi.dp(activity,6));
		delete.setTextColor(NativeUi.MUTED); delete.getLayoutParams().height=loginButton.getLayoutParams().height;
		row.addView(NativeUi.row(activity,loginButton,delete)); return row;
	}
	private static void confirm(Activity activity,String title,String message,String action,Runnable accepted) {
		LinearLayout content=NativeUi.column(activity); Dialog dialog=panel(activity,title,content);
		content.addView(NativeUi.text(activity,message,15,NativeUi.MUTED));
		content.addView(NativeUi.woodButton(activity,action,()->{ dialog.dismiss(); accepted.run(); })); dialog.show();
	}
}
