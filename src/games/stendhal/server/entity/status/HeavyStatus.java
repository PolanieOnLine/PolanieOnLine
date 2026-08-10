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

/**
 * A status ailment that causes the entity to move more slowly
 */
public class HeavyStatus extends Status {
	private final Integer durationSeconds;

	/** Creates the legacy heavy status with its normal random duration. */
	public HeavyStatus() {
		super("heavy");
		this.durationSeconds = null;
	}

	/**
	 * Creates a heavy status with a fixed duration. This is used by short combat
	 * procs which must not inherit the legacy 30-second-to-five-minute duration.
	 *
	 * @param durationSeconds duration in seconds, must be positive
	 */
	public HeavyStatus(final int durationSeconds) {
		super("heavy");
		if (durationSeconds <= 0) {
			throw new IllegalArgumentException("Heavy duration must be positive");
		}
		this.durationSeconds = Integer.valueOf(durationSeconds);
	}

	/** @return fixed duration in seconds, or {@code null} for legacy random timing */
	public Integer getDurationSeconds() {
		return durationSeconds;
	}

	/**
	 * @return StatusType
	 */
	@Override
	public StatusType getStatusType() {
		return StatusType.HEAVY;
	}
}
