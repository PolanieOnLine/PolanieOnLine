/***************************************************************************
 *                   Copyright © 2024 - PolanieOnLine                      *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.Arrays;
import java.util.List;

import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Backward-compatible alias for starting Dragon Land via StartMapEvent.
 *
 * Usage: /script ForceDragonLandEvent.class
 * Preferred: /script StartMapEvent.class dragon_land [force|safe]
 */
public class ForceDragonLandEvent extends ScriptImpl {
	private final StartMapEvent delegate = new StartMapEvent();

	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args != null && !args.isEmpty()) {
			admin.sendPrivateText("Użycie: /script ForceDragonLandEvent.class");
			admin.sendPrivateText("Preferowane: /script StartMapEvent.class dragon_land [force|safe]");
			return;
		}

		admin.sendPrivateText(
				"Alias kompatybilności: uruchamianie wydarzenia przez /script StartMapEvent.class dragon_land force");
		delegate.execute(admin, Arrays.asList("dragon_land", "force"));
	}
}
