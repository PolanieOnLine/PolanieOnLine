/***************************************************************************
 *                     Copyright © 2020 - Arianne                          *
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

import games.stendhal.common.NotificationType;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.BestiaryEvent;

/**
 * Item class that shows some basic information about enemies around Faiumoni.
 */
public class Bestiary extends OwnedItem {
	public Bestiary(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		setMenu("Czytaj|Użyj");
	}

	public Bestiary(final Bestiary item) {
		super(item);
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (!(user instanceof Player)) {
			return false;
		}

		final Player player = (Player) user;

		if (!super.onUsed(player)) {
			player.sendPrivateText(NotificationType.RESPONSE, "Czytasz: Ten bestiariusz jest własnością " + getOwner() + ". Zwróć go prawowitemu właścicielowi.");
			return false;
		}

		player.addEvent(new BestiaryEvent(player));
		player.notifyWorldAboutChanges();

		return true;
	}

	@Override
	public void setOwner(final String name) {
		put("itemdata", name);
	}

	@Override
	public String getOwner() {
		return get("itemdata");
	}
}
