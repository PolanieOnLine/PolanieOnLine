/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.common.constants;

/** Shared timing constants used by both server and clients. */
public final class GameTiming {
	/** Duration of one server turn. */
	public static final int MILLISECONDS_PER_TURN = 300;
	/** Duration of one server turn expressed in seconds. */
	public static final double SECONDS_PER_TURN =
			MILLISECONDS_PER_TURN / 1000.0;

	private GameTiming() {
		// constants class
	}
}
