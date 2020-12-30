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

	public static void setVisible(Player player, boolean setVisible) {
		String on = "Założyłeś pierścień na palec! Używaj go mądrze!";
		String off = "Zdjąłeś pierścień z palca i stałeś się widoczny!";
		if (player.getGender() == "F") {
			on = "Założyłaś pierścień na palec! Używaj go mądrze!";
			off = "Zdjęłaś pierścień z palca i stałaś się widoczna!";
		}

		if (setVisible == true) {
			player.sendPrivateText(on);

			player.stopAttack();
			player.setInvisible(setVisible);
			player.setVisibility(50);
		} else {
			player.sendPrivateText(off);

			player.setInvisible(setVisible);
		  	player.setVisibility(100);
		}
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
			setVisible(player, false);

		  	return true;
		} else {
			setVisible(player, true);

			return true;
		}
	}
}