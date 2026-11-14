/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.rule.rarity.AdditionalCombatAffixDefinition;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.creature.Creature;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.npc.TrainingDummy;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StatusList;

/** Consecutive real misses arm one accuracy override and a short damage bonus. */
public final class InevitabilityService {
	private static final long BONUS_DURATION = 4000L;
	private static final Map<StatusList, Streak> STREAKS = new WeakHashMap<>();

	private InevitabilityService() { }

	public static boolean resolveHit(final Player player, final RPEntity target, final boolean rolledHit) {
		return resolveHit(player, target, rolledHit, System.currentTimeMillis());
	}

	static boolean resolveHit(final Player player, final RPEntity target, final boolean rolledHit, final long now) {
		final Item weapon = signature(player);
		if (weapon == null || player.getHP() <= 0 || target.getHP() <= 0 || target instanceof TrainingDummy
				|| player.getZone() == null || player.getZone() != target.getZone()
				|| target instanceof Creature && ((Creature) target).isImmortal()) {
			reset(player);
			return rolledHit;
		}
		Streak streak = STREAKS.get(player.getStatusList());
		if (streak == null || streak.target.get() != target || streak.weapon.get() != weapon
				|| streak.zone.get() != player.getZone()) {
			streak = new Streak(target, weapon, player.getZone());
			STREAKS.put(player.getStatusList(), streak);
		}
		if (streak.armed) {
			streak.armed = false;
			streak.misses = 0;
			return true;
		}
		if (rolledHit) {
			streak.misses = 0;
		} else if (++streak.misses == 2) {
			streak.misses = 0;
			streak.armed = true;
			streak.bonusUntil = now + BONUS_DURATION;
		}
		return rolledHit;
	}

	public static int boostDamage(final Player player, final RPEntity target, final int damage) {
		return boostDamage(player, target, damage, System.currentTimeMillis());
	}

	static int boostDamage(final Player player, final RPEntity target, final int damage, final long now) {
		final Streak streak = STREAKS.get(player.getStatusList());
		if (damage <= 0 || player.getHP() <= 0 || streak == null || streak.bonusUntil <= now
				|| streak.target.get() != target || streak.weapon.get() != signature(player)
				|| streak.zone.get() != player.getZone() || player.getZone() != target.getZone()) {
			return damage;
		}
		return (int) Math.min(Integer.MAX_VALUE, Math.round(damage * 1.20));
	}

	public static void reset(final Player player) {
		STREAKS.remove(player.getStatusList());
	}

	private static Item signature(final Player player) {
		for (Item weapon : player.getWeapons()) {
			if (AdditionalCombatAffixDefinition.INEVITABILITY.value(weapon) > 0.0) {
				return weapon;
			}
		}
		return null;
	}

	private static final class Streak {
		final WeakReference<RPEntity> target;
		final WeakReference<Item> weapon;
		final WeakReference<StendhalRPZone> zone;
		int misses;
		boolean armed;
		long bonusUntil;

		Streak(final RPEntity target, final Item weapon, final StendhalRPZone zone) {
			this.target = new WeakReference<>(target);
			this.weapon = new WeakReference<>(weapon);
			this.zone = new WeakReference<>(zone);
		}
	}
}
