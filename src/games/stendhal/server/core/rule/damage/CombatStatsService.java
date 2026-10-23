/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import java.util.LinkedHashMap;
import java.util.Map;

import games.stendhal.common.constants.CombatStat;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.status.StatusType;

/** Read-only combat calculation snapshot, published only when its values change. */
public final class CombatStatsService {
	private CombatStatsService() { }

	public static boolean update(final Player player) {
		final Map<String, String> snapshot = new LinkedHashMap<String, String>();
		for (CombatStat stat : CombatStat.values()) {
			final double value;
			final double limit;
			switch (stat) {
			case CRITICAL_CHANCE:
				value = CriticalHitService.getCriticalChance(player);
				limit = CriticalHitService.MAX_CRITICAL_CHANCE;
				break;
			case CRITICAL_DAMAGE_BONUS:
				value = 100.0 * (CriticalHitService.getCriticalDamageMultiplier(player)
						- CriticalHitService.BASE_CRITICAL_DAMAGE_MULTIPLIER);
				limit = 100.0 * CriticalHitService.MAX_CRITICAL_DAMAGE_BONUS;
				break;
			case PARRY_CHANCE:
				value = 100.0 * ParryService.getParryChance(player);
				limit = 100.0 * ParryService.MAX_PARRY_CHANCE;
				break;
			default:
				final StatusType status = StatusType.valueOf(stat.name().substring("RESIST_".length()));
				value = 100.0 * EquipmentStatusResistanceService.getResistance(player, status);
				limit = 100.0 * EquipmentStatusResistanceService.getMaximumResistance(player, status);
			}
			snapshot.put(stat.getKey(), Double.toString(value));
			snapshot.put(stat.getLimitKey(), Double.toString(limit));
		}
		if (player.hasMap(CombatStat.ATTRIBUTE) && snapshot.equals(player.getMap(CombatStat.ATTRIBUTE))) {
			return false;
		}
		for (Map.Entry<String, String> entry : snapshot.entrySet()) {
			if (!entry.getValue().equals(player.get(CombatStat.ATTRIBUTE, entry.getKey()))) {
				player.put(CombatStat.ATTRIBUTE, entry.getKey(), entry.getValue());
			}
		}
		player.notifyWorldAboutChanges();
		return true;
	}
}
