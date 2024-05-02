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
		super("wounded", amount, frequency, regen);
	}

	@Override
	public StatusType getStatusType() {
		return StatusType.BLEEDING;
	}
}
