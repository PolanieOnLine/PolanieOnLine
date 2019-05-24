/***************************************************************************
 *                   (C) Copyright 2003-2018 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.client.gui.j2d.entity;

import java.util.List;

import games.stendhal.client.entity.ActionType;
import games.stendhal.client.entity.Goat;
import games.stendhal.client.entity.User;

/**
 * The 2D view of a goat.
 */
class Goat2DView extends DomesticAnimal2DView<Goat> {
	/**
	 * The weight that a goat becomes big.
	 */
	private static final int BIG_WEIGHT = 60;

	/**
	 * Get the weight at which the animal becomes big.
	 *
	 * @return A weight.
	 */
	@Override
	protected int getBigWeight() {
		return BIG_WEIGHT;
	}

	/**
	 * Build a list of entity specific actions. <strong>NOTE: The first entry
	 * should be the default.</strong>
	 *
	 * @param list
	 *            The list to populate.
	 */
	@Override
	protected void buildActions(final List<String> list) {
		super.buildActions(list);
		User user = User.get();
		Goat goat = entity;

		if (user != null) {
			if (!user.hasGoat()) {
				list.add(ActionType.OWN.getRepresentation());
			} else if ((goat != null) && (user.getGoatID() == goat.getID()
					.getObjectID())) {
				list.add(ActionType.LEAVE_GOAT.getRepresentation());
			}
		}
	}

	/**
	 * Perform an action.
	 *
	 * @param at
	 *            The action.
	 */
	@Override
	public void onAction(final ActionType at) {
		if (isReleased()) {
			return;
		}
		switch (at) {
		case LEAVE_GOAT:
			at.send(at.fillTargetInfo(entity));
			break;

		default:
			super.onAction(at);
			break;
		}
	}
}
