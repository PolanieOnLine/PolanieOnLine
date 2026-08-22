/***************************************************************************
 *                   (C) Copyright 2003-2022 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui;

import games.stendhal.client.entity.factory.EntityMap;

/**
 * A key ring.
 */
@SuppressWarnings("serial")
class KeyRing extends FeatureEnabledSlotWindow {
	/**
	 * Create a key ring.
	 */
	public KeyRing() {
		// Remember if you change these numbers change also a number in
		// src/games/stendhal/server/entity/RPEntity.java
		super("keyring", 6, 2);
		// A panel window; forbid closing
		setCloseable(false);
	}

	/**
	 * A feature was enabled.
	 *
	 * @param name
	 *            The name of the feature.
	 * @param value
	 *            Optional feature specific data.
	 */
	@Override
	public void featureEnabled(final String name, String value) {
		if (name.equals("keyring")) {
			if (value.equals("")) {
				value = "6 2";
			}
			String[] values = value.split(" ");
			setSlotsLayout(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
			setAcceptedTypes(EntityMap.getClass("item", null, null));
		}
		super.featureEnabled(name, value);
	}
}
