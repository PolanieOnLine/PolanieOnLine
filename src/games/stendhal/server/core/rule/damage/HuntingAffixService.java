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

/** Conditional equipped affixes and a transient movement bonus separate from base speed. */
public final class HuntingAffixService {
	private static final double PACE_MULTIPLIER = 1.10;
	private static final long PACE_DURATION = 4000L;
	private static final Map<StatusList, Pace> PACES = new WeakHashMap<>();

	private HuntingAffixService() { }

	public static int outgoingDamage(final Player player, final int damage) {
		if (damage <= 0 || player.getHP() <= 0 || player.getBaseHP() <= 0
				|| (long) player.getHP() * 10 >= (long) player.getBaseHP() * 3
				|| !equipped(player, AdditionalCombatAffixDefinition.LAST_STAND)) {
			return damage;
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.round(damage * 1.20));
	}

	public static int reduceCreatureHit(final Player player, final RPEntity attacker, final int damage) {
		if (damage <= 0 || !(attacker instanceof Creature) || player.getZone() == null
				|| attacker.getZone() != player.getZone()
				|| !equipped(player, AdditionalCombatAffixDefinition.DEFENSE_CIRCLE)) {
			return damage;
		}
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
		return Math.max(1, (int) Math.round(damage * (1.0 - reduction)));
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
		final Item boots = player.getBoots();
		if (AdditionalCombatAffixDefinition.HUNTERS_PACE.value(boots) <= 0.0) {
			return;
		}
		final Pace previous = PACES.get(player.getStatusList());
		PACES.put(player.getStatusList(), new Pace(boots, now + PACE_DURATION));
		if (previous == null && !player.stopped()) {
			player.setSpeed(player.getSpeed() * PACE_MULTIPLIER);
		}
	}

	public static double movementMultiplier(final Player player) {
		tick(player);
		return PACES.containsKey(player.getStatusList()) ? PACE_MULTIPLIER : 1.0;
	}

	public static void tick(final Player player) {
		tick(player, System.currentTimeMillis());
	}

	static void tick(final Player player, final long now) {
		final Pace pace = PACES.get(player.getStatusList());
		if (pace != null && (now >= pace.expires || player.getHP() <= 0 || player.has("dead")
				|| player.isDisconnected() || player.getBoots() != pace.boots.get()
				|| AdditionalCombatAffixDefinition.HUNTERS_PACE.value(player.getBoots()) <= 0.0)) {
			reset(player);
		}
	}

	public static void onUnequipped(final Player player, final Item item) {
		final Pace pace = PACES.get(player.getStatusList());
		if (pace != null && pace.boots.get() == item) {
			reset(player);
		}
	}

	public static void reset(final Player player) {
		if (PACES.remove(player.getStatusList()) != null && !player.stopped()) {
			player.setSpeed(player.getSpeed() / PACE_MULTIPLIER);
		}
	}

	private static boolean equipped(final Player player, final AdditionalCombatAffixDefinition affix) {
		for (final Item item : player.getDefenseItems()) {
			if (affix.value(item) > 0.0) {
				return true;
			}
		}
		return false;
	}

	private static final class Pace {
		final WeakReference<Item> boots;
		final long expires;

		Pace(final Item boots, final long expires) {
			this.boots = new WeakReference<>(boots);
			this.expires = expires;
		}
	}
}
