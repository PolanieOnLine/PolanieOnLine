package games.stendhal.server.entity.item;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import games.stendhal.server.maps.challengearena.ChallengeArenaTier;

public class ChallengeArenaRewardChestTest {
	@Test
	public void rewardDataRoundTripsTierAndPlayerLevel() {
		final String value = ChallengeArenaRewardChest.createRewardData(
				ChallengeArenaTier.LEGEND, 350);
		final ChallengeArenaRewardChest.RewardData parsed =
				ChallengeArenaRewardChest.parseRewardData(value);

		assertEquals(ChallengeArenaTier.LEGEND, parsed.getTier());
		assertEquals(350, parsed.getPlayerLevel());
	}

	@Test
	public void rewardDataRejectsTierWithoutEquipmentChest() {
		assertNull(ChallengeArenaRewardChest.parseRewardData("TRIAL;350"));
		assertNull(ChallengeArenaRewardChest.parseRewardData("SKIRMISH;350"));
	}

	@Test
	public void rewardDataRejectsMalformedValues() {
		assertNull(ChallengeArenaRewardChest.parseRewardData(null));
		assertNull(ChallengeArenaRewardChest.parseRewardData(""));
		assertNull(ChallengeArenaRewardChest.parseRewardData("LEGEND"));
		assertNull(ChallengeArenaRewardChest.parseRewardData("UNKNOWN;350"));
		assertNull(ChallengeArenaRewardChest.parseRewardData("LEGEND;0"));
		assertNull(ChallengeArenaRewardChest.parseRewardData("LEGEND;bad"));
	}
}
