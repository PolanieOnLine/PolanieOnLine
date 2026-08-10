/* $Id$ */
/***************************************************************************
 *                   (C) Copyright 2003-2010 - Stendhal                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.entity.creature.impl.attack;

import java.util.Map;

public class AttackStrategyFactory {
	private static final AttackStrategy HAND_TO_HAND = new HandToHand();
	private static final AttackStrategy COWARD = new Coward();
	private static final AttackStrategy STUPID_COWARD = new StupidCoward();
	private static final AttackStrategy GANDHI = new Gandhi();
	private static final AttackStrategy ATTACK_WEAKEST = new AttackWeakest();

	private static final AttackStrategy CAMOUFLAGED =
			new DecamouflageAttackStrategy(HAND_TO_HAND);

	public static AttackStrategy get(final Map<String, String> aiProfiles) {
		final AttackStrategy strategy;
		if (aiProfiles.containsKey("archer")) {
			strategy = new RangeAttack(aiProfiles.get("archer"));
		} else if (aiProfiles.containsKey("coward")) {
			strategy = COWARD;
		} else if (aiProfiles.containsKey("gandhi")) {
			strategy = GANDHI;
		} else if (aiProfiles.containsKey("stupid coward")) {
			strategy = STUPID_COWARD;
		} else if (aiProfiles.containsKey("attack weakest")) {
			strategy = ATTACK_WEAKEST;
		} else if (aiProfiles.containsKey("strategy")) {
			strategy = CompoundAttackStrategy.create(aiProfiles.get("strategy"));
		} else if (aiProfiles.containsKey("camouflage")) {
			strategy = CAMOUFLAGED;
		} else {
			strategy = HAND_TO_HAND;
		}

		// Apply the attack lock once at the outermost strategy. Positioning and
		// target selection remain delegated, so stun is not a movement/root effect.
		return new StunAwareAttackStrategy(strategy);
	}
}
