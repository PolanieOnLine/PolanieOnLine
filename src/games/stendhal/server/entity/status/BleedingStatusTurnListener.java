/***************************************************************************
 *                 (C) Copyright 2019-2026 - PolanieOnLine                 *
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

import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;

/** Runs one wound on its own cadence. */
public class BleedingStatusTurnListener implements TurnListener {
	private static final String ATTRIBUTE_NAME = "bleeding";

	private final StatusList statusList;
	private final BleedingStatus bleeding;

	public BleedingStatusTurnListener(final StatusList statusList,
			final BleedingStatus bleeding) {
		this.statusList = statusList;
		this.bleeding = bleeding;
	}

	@Override
	public void onTurnReached(final int turn) {
		final RPEntity entity = statusList.getEntity();
		if (entity == null || !statusList.getStatuses().contains(bleeding)) {
			return;
		}

		if (bleeding.isConsumed() || entity.getHP() <= 0) {
			statusList.remove(bleeding);
			return;
		}

		final int woundDamage = bleeding.consumeNextTick();
		final int damage = Math.min(woundDamage, entity.getHP());
		if (damage > 0) {
			// Preserve the old signed client attribute convention while damage and
			// wound internals use positive values.
			entity.put(ATTRIBUTE_NAME, -damage);
			final Entity source = bleeding.getSource();
			if (source != null) {
				// onDamaged expects its caller to cap damage at remaining HP. Doing
				// that here also keeps damage contribution and XP attribution exact.
				entity.onDamaged(source, damage);
			} else {
				// Compatibility fallback for legacy/environmental bleeding callers.
				entity.damage(damage, bleeding);
			}
		}

		if (!statusList.getStatuses().contains(bleeding)) {
			return;
		}
		if (bleeding.isConsumed() || entity.getHP() <= 0) {
			statusList.remove(bleeding);
			return;
		}

		entity.notifyWorldAboutChanges();
		BleedingStatusHandler.schedule(statusList, bleeding);
	}
}
