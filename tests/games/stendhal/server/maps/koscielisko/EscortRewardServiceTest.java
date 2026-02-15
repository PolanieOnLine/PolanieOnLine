package games.stendhal.server.maps.koscielisko;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class EscortRewardServiceTest {
	@Test
	public void shouldResolveRewardTierFromHealthRatio() {
		assertThat(EscortRewardService.RewardTier.fromHealthRatio(0.85d), is(EscortRewardService.RewardTier.HIGH));
		assertThat(EscortRewardService.RewardTier.fromHealthRatio(0.60d), is(EscortRewardService.RewardTier.MEDIUM));
		assertThat(EscortRewardService.RewardTier.fromHealthRatio(0.30d), is(EscortRewardService.RewardTier.LOW));
		assertThat(EscortRewardService.RewardTier.fromHealthRatio(0.10d), is(EscortRewardService.RewardTier.NONE));
	}
}
