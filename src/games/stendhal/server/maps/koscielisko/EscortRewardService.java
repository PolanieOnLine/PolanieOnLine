package games.stendhal.server.maps.koscielisko;

import java.util.List;
import java.util.Set;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.entity.player.Player;

final class EscortRewardService {
	void payout(final double finalHealthRatio, final Set<String> rewardEligiblePlayers, final List<Player> playersInObserverZones) {
		final RewardTier tier = RewardTier.fromHealthRatio(finalHealthRatio);
		if (tier == RewardTier.NONE) {
			return;
		}
		int rewardedPlayers = 0;
		for (final Player player : playersInObserverZones) {
			if (player == null || !rewardEligiblePlayers.contains(player.getName())) {
				continue;
			}
			player.addXP(tier.xpReward);
			player.addKarma(tier.karmaReward);
			player.sendPrivateText("[Kościelisko] Za obronę szlaku: +" + tier.xpReward + " XP, +" + tier.karmaReward
					+ " karmy (siły Wielkoluda: " + Math.round(finalHealthRatio * 100.0d) + "%).");
			rewardedPlayers++;
		}
		if (rewardedPlayers == 0) {
			SingletonRepository.getRuleProcessor().tellAllPlayers(
					NotificationType.PRIVMSG,
					"Szlak obroniony, ale nikt nie utrzymał tempa do nagrody.");
		}
	}

	enum RewardTier {
		HIGH(0.8d, 220000, 20.0d),
		MEDIUM(0.5d, 140000, 12.0d),
		LOW(0.2d, 80000, 7.0d),
		NONE(0.0d, 0, 0.0d);

		private final double threshold;
		private final int xpReward;
		private final double karmaReward;

		RewardTier(final double threshold, final int xpReward, final double karmaReward) {
			this.threshold = threshold;
			this.xpReward = xpReward;
			this.karmaReward = karmaReward;
		}

		static RewardTier fromHealthRatio(final double healthRatio) {
			if (healthRatio >= HIGH.threshold) {
				return HIGH;
			}
			if (healthRatio >= MEDIUM.threshold) {
				return MEDIUM;
			}
			if (healthRatio >= LOW.threshold) {
				return LOW;
			}
			return NONE;
		}
	}
}
