package games.stendhal.server.entity.player;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ProgressionConfigTest {

	@Test
	public void shouldBuildMonotonicMasteryTableUpToConfiguredMaxLevel() {
		long previous = 0L;
		for (int level = 1; level <= ProgressionConfig.MASTERY_MAX_LEVEL; level++) {
			final long current = ProgressionConfig.getMasteryXPForLevel(level);
			assertTrue("Mastery XP table must be monotonic", current >= previous);
			previous = current;
		}
		assertThat(ProgressionConfig.getMasteryXPForLevel(ProgressionConfig.MASTERY_MAX_LEVEL),
			is(ProgressionConfig.getMasteryMaxXP()));
	}

	@Test
	public void shouldConvertXpBackToExpectedMasteryLevel() {
		for (int level = 0; level < ProgressionConfig.MASTERY_MAX_LEVEL; level++) {
			final long startXP = ProgressionConfig.getMasteryXPForLevel(level);
			final long nextXP = ProgressionConfig.getMasteryXPForLevel(level + 1);
			assertThat(ProgressionConfig.getMasteryLevelForXP(startXP), is(level));
			if (nextXP > startXP + 1L) {
				assertThat(ProgressionConfig.getMasteryLevelForXP(nextXP - 1L), is(level));
			}
		}
		assertThat(ProgressionConfig.getMasteryLevelForXP(ProgressionConfig.getMasteryMaxXP()),
			is(ProgressionConfig.MASTERY_MAX_LEVEL));
	}

	@Test
	public void shouldKeepIntendedPacingAcrossEarlyMidAndLateGame() {
		final int earlyEnd = ProgressionConfig.MASTERY_EARLY_END_LEVEL;
		final int midEnd = ProgressionConfig.MASTERY_MID_END_LEVEL;
		final int maxLevel = ProgressionConfig.MASTERY_MAX_LEVEL;

		final double earlyAvg = averageDelta(1, earlyEnd);
		final double midAvg = averageDelta(earlyEnd + 1, midEnd);
		final double lateAvg = averageDelta(midEnd + 1, maxLevel);

		assertTrue("Early should be faster than mid", earlyAvg < midAvg);
		assertTrue("Late should be slower than mid", lateAvg > midAvg);
	}

	private static double averageDelta(final int fromLevel, final int toLevel) {
		if (toLevel < fromLevel) {
			return 0.0d;
		}
		long sum = 0L;
		for (int level = fromLevel; level <= toLevel; level++) {
			sum += ProgressionConfig.getMasteryXPForLevel(level)
				- ProgressionConfig.getMasteryXPForLevel(level - 1);
		}
		return sum / (double) (toLevel - fromLevel + 1);
	}
}
