package games.stendhal.client;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class FramePacerTest {
	@Test
	public void everySupportedFpsBlocksForItsRemainingFrameTime() {
		for (int fps : new int[] { 30, 60, 90, 120, 144, 165, 240 }) {
			long duration = Math.round(1_000_000_000.0 / fps);
			long[] clock = { 1000 };
			List<Long> waits = new ArrayList<>();
			long adjustment = FramePacer.await(duration, () -> clock[0],
					wait -> {
						waits.add(wait);
						clock[0] += wait;
					});
			assertEquals(Arrays.asList(duration), waits);
			assertEquals(0, adjustment);
		}
	}

	@Test
	public void earlyWakeupsBlockAgainOnlyForTheUnfinishedPart() {
		long[] clock = { 0 };
		List<Long> waits = new ArrayList<>();
		assertEquals(0, FramePacer.await(4000, () -> clock[0], wait -> {
			waits.add(wait);
			clock[0] += waits.size() == 1 ? 1000 : wait;
		}));
		assertEquals(Arrays.asList(4000L, 3000L), waits);
	}

	@Test
	public void oversleepIsReturnedForTheExistingNextFrameCorrection() {
		long[] clock = { 0 };
		assertEquals(-1250, FramePacer.await(4000, () -> clock[0],
				wait -> clock[0] += wait + 1250));
	}

	@Test
	public void interruptionReturnsWithoutSpinningOrClearingTheFlag() {
		Thread.currentThread().interrupt();
		try {
			FramePacer.await(4000, () -> 0, wait -> fail(
					"Interrupted thread must not park repeatedly"));
			assertTrue(Thread.currentThread().isInterrupted());
		} finally {
			Thread.interrupted();
		}
	}

	@Test
	public void monotonicClockWrapStillMeasuresElapsedTime() {
		long[] clock = { Long.MAX_VALUE - 50 };
		assertEquals(0, FramePacer.await(100, () -> clock[0],
				wait -> clock[0] += wait));
	}
}
