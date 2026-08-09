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

import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.Killer;

/**
 * One physical bleeding wound.
 *
 * <p>A wound owns its remaining damage, tick cadence and original damage
 * source. Multiple wounds are managed by {@link BleedingStatusHandler}; this
 * class deliberately does not reuse {@link ConsumableStatus}, because bleeding
 * needs deterministic per-wound timing and source attribution.</p>
 */
public class BleedingStatus extends Status implements Killer {
	private final int totalDamage;
	private final int tickIntervalTurns;
	private final Entity source;
	private int remainingDamage;
	private int ticksRemaining;
	private boolean applied;

	/**
	 * Creates one bleeding wound.
	 *
	 * @param totalDamage total positive damage dealt by the wound
	 * @param ticks number of damage ticks
	 * @param tickIntervalTurns turns between consecutive ticks
	 * @param source entity that caused the wound, or {@code null} for a legacy
	 * 		environmental source
	 */
	public BleedingStatus(final int totalDamage, final int ticks,
			final int tickIntervalTurns, final Entity source) {
		super("bleeding");
		if (totalDamage <= 0) {
			throw new IllegalArgumentException("Bleeding damage must be positive");
		}
		if (ticks <= 0) {
			throw new IllegalArgumentException("Bleeding ticks must be positive");
		}
		if (tickIntervalTurns <= 0) {
			throw new IllegalArgumentException("Bleeding tick interval must be positive");
		}
		this.totalDamage = totalDamage;
		this.remainingDamage = totalDamage;
		this.ticksRemaining = ticks;
		this.tickIntervalTurns = tickIntervalTurns;
		this.source = source;
	}

	/**
	 * Compatibility constructor for older callers that used the poison-like
	 * amount/frequency/regen representation. New code should use the explicit
	 * wound constructor above.
	 *
	 * @param amount old signed total amount
	 * @param frequency old tick frequency
	 * @param regen old signed damage per tick
	 */
	@Deprecated
	public BleedingStatus(final int amount, final int frequency, final int regen) {
		this(Math.max(1, Math.abs(amount)), legacyTickCount(amount, regen),
				Math.max(1, frequency), null);
	}

	private static int legacyTickCount(final int amount, final int regen) {
		final int total = Math.max(1, Math.abs(amount));
		final int perTick = Math.max(1, Math.abs(regen));
		return Math.max(1, (int) Math.ceil(total / (double) perTick));
	}

	/**
	 * Consumes the next deterministic damage tick. Remainders are distributed
	 * across the remaining ticks, so all ticks always add up to totalDamage.
	 *
	 * @return positive damage for this tick, or 0 when already consumed
	 */
	public int consumeNextTick() {
		if (isConsumed()) {
			return 0;
		}
		final int damage = (int) Math.ceil(remainingDamage
				/ (double) ticksRemaining);
		remainingDamage -= damage;
		ticksRemaining--;
		return damage;
	}

	public int getTotalDamage() {
		return totalDamage;
	}

	public int getRemainingDamage() {
		return remainingDamage;
	}

	public int getTicksRemaining() {
		return ticksRemaining;
	}

	public int getTickIntervalTurns() {
		return tickIntervalTurns;
	}

	public Entity getSource() {
		return source;
	}

	public boolean isConsumed() {
		return remainingDamage <= 0 || ticksRemaining <= 0;
	}

	void markApplied() {
		applied = true;
	}

	boolean wasApplied() {
		return applied;
	}

	@Override
	public StatusType getStatusType() {
		return StatusType.BLEEDING;
	}
}
