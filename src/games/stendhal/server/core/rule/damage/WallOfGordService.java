/***************************************************************************
 *                   (C) Copyright 2003-2026 - Stendhal                    *
 ***************************************************************************/
package games.stendhal.server.core.rule.damage;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import games.stendhal.server.core.rule.rarity.EquipmentAffixService;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Runtime and materialization rules for the legendary Wall of the Gord affix. */
public final class WallOfGordService {
	public static final String ATTRIBUTE = "legendary_wall_of_gord";
	public static final double TRIGGER_MAX_HP_FRACTION = 0.10;
	public static final double DAMAGE_REDUCTION = 0.35;
	public static final long COOLDOWN_MILLISECONDS = 8000L;

	private static final Map<Player, Long> COOLDOWNS =
			Collections.synchronizedMap(new WeakHashMap<Player, Long>());

	private WallOfGordService() {
		// utility class
	}

	/** Fresh Wall of the Gord signatures are available only on defensive armour. */
	public static boolean isEligible(final Item item) {
		return EquipmentAffixService.isArmour(item) && !item.has(ATTRIBUTE);
	}

	/** Materializes the signature as a marker without changing item DEF. */
	public static boolean apply(final Item item, final Random random) {
		if (!isEligible(item)) {
			return false;
		}
		if (random == null) {
			throw new IllegalArgumentException("Random source must not be null");
		}
		item.put(ATTRIBUTE, 1.0);
		return true;
	}

	/**
	 * Reduces one resolved direct Creature hit when it is large enough and the
	 * legendary cooldown is ready. The caller must invoke this after critical-hit
	 * multiplication but before HP limiting, lifesteal and onDamaged().
	 *
	 * @param player defender
	 * @param incomingDamage resolved direct-hit damage before this signature
	 * @return actual direct-hit damage after this signature
	 */
	public static int reduceDirectCreatureHit(final Player player,
			final int incomingDamage) {
		return reduceDirectCreatureHit(player, incomingDamage,
				System.currentTimeMillis());
	}

	static int reduceDirectCreatureHit(final Player player,
			final int incomingDamage, final long now) {
		if (player == null || incomingDamage <= 0 || !hasWallOfGord(player)) {
			return incomingDamage;
		}

		final int maxHP = player.getBaseHP();
		if (maxHP <= 0) {
			return incomingDamage;
		}
		final int triggerDamage = Math.max(1,
				(int) Math.ceil(maxHP * TRIGGER_MAX_HP_FRACTION));
		if (incomingDamage < triggerDamage) {
			return incomingDamage;
		}

		synchronized (COOLDOWNS) {
			final Long readyAt = COOLDOWNS.get(player);
			if (readyAt != null && now < readyAt.longValue()) {
				return incomingDamage;
			}
			COOLDOWNS.put(player, Long.valueOf(now + COOLDOWN_MILLISECONDS));
		}

		return Math.max(1, (int) Math.round(incomingDamage
				* (1.0 - DAMAGE_REDUCTION)));
	}

	private static boolean hasWallOfGord(final Player player) {
		for (final Item item : player.getDefenseItems()) {
			if (item != null && item.has(ATTRIBUTE)) {
				return true;
			}
		}
		return false;
	}

	static void clearCooldownsForTests() {
		COOLDOWNS.clear();
	}
}
