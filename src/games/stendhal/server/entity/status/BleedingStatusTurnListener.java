/***************************************************************************
 *                 (C) Copyright 2019-2021 - PolanieOnLine                 *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.RPEntity;

public class BleedingStatusTurnListener implements TurnListener {
	private StatusList statusList;
	private static final String ATTRIBUTE_NAME = "bleeding";

	public BleedingStatusTurnListener(StatusList statusList) {
		this.statusList = statusList;
	}

	@Override
	public void onTurnReached(int turn) {
		RPEntity entity = statusList.getEntity();
		if (entity == null) {
			return;
		}

		BleedingStatus bleeding = statusList.getFirstStatusByClass(BleedingStatus.class);
		if ((bleeding == null) || !bleeding.isActive()) {
			if (clearAttribute(entity)) {
				entity.notifyWorldAboutChanges();
			}
			return;
		}

		int damage = bleeding.processTurn();
		boolean dirty = false;

		if (damage > 0) {
			entity.damage(damage, bleeding);
			entity.put(ATTRIBUTE_NAME, bleeding.getClientIntensity());
			dirty = true;
		}

		if (!bleeding.isActive()) {
			statusList.remove(bleeding);
			dirty |= clearAttribute(entity);
		} else if (damage > 0) {
			// ensure client gets updated intensity after severity changes
			dirty = true;
		}

		if (dirty) {
			entity.notifyWorldAboutChanges();
		}

		TurnNotifier.get().notifyInTurns(0, this);
	}

	private boolean clearAttribute(RPEntity entity) {
		if (entity.has(ATTRIBUTE_NAME)) {
			entity.remove(ATTRIBUTE_NAME);
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return statusList.hashCode() * 31;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		BleedingStatusTurnListener other = (BleedingStatusTurnListener) obj;
		return statusList.equals(other.statusList);
	}
}
