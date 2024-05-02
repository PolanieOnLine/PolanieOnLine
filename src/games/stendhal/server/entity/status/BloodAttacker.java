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

import games.stendhal.common.Rand;
import games.stendhal.common.grammar.Grammar;
import games.stendhal.server.core.engine.GameEvent;
import games.stendhal.server.entity.RPEntity;

public class BloodAttacker extends StatusAttacker {
	/**
	 * BloodAttacker
	 *
	 * @param probability probability
	 * First amount
	 * Second frequency
	 * Third -regen
	 */
	public BloodAttacker(final int probability, final int atk) {
		super(new BleedingStatus(howMuchReduceHealth(atk), Rand.roll1D20(), reduceHealth(atk)), probability);
	}

	static int howMuchReduceHealth(final int atk) {
		int reduce = Rand.roll1D1000();
		if (atk < 600) {
			reduce = Rand.roll1D50();
		} else if (atk < 1000) {
			reduce = Rand.roll1D100();
		} else if (atk < 1600) {
			reduce = Rand.roll1D200() * 2;
		}
		return -reduce;
	}

	static int reduceHealth(final int atk) {
		int reduce = Rand.roll1D200();
		if (atk < 400) {
			reduce = Rand.roll1D10();
		} else if (atk < 1200) {
			reduce = Rand.roll1D20() * 2;
		} else if (atk < 1600) {
			reduce = Rand.roll1D100();
		}
		return -reduce;
	}

	@Override
	public void onAttackAttempt(RPEntity target, RPEntity attacker) {
		double myProbability = getProbability();

		final int roll = Rand.roll1D100();
		if (roll <= myProbability) {
			if (target.getStatusList().inflictStatus((Status) getStatus().clone(), attacker)) {
				new GameEvent(attacker.getName(), "bleeding", target.getName()).raise();
				target.sendPrivateText(target.getGenderVerb("Zostałeś") + " znacznie " + target.getGenderVerb("zraniony") + " przez " + attacker.getName() + ".");
			}
		}
	}

	@Override
	public void onHit(RPEntity target, RPEntity attacker, int damage) {
		// do nothing, especially do not process the logic of the super class
	}
}
