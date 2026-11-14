/***************************************************************************
 *                    Copyright © 2003-2022 - Arianne                      *
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

import games.stendhal.server.entity.player.Player;
import marauroa.common.game.SlotOwner;

public class PlayerBagSlot extends PlayerSlot {
	public PlayerBagSlot(final String player) {
		super(player);
	}

	@Override
	public boolean isFull() {
		final SlotOwner owner = getOwner();
		if (!(owner instanceof Player)) {
			return super.isFull();
		}
		return size() >= getMaxSize();
	}

	/** Current effective capacity, including a player's expanded backpack. */
	public int getMaxSize() {
		if (!(getOwner() instanceof Player)) {
			return super.getCapacity();
		}
		final Player player = (Player) getOwner();
		if (!player.hasFeature("bag")) {
			return 36;
		}
		return player.getMaxSlotSize("bag");
	}
}
