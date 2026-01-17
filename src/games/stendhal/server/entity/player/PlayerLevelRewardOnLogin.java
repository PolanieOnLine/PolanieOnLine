/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.util.LevelRewardRegistry;

public class PlayerLevelRewardOnLogin implements LoginListener {
	@Override
	public void onLoggedIn(final Player player) {
		LevelRewardRegistry.checkAndInitiate(player);
	}
}
