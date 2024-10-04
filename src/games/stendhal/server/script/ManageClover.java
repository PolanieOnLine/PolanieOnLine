/***************************************************************************
 *                    Copyright © 2024 - Faiumoni e. V.                    *
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
import java.util.Locale;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.scripting.AbstractAdminScript;
import games.stendhal.server.entity.mapstuff.spawner.CloverSpawner;
import games.stendhal.server.util.TimeUtil;

/**
 * Locates or re-spawns four-leaf clover.
 */
public class ManageClover extends AbstractAdminScript {
	@Override
	protected void run(List<String> args) {
		final String command = args.get(0).toLowerCase(Locale.ENGLISH);
		final CloverSpawner spawner = CloverSpawner.get();
		// re-spawn is always set to midnight
		final String timeToRespawn = TimeUtil.timeUntil(TimeUtil.secondsToMidnight());
		if ("status".equals(command)) {
			String zoneDesc = "nie znaleziono";
			final StendhalRPZone zone = spawner.getZone();
			if (zone != null) {
				zoneDesc = zone.getName() + " na " + spawner.getX() + "," + spawner.getY();
			}
			final boolean available = spawner.hasPickableClover();
			admin.sendPrivateText("Lokalizacja: " + zoneDesc + "\nKoniczyna dostępna: "
						+ (available ? "tak" : "nie") + "\nPonownie pojawi się za " + timeToRespawn);
		} else if ("respawn".equals(command)) {
			spawner.onTurnReached(0);
			final StendhalRPZone zone = spawner.getZone();
			if (spawner.hasPickableClover() && zone != null) {
				admin.sendPrivateText("Pojawiła się koniczyna w " + zone.getName() + " na " + spawner.getX() + ","
						+ spawner.getY() + "\nPonownie pojawi się za " + timeToRespawn);
			} else {
				admin.sendPrivateText(NotificationType.ERROR,
						"Nie udało się odrodzić koniczyny, spróbuj ponownie za 15 minut");
			}
		} else {
			// show unmodified command
			admin.sendPrivateText(NotificationType.ERROR, "Nieznana komenda \"" + args.get(0) + "\"");
		}
	}

	@Override
	public int getMinParams() {
		return 1;
	}

	@Override
	public int getMaxParams() {
		return 1;
	}

	@Override
	protected List<String> getParamDetails() {
		return Arrays.asList(
			"status: Pobiera lokalizację spawnera i dostępność koniczyny.",
			"respawn: Zmusza koniczynę do natychmiastowego odrodzenia się w losowym miejscu."
		);
	}
}