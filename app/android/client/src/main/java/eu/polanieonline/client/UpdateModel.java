/* Copyright 2026 PolanieOnLine. SPDX-License-Identifier: GPL-2.0-or-later */
package eu.polanieonline.client;

import android.app.Application;
import android.os.Build;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import java.io.*;
import java.net.HttpURLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Retains download state through rotation. Only verified APKs enter the shared directory. */
public final class UpdateModel extends AndroidViewModel {
	static final class State {
		final String message;
		final AndroidUpdate update;
		final int progress;
		final boolean busy,ready;
		State(String message,AndroidUpdate update,int progress,boolean busy,boolean ready) {
			this.message=message; this.update=update; this.progress=progress; this.busy=busy; this.ready=ready;
		}
	}
	final MutableLiveData<State> state=new MutableLiveData<>();
	private final ExecutorService io=Executors.newSingleThreadExecutor();
	private volatile boolean cancelled;
	private volatile HttpURLConnection connection;
	private boolean working,started;
	public UpdateModel(Application app) { super(app); }
	File apk() { return new File(getApplication().getCacheDir(),"android-updater/verified/update.apk"); }
	private File partial() { return new File(getApplication().getCacheDir(),"android-updater/unverified/partial.apk"); }
	void check() {
		if(working) { return; }
		started=true; working=true; cancelled=false;
		state.setValue(new State("Sprawdzanie aktualizacji",null,0,true,false));
		io.execute(()->{
			try {
				remove(partial()); remove(apk());
				AndroidUpdate update=UpdateTransport.check();
				String message=update==null ? "Nie opublikowano jeszcze aktualizacji dla tej aplikacji."
					: update.versionCode<=BuildConfig.VERSION_CODE ? "Masz aktualną wersję aplikacji."
					: update.minSdk>Build.VERSION.SDK_INT ? "Ta aktualizacja wymaga nowszej wersji Androida."
					: "Dostępna wersja "+update.versionName;
				publish(new State(message,update!=null && update.newerThan(BuildConfig.VERSION_CODE,Build.VERSION.SDK_INT) ? update : null,0,false,false));
			} catch(Exception e) { publish(new State("Nie udało się sprawdzić aktualizacji. Sprawdź połączenie i spróbuj ponownie.",null,0,false,false)); }
		});
	}
	void start() { if(!started) { check(); } }
	void download(AndroidUpdate update) {
		if(working || update==null || !Updates.direct(getApplication())) { return; }
		working=true; cancelled=false;
		state.setValue(new State("Pobieranie aktualizacji",update,0,true,false));
		io.execute(()->{
			try {
				remove(apk()); remove(partial());
				if(!partial().getParentFile().isDirectory() && !partial().getParentFile().mkdirs()) { throw new IOException("cache"); }
				connection=UpdateTransport.open(update.url);
				if(connection.getResponseCode()!=200) { throw new IOException("status"); }
				long declared=connection.getContentLength();
				if(declared>=0 && declared!=update.size) { throw new IOException("size"); }
				final int[] last={-1};
				try(InputStream input=connection.getInputStream(); OutputStream output=new FileOutputStream(partial())) {
					AndroidUpdate.copyVerified(input,output,update,()->cancelled,count->{
						int percent=(int)(count*100/update.size);
						if(percent!=last[0]) { last[0]=percent; state.postValue(new State("Pobieranie aktualizacji",update,percent,true,false)); }
					});
				}
				UpdateVerifier.verify(getApplication(),partial(),update);
				if(cancelled) { throw new InterruptedIOException(); }
				if(!apk().getParentFile().isDirectory() && !apk().getParentFile().mkdirs()) { throw new IOException("cache"); }
				if(!partial().renameTo(apk())) { throw new IOException("move"); }
				publish(new State("Aktualizacja gotowa. Instalację potwierdzisz w oknie Androida.",update,100,false,true));
			} catch(Exception e) {
				clearAfterFailure();
				publish(new State(cancelled ? "Pobieranie anulowane." : "Nie udało się pobrać lub zweryfikować aktualizacji. Spróbuj ponownie.",update,0,false,false));
			} finally { HttpURLConnection c=connection; connection=null; if(c!=null) { c.disconnect(); } }
		});
	}
	void verifyForInstall(AndroidUpdate update,Runnable success) {
		if(working) { return; }
		working=true; cancelled=false;
		state.setValue(new State("Sprawdzanie pobranego pliku",update,100,true,false));
		io.execute(()->{
			try(InputStream input=new FileInputStream(apk())) {
				AndroidUpdate.copyVerified(input,new OutputStream(){ @Override public void write(int b) { }
					@Override public void write(byte[] b,int off,int len) { } },update,()->cancelled,count->{});
				UpdateVerifier.verify(getApplication(),apk(),update);
				new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{
					working=false; if(cancelled) {
						clearAfterFailure(); state.setValue(new State("Anulowano instalację.",update,0,false,false)); return;
					}
					state.setValue(new State("Aktualizacja gotowa do instalacji.",update,100,false,true)); success.run();
				});
			} catch(Exception e) { clearAfterFailure(); publish(new State("Pobrany plik nie przeszedł weryfikacji. Pobierz aktualizację ponownie.",update,0,false,false)); }
		});
	}
	void cancel() { cancelled=true; HttpURLConnection c=connection; if(c!=null) { c.disconnect(); } }
	private void publish(State value) {
		new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{
			working=false;
			if(cancelled && value.ready) {
				clearAfterFailure(); state.setValue(new State("Pobieranie anulowane.",value.update,0,false,false));
			} else { state.setValue(value); }
		});
	}
	private static void remove(File file) { if(file.exists() && !file.delete()) { throw new IllegalStateException("Cannot clear updater cache"); } }
	private void clearAfterFailure() {
		try { remove(partial()); } catch(IllegalStateException ignored) { /* Never shared with installer. */ }
		try { remove(apk()); } catch(IllegalStateException ignored) { /* State still blocks installation. */ }
	}
	@Override protected void onCleared() { cancel(); io.shutdownNow(); }
}
