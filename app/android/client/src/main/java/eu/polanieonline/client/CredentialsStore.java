/***************************************************************************
 *                    Copyright © 2025 - PolanieOnLine                     *
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

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

/**
 * Helper for storing login credentials using encrypted shared preferences.
 */
public final class CredentialsStore {

	/** Encrypted preferences file name. */
	private static final String PREF_FILE = "eu.polanieonline.client.credentials";
	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";

	private CredentialsStore() {
	}

	/**
	 * Saves provided credentials in encrypted shared preferences.
	 *
	 * @param context
	 *     Context for accessing preferences.
	 * @param username
	 *     Username to store.
	 * @param password
	 *     Password to store.
	 */
	public static void save(final Context context, final String username, final String password) {
		final SharedPreferences prefs = getPreferences(context);
		if (prefs == null) {
			Logger.warn("Encrypted preferences unavailable; skipping credential save.");
			return;
		}
		prefs.edit().putString(KEY_USERNAME, username).putString(KEY_PASSWORD, password).apply();
	}

	/**
	 * Removes any stored credentials.
	 *
	 * @param context
	 *     Context for accessing preferences.
	 */
	public static void clear(final Context context) {
		final SharedPreferences prefs = getPreferences(context);
		if (prefs == null) {
			Logger.warn("Encrypted preferences unavailable; skipping credential clear.");
			return;
		}
		prefs.edit().remove(KEY_USERNAME).remove(KEY_PASSWORD).apply();
	}

	/**
	 * Loads saved credentials if available.
	 *
	 * @param context
	 *     Context for accessing preferences.
	 * @return
	 *     Stored credentials or <code>null</code> if not available.
	 */
	@Nullable
	public static Credentials load(final Context context) {
		final SharedPreferences prefs = getPreferences(context);
		if (prefs == null) {
			Logger.warn("Encrypted preferences unavailable; skipping credential load.");
			return null;
		}
		final String username = prefs.getString(KEY_USERNAME, "");
		final String password = prefs.getString(KEY_PASSWORD, "");
		if (username == null || password == null || username.trim().equals("") || password.equals("")) {
			return null;
		}
		return new Credentials(username, password);
	}

	/**
	 * Retrieves encrypted shared preferences instance.
	 *
	 * @param context
	 *     Context for accessing preferences.
	 * @return
	 *     Encrypted shared preferences or <code>null</code> if unavailable.
	 */
	@Nullable
	private static SharedPreferences getPreferences(final Context context) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				Logger.warn("EncryptedSharedPreferences requires Android M or above.");
				return null;
			}
			final MasterKey masterKey = new MasterKey.Builder(context)
				.setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
				.build();
			return EncryptedSharedPreferences.create(
				context,
				PREF_FILE,
				masterKey,
				EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
				EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
			);
		} catch (final Exception e) {
			Logger.error("Unable to initialize encrypted preferences: " + e.getMessage());
			return null;
		}
	}

	/**
	 * Represents a set of saved credentials.
	 */
	public static final class Credentials {
		private final String username;
		private final String password;

		Credentials(final String username, final String password) {
			this.username = username;
			this.password = password;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}
	}
}
