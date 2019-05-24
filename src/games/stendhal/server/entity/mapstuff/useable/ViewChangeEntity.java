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
package games.stendhal.server.entity.mapstuff.useable;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.events.ViewChangeEvent;

/**
 * An entity that when used, tells the client to change the view center. Used
 * for the DM arena scrying devices. If this starts to be used elsewhere, it
 * should be generalized to use conditions and actions.
 */
public class ViewChangeEntity extends UseableEntity {
	private static final String QUEST = "learn_scrying";
	private static final int COST = 5;

	private final int x;
	private final int y;

	/**
	 * Create a new ViewChangeEntity.
	 *
	 * @param x x coordinate of the view center
	 * @param y y coordinate of the view center
	 */
	public ViewChangeEntity(int x, int y) {
		this.x = x;
		this.y = y;
		setResistance(0);
	}

	@Override
	public String describe() {
		return "Oto scrying orb. Zapisane jest \"Użycie kosztuje " + COST
			+ " money. Stój w spokoju i skoncentruj się podczas oglądania\".";
	}

	@Override
	public boolean onUsed(RPEntity user) {
		if (!nextTo(user)) {
			user.sendPrivateText("Nie możesz stąd dosięgnąć.");
			return false;
		}
		if (user instanceof Player) {
			Player player = (Player) user;
			if (player.hasQuest(QUEST)) {
				if (player.drop("money", COST)) {
					player.addEvent(new ViewChangeEvent(x, y));
					player.notifyWorldAboutChanges();
					return true;
				} else {
					player.sendPrivateText("Nie posiadasz wystarczająco dużo money.");
				}
			} else {
				player.sendPrivateText("Nie wiesz jak obsłużyć to dziwne urządzenie.");
			}
		}
		return false;
	}
}
