/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.common.Rand;
import games.stendhal.server.core.rule.rarity.EquipmentUtilityAffixDefinition;
import games.stendhal.server.core.rule.rarity.WeaponAffixService;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.creature.DomesticAnimal;
import games.stendhal.server.entity.item.ConsumableItem;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.Potion;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;

/** Equipped ordinary utility rolls, separate from glyphs and legendary signatures. */
public final class EquipmentUtilityAffixService {
	private static final Map<Creature, Double> LOOT_BONUSES = new WeakHashMap<>();

	private EquipmentUtilityAffixService() { }

	/** Only an actual isolated creature fight receives the ordinary damage bonus. */
	public static int outgoingDamage(final Player player, final RPEntity defender, final int damage) {
		double bonus = 0.0;
		for (final Item item : new Item[] {player.getArmor(), player.getShield(), player.getHelmet(),
				player.getLegs(), player.getBoots()}) {
			bonus += EquipmentUtilityAffixDefinition.SINGLE_COMBAT.value(item);
		}
		if (bonus <= 0.0) {
			return damage;
		}
		if (damage <= 0 || player.getHP() <= 0 || !(defender instanceof Creature)
				|| !validCreature((Creature) defender) || !sameZone(player, defender)
				|| (player.getAttackTarget() != defender && defender.getAttackTarget() != player)
				|| !canReach(player, defender)) {
			return damage;
		}
		for (final Entity source : player.getAttackSources()) {
			if (!(source instanceof RPEntity) || source == defender) {
				continue;
			}
			final RPEntity opponent = (RPEntity) source;
			if (opponent.getAttackTarget() != player || opponent.getHP() <= 0
					|| !sameZone(player, opponent) || !canReach(opponent, player)) {
				continue;
			}
			if (opponent instanceof Player || (opponent instanceof Creature && validCreature((Creature) opponent))) {
				return damage;
			}
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.round(damage * (1.0 + Math.min(0.10, bonus))));
	}

	/** Snapshot the actual corpse beneficiary before death clears the damage ledger. */
	public static void prepareCreatureLoot(final Creature victim, final Player finalBlowPlayer) {
		LOOT_BONUSES.remove(victim);
		if (victim instanceof DomesticAnimal || victim.getHP() > 0 || victim.getZone() == null) {
			return;
		}
		final String beneficiary = victim.getCorpseDeserver();
		Player recipient = beneficiary == null ? finalBlowPlayer : null;
		if (beneficiary != null) {
			for (final Player player : victim.getZone().getPlayers()) {
				if (beneficiary.equals(player.getName())) {
					recipient = player;
					break;
				}
			}
		}
		if (recipient == null || recipient.getZone() != victim.getZone() || recipient.getHP() <= 0
				|| recipient.has("dead") || recipient.isDisconnected()) {
			return;
		}
		double bonus = 0.0;
		for (final Item item : new Item[] {recipient.getCloak(), recipient.getBelt(), recipient.getRing(),
				recipient.getRingB(), recipient.getNecklace()}) {
			bonus += EquipmentUtilityAffixDefinition.COIN_BONUS.value(item);
		}
		if (bonus > 0.0) {
			LOOT_BONUSES.put(victim, Math.min(0.15, bonus));
		}
	}

	/** A death snapshot is used once and retains neither the creature nor the player. */
	public static double takeCreatureLootBonus(final Creature victim) {
		final Double bonus = LOOT_BONUSES.remove(victim);
		return bonus == null ? 0.0 : bonus.doubleValue();
	}

	/** Call only for freshly generated creature drops, never carried item instances. */
	public static void boostGeneratedMoney(final Item item, final double bonus) {
		if (bonus <= 0.0 || !(item instanceof StackableItem) || !"money".equals(item.getItemClass())) {
			return;
		}
		final StackableItem money = (StackableItem) item;
		if (money.getQuantity() > 0) {
			money.setQuantity((int) Math.min(Integer.MAX_VALUE,
					Math.round(money.getQuantity() * (1.0 + Math.min(0.15, bonus)))));
		}
	}

	private static boolean sameZone(final Player player, final RPEntity opponent) {
		return player.getZone() != null && player.getZone() == opponent.getZone();
	}

	private static boolean validCreature(final Creature creature) {
		return creature.getHP() > 0 && !creature.isImmortal() && !(creature instanceof DomesticAnimal);
	}

	private static boolean canReach(final RPEntity attacker, final RPEntity defender) {
		final int range = attacker.getMaxRangeForArcher();
		return (attacker.nextTo(defender) || range > 0 && attacker.squaredDistance(defender) <= (long) range * range)
				&& attacker.hasLineOfSight(defender);
	}

	public static void onCreatureKilled(final Player player, final Creature victim) {
		if (player.getHP() <= 0 || player.has("dead") || player.isDisconnected()
				|| victim instanceof DomesticAnimal || victim.getHP() > 0 || player.getZone() == null
				|| player.getZone() != victim.getZone()) {
			return;
		}
		final int healing = (int) Math.min(200, EquipmentUtilityAffixDefinition.KILL_RECOVERY.value(player.getGloves())
				+ EquipmentUtilityAffixDefinition.KILL_RECOVERY.value(player.getBelt()));
		if (healing > 0 && player.heal(healing, true) > 0) {
			player.notifyWorldAboutChanges();
		}
	}

	/** Called only after a healing potion passes its normal use checks. */
	public static boolean conserveHealingPotion(final Player player, final ConsumableItem potion) {
		if (!(potion instanceof Potion) || potion.getQuantity() <= 0 || potion.getAmount() <= 0
				|| potion.getRegen() < 0) {
			return false;
		}
		final long maximum = LifeTideService.isActive(player)
				? Math.min(Integer.MAX_VALUE, (long) player.getBaseHP() + player.getBaseHP() / 5)
				: player.getBaseHP();
		if (player.getHP() <= 0 || player.has("dead") || player.getHP() >= maximum) {
			return false;
		}
		double chance = 0.0;
		for (final Item item : player.getDefenseItems()) {
			chance += EquipmentUtilityAffixDefinition.POTION_CONSERVATION.value(item);
		}
		return succeeds(Math.min(0.20, chance));
	}

	/** Each actual weapon use rolls only that weapon's own saved affix. */
	public static boolean conserveDurability(final Item weapon) {
		if (!WeaponAffixService.isWeapon(weapon) || !weapon.has("durability") || weapon.getInt("durability") <= 0) {
			return false;
		}
		return succeeds(Math.min(0.20, EquipmentUtilityAffixDefinition.DURABILITY_CONSERVATION.value(weapon)));
	}

	private static boolean succeeds(final double chance) {
		return chance > 0.0 && Rand.randUniform(1, 10000) <= Math.round(chance * 10000);
	}
}
