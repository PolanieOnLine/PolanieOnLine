/***************************************************************************
 *                 (C) Copyright 2003-2024 - PolanieOnLine                 *
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

import java.util.Map;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.util.TimeUtil;

/**
 * Represents the rainbow beans that takes the player to the dream world zone,
 * after which it will teleport player to a random location in 0_zakopane_c.
 */
public class MagicznyScroll extends TimedTeleportScroll {
	private static final long DELAY = 2 * TimeUtil.MILLISECONDS_IN_WEEK;

	/**
	 * Creates a new timed marked MagicznyScroll scroll.
	 *
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public MagicznyScroll(final String name, final String clazz, final String subclass,
			final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public MagicznyScroll(final MagicznyScroll item) {
		super(item);
	}

	@Override
	protected boolean useTeleportScroll(final Player player) {
		final String QUEST_SLOT = "ozo";
		long lastuse = -1;
		if (player.hasQuest(QUEST_SLOT)) {
			final String[] tokens = player.getQuest(QUEST_SLOT).split(";");
			if (tokens.length == 4) {
				// we stored a last time (or -1)
				lastuse = Long.parseLong(tokens[3]);
			}
			final long timeRemaining = (lastuse + DELAY) - System.currentTimeMillis();
			if (timeRemaining > 0) {
				// player used the scroll within the last DELAY hours
				// so are not allowed to go yet. but don't reset the last time taken.
				// the private text doesn't get sent because events are lost on zone change. (marauroa bug)
				player.sendPrivateText(player.getGenderVerb("Pochorowałeś") + " się od nadużywania magii.");
				this.removeOne();
				final Item sick = SingletonRepository.getEntityManager().getItem("wymioty");
				player.getZone().add(sick);
				sick.setPosition(player.getX(), player.getY() + 1);
				return false;
			} else {
				// don't overwrite the last bought time from Ozo, this is in tokens[1]
				player.setQuest(QUEST_SLOT, "bought;" + tokens[1] + ";taken;" + System.currentTimeMillis());
				return super.useTeleportScroll(player);
			}
		} else {
			// players can only buy magiczny scroll fon Ozo who stores the time bought in quest slot
			// so if they didn't have the quest slot they got the scroll ''illegally''
			player.sendPrivateText("To był podejrzany zwój. Następnym razem kupuj od Ozo.");
			this.removeOne();
			final Item sick = SingletonRepository.getEntityManager().getItem("wymioty");
			player.getZone().add(sick);
			sick.setPosition(player.getX(), player.getY() + 1);
			return false;
		}
	}

	@Override
	protected String getBeforeReturnMessage() {
		return "Powoli tracisz moc...";
	}

	@Override
	protected String getAfterReturnMessage() {
		return "Uff... żyjesz. To było dziwne doświadczenie.";
	}
}
