/***************************************************************************
 *                 (C) Copyright 2019-2026 - PolanieOnLine                 *
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

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.events.TeleportNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;
import marauroa.common.game.RPObject;
import marauroa.server.game.rp.InstanceZoneDescriptor;
import marauroa.server.game.rp.InstanceZoneManager;

public class RingOfTeleportation extends Item {
	private static final Logger logger = Logger.getLogger(RingOfTeleportation.class);

	/** The cooling period in seconds. */
	private static final int MIN_COOLING_PERIOD = 5 * 60;
	private static final String LAST_USE = "frequency";
	private static final String OZO_MAZE = "7_labirynt";
	private static final String HAIZEN_MAZE_NAME = "Labirynt Haizena";

	public RingOfTeleportation(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		setPersistent(true);
		updateState();
	}

	public RingOfTeleportation(final RingOfTeleportation item) {
		super(item);
		updateState();
	}

	/** Create a RingOfTeleportation. */
	public RingOfTeleportation() {
		super("pierścień powrotu", "ring", "ametyst-ring", null);
		put("amount", 0);
		updateState();
	}

	private void updateState() {
		if (isUsed()) {
			setState(0);
		} else {
			setState(1);
		}
	}

	@Override
	public void fill(final RPObject rpobject) {
		super.fill(rpobject);
		updateState();
	}

	public boolean isUsed() {
		return getInt("amount") == 0;
	}

	public void usedRing() {
		setEntityClass("ring");
		setEntitySubclass("ametyst-ring");
		put("amount", 0);
		put("state", 0);
	}

	public void activeRing() {
		put("amount", 1);
		put("state", 1);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		final RPObject base = getBaseContainer();

		if ((user instanceof Player) && user.nextTo((Entity) base)) {
			return teleportToSavedPosition((Player) user);
		}
		return false;
	}

	/** Get the last use time in seconds. */
	private int getLastUsed() {
		if (has(LAST_USE)) {
			return getInt(LAST_USE);
		}
		return -1;
	}

	/** Store current system time as the last used. */
	private void storeLastUsed() {
		put(LAST_USE, (int) (System.currentTimeMillis() / 1000));
	}

	private int getCoolingPeriod() {
		return MIN_COOLING_PERIOD;
	}

	private boolean teleportToSavedPosition(final Player player) {
		if (!isContained()) {
			player.sendPrivateText(player.getGenderVerb("Powinieneś") + " podnieść swój pierścień powrotu, by go użyć.");
			return false;
		}

		final int secondsNeeded = getLastUsed() + getCoolingPeriod() - (int) (System.currentTimeMillis() / 1000);
		if (secondsNeeded > 0) {
			player.sendPrivateText("Pierścień jeszcze nie odzyskał w pełni swojej mocy. "
					+ "Myślisz, że będzie gotowy w ciągu "
					+ TimeUtil.approxTimeUntil(secondsNeeded) + ".");
			return false;
		}

		if (isUsed()) {
			return saveCurrentPosition(player);
		}
		return returnToSavedPosition(player);
	}

	private boolean saveCurrentPosition(final Player player) {
		final StendhalRPZone zone = player.getZone();
		if (isForbiddenReturnZone(zone)) {
			player.sendPrivateText("Magia pierścienia nie potrafi zapisać tego labiryntu. Musisz wydostać się z niego w sposób przewidziany dla tej próby.");
			return false;
		}

		setItemData(zone.getName() + " " + player.getX() + " " + player.getY());
		activeRing();
		return true;
	}

	private boolean returnToSavedPosition(final Player player) {
		final SavedPosition saved = parseSavedPosition(getItemData());
		if (saved == null) {
			invalidateSavedPosition(player, "Zapisane miejsce w pierścieniu jest uszkodzone. Kamień stracił ten zapis i trzeba wskazać mu nowe miejsce.");
			return false;
		}

		final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(saved.zoneName);
		if (zone == null) {
			logger.warn("Ring of return points to unknown zone " + saved.zoneName + " for " + player.getName());
			invalidateSavedPosition(player, "Zapisana w pierścieniu lokacja już nie istnieje. Kamień stracił ten zapis i nie przeniósł cię w inne miejsce zastępcze.");
			return false;
		}
		if (isForbiddenReturnZone(zone)) {
			invalidateSavedPosition(player, "Pierścień miał zapisane miejsce w labiryncie, z którego nie wolno uciekać w ten sposób. Ten stary zapis został usunięty.");
			return false;
		}
		if (player.getKeyedSlot("!visited", saved.zoneName) == null) {
			player.sendPrivateText(player.getGenderVerb("Słyszałeś") + " wiele opowieści o miejscu, do którego chcesz się przenieść "
					+ "i nie możesz się skoncentrować ponieważ nigdy tam nie " + player.getGenderVerb("byłeś") + ".");
			return false;
		}

		if (player.teleport(zone, saved.x, saved.y, null, player)) {
			TeleportNotifier.get().notify(player, true);
			setItemData(null);
			storeLastUsed();
			usedRing();
			return true;
		}
		return false;
	}

	private void invalidateSavedPosition(final Player player, final String message) {
		setItemData(null);
		usedRing();
		player.sendPrivateText(message);
	}

	private static SavedPosition parseSavedPosition(final String itemdata) {
		if (itemdata == null) {
			return null;
		}
		final StringTokenizer tokenizer = new StringTokenizer(itemdata);
		if (tokenizer.countTokens() != 3) {
			return null;
		}
		final String zoneName = tokenizer.nextToken();
		try {
			final int x = Integer.parseInt(tokenizer.nextToken());
			final int y = Integer.parseInt(tokenizer.nextToken());
			return new SavedPosition(zoneName, x, y);
		} catch (final NumberFormatException e) {
			return null;
		}
	}

	static boolean isForbiddenReturnZone(final StendhalRPZone zone) {
		if (zone == null) {
			return true;
		}
		if (OZO_MAZE.equals(zone.getName()) || zone.getName().endsWith("_maze")) {
			return true;
		}
		if (zone.getAttributes() != null && HAIZEN_MAZE_NAME.equals(zone.getAttributes().get("readable_name"))) {
			return true;
		}

		try {
			final InstanceZoneManager manager = SingletonRepository.getRPWorld().getInstanceZoneManager();
			if (manager != null) {
				final InstanceZoneDescriptor descriptor = manager.getDescriptor(zone.getID());
				if (descriptor != null && "maze".equals(descriptor.getBaseZoneId())) {
					return true;
				}
			}
		} catch (final RuntimeException e) {
			// A standalone zone used in tests may not belong to an initialized world.
			// Explicit names and attributes above still protect the known mazes.
			logger.debug("Could not inspect instance descriptor for " + zone.getName(), e);
		}
		return false;
	}

	private static final class SavedPosition {
		private final String zoneName;
		private final int x;
		private final int y;

		SavedPosition(final String zoneName, final int x, final int y) {
			this.zoneName = zoneName;
			this.x = x;
			this.y = y;
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

		final String itemdata = getItemData();
		if (itemdata != null) {
			text += " Zapisana pozycja: " + itemdata;
		}

		return text;
	}
}
