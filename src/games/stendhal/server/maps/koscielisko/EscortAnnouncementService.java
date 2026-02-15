package games.stendhal.server.maps.koscielisko;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import games.stendhal.common.NotificationType;
import games.stendhal.server.core.engine.SingletonRepository;

final class EscortAnnouncementService {
	private static final int HP_STATUS_DELTA_PERCENT_THRESHOLD = 5;
	private static final int[] WAVE_PROGRESS_MILESTONES = { 25, 50, 75, 90 };

	private final Logger logger;
	private final String eventName;
	private final BroadcastRateLimiter operationalLimiter;
	private final BroadcastRateLimiter criticalLimiter;
	private int lastAnnouncedHpPercent;

	EscortAnnouncementService(final Logger logger,
			final String eventName,
			final long waveStatusMinIntervalMillis,
			final long criticalStatusMinIntervalMillis) {
		this.logger = logger;
		this.eventName = eventName;
		this.operationalLimiter = new BroadcastRateLimiter(waveStatusMinIntervalMillis);
		this.criticalLimiter = new BroadcastRateLimiter(criticalStatusMinIntervalMillis);
		this.lastAnnouncedHpPercent = -1;
	}

	void reset() {
		lastAnnouncedHpPercent = -1;
		operationalLimiter.clear();
		criticalLimiter.clear();
	}

	Integer findWaveMilestone(final int progressPercent) {
		for (final int milestone : WAVE_PROGRESS_MILESTONES) {
			if (progressPercent >= milestone) {
				return milestone;
			}
		}
		return null;
	}

	boolean canAnnounceWaveMilestone(final int milestone, final long nowMillis) {
		return operationalLimiter.tryAcquire("FALA_" + milestone, nowMillis);
	}

	void announceProgressStatus(final String stage, final int progressPercent, final int hpPercent) {
		final boolean includeHp = shouldAttachHp(stage, hpPercent);
		final String hpSuffix = includeHp ? " (siły: " + hpPercent + "%)." : ".";
		final String message;
		switch (stage) {
			case "START":
				message = "[Kościelisko] Eskorta rusza. Utrzymajcie Wielkoluda przy życiu" + hpSuffix;
				break;
			case "50%":
				message = "[Kościelisko] Połowa trasy za nimi. Trzymajcie front" + hpSuffix;
				break;
			case "FALA_25":
				message = "[Kościelisko] Pierwsza linia pęka. Odepchnijcie zejście" + hpSuffix;
				break;
			case "FALA_50":
				message = "[Kościelisko] Presja rośnie. Trzymajcie środek szlaku" + hpSuffix;
				break;
			case "FALA_75":
				message = "[Kościelisko] Szturm się zagęszcza. Osłońcie Wielkoluda" + hpSuffix;
				break;
			case "FALA_90":
				message = "[Kościelisko] Ostatni napór! Utrzymajcie marsz do końca" + hpSuffix;
				break;
			default:
				message = "[Kościelisko] Marsz trwa (" + progressPercent + "%)" + hpSuffix;
				break;
		}
		SingletonRepository.getRuleProcessor().tellAllPlayers(NotificationType.PRIVMSG, message);
		if (logger.isDebugEnabled()) {
			logger.debug(eventName + " operational broadcast sent: stage=" + stage + ", progress="
					+ progressPercent + "%, hp=" + hpPercent + "%, includeHp=" + includeHp + ".");
		}
	}

	boolean tryAnnounceCriticalHp(final int hpPercent, final long nowMillis) {
		if (!criticalLimiter.tryAcquire("GIANT_CRITICAL_HP", nowMillis)) {
			return false;
		}
		SingletonRepository.getRuleProcessor().tellAllPlayers(
				NotificationType.PRIVMSG,
				"[Kościelisko] Wielkolud słabnie! Zostało mu " + hpPercent + "% sił.");
		return true;
	}

	private boolean shouldAttachHp(final String stage, final int hpPercent) {
		if ("START".equals(stage)) {
			lastAnnouncedHpPercent = hpPercent;
			return true;
		}
		if (lastAnnouncedHpPercent < 0) {
			lastAnnouncedHpPercent = hpPercent;
			return true;
		}
		if ((lastAnnouncedHpPercent - hpPercent) >= HP_STATUS_DELTA_PERCENT_THRESHOLD) {
			lastAnnouncedHpPercent = hpPercent;
			return true;
		}
		return false;
	}

	static final class BroadcastRateLimiter {
		private final long minIntervalMillis;
		private final Map<String, Long> lastBroadcastAtMillis = new HashMap<>();

		BroadcastRateLimiter(final long minIntervalMillis) {
			this.minIntervalMillis = Math.max(0L, minIntervalMillis);
		}

		synchronized boolean tryAcquire(final String key, final long nowMillis) {
			final Long previous = lastBroadcastAtMillis.get(key);
			if (previous != null && (nowMillis - previous) < minIntervalMillis) {
				return false;
			}
			lastBroadcastAtMillis.put(key, nowMillis);
			return true;
		}

		synchronized void clear() {
			lastBroadcastAtMillis.clear();
		}
	}
}
