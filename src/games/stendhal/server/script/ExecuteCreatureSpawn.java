/***************************************************************************
 *                   Copyright © 2003-2022 - Arianne                       *
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

import java.util.List;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.mapstuff.spawner.CreatureRespawnPoint;

public class ExecuteCreatureSpawn extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() < 3) {
			admin.sendPrivateText(NotificationType.ERROR, "Brakujący parametr: ExecuteCreatureSpawn.class <strefa> <x> <y>");
			return;
		}

		final String zoneName = args.get(0);
		final String sx = args.get(1);
		final String sy = args.get(2);

		int x;
		int y;
		try {
			x = Integer.parseInt(sx);
		} catch (final NumberFormatException e) {
			admin.sendPrivateText(NotificationType.ERROR, "Współrzędna X musi być liczbą");
			return;
		}
		try {
			y = Integer.parseInt(sy);
		} catch (final NumberFormatException e) {
			admin.sendPrivateText(NotificationType.ERROR, "Współrzędna Y musi być liczbą");
			return;
		}

		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);

		if (zone == null) {
			admin.sendPrivateText(NotificationType.ERROR, "Nieznana strefa: " + zoneName);
			return;
		}

		CreatureRespawnPoint spawnPoint = null;
		String zoneSpawns = "";
		for (final CreatureRespawnPoint p: zone.getRespawnPointList()) {
			if (zoneSpawns.equals("")) {
				zoneSpawns = p.getPrototypeCreature().getName() + " (" + p.getX() + "," + p.getY() + ")";
			} else {
				zoneSpawns += ", " + p.getPrototypeCreature().getName() + " (" + p.getX() + "," + p.getY() + ")";
			}

			if (p.getX() == x && p.getY() == y) {
				spawnPoint = p;
			}
		}

		if (spawnPoint == null) {
			admin.sendPrivateText(NotificationType.ERROR, "Nie znaleziono punktu odradzania na mapie " + zoneName
					+ " " + sx + "," + sy + ". Dostępne punkty odradzania: " + zoneSpawns);
			return;
		}

		admin.sendPrivateText("Odrodzono " + spawnPoint.getPrototypeCreature().getName()
				+ " na mapie " + zoneName + " " + sx + "," + sy);
		spawnPoint.spawnNow();
	}
}
