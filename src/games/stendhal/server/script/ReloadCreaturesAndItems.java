package games.stendhal.server.script;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.log4j.Logger;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.rule.defaultruleset.DefaultEntityManager;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import marauroa.server.game.resource.Reloadable;
import marauroa.server.game.resource.ResourceProvider;
import marauroa.server.game.resource.ResourceReloadService;

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
		final DefaultEntityManager refreshed = new DefaultEntityManager();
		final Field field = SingletonRepository.class.getDeclaredField("entityManager");
		field.setAccessible(true);
		field.set(null, refreshed);
		LOGGER.info("Entity manager refreshed using current configuration.");
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
