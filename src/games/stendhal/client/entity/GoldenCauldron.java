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
		ensureReadyAtFromStart();
		applyReadyInSeconds();
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
			if (readyInSeconds <= 0) {
				fireChange(PROP_READY_AT);
			}
		}
		if (changes.has("ready_started_at")) {
			readyStartedAt = changes.getLong("ready_started_at");
			ensureReadyAtFromStart();
			if (!changes.has("ready_at") && !changes.has("ready_in")) {
				fireChange(PROP_READY_AT);
			}
		}
		if (changes.has("ready_in")) {
			readyInSeconds = changes.getInt("ready_in");
			applyReadyInSeconds();
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
			if (!changes.has("ready_at") && !changes.has("ready_in")) {
				fireChange(PROP_READY_AT);
			}
		}
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
