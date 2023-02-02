/***************************************************************************
 *                   Copyright © 2003-2023 - Arianne                       *
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

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.AbstractAdminScript;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;
import games.stendhal.server.entity.player.Player;

/**
 * Script for invoking spawning of creature spawn point.
 *
 * Usage: /script SpawnCreature.class <x> <y>
 */
public class SpawnCreature extends AbstractAdminScript {
	@Override
	protected void run(final Player admin, final List<String> args) {
		int x;
		int y;
		try {
			x = Integer.parseInt(args.get(0));
		} catch (final NumberFormatException e) {
			admin.sendPrivateText(NotificationType.ERROR, "Współrzędna X musi być liczbą");
			return;
		}
		try {
			y = Integer.parseInt(args.get(1));
		} catch (final NumberFormatException e) {
			admin.sendPrivateText(NotificationType.ERROR, "Współrzędna Y musi być liczbą");
			return;
		}

		final StendhalRPZone zone = admin.getZone();
		if (zone == null) {
			admin.sendPrivateText(NotificationType.ERROR, "Nie znajdujesz się w"
					+ " odpowiednim miejscu do odradzania stworzeń.");
			return;
		}

		final String zoneName = zone.getName();
		boolean spawned = false;
		for (final CreatureRespawnPoint p: zone.getRespawnPointList()) {
			if (p.getX() == x && p.getY() == y) {
				admin.sendPrivateText("Odradzanie " + p.getPrototypeCreature().getName()
						+ " w strefie " + zoneName + " " + x + "," + y);
				p.spawnNow();
				spawned = true;
			}
		}
		if (spawned) {
			return;
		}

		admin.sendPrivateText(NotificationType.ERROR, "Nie znaleziono punktu odradzania w "
				+ zoneName + " " + x + "," + y + ". Wykonaj `/script "
				+ ListSpawnPoints.class.getSimpleName()
				+ ".class` aby wyświetlić listę dostępnych punktów odrodzeń.");
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
