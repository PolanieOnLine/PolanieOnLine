package games.stendhal.server.entity;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Tracks chest opening value distribution for live balancing follow-up.
 */
public final class ChestOpenTelemetry {
	private static final Logger logger = Logger.getLogger(ChestOpenTelemetry.class);
	private static final int LOG_EVERY_OPENS = 200;

	private static final Map<String, RunningStats> stats = new HashMap<String, RunningStats>();

	private ChestOpenTelemetry() {
	}

	public static synchronized void record(final String chestId, final double rolledValue) {
		RunningStats chestStats = stats.get(chestId);
		if (chestStats == null) {
			chestStats = new RunningStats();
			stats.put(chestId, chestStats);
		}

		chestStats.add(rolledValue);
		if (chestStats.getCount() % LOG_EVERY_OPENS == 0) {
			logger.info("Chest telemetry: " + chestId + " opens=" + chestStats.getCount()
					+ " avg_value=" + Math.round(chestStats.getMean())
					+ " stddev=" + Math.round(chestStats.getStdDev()));
		}
	}

	private static final class RunningStats {
		private long count;
		private double mean;
		private double m2;

		void add(final double value) {
			count++;
			final double delta = value - mean;
			mean += delta / count;
			final double delta2 = value - mean;
			m2 += delta * delta2;
		}

		long getCount() {
			return count;
		}

		double getMean() {
			return mean;
		}

		double getStdDev() {
			if (count < 2) {
				return 0;
			}
			return Math.sqrt(m2 / (count - 1));
		}
	}
}
