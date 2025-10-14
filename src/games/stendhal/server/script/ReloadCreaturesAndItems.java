package games.stendhal.server.script;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.log4j.Logger;

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
			return "data/conf";
		}

		@Override
		public void reload(final ResourceProvider provider) throws Exception {
			performReload();
		}
	}
}
