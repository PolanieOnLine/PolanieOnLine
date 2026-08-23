/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
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

@SuppressWarnings("serial")
class MagicBag extends FeatureEnabledSlotWindow {
	/**
	 * Create a magic bag.
	 */
	public MagicBag() {
		// Remember if you change these numbers change also a number in
		// src/games/stendhal/server/entity/RPEntity.java
		super("magicbag", 6, 1);
		// A panel window; forbid closing
		setCloseable(false);
	}

}
