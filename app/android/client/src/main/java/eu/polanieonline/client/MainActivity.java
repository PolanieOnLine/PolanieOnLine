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
import androidx.appcompat.app.AppCompatActivity;

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

			LogConfigurator.configure(this);

			setContentView(R.layout.activity_main);
			clientList = findViewById(R.id.clientList);
			createClientView();
			// NOTE: client view instance must be created before initializing menu
			menu = Menu.get();
			updateOrientation();
		} catch (final Exception e) {
			// TODO: add option to save to file or copy to clipboard the error
			e.printStackTrace();
			LOG.error(e.toString());
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
				"Wystąpił nieobsłużony wyjątek: \"" + e.getMessage() + "\""
				+ "\n\nMożesz zgłosić ten błąd tutaj: https://s1.polanieonline.eu/development/bug.html"
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
	 * Handles toggling menu when the system "back" button is pressed.
	 */
	@Override
	public void onBackPressed() {
		menu.toggleVisibility();
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
		return (ClientView) clientList.getChildAt(0);
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
		getActiveClientView().loadLogin();
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
		if (clientView != null && PageId.TITLE.equals(clientView.getCurrentPageId())) {
			SplashUtil.get().update();
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
		super.onDestroy();
	}

	/**
	 * Creates preferences activity.
	 */
	public void showSettings() {
		startActivity(new Intent(this, PreferencesActivity.class));
	}
}
