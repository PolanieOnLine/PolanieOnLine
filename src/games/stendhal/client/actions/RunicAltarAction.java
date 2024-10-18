/***************************************************************************
 *                 (C) Copyright 2019-2024 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.actions;

import games.stendhal.client.gui.j2DClient;

class RunicAltarAction implements SlashAction {
	@Override
	public boolean execute(String[] params, String remainder) {
		j2DClient client = j2DClient.get();
		if (client == null) {
			return false;
		}

		client.getVisibleRunicAltar();
		return true;
	}

	@Override
	public int getMaximumParameters() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getMinimumParameters() {
		// TODO Auto-generated method stub
		return 0;
	}
}
