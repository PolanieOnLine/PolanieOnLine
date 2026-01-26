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

import java.util.Collections;
import java.util.List;

import games.stendhal.server.core.scripting.AbstractAdminScript;
import games.stendhal.server.maps.dragon.DragonLandEvent;

/**
 * Forces the Dragon Land event to start.
 *
 * Usage: /script ForceDragonLandEvent.class
 */
public class ForceDragonLandEvent extends AbstractAdminScript {
	@Override
	protected void run(final List<String> args) {
		if (DragonLandEvent.forceStart()) {
			sendText("Wydarzenie Smocza Kraina zostało uruchomione.");
		} else {
			sendWarning("Wydarzenie Smocza Kraina już trwa.");
		}
	}

	@Override
	protected int getMinParams() {
		return 0;
	}

	@Override
	protected int getMaxParams() {
		return 0;
	}

	@Override
	protected List<String> getParamStrings() {
		return Collections.emptyList();
	}
}
