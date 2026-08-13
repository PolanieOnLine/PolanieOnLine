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
package games.stendhal.client.entity;

/**
 * Resets static entity state that belongs to the currently active character.
 */
public final class CharacterSessionEntityReset {
	private CharacterSessionEntityReset() {
		// utility class
	}

	/**
	 * Clear static references that must not survive changing characters.
	 */
	public static void reset() {
		User.updateGroupStatus(null, null, null);
		User.setNull();
	}
}
