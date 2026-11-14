/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import java.util.Map;
import java.util.WeakHashMap;

import games.stendhal.server.core.rule.damage.ArmorPoolService.DamageResult;
import games.stendhal.server.core.rule.rarity.AdditionalCombatAffixDefinition;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.RPEntity;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StatusList;

/** Incoming hit protection; persistent cooldowns belong to the character, never the item. */
public final class SurvivalAffixService {
	public static final String IRON_RESOLVE_COOLDOWN_ATTRIBUTE = "iron_resolve_ready_at";
	private static final long VIGILANCE_PAUSE = 10000L;
	private static final long IRON_RESOLVE_COOLDOWN = 120000L;
	private static final Map<StatusList, Map<Entity, Long>> LAST_HITS = new WeakHashMap<>();

	private SurvivalAffixService() { }

	public static int reduceDirectHit(final RPEntity defender, final Entity attacker, final int damage) {
		return defender instanceof Player
				? reduceDirectHit((Player) defender, attacker, damage, System.currentTimeMillis()) : damage;
	}

	static int reduceDirectHit(final Player player, final Entity attacker, final int damage, final long now) {
		if (damage <= 0 || player.getHP() <= 0 || !(attacker instanceof RPEntity) || attacker == player) {
			return damage;
		}
		final Map<Entity, Long> previousHits = LAST_HITS.computeIfAbsent(player.getStatusList(), key -> new WeakHashMap<>());
		final Long previous = previousHits.put(attacker, now);
		if (previous != null && now - previous < VIGILANCE_PAUSE) {
			return damage;
		}
		double reduction = 0.0;
		for (Item item : player.getDefenseItems()) {
			reduction += AdditionalCombatAffixDefinition.VIGILANCE.value(item);
		}
		return Math.max(1, (int) Math.round(damage * (1.0 - Math.min(0.25, reduction))));
	}

	public static DamageResult guardFatalHit(final RPEntity defender, final DamageResult damage) {
		return defender instanceof Player
				? guardFatalHit((Player) defender, damage, System.currentTimeMillis()) : damage;
	}

	static DamageResult guardFatalHit(final Player player, final DamageResult damage, final long now) {
		if (player.getHP() <= 0 || damage.getHealthDamage() < player.getHP() || !hasIronResolve(player)) {
			return damage;
		}
		long readyAt = 0L;
		if (player.has(IRON_RESOLVE_COOLDOWN_ATTRIBUTE)) {
			try {
				readyAt = Long.parseLong(player.get(IRON_RESOLVE_COOLDOWN_ATTRIBUTE));
			} catch (NumberFormatException ignored) {
				// Older or damaged saves must not prevent ordinary combat from resolving.
			}
		}
		if (readyAt > now) {
			return damage;
		}
		player.put(IRON_RESOLVE_COOLDOWN_ATTRIBUTE, Long.toString(now + IRON_RESOLVE_COOLDOWN));
		return damage.withMaximumHealthDamage(player.getHP() - 1);
	}

	private static boolean hasIronResolve(final Player player) {
		for (Item item : player.getDefenseItems()) {
			if (AdditionalCombatAffixDefinition.IRON_RESOLVE.value(item) > 0.0) {
				return true;
			}
		}
		return false;
	}
}
