/***************************************************************************
 *                (C) Copyright 2003-2013 - Faiumoni e. V.                 *
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

import java.lang.ref.WeakReference;

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.Killer;

/**
 * poison status
 *
 * Note: this class has a natural ordering that is inconsistent with equals.
 */
public class PoisonStatus extends ConsumableStatus implements Killer {
	private final WeakReference<Entity> source;

	/**
	 * Poison
	 *
	 * @param amount     total amount
	 * @param frequency  frequency of events
	 * @param regen      hp change on each event
	 */
	public PoisonStatus(int amount, int frequency, int regen) {
		this(amount, frequency, regen, null);
	}

	public PoisonStatus(final int amount, final int frequency, final int regen, final Entity source) {
		super("poison", amount, frequency, regen);
		this.source = new WeakReference<>(source);
	}

	public Entity getSource() {
		return source.get();
	}

	/**
	 * returns the status type
	 *
	 * @return StatusType
	 */
	@Override
	public StatusType getStatusType() {
		return StatusType.POISONED;
	}

}
