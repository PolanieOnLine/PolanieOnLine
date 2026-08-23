/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.server.entity.creature.Creature;

/** Applies the extra final-encounter layer used by the two highest tiers. */
public final class ChallengeArenaChampionService {
	private ChallengeArenaChampionService() {
	}

	public static void promote(final Creature creature,
			final ChallengeArenaTier tier) {
		if (creature == null || tier == null
				|| tier.getChampionHpMultiplier() <= 1.0) {
			return;
		}

		final int hp = scalePositive(creature.getBaseHP(),
				tier.getChampionHpMultiplier());
		creature.setBaseHP(hp);
		creature.setHP(hp);
		creature.setAtk(scaleCombat(creature.getAtk(),
				tier.getChampionAttackMultiplier()));
		if (creature.getRatk() > 0) {
			creature.setRatk(scaleCombat(creature.getRatk(),
					tier.getChampionAttackMultiplier()));
		}
		creature.setDef(scaleCombat(creature.getDef(),
				tier.getChampionDefenseMultiplier()));

		final String currentTitle = creature.has("title")
				? creature.get("title") : creature.getName();
		creature.put("title", "Czempion Areny " + currentTitle);
	}

	private static int scalePositive(final int value, final double multiplier) {
		if (value <= 0) {
			return value;
		}
		return Math.max(value + 1,
				(int) Math.round(value * multiplier + 0.000000001));
	}

	private static int scaleCombat(final int value, final double multiplier) {
		return Math.min(Short.MAX_VALUE, scalePositive(value, multiplier));
	}
}
