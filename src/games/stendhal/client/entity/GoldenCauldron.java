/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2024 - PolanieOnLine                    *
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

import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Client representation of Draconia's golden cauldron.
 */
public class GoldenCauldron extends StatefulEntity {
	/** Property fired when the cauldron toggles its open state. */
	public static final Property PROP_OPEN = new Property();
	/** Property fired when the cauldron changes the current brewer. */
	public static final Property PROP_BREWER = new Property();
	/** Slot name containing the ingredients dropped into the cauldron. */
	public static final String MIX_SLOT = "mix";

	private boolean open;
	private String brewer;

	@Override
	public void initialize(final RPObject object) {
		super.initialize(object);

		open = object.has("open");
		if (object.has("brewer")) {
			brewer = object.get("brewer");
		} else {
			brewer = null;
		}
	}

	@Override
	public void onChangedAdded(final RPObject object, final RPObject changes) {
		super.onChangedAdded(object, changes);

		if (changes.has("open")) {
			open = true;
			fireChange(PROP_OPEN);
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
		if (changes.has("brewer")) {
			brewer = null;
			fireChange(PROP_BREWER);
		}
	}

	/**
	 * Determine if the cauldron is currently open for brewing.
	 *
	 * @return {@code true} when the ingredient window should be visible
	 */
	public boolean isOpen() {
		return open;
	}

	/**
	 * Name of the player currently operating the cauldron.
	 *
	 * @return brewer name or {@code null}
	 */
	public String getBrewer() {
		return brewer;
	}

	/**
	 * Check whether the provided player name matches the current brewer.
	 *
	 * @param playerName player character name
	 * @return {@code true} if the player operates the cauldron
	 */
	public boolean isBrewer(final String playerName) {
		if (brewer == null || playerName == null) {
			return false;
		}
		return brewer.equalsIgnoreCase(playerName);
	}

	/**
	 * Access the ingredient slot.
	 *
	 * @return slot storing dropped ingredients
	 */
	public RPSlot getMixSlot() {
		return getSlot(MIX_SLOT);
	}
}
