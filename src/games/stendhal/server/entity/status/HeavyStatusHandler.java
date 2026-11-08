/***************************************************************************
 *                (C) Copyright 2003-2014 - Faiumoni e. V.                 *
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

import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.common.NotificationType;
import games.stendhal.common.Rand;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.rule.damage.TacticalAffixCombatService;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;

/**
 * Handles HeavyStatus
 */
public class HeavyStatusHandler implements StatusHandler<HeavyStatus> {
	private final Map<StatusList, SlowState> active = new WeakHashMap<>();

	private static final class SlowState {
		final double originalSpeed;
		final int duration;

		SlowState(final double originalSpeed, final int duration) {
			this.originalSpeed = originalSpeed;
			this.duration = duration;
		}
	}

	/**
	 * @param status
	 * 		Status to inflict
	 * @param statusList
	 * 		StatusList
	 * @param attacker
	 * 		the attacker
	 */
	@Override
	public void inflict(HeavyStatus status, StatusList statusList, Entity attacker) {
		if (!statusList.hasStatus(status.getStatusType())) {
			RPEntity entity = statusList.getEntity();
			if (entity != null) {
				/* save the entity's original speed to be replaced later */
				final double originalSpeed = entity.getBaseSpeed();
				/* slow the entity down to half walking speed */
				entity.setBaseSpeed(originalSpeed / 2);

				/* Legendary combat procs may request a short fixed duration. */
				final int duration = duration(status);

				/* status was not inflicted by another entity */
				if (attacker == null) {
					entity.sendPrivateText(NotificationType.SCENE_SETTING,
							"Twoje stopy zaczynają być ciężkie. Jesteś ociężały.");
				} else {
					entity.sendPrivateText(NotificationType.SCENE_SETTING,
							"Twoje stopy zaczynają być ciężkie. Zostałeś ociężały przez "
					+ attacker.getName() + ".");
				}

				active.put(statusList, new SlowState(originalSpeed, duration));
				statusList.addInternal(status);
				statusList.activateStatusAttribute("status_" + status.getName());
				TurnNotifier.get().notifyInTurns(TacticalAffixCombatService.statusDurationTurns(entity, duration),
						new StatusRemover(statusList, status));
			}
		}
	}

	/**
	 * removes a status
	 *
	 * @param status
	 * 		inflicted status
	 * @param statusList
	 * 		StatusList
	 */
	@Override
	public void remove(HeavyStatus status, StatusList statusList) {
		if (!statusList.getStatuses().contains(status)) {
			return;
		}
		statusList.removeInternal(status);

		RPEntity entity = statusList.getEntity();
		if (entity == null) {
			return;
		}

		Status nextStatus = statusList.getFirstStatusByClass(HeavyStatus.class);
		final SlowState state = active.get(statusList);
		if (nextStatus != null) {
			final HeavyStatus nextHeavy = (HeavyStatus) nextStatus;
			final int nextDuration;
			if (nextHeavy.getDurationSeconds() == null && state != null) {
				nextDuration = state.duration;
			} else {
				nextDuration = duration(nextHeavy);
			}
			TurnNotifier.get().notifyInTurns(TacticalAffixCombatService.statusDurationTurns(entity, nextDuration),
					new StatusRemover(statusList, nextStatus));
		} else {
			active.remove(statusList);
			if (state != null) {
				entity.setBaseSpeed(state.originalSpeed);
			}
			entity.remove("status_" + status.getName());
			entity.sendPrivateText(NotificationType.SCENE_SETTING, "Już nie jesteś przeciążony.");
		}
	}

	private int duration(final HeavyStatus status) {
		if (status.getDurationSeconds() != null) {
			return status.getDurationSeconds().intValue();
		}
		// Preserve the legacy random timing.
		final Double minimum = 3.3 * 30;
		final Double maximum = 3.3 * 300;
		return Rand.randUniform(minimum.intValue(), maximum.intValue());
	}
}
