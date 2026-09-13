package games.stendhal.server.script;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import games.stendhal.server.core.config.ProductionGroupsXMLLoader;
import games.stendhal.server.core.config.ShopGroupsXMLLoader;
import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.EntityManager;
import games.stendhal.server.core.rule.defaultruleset.DefaultEntityManager;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import games.stendhal.server.entity.npc.behaviour.journal.MerchantsRegister;
import games.stendhal.server.entity.npc.behaviour.journal.ProducerRegister;
import games.stendhal.server.entity.npc.shop.ShopType;
import games.stendhal.server.entity.npc.shop.ShopsList;
import marauroa.common.resource.Reloadable;
import marauroa.common.resource.ResourceProvider;
import marauroa.common.resource.ResourceReloadService;

public class ReloadCreaturesAndItems extends ScriptImpl {
	private static final Logger LOGGER = Logger.getLogger(ReloadCreaturesAndItems.class);

	private static final ResourceReloadService RELOAD_SERVICE;
	private static final Reloadable RELOADABLE;
	private static final String CREATURES_INDEX = "data/conf/creatures.xml";
	private static final String ITEMS_INDEX = "data/conf/items.xml";

	static {
		ResourceReloadService service = null;
		Reloadable reloadable = null;
		try {
			service = ResourceReloadService.getInstance();
			reloadable = new CreaturesAndItemsReloadable();
			service.register(reloadable);
			LOGGER.info("Registered hot reload handler for creature and item definitions.");
		} catch (final NoClassDefFoundError e) {
			LOGGER.info("Resource reload API not available; falling back to immediate reloads.");
			service = null;
			reloadable = null;
		} catch (final Exception e) {
			LOGGER.error("Unable to register reload handler", e);
			service = null;
			reloadable = null;
		}
		RELOAD_SERVICE = service;
		RELOADABLE = reloadable;
	}

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);
		if (RELOAD_SERVICE != null && RELOADABLE != null) {
			try {
				RELOAD_SERVICE.requestReload(RELOADABLE);
				admin.sendPrivateText("Reload of creature and item definitions queued.");
				return;
			} catch (final Exception e) {
				LOGGER.error("Failed to queue reload via ResourceReloadService", e);
				admin.sendPrivateText("Queuing reload failed; attempting immediate reload.");
			}
		}
		reloadNow(admin);
	}

	private static void reloadNow(final Player admin) {
		try {
			performReload();
			if (admin != null) {
				admin.sendPrivateText("Creature and item definitions reloaded immediately.");
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to reload creature and item definitions", e);
			if (admin != null) {
				admin.sendPrivateText("Reload failed: " + e.getMessage());
			}
		}
	}

	private static void performReload() throws Exception {
		touchResourcesFromFileSystem();
		resetShopCaches();
		resetProductionCaches();
		final DefaultEntityManager refreshed = new DefaultEntityManager();
		try {
			final Method setter = SingletonRepository.class.getDeclaredMethod("setEntityManager", EntityManager.class);
			setter.setAccessible(true);
			setter.invoke(null, refreshed);
		} catch (final ReflectiveOperationException reflectionFailure) {
			final Field repoField = SingletonRepository.class.getDeclaredField("entityManager");
			repoField.setAccessible(true);
			repoField.set(null, refreshed);
		}
		LOGGER.info("Entity manager refreshed using current configuration.");
	}

	private static void resetShopCaches() throws Exception {
		resetLoaderGuard(ShopGroupsXMLLoader.class);
		final ShopsList shops = SingletonRepository.getShopsList();
		clearShopContents(shops, ShopType.ITEM_SELL);
		clearShopContents(shops, ShopType.ITEM_BUY);
		clearShopContents(shops, ShopType.TRADE);
		SingletonRepository.getOutfitShopsList().getContents().clear();
		final MerchantsRegister merchants = SingletonRepository.getMerchantsRegister();
		merchants.getBuyers().clear();
		merchants.getSellers().clear();
	}

	private static void clearShopContents(final ShopsList shops, final ShopType type) {
		if (shops.getContents(type) != null) {
			shops.getContents(type).clear();
		}
	}

	private static void resetProductionCaches() throws Exception {
		resetLoaderGuard(ProductionGroupsXMLLoader.class);
		final ProducerRegister register = SingletonRepository.getProducerRegister();
		register.getProducers().clear();
		register.getMultiProducers().clear();
	}

	private static void resetLoaderGuard(final Class<?> loaderClass) throws Exception {
		final Field field = loaderClass.getDeclaredField("loaded");
		field.setAccessible(true);
		field.setBoolean(null, false);
	}

	private static final class CreaturesAndItemsReloadable implements Reloadable {
		@Override
		public String resourcePath() {
			return CREATURES_INDEX;
		}

		@Override
		public void reload(final ResourceProvider provider) throws Exception {
			touchResourcesFromProvider(provider);
			performReload();
		}
	}

	private static void touchResourcesFromProvider(final ResourceProvider provider) throws Exception {
		final Set<String> resources = new LinkedHashSet<String>();
		resources.addAll(readGroupResources(provider, CREATURES_INDEX));
		resources.addAll(readGroupResources(provider, ITEMS_INDEX));
		for (final String resource : resources) {
			try (InputStream ignored = provider.open(resource)) {
				// opening the resource is enough to ensure the provider refreshes it
			}
		}
	}

	private static Set<String> readGroupResources(final ResourceProvider provider, final String indexResource) throws Exception {
		final Set<String> resources = new LinkedHashSet<String>();
		resources.add(normalizeResource(indexResource));
		final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);
		final DocumentBuilder builder = factory.newDocumentBuilder();
		try (InputStream in = provider.open(indexResource)) {
			final Document doc = builder.parse(in);
			final NodeList nodes = doc.getElementsByTagNameNS("*", "group");
			for (int i = 0; i < nodes.getLength(); i++) {
				final Element element = (Element) nodes.item(i);
				final String uri = element.getAttribute("uri");
				if (uri == null || uri.isEmpty()) {
					continue;
				}
				final String resolved = resolveRelativePath(indexResource, uri);
				resources.add(resolved);
			}
		}
		return resources;
	}

	private static String resolveRelativePath(final String indexResource, final String uri) {
		final Path base = Paths.get(indexResource).getParent();
		final Path resolved = (base == null ? Paths.get(uri) : base.resolve(uri)).normalize();
		return normalizeResource(resolved.toString());
	}

	private static String normalizeResource(final String resource) {
		return resource.replace('\\', '/');
	}

	private static String normalizeResource(final Path resource) {
		return normalizeResource(resource.normalize().toString());
	}

	private static void touchResourcesFromFileSystem() {
		final Set<Path> resources = new LinkedHashSet<Path>();
		resources.add(Paths.get(CREATURES_INDEX));
		resources.add(Paths.get(ITEMS_INDEX));
		resources.addAll(listXmlFiles(Paths.get(CREATURES_INDEX).getParent()));
		resources.addAll(listXmlFiles(Paths.get(ITEMS_INDEX).getParent()));
		for (final Path path : resources) {
			if (path == null) {
				continue;
			}
			if (Files.isRegularFile(path)) {
				try (InputStream ignored = Files.newInputStream(path)) {
					// best effort to ensure the JVM notices file timestamp updates
				} catch (final Exception e) {
					LOGGER.debug("Unable to touch resource on filesystem: " + path, e);
				}
			}
		}
	}

	private static List<Path> listXmlFiles(final Path directory) {
		if (directory == null || !Files.isDirectory(directory)) {
			return new ArrayList<Path>();
		}
		try (final java.util.stream.Stream<Path> stream = Files.list(directory)) {
			final List<Path> files = new ArrayList<Path>();
			stream.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".xml"))
			.forEach(files::add);
			return files;
		} catch (final Exception e) {
			LOGGER.debug("Unable to list XML files under " + directory, e);
			return new ArrayList<Path>();
		}
	}
}
