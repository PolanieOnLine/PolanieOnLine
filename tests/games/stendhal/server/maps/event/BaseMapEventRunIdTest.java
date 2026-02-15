/***************************************************************************
 *                    Copyright © 2026 - PolanieOnLine                    *
 ***************************************************************************/
/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
package games.stendhal.server.maps.event;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.maps.MockStendlRPWorld;

public class BaseMapEventRunIdTest {
	@BeforeClass
	public static void setUpClass() {
		MockStendlRPWorld.get();
	}

	@Before
	public void setUp() {
		SingletonRepository.getTurnNotifier().getEventListForDebugging().clear();
	}

	@Test
	public void shouldIgnoreStaleWaveCallbacksAfterRestart() {
		final TestMapEvent event = new TestMapEvent(createConfig());
		final TurnNotifier turnNotifier = SingletonRepository.getTurnNotifier();
		final int startTurn = turnNotifier.getCurrentTurnForDebugging();

		assertThat(event.start(), is(true));
		event.stop();
		assertThat(event.start(), is(true));

		final int firstWaveTurn = startTurn + SingletonRepository.getRPWorld().getTurnsInSeconds(2) + 1;

		advanceTurns(startTurn + 1, firstWaveTurn - 1);
		assertThat(event.getSpawnedCreatures(), is(Collections.<String>emptyList()));

		advanceTurns(firstWaveTurn, firstWaveTurn);
		assertThat(event.getSpawnedCreatures().size(), is(1));
		assertThat(event.getSpawnedCreatures().get(0), is("first_wave"));
	}

	private static MapEventConfig createConfig() {
		return MapEventConfig.builder("RunId regression")
				.duration(Duration.ofSeconds(20))
				.waves(MapEventConfigSupport.waves(
						MapEventConfigSupport.wave(2, MapEventConfigSupport.spawn("first_wave", 1)),
						MapEventConfigSupport.wave(2, MapEventConfigSupport.spawn("second_wave", 1))))
				.build();
	}

	private static void advanceTurns(final int fromTurnInclusive, final int toTurnInclusive) {
		if (toTurnInclusive < fromTurnInclusive) {
			return;
		}
		for (int turn = fromTurnInclusive; turn <= toTurnInclusive; turn++) {
			SingletonRepository.getRPWorld().nextTurn();
			SingletonRepository.getTurnNotifier().logic(turn);
		}
	}

	private static class TestMapEvent extends BaseMapEvent {
		private final List<String> spawnedCreatures = new ArrayList<>();

		private TestMapEvent(final MapEventConfig config) {
			super(Logger.getLogger(BaseMapEventRunIdTest.class), config);
		}

		private boolean start() {
			return startEvent();
		}

		private void stop() {
			endEvent();
		}

		private List<String> getSpawnedCreatures() {
			return spawnedCreatures;
		}

		@Override
		protected void onStart() {
			// no-op
		}

		@Override
		protected void onStop() {
			// no-op
		}

		@Override
		protected void spawnCreatures(final String creatureName, final int count) {
			for (int i = 0; i < count; i++) {
				spawnedCreatures.add(creatureName);
			}
		}
	}
}
