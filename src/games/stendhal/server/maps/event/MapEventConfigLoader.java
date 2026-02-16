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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.maps.dragon.DragonMapEventConfigProvider;
import games.stendhal.server.maps.kikareukin.KikareukinMapEventConfigProvider;
import games.stendhal.server.maps.koscielisko.KoscieliskoMapEventConfigProvider;

public final class MapEventConfigLoader {
	private static final Logger LOGGER = Logger.getLogger(MapEventConfigLoader.class);

	/**
	 * Naming convention for globally unique event IDs:
	 * <ul>
	 * <li>{@code dragon_*} for Dragon Land events</li>
	 * <li>{@code koscielisko_*} for Kościelisko events</li>
	 * <li>{@code kikareukin_*} for Kikareukin events</li>
	 * </ul>
	 *
	 * Prefixing IDs by domain helps avoid collisions between independent providers.
	 */
	public static final String DRAGON_LAND_DEFAULT = DragonMapEventConfigProvider.DRAGON_LAND_DEFAULT;
	public static final String KIKAREUKIN_ANGEL_PREVIEW = KikareukinMapEventConfigProvider.KIKAREUKIN_ANGEL_PREVIEW;
	public static final String KOSCIELISKO_GIANT_ESCORT = KoscieliskoMapEventConfigProvider.KOSCIELISKO_GIANT_ESCORT;
	private static final String VALIDATION_MODE_PROPERTY = "mapevent.validation.mode";

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

	static ValidationMode configuredValidationMode() {
		return ValidationMode.fromSystemProperty(System.getProperty(VALIDATION_MODE_PROPERTY));
	}

	private static Map<String, MapEventConfig> createConfigs() {
		return createConfigs(Arrays.asList(
				new DragonMapEventConfigProvider(),
				new KoscieliskoMapEventConfigProvider(),
				new KikareukinMapEventConfigProvider()));
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

	static Map<String, MapEventConfig> createConfigs(final Iterable<MapEventConfigProvider> providers,
			final ValidationMode validationMode,
			final ValidationContext validationContext) {
		final Map<String, MapEventConfig> configs = new LinkedHashMap<>(createConfigs(providers));
		final Set<String> invalidConfigIds = applyValidationResult(validateConfigs(configs, validationContext), validationMode);
		for (String invalidConfigId : invalidConfigIds) {
			configs.remove(invalidConfigId);
		}
		return Collections.unmodifiableMap(configs);
	}

	static Set<String> validateLoadedConfigs(final ValidationMode validationMode) {
		final ValidationResult validationResult = validateConfigs(CONFIGS, SingletonValidationContext.INSTANCE);
		return applyValidationResult(validationResult, validationMode);
	}

	private static Set<String> applyValidationResult(final ValidationResult validationResult,
			final ValidationMode validationMode) {
		if (!validationResult.hasErrors()) {
			return Collections.emptySet();
		}

		if (validationMode == ValidationMode.STRICT) {
			throw new IllegalStateException("Map event config validation failed:" + validationResult.toMultilineMessage());
		}

		LOGGER.error("Map event config validation failed in permissive mode. Invalid events were disabled:"
				+ validationResult.toMultilineMessage());
		return validationResult.getInvalidEventIds();
	}

	private static ValidationResult validateConfigs(final Map<String, MapEventConfig> configs,
			final ValidationContext validationContext) {
		final ValidationResult result = new ValidationResult();
		for (Map.Entry<String, MapEventConfig> entry : configs.entrySet()) {
			final String eventId = entry.getKey();
			final MapEventConfig config = entry.getValue();

			for (String zoneName : config.getZones()) {
				if (!validationContext.zoneExists(zoneName)) {
					result.add(eventId, "missing zone in getZones(): '" + zoneName + "'");
				}
			}
			for (String observerZoneName : config.getObserverZones()) {
				if (!validationContext.zoneExists(observerZoneName)) {
					result.add(eventId, "missing zone in getObserverZones(): '" + observerZoneName + "'");
				}
			}

			if (config.getWaves().isEmpty()) {
				result.add(eventId, "no waves defined");
			}

			for (BaseMapEvent.EventWave wave : config.getWaves()) {
				for (BaseMapEvent.EventSpawn spawn : wave.getSpawns()) {
					final String creatureName = spawn.getCreatureName();
					if (!validationContext.creatureTemplateExists(creatureName)) {
						result.add(eventId, "missing creature template in wave spawn: '" + creatureName + "'");
					}
				}
			}
		}

		return result;
	}

	private static String providerName(final MapEventConfigProvider provider) {
		final String simpleName = provider.getClass().getSimpleName();
		return simpleName.isEmpty() ? provider.getClass().getName() : simpleName;
	}

	public enum ValidationMode {
		STRICT,
		PERMISSIVE;

		static ValidationMode fromSystemProperty(final String rawMode) {
			if (rawMode == null) {
				return STRICT;
			}
			if ("permissive".equalsIgnoreCase(rawMode.trim())) {
				return PERMISSIVE;
			}
			return STRICT;
		}
	}

	interface ValidationContext {
		boolean zoneExists(String zoneName);

		boolean creatureTemplateExists(String creatureName);
	}

	private enum SingletonValidationContext implements ValidationContext {
		INSTANCE;

		@Override
		public boolean zoneExists(final String zoneName) {
			return SingletonRepository.getRPWorld().getZone(zoneName) != null;
		}

		@Override
		public boolean creatureTemplateExists(final String creatureName) {
			return SingletonRepository.getEntityManager().getCreature(creatureName) != null;
		}
	}

	private static final class ValidationResult {
		private final Map<String, List<String>> errorsByEventId = new LinkedHashMap<>();

		void add(final String eventId, final String error) {
			errorsByEventId.computeIfAbsent(eventId, ignored -> new ArrayList<>()).add(error);
		}

		boolean hasErrors() {
			return !errorsByEventId.isEmpty();
		}

		Set<String> getInvalidEventIds() {
			return new LinkedHashSet<>(errorsByEventId.keySet());
		}

		String toMultilineMessage() {
			final StringBuilder messageBuilder = new StringBuilder();
			for (Map.Entry<String, List<String>> entry : errorsByEventId.entrySet()) {
				messageBuilder.append("\n - eventId='").append(entry.getKey()).append("':");
				for (String error : entry.getValue()) {
					messageBuilder.append("\n   * ").append(error);
				}
			}
			return messageBuilder.toString();
		}
	}
}
