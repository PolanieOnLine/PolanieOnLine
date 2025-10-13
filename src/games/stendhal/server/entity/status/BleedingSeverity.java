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

import games.stendhal.common.Rand;

/**
 * Severity tiers for bleeding.
 */
public enum BleedingSeverity {
	NONE(0, 0, 0, 0, 0),
	MINOR(1, 20, 4, 2, 40),
	MODERATE(2, 18, 3, 4, 36),
	SEVERE(3, 14, 2, 6, 28),
	CRITICAL(4, 12, 1, 8, 24);

	private final int clientIntensity;
	private final int baseDuration;
	private final int interval;
	private final int damagePerTick;
	private final int maxDuration;

	BleedingSeverity(int clientIntensity, int baseDuration, int interval, int damagePerTick, int maxDuration) {
		this.clientIntensity = clientIntensity;
		this.baseDuration = baseDuration;
		this.interval = interval;
		this.damagePerTick = damagePerTick;
		this.maxDuration = maxDuration;
	}

	public int getClientIntensity() {
		return clientIntensity;
	}

	public int getBaseDuration() {
		return baseDuration;
	}

	public int getInterval() {
		return interval;
	}

	public int getDamagePerTick() {
		return damagePerTick;
	}

	public int getMaxDuration() {
		return maxDuration;
	}

	public int getRefreshDuration() {
		return Math.max(1, baseDuration / 2);
	}

	public boolean isNone() {
		return this == NONE;
	}

	public BleedingSeverity downgrade() {
		if (isNone()) {
			return NONE;
		}
		return values()[ordinal() - 1];
	}

	public BleedingSeverity escalate() {
		if (this == values()[values().length - 1]) {
			return this;
		}
		return values()[ordinal() + 1];
	}

	public static int maxRank() {
		return values().length - 1;
	}

	public static BleedingSeverity fromAttackPower(int attack) {
		BleedingSeverity base;
		if (attack < 600) {
			base = MINOR;
		} else if (attack < 1000) {
			base = MODERATE;
		} else if (attack < 1600) {
			base = SEVERE;
		} else {
			base = CRITICAL;
		}

		int roll = Rand.roll1D100();
		if (roll > 92) {
			base = base.escalate();
		} else if (roll < 18) {
			base = base.downgrade();
		}
		return base;
	}
}
