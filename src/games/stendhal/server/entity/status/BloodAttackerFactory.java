/***************************************************************************
 *                 (C) Copyright 2019-2021 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.status;

public class BloodAttackerFactory {
	public static BloodAttacker get(final String profile) {
		if (profile != null) {
			final String[] statusparams = profile.split(";");
			return new BloodAttacker(Integer.parseInt(statusparams[0]));
		}
		return null;
	}
}
