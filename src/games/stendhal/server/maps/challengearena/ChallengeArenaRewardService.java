/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.maps.challengearena;

import games.stendhal.common.MathHelper;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.rarity.ItemCreationContext;
import games.stendhal.server.entity.item.ChallengeArenaRewardChest;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.item.StackableItem;
import games.stendhal.server.entity.player.Player;

/** Handles non-money completion rewards and persistent Challenge Arena stats. */
public final class ChallengeArenaRewardService {
	public static final String STATS_SLOT = "challenge_arena_stats";
	public static final String REWARD_CHEST_NAME = "skrzynia Areny Wyzwań";

	private ChallengeArenaRewardService() {
	}

	public static void recordEntry(final Player player,
			final ChallengeArenaTier tier) {
		if (player == null || tier == null) {
			return;
		}
		final long totalSpent = getLong(player, 3) + tier.getStake();
		player.setQuest(STATS_SLOT, 3, Long.toString(totalSpent));
	}

	public static int getWins(final Player player) {
		return player == null ? 0 : getInt(player, 0);
	}

	public static int getBestStake(final Player player) {
		return player == null ? 0 : getInt(player, 1);
	}

	public static long getBestTimeMillis(final Player player) {
		return player == null ? 0L : getLong(player, 2);
	}

	public static long getTotalSpent(final Player player) {
		return player == null ? 0L : getLong(player, 3);
	}

	public static void rewardVictory(final Player player,
			final ChallengeArenaTier tier, final long durationMillis) {
		if (player == null || tier == null) {
			return;
		}

		final int completionXp = Math.max(1, tier.getStake() / 20);
		player.addXP(completionXp);

		final int fragments = getFragmentReward(tier);
		final StackableItem fragment = (StackableItem) SingletonRepository
				.getEntityManager().getItem("fragment glifu");
		fragment.setQuantity(fragments);
		player.equipOrPutOnGround(fragment);
		player.incObtainedForItem(fragment.getName(), fragments);

		final boolean chestAwarded = tier.awardsEquipmentChest()
				&& awardEquipmentChest(player, tier);

		final int wins = getInt(player, 0) + 1;
		player.setQuest(STATS_SLOT, 0, Integer.toString(wins));

		final int bestStake = getInt(player, 1);
		if (tier.getStake() > bestStake) {
			player.setQuest(STATS_SLOT, 1, Integer.toString(tier.getStake()));
			player.setQuest(STATS_SLOT, 2, Long.toString(durationMillis));
		} else if (tier.getStake() == bestStake) {
			final long bestTime = getLong(player, 2);
			if (bestTime <= 0L || durationMillis < bestTime) {
				player.setQuest(STATS_SLOT, 2, Long.toString(durationMillis));
			}
		}

		ChallengeArenaRankingService.updateRanking(player);

		String message = "Ukończyłeś Arenę Wyzwań. Otrzymujesz "
				+ completionXp + " punktów doświadczenia oraz "
				+ formatFragmentReward(fragments) + ".";
		if (chestAwarded) {
			message += " Otrzymujesz również skrzynię z losowym wyposażeniem.";
		}
		player.sendPrivateText(message);
	}

	private static String formatFragmentReward(final int fragments) {
		return fragments == 1 ? "1 fragment glifu"
				: fragments + " fragmentów glifu";
	}

	private static boolean awardEquipmentChest(final Player player,
			final ChallengeArenaTier tier) {
		final Item item = SingletonRepository.getEntityManager().getItem(
				REWARD_CHEST_NAME, ItemCreationContext.defaultCreation());
		if (!(item instanceof ChallengeArenaRewardChest)) {
			return false;
		}
		final ChallengeArenaRewardChest chest = (ChallengeArenaRewardChest) item;
		chest.setItemData(ChallengeArenaRewardChest.createRewardData(
				tier, Math.max(1, player.getLevel())));
		chest.setBoundTo(player.getName());
		chest.setPersistent(true);
		player.equipOrPutOnGround(chest);
		player.incObtainedForItem(chest.getName(), 1);
		return true;
	}

	static int getFragmentReward(final ChallengeArenaTier tier) {
		switch (tier) {
			case TRIAL:
			case SKIRMISH:
				return 1;
			case HUNTER:
				return 2;
			case VETERAN:
				return 3;
			case CHAMPION:
				return 4;
			case LEGEND:
				return 6;
			default:
				return 1;
		}
	}

	private static int getInt(final Player player, final int index) {
		return MathHelper.parseInt(player.getQuest(STATS_SLOT, index));
	}

	private static long getLong(final Player player, final int index) {
		final String value = player.getQuest(STATS_SLOT, index);
		if (value == null || value.length() == 0) {
			return 0L;
		}
		try {
			return Long.parseLong(value);
		} catch (final NumberFormatException e) {
			return 0L;
		}
	}
}
