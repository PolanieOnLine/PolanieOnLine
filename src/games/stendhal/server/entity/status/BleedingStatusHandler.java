package games.stendhal.server.entity.status;

import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;

public class BleedingStatusHandler implements StatusHandler<BleedingStatus> {
	/**
	 * inflicts a status
	 *
	 * @param status Status to inflict
	 * @param statusList StatusList
	 * @param attacker the attacker
	 */
	@Override
	public void inflict(BleedingStatus status, StatusList statusList, Entity attacker) {
		RPEntity entity = statusList.getEntity();
		if (entity == null) {
			return;
		}

		int count = statusList.countStatusByType(status.getStatusType());
		if (count <= 6) {
			statusList.addInternal(status);
		}

		statusList.activateStatusAttribute("bleeding");

		// activate the turnListener, if this is the first instance of this status
		// note: the turnListener is called one last time after the last instance was comsumed to cleanup attributes.
		// So even with count==0, there might still be a listener which needs to be removed
		if (count == 0) {
			TurnListener turnListener = new BleedingStatusTurnListener(statusList);
			TurnNotifier.get().dontNotify(turnListener);
			TurnNotifier.get().notifyInTurns(0, turnListener);
		}
	}

	/**
	 * removes a status
	 *
	 * @param status Status to inflict
	 * @param statusList StatusList
	 */
	@Override
	public void remove(BleedingStatus status, StatusList statusList) {
		statusList.removeInternal(status);
	}
}
