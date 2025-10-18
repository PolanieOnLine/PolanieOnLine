/***************************************************************************
 *                   (C) Copyright 2003-2024 - Stendhal                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.entity;

import games.stendhal.client.StendhalClient;
import games.stendhal.client.entity.User;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Client representation of the golden cauldron entity.
 */
public class GoldenCauldron extends Entity {
	private static final long BREW_TIME_MILLIS = 5L * 60L * 1000L;
	public static final Property PROP_OPEN = new Property();
	public static final Property PROP_STATE = new Property();
	public static final Property PROP_STATUS = new Property();
	public static final Property PROP_BREWER = new Property();
	public static final Property PROP_READY_AT = new Property();

	private boolean open;
	private int state;
	private String status;
	private String brewer;
	private RPSlot content;
	private long readyAt;
	private int readyInSeconds;
	private long readyStartedAt;
	private String cacheKey;

	private static final Map<String, Long> READY_AT_CACHE = new ConcurrentHashMap<String, Long>();
	private static final Map<String, Long> READY_STARTED_CACHE = new ConcurrentHashMap<String, Long>();

	public GoldenCauldron() {
		status = "";
	}

	public RPSlot getContent() {
		return content;
	}

	public boolean isOpen() {
		return open;
	}

	public boolean isActive() {
		return state > 0;
	}

	public String getStatusText() {
		return status;
	}

	public long getReadyAt() {
		return readyAt;
	}

	public boolean isControlledByUser() {
		final User user = User.get();
		if (user == null) {
			return false;
		}
		return brewer != null
		&& brewer.equalsIgnoreCase(StendhalClient.get().getCharacter());
	}

	@Override
	public void initialize(final RPObject object) {
		super.initialize(object);

		cacheKey = buildCacheKey(object);

		if (object.hasSlot("content")) {
			content = object.getSlot("content");
		} else {
			content = null;
		}

		open = object.has("open");
		state = object.has("state") ? object.getInt("state") : 0;
		status = object.has("status") ? object.get("status") : "";
		brewer = object.has("brewer") ? object.get("brewer") : null;
		readyAt = object.has("ready_at") ? object.getLong("ready_at") : 0L;
		readyStartedAt = object.has("ready_started_at") ? object.getLong("ready_started_at") : 0L;
		readyInSeconds = object.has("ready_in") ? object.getInt("ready_in") : 0;
		restoreCachedTimes();
		ensureReadyAtFromStart();
		applyReadyInSeconds();
		cacheReadyTimes();
	}

	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);

		if (changes.has("open")) {
			open = true;
			fireChange(PROP_OPEN);
		}
		if (changes.has("state")) {
			state = changes.getInt("state");
			fireChange(PROP_STATE);
		}
		if (changes.has("status")) {
			status = changes.get("status");
			fireChange(PROP_STATUS);
		}
		if (changes.has("brewer")) {
			brewer = changes.get("brewer");
			fireChange(PROP_BREWER);
		}
		if (changes.has("ready_at")) {
			readyAt = changes.getLong("ready_at");
			cacheReadyTimes();
			if (readyInSeconds <= 0) {
				fireChange(PROP_READY_AT);
			}
		}
		if (changes.has("ready_started_at")) {
			readyStartedAt = changes.getLong("ready_started_at");
			cacheReadyTimes();
			ensureReadyAtFromStart();
			if (!changes.has("ready_at") && !changes.has("ready_in")) {
				fireChange(PROP_READY_AT);
			}
		}
		if (changes.has("ready_in")) {
			readyInSeconds = changes.getInt("ready_in");
			applyReadyInSeconds();
			cacheReadyTimes();
			fireChange(PROP_READY_AT);
		}
	}

	@Override
	public void onChangedRemoved(final RPObject object, final RPObject changes) {
		super.onChangedRemoved(object, changes);

		if (changes.has("open")) {
			open = false;
			fireChange(PROP_OPEN);
		}
		if (changes.has("state")) {
			state = 0;
			fireChange(PROP_STATE);
		}
		if (changes.has("status")) {
			status = "";
			fireChange(PROP_STATUS);
		}
		if (changes.has("brewer")) {
			brewer = null;
			fireChange(PROP_BREWER);
		}
		if (changes.has("ready_at")) {
			readyAt = 0L;
			if (!changes.has("ready_in")) {
				readyInSeconds = 0;
			}
			cacheReadyTimes();
			fireChange(PROP_READY_AT);
		}
		if (changes.has("ready_in")) {
			readyInSeconds = 0;
			if (!changes.has("ready_at")) {
				fireChange(PROP_READY_AT);
			}
		}
		if (changes.has("ready_started_at")) {
			readyStartedAt = 0L;
			cacheReadyTimes();
			if (!changes.has("ready_at") && !changes.has("ready_in")) {
				fireChange(PROP_READY_AT);
			}
		}
	}

	private void restoreCachedTimes() {
		if (readyAt <= 0) {
			final Long cached = READY_AT_CACHE.get(getCacheKey());
			if ((cached != null) && (cached > System.currentTimeMillis())) {
				readyAt = cached.longValue();
			}
		}

		if (readyStartedAt <= 0) {
			final Long cachedStart = READY_STARTED_CACHE.get(getCacheKey());
			if (cachedStart != null) {
				readyStartedAt = cachedStart.longValue();
			} else if (readyAt > 0) {
				readyStartedAt = readyAt - BREW_TIME_MILLIS;
			}
		}
	}

	private void cacheReadyTimes() {
		final String key = getCacheKey();

		if (readyAt > 0) {
			READY_AT_CACHE.put(key, Long.valueOf(readyAt));
		} else {
			final Long cached = READY_AT_CACHE.get(key);
			if ((cached != null) && (cached.longValue() <= System.currentTimeMillis())) {
				READY_AT_CACHE.remove(key);
			}
		}

		if (readyStartedAt > 0) {
			READY_STARTED_CACHE.put(key, Long.valueOf(readyStartedAt));
		} else {
			final Long cachedStart = READY_STARTED_CACHE.get(key);
			if (cachedStart != null) {
				final long finish = cachedStart.longValue() + BREW_TIME_MILLIS;
				if (finish <= System.currentTimeMillis()) {
					READY_STARTED_CACHE.remove(key);
				}
			}
		}
	}

	private String getCacheKey() {
		if (cacheKey == null) {
			final RPObject.ID id = getID();
			final String zone = (id != null) ? id.getZoneID() : "";
			final int px = (int) Math.round(getX());
			final int py = (int) Math.round(getY());
			cacheKey = zone + ':' + px + ':' + py;
		}
		return cacheKey;
	}

	private String buildCacheKey(final RPObject object) {
		final RPObject.ID id = object.getID();
		final String zone = (id != null) ? id.getZoneID() : "";
		final int px = object.has("x") ? object.getInt("x") : (int) Math.round(getX());
		final int py = object.has("y") ? object.getInt("y") : (int) Math.round(getY());
		final String name = object.has("name") ? object.get("name") : "";
		return zone + ':' + name + ':' + px + ':' + py;
	}

	private void applyReadyInSeconds() {
		if (readyInSeconds <= 0) {
			ensureReadyAtFromStart();
			return;
		}

		final long candidate = System.currentTimeMillis() + (readyInSeconds * 1000L);
		if (readyAt > 0) {
			readyAt = Math.min(readyAt, candidate);
		} else if (readyStartedAt > 0) {
			final long expected = readyStartedAt + BREW_TIME_MILLIS;
			if (expected > System.currentTimeMillis()) {
				readyAt = Math.min(expected, candidate);
			} else {
				readyAt = candidate;
			}
		} else {
			readyAt = candidate;
		}
	}

	private void ensureReadyAtFromStart() {
		if (readyStartedAt <= 0) {
			return;
		}

		final long expected = readyStartedAt + BREW_TIME_MILLIS;
		if (expected <= 0) {
			return;
		}

		if (readyAt <= 0 || readyAt > expected) {
			readyAt = expected;
		}
	}
}
