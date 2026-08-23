/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.server.core.rule.rarity.EquipmentAffixService;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Runtime behavior of tactical defensive equipment affixes. */
public final class EquipmentAffixCombatService {
	/** Hunter's Mark softens only an unfavorable semantic armor matchup. */
	public static final double HUNTER_MARK_ARMOR_PENETRATION = 0.05;
	/** Marks expire quickly so switching attackers matters during group fights. */
	public static final long HUNTER_MARK_DURATION_MILLISECONDS = 6000L;
	/** Several spiked pieces may help, but a full set cannot reflect without bound. */
	public static final double MAX_SPIKED_REFLECTION = 0.10;
	public static final int GIANT_SLAYER_LEVEL_STEP = 50;
	public static final double GIANT_SLAYER_PENETRATION_PER_STEP = 0.01;
	public static final double MAX_GIANT_SLAYER_PENETRATION = 0.10;

	private static final Map<Player, HunterMark> HUNTER_MARKS =
			Collections.synchronizedMap(new WeakHashMap<Player, HunterMark>());

	private EquipmentAffixCombatService() {
		// utility class
	}

	/**
	 * Handles one successful creature hit after the normal combat pipeline.
	 * Hunter's Mark may react to ranged or melee damage. Spiked Plating only
	 * reflects melee damage and never participates in crit or lifesteal logic.
	 */
	public static void onCreatureDamagedPlayer(final Creature attacker,
			final RPEntity defender, final int hitPointsBefore,
			final boolean melee) {
		if (attacker == null || !(defender instanceof Player)
				|| hitPointsBefore <= 0) {
			return;
		}
		final Player player = (Player) defender;
		final int damage = Math.max(0, hitPointsBefore - player.getHP());
		if (damage <= 0 || player.getHP() <= 0) {
			return;
		}

		if (hasEquippedAffix(player, EquipmentAffixService.HUNTER_MARK_ATTRIBUTE)) {
			mark(player, attacker, System.currentTimeMillis());
		}

		if (!melee || attacker.getHP() <= 0) {
			return;
		}
		final int reflected = getReflectedDamage(player, damage);
		if (reflected > 0) {
			attacker.onDamaged(player, Math.min(reflected, attacker.getHP()));
		}
	}

	/** Returns the fixed Hunter's Mark penetration while this exact target is marked. */
	public static double getHunterMarkArmorPenetration(final Player player,
			final RPEntity target) {
		if (player == null || target == null
				|| !hasEquippedAffix(player, EquipmentAffixService.HUNTER_MARK_ATTRIBUTE)
				|| !isMarked(player, target, System.currentTimeMillis())) {
			return 0.0;
		}
		return HUNTER_MARK_ARMOR_PENETRATION;
	}

	/**
	 * Returns Giant Slayer penetration: +1% per full 50 levels of target
	 * advantage, capped at 10%. Duplicate affixes intentionally do not stack.
	 */
	public static double getGiantSlayerArmorPenetration(final Player player,
			final RPEntity target) {
		if (player == null || target == null
				|| !hasEquippedAffix(player, EquipmentAffixService.GIANT_SLAYER_ATTRIBUTE)) {
			return 0.0;
		}
		return giantSlayerPenetration(player.getLevel(), target.getLevel());
	}

	static double giantSlayerPenetration(final int playerLevel,
			final int targetLevel) {
		final int difference = targetLevel - playerLevel;
		if (difference < GIANT_SLAYER_LEVEL_STEP) {
			return 0.0;
		}
		final int steps = difference / GIANT_SLAYER_LEVEL_STEP;
		return Math.min(MAX_GIANT_SLAYER_PENETRATION,
				steps * GIANT_SLAYER_PENETRATION_PER_STEP);
	}

	static int reflectedDamage(final int incomingDamage,
			final double reflectionFraction) {
		if (incomingDamage <= 0 || reflectionFraction <= 0.0
				|| Double.isNaN(reflectionFraction)) {
			return 0;
		}
		return Math.max(0, (int) Math.round(incomingDamage
				* Math.min(MAX_SPIKED_REFLECTION, reflectionFraction)));
	}

	private static int getReflectedDamage(final Player player,
			final int incomingDamage) {
		double reflection = 0.0;
		for (final Item item : player.getDefenseItems()) {
			if (item != null && item.has(EquipmentAffixService.SPIKED_PLATING_ATTRIBUTE)) {
				reflection += clampFraction(item.getDouble(
						EquipmentAffixService.SPIKED_PLATING_ATTRIBUTE));
			}
		}
		return reflectedDamage(incomingDamage, reflection);
	}

	private static boolean hasEquippedAffix(final Player player,
			final String attribute) {
		for (final Item item : player.getDefenseItems()) {
			if (item != null && item.has(attribute)) {
				return true;
			}
		}
		return false;
	}

	static void mark(final Player player, final RPEntity target, final long now) {
		if (player == null || target == null) {
			return;
		}
		HUNTER_MARKS.put(player,
				new HunterMark(target, now + HUNTER_MARK_DURATION_MILLISECONDS));
	}

	static boolean isMarked(final Player player, final RPEntity target,
			final long now) {
		final HunterMark mark = HUNTER_MARKS.get(player);
		if (mark == null) {
			return false;
		}
		if (now > mark.expiresAt || mark.target != target
				|| target.getHP() <= 0 || player.getZone() != target.getZone()) {
			if (now > mark.expiresAt || mark.target == target) {
				HUNTER_MARKS.remove(player);
			}
			return false;
		}
		return true;
	}

	static void clearMarksForTests() {
		HUNTER_MARKS.clear();
	}

	private static double clampFraction(final double value) {
		if (Double.isNaN(value)) {
			return 0.0;
		}
		return Math.min(1.0, Math.max(0.0, value));
	}

	private static final class HunterMark {
		private final RPEntity target;
		private final long expiresAt;

		HunterMark(final RPEntity target, final long expiresAt) {
			this.target = target;
			this.expiresAt = expiresAt;
		}
	}
}
