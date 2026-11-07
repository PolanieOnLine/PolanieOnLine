/* Copyright (C) 2026 - PolanieOnLine; GPL version 2 or later. */
package games.stendhal.client;

import java.util.concurrent.locks.LockSupport;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/** Blocks between frames, retaining the caller's oversleep compensation. */
final class FramePacer {
	private static final LongSupplier CLOCK = System::nanoTime;
	private static final LongConsumer PARK = LockSupport::parkNanos;

	private FramePacer() {
	}

	static long await(long nanos) {
		return await(nanos, CLOCK, PARK);
	}

	static long await(long nanos, LongSupplier clock, LongConsumer park) {
		long start = clock.getAsLong();
		long remaining = nanos;
		while (remaining > 0L && !Thread.currentThread().isInterrupted()) {
			park.accept(remaining);
			// Recheck the deadline after early/spurious wakeups. No active
			// wait,
			// including at 144/240 FPS or on Java 8 without Thread.onSpinWait.
			remaining = nanos - (clock.getAsLong() - start);
		}
		return remaining;
	}
}
