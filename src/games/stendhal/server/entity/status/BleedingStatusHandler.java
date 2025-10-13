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
import games.stendhal.server.core.events.TutorialNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

public class BleedingStatusHandler implements StatusHandler<BleedingStatus> {
	@Override
	public void inflict(BleedingStatus status, StatusList statusList, Entity attacker) {
		RPEntity entity = statusList.getEntity();
		if ((entity == null) || (status == null)) {
			return;
		}

		double resistance = entity.has("resist_bleeding") ? entity.getDouble("resist_bleeding") : 0.0;
		status.applyResistance(resistance);
		if (!status.isActive()) {
			return;
		}

		BleedingStatus current = statusList.getFirstStatusByClass(BleedingStatus.class);
		boolean firstInstance = current == null;

		if (firstInstance) {
			statusList.addInternal(status);
			current = status;
			statusList.activateStatusAttribute("bleeding");
		} else {
			current.merge(status);
		}

		updateAttribute(entity, current);

		if (entity instanceof Player) {
			TutorialNotifier.bleeding((Player) entity);
		}

		if (firstInstance) {
			TurnListener turnListener = new BleedingStatusTurnListener(statusList);
			TurnNotifier.get().dontNotify(turnListener);
			TurnNotifier.get().notifyInTurns(0, turnListener);
		}
	}

	private void updateAttribute(RPEntity entity, BleedingStatus status) {
		if (!status.isActive()) {
			if (entity.has("bleeding")) {
				entity.remove("bleeding");
				entity.notifyWorldAboutChanges();
			}
			return;
		}
		entity.put("bleeding", status.getClientIntensity());
		entity.notifyWorldAboutChanges();
	}

	@Override
	public void remove(BleedingStatus status, StatusList statusList) {
		statusList.removeInternal(status);
	}
}
