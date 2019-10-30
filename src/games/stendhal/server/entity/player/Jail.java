/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.Direction;
import games.stendhal.common.MathHelper;
import games.stendhal.common.NotificationType;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.config.ZoneConfigurator;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.LoginListener;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.ZoneEnterExitListener;
import games.stendhal.server.entity.Outfit;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.mapstuff.office.ArrestWarrant;
import games.stendhal.server.entity.mapstuff.office.ArrestWarrantList;
import marauroa.common.game.RPObject;

/**
 * This class is responsible of keeping players who have misbehaved in a special
 * jail area where they can't do any harm. The misbehaving player will be
 * automatically released after a specified number of minutes.
 *
 * @author daniel
 */
public class Jail implements ZoneConfigurator, LoginListener {
	private static final Outfit UNIFORM = new Outfit(null, 998, null, null, null, 998, null);
	private static final Outfit UNIFORM1 = new Outfit(8, 998, 16, null, null, 998, null);
	private static final Outfit UNIFORM2 = new Outfit(2, 998, 19, null, null, 998, null);
	private static final Outfit MANHORSE1 = new Outfit(25, null, null, null, null, null, null);
	private static final Outfit MANHORSE2 = new Outfit(31, null, null, null, null, null, null);
	private static final Outfit MANHORSE3 = new Outfit(32, null, null, null, null, null, null);
	private static final Outfit MANHORSE4 = new Outfit(35, null, null, null, null, null, null);
	private static final Outfit MANHORSE5 = new Outfit(37, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE1 = new Outfit(24, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE2 = new Outfit(26, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE3 = new Outfit(27, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE4 = new Outfit(28, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE5 = new Outfit(29, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE6 = new Outfit(30, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE7= new Outfit(33, null, null, null, null, null, null);
	private static final Outfit WOMANHORSE8= new Outfit(34, null, null, null, null, null, null);
	static StendhalRPZone jailzone;

	private static final Logger LOGGER = Logger.getLogger(Jail.class);
	private static final List<Point> cellEntryPoints = Arrays.asList(
			new Point(8, 3),
			new Point(13, 3),
			new Point(18, 3),
			new Point(23, 3),
			new Point(28, 3),
			new Point(8, 11),
			new Point(13, 11),
			new Point(18, 11),
			new Point(23, 11),
			new Point(28, 11)
		);

		private static final Rectangle[] cellBlocks = {
			new Rectangle(6, 1, 30, 4),
			new Rectangle(6, 10, 30, 13)
		};

	private ArrestWarrantList arrestWarrants;

	private final List<Cell> cells = new LinkedList<Cell>();

	private final ZoneEnterExitListener listener = new ZoneEnterExitListener() {
		@Override
		public void onEntered(final RPObject object, final StendhalRPZone zone) {
			if (object.getRPClass().subclassOf("player")) {
				//TODO: could this be a bug ? (durkham)

			}
		}

		@Override
		public void onExited(final RPObject object, final StendhalRPZone zone) {
			if (object instanceof RPEntity && object.getRPClass().subclassOf("player")) {
				String playerName = ((RPEntity)object).getName();

				for (final Cell cell : cells) {
					cell.remove(playerName);
				}
			}
		}
	};


	private final class Jailer implements TurnListener {

		private final String criminalName;

		Jailer(final String name) {
			criminalName = name;
		}

		@Override
		public boolean equals(final Object obj) {
			if (obj instanceof Jailer) {
				final Jailer other = (Jailer) obj;
				return criminalName.equals(other.criminalName);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return criminalName.hashCode();
		}

		@Override
		public void onTurnReached(final int currentTurn) {
			release(criminalName);
		}

		@Override
		public String toString() {
			return "Jailer [criminalName=" + criminalName + "]";
		}
	}

	/**
	 * @param criminalName
	 *            The name of the player who should be jailed
	 * @param policeman
	 *            The object for the RPEntity or admin who wants to jail the criminal
	 * @param minutes
	 *            The duration of the sentence
	 * @param reason why criminal was jailed
	 */
	public void imprison(final String criminalName, final RPEntity policeman,
			final int minutes, final String reason) {

		final Player criminal = SingletonRepository.getRuleProcessor().getPlayer(
						criminalName);

		arrestWarrants.removeByName(criminalName);
		final ArrestWarrant arrestWarrant = new ArrestWarrant(criminalName, policeman.getName(), minutes, reason);

		policeman.sendPrivateText("Aresztowałeś " + criminalName
			+ " na " + minutes + " minutę. Powód: " + reason + ".");
		SingletonRepository.getRuleProcessor().sendMessageToSupporters("JailKeeper",
			policeman.getName() + " aresztował " + criminalName
			+ " na " + minutes + " minutę. Powód: " + reason
			+ ".");

		if (criminal == null) {
			final String text = "Nie ma wojownika zwanego " + criminalName + ", ale został wydany za nim list gończy.";
			policeman.sendPrivateText(text);
			LOGGER.info(text);
		} else {
			arrestWarrant.setStarted();
			imprison(criminal, policeman, minutes);
			criminal.sendPrivateText(NotificationType.SUPPORT,
					"Zostałeś aresztowany przez " + policeman.getName()
					+ " na " + minutes + " minutę. Powód: " + reason + ".");
			LOGGER.info(criminal.getName() + " has been jailed for " + minutes
					+ " " + Grammar.plnoun(minutes, "minute") + ". Reason: " + reason + ".");
		}
		arrestWarrants.add(arrestWarrant);
	}

	protected void imprison(final Player criminal, final RPEntity policeman, final int minutes) {

		if (jailzone == null) {
			final String text = "Żaden obszar nie został ustawiony jako Jailzone";
			policeman.sendPrivateText(text);
			LOGGER.error(text);
			return;
		}
		final boolean successful = teleportToAvailableCell(criminal, policeman);
		if (successful) {
			final Jailer jailer = new Jailer(criminal.getName());
			SingletonRepository.getTurnNotifier().dontNotify(jailer);

			// Set a timer so that the inmate is automatically released after
			// serving his sentence. Negative times are treated as "forever",
			// thus no turn notifiers are needed. Zero is useful for freeing
			// players, so we handle that normally.
			LOGGER.info("Setting turn notifier for " + (minutes * 60) + " " + jailer);
			if (minutes >= 0) {
				if (MANHORSE1.isPartOf(criminal.getOutfit())
					|| MANHORSE2.isPartOf(criminal.getOutfit())
					|| MANHORSE3.isPartOf(criminal.getOutfit())
					|| MANHORSE4.isPartOf(criminal.getOutfit())
					|| MANHORSE5.isPartOf(criminal.getOutfit())) {
					SingletonRepository.getTurnNotifier().notifyInSeconds(minutes * 60, jailer);
					criminal.setOutfit(UNIFORM2, true);
				} else if (WOMANHORSE1.isPartOf(criminal.getOutfit())
					|| WOMANHORSE2.isPartOf(criminal.getOutfit())
					|| WOMANHORSE3.isPartOf(criminal.getOutfit())
					|| WOMANHORSE4.isPartOf(criminal.getOutfit())
					|| WOMANHORSE5.isPartOf(criminal.getOutfit())
					|| WOMANHORSE6.isPartOf(criminal.getOutfit())
					|| WOMANHORSE7.isPartOf(criminal.getOutfit())
					|| WOMANHORSE8.isPartOf(criminal.getOutfit())) {
					SingletonRepository.getTurnNotifier().notifyInSeconds(minutes * 60, jailer);
					criminal.setOutfit(UNIFORM1, true);
				} else {
					SingletonRepository.getTurnNotifier().notifyInSeconds(minutes * 60, jailer);
					criminal.setOutfit(UNIFORM, true);
				}
			}
		} else {
			policeman.sendPrivateText("Nie można znaleźć celi dla "	+ criminal.getName());
			LOGGER.error("Could not find a cell for " + criminal.getName());
		}
	}

	private boolean teleportToAvailableCell(final Player criminal,
			final RPEntity policeman) {
		Collections.shuffle(cells);
		for (final Cell cell : cells) {
			if (cell.isEmpty()) {
				// could make the last parameter the policeman, if the policeman is a player
				if (criminal.teleport(jailzone, cell.getEntry().x, cell
						.getEntry().y, Direction.DOWN, null)) {
					cell.add(criminal.getName());
					return true;
				}
			}

		}

		return false;
	}

	/**
	 * Releases an inmate and teleports him to Semos city, but only if he is
	 * still in jail.
	 *
	 * @param inmateName
	 *            the name of the inmate who should be released
	 * @return true if the player has not logged out before he was released
	 */
	public boolean release(final String inmateName) {
		final Player inmate = SingletonRepository.getRuleProcessor().getPlayer(inmateName);
		if (inmate == null) {
			LOGGER.info("Jailed player " + inmateName + " has logged out.");
			return false;
		}

		release(inmate);
		return true;
	}

	void release(final Player inmate) {
		// Only teleport the player if he is still in jail.
		// It could be that an admin has teleported him out earlier.
		if (isInJail(inmate)) {

			final StendhalRPZone exitZone = jailzone;

			inmate.teleport(exitZone, 8, 7, Direction.LEFT, null);
			inmate.sendPrivateText(NotificationType.SUPPORT,
					"Skończyła się twoja kara. Możesz teraz odejść.");
			putOffUniform(inmate);
			LOGGER.info("Player " + inmate.getName() + " released from jail.");
		} else {
			LOGGER.info("Tried to release player " + inmate.getName() + ", but " + inmate.getName() + " is not in jail.");
		}

		// The player completed his sentence and did not logout
		// so destroy the ArrestWarrant
		arrestWarrants.removeByName(inmate.getName());
	}

	private void putOffUniform(final Player player) {
		if (UNIFORM2.isPartOf(player.getOutfit())
			|| UNIFORM1.isPartOf(player.getOutfit())
			|| UNIFORM.isPartOf(player.getOutfit())) {
				player.returnToOriginalOutfit();
		}
	}

	/**
	 * Is player in a jail cell? Ignores visitors outside of cells.
	 *
	 * @param inmate
	 *            player to check
	 * @return true, if it is in jail, false otherwise.
	 */
	public static boolean isInJail(final Player inmate) {
		final StendhalRPZone zone = inmate.getZone();

		if ((zone != null) && zone.equals(jailzone)) {
			for (final Rectangle cellBlock : cellBlocks) {
				if (cellBlock.contains(inmate.getX(), inmate.getY())) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Destroy the arrest warrant so that the player is not jailed again on next login.
	 * @param player
	 */
	public void grantParoleIfPlayerWasAPrisoner(final Player player) {

		arrestWarrants.removeByName(player.getName());
	}

	@Override
	public void onLoggedIn(final Player player) {
		// we need to do this on the next turn because the
		// client does not get any private messages otherwise
		// sometime the client does not get the map content
		// if it gets a cross zone teleport too early. so we wait
		// 5 seconds.
		SingletonRepository.getTurnNotifier().notifyInSeconds(5, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				final String name = player.getName();

				final ArrestWarrant arrestWarrant = arrestWarrants.getByName(name);
				if (arrestWarrant == null) {
					return;
				}

				if (removeVeryOldWarrants(arrestWarrant)) {
					LOGGER.warn("Removed very old:" + arrestWarrant);
					release(player);
					return;
				}

				final long timestamp = arrestWarrant.getTimestamp();
				player.sendPrivateText(NotificationType.SUPPORT,
						"Zostałeś aresztowany "
					+ " przez " + arrestWarrant.getPoliceOfficer()
					+ " na " + arrestWarrant.getMinutes()
					+ " minutę" + " o " + String.format("%tF", timestamp)
					+ ". Powód: " + arrestWarrant.getReason() + ".");
				LOGGER.info(player.getName() + " logged in who has been jailed "
					+ " by " + arrestWarrant.getPoliceOfficer()
					+ " for " + arrestWarrant.getMinutes()
					+ " " + Grammar.plnoun(arrestWarrant.getMinutes(), "minute") + " on " + String.format("%tF", timestamp)
					+ ". Reason: " + arrestWarrant.getReason() + ".");

				handleEscapeMessages(arrestWarrant);
				imprison(player, player, arrestWarrant.getMinutes());
			}

			public boolean removeVeryOldWarrants(final ArrestWarrant arrestWarrant) {
				final long timestamp = arrestWarrant.getTimestamp();
				if (timestamp + 30 * MathHelper.MILLISECONDS_IN_ONE_DAY < System.currentTimeMillis()) {
					arrestWarrants.removeByName(arrestWarrant.getCriminal());
					return true;
				}

				return false;
			}


			public void handleEscapeMessages(final ArrestWarrant arrestWarrant) {
				if (arrestWarrant.isStarted()) {
					// Notify player that his sentences is starting again because he tried to escape by logging out
					player.sendPrivateText(NotificationType.SUPPORT,
							"Bez względu na to ile czasu "
							+ "już spędziłeś w więzieniu to twoja kara "
							+ "odbywa się od nowa, ponieważ próbowałeś uciec.");
				} else {
					// Jail player who was offline at the time /jail was issued.
					arrestWarrant.setStarted();
				}
			}
		});
	}

	public String listJailed() {
		if (arrestWarrants != null) {
			return arrestWarrants.toString();
		}
		return "jail not inited ?";
	}

	/**
	 * Get the ArrestWarrant of a jailed player.
	 *
	 * @param name the name of the player
	 * @return the ArrestWarrant for the player, or null if there is none
	 */
	public ArrestWarrant getWarrant(final String name) {
		return arrestWarrants.getByName(name);
	}

	@Override
	public void configureZone(final StendhalRPZone zone, final Map<String, String> attributes) {
		SingletonRepository.getLoginNotifier().addListener(this);
		SingletonRepository.setJail(this);
		zone.addZoneEnterExitListener(listener);
		initCells();
		arrestWarrants = new ArrestWarrantList(zone);
		jailzone = zone;
	}

	private void initCells() {
		for (final Point p : cellEntryPoints) {
			cells.add(new Cell(p));
		}

	}
}
