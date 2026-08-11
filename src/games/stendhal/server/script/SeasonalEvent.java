/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.events.seasonal.SeasonalEventService;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Administrative runtime control for seasonal events.
 *
 * Usage:
 * <pre>
 * /script SeasonalEvent.class christmas on
 * /script SeasonalEvent.class christmas off
 * /script SeasonalEvent.class christmas status
 * </pre>
 *
 * {@code xmas} is accepted as an alias for {@code christmas}.
 */
public class SeasonalEvent extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() != 2) {
			usage(admin);
			return;
		}

		final String event = args.get(0).trim().toLowerCase();
		final String action = args.get(1).trim().toLowerCase();
		if (!("christmas".equals(event) || "xmas".equals(event))) {
			admin.sendPrivateText("Nieznany event: " + args.get(0)
					+ ". Obecnie obsługiwany jest christmas (alias: xmas).");
			return;
		}

		final SeasonalEventService service = SeasonalEventService.get();
		if ("status".equals(action)) {
			admin.sendPrivateText("Christmas: "
					+ (service.isChristmasEnabled() ? "AKTYWNY" : "WYŁĄCZONY")
					+ (service.isTransitionInProgress() ? " (trwa przełączanie)" : "")
					+ "; warunek: stendhal.christmas");
			return;
		}

		final Boolean target = parseState(action);
		if (target == null) {
			usage(admin);
			return;
		}

		final boolean accepted = service.requestChristmas(target.booleanValue(),
				new SeasonalEventService.ResultListener() {
					@Override
					public void onResult(final boolean success, final String message) {
						admin.sendPrivateText((success ? "[OK] " : "[BŁĄD] ") + message);
					}
				});
		if (!accepted) {
			admin.sendPrivateText("Inny event sezonowy jest właśnie przełączany. Spróbuj ponownie po zakończeniu operacji.");
			return;
		}

		if (service.isTransitionInProgress()) {
			admin.sendPrivateText("Przygotowuję wariant Christmas=" + target
					+ " poza wątkiem RP. Zmiana zostanie zastosowana po pełnej walidacji zasobów.");
		}
	}

	private static Boolean parseState(final String value) {
		if ("on".equals(value) || "true".equals(value)
				|| "wlacz".equals(value) || "włącz".equals(value)) {
			return Boolean.TRUE;
		}
		if ("off".equals(value) || "false".equals(value)
				|| "wylacz".equals(value) || "wyłącz".equals(value)) {
			return Boolean.FALSE;
		}
		return null;
	}

	private static void usage(final Player admin) {
		admin.sendPrivateText("Użycie: /script SeasonalEvent.class {christmas|xmas} {on|off|status}");
	}
}
