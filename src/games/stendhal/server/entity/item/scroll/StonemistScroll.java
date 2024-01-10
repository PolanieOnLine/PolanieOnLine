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

import games.stendhal.common.MathHelper;
import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.game.IRPZone;

public class StonemistScroll extends Item {
	private static final int MIN_USETIME = 1 * 60; // 1 hour
	private final int timeInTurns = 24000; // 200 turns = 1 minute, 24000 = 2 hours

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
			if (((Player) user).getQuest("alt_teleportback") != null)
				return teleportBack((Player) user);
			else
				return findAltZonesAndTeleport((Player) user);
		}
		return false;
	}

	private boolean findAltZonesAndTeleport(Player player) {
		if (!isTimePassed(player)) {
			player.sendPrivateText("Wciąż wyczuwasz jak mgielny kamień zbiera energię do ponownego otworzenia portalu. "
					+ "Podejrzewasz, że będzie gotowy w ciągu " + TimeUtil.approxTimeUntil((int) currentTime(player)) + ".");
			return false;
		}

		List<String> altZones = new ArrayList<>();
		for (IRPZone irpZone : SingletonRepository.getRPWorld()) {
			StendhalRPZone zone = (StendhalRPZone) irpZone;
			String[] zoneParts = zone.getName().split("_");

			if (containsAlt(zoneParts)) {
				altZones.add(zone.getName());
			}

			if (playerIsSimiliarZone(player, altZones)) {
				createWarningBeforeRetransport(player, timeInTurns);
				createReTransportTimer(player, timeInTurns);

				player.setQuest("alt_teleportback", player.getZone().getName());
				player.setQuest("alt_usedtime", Integer.toString((int) (System.currentTimeMillis() / 1000)));
				player.teleport(zone, player.getX(), player.getY(), null, player);
				player.sendPrivateText(NotificationType.CLIENT, "Kamień otworzył portal i wciągnął cię do środka...");
				return true;
			}
		}

		player.sendPrivateText("Dziwnie błyszczący się kamień nic nie zrobił...");
		return false;
	}

	boolean isTimePassed(final Player player) {
		return (currentTime(player) <= 0L);
	}

	int currentTime(final Player player) {
		if (player.getQuest("alt_usedtime") == null) {
			return 0;
		}

		final int usageDelay = MIN_USETIME * MathHelper.MINUTES_IN_ONE_HOUR;
		try {
			int lastUsageTime = Integer.parseInt(player.getQuest("alt_usedtime"));
			return (lastUsageTime + usageDelay) - (int) (System.currentTimeMillis() / 1000);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
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

	private void createReTransportTimer(final Player player, final int timeInTurns) {
		SingletonRepository.getTurnNotifier().notifyInTurns(timeInTurns,
				new TimedTeleportTurnListener(player));
	}

	private void createWarningBeforeRetransport(final Player player, final int timeInTurns) {
		SingletonRepository.getTurnNotifier().notifyInTurns((int) (timeInTurns * 0.9),
			new TimedTeleportWarningTurnListener(player,
				player.getZone(),
				"Portal utworzony przez kamień słabnie, odczuwasz wrażenie potrzeby powrotu do swojego świata!"));
	}

	public boolean teleportBack(final Player player) {
		final StendhalRPZone returnZone = SingletonRepository.getRPWorld().getZone(player.getQuest("alt_teleportback"));
		int returnX = player.getX();
		int returnY = player.getY();

		player.removeQuest("alt_teleportback");
		boolean result = player.teleport(returnZone, returnX, returnY, null, player);
		player.sendPrivateText(NotificationType.WARNING, "Moc kamienia tymczasowo się wyczerpała... Wymuszono na tobie powrót do realnego świata.");

		return result;
	}

	class TimedTeleportTurnListener implements TurnListener {
		private final Player player;

		TimedTeleportTurnListener(final Player player) {
			this.player = player;
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			teleportBack(player);
		}
	}

	static class TimedTeleportWarningTurnListener implements TurnListener {
		private final Player player;
		private final StendhalRPZone zone;
		private final String warningMessage;

		TimedTeleportWarningTurnListener(final Player player, final StendhalRPZone zone,
				final String warningMessage) {
			this.player = player;
			this.zone = zone;
			this.warningMessage = warningMessage;
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			if ((player == null) || (player.getZone() == null) || (zone == null)) {
				return;
			}
			if (player.getZone().getName().equals(zone.getName())) {
				player.sendPrivateText(NotificationType.CLIENT, warningMessage);
			}
		}
	}
}
