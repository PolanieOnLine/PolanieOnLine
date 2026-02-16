/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                     *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.item.Item;
import games.stendhal.server.entity.player.Player;

/**
 * Awards additional chest rewards for top performers in random map events.
 */
public final class EventActivityChestRewardService {
	private static final int GOLD_TOP_INDEX = 0;
	private static final int SILVER_TOP_INDEX = 2;
	private static final int BRONZE_TOP_INDEX = 9;

	private EventActivityChestRewardService() {
		// utility class
	}

	public static int awardTopActivityChests(final String eventDisplayName,
			final List<QualifiedParticipant> participants) {
		if (participants == null || participants.isEmpty()) {
			return 0;
		}

		final List<QualifiedParticipant> ranked = new ArrayList<>(participants);
		ranked.sort(Comparator
				.comparingDouble(QualifiedParticipant::getActivityScore).reversed()
				.thenComparing(Comparator.comparingInt(QualifiedParticipant::getDamage).reversed())
				.thenComparing(Comparator.comparingInt(QualifiedParticipant::getKillAssists).reversed())
				.thenComparing(participant -> participant.getPlayer().getName(), String.CASE_INSENSITIVE_ORDER));

		int awarded = 0;
		for (int index = 0; index < ranked.size(); index++) {
			final ChestTier tier = resolveTier(index);
			if (tier == null) {
				continue;
			}

			final QualifiedParticipant participant = ranked.get(index);
			final Player player = participant.getPlayer();
			final Item chest = SingletonRepository.getEntityManager().getItem(tier.itemName);
			if (chest == null) {
				continue;
			}

			chest.setBoundTo(player.getName());
			player.equipOrPutOnGround(chest);
			player.notifyWorldAboutChanges();
			player.sendPrivateText("Za aktywność podczas wydarzenia " + eventDisplayName + " otrzymujesz "
					+ tier.displayName + ".");
			awarded++;
		}

		return awarded;
	}

	private static ChestTier resolveTier(final int rankingIndex) {
		if (rankingIndex <= GOLD_TOP_INDEX) {
			return ChestTier.GOLD;
		}
		if (rankingIndex <= SILVER_TOP_INDEX) {
			return ChestTier.SILVER;
		}
		if (rankingIndex <= BRONZE_TOP_INDEX) {
			return ChestTier.BRONZE;
		}
		return null;
	}

	public static final class QualifiedParticipant {
		private final Player player;
		private final double activityScore;
		private final int damage;
		private final int killAssists;

		public QualifiedParticipant(final Player player, final double activityScore, final int damage,
				final int killAssists) {
			this.player = Objects.requireNonNull(player, "player");
			this.activityScore = activityScore;
			this.damage = Math.max(0, damage);
			this.killAssists = Math.max(0, killAssists);
		}

		public Player getPlayer() {
			return player;
		}

		public double getActivityScore() {
			return activityScore;
		}

		public int getDamage() {
			return damage;
		}

		public int getKillAssists() {
			return killAssists;
		}
	}

	private enum ChestTier {
		GOLD("złota skrzynia", "złotą skrzynię"),
		SILVER("srebrna skrzynia", "srebrną skrzynię"),
		BRONZE("brązowa skrzynia", "brązową skrzynię");

		private final String itemName;
		private final String displayName;

		ChestTier(final String itemName, final String displayName) {
			this.itemName = itemName;
			this.displayName = displayName;
		}
	}
}
