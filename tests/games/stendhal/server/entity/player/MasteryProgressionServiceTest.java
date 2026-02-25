package games.stendhal.server.entity.player;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import games.stendhal.server.entity.player.MasteryProgressionService.MasteryProgressionResult;
import games.stendhal.server.entity.player.Player;
import utilities.PlayerTestHelper;

public class MasteryProgressionServiceTest {

	private final MasteryProgressionService service = new MasteryProgressionService();

	@Test
	public void shouldHandleLargeExpPackagesWithMultipleMasteryLevelUps() {
		final Player player = PlayerTestHelper.createPlayer("mastery-multi-level");
		final long expToReachLevel105 = ProgressionConfig.getMasteryXPForLevel(105);

		final MasteryProgressionResult result = service.addMasteryExp(player, expToReachLevel105);

		assertThat(player.getInt("mastery_level"), is(105));
		assertThat(result.getLevelUps().size(), is(105));
		assertThat(result.getReachedMilestones().size(), is(1));
		assertThat(result.getReachedMilestones().get(0), is(100));
	}

	@Test
	public void shouldStopAtMasteryCap2000AndMarkAsCapped() {
		final Player player = PlayerTestHelper.createPlayer("mastery-hard-cap");
		player.put("mastery_level", ProgressionConfig.MASTERY_MAX_LEVEL - 1);
		player.put("mastery_exp", ProgressionConfig.getMasteryXPForLevel(ProgressionConfig.MASTERY_MAX_LEVEL - 1));
		player.put("mastery_xp", ProgressionConfig.getMasteryXPForLevel(ProgressionConfig.MASTERY_MAX_LEVEL - 1));

		final MasteryProgressionResult result = service.addMasteryExp(player, Long.MAX_VALUE);

		assertThat(player.getInt("mastery_level"), is(ProgressionConfig.MASTERY_MAX_LEVEL));
		assertThat(player.getLong("mastery_exp"), is(ProgressionConfig.getMasteryMaxXP()));
		assertThat(result.isCappedAfterUpdate(), is(true));
		assertThat(result.getReachedMilestones().contains(ProgressionConfig.MASTERY_MAX_LEVEL), is(true));
	}

	@Test
	public void shouldReportAlreadyCappedWhenNoFurtherGainIsPossible() {
		final Player player = PlayerTestHelper.createPlayer("mastery-already-capped");
		player.put("mastery_level", ProgressionConfig.MASTERY_MAX_LEVEL);
		player.put("mastery_exp", ProgressionConfig.getMasteryMaxXP());
		player.put("mastery_xp", ProgressionConfig.getMasteryMaxXP());

		final MasteryProgressionResult result = service.addMasteryExp(player, 10_000L);

		assertThat(result.isAlreadyCapped(), is(true));
		assertThat(result.getAppliedMasteryExpGain(), is(0L));
	}
}
