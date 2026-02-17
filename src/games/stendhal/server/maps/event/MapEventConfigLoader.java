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

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.maps.dragon.DragonMapEventConfigProvider;
import games.stendhal.server.maps.kikareukin.KikareukinMapEventConfigProvider;
import games.stendhal.server.maps.koscielisko.KoscieliskoMapEventConfigProvider;
import games.stendhal.server.maps.tatry.kuznice.TatryMapEventConfigProvider;

public final class MapEventConfigLoader {
	private static final Logger LOGGER = Logger.getLogger(MapEventConfigLoader.class);

	/**
	 * Naming convention for globally unique event IDs:
	 * <ul>
	 * <li>{@code dragon_*} for Dragon Land events</li>
	 * <li>{@code koscielisko_*} for Kościelisko events</li>
	 * <li>{@code kikareukin_*} for Kikareukin events</li>
	 * <li>{@code tatry_*} for Tatry events</li>
	 * </ul>
	 *
	 * Prefixing IDs by domain helps avoid collisions between independent providers.
	 */
	public static final String DRAGON_LAND_DEFAULT = DragonMapEventConfigProvider.DRAGON_LAND_DEFAULT;
	public static final String KIKAREUKIN_ANGEL_PREVIEW = KikareukinMapEventConfigProvider.KIKAREUKIN_ANGEL_PREVIEW;
	public static final String KOSCIELISKO_GIANT_ESCORT = KoscieliskoMapEventConfigProvider.KOSCIELISKO_GIANT_ESCORT;
	public static final String TATRY_KUZNICE_BANDIT_RAID = TatryMapEventConfigProvider.TATRY_KUZNICE_BANDIT_RAID;

	private static final Map<String, MapEventConfig> CONFIGS = createConfigs();

	private MapEventConfigLoader() {
		// utility class
	}

	public static MapEventConfig load(final String configId) {
		if (configId == null) {
			throw new IllegalArgumentException("Map event config id cannot be null.");
		}
		final MapEventConfig config = CONFIGS.get(configId);
		if (config == null) {
			throw new IllegalArgumentException("Unknown map event config: " + configId
					+ ". Available configIds: " + availableConfigIds() + ".");
		}
		return config;
	}

	public static boolean hasConfigId(final String configId) {
		return configId != null && CONFIGS.containsKey(configId);
	}

	public static Set<String> availableConfigIds() {
		return Collections.unmodifiableSet(CONFIGS.keySet());
	}

	private static Map<String, MapEventConfig> createConfigs() {
		return createConfigs(Arrays.asList(
				new DragonMapEventConfigProvider(),
				new KoscieliskoMapEventConfigProvider(),
				new KikareukinMapEventConfigProvider(),
				new TatryMapEventConfigProvider()));
	}

	static Map<String, MapEventConfig> createConfigs(final Iterable<MapEventConfigProvider> providers) {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>();
		final Map<String, String> ownerProvidersByEventId = new LinkedHashMap<>();

		for (MapEventConfigProvider provider : providers) {
			final String providerName = providerName(provider);
			for (Map.Entry<String, MapEventConfig> entry : provider.loadConfigs().entrySet()) {
				final String eventId = entry.getKey();
				final String previousOwner = ownerProvidersByEventId.putIfAbsent(eventId, providerName);
				if (previousOwner != null) {
					final String errorMessage = "Duplicate map event eventId='" + eventId
							+ "' declared by providers '" + previousOwner + "' and '" + providerName
							+ "'. Event registry initialization aborted.";
					LOGGER.error(errorMessage);
					throw new IllegalStateException(errorMessage);
				}
				configs.put(eventId, entry.getValue());
			}
		}

		return Collections.unmodifiableMap(configs);
	}

	private static String providerName(final MapEventConfigProvider provider) {
		final String simpleName = provider.getClass().getSimpleName();
		return simpleName.isEmpty() ? provider.getClass().getName() : simpleName;
	}
}
