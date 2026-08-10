/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.common.Rand;
import games.stendhal.server.core.rule.rarity.LegendaryEquipmentAffixService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/**
 * Resolves the chance that a player parries an incoming melee attack with the
 * weapons currently held in their hands.
 */
public final class ParryService {
	public static final String PARRY_CHANCE_ATTRIBUTE = "parry_chance";
	public static final String LEGENDARY_DUEL_MASTER_ATTRIBUTE =
			"legendary_duel_master";
	/** Hard cap for the player's final parry chance. */
	public static final double MAX_PARRY_CHANCE = 0.15;
	public static final double DUEL_MASTER_PARRY_BONUS = 0.05;
	public static final double DUEL_MASTER_RIPOSTE_DAMAGE_BONUS = 0.30;
	public static final double UNYIELDING_PROTECTION_PARRY_BONUS = 0.10;
	public static final double UNYIELDING_PROTECTION_HP_THRESHOLD = 0.30;

	/**
	 * Riposte readiness is transient combat state, not item persistence. A weak
	 * map avoids retaining discarded item instances while keeping readiness tied
	 * to the exact sword which will consume the one armed riposte.
	 */
	private static final Map<Item, Boolean> DUEL_MASTER_RIPOSTES =
			Collections.synchronizedMap(new WeakHashMap<Item, Boolean>());

	private ParryService() {
		// utility class
	}

	/**
	 * Returns the combined parry chance of currently held melee weapons.
	 * Individual weapon chances are independent, but the final result is capped
	 * at 15%. Values outside the supported range are clamped defensively.
	 *
	 * Legendary Duel Master swords contribute an additional five percentage
	 * points even when the sword did not roll the regular parry affix. Legendary
	 * Unyielding Protection armour contributes one emergency 10% source below
	 * 30% HP. All sources share the same final 15% cap.
	 *
	 * @param player defending player
	 * @return combined chance in the range [0, 0.15]
	 */
	public static double getParryChance(final Player player) {
		if (player == null) {
			return 0.0;
		}

		double failureChance = 1.0;
		boolean hasParrySource = false;
		for (final Item weapon : player.getWeapons()) {
			if (weapon == null || weapon.isNonMeleeWeapon()) {
				continue;
			}

			double chance = weapon.has(PARRY_CHANCE_ATTRIBUTE)
					? weapon.getDouble(PARRY_CHANCE_ATTRIBUTE) : 0.0;
			if (weapon.has(LEGENDARY_DUEL_MASTER_ATTRIBUTE)) {
				chance += DUEL_MASTER_PARRY_BONUS;
			}
			chance = clampChance(chance);
			if (chance <= 0.0) {
				continue;
			}
			hasParrySource = true;
			failureChance *= 1.0 - chance;
		}

		if (isUnyieldingProtectionActive(player)) {
			hasParrySource = true;
			failureChance *= 1.0 - UNYIELDING_PROTECTION_PARRY_BONUS;
		}

		return hasParrySource
				? clampChance(1.0 - failureChance)
				: 0.0;
	}

	/**
	 * Rolls one parry attempt for an incoming melee attack. A successful Duel
	 * Master parry arms one +30% riposte on one currently held legendary sword.
	 *
	 * @param player defending player
	 * @return {@code true} when the incoming attack is completely parried
	 */
	public static boolean rollParry(final Player player) {
		final double chance = getParryChance(player);
		if (chance <= 0.0) {
			return false;
		}
		final boolean success = isParrySuccessful(chance, Rand.rand());
		if (success) {
			markDuelMasterRiposte(player);
		}
		return success;
	}

	/** Package-visible deterministic seam for unit tests. */
	static boolean isParrySuccessful(final double chance, final double roll) {
		if (Double.isNaN(roll) || roll < 0.0 || roll >= 1.0) {
			throw new IllegalArgumentException("Parry roll must be in [0, 1)");
		}
		return roll < clampChance(chance);
	}

	static void markDuelMasterRiposte(final Player player) {
		if (player == null) {
			return;
		}
		for (final Item weapon : player.getWeapons()) {
			if (weapon != null && !weapon.isNonMeleeWeapon()
					&& weapon.has(LEGENDARY_DUEL_MASTER_ATTRIBUTE)) {
				DUEL_MASTER_RIPOSTES.put(weapon, Boolean.TRUE);
				return;
			}
		}
	}

	/**
	 * Consumes at most one armed Duel Master riposte from currently attacking
	 * weapons. Misses do not consume it because this method is called only after
	 * the combat formula produced positive damage.
	 */
	public static boolean consumeDuelMasterRiposte(final List<Item> weapons) {
		if (weapons == null || weapons.isEmpty()) {
			return false;
		}
		synchronized (DUEL_MASTER_RIPOSTES) {
			for (final Item weapon : weapons) {
				if (weapon != null && weapon.has(LEGENDARY_DUEL_MASTER_ATTRIBUTE)
						&& DUEL_MASTER_RIPOSTES.remove(weapon) != null) {
					return true;
				}
			}
			return false;
		}
	}

	private static boolean isUnyieldingProtectionActive(final Player player) {
		final int baseHp = player.getBaseHP();
		final int hp = player.getHP();
		if (baseHp <= 0 || hp <= 0
				|| (double) hp / (double) baseHp
						>= UNYIELDING_PROTECTION_HP_THRESHOLD) {
			return false;
		}
		for (final Item item : player.getDefenseItems()) {
			if (item != null
					&& item.has(LegendaryEquipmentAffixService.UNYIELDING_PROTECTION_ATTRIBUTE)) {
				return true;
			}
		}
		return false;
	}

	private static double clampChance(final double chance) {
		if (Double.isNaN(chance) || chance <= 0.0) {
			return 0.0;
		}
		return Math.min(MAX_PARRY_CHANCE, chance);
	}
}
