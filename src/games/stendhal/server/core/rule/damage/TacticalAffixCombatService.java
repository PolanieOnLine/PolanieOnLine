/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.common.constants.ItemTooltip;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Equipped affixes and short-lived attack sequences; no saved combat statistics are modified. */
public final class TacticalAffixCombatService {
	private static final double MAX_CRUSHING = 0.30;
	private static final double MAX_HARDENING = 0.30;
	private static final double TENACITY_STEP = 0.03;
	private static final int MAX_TENACITY_STACKS = 5;
	private static final long STREAK_PAUSE = 5000L;
	private static final long BREACH_DURATION = 6000L;
	private static final double BREACH_BONUS = 0.15;
	private static final int CLEAVE_HITS = 4;
	private static final double CLEAVE_DAMAGE = 0.30;
	private static final int CLEAVE_TARGETS = 2;
	private static final double RECOVERY_DURATION = 0.70;
	private static final Map<Player, Sequence> SEQUENCES =
			Collections.synchronizedMap(new WeakHashMap<Player, Sequence>());

	private TacticalAffixCombatService() { }

	public static double getArmorMultiplier(final Player player) {
		if (player == null) {
			return 1.0;
		}
		double bonus = WeaponAffixCombatService.getWeightedFraction(
				player.getWeapons(), ItemTooltip.ARMOR_CRUSHING);
		for (Item item : player.getDefenseItems()) {
			bonus += fraction(item, ItemTooltip.ARMOR_CRUSHING);
		}
		return 1.0 + Math.min(MAX_CRUSHING, bonus);
	}

	/** Only the extra critical portion is reduced; ordinary damage is unchanged. */
	public static int reduceCriticalBonus(final RPEntity defender,
			final int baseDamage, final int criticalDamage) {
		if (!(defender instanceof Player) || baseDamage <= 0 || criticalDamage <= baseDamage) {
			return criticalDamage;
		}
		double reduction = 0.0;
		for (Item item : defender.getDefenseItems()) {
			reduction += fraction(item, ItemTooltip.CRITICAL_HARDENING);
		}
		return baseDamage + (int) Math.round((criticalDamage - baseDamage)
				* (1.0 - Math.min(MAX_HARDENING, reduction)));
	}

	public static int prepareHit(final Player player, final RPEntity target, final int damage) {
		return prepareHit(player, target, damage, System.currentTimeMillis());
	}

	static int prepareHit(final Player player, final RPEntity target,
			final int damage, final long now) {
		if (damage <= 0 || target.getHP() <= 0 || player.getZone() != target.getZone()
				|| !hasOffensiveSignature(player)) {
			return damage;
		}
		Sequence sequence = sequence(player, target, now);
		double bonus = sequence.stacks * TENACITY_STEP * signatureWeight(player, ItemTooltip.LEGENDARY_TENACITY);
		if (sequence.breaches.containsKey(target) && target.getArmorHP() == 0) {
			bonus += BREACH_BONUS * signatureWeight(player, ItemTooltip.LEGENDARY_BREACH);
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.round(damage * (1.0 + bonus)));
	}

	public static void onHit(final Player player, final RPEntity target, final int armorBefore,
			final ArmorPoolService.DamageResult result, final boolean melee) {
		onHit(player, target, armorBefore, result, melee, System.currentTimeMillis());
	}

	static void onHit(final Player player, final RPEntity target, final int armorBefore,
			final ArmorPoolService.DamageResult result, final boolean melee, final long now) {
		if (result.getTotalDamage() <= 0 || player.getHP() <= 0
				|| player.getZone() == null || (target.getHP() > 0 && player.getZone() != target.getZone())
				|| !hasOffensiveSignature(player)) {
			return;
		}
		Sequence sequence = sequence(player, target, now);
		sequence.lastHit = now;
		if (signature(player, ItemTooltip.LEGENDARY_TENACITY) != null) {
			sequence.stacks = Math.min(MAX_TENACITY_STACKS, sequence.stacks + 1);
		}
		if (armorBefore > 0 && result.getArmorDamage() > 0 && target.getArmorHP() == 0
				&& target.getHP() > 0 && signature(player, ItemTooltip.LEGENDARY_BREACH) != null) {
			sequence.breaches.put(target, now + BREACH_DURATION);
		}
		if (melee && signature(player, ItemTooltip.LEGENDARY_CLEAVE) != null
				&& ++sequence.cleaveHits >= CLEAVE_HITS) {
			sequence.cleaveHits = 0;
			cleave(player, target, result.getTotalDamage());
		}
	}

	/** Target changes reset target-specific bonuses, including switches followed by a miss. */
	public static void targetChanged(final Player player) {
		Sequence sequence = SEQUENCES.get(player);
		if (sequence != null) {
			sequence.target.clear();
			sequence.stacks = 0;
		}
	}

	/** Item hooks also catch unequip/re-equip between hits and changes to either hand. */
	public static void equipmentChanged(final Player player) {
		SEQUENCES.remove(player);
	}

	/** Freeze duration when inflicted, using turn precision rather than rounding to full seconds. */
	public static int statusDurationTurns(final RPEntity entity, final int seconds) {
		int turns = SingletonRepository.getRPWorld().getTurnsInSeconds(seconds);
		if (entity instanceof Player) {
			for (Item item : entity.getDefenseItems()) {
				if (fraction(item, ItemTooltip.LEGENDARY_QUICK_RECOVERY) > 0) {
					return Math.max(1, (int) Math.round(turns * RECOVERY_DURATION));
				}
			}
		}
		return turns;
	}

	private static Sequence sequence(final Player player, final RPEntity target, final long now) {
		Sequence sequence = SEQUENCES.get(player);
		if (sequence == null || sequence.zone.get() != player.getZone()) {
			sequence = new Sequence(player.getZone());
			SEQUENCES.put(player, sequence);
		}
		if (sequence.target.get() != target) {
			sequence.target = new WeakReference<RPEntity>(target);
			sequence.stacks = 0;
		}
		sequence.breaches.entrySet().removeIf(entry -> entry.getValue() <= now);
		if (now - sequence.lastHit >= STREAK_PAUSE) {
			sequence.stacks = 0;
			sequence.cleaveHits = 0;
		}
		Item tenacity = signature(player, ItemTooltip.LEGENDARY_TENACITY);
		if (sequence.tenacityWeapon.get() != tenacity) {
			sequence.tenacityWeapon = new WeakReference<Item>(tenacity);
			sequence.stacks = 0;
		}
		Item breach = signature(player, ItemTooltip.LEGENDARY_BREACH);
		if (sequence.breachWeapon.get() != breach) {
			sequence.breachWeapon = new WeakReference<Item>(breach);
			sequence.breaches.clear();
		}
		Item cleave = signature(player, ItemTooltip.LEGENDARY_CLEAVE);
		if (sequence.cleaveWeapon.get() != cleave) {
			sequence.cleaveWeapon = new WeakReference<Item>(cleave);
			sequence.cleaveHits = 0;
		}
		return sequence;
	}

	private static void cleave(final Player player, final RPEntity primary, final int damage) {
		StendhalRPZone zone = player.getZone();
		if (zone.isInProtectionArea(player)) {
			return;
		}
		int secondaryDamage = (int) Math.round(damage * CLEAVE_DAMAGE
				* signatureWeight(player, ItemTooltip.LEGENDARY_CLEAVE));
		int remaining = CLEAVE_TARGETS;
		for (Entity entity : zone.getEntitiesOfClass(Creature.class)) {
			if (player.getHP() <= 0 || player.getZone() != zone) {
				break;
			}
			Creature creature = (Creature) entity;
			if (creature == primary || creature.getHP() <= 0 || creature.isImmortal() || !creature.isAttackable()
					|| creature.getAttackTarget() != player || !player.nextTo(creature)
					|| zone.isInProtectionArea(creature) || !player.hasLineOfSight(creature)) {
				continue;
			}
			// Use the damage sink directly: armour/kill credit still work, but no
			// secondary critical rolls, lifesteal, weapon procs or recursive cleaves.
			creature.receiveCombatDamage(player, secondaryDamage);
			if (--remaining == 0) {
				break;
			}
		}
	}

	private static boolean hasOffensiveSignature(final Player player) {
		return signature(player, ItemTooltip.LEGENDARY_TENACITY) != null
				|| signature(player, ItemTooltip.LEGENDARY_BREACH) != null
				|| signature(player, ItemTooltip.LEGENDARY_CLEAVE) != null;
	}

	private static Item signature(final Player player, final String attribute) {
		for (Item weapon : player.getWeapons()) {
			if (fraction(weapon, attribute) > 0) {
				return weapon;
			}
		}
		return null;
	}

	private static double signatureWeight(final Player player, final String attribute) {
		return WeaponAffixCombatService.getWeightedFixedBonus(player.getWeapons(), attribute, 1.0);
	}

	private static double fraction(final Item item, final String attribute) {
		if (item == null || !item.has(attribute)) {
			return 0.0;
		}
		double value = item.getDouble(attribute);
		return Double.isFinite(value) ? Math.min(1.0, Math.max(0.0, value)) : 0.0;
	}

	private static final class Sequence {
		private final WeakReference<StendhalRPZone> zone;
		private WeakReference<RPEntity> target = new WeakReference<RPEntity>(null);
		private WeakReference<Item> tenacityWeapon = new WeakReference<Item>(null);
		private WeakReference<Item> breachWeapon = new WeakReference<Item>(null);
		private WeakReference<Item> cleaveWeapon = new WeakReference<Item>(null);
		private final Map<RPEntity, Long> breaches = new WeakHashMap<RPEntity, Long>();
		private long lastHit;
		private int stacks;
		private int cleaveHits;

		Sequence(final StendhalRPZone zone) {
			this.zone = new WeakReference<StendhalRPZone>(zone);
		}
	}
}
