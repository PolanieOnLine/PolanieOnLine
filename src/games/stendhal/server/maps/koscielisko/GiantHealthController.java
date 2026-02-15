package games.stendhal.server.maps.koscielisko;

import java.time.Duration;

import games.stendhal.server.entity.npc.SpeakerNPC;

final class GiantHealthController {
	private final int failHpThreshold;
	private final int criticalHpThreshold;
	private final int healCapPerTick;
	private boolean criticalHealthAnnounced;
	private boolean halfProgressAnnounced;
	private int giantHpBeforeTick;
	private long eventStartedAtMillis;
	private int giantEventHp;

	GiantHealthController(final int failHpThreshold, final int criticalHpThreshold, final int healCapPerTick) {
		this.failHpThreshold = failHpThreshold;
		this.criticalHpThreshold = criticalHpThreshold;
		this.healCapPerTick = healCapPerTick;
	}

	void reset(final int eventHp, final long startedAtMillis) {
		criticalHealthAnnounced = false;
		halfProgressAnnounced = false;
		giantEventHp = Math.max(1, eventHp);
		giantHpBeforeTick = giantEventHp;
		eventStartedAtMillis = startedAtMillis;
	}

	boolean shouldFail(final SpeakerNPC giant) {
		return giant == null || giant.getHP() <= failHpThreshold;
	}

	void limitHealingPerTick(final SpeakerNPC giant) {
		final int currentHp = giant.getHP();
		if (currentHp > giantHpBeforeTick + healCapPerTick) {
			giant.setHP(giantHpBeforeTick + healCapPerTick);
		}
		giantHpBeforeTick = giant.getHP();
	}

	void announceMilestones(final SpeakerNPC giant,
			final Duration eventDuration,
			final long nowMillis,
			final EscortAnnouncementService announcementService) {
		if (!halfProgressAnnounced && eventStartedAtMillis > 0L) {
			final long elapsedMillis = nowMillis - eventStartedAtMillis;
			if (elapsedMillis >= eventDuration.toMillis() / 2) {
				halfProgressAnnounced = true;
				announcementService.announceProgressStatus("50%", 50, hpPercent(giant));
			}
		}
		if (!criticalHealthAnnounced && giant.getHP() <= criticalHpThreshold) {
			if (announcementService.tryAnnounceCriticalHp(hpPercent(giant), nowMillis)) {
				criticalHealthAnnounced = true;
			}
		}
	}

	int hpPercent(final SpeakerNPC giant) {
		return Math.max(0, Math.round((giant.getHP() * 100.0f) / giantEventHp));
	}
}
