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
	public BloodAttacker(final int probability) {
		super(new BleedingStatus(Rand.roll1D200(), Rand.roll1D20(), Rand.roll1D50()), probability);
	}

	@Override
	public void onAttackAttempt(RPEntity target, RPEntity attacker) {
		double myProbability = getProbability();

		final int roll = Rand.roll1D100();
		if (roll <= myProbability) {
			if (target.getStatusList().inflictStatus((Status) getStatus().clone(), attacker)) {
				new GameEvent(attacker.getName(), "bleeding", target.getName()).raise();
				target.sendPrivateText(Grammar.genderVerb(target.getGender(), "Zostałeś") + " mocno " + Grammar.genderVerb(target.getGender(), "zraniony") + " przez " + attacker.getName() + ".");
			}
		}
	}

	@Override
	public void onHit(RPEntity target, RPEntity attacker, int damage) {
		// do nothing, especially do not process the logic of the super class
	}
}
