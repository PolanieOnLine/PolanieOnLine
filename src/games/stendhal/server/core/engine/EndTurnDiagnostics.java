/***************************************************************************
 *                    (C) Copyright 2003-2026 - Marauroa                   *
 ***************************************************************************/
package games.stendhal.server.core.engine;

/**
 * Low-allocation diagnostics for the Stendhal-specific work executed inside
 * {@link StendhalRPRuleProcessor#endTurn()}.
 */
public final class EndTurnDiagnostics {
	private static final int TOP_COUNT = 3;

	private final String[] topListenerNames = new String[TOP_COUNT];
	private final long[] topListenerNanos = new long[TOP_COUNT];
	private final String[] topZoneNames = new String[TOP_COUNT];
	private final long[] topZoneNanos = new long[TOP_COUNT];

	private int listenerCount;
	private int listenerFailures;
	private long listenerTotalNanos;
	private long turnNotifierNanos;
	private int zoneCount;
	private long zoneTotalNanos;
	private long elapsedNanos;

	/** Reset counters for a new endTurn execution. */
	public void reset() {
		listenerCount = 0;
		listenerFailures = 0;
		listenerTotalNanos = 0L;
		turnNotifierNanos = 0L;
		zoneCount = 0;
		zoneTotalNanos = 0L;
		elapsedNanos = 0L;
		clearTop(topListenerNames, topListenerNanos);
		clearTop(topZoneNames, topZoneNanos);
	}

	/** Record one TurnNotifier callback. */
	public void recordTurnListener(final String name, final long durationNanos,
			final boolean failed) {
		listenerCount++;
		if (failed) {
			listenerFailures++;
		}
		listenerTotalNanos += durationNanos;
		recordTop(topListenerNames, topListenerNanos, name, durationNanos);
	}

	/** Record total TurnNotifier.logic duration. */
	public void recordTurnNotifierDuration(final long durationNanos) {
		turnNotifierNanos = durationNanos;
	}

	/** Record one StendhalRPZone.logic duration. */
	public void recordZone(final String zoneName, final long durationNanos) {
		zoneCount++;
		zoneTotalNanos += durationNanos;
		recordTop(topZoneNames, topZoneNanos, zoneName, durationNanos);
	}

	/** Finish the current endTurn measurement. */
	public void finish(final long durationNanos) {
		elapsedNanos = durationNanos;
	}

	/** Format diagnostics only on the slow path. */
	public String format(final int turn) {
		final StringBuilder out = new StringBuilder(256);
		out.append("Slow Stendhal endTurn [turn=").append(turn);
		out.append(", elapsedMs=").append(toMillis(elapsedNanos));
		out.append(", turnNotifierMs=").append(toMillis(turnNotifierNanos));
		out.append(", listeners={count=").append(listenerCount);
		out.append(",failures=").append(listenerFailures);
		out.append(",totalMs=").append(toMillis(listenerTotalNanos));
		out.append(",top=");
		appendTop(out, topListenerNames, topListenerNanos);
		out.append("}, zones={count=").append(zoneCount);
		out.append(",totalMs=").append(toMillis(zoneTotalNanos));
		out.append(",top=");
		appendTop(out, topZoneNames, topZoneNanos);
		out.append("}]");
		return out.toString();
	}

	private static void clearTop(final String[] names, final long[] durations) {
		for (int i = 0; i < TOP_COUNT; i++) {
			names[i] = null;
			durations[i] = 0L;
		}
	}

	private static void recordTop(final String[] names, final long[] durations,
			final String name, final long durationNanos) {
		for (int i = 0; i < TOP_COUNT; i++) {
			if (durationNanos > durations[i]) {
				for (int j = TOP_COUNT - 1; j > i; j--) {
					durations[j] = durations[j - 1];
					names[j] = names[j - 1];
				}
				durations[i] = durationNanos;
				names[i] = name;
				return;
			}
		}
	}

	private static void appendTop(final StringBuilder out, final String[] names,
			final long[] durations) {
		out.append('[');
		boolean first = true;
		for (int i = 0; i < TOP_COUNT; i++) {
			if (names[i] == null) {
				continue;
			}
			if (!first) {
				out.append(',');
			}
			out.append(names[i]).append(':').append(toMicros(durations[i])).append("us");
			first = false;
		}
		out.append(']');
	}

	private static long toMillis(final long nanos) {
		return nanos / 1000000L;
	}

	private static long toMicros(final long nanos) {
		return nanos / 1000L;
	}
}
