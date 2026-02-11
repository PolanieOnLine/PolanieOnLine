/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************
 ***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.entity.npc.SpeakerNPC;

public final class KoscieliskoGiantEscortEvent extends ConfiguredMapEvent {
	private static final Logger LOGGER = Logger.getLogger(KoscieliskoGiantEscortEvent.class);
	private static final String GIANT_NPC_NAME = "Wielkolud";
	private static final int GIANT_EVENT_HP = 50000;
	private static final int GIANT_FAIL_HP_THRESHOLD = 100;
	private static final int HEALTH_CHECK_INTERVAL_SECONDS = 1;

	private final TurnListener giantHealthMonitor = new TurnListener() {
		@Override
		public void onTurnReached(final int currentTurn) {
			monitorGiantHealth();
		}
	};

	private volatile SpeakerNPC giantNpc;
	private volatile GiantSnapshot snapshot;
	private volatile boolean failedByGiantHealth;
	private volatile boolean escortSuccess;

	public KoscieliskoGiantEscortEvent() {
		super(LOGGER, MapEventConfigLoader.load(MapEventConfigLoader.KOSCIELISKO_GIANT_ESCORT));
	}

	@Override
	protected void onStart() {
		failedByGiantHealth = false;
		escortSuccess = false;
		snapshot = null;
		giantNpc = SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		super.onStart();

		if (giantNpc == null) {
			LOGGER.warn(getEventName() + " cannot start objective tracking: NPC '" + GIANT_NPC_NAME + "' not found.");
			failedByGiantHealth = true;
			endEvent();
			return;
		}

		snapshot = GiantSnapshot.capture(giantNpc);
		giantNpc.setBaseHP(GIANT_EVENT_HP);
		giantNpc.setHP(GIANT_EVENT_HP);
		scheduleHealthMonitor();
	}

	@Override
	protected void onStop() {
		SingletonRepository.getTurnNotifier().dontNotify(giantHealthMonitor);

		final SpeakerNPC currentGiant = giantNpc;
		escortSuccess = !failedByGiantHealth
				&& currentGiant != null
				&& currentGiant.getHP() > GIANT_FAIL_HP_THRESHOLD;

		if (currentGiant != null && snapshot != null) {
			snapshot.restore(currentGiant);
		}
		snapshot = null;
		giantNpc = null;

		super.onStop();
	}

	@Override
	protected String getStopAnnouncementMessage() {
		if (escortSuccess) {
			return "Eskorta zakończona sukcesem! Wielkolud przetrwał napór wrogów.";
		}
		return "Eskorta zakończona porażką - Wielkolud został ciężko ranny.";
	}

	private void monitorGiantHealth() {
		if (!isEventActive()) {
			return;
		}

		final SpeakerNPC currentGiant = giantNpc != null ? giantNpc : SingletonRepository.getNPCList().get(GIANT_NPC_NAME);
		giantNpc = currentGiant;
		if (currentGiant == null || currentGiant.getHP() <= GIANT_FAIL_HP_THRESHOLD) {
			failedByGiantHealth = true;
			endEvent();
			return;
		}

		scheduleHealthMonitor();
	}

	private void scheduleHealthMonitor() {
		SingletonRepository.getTurnNotifier().notifyInSeconds(HEALTH_CHECK_INTERVAL_SECONDS, giantHealthMonitor);
	}

	private static final class GiantSnapshot {
		private final int hp;
		private final int baseHp;
		private final int atk;
		private final int ratk;
		private final int def;

		private GiantSnapshot(final int hp, final int baseHp, final int atk, final int ratk, final int def) {
			this.hp = hp;
			this.baseHp = baseHp;
			this.atk = atk;
			this.ratk = ratk;
			this.def = def;
		}

		private static GiantSnapshot capture(final SpeakerNPC giant) {
			return new GiantSnapshot(giant.getHP(), giant.getBaseHP(), giant.getAtk(), giant.getRatk(), giant.getDef());
		}

		private void restore(final SpeakerNPC giant) {
			giant.setAtk(atk);
			giant.setRatk(ratk);
			giant.setDef(def);
			giant.setBaseHP(baseHp);
			giant.setHP(Math.min(hp, baseHp));
		}
	}
}
