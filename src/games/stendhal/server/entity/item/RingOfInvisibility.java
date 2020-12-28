/***************************************************************************
 *                      (C) Copyright 2020 - Stendhal                      *
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

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/** 
 * If ring has been used by player, he will be invisible for monsters.
 * When a player attacks a creature, player become visible
 *
 * @author KarajuSs
 */
public class RingOfInvisibility extends Item {
	/**
	 * Creates a new invisibility ring and immediately sets
	 * the player to be invisible to creatures
	 * 
	 * @param name
	 * @param clazz
	 * @param subclass
	 * @param attributes
	 */
	public RingOfInvisibility(final String name, final String clazz, final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
		setPersistent(true);
	}

	/**
	 * Copy constructor.
	 *
	 * @param item
	 *            item to copy
	 */
	public RingOfInvisibility(final RingOfInvisibility item) {
		super(item);
	}

	public static boolean isInvisible(final Player player) {
		return player.isInvisibleToCreatures();
	}

	private void setInvisibility(final Player player) {
		player.sendPrivateText("Pierścień niewidzialności zaczął działać! Używaj tego mądrze!");

		player.stopAttack();
		player.setInvisible(true);
		player.setVisibility(50);
	}
	public static void removeInvisibility(final Player player) {
		player.sendPrivateText("Niewidzialność przestała działać!");

		player.setInvisible(false);
	  	player.setVisibility(100);
	}

	/**
	 * Method that sets the player using the ring
	 * to be invisible
	 */
	@Override
	public boolean onUsed(final RPEntity entity) {
		if ((entity instanceof Player)) {
			return makeInvisible((Player) entity);
		}
		return false;
	}

	private boolean makeInvisible(final Player player) {
		if (isInvisible(player)) {
			removeInvisibility(player);

		  	return true;
		} else {
			setInvisibility(player);

			return true;
		}
	}
}