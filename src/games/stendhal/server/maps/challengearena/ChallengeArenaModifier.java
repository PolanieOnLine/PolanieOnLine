/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import games.stendhal.server.entity.creature.Creature;

/** Random combat modifiers used by the higher Challenge Arena tiers. */
public enum ChallengeArenaModifier {
	FURY("Szał") {
		@Override
		void apply(final Creature creature) {
			creature.setAtk(scaleCombat(creature.getAtk(), 1.12));
			if (creature.getRatk() > 0) {
				creature.setRatk(scaleCombat(creature.getRatk(), 1.12));
			}
		}
	},
	ENDURANCE("Wytrzymałość") {
		@Override
		void apply(final Creature creature) {
			final int hp = scalePositive(creature.getBaseHP(), 1.20);
			creature.setBaseHP(hp);
			creature.setHP(hp);
		}
	},
	IRON_SKIN("Twarda skóra") {
		@Override
		void apply(final Creature creature) {
			creature.setDef(scaleCombat(creature.getDef(), 1.15));
		}
	};

	private final String displayName;

	ChallengeArenaModifier(final String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	abstract void apply(Creature creature);

	static List<ChallengeArenaModifier> randomModifiers(final int count) {
		if (count <= 0) {
			return Collections.emptyList();
		}
		final List<ChallengeArenaModifier> available =
				new ArrayList<ChallengeArenaModifier>(Arrays.asList(values()));
		Collections.shuffle(available);
		return new ArrayList<ChallengeArenaModifier>(available.subList(0,
				Math.min(count, available.size())));
	}

	private static int scalePositive(final int value, final double multiplier) {
		if (value <= 0) {
			return value;
		}
		return Math.max(value + 1, (int) Math.round(value * multiplier));
	}

	private static int scaleCombat(final int value, final double multiplier) {
		return Math.min(Short.MAX_VALUE, scalePositive(value, multiplier));
	}
}
