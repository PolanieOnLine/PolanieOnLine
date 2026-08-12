/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import games.stendhal.server.core.engine.SingletonRepository;
import games.stendhal.server.core.events.TurnListener;
import games.stendhal.server.core.events.TurnNotifier;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;
import marauroa.common.resource.ResourceReloadService;

/**
 * Admin adapter for resources registered in Marauroa ResourceReloadService.
 *
 * The script accepts only stable registered resource ids. Loading and
 * validation are executed on a dedicated control worker so XML and I/O never
 * run inside the RP turn. Applying the prepared candidate remains owned by the
 * Marauroa safe point between turns.
 */
public class ResourceReload extends ScriptImpl {
	private static final ExecutorService WORKER = Executors.newSingleThreadExecutor(new ThreadFactory() {
		@Override
		public Thread newThread(final Runnable runnable) {
			final Thread thread = new Thread(runnable, "ResourceReloadPrepare");
			thread.setDaemon(true);
			return thread;
		}
	});

	@Override
	public void execute(final Player admin, final List<String> args) {
		super.execute(admin, args);

		final ResourceReloadService service = ResourceReloadService.getInstance();
		if (args.size() == 1 && "list".equalsIgnoreCase(args.get(0))) {
			final Set<String> ids = service.getRegisteredResourceIds();
			admin.sendPrivateText("Zarejestrowane zasoby reloadu: " + ids
					+ ". Oczekujący kandydat: " + (service.hasPendingReloads() ? "tak" : "nie") + ".");
			return;
		}

		if (args.size() != 2 || !"reload".equalsIgnoreCase(args.get(0))) {
			admin.sendPrivateText("Użycie: /script ResourceReload.class list lub "
					+ "/script ResourceReload.class reload <resource-id>.");
			return;
		}

		final String resourceId = args.get(1);
		if (!service.getRegisteredResourceIds().contains(resourceId)) {
			admin.sendPrivateText("Nieznane resource-id '" + resourceId + "'. Dostępne: "
					+ service.getRegisteredResourceIds() + ".");
			return;
		}

		final String adminName = admin.getName();
		admin.sendPrivateText("Przygotowuję zasób '" + resourceId
				+ "' poza wątkiem RP. Aktywny stan nie zostanie zmieniony przed pełną walidacją.");

		WORKER.execute(new Runnable() {
			@Override
			public void run() {
				final boolean prepared = service.requestReload(resourceId);
				if (prepared) {
					notifyAdmin(adminName, "Zasób '" + resourceId
							+ "' został przygotowany i zwalidowany. Zostanie zastosowany na najbliższym safe poincie.");
				} else {
					notifyAdmin(adminName, "Nie udało się przygotować zasobu '" + resourceId
							+ "'. Aktywny stan pozostał bez zmian. Szczegóły znajdują się w logu serwera.");
				}
			}
		});
	}

	private static void notifyAdmin(final String adminName, final String message) {
		TurnNotifier.get().notifyInTurns(0, new TurnListener() {
			@Override
			public void onTurnReached(final int currentTurn) {
				final Player currentAdmin = SingletonRepository.getRuleProcessor().getPlayer(adminName);
				if (currentAdmin != null) {
					currentAdmin.sendPrivateText(message);
				}
			}
		});
	}
}
