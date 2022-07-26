/***************************************************************************
 *                 (C) Copyright 2019-2022 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TeleportNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.game.RPObject;

public class RingOfTeleportation extends Item {
	private static final Logger logger = Logger.getLogger(RingOfTeleportation.class);

	/** The cooling period in seconds */
	private static final int MIN_COOLING_PERIOD = 5 * 60;
	private static final String LAST_USE = "frequency";

	public RingOfTeleportation(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		setPersistent(true);
	}

	public RingOfTeleportation(final RingOfTeleportation item) {
		super(item);
	}

	public boolean isUsed() {
		return getInt("amount") == 0;
	}

	public void usedRing() {
		setEntityClass("ring");
		setEntitySubclass("ametyst-ring");

		put("amount", 0);
		put("frequency", getLastUsed());
	}

	public void activeRing() {
		put("amount", 1);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		final RPObject base = getBaseContainer();

		if ((user instanceof Player) && user.nextTo((Entity) base)) {
			return teleportToSavedPosition((Player) user);
		}
		return false;
	}

	/**
	 * Get the last use time in seconds
	 * @return last use time
	 */
	private int getLastUsed() {
		if (has(LAST_USE)) {
			return getInt(LAST_USE);
		} else {
			return -1;
		}
	}

	/**
	 * Store current system time as the last used
	 */
	private void storeLastUsed() {
		put(LAST_USE, (int) (System.currentTimeMillis() / 1000));
	}

	private int getCoolingPeriod() {
		return MIN_COOLING_PERIOD;
	}

	private boolean teleportToSavedPosition(final Player player) {
		// don't allow use if on the ground
		if (!isContained()) {
			player.sendPrivateText(Grammar.genderVerb(player.getGender(), "Powinieneś") + " podnieść swój pierścień powrotu, by go użyć.");
			return false;
		}

		final int secondsNeeded = getLastUsed() + getCoolingPeriod() - (int) (System.currentTimeMillis() / 1000);
		if (secondsNeeded > 0) {
			player.sendPrivateText("Pierścień jeszcze nie odzyskał w pełni swojej mocy. "
					+ Grammar.genderVerb(player.getGender(), "Myślałeś")
					+ ", że będzie gotowy w ciągu "
					+ TimeUtil.approxTimeUntil(secondsNeeded) + ".");

			return false;
		}

		StendhalRPZone zone = player.getZone();

		if (isUsed()) {
			setInfoString(player.getID().getZoneID() + " " + player.getX() + " " + player.getY());
			activeRing();

			return true;
		} else {
			zone = SingletonRepository.getRPWorld().getZone("0_semos_city");
			int x = 30;
			int y = 40;

			final String infostring = getInfoString();
			if (infostring != null) {
				final StringTokenizer st = new StringTokenizer(infostring);
				if (st.countTokens() == 3) {
					// check destination
					final String zoneName = st.nextToken();
					final StendhalRPZone temp = SingletonRepository.getRPWorld().getZone(zoneName);
					if (temp == null) {
						player.sendPrivateText("Z dziwnych powodów pierścień nie przeniósł mnie tam gdzie chciałem.");
						logger.warn("marked scroll to unknown zone " + infostring
								+ " teleported " + player.getName() + " to Semos instead");
					} else {
						if (player.getKeyedSlot("!visited", zoneName) == null) {
							player.sendPrivateText(Grammar.genderVerb(player.getGender(), "Słyszałeś") + " wiele opowieści o miejscu, do którego chcesz się przenieść "
									+ "i nie możesz się skoncentrować ponieważ nigdy tam nie " + Grammar.genderVerb(player.getGender(), "byłeś") + ".");
							return false;
						} else {
							zone = temp;
							x = Integer.parseInt(st.nextToken());
							y = Integer.parseInt(st.nextToken());
						}
					}
				}
			}

			if (player.teleport(zone, x, y, null, player)) {
				TeleportNotifier.get().notify(player, true);
				setInfoString(null);
				storeLastUsed();
				usedRing();

				return true;
			}
			return false;
		}
	}

	@Override
	public String describe() {
		String text;
		if (isUsed()) {
			text = "Oto §'pierścień powrotu'. Kamień nie ma zapisanego miejsca gdzie mógłby przenieść przez co stracił moc i blask.";
		} else {
			text = "Oto §'pierścień powrotu'. Kamień potrafiący zapisać aktualne miejsce pobytu.";
		}

		if (isBound()) {
			text += " Oto specjalna nagroda dla " + getBoundTo() + " za wykonanie zadania, która nie może być używana przez innych.";
		}

		final String infostring = getInfoString();
		if (infostring != null) {
			text += " Zapisana pozycja: " + infostring;
		}

		return text;
	}
}
