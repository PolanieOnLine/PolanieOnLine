/***************************************************************************
 *                 (C) Copyright 2026 - PolanieOnLine                     *
 ***************************************************************************/
package games.stendhal.server.entity.player;

import games.stendhal.common.Level;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rp.achievement.AchievementNotifier;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.Item;
import marauroa.common.game.RPObject;
import marauroa.common.game.RPSlot;

/**
 * Permanent character reborn progression.
 *
 * Reborns are character progression, not quests. The first five reborns grant
 * capped combat and health bonuses. Later reborns remain available without
 * increasing character power further.
 */
public final class RebornSystem {
	public static final String ATTR_REBORNS = "reborns";
	public static final String ATTR_REWARDS = "reborn_rewards";
	public static final String ATTR_MIGRATION_VERSION = "reborn_migration_version";

	private static final int MIGRATION_VERSION = 1;
	private static final int POWER_REBORN_LIMIT = 5;
	private static final int ATTACK_BONUS_PER_REBORN = 2;
	private static final int COMBAT_XP_BONUS_PER_REBORN = 5;
	private static final int HEALTH_PER_LEVEL = 10;

	private static final int REWARD_THIRD = 1;
	private static final int REWARD_FOURTH = 2;
	private static final int REWARD_FIFTH = 4;

	private static final String LEGACY_REBORN_SLOT = "reset_level";
	private static final String LEGACY_REWARD_THIRD = "reborn_extra_reward3";
	private static final String LEGACY_REWARD_FOURTH = "reborn_extra_reward4";
	private static final String LEGACY_REWARD_FIFTH = "reborn_extra_reward5";

	private static final String LEFT_DAGGER = "sztylet leworęczny";
	private static final String RIGHT_DAGGER = "sztylet praworęczny";
	private static final String MITHRIL_AMULET = "amulecik z mithrilu";
	private static final String EXCALIBUR = "ekskalibur";

	private RebornSystem() {
		// utility class
	}

	public static int getRebornCount(final Player player) {
		if (player == null || !player.has(ATTR_REBORNS)) {
			return 0;
		}
		return Math.max(0, player.getInt(ATTR_REBORNS));
	}

	public static int getAttackBonusPercent(final Player player) {
		return getPowerRebornCount(player) * ATTACK_BONUS_PER_REBORN;
	}

	public static int getCombatExperienceBonusPercent(final Player player) {
		return getPowerRebornCount(player) * COMBAT_XP_BONUS_PER_REBORN;
	}

	public static int getHealthBonus(final Player player) {
		final int reborns = getPowerRebornCount(player);
		if (reborns <= 0) {
			return 0;
		}
		if (reborns < POWER_REBORN_LIMIT) {
			return reborns * 1000;
		}
		return 6000;
	}

	public static int applyAttackBonus(final Player player, final int damage) {
		if (damage <= 0) {
			return damage;
		}
		return scaleByPercent(damage, getAttackBonusPercent(player));
	}

	public static int applyCombatExperienceBonus(final Player player,
			final int experience) {
		if (experience <= 0) {
			return experience;
		}
		return scaleByPercent(experience, getCombatExperienceBonusPercent(player));
	}

	private static int scaleByPercent(final int value, final int bonusPercent) {
		if (bonusPercent <= 0) {
			return value;
		}
		return Math.max(value,
				(int) Math.round(value * (1.0 + bonusPercent / 100.0)));
	}

	private static int getPowerRebornCount(final Player player) {
		return Math.min(POWER_REBORN_LIMIT, getRebornCount(player));
	}

	private static int getHealthRewardForReborn(final int reborn) {
		if (reborn >= 1 && reborn <= 4) {
			return 1000;
		}
		if (reborn == 5) {
			return 2000;
		}
		return 0;
	}

	private static int getLevelHealthToReset() {
		return Level.maxLevel() * HEALTH_PER_LEVEL;
	}

	public static boolean canReborn(final Player player) {
		return player != null && player.getLevel() == Level.maxLevel();
	}

	/**
	 * Performs one reborn and returns the new reborn count.
	 *
	 * @return new reborn count or -1 when the player cannot reborn
	 */
	public static int performReborn(final Player player) {
		if (!canReborn(player)) {
			return -1;
		}

		final int reborns = getRebornCount(player) + 1;
		final int healthReward = getHealthRewardForReborn(reborns);
		final int levelHealth = getLevelHealthToReset();
		final int resetBaseHP = player.getBaseHP() - levelHealth + healthReward;
		final int resetHP = player.getHP() - levelHealth + healthReward;

		player.put(ATTR_REBORNS, reborns);
		RebornDisplay.sync(player);
		player.setXP(0);
		player.setLevel(0);

		// Keep the health reset compatible with the old Yerena implementation.
		// Only health gained from the maximum level is removed. Permanent health
		// from quests and earlier reborns remains on the character.
		player.setBaseHP(resetBaseHP);
		player.setHP(resetHP);

		claimPendingRewards(player);

		// A reborn lowers the level, so the normal level-up achievement hook does
		// not run. Trigger its achievement check after the new reborn count exists.
		AchievementNotifier.get().onLevelChange(player);
		player.notifyWorldAboutChanges();
		return reborns;
	}

	/**
	 * Recalculates HP through the normal compatibility converter and then adds the
	 * reborn health component. Missing health is preserved across login repairs.
	 */
	public static void updateBaseHP(final Player player) {
		if (player == null || player.getAdminLevel() > 0) {
			return;
		}
		final int missingHealth = Math.max(0,
				player.getBaseHP() - player.getHP());
		updateBaseHP(player, missingHealth);
	}

	private static void updateBaseHP(final Player player,
			final int missingHealth) {
		UpdateConverter.updateBaseHP(player);
		final int expectedBaseHP = player.getBaseHP() + getHealthBonus(player);
		if (player.getBaseHP() != expectedBaseHP) {
			player.setBaseHP(expectedBaseHP);
		}
		player.setHP(Math.max(0, expectedBaseHP - missingHealth));
	}

	/**
	 * Converts the old quest based reborn states to permanent player attributes.
	 * It is safe to call on every login.
	 */
	public static void migrateLegacyData(final Player player) {
		if (player == null) {
			return;
		}
		if (player.has(ATTR_MIGRATION_VERSION)
				&& player.getInt(ATTR_MIGRATION_VERSION) >= MIGRATION_VERSION) {
			RebornDisplay.sync(player);
			return;
		}

		final int reborns = player.has(ATTR_REBORNS)
				? Math.max(0, player.getInt(ATTR_REBORNS))
				: getLegacyRebornCount(player.getQuest(LEGACY_REBORN_SLOT));
		int rewards = player.has(ATTR_REWARDS)
				? Math.max(0, player.getInt(ATTR_REWARDS)) : 0;

		if (reborns >= 3 && legacyRewardWasClaimed(player,
				LEGACY_REWARD_THIRD, LEFT_DAGGER, RIGHT_DAGGER)) {
			rewards |= REWARD_THIRD;
		}
		if (reborns >= 4 && legacyRewardWasClaimed(player,
				LEGACY_REWARD_FOURTH, MITHRIL_AMULET)) {
			rewards |= REWARD_FOURTH;
		}
		if (reborns >= 5 && legacyRewardWasClaimed(player,
				LEGACY_REWARD_FIFTH, EXCALIBUR)) {
			rewards |= REWARD_FIFTH;
		}

		player.put(ATTR_REBORNS, reborns);
		player.put(ATTR_REWARDS, rewards);

		player.removeQuest(LEGACY_REBORN_SLOT);
		player.removeQuest(LEGACY_REWARD_THIRD);
		player.removeQuest(LEGACY_REWARD_FOURTH);
		player.removeQuest(LEGACY_REWARD_FIFTH);

		player.put(ATTR_MIGRATION_VERSION, MIGRATION_VERSION);
		RebornDisplay.sync(player);
	}

	static int getLegacyRebornCount(final String state) {
		if (state == null || state.length() == 0 || "start".equals(state)) {
			return 0;
		}

		if (state.startsWith("start;")) {
			return Math.max(0, parsePositiveInt(state.substring("start;".length())) - 1);
		}
		if ("done".equals(state) || "done_reborn".equals(state)) {
			return 1;
		}
		if (state.startsWith("done;reborn_")) {
			return parsePositiveInt(state.substring("done;reborn_".length()));
		}
		if (state.startsWith("done;")) {
			return parsePositiveInt(state.substring("done;".length()));
		}

		int reborns = 0;
		for (final String part : state.split(";")) {
			if (part.startsWith("reborn_")) {
				reborns = Math.max(reborns,
						parsePositiveInt(part.substring("reborn_".length())));
			}
		}
		return reborns;
	}

	private static int parsePositiveInt(final String value) {
		try {
			return Math.max(0, Integer.parseInt(value));
		} catch (final NumberFormatException e) {
			return 0;
		}
	}

	private static boolean legacyRewardWasClaimed(final Player player,
			final String questSlot, final String... itemNames) {
		if (player.isQuestInState(questSlot, "done")) {
			return true;
		}
		for (final String itemName : itemNames) {
			if (!hasBoundItem(player, itemName)) {
				return false;
			}
		}
		return true;
	}

	public static boolean hasPendingRewards(final Player player) {
		final int reborns = getRebornCount(player);
		final int rewards = getRewardMask(player);
		return (reborns >= 3 && (rewards & REWARD_THIRD) == 0)
				|| (reborns >= 4 && (rewards & REWARD_FOURTH) == 0)
				|| (reborns >= 5 && (rewards & REWARD_FIFTH) == 0);
	}

	/** Grants any milestone rewards still owed to the player. */
	public static boolean claimPendingRewards(final Player player) {
		if (player == null) {
			return false;
		}
		final int reborns = getRebornCount(player);
		int rewards = getRewardMask(player);
		boolean granted = false;

		if (reborns >= 3 && (rewards & REWARD_THIRD) == 0) {
			final boolean left = hasBoundItem(player, LEFT_DAGGER)
					|| giveBoundItem(player, LEFT_DAGGER);
			final boolean right = hasBoundItem(player, RIGHT_DAGGER)
					|| giveBoundItem(player, RIGHT_DAGGER);
			if (left && right) {
				rewards |= REWARD_THIRD;
				granted = true;
			}
		}
		if (reborns >= 4 && (rewards & REWARD_FOURTH) == 0) {
			if (hasBoundItem(player, MITHRIL_AMULET)
					|| giveBoundItem(player, MITHRIL_AMULET)) {
				rewards |= REWARD_FOURTH;
				granted = true;
			}
		}
		if (reborns >= 5 && (rewards & REWARD_FIFTH) == 0) {
			if (hasBoundItem(player, EXCALIBUR)
					|| giveBoundItem(player, EXCALIBUR)) {
				rewards |= REWARD_FIFTH;
				granted = true;
			}
		}

		player.put(ATTR_REWARDS, rewards);
		return granted;
	}

	public static int getClaimedRewardCount(final Player player) {
		final int rewards = getRewardMask(player);
		int count = 0;
		if ((rewards & REWARD_THIRD) != 0) {
			count++;
		}
		if ((rewards & REWARD_FOURTH) != 0) {
			count++;
		}
		if ((rewards & REWARD_FIFTH) != 0) {
			count++;
		}
		return count;
	}

	private static int getRewardMask(final Player player) {
		if (player == null || !player.has(ATTR_REWARDS)) {
			return 0;
		}
		return Math.max(0, player.getInt(ATTR_REWARDS));
	}

	private static boolean giveBoundItem(final Player player,
			final String itemName) {
		final Item item = SingletonRepository.getEntityManager().getItem(
				itemName, ItemCreationContext.questReward());
		if (item == null) {
			return false;
		}
		item.setBoundTo(player.getName());
		player.equipOrPutOnGround(item);
		return true;
	}

	private static boolean hasBoundItem(final Player player,
			final String itemName) {
		for (final RPSlot slot : player.slots()) {
			if (hasBoundItem(player, slot, itemName)) {
				return true;
			}
		}
		return false;
	}

	private static boolean hasBoundItem(final Player player, final RPSlot slot,
			final String itemName) {
		for (final RPObject object : slot) {
			if (object instanceof Item) {
				final Item item = (Item) object;
				if (itemName.equals(item.getName())
						&& player.getName().equals(item.getBoundTo())) {
					return true;
				}
			}
			for (final RPSlot child : object.slots()) {
				if (hasBoundItem(player, child, itemName)) {
					return true;
				}
			}
		}
		return false;
	}
}
