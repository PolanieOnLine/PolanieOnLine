/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.*;
import android.content.*;
import android.content.pm.PackageInfo;
import android.net.*;
import android.os.Build;
import android.text.InputFilter;
import android.webkit.WebView;
import android.widget.*;

/** Deliberately excludes logs, URLs, account names, cookies and game/chat contents. */
final class Diagnostics {
	private Diagnostics() { }
	static String report(android.content.Context context) {
		String web="niedostępna";
		if(Build.VERSION.SDK_INT>=26) { PackageInfo pkg=WebView.getCurrentWebViewPackage(); if(pkg!=null) { web=pkg.packageName+" "+pkg.versionName; } }
		String network="brak";
		ConnectivityManager cm=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(cm!=null) {
			if(Build.VERSION.SDK_INT>=23) {
				NetworkCapabilities caps=cm.getNetworkCapabilities(cm.getActiveNetwork());
				if(caps!=null) { network=caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ? "Wi-Fi"
					: caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ? "sieć komórkowa" : "inna sieć"; }
			} else {
				android.net.NetworkInfo info=cm.getActiveNetworkInfo(); if(info!=null && info.isConnected()) { network=info.getType()==ConnectivityManager.TYPE_WIFI ? "Wi-Fi" : "inna sieć"; }
			}
		}
		MainActivity main=MainActivity.get();
		String state=main==null ? "nie uruchomiono gry" : main.getActiveClientView().connectionDescription();
		return "PolanieOnLine Android "+BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")"
			+"\nAndroid: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")"
			+"\nTelefon: "+Build.MANUFACTURER+" "+Build.MODEL
			+"\nWebView: "+web+"\nSieć: "+network+"\nPołączenie: "+state;
	}
	static void show(Activity activity) {
		LinearLayout content=NativeUi.column(activity); int pad=NativeUi.dp(activity,20); content.setPadding(pad,pad,pad,pad);
		String report=report(activity);
		content.addView(NativeUi.text(activity,"Opisz, co się wydarzyło. Nie wpisuj hasła. Raport nie zawiera rozmów ani danych konta.",14,NativeUi.MUTED));
		EditText description=new EditText(activity); description.setHint("Opis problemu"); description.setMinLines(3); description.setMaxLines(6);
		description.setTextColor(NativeUi.TEXT); description.setHintTextColor(NativeUi.MUTED);
		description.setFilters(new InputFilter[]{new InputFilter.LengthFilter(2000)}); content.addView(description);
		TextView preview=NativeUi.text(activity,report,13,NativeUi.MUTED); preview.setTextIsSelectable(true); content.addView(preview);
		ScrollView scroll=new ScrollView(activity); scroll.addView(content);
		AlertDialog dialog=new AlertDialog.Builder(activity).setTitle("Zgłoś problem").setView(scroll)
			.setNegativeButton("Anuluj",null).setNeutralButton("Skopiuj",null).setPositiveButton("Przygotuj e-mail",null).create();
		dialog.setOnShowListener(d->{
			dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(v->{
				ClipboardManager clipboard=(ClipboardManager)activity.getSystemService(Context.CLIPBOARD_SERVICE);
				if(clipboard!=null) { clipboard.setPrimaryClip(ClipData.newPlainText("Raport PolanieOnLine",description.getText()+"\n\n"+report)); Toast.makeText(activity,"Skopiowano raport.",Toast.LENGTH_SHORT).show(); }
			});
			dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
				String body=description.getText()+"\n\n"+report;
				// Entire draft is visible here and can still be edited in the chosen email app.
				Uri uri=Uri.parse("mailto:support@polanieonline.eu").buildUpon()
					.appendQueryParameter("subject","Problem w aplikacji Android "+BuildConfig.VERSION_NAME)
					.appendQueryParameter("body",body).build();
				try { activity.startActivity(new Intent(Intent.ACTION_SENDTO,uri)); }
				catch(ActivityNotFoundException e) { Toast.makeText(activity,"Nie znaleziono aplikacji poczty. Skopiuj raport i wyślij na support@polanieonline.eu.",Toast.LENGTH_LONG).show(); }
			});
		});
		dialog.show();
	}
}
