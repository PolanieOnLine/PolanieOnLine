/***************************************************************************
 *                 Copyright © 2003-2024 - Faiumoni e. V.                  *
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;

import games.stendhal.server.constants.StandardMessages;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.AbstractAdminScript;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

/**
 * Script for invoking spawning of creature spawn point.
 *
 * Usage: /script SpawnCreature.class <x> <y>
 */
public class SpawnCreature extends AbstractAdminScript {
	@Override
	protected void run(final List<String> args) {
		checkNotNull(admin);
		int x;
		int y;
		try {
			x = Integer.parseInt(args.get(0));
		} catch (final NumberFormatException e) {
			StandardMessages.paramMustBeNumber(admin, "Współrzędna X");
			return;
		}
		try {
			y = Integer.parseInt(args.get(1));
		} catch (final NumberFormatException e) {
			StandardMessages.paramMustBeNumber(admin, "Współrzędna Y");
			return;
		}

		final StendhalRPZone zone = admin.getZone();
		if (zone == null) {
			sendError("Znajdujesz się w nieodpowiednim miejscu do odradzania stworzeń.");
			return;
		}

		final String zoneName = zone.getName();
		boolean spawned = false;
		for (final CreatureRespawnPoint p: zone.getRespawnPointList()) {
			if (p.getX() == x && p.getY() == y) {
				sendText("Odradzanie " + p.getPrototypeCreature().getName() + " w strefie "
						+ zoneName + " " + x + "," + y);
				p.spawnNow();
				spawned = true;
			}
		}
		if (spawned) {
			return;
		}

		sendError("Nie znaleziono punktu odradzania w " + zoneName + " " + x + "," + y + ". Wykonaj `/script "
				+ ListSpawnPoints.class.getSimpleName() + ".class` aby wyświetlić listę dostępnych punktów odrodzeń.");
	}

	@Override
	protected int getMinParams() {
		return 2;
	}

	@Override
	protected int getMaxParams() {
		return 2;
	}

	@Override
	protected List<String> getParamStrings() {
		return Arrays.asList("<x> <y>");
	}
}
