/***************************************************************************
 *                 (C) Copyright 2019-2021 - PolanieOnLine                 *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.status;

import java.util.LinkedList;
import java.util.List;

import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.RPEntity;

public class BleedingStatusTurnListener implements TurnListener {
	private StatusList statusList;
	private static final String ATTRIBUTE_NAME = "wounded";

	public BleedingStatusTurnListener(StatusList statusList) {
		this.statusList = statusList;
	}

	@Override
	public void onTurnReached(int turn) {
		RPEntity entity = statusList.getEntity();
		if (entity == null) {
			return;
		}

		List<BleedingStatus> toConsume = statusList.getAllStatusByClass(BleedingStatus.class);
		if (toConsume.isEmpty()) {
			if (entity.has(ATTRIBUTE_NAME)) {
				entity.remove(ATTRIBUTE_NAME);
				entity.notifyWorldAboutChanges();
			}
			return;
		}

		List<ConsumableStatus> toRemove = new LinkedList<ConsumableStatus>();
		int sum = 0;
		int amount = 0;
		for (final BleedingStatus bleeding : toConsume) {
			if (turn % bleeding.getFrecuency() == 0) {
				if (bleeding.consumed()) {
					toRemove.add(bleeding);
				} else {
					amount = bleeding.consume();
					entity.damage(-amount, bleeding);
					sum += amount;
					entity.put(ATTRIBUTE_NAME, sum);
				}
				entity.notifyWorldAboutChanges();
			}
		}

		for (final ConsumableStatus consumable : toRemove) {
			statusList.remove(consumable);
		}
		TurnNotifier.get().notifyInTurns(0, this);
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
