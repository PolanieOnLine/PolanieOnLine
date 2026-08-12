/***************************************************************************
 *                   Copyright © 2026 - PolanieOnLine                      *
 ***************************************************************************/
package games.stendhal.server.script;

import java.util.List;

import games.stendhal.server.core.events.seasonal.SeasonalEventService;
import games.stendhal.server.core.events.seasonal.SeasonalEventType;
import games.stendhal.server.core.scripting.ScriptImpl;
import games.stendhal.server.entity.player.Player;

/**
 * Administrative runtime control for seasonal events.
 *
 * Usage:
 * <pre>
 * /script SeasonalEvent.class christmas on
 * /script SeasonalEvent.class minetown on
 * /script SeasonalEvent.class easter on
 * /script SeasonalEvent.class easter off
 * /script SeasonalEvent.class easter status
 * </pre>
 */
public class SeasonalEvent extends ScriptImpl {
	@Override
	public void execute(final Player admin, final List<String> args) {
		if (args.size() != 2) {
			usage(admin);
			return;
		}

		final SeasonalEventType event = SeasonalEventType.parse(args.get(0));
		final String action = args.get(1).trim().toLowerCase();
		if (event == null) {
			admin.sendPrivateText("Nieznany event: " + args.get(0)
					+ ". Obsługiwane są christmas (xmas), minetown (mine-town, revival) oraz easter.");
			return;
		}

		final SeasonalEventService service = SeasonalEventService.get();
		if ("status".equals(action)) {
			admin.sendPrivateText(event.getDisplayName() + ": "
					+ (event.isEnabled(service) ? "AKTYWNY" : "WYŁĄCZONY")
					+ transitionSuffix(service)
					+ "; warunek: " + event.getProperty());
			return;
		}

		final Boolean target = parseState(action);
		if (target == null) {
			usage(admin);
			return;
		}

		final SeasonalEventService.ResultListener listener =
				new SeasonalEventService.ResultListener() {
					@Override
					public void onResult(final boolean success, final String message) {
						admin.sendPrivateText((success ? "[OK] " : "[BŁĄD] ") + message);
					}
				};

		final boolean accepted = event.request(service, target.booleanValue(), listener);
		if (!accepted) {
			admin.sendPrivateText("Inny event sezonowy jest właśnie przełączany. Spróbuj ponownie po zakończeniu operacji.");
			return;
		}

		if (service.isTransitionInProgress()) {
			admin.sendPrivateText("Przygotowuję wariant " + event.getCommandName() + "=" + target
					+ " poza wątkiem RP. Zmiana zostanie zastosowana po pełnej walidacji zasobów.");
		}
	}

	private static String transitionSuffix(final SeasonalEventService service) {
		return service.isTransitionInProgress() ? " (trwa przełączanie)" : "";
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
		admin.sendPrivateText("Użycie: /script SeasonalEvent.class {christmas|xmas|minetown|mine-town|revival|easter} {on|off|status}");
	}
}
