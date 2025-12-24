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
import android.text.TextUtils;
import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Helper for storing login credentials using encrypted shared preferences.
 */
public final class CredentialsStore {

	/** Encrypted preferences file name. */
	private static final String PREF_FILE = "eu.polanieonline.client.credentials";
	private static final String KEY_CREDENTIALS = "credentials";
	private static final String KEY_USERNAME_LEGACY = "username";
	private static final String KEY_PASSWORD_LEGACY = "password";
	private static final int MAX_ENTRIES = 5;

	private CredentialsStore() {
	}

	/**
	 * Saves provided credentials in encrypted shared preferences.
	 *
	 * @param context  Context for accessing preferences.
	 * @param username Username to store.
	 * @param password Password to store.
	 */
	public static void save(final Context context, final String username, final String password) {
		final SharedPreferences prefs = getPreferences(context);
		if (prefs == null) {
			Logger.warn("Encrypted preferences unavailable; skipping credential save.");
			return;
		}
		if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
			Logger.warn("Credentials missing username or password; skipping save.");
			return;
		}
		final List<Credentials> credentials = loadAll(prefs);
		final Credentials updated = new Credentials(username.trim(), password, System.currentTimeMillis());
		final Iterator<Credentials> iterator = credentials.iterator();
		while (iterator.hasNext()) {
			final Credentials current = iterator.next();
			if (current.matchesUsername(username)) {
				iterator.remove();
			}
		}
		credentials.add(0, updated);
		while (credentials.size() > MAX_ENTRIES) {
			credentials.remove(credentials.size() - 1);
		}
		prefs.edit().putString(KEY_CREDENTIALS, serialize(credentials)).apply();
	}

	/**
	 * Removes any stored credentials.
	 *
	 * @param context Context for accessing preferences.
	 */
	public static void clear(final Context context) {
		final SharedPreferences prefs = getPreferences(context);
		if (prefs == null) {
			Logger.warn("Encrypted preferences unavailable; skipping credential clear.");
			return;
		}
		prefs.edit().remove(KEY_CREDENTIALS).remove(KEY_USERNAME_LEGACY).remove(KEY_PASSWORD_LEGACY).apply();
	}

	/**
	 * Loads most recently saved credentials if available.
	 *
	 * @param context Context for accessing preferences.
	 * @return Most recent credentials or <code>null</code> if not available.
	 */
	@Nullable
	public static Credentials load(final Context context) {
		final List<Credentials> all = loadAll(context);
		if (all.isEmpty()) {
			return null;
		}
		return all.get(0);
	}

	/**
	 * Loads all saved credentials ordered by most recent usage.
	 *
	 * @param context Context for accessing preferences.
	 * @return List of saved credentials.
	 */
	public static List<Credentials> loadAll(final Context context) {
		final SharedPreferences prefs = getPreferences(context);
		if (prefs == null) {
			Logger.warn("Encrypted preferences unavailable; skipping credential load.");
			return Collections.emptyList();
		}
		return loadAll(prefs);
	}

	/**
	 * Finds credentials for a given username (case-insensitive).
	 *
	 * @param credentials Stored credentials list.
	 * @param username    Username to look up.
	 * @return Matching credentials or <code>null</code>.
	 */
	@Nullable
	public static Credentials findByUsername(final List<Credentials> credentials, final String username) {
		if (credentials == null || TextUtils.isEmpty(username)) {
			return null;
		}
		for (final Credentials credential : credentials) {
			if (credential.matchesUsername(username)) {
				return credential;
			}
		}
		return null;
	}

	/**
	 * Retrieves encrypted shared preferences instance.
	 *
	 * @param context Context for accessing preferences.
	 * @return Encrypted shared preferences or <code>null</code> if unavailable.
	 */
	@Nullable
	private static SharedPreferences getPreferences(final Context context) {
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
				Logger.warn("EncryptedSharedPreferences requires Android M or above.");
				return null;
			}
			final MasterKey masterKey = new MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
					.build();
			return EncryptedSharedPreferences.create(context, PREF_FILE, masterKey,
					EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
					EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
		} catch (final Exception e) {
			Logger.error("Unable to initialize encrypted preferences: " + e.getMessage());
			return null;
		}
	}

	private static List<Credentials> loadAll(final SharedPreferences prefs) {
		try {
			final String serialized = prefs.getString(KEY_CREDENTIALS, "");
			if (TextUtils.isEmpty(serialized)) {
				final String legacyUsername = prefs.getString(KEY_USERNAME_LEGACY, "");
				final String legacyPassword = prefs.getString(KEY_PASSWORD_LEGACY, "");
				if (!TextUtils.isEmpty(legacyUsername) && !TextUtils.isEmpty(legacyPassword)) {
					final List<Credentials> migrated = new ArrayList<>();
					migrated.add(new Credentials(legacyUsername.trim(), legacyPassword, System.currentTimeMillis()));
					prefs.edit().remove(KEY_USERNAME_LEGACY).remove(KEY_PASSWORD_LEGACY)
							.putString(KEY_CREDENTIALS, serialize(migrated)).apply();
					return migrated;
				}
				return new ArrayList<>();
			}
			final JSONArray array = new JSONArray(serialized);
			final List<Credentials> credentials = new ArrayList<>();
			for (int i = 0; i < array.length(); i++) {
				final JSONObject obj = array.optJSONObject(i);
				if (obj == null) {
					continue;
				}
				final String username = obj.optString("username", "").trim();
				final String password = obj.optString("password", "");
				final long lastUsed = obj.optLong("lastUsed", 0);
				if (username.equals("") || password.equals("")) {
					continue;
				}
				credentials.add(new Credentials(username, password, lastUsed));
			}
			Collections.sort(credentials, new java.util.Comparator<Credentials>() {
				@Override
				public int compare(final Credentials a, final Credentials b) {
					return Long.compare(b.getLastUsed(), a.getLastUsed());
				}
			});
			return credentials;
		} catch (final Exception e) {
			Logger.error("Unable to load encrypted credentials: " + e.getMessage());
			return new ArrayList<>();
		}
	}

	private static String serialize(final List<Credentials> credentials) {
		try {
			final JSONArray array = new JSONArray();
			for (final Credentials credential : credentials) {
				final JSONObject obj = new JSONObject();
				obj.put("username", credential.getUsername());
				obj.put("password", credential.getPassword());
				obj.put("lastUsed", credential.getLastUsed());
				array.put(obj);
			}
			return array.toString();
		} catch (final Exception e) {
			Logger.error("Unable to serialize encrypted credentials: " + e.getMessage());
			return "[]";
		}
	}

	/**
	 * Represents a set of saved credentials.
	 */
	public static final class Credentials {
		private final String username;
		private final String password;
		private final long lastUsed;

		Credentials(final String username, final String password, final long lastUsed) {
			this.username = username;
			this.password = password;
			this.lastUsed = lastUsed;
		}

		public String getUsername() {
			return username;
		}

		public String getPassword() {
			return password;
		}

		public long getLastUsed() {
			return lastUsed;
		}

		boolean matchesUsername(final String otherUsername) {
			return username.equalsIgnoreCase(otherUsername);
		}
	}
}
