/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.glyph;

import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/**
 * Centralizes the interpretation of legacy glyph attributes.
 *
 * Storage/wire keys intentionally remain unchanged so existing persisted glyphs
 * do not require a migration. Method names describe the gameplay semantics
 * instead of repeating historical attribute names where those names are
 * misleading (for example {@code rate_increase} actually reduces attack rate).
 */
public final class GlyphEffectService {
	public static final String SKILL_ATTACK_ATTRIBUTE = "skill_atk";
	public static final String HEALTH_ATTRIBUTE = "health";
	public static final String ATTACK_PERCENT_ATTRIBUTE = "atk_additional_bonus";
	public static final String LIFESTEAL_PERCENT_ATTRIBUTE = "lifesteal_increase";
	public static final String LEGACY_ATTACK_RATE_REDUCTION_ATTRIBUTE = "rate_increase";

	private GlyphEffectService() {
		// utility class
	}

	/**
	 * Returns the flat attack-skill bonus from all currently equipped glyphs.
	 */
	public static int getSkillAttackBonus(final RPEntity entity) {
		return getIntegralBonus(entity, SKILL_ATTACK_ATTRIBUTE);
	}

	/**
	 * Returns the maximum-health bonus from all currently equipped glyphs.
	 */
	public static int getHealthBonus(final RPEntity entity) {
		return getIntegralBonus(entity, HEALTH_ATTRIBUTE);
	}

	/**
	 * Returns the additive equipment-attack percentage as a fraction.
	 * A stored value of {@code 10.0} therefore returns {@code 0.10}.
	 */
	public static double getAttackPercentBonusFraction(final RPEntity entity) {
		return getDecimalBonus(entity, ATTACK_PERCENT_ATTRIBUTE) / 100.0;
	}

	/**
	 * Returns glyph-granted lifesteal as a fraction of damage dealt.
	 * A stored value of {@code 20.0} therefore returns {@code 0.20}.
	 */
	public static double getLifestealBonusFraction(final RPEntity entity) {
		return getDecimalBonus(entity, LIFESTEAL_PERCENT_ATTRIBUTE) / 100.0;
	}

	/**
	 * Returns the numeric attack-rate reduction supplied by glyphs.
	 *
	 * The persisted attribute is historically called {@code rate_increase}, but
	 * lower attack-rate values are faster and the value is subtracted from the
	 * weapon rate. The semantic name here prevents new code from propagating that
	 * misleading terminology.
	 */
	public static double getAttackRateReduction(final RPEntity entity) {
		return getDecimalBonus(entity, LEGACY_ATTACK_RATE_REDUCTION_ATTRIBUTE);
	}

	/**
	 * Applies only the stateful maximum-health part of a newly equipped glyph.
	 * Attack-skill bonuses are deliberately not persisted into Player.atk; they
	 * are resolved dynamically by DressedEntity.
	 */
	public static void applyHealthOnEquipped(final Player player, final Item glyph) {
		final int bonus = getIntegralAttribute(glyph, HEALTH_ATTRIBUTE);
		if (bonus == 0) {
			return;
		}

		final int newBaseHP = Math.max(0, player.getBaseHP() + bonus);
		player.setBaseHP(newBaseHP);
		if (player.getHP() > newBaseHP) {
			player.setHP(newBaseHP);
		}
	}

	/**
	 * Removes the stateful maximum-health part of an unequipped glyph and keeps
	 * current HP inside the new maximum. If a negative health modifier is removed
	 * while the player was at full health, the previous behavior of moving the
	 * current HP to the restored maximum is preserved.
	 */
	public static void applyHealthOnUnequipped(final Player player, final Item glyph) {
		final int bonus = getIntegralAttribute(glyph, HEALTH_ATTRIBUTE);
		if (bonus == 0) {
			return;
		}

		final int oldBaseHP = player.getBaseHP();
		final int newBaseHP = Math.max(0, oldBaseHP - bonus);
		final boolean wasAtFullHealth = player.getHP() == oldBaseHP;
		if (wasAtFullHealth || player.getHP() > newBaseHP) {
			player.setHP(newBaseHP);
		}
		player.setBaseHP(newBaseHP);
	}

	private static int getIntegralBonus(final RPEntity entity,
			final String attribute) {
		if (entity == null) {
			return 0;
		}

		int bonus = 0;
		for (final Item glyph : entity.getAllEquippedGlyphs()) {
			bonus += getIntegralAttribute(glyph, attribute);
		}
		return bonus;
	}

	private static double getDecimalBonus(final RPEntity entity,
			final String attribute) {
		if (entity == null) {
			return 0.0;
		}

		double bonus = 0.0;
		for (final Item glyph : entity.getAllEquippedGlyphs()) {
			if (glyph != null && glyph.has(attribute)) {
				final double value = glyph.getDouble(attribute);
				if (!Double.isNaN(value)) {
					bonus += value;
				}
			}
		}
		return bonus;
	}

	private static int getIntegralAttribute(final Item glyph,
			final String attribute) {
		return glyph != null && glyph.has(attribute) ? glyph.getInt(attribute) : 0;
	}
}
