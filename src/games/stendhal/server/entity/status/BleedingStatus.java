package games.stendhal.server.entity.status;

import games.stendhal.server.entity.Killer;

public class BleedingStatus extends ConsumableStatus implements Killer {
	/**
	 * Bleeding
	 *
	 * @param amount     total amount
	 * @param frequency  frequency of events
	 * @param regen      hp change on each event
	 */
	public BleedingStatus(int amount, int frequency, int regen) {
		super("bleeding", amount, frequency, -regen);
	}

	@Override
	public StatusType getStatusType() {
		return StatusType.BLEEDING;
	}
}
