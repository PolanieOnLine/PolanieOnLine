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

import java.util.List;

import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.events.TutorialNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.player.Player;

/** Handles stackable physical bleeding wounds. */
public class BleedingStatusHandler implements StatusHandler<BleedingStatus> {
	public static final int MAX_STACKS = 3;
	private static final String ATTRIBUTE_NAME = "bleeding";

	@Override
	public void inflict(final BleedingStatus status, final StatusList statusList,
			final Entity attacker) {
		final RPEntity entity = statusList.getEntity();
		if (entity == null) {
			return;
		}

		if (!addOrReplaceWound(status, statusList)) {
			return;
		}

		status.markApplied();
		statusList.activateStatusAttribute(ATTRIBUTE_NAME);
		if (entity instanceof Player) {
			TutorialNotifier.bleeding((Player) entity);
		}
		schedule(statusList, status);
	}

	/**
	 * Adds a wound up to the stack cap. At the cap, a stronger incoming wound
	 * replaces the weakest remaining wound; weaker/equal wounds are ignored.
	 */
	static boolean addOrReplaceWound(final BleedingStatus incoming,
			final StatusList statusList) {
		final List<BleedingStatus> wounds = statusList
				.getAllStatusByClass(BleedingStatus.class);
		if (wounds.size() < MAX_STACKS) {
			statusList.addInternal(incoming);
			return true;
		}

		BleedingStatus weakest = null;
		for (final BleedingStatus wound : wounds) {
			if (weakest == null || wound.getRemainingDamage()
					< weakest.getRemainingDamage()) {
				weakest = wound;
			}
		}
		if (weakest == null || incoming.getRemainingDamage()
				<= weakest.getRemainingDamage()) {
			return false;
		}

		// Remove internally so the shared client attribute is not cleared during
		// the atomic replacement. The old per-wound listener will later no-op.
		statusList.removeInternal(weakest);
		statusList.addInternal(incoming);
		return true;
	}

	static void schedule(final StatusList statusList,
			final BleedingStatus status) {
		// TurnNotifier adds one turn internally. Subtract one so the configured
		// interval is the actual number of turn boundaries between ticks.
		final int notifierDelay = Math.max(0, status.getTickIntervalTurns() - 1);
		TurnNotifier.get().notifyInTurns(notifierDelay,
				new BleedingStatusTurnListener(statusList, status));
	}

	@Override
	public void remove(final BleedingStatus status, final StatusList statusList) {
		statusList.removeInternal(status);
		final RPEntity entity = statusList.getEntity();
		if (entity != null
				&& statusList.countStatusByType(StatusType.BLEEDING) == 0
				&& entity.has(ATTRIBUTE_NAME)) {
			entity.remove(ATTRIBUTE_NAME);
			entity.notifyWorldAboutChanges();
		}
	}
}
