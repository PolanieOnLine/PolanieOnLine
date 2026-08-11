/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.core.events.seasonal;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import games.stendhal.common.filter.FilterCriteria;
import games.stendhal.common.tiled.StendhalMapStructure;
import games.stendhal.server.core.config.XMLUtil;
import games.stendhal.server.core.config.ZoneGroupsXMLLoader;
import games.stendhal.server.core.config.zone.ConfiguratorDescriptor;
import games.stendhal.server.core.config.zone.ConfiguratorXMLReader;
import games.stendhal.server.core.config.zone.EntitySetupDescriptor;
import games.stendhal.server.core.config.zone.EntitySetupXMLReader;
import games.stendhal.server.core.config.zone.ZoneMapUpdater;
import games.stendhal.server.core.config.zone.ZoneMapUpdater.MapUpdatePlan;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.engine.StendhalRPZone;
import games.stendhal.server.core.engine.ZoneAttributes;
import games.stendhal.server.core.rp.WeatherUpdater;
import games.stendhal.server.entity.Entity;
import games.stendhal.server.entity.mapstuff.WeatherEntity;
import games.stendhal.server.entity.npc.SpeakerNPC;

/**
 * Detached, validated runtime changes for zones depending on
 * {@code stendhal.christmas}.
 */
final class ChristmasZonePlan {
	private static final String PROPERTY = "stendhal.christmas";
	private static final URI ZONE_GROUPS = URI.create("/data/conf/zones.xml");

	private final List<ZoneChange> changes;

	private ChristmasZonePlan(final List<ZoneChange> changes) {
		this.changes = changes;
	}

	static ChristmasZonePlan prepare(final boolean enabled) throws Exception {
		try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, enabled)) {
			final List<ZoneChange> changes = new ArrayList<ZoneChange>();
			final ZoneGroupsXMLLoader groupsLoader = new ZoneGroupsXMLLoader(ZONE_GROUPS);
			for (final URI resource : groupsLoader.getZoneGroups()) {
				prepareFile(resource.getPath(), enabled, changes);
			}
			if (changes.isEmpty()) {
				throw new IllegalStateException("No zones depend on " + PROPERTY);
			}
			return new ChristmasZonePlan(changes);
		}
	}

	void apply() throws Exception {
		for (final ZoneChange change : changes) {
			change.apply();
		}
	}

	private static void prepareFile(final String resource, final boolean enabled,
			final List<ZoneChange> changes) throws Exception {
		final InputStream stream = ChristmasZonePlan.class.getResourceAsStream(resource);
		if (stream == null) {
			throw new IllegalStateException("Missing zone configuration " + resource);
		}
		try {
			final Document document = XMLUtil.parse(stream);
			for (final Element zone : XMLUtil.getElements(document.getDocumentElement(), "zone")) {
				if (dependsOnProperty(zone)) {
					changes.add(ZoneChange.prepare(zone, enabled));
				}
			}
		} finally {
			stream.close();
		}
	}

	private static boolean dependsOnProperty(final Element element) {
		if (usesProperty(element.getAttribute("condition"))) {
			return true;
		}
		for (final Element child : XMLUtil.getElements(element)) {
			if (dependsOnProperty(child)) {
				return true;
			}
		}
		return false;
	}

	private static boolean usesProperty(final String condition) {
		if (condition == null) {
			return false;
		}
		final String value = condition.trim();
		return PROPERTY.equals(value) || ("!" + PROPERTY).equals(value);
	}

	private static boolean hasConditionalParameter(final Element zone,
			final String name) {
		for (final Element attributes : XMLUtil.getElements(zone, "attributes")) {
			for (final Element parameter : XMLUtil.getElements(attributes, "parameter")) {
				if (name.equals(parameter.getAttribute("name"))
						&& usesProperty(parameter.getAttribute("condition"))) {
					return true;
				}
			}
		}
		return false;
	}

	private static Map<String, String> readActiveAttributes(final Element zone) {
		final Map<String, String> attributes = new HashMap<String, String>();
		for (final Element block : XMLUtil.getElements(zone, "attributes")) {
			if (!XMLUtil.checkCondition(block.getAttribute("condition"))) {
				continue;
			}
			for (final Element parameter : XMLUtil.getElements(block, "parameter")) {
				if (XMLUtil.checkCondition(parameter.getAttribute("condition"))
						&& parameter.hasAttribute("name")) {
					attributes.put(parameter.getAttribute("name"),
							XMLUtil.getText(parameter).trim());
				}
			}
		}
		return attributes;
	}

	private static String readActiveFile(final Element zone) {
		String file = zone.getAttribute("file");
		for (final Element block : XMLUtil.getElements(zone, "attributes")) {
			if (!XMLUtil.checkCondition(block.getAttribute("condition"))) {
				continue;
			}
			for (final Element parameter : XMLUtil.getElements(block, "parameter")) {
				if ("file".equals(parameter.getAttribute("name"))
						&& XMLUtil.checkCondition(parameter.getAttribute("condition"))) {
					file = XMLUtil.getText(parameter).trim();
				}
			}
		}
		return file;
	}

	private static void validateSupportedConditions(final Element zone) {
		if (usesProperty(zone.getAttribute("condition"))) {
			throw new IllegalStateException("Conditional whole-zone lifecycle is not supported for "
					+ zone.getAttribute("name"));
		}
		for (final Element child : XMLUtil.getElements(zone)) {
			final String tag = child.getTagName();
			if (usesProperty(child.getAttribute("condition"))
					&& !("attributes".equals(tag) || "entity".equals(tag)
							|| "configurator".equals(tag))) {
				throw new IllegalStateException("Unsupported conditional element <"
						+ tag + "> in zone " + zone.getAttribute("name"));
			}
			if ("configurator".equals(tag) && dependsOnProperty(child)
					&& !usesProperty(child.getAttribute("condition"))) {
				throw new IllegalStateException("Conditional configurator parameters are not yet supported in zone "
						+ zone.getAttribute("name"));
			}
		}
	}

	private static final class ZoneChange {
		private final String zoneName;
		private final MapUpdatePlan mapUpdate;
		private final Map<String, String> attributes;
		private final List<ConditionalEntity> entities;
		private final List<ConditionalConfigurator> configurators;

		private ZoneChange(final String zoneName, final MapUpdatePlan mapUpdate,
				final Map<String, String> attributes,
				final List<ConditionalEntity> entities,
				final List<ConditionalConfigurator> configurators) {
			this.zoneName = zoneName;
			this.mapUpdate = mapUpdate;
			this.attributes = attributes;
			this.entities = entities;
			this.configurators = configurators;
		}

		static ZoneChange prepare(final Element zone, final boolean enabled) throws Exception {
			validateSupportedConditions(zone);
			final String zoneName = zone.getAttribute("name");
			MapUpdatePlan mapUpdate = null;
			if (hasConditionalParameter(zone, "file")) {
				final String targetFile = readActiveFile(zone);
				final StendhalMapStructure targetMap = ZoneMapUpdater.prepare(targetFile);

				final String referenceFile;
				try (XMLUtil.ConditionOverride ignored = XMLUtil.overrideCondition(PROPERTY, !enabled)) {
					referenceFile = readActiveFile(zone);
				}
				final StendhalMapStructure referenceMap = ZoneMapUpdater.prepare(referenceFile);
				mapUpdate = ZoneMapUpdater.prepareMapUpdate(
						referenceMap, targetMap, referenceFile, targetFile);
			}
			final Map<String, String> attributes = hasConditionalParameter(zone, "weather")
					? readActiveAttributes(zone) : null;

			final List<ConditionalEntity> entities = new ArrayList<ConditionalEntity>();
			final EntitySetupXMLReader entityReader = new EntitySetupXMLReader();
			final List<ConditionalConfigurator> configurators =
					new ArrayList<ConditionalConfigurator>();
			final ConfiguratorXMLReader configuratorReader = new ConfiguratorXMLReader();

			for (final Element child : XMLUtil.getElements(zone)) {
				final String condition = child.getAttribute("condition");
				if (!usesProperty(condition)) {
					continue;
				}
				if ("entity".equals(child.getTagName())) {
					final EntitySetupDescriptor descriptor =
							(EntitySetupDescriptor) entityReader.read(child);
					if (descriptor == null || descriptor.getImplementation() == null) {
						throw new IllegalStateException("Invalid conditional entity in " + zoneName);
					}
					entities.add(new ConditionalEntity(descriptor,
							Class.forName(descriptor.getImplementation()),
							XMLUtil.checkCondition(condition)));
				} else if ("configurator".equals(child.getTagName())) {
					final String className = child.getAttribute("class-name");
					if (!"games.stendhal.server.maps.semos.city.RudolphNPC".equals(className)) {
						throw new IllegalStateException("No runtime cleanup registered for conditional configurator "
								+ className);
					}
					final ConfiguratorDescriptor descriptor =
							(ConfiguratorDescriptor) configuratorReader.read(child);
					configurators.add(new ConditionalConfigurator(descriptor,
							XMLUtil.checkCondition(condition)));
				}
			}

			return new ZoneChange(zoneName, mapUpdate,
					attributes, entities, configurators);
		}

		void apply() throws Exception {
			final StendhalRPZone zone = SingletonRepository.getRPWorld().getZone(zoneName);
			if (zone == null) {
				throw new IllegalStateException("Zone is not loaded: " + zoneName);
			}

			if (mapUpdate != null && !mapUpdate.isEmpty()) {
				mapUpdate.apply(zone);
			}
			if (attributes != null) {
				applyAttributes(zone, attributes);
			}
			for (final ConditionalEntity entity : entities) {
				entity.removeExisting(zone);
			}
			for (final ConditionalEntity entity : entities) {
				entity.setupIfActive(zone);
			}
			for (final ConditionalConfigurator configurator : configurators) {
				configurator.apply(zone);
			}
			zone.notifyOnlinePlayers();
		}
	}

	private static void applyAttributes(final StendhalRPZone zone,
			final Map<String, String> values) {
		final ZoneAttributes attributes = zone.getAttributes();
		WeatherUpdater.get().unmanageAttributes(attributes);
		removeRemainingWeatherEntities(zone);

		final String weather = values.get("weather");
		if (weather == null || weather.trim().isEmpty()) {
			attributes.remove("weather");
		} else {
			attributes.put("weather", weather);
		}
	}

	private static void removeRemainingWeatherEntities(final StendhalRPZone zone) {
		final List<Entity> weatherEntities = new ArrayList<Entity>(
				zone.getFilteredEntities(new FilterCriteria<Entity>() {
					@Override
					public boolean passes(final Entity entity) {
						return entity instanceof WeatherEntity;
					}
				}));
		for (final Entity weatherEntity : weatherEntities) {
			zone.remove(weatherEntity);
		}
	}

	private static final class ConditionalEntity {
		private final EntitySetupDescriptor descriptor;
		private final Class<?> implementation;
		private final boolean active;

		private ConditionalEntity(final EntitySetupDescriptor descriptor,
				final Class<?> implementation, final boolean active) {
			this.descriptor = descriptor;
			this.implementation = implementation;
			this.active = active;
		}

		void removeExisting(final StendhalRPZone zone) {
			final List<Entity> existing = new ArrayList<Entity>(
					zone.getEntitiesAt(descriptor.getX(), descriptor.getY()));
			for (final Entity entity : existing) {
				if (implementation.isInstance(entity)) {
					zone.remove(entity);
				}
			}
		}

		void setupIfActive(final StendhalRPZone zone) {
			if (active) {
				descriptor.setup(zone);
			}
		}
	}

	private static final class ConditionalConfigurator {
		private final ConfiguratorDescriptor descriptor;
		private final boolean active;

		private ConditionalConfigurator(final ConfiguratorDescriptor descriptor,
				final boolean active) {
			this.descriptor = descriptor;
			this.active = active;
		}

		void apply(final StendhalRPZone zone) {
			final SpeakerNPC rudolph = SingletonRepository.getNPCList().get("Rudolph");
			if (rudolph != null) {
				zone.remove(rudolph);
			}
			if (active) {
				descriptor.setup(zone);
			}
		}
	}
}
