/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.List;

import games.stendhal.common.Rand;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.BleedingAttacker;
import games.stendhal.server.entity.status.HeavyStatus;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.entity.status.Status;
import games.stendhal.server.entity.status.StatusType;
import games.stendhal.server.entity.status.StunnedStatus;

/** Resolves conditional damage and status-proc weapon affixes. */
public final class WeaponAffixCombatService {
	public static final String BLEED_ON_HIT_ATTRIBUTE = "bleed_on_hit";
	public static final String POISON_ON_HIT_ATTRIBUTE = "poison_on_hit";
	public static final String EXECUTE_DAMAGE_ATTRIBUTE = "execute_damage";
	public static final String DISTANCE_DAMAGE_ATTRIBUTE = "distance_damage";
	public static final String LEGENDARY_DEEP_WOUNDS_ATTRIBUTE =
			"legendary_deep_wounds";
	public static final String LEGENDARY_LONGSHOT_ATTRIBUTE =
			"legendary_longshot";
	public static final String LEGENDARY_EXECUTIONER_ATTRIBUTE =
			"legendary_executioner";
	public static final String LEGENDARY_CRUSHING_BLOW_ATTRIBUTE =
			"legendary_crushing_blow";
	public static final String LEGENDARY_STUNNING_FORCE_ATTRIBUTE =
			"legendary_stunning_force";
	public static final String LEGENDARY_BINDING_STRIKE_ATTRIBUTE =
			"legendary_binding_strike";
	public static final String LEGENDARY_MERCILESS_REACH_ATTRIBUTE =
			"legendary_merciless_reach";
	public static final String LEGENDARY_FALCON_EYE_ATTRIBUTE =
			"legendary_falcon_eye";
	public static final String LEGENDARY_FIRST_SALVO_ATTRIBUTE =
			"legendary_first_salvo";
	public static final String LEGENDARY_POWER_OVERLOAD_ATTRIBUTE =
			"legendary_power_overload";
	public static final String LEGENDARY_ARCANE_FOCUS_ATTRIBUTE =
			"legendary_arcane_focus";

	private static final double EXECUTE_HP_THRESHOLD = 0.25;
	private static final double LEGENDARY_EXECUTION_HP_THRESHOLD = 0.20;
	private static final double FIRST_SALVO_HP_THRESHOLD = 0.80;
	private static final double LEGENDARY_EXECUTION_DAMAGE_BONUS = 0.35;
	private static final double LEGENDARY_LONGSHOT_DAMAGE_BONUS = 0.25;
	private static final double LEGENDARY_CRUSHING_BLOW_DAMAGE_BONUS = 0.25;
	public static final double LEGENDARY_FALCON_EYE_CRITICAL_CHANCE_BONUS = 10.0;
	private static final double LEGENDARY_FIRST_SALVO_DAMAGE_BONUS = 0.30;
	private static final double LEGENDARY_POWER_OVERLOAD_PROC_CHANCE = 0.15;
	private static final double LEGENDARY_POWER_OVERLOAD_DAMAGE_BONUS = 0.50;
	private static final double LEGENDARY_ARCANE_FOCUS_DAMAGE_BONUS = 0.25;
	private static final double LEGENDARY_STUN_PROC_CHANCE = 0.15;
	private static final double LEGENDARY_HEAVY_PROC_CHANCE = 0.15;
	private static final int LEGENDARY_HEAVY_DURATION_SECONDS = 10;
	private static final double MAX_STATUS_PROC_CHANCE = 0.25;
	private static final double BLEED_TOTAL_DAMAGE_FACTOR = 0.25;
	private static final double DEEP_WOUNDS_PROC_CHANCE = 0.15;
	private static final double DEEP_WOUNDS_DAMAGE_FACTOR = 0.35;
	private static final double POISON_TOTAL_DAMAGE_FACTOR = 0.20;
	private static final int POISON_TICKS = 4;
	private static final int POISON_FREQUENCY = 3;
	private static final int PROC_ROLL_SCALE = 10000;

	private WeaponAffixCombatService() {
		// utility class
	}

	public static int applyConditionalDamageBonuses(final int damage,
			final List<Item> weapons, final RPEntity defender,
			final boolean rangedAttack) {
		if (damage <= 0) {
			return damage;
		}

		double multiplier = 1.0;
		if (isExecuteActive(defender)) {
			multiplier *= 1.0 + getWeightedFraction(weapons,
					EXECUTE_DAMAGE_ATTRIBUTE);
		}
		if (isLegendaryExecutionActive(defender)) {
			multiplier *= 1.0 + getWeightedFixedBonus(weapons,
					LEGENDARY_EXECUTIONER_ATTRIBUTE,
					LEGENDARY_EXECUTION_DAMAGE_BONUS);
		}
		if (isCrushingBlowActive(defender)) {
			multiplier *= 1.0 + getWeightedFixedBonus(weapons,
					LEGENDARY_CRUSHING_BLOW_ATTRIBUTE,
					LEGENDARY_CRUSHING_BLOW_DAMAGE_BONUS);
		}

		if (ParryService.consumeDuelMasterRiposte(weapons)) {
			multiplier *= 1.0 + ParryService.DUEL_MASTER_RIPOSTE_DAMAGE_BONUS;
		}

		if (rangedAttack) {
			multiplier *= 1.0 + getWeightedFraction(weapons,
					DISTANCE_DAMAGE_ATTRIBUTE);
			multiplier *= 1.0 + getWeightedFixedBonus(weapons,
					LEGENDARY_LONGSHOT_ATTRIBUTE,
					LEGENDARY_LONGSHOT_DAMAGE_BONUS);
			if (isFirstSalvoActive(defender)) {
				multiplier *= 1.0 + getWeightedFixedBonus(weapons,
						LEGENDARY_FIRST_SALVO_ATTRIBUTE,
						LEGENDARY_FIRST_SALVO_DAMAGE_BONUS);
			}
			if (hasCombatStatus(defender)) {
				multiplier *= 1.0 + getWeightedFixedBonus(weapons,
						LEGENDARY_ARCANE_FOCUS_ATTRIBUTE,
						LEGENDARY_ARCANE_FOCUS_DAMAGE_BONUS);
			}
			final double overloadChance = combinedFixedProcChance(weapons,
					LEGENDARY_POWER_OVERLOAD_ATTRIBUTE,
					LEGENDARY_POWER_OVERLOAD_PROC_CHANCE);
			if (overloadChance > 0.0
					&& rollChance(overloadChance,
							Rand.randUniform(1, PROC_ROLL_SCALE))) {
				multiplier *= 1.0 + LEGENDARY_POWER_OVERLOAD_DAMAGE_BONUS;
			}
		}
		return scaleDamage(damage, multiplier);
	}

	public static boolean isExecuteActive(final RPEntity defender) {
		return isBelowHealthThreshold(defender, EXECUTE_HP_THRESHOLD);
	}

	public static boolean isLegendaryExecutionActive(final RPEntity defender) {
		return isBelowHealthThreshold(defender,
				LEGENDARY_EXECUTION_HP_THRESHOLD);
	}

	public static boolean isFirstSalvoActive(final RPEntity defender) {
		if (defender == null || defender.getHP() <= 0 || defender.getBaseHP() <= 0) {
			return false;
		}
		return (double) defender.getHP() / (double) defender.getBaseHP()
				>= FIRST_SALVO_HP_THRESHOLD;
	}

	public static boolean isCrushingBlowActive(final RPEntity defender) {
		if (!(defender instanceof Creature)) {
			return false;
		}
		final WeaponArmorInteractionService.ArmorTier tier =
				WeaponArmorInteractionService.classify(
						((Creature) defender).getArmorType());
		return tier == WeaponArmorInteractionService.ArmorTier.MEDIUM
				|| tier == WeaponArmorInteractionService.ArmorTier.HEAVY;
	}

	private static boolean isBelowHealthThreshold(final RPEntity defender,
			final double threshold) {
		if (defender == null || defender.getHP() <= 0 || defender.getBaseHP() <= 0) {
			return false;
		}
		return ((double) defender.getHP() / (double) defender.getBaseHP())
				< threshold;
	}

	public static double getWeightedFraction(final List<Item> weapons,
			final String attribute) {
		if (weapons == null || weapons.isEmpty() || attribute == null) {
			return 0.0;
		}

		double weightedValue = 0.0;
		double totalWeight = 0.0;
		for (final Item weapon : weapons) {
			if (weapon == null) {
				continue;
			}
			double weight = Math.max(0.0, weapon.getAverageDamage());
			if (weight == 0.0) {
				weight = 1.0;
			}
			final double value = weapon.has(attribute)
					? clampFraction(weapon.getDouble(attribute)) : 0.0;
			weightedValue += weight * value;
			totalWeight += weight;
		}
		return totalWeight == 0.0 ? 0.0 : weightedValue / totalWeight;
	}

	public static double getWeightedFixedBonus(final List<Item> weapons,
			final String attribute, final double fixedBonus) {
		if (weapons == null || weapons.isEmpty() || attribute == null
				|| fixedBonus <= 0.0 || Double.isNaN(fixedBonus)) {
			return 0.0;
		}
		double weightedValue = 0.0;
		double totalWeight = 0.0;
		for (final Item weapon : weapons) {
			if (weapon == null) {
				continue;
			}
			double weight = Math.max(0.0, weapon.getAverageDamage());
			if (weight == 0.0) {
				weight = 1.0;
			}
			weightedValue += weight * (weapon.has(attribute) ? fixedBonus : 0.0);
			totalWeight += weight;
		}
		return totalWeight == 0.0 ? 0.0 : weightedValue / totalWeight;
	}

	public static void applyOnHitProcs(final Player attacker,
			final RPEntity defender, final List<Item> weapons, final int damage) {
		if (attacker == null || defender == null || defender.getHP() <= 0
				|| damage <= 0) {
			return;
		}

		final BleedingAttacker bleeding = createBleedingAttacker(weapons);
		if (bleeding != null) {
			bleeding.onHit(defender, attacker, damage);
		}

		final double poisonChance = effectiveStatusChance(defender,
				StatusType.POISONED,
				combinedProcChance(weapons, POISON_ON_HIT_ATTRIBUTE));
		if (poisonChance > 0.0
				&& rollChance(poisonChance, Rand.randUniform(1, PROC_ROLL_SCALE))) {
			defender.getStatusList().inflictStatus(createPoisonStatus(damage),
					attacker);
		}

		// Stunning Force is true crowd control. It uses its own resistance and
		// deliberately does not roll again while an existing stun is active,
		// preserving the handler's no-refresh contract.
		if (!defender.getStatusList().hasStatus(StatusType.STUNNED)) {
			final double stunChance = effectiveStatusChance(defender,
					StatusType.STUNNED, combinedFixedProcChance(weapons,
							LEGENDARY_STUNNING_FORCE_ATTRIBUTE,
							LEGENDARY_STUN_PROC_CHANCE));
			if (stunChance > 0.0
					&& rollChance(stunChance,
							Rand.randUniform(1, PROC_ROLL_SCALE))) {
				defender.getStatusList().inflictStatus(new StunnedStatus(), attacker);
			}
		}

		// Binding Strike remains a movement-control HEAVY effect; it is intentionally
		// separate from stun so both status families keep independent resistance.
		final double heavyChance = effectiveStatusChance(defender,
				StatusType.HEAVY, combinedFixedProcChance(weapons,
						LEGENDARY_BINDING_STRIKE_ATTRIBUTE,
						LEGENDARY_HEAVY_PROC_CHANCE));
		if (heavyChance > 0.0
				&& rollChance(heavyChance, Rand.randUniform(1, PROC_ROLL_SCALE))) {
			defender.getStatusList().inflictStatus(
					new HeavyStatus(LEGENDARY_HEAVY_DURATION_SECONDS), attacker);
		}
	}

	static BleedingAttacker createBleedingAttacker(final List<Item> weapons) {
		final double normalChance = combinedProcChance(weapons,
				BLEED_ON_HIT_ATTRIBUTE);
		final double deepWoundsChance = combinedFixedProcChance(weapons,
				LEGENDARY_DEEP_WOUNDS_ATTRIBUTE, DEEP_WOUNDS_PROC_CHANCE);
		final double combinedChance = Math.min(MAX_STATUS_PROC_CHANCE,
				1.0 - ((1.0 - normalChance) * (1.0 - deepWoundsChance)));
		if (combinedChance <= 0.0) {
			return null;
		}
		final double damageFactor = deepWoundsChance > 0.0
				? DEEP_WOUNDS_DAMAGE_FACTOR : BLEED_TOTAL_DAMAGE_FACTOR;
		return new BleedingAttacker(combinedChance * 100.0, damageFactor);
	}

	public static double combinedProcChance(final List<Item> weapons,
			final String attribute) {
		if (weapons == null || weapons.isEmpty() || attribute == null) {
			return 0.0;
		}
		double missAll = 1.0;
		for (final Item weapon : weapons) {
			if (weapon != null && weapon.has(attribute)) {
				missAll *= 1.0 - clampFraction(weapon.getDouble(attribute));
			}
		}
		return Math.min(MAX_STATUS_PROC_CHANCE, 1.0 - missAll);
	}

	static double combinedFixedProcChance(final List<Item> weapons,
			final String attribute, final double sourceChance) {
		if (weapons == null || weapons.isEmpty() || attribute == null) {
			return 0.0;
		}
		double missAll = 1.0;
		for (final Item weapon : weapons) {
			if (weapon != null && weapon.has(attribute)) {
				missAll *= 1.0 - clampFraction(sourceChance);
			}
		}
		return Math.min(MAX_STATUS_PROC_CHANCE, 1.0 - missAll);
	}

	static boolean isPowerOverloadSuccessful(final List<Item> weapons,
			final boolean rangedAttack, final int roll) {
		if (!rangedAttack) {
			return false;
		}
		final double chance = combinedFixedProcChance(weapons,
				LEGENDARY_POWER_OVERLOAD_ATTRIBUTE,
				LEGENDARY_POWER_OVERLOAD_PROC_CHANCE);
		return chance > 0.0 && rollChance(chance, roll);
	}

	static boolean hasCombatStatus(final RPEntity defender) {
		if (defender == null || defender.getStatusList() == null) {
			return false;
		}
		final StatusType[] combatStatuses = {
			StatusType.POISONED, StatusType.BLEEDING, StatusType.SHOCKED,
			StatusType.CONFUSED, StatusType.HEAVY, StatusType.STUNNED,
			StatusType.ZOMBIE
		};
		for (final StatusType statusType : combatStatuses) {
			if (defender.getStatusList().hasStatus(statusType)) {
				return true;
			}
		}
		return false;
	}

	static boolean rollChance(final double chance, final int roll) {
		if (roll < 1 || roll > PROC_ROLL_SCALE) {
			throw new IllegalArgumentException("Proc roll must be in [1, 10000]");
		}
		return roll <= Math.round(clampFraction(chance) * PROC_ROLL_SCALE);
	}

	static double effectiveStatusChance(final RPEntity defender,
			final StatusType statusType, final double baseChance) {
		final double chance = clampFraction(baseChance);
		if (defender == null || statusType == null) {
			return chance;
		}
		final double resistance = EquipmentStatusResistanceService.getResistance(
				defender, statusType);
		return clampFraction(chance * (1.0 - resistance));
	}

	private static Status createPoisonStatus(final int hitDamage) {
		final int totalDamage = Math.max(1,
				(int) Math.round(hitDamage * POISON_TOTAL_DAMAGE_FACTOR));
		final int perTick = Math.max(1,
				(int) Math.ceil(totalDamage / (double) POISON_TICKS));
		return new PoisonStatus(-totalDamage, POISON_FREQUENCY, -perTick);
	}

	private static int scaleDamage(final int damage, final double multiplier) {
		return Math.max(0,
				(int) Math.round(damage * Math.max(0.0, multiplier)));
	}

	private static double clampFraction(final double value) {
		if (Double.isNaN(value)) {
			return 0.0;
		}
		return Math.min(1.0, Math.max(0.0, value));
	}
}
