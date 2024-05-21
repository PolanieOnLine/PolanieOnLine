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
import java.util.List;
import java.util.Map;

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
	// Constants for usage time and turn duration
	private static final int MIN_USETIME = 1 * 60; // 1 hour
	private final int timeInTurns = 24000; // 200 turns = 1 minute, 24000 = 2 hours

	/**
	 * Constructor for StonemistScroll.
	 *
	 * @param name       The name of the item.
	 * @param clazz      The class of the item.
	 * @param subclass   The subclass of the item.
	 * @param attributes Additional attributes for the item.
	 */
	public StonemistScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor for StonemistScroll.
	 *
	 * @param item The StonemistScroll item to copy.
	 */
	public StonemistScroll(final StonemistScroll item) {
		super(item);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (user instanceof Player) {
			if (((Player) user).getQuest("alt_teleportback") != null)
				return teleportBack((Player) user, true);
			else
				return findAltZonesAndTeleport((Player) user);
		}
		return false;
	}

	/**
	 * Find alternative zones and initiate teleportation
	 */
	private boolean findAltZonesAndTeleport(Player player) {
		if (!isTimePassed(player)) {
			player.sendPrivateText("Wciąż wyczuwasz jak mgielny kamień zbiera energię do ponownego otworzenia portalu. "
					+ "Podejrzewasz, że będzie gotowy w ciągu " + TimeUtil.approxTimeUntil((int) currentTime(player)) + ".");
			return false;
		}

		for (IRPZone irpZone : SingletonRepository.getRPWorld()) {
			StendhalRPZone zone = (StendhalRPZone) irpZone;

			List<String> altZones = new ArrayList<>();
			if (containsAlt(zone.getName())) {
				altZones.add(zone.getName());
			}

			if (playerIsSimiliarZone(player, altZones)) {
				createWarningBeforeRetransport(player, timeInTurns);
				createReTransportTimer(player, timeInTurns);

				player.setQuest("alt_teleportback", "true");
				player.setQuest("alt_usedtime", Integer.toString((int) (System.currentTimeMillis() / 1000)));
				player.teleport(zone, player.getX(), player.getY(), null, player);
				player.sendPrivateText(NotificationType.CLIENT, "Kamień otworzył portal i wciągnął cię do środka...");
				return true;
			}
		}

		// Display a message if the Stonemist Scroll didn't do anything
		player.sendPrivateText("Dziwnie błyszczący się kamień nic nie zrobił...");
		return false;
	}

	/**
	 * Checks if enough time has passed since the last usage of the StonemistScroll.
	 *
	 * @param player The player using the StonemistScroll.
	 * @return True if enough time has passed, false otherwise.
	 */
	public boolean isTimePassed(final Player player) {
		return (currentTime(player) <= 0L);
	}

	/**
	 * Calculates the remaining time until the StonemistScroll can be used again.
	 *
	 * @param player The player using the StonemistScroll.
	 * @return The remaining time in seconds.
	 */
	private int currentTime(final Player player) {
		if (player.getQuest("alt_usedtime") == null) {
			return 0;
		}

		final int usageDelay = MIN_USETIME * TimeUtil.MINUTES_IN_HOUR;
		try {
			int lastUsageTime = Integer.parseInt(player.getQuest("alt_usedtime"));
			return (lastUsageTime + usageDelay) - (int) (System.currentTimeMillis() / 1000);
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return 0;
		}
	}

	/**
	 * Removes the "alt_" prefix from a zone name if present.
	 *
	 * @param zoneName The original zone name.
	 * @return The zone name without the "alt_" prefix.
	 */
	public String removeAltPrefix(String zoneName) {
		if (containsAlt(zoneName)) {
			return zoneName.substring(4);
		}
		return zoneName;
	}

	private boolean checkZones(Player player, List<String> zones) {
		String currentPlayerZone = removeAltPrefix(player.getZone().getName());
		if (currentPlayerZone.length() < 3) {
			return false;
		}

		for (String zone : zones) {
			String zoneWithoutAlt = removeAltPrefix(zone);
			if (zoneWithoutAlt.length() != currentPlayerZone.length()) {
				continue;
			}

			boolean matches = true;
			for (int i = 1; i < currentPlayerZone.length(); i++) {
				if (zoneWithoutAlt.charAt(i) != currentPlayerZone.charAt(i)) {
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

	/**
	 * Checks if the player's zone is similar to any of the provided alternative zones.
	 *
	 * @param player   The player to check.
	 * @param altZones The list of alternative zones.
	 * @return True if the player's zone is similar to any of the alternative zones, false otherwise.
	 */
	public boolean playerIsSimiliarZone(Player player, List<String> altZones) {
		return checkZones(player, altZones);
	}

	/**
	 * Checks if a zone name contains the "alt_" prefix.
	 *
	 * @param zone The zone name to check.
	 * @return True if the zone name contains the "alt_" prefix, false otherwise.
	 */
	public boolean containsAlt(String zone) {
		return zone.startsWith("alt_");
	}

	/**
	 * Creates a timer for re-transporting the player after a specified number of turns.
	 *
	 * @param player      The player to be re-transported.
	 * @param timeInTurns The number of turns after which the re-transport should occur.
	 */
	private void createReTransportTimer(final Player player, final int timeInTurns) {
		SingletonRepository.getTurnNotifier().notifyInTurns(timeInTurns,
				new TimedTeleportTurnListener(player));
	}

	/**
	 * Creates a warning timer before re-transporting the player.
	 *
	 * @param player      The player to receive the warning.
	 * @param timeInTurns The number of turns before re-transport when the warning should be sent.
	 */
	private void createWarningBeforeRetransport(final Player player, final int timeInTurns) {
		SingletonRepository.getTurnNotifier().notifyInTurns((int) (timeInTurns * 0.9),
			new TimedTeleportWarningTurnListener(player,
				player.getZone(),
				"Portal utworzony przez kamień słabnie, odczuwasz wrażenie potrzeby powrotu do swojego świata!"));
	}

	private StendhalRPZone getRPZone(String zone) {
		return SingletonRepository.getRPWorld().getZone(zone);
	}

	public boolean teleportBack(final Player player) {
		return teleportBack(player, false);
	}

	/**
	 * Teleports the player back to the original zone.
	 *
	 * @param player      The player to be teleported.
	 * @param sendMessage True if a message should be sent to the player, false otherwise.
	 * @return True if the teleportation was successful, false otherwise.
	 */
	private boolean teleportBack(final Player player, boolean sendMessage) {
		final StendhalRPZone returnZone = getRPZone(removeAltPrefix(player.getZone().getName()));
		int returnX = player.getX();
		int returnY = player.getY();

		player.removeQuest("alt_teleportback");
		boolean result = player.teleport(returnZone, returnX, returnY, null, player);

		if (sendMessage) {
			player.sendPrivateText(NotificationType.WARNING, "Moc kamienia tymczasowo się wyczerpała... Wymuszono na tobie powrót do realnego świata.");
		}

		return result;
	}

	/**
	 * Listener for the timed teleportation turn event.
	 */
	private class TimedTeleportTurnListener implements TurnListener {
		private final Player player;

		/**
		 * Constructor for TimedTeleportTurnListener.
		 *
		 * @param player The player associated with the listener.
		 */
		TimedTeleportTurnListener(final Player player) {
			this.player = player;
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			teleportBack(player);
		}
	}

	/**
	 * Listener for the timed teleportation warning turn event.
	 */
	private static class TimedTeleportWarningTurnListener implements TurnListener {
		private final Player player;
		private final StendhalRPZone zone;
		private final String warningMessage;

		/**
		 * Constructor for TimedTeleportWarningTurnListener.
		 *
		 * @param player         The player associated with the listener.
		 * @param zone           The zone associated with the warning.
		 * @param warningMessage The warning message to be sent.
		 */
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
