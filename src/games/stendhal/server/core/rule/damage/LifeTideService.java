/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.server.core.rule.damage;

import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/** Potion-only temporary HP above the player's unchanged maximum health. */
public final class LifeTideService {
	public static final String ATTRIBUTE = "legendary_life_tide";
	public static final String DECAY_ATTRIBUTE = "life_tide_decay";
	private static final long GRACE_MILLISECONDS = 10000L;
	private static final long DECAY_INTERVAL_MILLISECONDS = 2000L;
	private static final int DECAY_HP = 20;
	private static final Map<Player, Long> NEXT_DECAY = new WeakHashMap<Player, Long>();

	private LifeTideService() { }

	public static boolean isEligible(final Item item) {
		if (item == null || item.has(ATTRIBUTE)) {
			return false;
		}
		return "armor".equals(item.getItemClass()) || "helmet".equals(item.getItemClass())
				|| "belt".equals(item.getItemClass()) || "belts".equals(item.getItemClass());
	}

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

	public static int healFromPotion(final Player player, final int amount) {
		return healFromPotion(player, amount, System.currentTimeMillis());
	}

	static int healFromPotion(final Player player, final int amount, final long now) {
		if (amount <= 0 || player.getHP() <= 0 || player.has("dead")) {
			return 0;
		}
		if (!isActive(player)) {
			return player.heal(amount, true);
		}
		// Process already-due decay before adding healing, without restarting its timer.
		tick(player, now);
		final int before = player.getHP();
		final int maximum = maximumHP(player);
		final int given = (int) Math.min(amount, Math.max(0L, (long) maximum - before));
		if (given > 0) {
			player.setHP(before + given);
			player.put("heal", given);
		}
		if (player.getHP() > player.getBaseHP()
				&& (before <= player.getBaseHP() || !NEXT_DECAY.containsKey(player))) {
			NEXT_DECAY.put(player, now + GRACE_MILLISECONDS);
		}
		return given;
	}

	public static void tick(final Player player) {
		tick(player, System.currentTimeMillis());
	}

	static void tick(final Player player, final long now) {
		if (player.getHP() <= player.getBaseHP()) {
			NEXT_DECAY.remove(player);
			return;
		}
		if (player.isDisconnected() || player.has("dead") || !isActive(player)) {
			reset(player);
			player.notifyWorldAboutChanges();
			return;
		}
		if (player.getHP() > maximumHP(player)) {
			player.setHP(maximumHP(player));
			player.notifyWorldAboutChanges();
		}
		final Long next = NEXT_DECAY.get(player);
		if (next == null || now < next.longValue()) {
			return;
		}
		final long pulses = 1 + (now - next.longValue()) / DECAY_INTERVAL_MILLISECONDS;
		final long loss = Math.min((long) player.getHP() - player.getBaseHP(),
				Math.min(pulses, Integer.MAX_VALUE) * DECAY_HP);
		player.setHP(player.getHP() - (int) loss);
		// A session counter distinguishes natural decay from simultaneous combat damage.
		final int decayed = player.has(DECAY_ATTRIBUTE) ? player.getInt(DECAY_ATTRIBUTE) : 0;
		player.put(DECAY_ATTRIBUTE, decayed > Integer.MAX_VALUE - loss ? (int) loss : decayed + (int) loss);
		if (player.getHP() > player.getBaseHP()) {
			NEXT_DECAY.put(player, now - (now - next.longValue()) % DECAY_INTERVAL_MILLISECONDS
					+ DECAY_INTERVAL_MILLISECONDS);
		} else {
			NEXT_DECAY.remove(player);
		}
		player.notifyWorldAboutChanges();
	}

	/** Equipment changes must not let a player retain an unequipped signature. */
	public static void reconcileEquipment(final Player player) {
		if (player.getHP() > player.getBaseHP() && !isActive(player)) {
			reset(player);
		}
	}

	/** Called before removal, while the item is still in its original slot. */
	public static void onUnequipped(final Player player, final Item removed) {
		if (removed.has(ATTRIBUTE)) {
			for (final Item equipped : player.getDefenseItems()) {
				if (equipped == removed) {
					reset(player);
					return;
				}
			}
		}
	}

	/** Surplus is transient and cannot be saved for a later session or life. */
	public static void reset(final Player player) {
		NEXT_DECAY.remove(player);
		if (player.getHP() > player.getBaseHP()) {
			player.setHP(player.getBaseHP());
		}
	}

	private static int maximumHP(final Player player) {
		final long maximum = player.getBaseHP();
		return (int) Math.min(Integer.MAX_VALUE, maximum + maximum / 5);
	}

	public static boolean isActive(final Player player) {
		for (final Item item : player.getDefenseItems()) {
			if (item != null && item.has(ATTRIBUTE) && item.getDouble(ATTRIBUTE) > 0.0) {
				return true;
			}
		}
		return false;
	}
}
