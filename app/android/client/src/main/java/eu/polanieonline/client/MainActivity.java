/***************************************************************************
 *                 Copyright © 2022-2024 - Faiumoni e. V.                  *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package eu.polanieonline.client;

import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Main app activity.
 */
public class MainActivity extends AppCompatActivity {

	/** Class logger. */
	private static final Logger LOG = LogManager.getLogger(MainActivity.class);

	/** Menu instance. */
	private Menu menu;
	/** Active clients. **/
	private ViewGroup clientList;
	private HomePanel home;
	private GameLoadingPanel loading;
	private boolean recoveryPending;

	/** Static activity instance. */
	private static MainActivity instance;


	/**
	 * Retrieves activity instance.
	 *
	 * NOTE: This may not be necessary if Android has built-in methods to retrieve current activity.
	 */
	public static MainActivity get() {
		return MainActivity.instance;
	}

	/**
	 * Called when main activity is created.
	 */
	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			// FIXME: may be considered unsafe as this is not technically a singleton
			MainActivity.instance = this;
			Menu.reset();
			SplashUtil.reset();

			LogConfigurator.configure(this);

			androidx.core.view.WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
			setContentView(R.layout.activity_main);
			NativeUi.darkBars(this);
			android.view.View content = findViewById(R.id.content);
			content.setBackgroundColor(NativeUi.INK);
			content.post(() -> NativeUi.darkBars(this));
			ViewCompat.setOnApplyWindowInsetsListener(content, (view, insets) -> {
				Insets safe = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout()
						| WindowInsetsCompat.Type.ime());
				view.setPadding(safe.left, safe.top, safe.right, safe.bottom);
				return insets;
			});
			clientList = findViewById(R.id.clientList);
			home = new HomePanel(this);
			loading = new GameLoadingPanel(this,findViewById(R.id.game_loading_panel));
			findViewById(R.id.reconnect_button).setOnClickListener(v -> {
				new AlertDialog.Builder(this).setMessage("Połączyć się ponownie? Bieżąca sesja zostanie zakończona.")
					.setNegativeButton("Anuluj", null).setPositiveButton("Połącz", (d, w) -> loadLogin()).show();
			});
			createClientView();
			// NOTE: client view instance must be created before initializing menu
			menu = Menu.get();
			getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
				@Override public void handleOnBackPressed() {
					if (loading.visible()) { loading.cancel(); return; }
					if (getActiveClientView()==null || getActiveClientView().onTitleScreen()) { onRequestQuit(); }
					else { menu.toggleVisibility(); }
				}
			});
			updateOrientation();
		} catch (final Exception e) {
			// TODO: add option to save to file or copy to clipboard the error
			LOG.error("Activity initialization failed: {}",e.getClass().getSimpleName());
			LOG.error("// -- //");
			final StringBuilder sb = new StringBuilder();
			for (final StackTraceElement ste: e.getStackTrace()) {
				final String traceLine = ste.toString();
				if (sb.length() > 0) {
					sb.append("\n");
				}
				sb.append(traceLine);
				LOG.error(traceLine);
			}
			LOG.error("// -- //");
			Notifier.showPrompt(
				"Nie udało się uruchomić aplikacji. Typ błędu: " + e.getClass().getSimpleName()
				+ "\n\nMożesz zgłosić ten błąd tutaj: https://github.com/PolanieOnLine/PolanieOnLine/issues"
				+ "\n\nŚlad stosu:\n" + sb.toString(),
				new Notifier.Action() {
					@Override
					protected void onCall() {
						finish();
					}
				});
		}
	}

	/**
	 * Creates view to host a client instance.
	 */
	private void createClientView() {
		final ClientView clientView = new ClientView(this);
		setActiveClientView(clientView);
		clientList.addView(clientView);
		clientView.loadTitleScreen();
		// show splash when a new view is created
		SplashUtil.get().setVisible(true);
	}

	/**
	 * Sets client view to be shown and hides all others.
	 *
	 * @param clientView
	 *   The `ClientView` instance to be visible.
	 */
	private void setActiveClientView(final ClientView clientView) {
		for (int idx = 0; idx < clientList.getChildCount(); idx++) {
			((ClientView) clientList.getChildAt(idx)).setActive(false);
		}
		clientView.setActive(true);
	}

	/**
	 * Sets client view to be shown and hides all others.
	 *
	 * @param clientIndex
	 *   The index of `ClientView` instance to be visible.
	 */
	private void setActiveClientView(final int clientIndex) {
		if (clientIndex < 0 || clientIndex >= clientList.getChildCount()) {
			LOG.error("Tried to access invalid client index: {}", clientIndex);
			LogConfigurator.notifyUser(Level.ERROR, "Tried to access invalid client index: " + clientIndex);
			return;
		}
		setActiveClientView((ClientView) clientList.getChildAt(clientIndex));
	}

	/**
	 * Retrieves active client view.
	 *
	 * @return
	 *   `ClientView` instance that is visible.
	 */
	public ClientView getActiveClientView() {
		for (int idx = 0; idx < clientList.getChildCount(); idx++) {
			final ClientView clientView = (ClientView) clientList.getChildAt(idx);
			if (clientView.isActive()) {
				return clientView;
			}
		}
		// default to first client view
		return clientList.getChildCount()==0 ? null : (ClientView) clientList.getChildAt(0);
	}

	/**
	 * Retrieves list of available client views.
	 *
	 * @return
	 *   List containing all created `ClientView` instances.
	 */
	public List<ClientView> getClientViewList() {
		final List<ClientView> available = new LinkedList<>();
		for (int idx = 0; idx < clientList.getChildCount(); idx++) {
			available.add((ClientView) clientList.getChildAt(idx));
		}
		return available;
	}

	/**
	 * Attempts to connect to client host.
	 */
	public void loadLogin() {
		if(getActiveClientView()!=null) { getActiveClientView().loadLogin(); }
	}

	void showLoading(ClientView client) {
		if(loading!=null && client==getActiveClientView()) { loading.begin(client,client::cancelLoginFlow); }
	}
	boolean isLoading(ClientView client) { return loading!=null && loading.owns(client); }
	void hideLoading(ClientView client) { if(loading!=null) { loading.finish(client); } }

	/** Only dispose the reported WebView. Other callbacks may follow for a shared renderer. */
	void recoverRenderer(ClientView failed) {
		hideLoading(failed);
		if(clientList==null) { failed.destroy(); return; }
		clientList.removeView(failed); failed.destroy();
		if(isFinishing() || isDestroyed() || recoveryPending) { return; }
		recoveryPending=true;
		new android.os.Handler(android.os.Looper.getMainLooper()).post(()->{
			recoveryPending=false; if(isFinishing() || isDestroyed()) { return; }
			if(clientList.getChildCount()==0) { createClientView(); }
			else {
				if(getActiveClientView()==null || !getActiveClientView().isActive()) { setActiveClientView(0); }
				SplashUtil.get().setVisible(getActiveClientView().onTitleScreen());
			}
			Menu.get().updateButtons();
			new AlertDialog.Builder(this).setTitle("Widok gry został zamknięty")
				.setMessage(getActiveClientView().onTitleScreen()
					? "Wrócono do menu. Możesz zalogować się ponownie. Zapisane konta pozostały na telefonie."
					: "Zamknięto uszkodzony widok. Pozostałe widoki i zapisane konta nie zostały usunięte.")
				.setPositiveButton("OK",null).show();
		});
	}

	void showHome(boolean visible) {
		if (home != null) { home.show(visible); }
		if (visible) { showReconnect(false, ""); }
	}

	void showReconnect(boolean visible, String reason) {
		findViewById(R.id.reconnect_panel).setVisibility(visible ? android.view.View.VISIBLE : android.view.View.GONE);
		((android.widget.TextView)findViewById(R.id.reconnect_text)).setText(reason);
	}

	void showTools() {
		new AlertDialog.Builder(this).setTitle("PolanieOnLine").setItems(
			new String[]{"Zapisane konta", "Kalendarz", "Zgłoś problem", "Pomoc na stronie"},
			(d, which) -> {
				if (which == 0) { SavedAccounts.show(this); }
				else if (which == 1) { startActivity(new Intent(this, CalendarActivity.class)); }
				else if (which == 2) { Diagnostics.show(this); }
				else { NativeUi.openSite(this, "/faq"); }
			}).setNegativeButton("Zamknij", null).show();
	}

	@Override protected void onResume() {
		super.onResume();
		if (clientList != null && clientList.getChildCount() > 0) { getActiveClientView().resumeConnectionChecks(); }
		if (home != null && getActiveClientView()!=null && getActiveClientView().onTitleScreen()) { home.show(true); }
		if(loading!=null) { loading.refresh(); }
	}

	@Override protected void onPause() {
		if (clientList != null) { for (ClientView client : getClientViewList()) { client.pauseConnectionChecks(); } }
		super.onPause();
	}

	/**
	 * Retrieves app orientation.
	 *
	 * @return
	 *   One of `Configuration.ORIENTATION_PORTRAIT` (1), `Configuration.ORIENTATION_LANDSCAPE` (2),
	 *   or `Configuration.ORIENTATION_UNDEFINED` (0).
	 */
	public int getOrientation() {
		return getResources().getConfiguration().orientation;
	}

	/**
	 * Sets screen orientation to user setting or locks in landscape or portrait.
	 */
	public void updateOrientation() {
		final String value = PreferencesActivity.getString("orientation");
		int orient = ActivityInfo.SCREEN_ORIENTATION_USER;
		switch (value) {
			case "landscape":
				orient = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
				break;
			case "portrait":
				orient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
				break;
		}
		// set orientation for main & sub activities
		setRequestedOrientation(orient);
		final PreferencesActivity preferencesActivity = PreferencesActivity.get();
		if (preferencesActivity != null) {
			preferencesActivity.setRequestedOrientation(orient);
		}
	}

	/**
	 * Opens a dialog to confirm exiting activity.
	 */
	public void onRequestQuit() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Wyjdź");

		builder.setPositiveButton("Tak", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				finish();
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		});

		builder.setNegativeButton("Nie", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int id) {
				dialog.cancel();
			}
		});

		final AlertDialog confirmQuit = builder.create();
		confirmQuit.show();
	}

	//~ @Override
	//~ protected void onResume() {
		//~ super.onResume();
	//~ }

	/**
	 * Listens for changes to orientation.
	 */
	@Override
	public void onConfigurationChanged(final Configuration config) {
		super.onConfigurationChanged(config);
		final ClientView clientView = getActiveClientView();
		if(loading!=null) { loading.refresh(); }
		if (clientView != null && PageId.TITLE.equals(clientView.getCurrentPageId())) {
			SplashUtil.get().update();
			if (home != null) { home.render(); }
		}
	}

	/**
	 * Called when main activity ends.
	 */
	@Override
	public void finish() {
		LOG.debug("{}.finish() called", MainActivity.class.getName());
		super.finish();
	}

	/**
	 * Called when main activity is destroyed.
	 */
	@Override
	protected void onDestroy() {
		LOG.debug("{}.onDestroy() called", MainActivity.class.getName());
		if (home != null) { home.dispose(); }
		if (loading != null) { loading.dispose(); }
		if (clientList != null) {
			List<ClientView> clients = getClientViewList();
			clientList.removeAllViews();
			for (ClientView client : clients) { client.disposeConnectionChecks(); client.destroy(); }
		}
		if (instance == this) { instance = null; Menu.reset(); SplashUtil.reset(); }
		MusicPlayer.stopMusic();
		super.onDestroy();
	}

	/**
	 * Creates preferences activity.
	 */
	public void showSettings() {
		startActivity(new Intent(this, PreferencesActivity.class));
	}
}
