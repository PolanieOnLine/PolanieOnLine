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
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.RPEntity;

public class BloodAttacker extends StatusAttacker {
	private final int attackPower;

	public BloodAttacker(final int probability, final int atk) {
		super(BleedingStatus.createFromAttackPower(atk), probability);
		this.attackPower = atk;
	}

	@Override
	public void onAttackAttempt(RPEntity target, RPEntity attacker) {
		double myProbability = getProbability();
		int roll = Rand.roll1D100();
		if (roll <= myProbability) {
			BleedingStatus inflicted = BleedingStatus.createFromAttackPower(attackPower);
			if (target.getStatusList().inflictStatus(inflicted, attacker)) {
				new GameEvent(attacker.getName(), "bleeding", target.getName()).raise();
				target.sendPrivateText(target.getGenderVerb("Zostałeś") + " "
				+ describeSeverity(inflicted.getSeverity()) + " "
				+ target.getGenderVerb("zraniony") + " przez " + attacker.getName() + ".");
			}
		}
	}

	private String describeSeverity(BleedingSeverity severity) {
		switch (severity) {
			case CRITICAL:
			return "śmiertelnie";
			case SEVERE:
			return "ciężko";
			case MODERATE:
			return "mocno";
			case MINOR:
			default:
			return "lekko";
		}
	}

	@Override
	public void onHit(RPEntity target, RPEntity attacker, int damage) {
		// do nothing, especially do not process the logic of the super class
	}
}
