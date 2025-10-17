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
	public static final Property PROP_OPEN = new Property();
	public static final Property PROP_STATE = new Property();
	public static final Property PROP_STATUS = new Property();
	public static final Property PROP_BREWER = new Property();

	private boolean open;
	private int state;
	private String status;
	private String brewer;
	private RPSlot content;

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

	public boolean isControlledByUser() {
		final User user = User.get();
		if (user == null) {
			return false;
		}
		return brewer != null && brewer.equalsIgnoreCase(StendhalClient.get().getCharacter());
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
	}
}
