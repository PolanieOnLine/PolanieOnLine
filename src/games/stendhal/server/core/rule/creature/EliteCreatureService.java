/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.creature;

import games.stendhal.common.Rand;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.creature.Creature;

/**
 * Promotes a small share of normal respawned creatures to elite variants.
 *
 * <p>The service deliberately changes only the concrete creature instance.
 * The XML prototype and sprite sheet remain untouched, so the next respawn
 * gets its own independent elite roll.</p>
 */
public final class EliteCreatureService {
	public static final String ELITE_TITLE_TYPE = "elite";
	public static final int MINIMUM_ELITE_LEVEL = 20;
	public static final int ELITE_CHANCE_PER_TEN_THOUSAND = 300;
	public static final int ELITE_RARITY_ROLLS = 2;

	public static final double HP_MULTIPLIER = 1.40;
	public static final double ATTACK_MULTIPLIER = 1.15;
	public static final double DEFENSE_MULTIPLIER = 1.10;
	public static final double XP_MULTIPLIER = 1.50;

	private EliteCreatureService() {
	}

	/**
	 * Rolls and applies the elite variant when the creature is eligible.
	 *
	 * @param creature freshly-created respawn instance
	 * @return {@code true} when the creature became elite
	 */
	public static boolean maybePromote(final Creature creature) {
		if (!isEligible(creature)) {
			return false;
		}
		if (Rand.rand(10000) >= ELITE_CHANCE_PER_TEN_THOUSAND) {
			return false;
		}
		promote(creature);
		return true;
	}

	/** Returns whether the creature is already an elite instance. */
	public static boolean isElite(final Creature creature) {
		return creature != null && creature.has("title_type")
				&& ELITE_TITLE_TYPE.equals(creature.get("title_type"));
	}

	/**
	 * Returns the normal generated-drop context, upgraded with one additional
	 * independent rarity roll for elite creatures. The rarity service keeps the
	 * better tier, preserving any item-specific rarity profile and all normal
	 * affix generation rules.
	 */
	public static ItemCreationContext getDropCreationContext(final Creature creature) {
		if (!isElite(creature)) {
			return ItemCreationContext.drop();
		}
		return ItemCreationContext.builder(ItemCreationContext.Source.DROP)
				.withRarityRolls(ELITE_RARITY_ROLLS)
				.build();
	}

	/**
	 * Eligibility is intentionally conservative for v1. Tutorial-level,
	 * authored bosses and special hidden/immortal creatures are not promoted.
	 * A future creature may also opt out through the {@code no_elite} AI
	 * profile without changing this service.
	 */
	static boolean isEligible(final Creature creature) {
		if (creature == null || isElite(creature)
				|| creature.getLevel() < MINIMUM_ELITE_LEVEL) {
			return false;
		}
		if (creature.has("immortal") || creature.has("unnamed")
				|| creature.has("no_hpbar")) {
			return false;
		}
		if (creature.getAIProfiles().containsKey("boss")
				|| creature.getAIProfiles().containsKey("no_elite")) {
			return false;
		}
		return !creature.has("title_type")
				|| "enemy".equals(creature.get("title_type"));
	}

	/**
	 * Applies deterministic elite bonuses. Package visibility keeps a direct
	 * test seam without exposing a gameplay command that can force promotion.
	 */
	static void promote(final Creature creature) {
		if (!isEligible(creature)) {
			return;
		}

		final String baseTitle = creature.has("title")
				? creature.get("title") : creature.getName();
		creature.put("title_type", ELITE_TITLE_TYPE);
		creature.put("title", "Elitarny " + baseTitle);

		final int eliteHP = scalePositive(creature.getBaseHP(), HP_MULTIPLIER);
		creature.setBaseHP(eliteHP);
		creature.setHP(eliteHP);
		creature.setAtk(scalePositive(creature.getAtk(), ATTACK_MULTIPLIER));
		if (creature.getRatk() > 0) {
			creature.setRatk(scalePositive(creature.getRatk(), ATTACK_MULTIPLIER));
		}
		creature.setDef(scalePositive(creature.getDef(), DEFENSE_MULTIPLIER));
		creature.setXP(scalePositive(creature.getXP(), XP_MULTIPLIER));
	}

	private static int scalePositive(final int value, final double multiplier) {
		if (value <= 0) {
			return value;
		}
		return Math.max(value + 1,
				(int) Math.round(value * multiplier + 0.000000001));
	}
}
