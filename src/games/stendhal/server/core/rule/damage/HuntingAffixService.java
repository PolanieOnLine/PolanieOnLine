/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.common.Rand;
import games.stendhal.server.core.rule.rarity.AdditionalCombatAffixDefinition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.Killer;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DomesticAnimal;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.BleedingStatus;
import games.stendhal.server.entity.status.PoisonStatus;
import games.stendhal.server.entity.status.StatusList;

/** Conditional equipped affixes and brief creature-damage protection after a kill. */
public final class HuntingAffixService {
	private static final double RESOLVE_REDUCTION = 0.10;
	private static final long RESOLVE_DURATION = 4000L;
	private static final Map<StatusList, Protection> PROTECTIONS = new WeakHashMap<>();

	private HuntingAffixService() { }

	public static int outgoingDamage(final Player player, final int damage) {
		return outgoingDamage(player, player.getAttackTarget(), damage);
	}

	public static int outgoingDamage(final Player player, final RPEntity defender, final int damage) {
		final int boosted = EquipmentUtilityAffixService.outgoingDamage(player, defender, damage);
		if (damage <= 0 || player.getHP() <= 0 || player.getBaseHP() <= 0
				|| (long) player.getHP() * 10 >= (long) player.getBaseHP() * 3
				|| !equipped(player, AdditionalCombatAffixDefinition.LAST_STAND)) {
			return boosted;
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.round(boosted * 1.20));
	}

	public static int reduceCreatureHit(final Player player, final RPEntity attacker, final int damage) {
		if (damage <= 0 || !(attacker instanceof Creature) || player.getZone() == null
				|| attacker.getZone() != player.getZone()) {
			return damage;
		}
		tick(player);
		final int protectedDamage = PROTECTIONS.containsKey(player.getStatusList())
				? Math.max(1, (int) Math.round(damage * (1.0 - RESOLVE_REDUCTION))) : damage;
		if (!equipped(player, AdditionalCombatAffixDefinition.DEFENSE_CIRCLE)) return protectedDamage;
		int attackers = 0;
		for (final Entity source : player.getAttackSources()) {
			if (!(source instanceof Creature)) {
				continue;
			}
			final Creature creature = (Creature) source;
			if (creature.getHP() <= 0 || creature.isImmortal()
					|| creature.getAttackTarget() != player || creature.getZone() != player.getZone()) {
				continue;
			}
			final int range = creature.getMaxRangeForArcher();
			if ((player.nextTo(creature) || range > 0 && creature.squaredDistance(player) <= (long) range * range)
					&& creature.hasLineOfSight(player) && ++attackers >= 5) {
				break;
			}
		}
		final double reduction = Math.min(0.12, Math.max(0, attackers - 1) * 0.03);
		return Math.max(1, (int) Math.round(protectedDamage * (1.0 - reduction)));
	}

	/** Snapshot equipped potency when weapon poison is inflicted, preserving its total damage. */
	public static PoisonStatus weaponPoison(final Player player, final int totalDamage,
			final int frequency, final int perTick) {
		final double multiplier = 1.0 + potency(player, AdditionalCombatAffixDefinition.POISON_POTENCY);
		final int boostedTotal = scaleStatusDamage(totalDamage, multiplier);
		final int boostedTick = scaleStatusDamage(perTick, multiplier);
		return new PoisonStatus(-boostedTotal, frequency, -boostedTick, player);
	}

	/** A wound snapshots its damage once; changing equipment cannot alter existing ticks. */
	public static int weaponBleedDamage(final Player player, final int totalDamage) {
		return scaleStatusDamage(totalDamage, 1.0 + potency(player, AdditionalCombatAffixDefinition.BLEED_POTENCY));
	}

	private static double potency(final Player player, final AdditionalCombatAffixDefinition affix) {
		double weighted = 0.0;
		double weights = 0.0;
		for (final Item weapon : player.getWeapons()) {
			final double weight = Math.max(1.0, weapon.getAverageDamage());
			weighted += weight * affix.value(weapon);
			weights += weight;
		}
		double bonus = weights > 0 ? weighted / weights : 0.0;
		if (player.hasGloves()) {
			bonus += affix.value(player.getGloves());
		}
		return Math.min(0.50, bonus);
	}

	private static int scaleStatusDamage(final int damage, final double multiplier) {
		return (int) Math.min(Integer.MAX_VALUE, Math.max(1L, Math.round(Math.max(1, damage) * multiplier)));
	}

	/** Only ordinary launcher ammunition is conserved, never spells or thrown weapons. */
	public static boolean conserveAmmunition(final Player player, final StackableItem ammunition) {
		final Item launcher = player.getProjectileLauncher();
		if (launcher == null || ammunition == null || ammunition != player.getAmmunition()) {
			return false;
		}
		final double chance = Math.min(0.20, AdditionalCombatAffixDefinition.AMMO_CONSERVATION.value(launcher));
		return chance > 0.0 && Rand.randUniform(1, 10000) <= Math.round(chance * 10000);
	}

	public static void onKilled(final Killer killer, final RPEntity victim) {
		Entity source = killer instanceof Entity ? (Entity) killer : null;
		if (killer instanceof PoisonStatus) {
			source = ((PoisonStatus) killer).getSource();
		} else if (killer instanceof BleedingStatus) {
			source = ((BleedingStatus) killer).getSource();
		}
		if (victim instanceof Creature) {
			EquipmentUtilityAffixService.prepareCreatureLoot((Creature) victim,
					source instanceof Player ? (Player) source : null);
		}
		if (source instanceof Player && victim instanceof Creature) {
			onKill((Player) source, (Creature) victim, System.currentTimeMillis());
		}
	}

	static void onKill(final Player player, final Creature victim, final long now) {
		tick(player, now);
		if (player.getHP() <= 0 || player.has("dead") || player.isDisconnected()
				|| victim instanceof DomesticAnimal || victim.getHP() > 0 || player.getZone() == null
				|| player.getZone() != victim.getZone()) {
			return;
		}
		EquipmentUtilityAffixService.onCreatureKilled(player, victim);
		final Item boots = player.getBoots();
		if (AdditionalCombatAffixDefinition.HUNTERS_RESOLVE.value(boots) <= 0.0) {
			return;
		}
		PROTECTIONS.put(player.getStatusList(), new Protection(boots, now + RESOLVE_DURATION));
	}

	public static void tick(final Player player) {
		tick(player, System.currentTimeMillis());
	}

	static void tick(final Player player, final long now) {
		final Protection protection = PROTECTIONS.get(player.getStatusList());
		if (protection != null && (now >= protection.expires || player.getHP() <= 0 || player.has("dead")
				|| player.isDisconnected() || player.getBoots() != protection.boots.get()
				|| AdditionalCombatAffixDefinition.HUNTERS_RESOLVE.value(player.getBoots()) <= 0.0)) {
			reset(player);
		}
	}

	public static void onUnequipped(final Player player, final Item item) {
		final Protection protection = PROTECTIONS.get(player.getStatusList());
		if (protection != null && protection.boots.get() == item) {
			reset(player);
		}
	}

	public static void reset(final Player player) {
		PROTECTIONS.remove(player.getStatusList());
	}

	private static boolean equipped(final Player player, final AdditionalCombatAffixDefinition affix) {
		for (final Item item : player.getDefenseItems()) {
			if (affix.value(item) > 0.0) {
				return true;
			}
		}
		return false;
	}

	private static final class Protection {
		final WeakReference<Item> boots;
		final long expires;

		Protection(final Item boots, final long expires) {
			this.boots = new WeakReference<>(boots);
			this.expires = expires;
		}
	}
}
