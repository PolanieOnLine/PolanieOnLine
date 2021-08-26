/***************************************************************************
 *                   (C) Copyright 2003-2021 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.slot;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.player.Player;

/**
 * Magicbag slots of players which contain items.
 *
 * @author hendrik
 */
public class PlayerMagicBagSlot extends PlayerSlot {

	/**
	 * Creates a new PlayerSlot.
	 *
	 * @param player player
	 */
	public PlayerMagicBagSlot(final String player) {
		super(player);
	}

	@Override
	public boolean isReachableForTakingThingsOutOfBy(final Entity entity) {
		if (!mayAccessMagicBag(entity)) {
			setErrorMessage("Twoja torba jest zepsuta. Powinieneś poszukać kogoś kto potrafi go naprawić.");
			return false;
		}
		return super.isReachableForTakingThingsOutOfBy(entity);
	}

	@Override
	public boolean isReachableForThrowingThingsIntoBy(Entity entity) {
		if (!mayAccessMagicBag(entity)) {
			setErrorMessage("Twoja torba jest zepsuta. Powinieneś poszukać kogoś kto potrafi go naprawić.");
			return false;
		}
		return super.isReachableForThrowingThingsIntoBy(entity);
	}

	/**
	 * checks whether the entity may access the key ring
	 *
	 * @param entity Entity
	 * @return true, if the keyring may be accessed, false otherwise
	 */
	private boolean mayAccessMagicBag(Entity entity) {
		if (!(entity instanceof Player)) {
			return false;
		}
		Player player = (Player) entity;
		return (player.getFeature("magicbag") != null);
	}
}
