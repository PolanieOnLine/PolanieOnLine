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
			MapEventConfigLoader.createConfigs(Arrays.asList(firstProvider, secondProvider));
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

		final Map<String, MapEventConfig> mergedConfigs = MapEventConfigLoader.createConfigs(Arrays.asList(
				dragonProvider,
				koscieliskoProvider,
				kikareukinProvider));

		assertThat(mergedConfigs.keySet().size(), is(3));
		assertThat(mergedConfigs.containsKey("dragon_unique"), is(true));
		assertThat(mergedConfigs.containsKey("koscielisko_unique"), is(true));
		assertThat(mergedConfigs.containsKey("kikareukin_unique"), is(true));
	}

	private static MapEventConfig minimalConfig(final String name) {
		return MapEventConfig.builder(name).build();
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
}
