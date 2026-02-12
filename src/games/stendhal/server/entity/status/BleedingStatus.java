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

import games.stendhal.server.entity.Killer;

public class BleedingStatus extends Status implements Killer {
	private static final int TREATMENT_STEP = 200;

	private BleedingSeverity severity;
	private int durationLeft;
	private int intervalCounter;
	private int maxDuration;

	public static BleedingStatus createFromAttackPower(int attackPower) {
		return new BleedingStatus(BleedingSeverity.fromAttackPower(attackPower));
	}

	public BleedingStatus(BleedingSeverity severity) {
		super("bleeding");
		this.severity = severity;
		resetCounters();
	}

	public BleedingStatus(int amount, int frequency, int regen) {
		super("bleeding");
		this.severity = translateLegacy(amount, frequency, regen);
		resetCounters();
	}

	private static BleedingSeverity translateLegacy(int amount, int frequency, int regen) {
		int damage = Math.abs(regen);
		if (damage >= BleedingSeverity.CRITICAL.getDamagePerTick()) {
			return BleedingSeverity.CRITICAL;
		}
		if (damage >= BleedingSeverity.SEVERE.getDamagePerTick()) {
			return BleedingSeverity.SEVERE;
		}
		if (damage >= BleedingSeverity.MODERATE.getDamagePerTick()) {
			return BleedingSeverity.MODERATE;
		}
		if (damage > 0) {
			return BleedingSeverity.MINOR;
		}
		return BleedingSeverity.NONE;
	}

	private void resetCounters() {
		if (severity.isNone()) {
			durationLeft = 0;
			intervalCounter = 0;
			maxDuration = 0;
		} else {
			durationLeft = severity.getBaseDuration();
			intervalCounter = severity.getInterval();
			maxDuration = severity.getMaxDuration();
		}
	}

	public BleedingSeverity getSeverity() {
		return severity;
	}

	public boolean isActive() {
		return !severity.isNone();
	}

	public int getClientIntensity() {
		return severity.getClientIntensity();
	}

	public void merge(BleedingStatus other) {
		if ((other == null) || !other.isActive()) {
			return;
		}
		if (!isActive()) {
			severity = other.severity;
			resetCounters();
			return;
		}

		if (other.severity.ordinal() > severity.ordinal()) {
			severity = other.severity;
			resetCounters();
		} else if (other.severity.ordinal() == severity.ordinal()) {
			durationLeft = Math.min(maxDuration, durationLeft + other.severity.getRefreshDuration());
		} else {
			durationLeft = Math.min(maxDuration, durationLeft + Math.max(1, other.severity.getRefreshDuration() / 2));
		}

		if ((intervalCounter <= 0) || (intervalCounter > severity.getInterval())) {
			intervalCounter = severity.getInterval();
		}
	}

	public void applyResistance(double resistance) {
		if (!isActive() || (resistance <= 0.0)) {
			return;
		}

		int reductions = (int) Math.floor(resistance * BleedingSeverity.maxRank());
		while ((reductions > 0) && isActive()) {
			downgrade();
			reductions--;
		}

		if (!isActive()) {
			return;
		}

		double factor = 1.0 - Math.min(resistance, 0.9);
		durationLeft = Math.max(1, (int) Math.ceil(durationLeft * factor));
		maxDuration = Math.max(durationLeft, Math.min(maxDuration, severity.getMaxDuration()));
		if ((intervalCounter <= 0) || (intervalCounter > severity.getInterval())) {
			intervalCounter = severity.getInterval();
		}
	}

	public int processTurn() {
		if (!isActive()) {
			return 0;
		}

		intervalCounter--;
		if (intervalCounter > 0) {
			return 0;
		}

		intervalCounter = severity.getInterval();
		durationLeft = Math.max(0, durationLeft - severity.getInterval());
		int damage = severity.getDamagePerTick();

		if (durationLeft <= 0) {
			downgrade();
		}

		return damage;
	}

	public boolean applyTreatment(int potency) {
		if (!isActive() || (potency <= 0)) {
			return false;
		}

		int steps = Math.max(1, potency / TREATMENT_STEP);
		boolean changed = false;
		while ((steps > 0) && isActive()) {
			downgrade();
			changed = true;
			steps--;
		}
		return changed;
	}

	private void downgrade() {
		severity = severity.downgrade();
		resetCounters();
	}

	@Override
	public StatusType getStatusType() {
		return StatusType.BLEEDING;
	}
}
