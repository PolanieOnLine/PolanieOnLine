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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class MapEventConfigLoaderTest {
	@Test
	public void shouldFailWhenEventIdConflictsAcrossProviders() {
		final MapEventConfigProvider firstProvider = new StubProvider("dragon_conflict");
		final MapEventConfigProvider secondProvider = new StubProvider("dragon_conflict");

		try {
			MapEventConfigLoader.createConfigs(
					Arrays.asList(firstProvider, secondProvider),
					MapEventConfigLoader.ValidationMode.STRICT,
					new StubValidationContext(true, true));
			fail("Expected IllegalStateException for duplicate eventId.");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), containsString("dragon_conflict"));
			assertThat(e.getMessage(), containsString("StubProvider"));
		}
	}

	@Test
	public void shouldMergeConfigsFromMultipleProvidersWhenIdsAreUnique() {
		final MapEventConfigProvider dragonProvider = new NamedStubProvider(Collections.singletonMap(
				"dragon_unique", minimalConfig("Dragon Event")));
		final MapEventConfigProvider koscieliskoProvider = new NamedStubProvider(Collections.singletonMap(
				"koscielisko_unique", minimalConfig("Koscielisko Event")));
		final MapEventConfigProvider kikareukinProvider = new NamedStubProvider(Collections.singletonMap(
				"kikareukin_unique", minimalConfig("Kikareukin Event")));

		final Map<String, MapEventConfig> mergedConfigs = MapEventConfigLoader.createConfigs(
				Arrays.asList(
						dragonProvider,
						koscieliskoProvider,
						kikareukinProvider),
				MapEventConfigLoader.ValidationMode.STRICT,
				new StubValidationContext(true, true));

		assertThat(mergedConfigs.keySet().size(), is(3));
		assertThat(mergedConfigs.containsKey("dragon_unique"), is(true));
		assertThat(mergedConfigs.containsKey("koscielisko_unique"), is(true));
		assertThat(mergedConfigs.containsKey("kikareukin_unique"), is(true));
	}

	private static MapEventConfig minimalConfig(final String name) {
		return MapEventConfig.builder(name)
				.zones(Collections.singletonList("known_zone"))
				.waves(Collections.singletonList(wave("known_creature")))
				.build();
	}

	@Test
	public void shouldFailInStrictModeWhenZoneDoesNotExist() {
		final MapEventConfig config = MapEventConfig.builder("Invalid Zone Event")
				.zones(Collections.singletonList("missing_zone"))
				.waves(Collections.singletonList(wave("szczur")))
				.build();

		try {
			MapEventConfigLoader.createConfigs(
					Collections.singletonList(new NamedStubProvider(Collections.singletonMap("dragon_invalid_zone", config))),
					MapEventConfigLoader.ValidationMode.STRICT,
					new StubValidationContext(false, true));
			fail("Expected IllegalStateException for missing zone in strict mode.");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), containsString("dragon_invalid_zone"));
			assertThat(e.getMessage(), containsString("missing zone"));
		}
	}

	@Test
	public void shouldDisableEventInPermissiveModeWhenCreatureTemplateDoesNotExist() {
		final MapEventConfig invalidConfig = MapEventConfig.builder("Invalid Creature Event")
				.zones(Collections.singletonList("known_zone"))
				.waves(Collections.singletonList(wave("missing_creature")))
				.build();

		final Map<String, MapEventConfig> configs = MapEventConfigLoader.createConfigs(
				Collections.singletonList(new NamedStubProvider(Collections.singletonMap("dragon_invalid_creature", invalidConfig))),
				MapEventConfigLoader.ValidationMode.PERMISSIVE,
				new StubValidationContext(true, false));

		assertFalse(configs.containsKey("dragon_invalid_creature"));
	}

	@Test
	public void shouldTreatEmptyWavesAsValidationError() {
		final MapEventConfig emptyWavesConfig = MapEventConfig.builder("Empty Waves Event")
				.zones(Collections.singletonList("known_zone"))
				.build();

		try {
			MapEventConfigLoader.createConfigs(
					Collections.singletonList(new NamedStubProvider(Collections.singletonMap("kikareukin_empty_waves", emptyWavesConfig))),
					MapEventConfigLoader.ValidationMode.STRICT,
					new StubValidationContext(true, true));
			fail("Expected IllegalStateException for empty waves.");
		} catch (IllegalStateException e) {
			assertThat(e.getMessage(), containsString("kikareukin_empty_waves"));
			assertThat(e.getMessage(), containsString("no waves defined"));
		}
	}

	private static BaseMapEvent.EventWave wave(final String creatureName) {
		return new BaseMapEvent.EventWave(
				10,
				Collections.singletonList(new BaseMapEvent.EventSpawn(creatureName, 1)));
	}

	private static class StubProvider implements MapEventConfigProvider {
		private final String eventId;

		private StubProvider(final String eventId) {
			this.eventId = eventId;
		}

		@Override
		public Map<String, MapEventConfig> loadConfigs() {
			final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
			configs.put(eventId, minimalConfig(eventId));
			return configs;
		}
	}

	private static class NamedStubProvider implements MapEventConfigProvider {
		private final Map<String, MapEventConfig> configs;

		private NamedStubProvider(final Map<String, MapEventConfig> configs) {
			this.configs = configs;
		}

		@Override
		public Map<String, MapEventConfig> loadConfigs() {
			return configs;
		}
	}

	private static class StubValidationContext implements MapEventConfigLoader.ValidationContext {
		private final boolean zoneExists;
		private final boolean creatureTemplateExists;

		private StubValidationContext(final boolean zoneExists, final boolean creatureTemplateExists) {
			this.zoneExists = zoneExists;
			this.creatureTemplateExists = creatureTemplateExists;
		}

		@Override
		public boolean zoneExists(final String zoneName) {
			return zoneExists;
		}

		@Override
		public boolean creatureTemplateExists(final String creatureName) {
			return creatureTemplateExists;
		}
	}
}
