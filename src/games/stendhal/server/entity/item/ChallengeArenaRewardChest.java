/***************************************************************************
 *                   (C) Copyright 2026 - PolanieOnLine                   *
 ***************************************************************************/
package games.stendhal.server.entity.item;

import java.util.Map;

import games.stendhal.server.maps.challengearena.ChallengeArenaLootService;
import games.stendhal.server.maps.challengearena.ChallengeArenaTier;
import games.stendhal.server.entity.player.Player;

/** Bound chest containing one equipment roll earned in Challenge Arena. */
public final class ChallengeArenaRewardChest extends StackableBox {
	private static final String DATA_SEPARATOR = ";";

	public ChallengeArenaRewardChest(final String name, final String clazz,
			final String subclass, final Map<String, String> attributes) {
		super(name, clazz, subclass, attributes);
	}

	public ChallengeArenaRewardChest(final ChallengeArenaRewardChest item) {
		super(item);
	}

	@Override
	protected boolean useMe(final Player player) {
		final RewardData data = parseRewardData(getItemData());
		if (data == null) {
			player.sendPrivateText("Ta skrzynia nie ma poprawnie zapisanej nagrody Areny Wyzwań.");
			return false;
		}
		if (isBound() && !player.getName().equals(getBoundTo())) {
			player.sendPrivateText("Ta skrzynia należy do innego wojownika.");
			return false;
		}

		final Item reward = ChallengeArenaLootService.createEquipmentReward(
				data.playerLevel, data.tier);
		if (reward == null) {
			player.sendPrivateText("Nie udało się przygotować nagrody. Spróbuj otworzyć skrzynię ponownie później.");
			return false;
		}

		removeOne();
		addRewardToInventory(player, reward);
		player.incObtainedForItem(reward.getName(), reward.getQuantity());
		player.notifyWorldAboutChanges();
		player.sendPrivateText("Ze skrzyni Areny Wyzwań otrzymujesz "
				+ reward.getName() + " o rzadkości "
				+ reward.getRarityOrCommon().getId() + ".");
		return true;
	}

	public static String createRewardData(final ChallengeArenaTier tier,
			final int playerLevel) {
		if (tier == null || playerLevel < 1) {
			throw new IllegalArgumentException("Invalid Challenge Arena reward data");
		}
		return tier.name() + DATA_SEPARATOR + playerLevel;
	}

	static RewardData parseRewardData(final String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		final String[] parts = value.split(DATA_SEPARATOR);
		if (parts.length != 2) {
			return null;
		}
		try {
			final ChallengeArenaTier tier = ChallengeArenaTier.valueOf(parts[0]);
			final int playerLevel = Integer.parseInt(parts[1]);
			if (!tier.awardsEquipmentChest() || playerLevel < 1) {
				return null;
			}
			return new RewardData(tier, playerLevel);
		} catch (final IllegalArgumentException e) {
			return null;
		}
	}

	static final class RewardData {
		private final ChallengeArenaTier tier;
		private final int playerLevel;

		RewardData(final ChallengeArenaTier tier, final int playerLevel) {
			this.tier = tier;
			this.playerLevel = playerLevel;
		}

		ChallengeArenaTier getTier() {
			return tier;
		}

		int getPlayerLevel() {
			return playerLevel;
		}
	}
}
