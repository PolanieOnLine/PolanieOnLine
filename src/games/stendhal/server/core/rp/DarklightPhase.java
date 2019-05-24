/***************************************************************************
 *                   (C) Copyright 2011-2012 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.rp;

/**
 * Dark light phase
 *
 * @author ??? but thank you!
 */
public enum DarklightPhase {

	/** during the night */
	NIGHT (0x47408c);

	private Integer color;

	private DarklightPhase(int color) {
		this.color = Integer.valueOf(color);
	}

	private DarklightPhase() {
		this.color = null;
	}

	/**
	 * gets the current daylight phase
	 *
	 * @return DaylightPhase
	 */
	public static DarklightPhase current() {
		return NIGHT;
	}

	/**
	 * gets the color of this daylight phase
	 *
	 * @return color
	 */
	public Integer getColor() {
		return color;
	}
}
