/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

/** Installation is always delegated to Android and requires the player's confirmation. */
public final class UpdateActivity extends AppCompatActivity {
	private UpdateModel model;
	private LinearLayout content;
	@Override protected void onCreate(Bundle saved) {
		super.onCreate(saved);
		content=NativeUi.column(this); NativeUi.screen(this,content,true);
		if(!Updates.direct(this)) {
			content.addView(NativeUi.woodButton(this,"Wróć",this::finish));
			content.addView(NativeUi.text(this,"Aktualizacje tej wersji są dostępne w sklepie Google Play.",18,NativeUi.TEXT));
			content.addView(NativeUi.woodButton(this,"Otwórz Google Play",()->{
				try { startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=eu.polanieonline.client"))); }
				catch(android.content.ActivityNotFoundException e) { Toast.makeText(this,"Nie znaleziono przeglądarki.",Toast.LENGTH_LONG).show(); }
			}));
			return;
		}
		model=new ViewModelProvider(this).get(UpdateModel.class);
		model.state.observe(this,this::render);
		if(Updates.playing()) { finish(); return; }
		model.start();
	}
	private void render(UpdateModel.State state) {
		content.removeAllViews();
		content.addView(NativeUi.woodButton(this,"Wróć",this::finish));
		LinearLayout card=NativeUi.card(this); content.addView(card);
		card.addView(NativeUi.text(this,"AKTUALIZACJE APLIKACJI",18,NativeUi.TEXT));
		card.addView(NativeUi.text(this,"Zainstalowana wersja: "+BuildConfig.VERSION_NAME+" ("+BuildConfig.VERSION_CODE+")",13,NativeUi.MUTED));
		card.addView(NativeUi.text(this,state.message,16,NativeUi.TEAL));
		if(state.update!=null) {
			card.addView(NativeUi.text(this,state.update.notes,14,NativeUi.TEXT));
			card.addView(NativeUi.text(this,String.format(java.util.Locale.ROOT,"Rozmiar: %.1f MB",state.update.size/1048576.0),12,NativeUi.MUTED));
		}
		if(state.busy && state.update!=null) {
			ProgressBar bar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);
			bar.setMax(100); bar.setProgress(state.progress); card.addView(bar);
			card.addView(NativeUi.text(this,state.progress+"%",13,NativeUi.MUTED));
			card.addView(NativeUi.woodButton(this,"Anuluj",model::cancel));
		} else if(state.ready) {
			card.addView(NativeUi.woodButton(this,"Zainstaluj",()->install(state.update)));
		} else if(!state.busy && state.update!=null) {
			card.addView(NativeUi.woodButton(this,"Pobierz aktualizację",()->{
				if(menuOnly()) { model.download(state.update); }
			}));
		}
		if(!state.busy) { card.addView(NativeUi.link(this,"Sprawdź ponownie",model::check)); }
	}
	private boolean menuOnly() {
		if(!Updates.playing()) { return true; }
		Toast.makeText(this,"Wróć do menu gry przed aktualizacją.",Toast.LENGTH_LONG).show(); return false;
	}
	private void install(AndroidUpdate update) {
		if(!menuOnly()) { return; }
		if(Build.VERSION.SDK_INT>=26 && !getPackageManager().canRequestPackageInstalls()) {
			new androidx.appcompat.app.AlertDialog.Builder(this,R.style.NativeDialog).setTitle("Zezwól na aktualizację")
				.setMessage("W ustawieniach Androida zezwól tej aplikacji na instalowanie aktualizacji. Następnie wróć tutaj i wybierz Zainstaluj.")
				.setPositiveButton("Otwórz ustawienia",(d,w)->{
					try { startActivity(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,Uri.parse("package:"+getPackageName()))); }
					catch(android.content.ActivityNotFoundException e) { Toast.makeText(this,"Otwórz uprawnienie instalacji w ustawieniach Androida.",Toast.LENGTH_LONG).show(); }
				}).setNegativeButton("Anuluj",null).show(); return;
		}
		model.verifyForInstall(update,()->{
			if(isFinishing() || isDestroyed() || !getLifecycle().getCurrentState().isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED) || !menuOnly()) { return; }
			try {
				Uri uri=FileProvider.getUriForFile(this,getPackageName()+".updates",model.apk());
				Intent intent=new Intent(Intent.ACTION_VIEW).setDataAndType(uri,"application/vnd.android.package-archive")
					.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
				intent.setClipData(android.content.ClipData.newRawUri("Aktualizacja",uri)); startActivity(intent);
			} catch(android.content.ActivityNotFoundException | IllegalArgumentException e) { Toast.makeText(this,"Nie można uruchomić instalatora Androida.",Toast.LENGTH_LONG).show(); }
		});
	}
	@Override protected void onDestroy() { if(isFinishing() && model!=null) { model.cancel(); } super.onDestroy(); }
}
