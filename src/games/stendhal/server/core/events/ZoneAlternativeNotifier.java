/***************************************************************************
 *                    (C) Copyright 2024 - PolanieOnLine                   *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.core.events;

import java.util.ArrayList;
import java.util.List;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.entity.item.scroll.StonemistScroll;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;

public class ZoneAlternativeNotifier {
	private static final EntityManager em = SingletonRepository.getEntityManager();
	private static final StonemistScroll stoneMist = (StonemistScroll) em.getItem("mgielny kamień");

	/**
	 * Zone changes.
	 *
	 * @param player
	 *			Player
	 * @param sourceZone
	 *			source zone
	 * @param destinationZone
	 *			destination zone
	 */
	public static void zoneChange(final Player player, final String sourceZone,
			final String destinationZone) {
		for (IRPZone irpZone : SingletonRepository.getRPWorld()) {
			StendhalRPZone zone = (StendhalRPZone) irpZone;
	
			handleAltZones(player, zone, destinationZone);
		}
	}

	private static void handleAltZones(Player player, StendhalRPZone zone, String destinationZone) {
		List<String> altZones = new ArrayList<>();
		if (stoneMist.containsAlt(zone.getName())) {
			altZones.add(zone.getName());
		}
	
		if (stoneMist.playerIsSimiliarZone(player, altZones)) {
			notifyPlayerAboutAltTeleport(player, zone, destinationZone);
		}
	}
	
	private static void notifyPlayerAboutAltTeleport(Player player, StendhalRPZone zone, String destinationZone) {
		if (player.isEquipped(stoneMist.getName()) && destinationZone.equals(stoneMist.removeAltPrefix(zone.getName()))) {
			new DelayedPlayerTextSender(player, "Mgielny kamień drży i pulsuje tajemniczą energią. Wydaje się to być dobry moment i miejsce na stworzenie przejścia między światami...", NotificationType.SCENE_SETTING, 2);
		}
	}
}
