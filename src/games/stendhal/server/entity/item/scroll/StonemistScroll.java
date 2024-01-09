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
package games.stendhal.server.entity.item.scroll;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import marauroa.common.game.IRPZone;

public class StonemistScroll extends Item {
	public StonemistScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public StonemistScroll(final StonemistScroll item) {
		super(item);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (user instanceof Player) {
			return findAltZonesAndTeleport((Player) user);
		}
		return false;
	}

	private boolean findAltZonesAndTeleport(Player player) {
		List<String> altZones = new ArrayList<>();
		for (IRPZone irpZone : SingletonRepository.getRPWorld()) {
			StendhalRPZone zone = (StendhalRPZone) irpZone;
			String[] zoneParts = zone.getName().split("_");

			if (containsAlt(zoneParts)) {
				altZones.add(zone.getName());
			}

			if (playerIsSimiliarZone(player, altZones)) {
				player.sendPrivateText("Kamień otworzył portal i wciągnął cię do środka...");
				player.teleport(zone, player.getX(), player.getY(), null, player);
				return true;
			}
		}

		player.sendPrivateText("Dziwnie błyszczący się kamień nic nie zrobił...");
		return false;
	}

	private boolean checkZones(Player player, List<String> zones) {
		String[] currentPlayerZone = player.getZone().getName().split("_");
		if (currentPlayerZone.length < 3) {
			return false;
		}

		for (String zone : zones) {
			String[] zoneParts = zone.split("_");
			if (zoneParts.length != currentPlayerZone.length) {
				continue;
			}

			boolean matches = true;
			for (int i = 1; i < currentPlayerZone.length; i++) {
				if (!zoneParts[i].equals(currentPlayerZone[i])) {
					matches = false;
					break;
				}
			}

			if (matches) {
				return true;
			}
		}

		return false;
	}

	private boolean playerIsSimiliarZone(Player player, List<String> altZones) {
		return checkZones(player, altZones);
	}

	private boolean containsAlt(String[] zoneParts) {
		List<String> zoneMatches = Arrays.asList(zoneParts);
		return zoneMatches.contains("alt");
	}
}
