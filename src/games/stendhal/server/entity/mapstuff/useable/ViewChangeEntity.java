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
 * for the DM arena scrying devices and other map observation points.
 */
public class ViewChangeEntity extends UseableEntity {
	private static final String QUEST = "learn_scrying";
	private static final int COST = 5;

	private final int x;
	private final int y;
	private final boolean requiresScryingKnowledge;
	private final int cost;

	/**
	 * Create a new ViewChangeEntity using the traditional scrying requirements.
	 *
	 * @param x x coordinate of the view center
	 * @param y y coordinate of the view center
	 */
	public ViewChangeEntity(int x, int y) {
		this(x, y, true, COST);
	}

	private ViewChangeEntity(final int x, final int y,
			final boolean requiresScryingKnowledge, final int cost) {
		this.x = x;
		this.y = y;
		this.requiresScryingKnowledge = requiresScryingKnowledge;
		this.cost = cost;
		setResistance(0);
	}

	/**
	 * Creates a free observation point without quest or money requirements.
	 *
	 * @param x x coordinate of the view center
	 * @param y y coordinate of the view center
	 * @return unrestricted view-changing entity
	 */
	public static ViewChangeEntity unrestricted(final int x, final int y) {
		return new ViewChangeEntity(x, y, false, 0);
	}

	@Override
	public String describe() {
		if (!requiresScryingKnowledge && cost == 0) {
			return "Oto wróżąca kula. Użyj jej, aby zmienić punkt obserwacji.";
		}
		return "Oto wróżąca kula. Zapisane jest \"Użycie kosztuje " + cost
			+ " money. Stój w spokoju i skoncentruj się podczas oglądania\".";
	}

	@Override
	public boolean onUsed(RPEntity user) {
		if (!nextTo(user)) {
			user.sendPrivateText("Nie możesz stąd dosięgnąć.");
			return false;
		}
		if (user instanceof Player) {
			final Player player = (Player) user;
			if (requiresScryingKnowledge && !player.hasQuest(QUEST)) {
				player.sendPrivateText("Nie wiesz jak obsłużyć to dziwne urządzenie.");
				return false;
			}
			if (cost > 0 && !player.drop("money", cost)) {
				player.sendPrivateText("Nie posiadasz wystarczająco dużo money.");
				return false;
			}

			player.addEvent(new ViewChangeEvent(x, y));
			player.notifyWorldAboutChanges();
			return true;
		}
		return false;
	}
}
