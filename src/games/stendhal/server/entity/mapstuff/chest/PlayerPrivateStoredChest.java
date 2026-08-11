/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.mapstuff.chest;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.slot.ChestSlot;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * A persistent chest that exists as a normal zone object on the server but is
 * only included in the perception of one player. Each owner therefore gets a
 * real independent chest/content slot even when multiple instances occupy the
 * same map coordinates.
 */
public class PlayerPrivateStoredChest extends StoredChest {

	public static final String PERCEPTION_KEY_ATTRIBUTE = "#perception_key";
	public static final String PERCEPTION_VALUE_ATTRIBUTE = "#perception_value";
	private static final String PLAYER_NAME_ATTRIBUTE = "name";

	/** Creates a new private chest for a player. */
	public PlayerPrivateStoredChest(final Player owner) {
		super();
		put(PERCEPTION_KEY_ATTRIBUTE, PLAYER_NAME_ATTRIBUTE);
		put(PERCEPTION_VALUE_ATTRIBUTE, owner.getName());
		replaceContentSlot();
	}

	/** Restores a private stored chest loaded from zone persistence. */
	public PlayerPrivateStoredChest(final RPObject object) {
		super(object);
		replaceContentSlot();
	}

	public String getOwnerName() {
		return get(PERCEPTION_VALUE_ATTRIBUTE);
	}

	public boolean isOwnedBy(final Entity entity) {
		return entity instanceof Player && getOwnerName().equals(entity.getName());
	}

	@Override
	public boolean onUsed(final RPEntity user) {
		if (!isOwnedBy(user)) {
			if (user instanceof Player) {
				((Player) user).sendPrivateText("Ta skrzynia nie zawiera Twojego wyposażenia zadania.");
			}
			return false;
		}
		return super.onUsed(user);
	}

	@Override
	public String getDescriptionName() {
		return "prywatna skrzynia zadania";
	}

	private void replaceContentSlot() {
		final RPSlot oldSlot = getSlot("content");
		final List<RPObject> contents = new LinkedList<RPObject>();
		for (final RPObject object : oldSlot) {
			contents.add(object);
		}
		oldSlot.clear();
		removeSlot("content");

		final RPSlot privateSlot = new PlayerPrivateChestSlot(this);
		addSlot(privateSlot);
		for (final RPObject object : contents) {
			privateSlot.add(object);
		}
	}

	/** Server-side guard in addition to perception filtering. */
	private static class PlayerPrivateChestSlot extends ChestSlot {
		private final PlayerPrivateStoredChest chest;

		PlayerPrivateChestSlot(final PlayerPrivateStoredChest chest) {
			super(chest);
			this.chest = chest;
		}

		@Override
		public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
			if (!chest.isOwnedBy(entity)) {
				setErrorMessage("Nie możesz zabrać wyposażenia z cudzej skrzyni zadania.");
				return false;
			}
			return super.isReachableForTakingThingsOutOfBy(entity);
		}
	}
}
